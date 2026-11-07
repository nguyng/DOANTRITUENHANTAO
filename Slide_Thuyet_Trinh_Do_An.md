---
marp: true
theme: default
paginate: true
header: 'Báo cáo Đồ án Trí tuệ Nhân tạo'
footer: 'Hệ thống Trợ lý ảo tiếp đón và phân luồng bệnh viện (MedAssist AI)'
---

# 🤖 Báo cáo Đồ án Trí tuệ Nhân tạo
## Đề tài: Hệ thống Trợ lý ảo hỗ trợ tiếp đón và phân luồng bệnh nhân ứng dụng Trí tuệ Nhân tạo (MedAssist AI)
**Giảng viên hướng dẫn:** Thầy Nguyễn Hà Huy Cường  
**Nhóm thực hiện:** Nhóm nghiên cứu & phát triển (2 thành viên)  
**Công nghệ:** Spring Boot 4.1.1 (Java 21) + Local LLM Ollama `qwen2.5-coder:3b` + PostgreSQL 18

---

# 📋 Nội dung trình bày

**Phần 1: Nhận thức & Đặt vấn đề**
1. Thực trạng quá tải khâu tiếp đón tại Bệnh viện
2. Vấn đề phân luồng sai lệch do triệu chứng tương đồng
3. Mục tiêu & Phạm vi nghiên cứu của đề tài

**Phần 2: Kiến trúc Hệ thống & Kỹ thuật Cốt lõi**
4. Kiến trúc 3 tầng kết hợp Local AI Layer
5. Đăng nhập định danh CCCD 12 số & Kiểm soát BHYT chính chủ 15 ký tự
6. Mô hình Local AI (Ollama `qwen2.5-coder:3b`) & Kỹ thuật Prompt Engineering
7. Quy trình Phân luồng Lâm sàng 2 Giai đoạn (Two-Phase Clinical Triage)
8. Bẫy Cấp cứu tức thì (Red Flag Emergency Bypass)
9. Bộ lọc câu hỏi ngoài y tế (Off-Topic Guard 3 tầng bảo vệ)
10. Điều phối hàng đợi & Quy trình phối hợp Bác sĩ - Y tá

**Phần 3: Kết quả Kiểm thử, Đánh giá & Demo**
11. Kết quả kiểm thử tự động (50/50 Unit Tests & 9/9 E2E Tests PASS)
12. Kết luận, hạn chế và hướng phát triển tương lai
13. Trình diễn thực nghiệm trực tiếp (Live Demo) & Q&A

---

# 🎯 1. Thực trạng & Đặt vấn đề

- **Quá tải khâu tiếp đón:** Bệnh nhân ùn ứ từ sáng sớm; nhân viên y tế mất nhiều thời gian trả lời các câu hỏi lặp đi lặp lại và nhập liệu thủ công.
- **Thách thức triệu chứng tương đồng:** 
  - Nhiều bệnh lý khác nhau có cùng biểu hiện ban đầu (Ví dụ: Cùng là "đau ngực", nguyên nhân có thể do *Nhồi máu cơ tim*, *Viêm phổi màng phổi*, *Trào ngược dạ dày*, hoặc *Đau thần kinh liên sườn*).
  - Các hệ thống AI hỏi 1 lượt (Single-turn) dễ đưa ra phán đoán sai lầm nghiêm trọng nếu không chủ động hỏi thêm triệu chứng mấu chốt.
- **Nguy cơ ca cấp cứu chậm trễ:** Bệnh nhân nguy kịch không thể chờ AI suy nghĩ hay đặt câu hỏi thăm dò.
- **Gian lận BHYT:** Nguy cơ mượn thẻ BHYT của người khác để hưởng chế độ giảm trừ viện phí sai quy định.

---

# 💡 2. Mục tiêu & Phạm vi Nghiên cứu

- 👉 **Mục tiêu 1 – Tiếp đón tự động 24/7:** Bệnh nhân đăng nhập bằng CCCD 12 số, tương tác qua kênh chat trực quan.
- 👉 **Mục tiêu 2 – Phân luồng lâm sàng 2 giai đoạn:** Ứng dụng Local LLM phân loại vào 11 chuyên khoa kèm mức ưu tiên M1–M5.
- 👉 **Mục tiêu 3 – Kiểm soát BHYT chính chủ:** Thẻ BHYT 15 ký tự (QĐ 1666) bắt buộc phải khớp với CCCD của tài khoản đăng nhập.
- 👉 **Mục tiêu 4 – Điều phối hàng đợi toàn diện:** Cấp STT điện tử, màn hình sảnh realtime và Dashboard cho y tá điều phối.

> [!IMPORTANT]
> **Giới hạn phạm vi y khoa:** Hệ thống **CHỈ đóng vai trò điều hướng hành chính và tiếp đón**, **KHÔNG chẩn đoán bệnh lý chuyên sâu** và **KHÔNG kê đơn thuốc**. Quyết định y khoa cuối cùng luôn thuộc về đội ngũ bác sĩ chuyên môn.

---

# 🏛️ 3. Kiến trúc Tổng thể Hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│ 1. GIAO DIỆN (Client): Thymeleaf + Tailwind CSS + WebSocket │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP / WebSocket STOMP
┌──────────────────────────────▼──────────────────────────────┐
│ 2. ỨNG DỤNG LÕI: Spring Boot 4.1.1 (Java 21 LTS)            │
│    ├─ Spring Security: Xác thực định danh CCCD 12 số        │
│    ├─ BHYT Engine: Xác thực 3 lớp (Regex, DB, Mock API)     │
│    ├─ ChatService: Điều phối hội thoại 2 giai đoạn          │
│    └─ Queue Module: Cấp STT nguyên tử & đồng bộ thời gian thực│
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │ HTTP POST (Internal)
┌──────────────▼──────────────┐ ┌─────────────▼───────────────┐
│ 3. CƠ SỞ DỮ LIỆU            │ │ 4. LOCAL AI ENGINE          │
│    PostgreSQL 18.6          │ │    Ollama (qwen2.5-coder:3b)│
│    • Lưu Users, Tickets     │ │    • Port 11434 (Offline)   │
│    • Lưu ChatMessages, Logs │ │    • Tuân thủ Nghị định 13  │
└─────────────────────────────┘ └─────────────────────────────┘
```

---

# 💳 4. Định danh CCCD & Xác thực BHYT Chính chủ

- **Định danh điện tử:** Bệnh nhân đăng nhập bằng số CCCD 12 số (hỗ trợ tương thích ngược đăng nhập bằng mã BHYT).
- **Chuẩn hóa mã thẻ 15 ký tự:** Tuân thủ Quyết định 1666/QĐ-BHXH (2 chữ cái đối tượng + 1 số mức hưởng + 2 số tỉnh + 10 số thứ tự).
- **Quy trình thẩm định 3 lớp kiểm soát:**
  1. *Lớp 1 – Kiểm tra định dạng Regex:* `^[A-Z]{2}[1-5][0-9]{2}[0-9]{10}$`.
  2. *Lớp 2 – Tra cứu CSDL:* Tìm kiếm thẻ trong CSDL nội bộ hoặc Mock API BHXH.
  3. *Lớp 3 – Đối soát chính chủ 1:1:* Đối chiếu thẻ BHYT nhập vào với số BHYT gắn với CCCD tài khoản đang đăng nhập.
- **Ngăn chặn gian lận viện phí:** Nếu nhập thẻ của người khác → Hệ thống lập tức trả về **HTTP 403 Forbidden** và từ chối áp dụng giảm giá.

---

# 🤖 5. Lựa chọn Mô hình AI: Local LLM

- **Mô hình cốt lõi:** `qwen2.5-coder:3b` (Alibaba Cloud, 3 tỷ tham số) chạy qua **Ollama** tại `localhost:11434`.
- **Vì sao chọn Local LLM thay vì Cloud API (OpenAI, Gemini)?**
  - **Bảo mật tuyệt đối:** Dữ liệu triệu chứng y tế được xử lý 100% offline nội bộ trên máy chủ bệnh viện, tuân thủ Nghị định 13/2023/NĐ-CP.
  - **Nhẹ & hiệu quả:** Mô hình ~1.9 GB chạy mượt trên phần cứng thông thường không bắt buộc GPU đắt tiền.
  - **Định dạng JSON chuẩn xác:** `format: "json"` giúp backend Spring Boot parse trực tiếp đối tượng TriageResult.
- **Kỹ thuật chính:** **Prompt Engineering** (không huấn luyện lại mô hình).
  - `temperature: 0.1` → Tối đa hóa tính nhất quán và ổn định cho nghiệp vụ y tế.
  - `num_predict: 350` → Phản hồi nhanh (< 1-2s), tránh sinh từ thừa hay lặp vô tận.
  - Định hình vai trò *"Trợ lý tiếp đón & phân luồng"*, tránh vi phạm bộ lọc Safety Refusal.

---

# 🩺 6. Phân luồng Lâm sàng 2 Giai đoạn (Two-Phase Triage)

```
[Bệnh nhân nhập triệu chứng: "Tôi bị đau ngực"]
       │
       ▼ (Giai đoạn 1: Thăm dò triệu chứng mấu chốt)
[AI PROBE: Lắng nghe ân cần, ĐẶT 2-3 CÂU HỎI LÂM SÀNG THEN CHỐT]
- Tính chất đau (nhói khi hít thở, đè nặng hay nóng rát sau xương ức)?
- Triệu chứng đi kèm (sốt, ho đờm, khó thở, vã mồ hôi, ợ chua)?
- Thời gian xuất hiện (vài giờ hay kéo dài nhiều ngày)?
       │
       ▼ (Bệnh nhân làm rõ: "Đau nhói khi hít sâu, ho đờm và sốt nhẹ 3 hôm nay")
[AI DECISION: Tổng hợp dữ kiện lâm sàng -> Ra quyết định phân luồng]
- Chẩn đoán phân biệt: Hướng nhiều đến Viêm phế quản / màng phổi
- Chốt chuyên khoa: KHOA HÔ HẤP (KHOHAP)
- Mức độ ưu tiên: M2 Khẩn cấp (hoặc M3 Ưu tiên), Độ tin cậy: 92%
- Tóm tắt triệu chứng & Lời dặn chuẩn bị trước khi vào phòng khám
```

---

# 🚨 7. Bẫy Cấp cứu Tức thì (Red Flag Emergency Bypass)

- **Nguyên tắc an toàn y tế số 1:** Tính mạng người bệnh là trên hết, không để AI suy luận làm chậm trễ thời gian vàng cấp cứu.
- **Cơ chế hoạt động:**
  - Quét trước các dấu hiệu nguy kịch cực đoan: *"bất tỉnh", "ngất", "co giật", "ngừng thở", "tím tái", "hôn mê", "nôn ra máu", "đau ngực kèm vã mồ hôi", "tai nạn mất máu"*.
  - **Bỏ qua hoàn toàn AI (Bypass LLM) trong chưa đầy 50 mili-giây**.
  - Tự động phân luồng trực tiếp vào **Khoa Cấp Cứu (`KCAPCUU`)** với mức ưu tiên tối cao **M1 (Nguy kịch)**.
  - Đẩy thẳng phiếu khám lên **TOP 1** hàng đợi trên Dashboard của Y tá.

---

# 🛡️ 8. Bộ lọc Câu hỏi Ngoài Y tế (Off-Topic Guard 3 Tầng)

- **Vấn đề thực tiễn:** Bệnh nhân có thể hỏi chuyện phi y tế (thời tiết, bóng đá, điện thoại, nấu ăn, crypto...). Nếu không chặn, AI có thể trả lời lan man hoặc phân luồng sai lệch vào chuyên khoa.
- **Kiến trúc bảo vệ 3 tầng (Defense in Depth):**
  1. **Tầng 1 (Java Pre-Filter):** Nhận diện từ khóa ngoài y tế (có dấu & không dấu), **chặn ngay trước khi gọi Ollama API**, phản hồi từ chối lịch sự trong < 5ms (tiết kiệm tài nguyên CPU/GPU).
  2. **Tầng 2 (Java Fallback Guard):** Khi chạy chế độ dự phòng offline, bộ lọc vẫn hoạt động, không bao giờ phân luồng bừa bãi.
  3. **Tầng 3 (System Prompt Guard):** Chỉ thị bắt buộc trong prompt yêu cầu model từ chối và trả về `has_triage_result = false`.
- **Giải pháp ranh giới từ Unicode (`(?U)\bho\b` và `(?U)\btê\b`):** Tránh bắt nhầm từ con y tế ngắn bên trong các từ thông thường như `th[ho]i tiet`, `ip[ho]ne`, `[ho]m nay`, `p[ho]`.
- **Không chặn nhầm:** Câu hỗn hợp có triệu chứng (*"tôi chơi bóng đá bị đau chân"*) vẫn được tiếp nhận bình thường.

---

# 👥 9. Điều phối Hàng đợi & Quy trình Phối hợp Bác sĩ - Y tá

- **Phiếu khám điện tử:** Sinh mã phiếu duy nhất, ghi rõ STT theo khoa, mức ưu tiên M1-M5 và câu chỉ dẫn vị trí phòng khám chi tiết.
- **Mô hình phối hợp tại phòng khám:**
  - **Bác sĩ chuyên khoa:** Tập trung 100% chuyên môn thăm khám, chẩn đoán lâm sàng và kê đơn sổ khám bệnh.
  - **Y tá phòng khám:** Quản lý hàng đợi trên Dashboard `/nurse/queue`, bấm **"Gọi số tiếp theo"**, đối chiếu BHYT, hỗ trợ chuyển khoa và bấm **"Hoàn tất khám"**.
- **Chuyển khoa linh hoạt:** Khi bác sĩ chỉ định chuyển khoa, y tá thực hiện chuyển khoa trên hệ thống; phiếu mới tự động sinh tại khoa đích và kế thừa mức ưu tiên.
- **Màn hình sảnh công cộng:** Màn hình LED `/display` cập nhật số đang khám thời gian thực qua WebSocket.

---

# 🧪 10. Kết quả Kiểm thử Tự động Toàn diện

### Kết quả Unit Tests (Maven Surefire): 50/50 PASS (100%)

| Bộ kiểm thử | Số ca | Kết quả | Trọng tâm kiểm thử |
|---|:---:|:---:|---|
| **OffTopicGuardTest** | 36 | **PASS ✅** | Nhận diện câu hỏi ngoài y tế (có/không dấu), không chặn nhầm câu y tế, bypass Ollama |
| **BhytVerificationServiceTest** | 6 | **PASS ✅** | Bắt lỗi thẻ rỗng (E00), sai format (E01), xác thực thẻ qua Mock API BHXH và DB nội bộ |
| **TwoPhaseClinicalTriageTest** | 5 | **PASS ✅** | Bẫy Red Flag cấp cứu, chuẩn hóa mã khoa, phân luồng 2 giai đoạn Probe & Decision |
| **UserCccdAndBhytMatchingTest** | 3 | **PASS ✅** | Tra cứu CCCD 12 số, đa định danh, kiểm soát toàn vẹn BHYT khớp CCCD |

### Kết quả Integration Tests (Python E2E Suite): 9/9 PASS (100%)
- Đăng nhập CCCD, tạo Session, Chat thăm dò Giai đoạn 1, Chốt khoa Giai đoạn 2, Red Flag Bypass, Chặn gian lận BHYT 403, Áp dụng BHYT chính chủ 200, Đăng nhập tương thích ngược.

---

# ⚖️ 11. Đánh giá Kết quả, Hạn chế & Hướng phát triển

### Kết quả đạt được:
- Xây dựng thành công hệ thống hoàn chỉnh từ tiếp đón, trò chuyện AI 2 giai đoạn, bẫy cấp cứu, xác thực BHYT chính chủ đến điều phối hàng đợi phòng khám.

### Hạn chế thẳng thắn nhìn nhận:
- Độ tin cậy (85-98%) hiện do mô hình **tự ước lượng**, chưa phải xác suất hiệu chỉnh thống kê; chưa đo độ chính xác trên tập dữ liệu lâm sàng có nhãn chuẩn do bác sĩ thẩm định.
- Bộ lọc Red Flag và Off-Topic dựa trên **từ khóa**, chưa bao phủ hết các phương ngữ hay cách diễn đạt phong phú.
- Cổng giám định BHXH hiện đang chạy trên cơ chế **giả lập (Mock API)**.

### Hướng phát triển trong tương lai:
- Phối hợp với bác sĩ xây dựng tập ca bệnh mẫu để đo đạc ma trận nhầm lẫn (Confusion Matrix).
- Nâng cấp bộ lọc từ khóa sang mô hình phân loại Intent chuyên biệt; tích hợp giọng nói (Voice-to-Text).
- Đăng ký kết nối chính thức với cổng tiếp nhận dữ liệu Giám định BHYT của BHXH Việt Nam.

---

# 🚀 12. Trình diễn Hệ thống (Live Demo) & Q&A

### Các tình huống thực nghiệm trực tiếp:
1. **Đăng nhập định danh:** Đăng nhập bằng CCCD `079204000001` / `123456`.
2. **AI Giai đoạn 1 (Thăm dò):** Nhập `"Tôi bị đau ngực"` → AI hỏi làm rõ triệu chứng.
3. **AI Giai đoạn 2 (Chốt khoa):** Nhập triệu chứng mấu chốt → AI phân luồng **Khoa Hô hấp**, cấp phiếu STT.
4. **Bẫy Cấp cứu (Red Flag):** Nhập `"Bệnh nhân đột ngột bất tỉnh"` → Cấp phiếu **Cấp cứu M1 ngay lập tức**.
5. **Bộ lọc ngoài y tế (Off-Topic):** Nhập `"Hôm nay bóng đá ai thắng"` → Từ chối lịch sự, bypass AI.
6. **Chặn gian lận BHYT:** Nhập thẻ của người khác → Bị từ chối HTTP 403; nhập đúng thẻ → Hưởng 100%.
7. **Điều phối hàng đợi Y tá:** Y tá gọi số tiếp theo, màn hình sảnh cập nhật realtime.

---

# ❓ CẢM ƠN QUÝ THẦY CÔ VÀ CÁC BẠN ĐÃ LẮNG NGHE!

**Nhóm sẵn sàng trả lời các câu hỏi phản biện của Hội đồng.**
