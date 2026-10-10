# 🔌 HƯỚNG DẪN KẾT NỐI MODEL AI VÀO WEB

## 1. Kiến trúc kết nối

```
Trình duyệt ──(AJAX /chat/send)──► Spring Boot :8081 ──(HTTP POST /chat)──► FastAPI AI :8000
                                        │                                      │
                                        │◄──────── JSON kết quả phân loại ──────┘
                                        ▼
                                   PostgreSQL (lưu tin nhắn, phát phiếu)
```

- Trình duyệt **không** gọi AI trực tiếp, mà đi qua Spring Boot (không cần cấu hình CORS).
- Spring Boot đọc địa chỉ AI từ `application.properties`:
  ```properties
  ai.service.url=http://localhost:8000
  ```

---

## 2. Hợp đồng API — hội thoại nhiều lượt (theo thiết kế của nhóm)

AI **hỏi thêm** (thời điểm khởi phát, mức khó chịu 1–10, tuổi, dấu hiệu cảnh báo) trước khi kết luận.
Vì vậy mỗi lượt chat có 2 trạng thái:

```
Lượt 1..n-1 : AI đang hỏi thêm      → has_triage_result = false  → web chỉ hiện bong bóng chat
Lượt n      : AI đủ thông tin       → has_triage_result = true   → web hiện thẻ KẾT QUẢ + nút Nhận phiếu
```

**Request** — Spring Boot gửi **kèm lịch sử hội thoại** (AI không cần tự lưu trạng thái,
khởi động lại service AI không mất phiên):
```http
POST http://localhost:8000/chat
Content-Type: application/json

{
  "session_id": "uuid-phien-chat",
  "patient": { "name": "Nguyen Nguyen", "age": 20, "gender": "MALE" },
  "history": [
    { "role": "bot",  "content": "Bạn đang gặp vấn đề sức khỏe gì hôm nay?" },
    { "role": "user", "content": "Sốt cao 39 độ" },
    { "role": "bot",  "content": "Sốt xuất hiện từ khi nào, mức khó chịu 1–10? Bạn bao nhiêu tuổi?" },
    { "role": "user", "content": "6 và 20 tuổi" }
  ],
  "message": "có"
}
```

**Response A — đang hỏi thêm:**
```json
{
  "reply": "Cảm ơn bạn... Bạn có kèm khó thở, đau ngực, lơ mơ, co giật hay phát ban không?",
  "has_triage_result": false,
  "intent": "SYMPTOM"
}
```

**Response B — đã phân loại xong** (tương ứng thẻ kết quả trong ảnh thiết kế):
```json
{
  "reply": "Bạn đang có **dấu hiệu cảnh báo kèm sốt cao**, nên tôi phân loại **khẩn cấp**...",
  "has_triage_result": true,
  "intent": "SYMPTOM",
  "triage": {
    "department_code": "KCAPCUU",
    "department_name": "Cấp cứu",
    "priority": "M2",
    "confidence": 88,
    "summary": "20 tuổi, sốt cao 39°C, khó chịu 6/10 và có kèm ít nhất một dấu hiệu cảnh báo...",
    "red_flags": ["Khó thở", "Đau ngực", "Lơ mơ", "Co giật", "Phát ban"],
    "advice": "Đến khoa Cấp cứu ngay để được đánh giá sớm; không tự trì hoãn theo dõi tại nhà."
  }
}
```

| Trường | Bắt buộc | Ý nghĩa / dùng ở đâu trên web |
|---|---|---|
| `reply` | ✅ | Bong bóng chat. Được dùng `**đậm**` (web sẽ render markdown) |
| `has_triage_result` | ✅ | `true` → hiện thẻ kết quả |
| `intent` | ✅ | `SYMPTOM` / `FAQ` |
| `triage.department_code` | ✅ khi có kết quả | **Mã khoa** trong bảng `DEPARTMENTS.code` → Spring tra ra UUID |
| `triage.department_name` | | Tên hiển thị "Khoa để xuất" (chỉ để hiển thị) |
| `triage.priority` | ✅ | `M1`…`M5` → badge "M2 · Khẩn cấp", ưu tiên của phiếu |
| `triage.confidence` | | 0–100, lưu vào phiếu |
| `triage.summary` | | Dòng tóm tắt trong thẻ, lưu vào `ai_summary` của phiếu (y tá xem) |
| `triage.red_flags` | | Các chip đỏ cảnh báo |
| `triage.advice` | | Khung lời khuyên màu xanh |

> [!IMPORTANT]
> **Dùng `department_code` để khớp khoa, không dùng tên.** Ảnh thiết kế ghi "Cấp cứu", còn DB lưu "Khoa Cấp Cứu", so theo tên sẽ không khớp.
> Danh sách mã hợp lệ (từ `database.sql`):
> `KCAPCUU` · `KSAN` · `KTIM` · `KTMH` · `KNHI` · `KTHANKINH` · `KMAT` · `KHOHAP` · `KTIEUHOA` · `KDALIEU` · `KXUONGKHOP`

> [!NOTE]
> Dataset hiện tại (`text,label,intent,severity`) chỉ có 3 mức `NORMAL/MEDIUM/EMERGENCY`. Nếu model chỉ dự đoán được 3 mức đó thì vẫn trả `priority` theo bảng quy đổi bên dưới. Phần quy đổi nên làm **phía AI**, để web luôn nhận M1–M5.

### Quy đổi mức độ → thang M1–M5 (nên làm phía AI)
Dataset chỉ có 3 mức, hệ thống dùng 5 mức:

| `severity` từ AI | Ưu tiên phiếu |
|---|---|
| `EMERGENCY` | **M1** nếu confidence ≥ 80, ngược lại **M2** |
| `MEDIUM` | **M3** |
| `NORMAL` | **M4** |
| `intent = FAQ` | Không phát phiếu |

---

## 3. Mẫu FastAPI (nhóm AI chỉ cần thay phần load/predict)

`ai_service/main.py`:
```python
from fastapi import FastAPI
from pydantic import BaseModel
import joblib

app = FastAPI()

# TODO: thay bằng cách load model của nhóm (joblib / torch / transformers ...)
dept_model = joblib.load("models/department_model.pkl")
sev_model  = joblib.load("models/severity_model.pkl")
intent_model = joblib.load("models/intent_model.pkl")

class ChatRequest(BaseModel):
    message: str
    session_id: str | None = None

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/chat")
def chat(req: ChatRequest):
    text = req.message.strip()
    intent = intent_model.predict([text])[0]

    if intent == "FAQ":
        return {"reply": "Bệnh viện làm việc từ 7h00 đến 17h00 các ngày trong tuần.",
                "has_triage_result": False, "intent": "FAQ"}

    probs = dept_model.predict_proba([text])[0]
    idx = probs.argmax()
    department = dept_model.classes_[idx]
    confidence = int(round(probs[idx] * 100))
    severity = sev_model.predict([text])[0]

    return {
        "reply": f"Bạn nên khám tại {department}. Mức độ: {severity}.",
        "has_triage_result": True,
        "intent": "SYMPTOM",
        "department": department,
        "severity": severity,
        "confidence": confidence,
    }
```

Chạy:
```powershell
pip install fastapi uvicorn joblib scikit-learn
uvicorn main:app --host 0.0.0.0 --port 8000
```

Kiểm tra nhanh (trước khi bật web):
```powershell
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d '{\"message\":\"toi bi dau nguc\",\"session_id\":\"test\"}'
```

---

## 4. ⚠️ Phần Spring Boot CHƯA sẵn sàng (cần sửa trước khi nối)

Kiểm tra code hiện tại cho thấy **dù AI trả đúng kết quả, web vẫn chưa hiện nút nhận phiếu**, vì:

| # | Vị trí | Vấn đề |
|---|---|---|
| 1 | `ChatService.AiResponse` | Chỉ giữ `reply` và `has_triage_result`. **Bỏ mất** khoa, mức độ, độ tin cậy |
| 2 | `PatientController /chat/send` | Chỉ trả về tin nhắn bot, không kèm kết quả phân loại |
| 3 | `chat.html` | Thẻ "KẾT QUẢ PHÂN LOẠI AI" chỉ render phía server (`${aiResult}`), nhưng chat gửi bằng AJAX nên thẻ không bao giờ hiện |
| 4 | Chưa có | Hàm đổi **tên khoa → UUID** và **severity → M1–M5** |
| 5 | `RestTemplate` | Chưa đặt timeout; model chậm có thể làm treo request |

**Việc cần làm phía Spring:**
1. Mở rộng `AiResponse` thêm `department`, `severity`, `confidence`.
2. `/chat/send` trả JSON gồm `{ reply, triage: { departmentId, departmentName, priority, confidence, locationGuide } }`.
3. JS trong `chat.html` hiển thị thẻ kết quả, kèm nút gửi `POST /chat/confirm` (sessionId, departmentId, priority, aiSummary, confidence).
4. Tra khoa theo tên, quy đổi mức độ theo bảng ở mục 2.
5. Đặt timeout ~10 giây. AI lỗi thì hiện cho bệnh nhân tự chọn khoa.

---

## 5. Quy trình chạy khi demo

```
1. PostgreSQL  (cổng 5432)        — đã chạy sẵn
2. FastAPI AI  : uvicorn main:app --port 8000
3. Spring Boot : .\mvnw.cmd spring-boot:run   (JDK 21, cổng 8081)
4. Mở http://localhost:8081/login → BHYT HN47900000001 / 123456 → chat
```
