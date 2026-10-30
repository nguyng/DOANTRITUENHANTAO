# 🔌 HƯỚNG DẪN KẾT NỐI LOCAL AI (OLLAMA) VÀO HỆ THỐNG MEDASSIST

> **Phiên bản kiến trúc mới (Tháng 10/2026):**  
> Hệ thống đã chuyển đổi toàn diện từ mô hình huấn luyện FastAPI/PhoBERT cũ sang **Mô hình Trí tuệ Nhân tạo Cục bộ (Local LLM via Ollama `qwen2.5-coder:3b`)**. Backend Spring Boot kết nối trực tiếp với Ollama API thông qua cơ chế hội thoại 2 giai đoạn (Two-Phase Differential Inquiry) và bẫy cấp cứu (Red Flag Bypass), không phụ thuộc vào service Python trung gian.

---

## 1. Kiến trúc kết nối trực tiếp

```
Trình duyệt Web ──(AJAX /chat/send)──► Spring Boot 4.1.1 (:8081) ──(HTTP POST /api/chat)──► Local Ollama (:11434)
                                             │                                                      │
                                             │                                       Model: qwen2.5-coder:3b
                                             ▼                                                      ▼
                                       PostgreSQL 18                                         JSON Response
                                 (Lưu ChatMessage, Ticket)                             (Phân tích & Phân luồng)
```

- **Spring Boot backend** đọc cấu hình AI từ `application.properties`:
  ```properties
  ai.service.url=http://localhost:11434
  ai.model.name=qwen2.5-coder:3b
  ```
- Backend chịu trách nhiệm quản lý phiên chat (`ChatSession`), lịch sử hội thoại (`ChatMessage`), và chuyển tiếp trực tiếp payload sang Ollama endpoint `/api/chat`.
- **Cơ chế chịu lỗi (Fail-safe Fallback):** Nếu Ollama phản hồi chậm hoặc ngoại tuyến, module phân luồng quy tắc Java tích hợp sẵn sẽ tự động kích hoạt để người bệnh luôn nhận được tư vấn mà không bị gián đoạn.

---

## 2. Quy trình Phân luồng Lâm sàng 2 Giai đoạn (Two-Phase Clinical Triage)

Nhằm khắc phục tình trạng phán đoán vội vàng khi các bệnh lý có triệu chứng ban đầu tương đồng (ví dụ: đau ngực do Tim mạch vs Viêm phổi màng phổi), hệ thống triển khai quy trình 2 giai đoạn:

```
[Bệnh nhân nhập triệu chứng ban đầu]
              │
              ▼
   Kiểm tra Red Flag Cấp cứu?
     ├── CÓ ──► BYPASS TOÀN BỘ AI ──► Định tuyến ngay vào KHOA CẤP CỨU (Mức M1)
     └── KHÔNG
           │
           ▼
   Xác định Giai đoạn Hội thoại
     ├── Lượt 1 (Mô tả ngắn, chưa đủ dữ kiện)
     │     └──► GIAI ĐOẠN 1: Khai thác Triệu chứng Mấu chốt (Probe Prompt)
     │           └── AI đặt câu hỏi làm rõ tính chất, thời gian, triệu chứng đi kèm
     │
     └── Lượt 2 trở đi (Đã có thông tin mấu chốt / Mô tả chi tiết > 120 ký tự)
           └──► GIAI ĐOẠN 2: Chẩn đoán Phân biệt & Chốt Khoa (Decision Prompt)
                 └── AI trả về JSON kết quả phân luồng và kích hoạt Thẻ tiếp đón
```

---

## 3. Hợp đồng Payload gửi tới Ollama (`/api/chat`)

### 3.1. Cấu trúc Request

Spring Boot gửi HTTP POST tới `http://localhost:11434/api/chat` với định dạng JSON:

```json
{
  "model": "qwen2.5-coder:3b",
  "format": "json",
  "stream": false,
  "options": {
    "temperature": 0.1,
    "num_predict": 350
  },
  "messages": [
    {
      "role": "system",
      "content": "... SYSTEM PROMPT (Probe hoặc Decision tùy giai đoạn) ..."
    },
    { "role": "user", "content": "Tôi bị đau ngực" },
    { "role": "assistant", "content": "Cơn đau ngực của bạn có cảm giác như thế nào?..." },
    { "role": "user", "content": "Đau nhói tăng khi hít sâu, ho có đờm và sốt" }
  ]
}
```

> [!IMPORTANT]
> - **Vai trò của Prompt:** Mô hình được cấu hình trong vai trò *"Trợ Lý Tiếp Đón & Phân Luồng Phòng Khám"* (nhân viên hành chính điều hướng), **KHÔNG** xưng là "Bác sĩ chẩn đoán bệnh" nhằm tránh cơ chế từ chối an toàn y tế (Safety Refusal) của mô hình mã nguồn mở.
> - **Kiểm soát sinh từ (`num_predict: 350`):** Giới hạn độ dài output để phản hồi nhanh (< 1-2 giây) và tránh lặp từ vô tận.

### 3.2. Cấu trúc Response Giai đoạn 1 (Khai thác triệu chứng - Chưa chốt khoa)

Khi người bệnh mô tả ngắn gọn ở lượt đầu tiên:
```json
{
  "reply": "Chào bạn, cơn đau ngực của bạn có cảm giác như thế nào (đau nhói khi hít thở, đè nặng lồng ngực hay nóng rát)? Cơn đau có lan đi đâu và có kèm sốt, ho đờm hay khó thở không?",
  "has_triage_result": false
}
```
*Kết quả:* Web chỉ hiển thị tin nhắn bot, chưa hiển thị thẻ chọn khoa tiếp nhận.

### 3.3. Cấu trúc Response Giai đoạn 2 (Đã đủ dữ kiện - Phân luồng)

Khi người bệnh đã trả lời câu hỏi làm rõ:
```json
{
  "reply": "Dựa trên triệu chứng đau ngực nhói tăng khi hít sâu kèm sốt và ho đờm, tình trạng của bạn hướng nhiều đến bệnh lý đường hô hấp (viêm phế quản/màng phổi). Tôi phân luồng bạn sang Khoa Hô Hấp.",
  "has_triage_result": true,
  "triage": {
    "department_code": "KHOHAP",
    "priority": "M2",
    "confidence": 92,
    "summary": "Đau ngực kiểu màng phổi tăng khi hít thở, ho đờm, sốt nhẹ",
    "advice": "Đến Khoa Hô Hấp để được nghe phổi và chụp X-quang lồng ngực kiểm tra."
  }
}
```

---

## 4. Danh mục 11 Chuyên khoa Hợp Lệ

Hệ thống quản lý thống nhất 11 chuyên khoa với mã chuẩn (`database.sql`):

| STT | Mã Khoa (`department_code`) | Tên Khoa | Ví dụ Triệu chứng Tiêu Biểu |
|:---:|:---|:---|:---|
| 1 | `KCAPCUU` | Khoa Cấp Cứu | Hôn mê, bất tỉnh, ngừng thở, nôn ra máu, đa chấn thương |
| 2 | `KTIM` | Khoa Tim Mạch | Đau thắt ngực đè nặng lan tay trái, vã mồ hôi, hồi hộp trống ngực |
| 3 | `KHOHAP` | Khoa Hô Hấp | Ho đờm, khó thở, đau nhói ngực khi hít sâu, viêm phổi |
| 4 | `KTIEUHOA` | Khoa Tiêu Hóa | Đau thượng vị, ợ chua, trào ngược, đau quặn bụng, tiêu chảy |
| 5 | `KXUONGKHOP` | Khoa Cơ Xương Khớp | Đau khớp gối, sưng khớp, thoái hóa cột sống, cứng khớp |
| 6 | `KTMH` | Khoa Tai Mũi Họng | Đau rát họng, viêm amidan, ù tai, ngạt mũi, chảy mủ tai |
| 7 | `KMAT` | Khoa Mắt | Đau mắt đỏ, cộm mắt, nhìn mờ, rách giác mạc, chảy nước mắt |
| 8 | `KDALIEU` | Khoa Da Liễu | Phát ban, ngứa ngáy, dị ứng da, mề đay, mụn nhọt |
| 9 | `KTHANKINH` | Khoa Thần Kinh | Đau nửa đầu, chóng mặt quay cuồng, tê bì tay chân, mất ngủ |
| 10 | `KNHI` | Khoa Nhi | Bệnh nhi dưới 15 tuổi có sốt, ho, phát ban, tiêu chảy |
| 11 | `KSAN` | Khoa Phụ Sản | Khám thai định kỳ, đau bụng dưới thai kỳ, rối loạn kinh nguyệt |

---

## 5. Thang Mức độ Ưu tiên (M1 – M5)

Hệ thống áp dụng thang phân loại mức độ khẩn cấp theo tiêu chuẩn bệnh viện:

| Mức | Tên mức | Thời gian đáp ứng | Xử lý trên hệ thống |
|:---:|:---|:---:|:---|
| **M1** | Nguy kịch | Ngay lập tức | Tự động bypass AI, đẩy thẳng lên đầu hàng đợi cấp cứu |
| **M2** | Khẩn cấp | <= 15 phút | Badge đỏ, ưu tiên gọi trước các ca thông thường |
| **M3** | Ưu tiên | <= 30 phút | Badge cam, khám trong thời gian tiêu chuẩn |
| **M4** | Thông thường | <= 60 phút | Badge xanh dương, thứ tự khám tuần tự |
| **M5** | Không khẩn | Hẹn lịch / Theo dõi | Khám sức khỏe định kỳ, tái khám |

---

## 6. Quy trình Vận hành Demo thực tế

Khi trình diễn hoặc kiểm thử toàn bộ hệ thống:

```powershell
# Bước 1: Khởi động Cơ sở dữ liệu PostgreSQL (port 5432)
# Đảm bảo database 'hospital_db' đang chạy (User: postgres / Pass: 29061612)

# Bước 2: Khởi động Ollama Local AI (port 11434)
ollama run qwen2.5-coder:3b

# Bước 3: Khởi động Backend Spring Boot (port 8081)
cd d:\DOANTRITUENHANTAO\HeThongPhanLuongBenhVien
./mvnw spring-boot:run

# Bước 4: Mở trình duyệt và trải nghiệm
# URL: http://localhost:8081/login
# Đăng nhập bằng CCCD: 079204000001 (Mật khẩu: 123456)
```

---

## 7. Bộ Kiểm thử Tự động Hóa (Test Suites)

Toàn bộ logic kết nối AI, phân luồng 2 giai đoạn và xác thực BHYT được tự động hóa qua 2 bộ test:

1. **Unit Test (Maven Surefire - 15 tests):**
   ```powershell
   ./mvnw test
   ```
   - `BhytVerificationServiceTest`: 6 tests kiểm tra định dạng và nguồn BHYT
   - `UserCccdAndBhytMatchingTest`: 3 tests kiểm tra tra cứu CCCD và khớp thẻ chính chủ
   - `TwoPhaseClinicalTriageTest`: 5 tests kiểm tra Red Flag, Prompt phân luồng và Fallback
   - `HeThongPhanLuongBenhVienApplicationTests`: 1 test nạp toàn bộ Spring Context

2. **Integration Test End-to-End (Python):**
   ```powershell
   python d:\DOANTRITUENHANTAO\HeThongPhanLuongBenhVien\test_integration.py
   ```
   Chạy 9 kịch bản thực tế đối soát từ đăng nhập CCCD, chat 2 giai đoạn, bẫy cấp cứu đến chặn gian lận BHYT (kết quả 9/9 PASS).