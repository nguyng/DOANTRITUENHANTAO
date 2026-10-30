# DANH SÁCH CÁC LOẠI SƠ ĐỒ HỆ THỐNG
# Dự án: Trợ lý ảo hỗ trợ tiếp đón và phân luồng bệnh nhân ứng dụng Local AI (MedAssist)

---

## TỔNG QUAN TRẠNG THÁI

| #  | Tên sơ đồ                                    | Nhóm          | Ưu tiên   | Trạng thái |
| -- | -------------------------------------------- | ------------- | --------- | ---------- |
| 1  | BFD - Biểu đồ phân rã chức năng              | Yêu cầu       | BẮT BUỘC  | Đã cập nhật|
| 2  | Use Case Diagram                             | Yêu cầu       | BẮT BUỘC  | Đã cập nhật|
| 3  | Use Case Description (Bảng mô tả)            | Yêu cầu       | BẮT BUỘC  | Đã cập nhật|
| 4  | DFD Mức 0 - Context Diagram                  | Luồng dữ liệu | BẮT BUỘC  | Đã cập nhật|
| 5  | DFD Mức 1 - Chi tiết luồng dữ liệu           | Luồng dữ liệu | BẮT BUỘC  | Đã cập nhật|
| 6  | ERD - Biểu đồ thực thể quan hệ               | Cơ sở dữ liệu | BẮT BUỘC  | Đã cập nhật|
| 7  | Lược đồ quan hệ (Relational Schema)          | Cơ sở dữ liệu | BẮT BUỘC  | Đã cập nhật|
| 8  | Activity Diagram - Biểu đồ hoạt động         | Hành vi       | BẮT BUỘC  | Đã cập nhật|
| 9  | Sequence Diagram - Biểu đồ tuần tự           | Hành vi       | BẮT BUỘC  | Đã cập nhật|
| 10 | Class Diagram - Biểu đồ lớp                  | Cấu trúc      | NÊN CÓ    | Đã cập nhật|
| 11 | State Diagram - Biểu đồ trạng thái           | Hành vi       | NÊN CÓ    | Đã cập nhật|
| 12 | System Architecture Diagram                  | Kiến trúc     | BẮT BUỘC  | Đã cập nhật|
| 13 | Component Diagram - Biểu đồ thành phần       | Cấu trúc      | NÊN CÓ    | Đã cập nhật|
| 14 | Deployment Diagram - Biểu đồ triển khai      | Kiến trúc     | NÊN CÓ    | Đã cập nhật|
| 15 | Sitemap / Navigation Flow                    | Giao diện     | NÊN CÓ    | Đã cập nhật|
| 16 | Wireframe / UI Mockup                        | Giao diện     | THÊM ĐIỂM | Đã cập nhật|
| 17 | Two-Phase Differential Inquiry (Sơ đồ mới)  | Quy trình AI  | BẮT BUỘC  | ĐÃ THÊM MỚI|

---

## NHÓM 1 - SƠ ĐỒ YÊU CẦU VÀ CHỨC NĂNG

### 1. BFD - Biểu đồ phân rã chức năng
(Business Function Diagram / Functional Decomposition Diagram)

- **Mục đích:** Phân tích toàn bộ chức năng hệ thống từ tổng quát đến chi tiết.
- **Công cụ gợi ý:** Draw.io, Microsoft Visio

`
HỆ THỐNG MEDASSIST AI (HOSPITAL TRIAGE SYSTEM)
|
+-- 1. Quản lý Người dùng & Xác thực
|   +-- 1.1. Đăng nhập bằng số CCCD (Bệnh nhân)
|   +-- 1.2. Đăng nhập bằng Mã nhân viên (Y tá / Bác sĩ / Admin)
|   +-- 1.3. Đổi mật khẩu tài khoản
|   +-- 1.4. Quản lý hồ sơ bệnh nhân (Admin & Cá nhân)
|   +-- 1.5. Kiểm tra & Xác thực thẻ BHYT liên kết CCCD chính chủ
|
+-- 2. Trợ lý Ảo Tiếp Đón & Phân Luồng (Local AI - Ollama)
|   +-- 2.1. Khởi tạo phiên trò chuyện lâm sàng
|   +-- 2.2. Khai thác chuyên sâu triệu chứng mấu chốt (Giai đoạn 1 - Probing)
|   +-- 2.3. Phát hiện dấu hiệu cờ đỏ cấp cứu (Emergency Red Flags -> KCAPCUU / M1)
|   +-- 2.4. Tổng hợp & Phân luồng chính xác vào 11 chuyên khoa (Giai đoạn 2 - Decision)
|   +-- 2.5. Phân tầng mức độ ưu tiên theo chuẩn quốc tế (M1 -> M5)
|   +-- 2.6. Tự động ghi nhật ký phân tích AI (AI Analysis Logs)
|
+-- 3. Quản lý Hàng đợi & Phát Phiếu Khám
|   +-- 3.1. Bốc số thứ tự & Tạo phiếu khám (Queue Ticket)
|   +-- 3.2. Ước tính vị trí hàng đợi và thời gian chờ
|   +-- 3.3. Y tá gọi số tiếp theo & Điều phối hàng đợi
|   +-- 3.4. Chuyển khoa khám bệnh
|   +-- 3.5. Hủy phiếu khám
|   +-- 3.6. In phiếu khám điện tử
|
+-- 4. Quản lý Khám bệnh & Viện phí BHYT
|   +-- 4.1. Xem danh sách lịch sử lần khám (Visit Records)
|   +-- 4.2. Xem chi tiết đơn thuốc và dịch vụ cận lâm sàng
|   +-- 4.3. Tự động tính miễn giảm BHYT (80%, 95%, 100%) theo thẻ hợp lệ
|
+-- 5. Quản trị Hệ thống (Admin)
|   +-- 5.1. Quản lý Danh mục Chuyên khoa & Phòng khám
|   +-- 5.2. Quản lý Nhân sự & Phân quyền
|   +-- 5.3. Cấu hình hỏi đáp FAQ
|   +-- 5.4. Thống kê, báo cáo lượt khám & công suất khoa
|   +-- 5.5. Theo dõi Nhật ký Phân tích AI & Độ trễ xử lý (AI Logs)
`

---

### 2. Use Case Diagram - Biểu đồ trường hợp sử dụng

- **Mục đích:** Thể hiện 4 Tác nhân (Actor) và các chức năng:
  - **Bệnh nhân:** Đăng nhập CCCD, Chat tư vấn AI đa bước, Xác nhận phân luồng nhận STT, Kiểm tra BHYT, Xem lịch sử khám & chi phí.
  - **Y tá / Tiếp đón:** Xem danh sách hàng đợi theo khoa, Gọi bệnh nhân, Chuyển khoa khám, Tra cứu thông tin bệnh nhân.
  - **Bác sĩ:** Tiếp nhận bệnh nhân, Khám, Kê đơn thuốc, Chỉ định cận lâm sàng, Hoàn thành lượt khám.
  - **Admin:** Quản trị danh mục khoa phòng, Quản lý tài khoản, Xem báo cáo thống kê, Theo dõi nhật ký AI.
  - **Màn hình sảnh:** Hiển thị số thứ tự đang được phục vụ tại các phòng khám realtime.

---

### 3. Use Case Description - Bảng mô tả Use Case mẫu

#### UC-01: Đăng nhập Bệnh nhân bằng CCCD
- **Tác nhân:** Bệnh nhân
- **Tiền điều kiện:** Bệnh nhân đã có tài khoản trên hệ thống
- **Luồng chính:**
  1. Bệnh nhân nhập số CCCD (12 chữ số) và mật khẩu.
  2. Hệ thống kiểm tra định danh trong bảng USERS.
  3. Hệ thống xác thực thành công và thiết lập phiên đăng nhập của Bệnh nhân.
- **Luồng ngoại lệ:** Sai thông tin -> Thông báo "Sai tài khoản hoặc mật khẩu".

#### UC-06: Tư vấn & Phân luồng lâm sàng đa bước (Two-Phase AI Triage)
- **Tác nhân:** Bệnh nhân, Local AI (Ollama)
- **Tiền điều kiện:** Bệnh nhân đã đăng nhập
- **Luồng chính:**
  1. Bệnh nhân nhắn triệu chứng ban đầu.
  2. Local AI phát hiện triệu chứng còn mơ hồ/dễ nhầm lẫn giữa các khoa.
  3. Local AI đặt 2-3 câu hỏi then chốt (vị trí, tính chất đau, hoàn cảnh xuất hiện, triệu chứng kèm theo).
  4. Bệnh nhân trả lời các câu hỏi làm rõ.
  5. Local AI tổng hợp các triệu chứng mấu chốt, xác định đúng chuyên khoa trong 11 khoa và mức ưu tiên M1-M5.
  6. Hệ thống hiển thị Thẻ phân luồng kèm lời giải thích lý do chọn khoa.
  7. Bệnh nhân bấm "Xác nhận nhận phiếu khám".
  8. Hệ thống cấp phiếu khám và xếp vào hàng đợi của khoa.
- **Luồng ngoại lệ (Cờ đỏ cấp cứu):** Bệnh nhân có dấu hiệu nguy kịch -> AI lập tức chuyển thẳng Khoa Cấp Cứu (M1), không hỏi thêm.

---

## NHÓM 2 - SƠ ĐỒ LUỒNG DỮ LIỆU (DFD)

### 4. DFD Mức 0 - Context Diagram (Biểu đồ ngữ cảnh)

`
[Bệnh nhân] -------- CCCD, Mật khẩu, Triệu chứng bệnh --------> ( HỆ THỐNG ) <-------- Quản lý khoa, nhân sự -------- [Admin]
            <------- Thẻ phân luồng, Phiếu khám STT, BHYT ------ ( MEDASSIST ) -------- Báo cáo thống kê, AI Logs ---->
[Y tá/Bác sĩ] <----- Danh sách hàng đợi bệnh nhân ------------- (    AI    )
              ------ Lệnh gọi số, chuyển khoa, kết quả khám ---> (          ) -------- STT hiện tại, Tên gọi --------> [Màn hình Sảnh]
                                                                     ^ |
                                                       Prompt / JSON | | Trả về Triage & Reply
                                                                     | v
                                                            [Local AI - Ollama]
                                                            (qwen2.5-coder:3b)
`

---

### 5. DFD Mức 1 - Chi tiết luồng dữ liệu

`
Luồng 1 - Tiếp đón, Khai thác triệu chứng & Phân luồng AI:
  [Bệnh nhân] -> Tin nhắn triệu chứng -> (1.1 Xử lý tin nhắn & Kiểm tra Cờ đỏ)
    -> [Có cờ đỏ cấp cứu] -> Chốt KCAPCUU (M1)
    -> [Triệu chứng thông thường] -> (1.2 Gửi Prompt sang Local AI Ollama) -> (1.3 Phản hồi câu hỏi mấu chốt / Triage)
    -> Lưu =CHAT_MESSAGES= & =AI_ANALYSIS_LOGS=
    -> (1.4 Xác nhận phiếu khám) -> =QUEUE_TICKETS= -> Phiếu STT -> [Bệnh nhân]

Luồng 2 - Xác thực BHYT chính chủ theo CCCD:
  [Bệnh nhân] -> Mã thẻ BHYT (15 ký tự) -> (2.1 Xác thực 3 tầng: Định dạng -> DB -> Mock BHXH)
    -> (2.2 Kiểm tra sở hữu BHYT với CCCD đang đăng nhập)
    -> Khớp -> Áp dụng mức giảm (80/95/100%) vào =USERS= -> [Bệnh nhân]
    -> Không khớp -> Báo lỗi từ chối

Luồng 3 - Điều phối hàng đợi:
  [Y tá] -> Lệnh gọi số -> (3.1 Cập nhật trạng thái CALLED) -> =QUEUE_TICKETS=
    -> (3.2 Phát thông báo) -> [Màn hình sảnh] & [Bệnh nhân]
`

---

## NHÓM 3 - SƠ ĐỒ CƠ SỞ DỮ LIỆU

### 6. ERD - Biểu đồ thực thể quan hệ

Các bảng chính trong CSDL PostgreSQL (hospital_db):
- **USERS**: id (PK), cccd (UNIQUE), hyt_number (UNIQUE), password_hash, ull_name, ole (PATIENT/NURSE/DOCTOR/ADMIN), department_id (FK), hyt_discount_rate, hyt_expiry_date, is_active...
- **DEPARTMENTS**: id (PK), code (KCAPCUU, KTIM, KHOHAP, KTMH, KTIEUHOA, KXUONGKHOP, KDALIEU, KMAT, KTHANKINH, KNHI, KSAN), 
ame, uilding, loor, oom_numbers, location_guide...
- **QUEUE_TICKETS**: id (PK), patient_id (FK->USERS), department_id (FK->DEPARTMENTS), 	icket_number, priority (M1, M2, M3, M4, M5), status (WAITING, CALLED, DONE, CANCELLED, TRANSFERRED), i_summary, i_confidence...
- **CHAT_SESSIONS**: id (PK), user_id (FK->USERS), 	icket_id (FK->QUEUE_TICKETS), started_at, ended_at
- **CHAT_MESSAGES**: id (PK), session_id (FK->CHAT_SESSIONS), sender_role (USER/BOT/SYSTEM), content, message_type, created_at
- **AI_ANALYSIS_LOGS**: id (PK), message_id (FK->CHAT_MESSAGES), i_model_used (qwen2.5-coder:3b), input_text, predicted_dept, confidence_score, processing_ms, created_at
- **VISIT_RECORDS**: id (PK), 	icket_id (FK->QUEUE_TICKETS), patient_id (FK->USERS), department_id (FK->DEPARTMENTS), doctor_id (FK->USERS), original_cost, insurance_paid, inal_cost, status...
- **MEDICAL_SERVICES**: id (PK), 
ame, price, is_bhyt_covered, hyt_price_limit
- **MEDICINES**: id (PK), 
ame, price, is_bhyt_covered, hyt_price_limit
- **VISIT_SERVICES**: id (PK), isit_id (FK->VISIT_RECORDS), service_id (FK->MEDICAL_SERVICES), patient_co_pay, hyt_pay
- **PRESCRIPTIONS**: id (PK), isit_id (FK->VISIT_RECORDS), medicine_id (FK->MEDICINES), quantity, dosage, patient_co_pay, hyt_pay

---

## NHÓM 4 - SƠ ĐỒ HÀNH VI (BEHAVIORAL)

### 8. Activity Diagram - Danh sách cần vẽ
- **AD-01:** Đăng nhập bệnh nhân bằng CCCD & Đồng bộ BHYT chính chủ.
- **AD-02:** Quy trình Chat AI 2 giai đoạn: Khai thác triệu chứng mấu chốt -> Phân luồng nhận STT.
- **AD-03:** Kiểm tra và xác thực thẻ BHYT trực tuyến (/bhyt/check).
- **AD-04:** Y tá tiếp đón gọi số và chuyển khoa khám.
- **AD-05:** Bác sĩ khám, kê đơn thuốc và hoàn tất hồ sơ lượt khám.
- **AD-06:** Tự động tính viện phí khấu trừ BHYT điện tử.

---

### 9. Sequence Diagram - Biểu đồ tuần tự

#### SD-01: Luồng Chat AI 2 giai đoạn & Phân luồng khám bệnh

`
Bệnh nhân              Spring Boot (ChatService)               Local AI (Ollama)            PostgreSQL
    |                               |                                  |                        |
    |--- 1. Gửi triệu chứng ban đầu ->|                                  |                        |
    |    ("Tôi bị đau ngực")        |--- 2. Kiểm tra Red Flags         |                        |
    |                               |--- 3. Lượt 1: Gửi Probe Prompt ->|                        |
    |                               |    (Yêu cầu hỏi mấu chốt)        |                        |
    |                               |<-- 4. Trả về câu hỏi làm rõ -----|                        |
    |<-- 5. Hiển thị câu hỏi -------|    (has_triage = false)          |                        |
    |    (Đau nhói hay đè nặng?...) |                                  |                        |
    |                               |                                  |                        |
    |--- 6. Trả lời triệu chứng --->|                                  |                        |
    |    ("Đau nhói khi hít sâu,    |--- 7. Lượt 2: Gửi Decision Prompt>|                       |
    |     kèm sốt và ho đờm")       |<-- 8. Trả về Khoa KHOHAP + M2 ---|                        |
    |                               |    (has_triage = true)           |                        |
    |                               |--- 9. Lưu tin nhắn & AI Log ----------------------------->|
    |<-- 10. Hiện Thẻ Phân Luồng ---|                                  |                        |
    |                               |                                  |                        |
    |--- 11. Bấm Xác Nhận ----------|                                  |                        |
    |                               |--- 12. Tạo Ticket & Cấp STT ------------------------------>|
    |<-- 13. Phiếu khám điện tử ----|                                  |                        |
`

---

## NHÓM 5 - SƠ ĐỒ KIẾN TRÚC & TRIỂN KHAI

### 12. System Architecture Diagram - Kiến trúc hệ thống

`
+-----------------------------------------------------------------------------------+
|                              CLIENT TIER (GIAO DIỆN)                              |
|   Tailwind CSS  ·  Alpine.js  ·  Lucide Icons  ·  Thymeleaf Server-Side Rendering |
+-----------------------------------------------------------------------------------+
                                         |
                                  HTTP (Port 8081)
                                         v
+-----------------------------------------------------------------------------------+
|                        APPLICATION TIER (SPRING BOOT 3.x)                         |
|  - Security: CCCD Auth, Role-based Access Control (PATIENT / NURSE / DOCTOR/ ADMIN)|
|  - Controllers: AuthController, PatientController, NurseController, AdminController|
|  - Services: ChatService, TicketService, BhytVerificationService, UserService...  |
|  - Repository: Spring Data JPA Hibernate                                          |
+-----------------------------------------------------------------------------------+
                     |                                              |
      HTTP JSON REST | (Port 11434)                   JDBC (5432)   |
                     v                                              v
+------------------------------------+             +--------------------------------+
|         LOCAL AI TIER              |             |           DATA TIER            |
|       Ollama Framework             |             |       PostgreSQL Database      |
|  Model: qwen2.5-coder:3b (1.9 GB)  |             |      (hospital_db: 11 bảng)    |
|  - Multi-Turn Differential Prompt  |             +--------------------------------+
|  - International Triage (M1 - M5)  |
+------------------------------------+
`

---

### 13. Deployment Diagram - Triển khai thực tế

- **Spring Boot Backend:** Cổng 8081 (http://localhost:8081)
- **Local AI Engine (Ollama):** Cổng 11434 (http://localhost:11434)
- **PostgreSQL Database:** Cổng 5432 (localhost:5432/hospital_db)

---

## NHÓM 6 - SƠ ĐỒ ĐIỀU HƯỚNG GIAO DIỆN (SITEMAP)

`
PUBLIC:
  ├── /login                  (Đăng nhập CCCD cho Bệnh nhân / Mã NV cho Cán bộ y tế)
  └── /display/:deptId        (Màn hình hiển thị STT sảnh chờ theo khoa)

PATIENT:
  ├── /chat                   (Trợ lý ảo phân luồng lâm sàng đa bước)
  ├── /ticket                 (Xem phiếu khám & số thứ tự hiện tại)
  ├── /tickets/:id/print      (In phiếu khám điện tử)
  ├── /bhyt/check             (Kiểm tra thẻ BHYT & xác thực liên kết CCCD chính chủ)
  ├── /profile                (Xem & cập nhật thông tin cá nhân)
  ├── /visits                 (Lịch sử các lần khám bệnh)
  ├── /visits/:id             (Chi tiết lượt khám, đơn thuốc & viện phí BHYT)
  └── /directions             (Sơ đồ & chỉ dẫn đường đi các khoa)

NURSE / DOCTOR:
  ├── /nurse/queue            (Hàng đợi tiếp đón & gọi số bệnh nhân)
  ├── /nurse/queue/:deptId    (Hàng đợi chi tiết của từng phòng khám)
  └── /nurse/search           (Tra cứu hồ sơ bệnh nhân nhanh)

ADMIN:
  ├── /admin/dashboard        (Tổng quan lượt khám & thống kê viện)
  ├── /admin/patients         (Danh sách & quản lý bệnh nhân, trạng thái BHYT)
  ├── /admin/departments      (Quản lý 11 chuyên khoa, vị trí phòng, chỉ dẫn)
  ├── /admin/staff            (Quản trị tài khoản Bác sĩ, Y tá)
  ├── /admin/faq              (Cấu hình câu hỏi thường gặp)
  ├── /admin/reports          (Báo cáo phân luồng & tài chính)
  └── /admin/logs             (Nhật ký phân tích AI, độ trễ xử lý, độ tin cậy)
`

---

## MỤC 17 - SƠ ĐỒ QUY TRÌNH 2 GIAI ĐOẠN (TWO-PHASE DIFFERENTIAL INQUIRY)

### 1. Ý nghĩa y khoa của quy trình
Khi người bệnh trình bày triệu chứng, rất nhiều bệnh lý nguy hiểm có biểu hiện ban đầu trùng lặp. Mô hình **Khai thác lâm sàng 2 giai đoạn** giúp AI không kết luận vội vã, mà đóng vai trò như một điều phối viên y tế chuyên nghiệp:
1. **Giai đoạn 1 (Lắng nghe & Khai thác triệu chứng mấu chốt)**: Lắng nghe triệu chứng ban đầu, đặt câu hỏi phân biệt có trọng tâm để thu thập các "dấu hiệu mấu chốt" (pivotal clues).
2. **Giai đoạn 2 (Tổng hợp & Phân luồng chính xác)**: Đối chiếu các dấu hiệu thu thập được với danh mục 11 chuyên khoa để đưa ra quyết định phân khoa và mức độ khẩn cấp chính xác nhất.
3. **Cơ chế Cờ Đỏ Cấp Cứu (Emergency Red Flags)**: Phát hiện dấu hiệu sinh tồn bị đe dọa để chuyển ngay vào Cấp Cứu mà không làm mất thời gian của bệnh nhân.

---

### 2. Sơ đồ luồng quy trình (Flowchart Mermaid)

`mermaid
flowchart TD
    Start([Bệnh nhân gửi tin nhắn triệu chứng]) --> CheckRedFlags{Phát hiện Cờ Đỏ Cấp Cứu?<br/><i>Bất tỉnh, ngừng thở, đau ngực vã mồ hôi, co giật, tai nạn nặng...</i>}

    %% Nhánh Cấp cứu khẩn cấp
    CheckRedFlags -- CÓ (Khẩn cấp) --> EmergencyTriage[CHỐT NGAY: KHOA CẤP CỨU - KCAPCUU<br/>Mức ưu tiên: M1 - Ngay lập tức<br/>Độ tin cậy: 99%]
    EmergencyTriage --> IssueEmergencyTicket[Cảnh báo nguy kịch & Cấp số khám ưu tiên số 1]
    IssueEmergencyTicket --> EndEmergency([Đến thẳng phòng cấp cứu tại cổng chính])

    %% Nhánh Khám thông thường
    CheckRedFlags -- KHÔNG --> CheckTurns{Số lượt hội thoại trong phiên?}

    %% Giai đoạn 1: Khai thác mấu chốt
    CheckTurns -- Lượt 1<br/><i>Triệu chứng còn chung chung</i> --> Phase1[GIAI ĐOẠN 1: KHAI THÁC TRIỆU CHỨNG MẤU CHỐT<br/><i>System Prompt: SYSTEM_PROMPT_PROBE</i>]
    Phase1 --> LockTriage[KHÔNG CHỐT KHOA: has_triage_result = false<br/>triage = null]
    LockTriage --> AskProbingQuestions[AI đặt 2-3 câu hỏi then chốt:<br/>1. Vị trí chính xác & cảm giác đau nhói / quặn / nóng rát / đè nặng?<br/>2. Hoàn cảnh tăng/giảm khi hít sâu / sau ăn / vận động / ấn vào?<br/>3. Triệu chứng đi kèm: sốt, ho đờm, khó thở, ợ chua, buồn nôn, tê bì?]
    AskProbingQuestions --> PatientReply([Bệnh nhân trả lời câu hỏi làm rõ])
    PatientReply --> CheckTurns

    %% Giai đoạn 2: Tổng hợp & Phân luồng
    CheckTurns -- Lượt 2 trở đi<br/><i>Đã có phản hồi làm rõ</i> --> Phase2[GIAI ĐOẠN 2: TỔNG HỢP & RA QUYẾT ĐỊNH PHÂN LUỒNG<br/><i>System Prompt: SYSTEM_PROMPT_DECISION</i>]
    Phase2 --> SynthesizeSymptoms[Tổng hợp triệu chứng mấu chốt:<br/>Đối chiếu các dấu hiệu phân biệt giữa các khoa]

    SynthesizeSymptoms --> MatchDept{Phân tích dấu hiệu mấu chốt thuộc khoa nào?}
    MatchDept -- Đau ngực + tăng khi hít sâu/ho + sốt/ho đờm --> DeptHOHAP[KHOA HÔ HẤP - KHOHAP]
    MatchDept -- Đau ngực + nóng rát sau ức sau ăn + ợ chua --> DeptTIEUHOA[KHOA TIÊU HÓA - KTIEUHOA]
    MatchDept -- Đau ngực + đè nặng gắng sức + lan cánh tay --> DeptTIM[KHOA TIM MẠCH - KTIM]
    MatchDept -- Đau ngực + tăng khi ấn sụn sườn/xoay người --> DeptXUONGKHOP[KHOA XƯƠNG KHỚP - KXUONGKHOP]
    MatchDept -- Đau họng nuốt rát + nghẹt mũi/xoang --> DeptTMH[KHOA TAI MŨI HỌNG - KTMH]
    MatchDept -- Đau đầu/chóng mặt + tê yếu nửa người/mất ngủ --> DeptTHANKINH[KHOA THẦN KINH - KTHANKINH]
    MatchDept -- Ngứa da + nổi mẩn đỏ/mề đay dị ứng --> DeptDALIEU[KHOA DA LIỄU - KDALIEU]
    MatchDept -- Đau mắt + đỏ/mờ mắt --> DeptMAT[KHOA MẮT - KMAT]
    MatchDept -- Trẻ em dưới 16 tuổi --> DeptNHI[KHOA NHI - KNHI]
    MatchDept -- Khám thai / phụ khoa --> DeptSAN[KHOA SẢN - KSAN]

    DeptHOHAP --> BuildTriageResult[Tạo kết quả Triage:<br/>- department_code<br/>- priority: M1 - M5<br/>- confidence: 85 - 98%<br/>- summary & advice<br/>- reply: giải thích rõ triệu chứng mấu chốt]
    DeptTIEUHOA --> BuildTriageResult
    DeptTIM --> BuildTriageResult
    DeptXUONGKHOP --> BuildTriageResult
    DeptTMH --> BuildTriageResult
    DeptTHANKINH --> BuildTriageResult
    DeptDALIEU --> BuildTriageResult
    DeptMAT --> BuildTriageResult
    DeptNHI --> BuildTriageResult
    DeptSAN --> BuildTriageResult

    BuildTriageResult --> SaveLog[Lưu vào AI_ANALYSIS_LOGS & CHAT_MESSAGES]
    SaveLog --> DisplayCard[Hiển thị Thẻ phân luồng trên màn hình Chat]
    DisplayCard --> UserConfirm{Bệnh nhân xác nhận?}
    UserConfirm -- Đổi khoa --> ReChat([Bệnh nhân nhắn thêm để điều chỉnh])
    UserConfirm -- Đồng ý nhận phiếu --> CreateTicket[Tạo phiếu khám QUEUE_TICKETS & Xếp vào hàng đợi]
    CreateTicket --> Done([Nhận phiếu khám điện tử thành công])
`

---

### 3. Ma trận phân biệt triệu chứng lâm sàng mấu chốt (Clinical Differential Matrix)

| Triệu chứng ban đầu | Cần phân biệt giữa các khoa | Câu hỏi then chốt (Giai đoạn 1) | Triệu chứng mấu chốt quyết định (Giai đoạn 2) | Chuyên khoa chốt | Mức ưu tiên |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Đau ngực** | Hô hấp vs Tiêu hóa vs Tim mạch vs Xương khớp | 1. Cơn đau nhói khi hít thở, nóng rát hay đè nặng?<br/>2. Đau tăng sau ăn hay khi gắng sức?<br/>3. Kèm sốt ho đờm hay ợ chua hay hồi hộp? | - Nhói tăng khi ho/hít sâu + ho đờm + sốt<br/>- Nóng rát sau xương ức sau ăn + ợ chua<br/>- Bóp nghẹt gắng sức lan cánh tay + hồi hộp<br/>- Đau chói khi ấn vào sụn sườn / xoay người | **KHOHAP**<br/>**KTIEUHOA**<br/>**KTIM**<br/>**KXUONGKHOP** | M2<br/>M3<br/>M2<br/>M4 |
| **Đau bụng** | Tiêu hóa vs Cấp cứu vs Sản phụ khoa | 1. Đau thượng vị, quanh rốn hay hạ vị?<br/>2. Quặn từng cơn hay liên tục dữ dội?<br/>3. Nữ giới có chậm kinh hay ra máu bất thường? | - Đau thượng vị + ợ chua / đi ngoài phân lỏng<br/>- Đau dữ dội hố chậu phải + sốt cao / nôn mửa<br/>- Đau quặn hạ vị ở nữ giới + chậm kinh | **KTIEUHOA**<br/>**KCAPCUU**<br/>**KSAN** | M3<br/>M1<br/>M2 |
| **Đau đầu / Chóng mặt** | Thần kinh vs Tim mạch vs Tai Mũi Họng | 1. Chóng mặt quay cuồng hay choáng váng?<br/>2. Có tê bì tay chân hay yếu liệt nửa người?<br/>3. Có đo huyết áp cao hay ù tai nhức xoang? | - Đau đầu kèm tê bì tay chân / mất ngủ kinh niên<br/>- Đau đầu vùng chẩm sau gáy + huyết áp cao<br/>- Nhà cửa quay cuồng khi đổi tư thế đầu + ù tai | **KTHANKINH**<br/>**KTIM**<br/>**KTMH** | M3<br/>M2<br/>M3 |
| **Đau họng / Sốt** | Tai Mũi Họng vs Hô hấp | 1. Đau rát khi nuốt nước bọt hay tức ngực?<br/>2. Ho khan nông hay ho sâu có đờm đặc? | - Đau rát họng khi nuốt + amidan sưng + nghẹt mũi<br/>- Ho đờm sâu trong phổi + sốt cao + khó thở | **KTMH**<br/>**KHOHAP** | M4<br/>M2 |
| **Phát ban / Ngứa** | Da liễu vs Cấp cứu (Phản vệ) | 1. Chỉ ngứa ngoài da hay có khó thở, nghẹn họng?<br/>2. Môi mắt có bị sưng phù không? | - Mề đay, mẩn đỏ ngứa thuần túy ngoài da<br/>- Mẩn ngứa kèm phù môi/mắt + khó thở/tức ngực | **KDALIEU**<br/>**KCAPCUU** | M4<br/>M1 |

---

*Cập nhật lần cuối: 30/10/2026 - Đồng bộ hoàn tất với kiến trúc Local AI (Ollama qwen2.5-coder:3b) và Xác thực BHYT theo CCCD.*