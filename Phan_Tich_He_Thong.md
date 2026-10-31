# PHÂN TÍCH & THIẾT KẾ HỆ THỐNG
# Trợ lý ảo hỗ trợ tiếp đón và phân luồng bệnh nhân ứng dụng AI

---

## 1. TỔNG QUAN KIẾN TRÚC HỆ THỐNG

### 1.1. Kiến trúc Tổng thể (High-Level Architecture)

Hệ thống được thiết kế theo mô hình **3-Tier Architecture** kết hợp với một **AI Service Layer** độc lập, giao tiếp thời gian thực qua **WebSocket**.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER (Tier 1)                         │
│  ┌─────────────────┐  ┌────────────────┐  ┌──────────────────────┐  │
│  │  Bệnh nhân Web  │  │ Dashboard Y tá │  │ Màn hình Hành lang   │  │
│  │  (Trình duyệt)  │  │ (Trình duyệt)  │  │ (Public Display)     │  │
│  └────────┬────────┘  └───────┬────────┘  └──────────┬───────────┘  │
└───────────┼───────────────────┼──────────────────────┼──────────────┘
            │ REST/WebSocket    │ REST/WebSocket        │ WebSocket
┌───────────▼───────────────────▼──────────────────────▼──────────────┐
│                      APPLICATION LAYER (Tier 2)                      │
│                    Spring Boot Backend (Java 21)                      │
│  ┌─────────────┐  ┌───────────────┐  ┌────────────┐  ┌──────────┐  │
│  │ View Layer  │  │  Chat Module  │  │Queue Module│  │ Notif.   │  │
│  │ (Thymeleaf) │  │  (WebSocket)  │  │  (Redis)   │  │ Module   │  │
│  └─────────────┘  └──────┬────────┘  └────────────┘  └──────────┘  │
│                           │ HTTP gRPC                                │
│                    ┌──────▼────────┐                                 │
│                    │  AI Gateway   │ ◄── Route to AI Services        │
│                    └──────┬────────┘                                 │
└───────────────────────────┼──────────────────────────────────────────┘
                            │ REST API
┌───────────────────────────▼──────────────────────────────────────────┐
│                      AI SERVICE LAYER (Python)                        │
│  ┌──────────────────────────┐   ┌───────────────────────────────┐    │
│  │   NLP Service (FastAPI)  │   │  CV Service (FastAPI)         │    │
│  │  • Ollama qwen2.5-coder:3b (Phân luồng 2 giai đoạn) │   │  • BHYT Verification Engine      │    │
│  │  • Trợ lý tiếp đón & FAQ         │   │  • Đối soát BHYT chính chủ khớp CCCD   │    │
│  │  • Sentiment Analysis    │   │  • Image Moderation           │    │
│  └──────────────────────────┘   └───────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
            │
┌───────────▼──────────────────────────────────────────────────────────┐
│                       DATA LAYER (Tier 3)                             │
│  ┌───────────────┐  ┌─────────────┐  ┌────────────┐  ┌──────────┐  │
│  │  PostgreSQL   │  │    Redis    │  │  MinIO /   │  │Rabbit MQ │  │
│  │  (Main DB)    │  │  (Cache &   │  │  AWS S3    │  │(Message  │  │
│  │               │  │   Queue)    │  │  (Images)  │  │ Broker)  │  │
│  └───────────────┘  └─────────────┘  └────────────┘  └──────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

### 1.2. Lựa chọn Công nghệ (Technology Stack)

| Tầng | Công nghệ | Lý do lựa chọn |
|------|-----------|----------------|
| **Frontend** | Thymeleaf + HTML5/CSS3 + Tailwind CSS | Giao diện tối ưu Mobile & Desktop, render trực tiếp từ Spring Boot |
| **Core Backend** | Spring Boot **4.1.1** (Java **21 LTS**) | Nền tảng Spring hiện đại, bảo mật cao, kiến trúc dịch vụ tinh gọn |
| **AI Engine** | Local LLM via Ollama (`qwen2.5-coder:3b`) | Xử lý suy luận cục bộ (:11434), bảo mật dữ liệu y tế, phân luồng 2 giai đoạn |
| **Database** | PostgreSQL **18** | Lưu trữ quan hệ ACID, hỗ trợ JSONB cho nhật ký AI và đối soát CCCD-BHYT |
| **Security & Auth** | Spring Security 6 (Session-based) | Định danh bệnh nhân bằng CCCD 12 số, phân quyền chặt chẽ PATIENT/NURSE/ADMIN |
| **BHYT Engine** | 3-Layer Verification Service | Chuẩn hóa thẻ BHYT 15 ký tự và đối soát chính chủ với CCCD |

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU (Database Design)

> [!NOTE]
> **Về tài khoản bệnh nhân:** Hệ thống KHÔNG xây dựng chức năng Admin tạo tài khoản bệnh nhân thông qua UI. Thay vào đó, nhóm sẽ **cài sẵn dữ liệu mẫu (Seed Data)** — gồm các tài khoản bệnh nhân với nhiều mức BHYT khác nhau — vào cơ sở dữ liệu trước khi báo cáo. Thầy cô có thể dùng trực tiếp các tài khoản này để test hệ thống.

### 2.1. ERD — Entity Relationship Diagram (Đã cập nhật)

```
┌──────────────────────────┐        ┌────────────────────┐       ┌──────────────────┐
│          USERS           │        │  VISIT_RECORDS     │       │   DEPARTMENTS    │
├──────────────────────────┤        ├────────────────────┤       ├──────────────────┤
│ PK  id (UUID)            │───────<│ PK  id (UUID)      │>──────│ PK  id (UUID)    │
│     bhyt_number  [UNIQUE]│        │ FK  patient_id     │       │     name         │
│  ★  (Mã BHYT hệ thống   │        │ FK  department_id  │       │     code         │
│      cấp, dùng đăng nhập)│        │ FK  doctor_id      │       │     building     │
│     password_hash        │        │     visit_name     │       │     floor        │
│     full_name            │        │  ★ (VD: Khám tổng  │       │     room_numbers │
│     phone                │        │      quát, Khám    │       │     location_guide│
│     email                │        │      ngoại trú)    │       │     is_active    │
│     dob                  │        │     diagnosis      │       └──────────────────┘
│     gender               │        │     visit_date     │
│     address              │        │     status         │       ┌──────────────────┐
│     role                 │        │     severity_level │       │   QUEUE_TICKETS  │
│   (PATIENT/NURSE/        │        │     original_cost  │       ├──────────────────┤
│    DOCTOR/ADMIN)         │        │  ★ (Giá gốc trước │       │ PK  id (UUID)    │
│ FK  department_id        │        │      khi trừ BH)   │       │ FK  patient_id   │
│  ★  (Bắt buộc với        │        │     insurance_paid │       │ FK  department_id│
│      Bác sĩ / Y tá)      │        │  ★ (BH trả bao    │       │     ticket_number│
│     avatar_url           │        │      nhiêu)        │       │     status       │
│  ★  (Mức % giảm của BH  │        │     final_cost     │       │ (WAITING/CALLED/ │
│      VD: 80%, 95%, 100%) │        │  ★ (Số tiền BN    │       │  DONE/CANCELLED) │
│  ★  bhyt_expiry_date    │        │      thực trả)     │       │  ★ severity      │
│  ★  (Hạn sử dụng thẻ BH)│        │     notes          │       │  (NORMAL/MEDIUM/ │
│     is_active            │        │     created_at     │       │   EMERGENCY)     │
│     created_by (FK Admin)│        │ FK  ticket_id      │       │     called_at    │
│     created_at           │        │  ★ (STT gốc)      │       │     done_at      │
│                          │        └────────┬───────────┘       │     created_at   │
└──────────────────────────┘                 │                   └──────────────────┘
         │                          ┌────────▼───────────┐
         │                          │   PRESCRIPTIONS    │  ← Bảng thuốc chi tiết
         │                          ├────────────────────┤
         │                          │ PK  id (UUID)      │
         │                          │ FK  visit_id       │
         │                          │     medicine_name  │
         │                          │  ★ (Tên thuốc)    │
         │                          │     dosage         │
         │                          │  ★ (Liều dùng)    │
         │                          │     quantity       │
         │                          │     unit_price     │
         │                          │     total_price    │
         │                          │     instructions   │
         │                          │  ★ (Hướng dẫn    │
         │                          │      uống thuốc)   │
         │                          │     created_at     │
         │                          └────────────────────┘
         │
         │              ┌──────────────────┐
         │              │  CHAT_SESSIONS   │
         │              ├──────────────────┤
         └─────────────<│ PK  id (UUID)    │
                        │ FK  user_id      │
                        │     started_at   │
                        │     ended_at     │
                        └───────┬──────────┘
                                │
                        ┌───────▼──────────┐       ┌──────────────────────┐
                        │  CHAT_MESSAGES   │       │  AI_ANALYSIS_LOGS    │
                        ├──────────────────┤       ├──────────────────────┤
                        │ PK  id (UUID)    │       │ PK  id (UUID)        │
                        │ FK  session_id   │       │ FK  message_id       │
                        │     sender_role  │       │     ai_model_used    │
                        │   (USER/BOT/     │       │     input_text       │
                        │    SYSTEM)       │       │     predicted_dept   │
                        │     content      │       │     predicted_dept   │
                        │     message_type │       │     confidence_score │
                        │  (TEXT/IMAGE)    │       │     sentiment        │
                        │     media_url    │       │     is_corrected     │
                        │     created_at   │       │     corrected_dept   │
                        └──────────────────┘       │     processing_ms    │
                                                   │     created_at       │
                                                   └──────────────────────┘
```

### 2.2. Mô tả các bảng dữ liệu chính

**Bảng `USERS`** *(Cập nhật)*: Lưu thông tin tất cả người dùng. Các trường mới được thêm:
- `bhyt_number`: Mã BHYT **do hệ thống sinh ra**, là **username** để đăng nhập (thay thế email/CCCD). Unique, không thể trùng.
- `bhyt_discount_rate`: Mức % bảo hiểm chi trả (VD: `0.80` = 80%, `0.95` = 95%). Admin nhập khi tạo tài khoản.
- `bhyt_expiry_date`: Ngày hết hạn thẻ BHYT. Hệ thống cảnh báo khi sắp hết hạn.
- `created_by`: FK trỏ về Admin đã tạo tài khoản này.
- `department_id`: FK trỏ đến bảng `DEPARTMENTS`. Bắt buộc đối với role là `DOCTOR` hoặc `NURSE` để biết họ trực thuộc Khoa nào.

**Bảng `VISIT_RECORDS`** *(Đổi tên từ MEDICAL_RECORDS, cập nhật)*: Mỗi bản ghi là một lần khám bệnh. Các trường quan trọng:
- `ticket_id`: FK trỏ về `QUEUE_TICKETS`. Liên kết bệnh án với số thứ tự ban đầu và chẩn đoán AI.
- `visit_name`: Tên lần khám.
- `original_cost`: Giá gốc tổng cộng.
- `insurance_paid`: Số tiền bảo hiểm đã chi trả (dựa trên chi tiết từng dịch vụ/thuốc).
- `final_cost`: Số tiền bệnh nhân **thực trả**.
- `diagnosis`: Chẩn đoán bệnh.

**Bảng `DEPARTMENTS`** *(Cập nhật)*: Danh sách các Khoa/Phòng trong bệnh viện. Hỗ trợ tính năng Chỉ dẫn đường đi (Cấp độ 1).
- `building`: Tòa nhà hoặc Khu vực (VD: "Tòa nhà A", "Khu B").
- `floor`: Tầng mấy (VD: 3).
- `room_numbers`: Số phòng (VD: "301-305").
- `location_guide`: Câu văn chỉ dẫn đường đi chi tiết từ cổng/sảnh chính (VD: *"Từ sảnh chính đi thang máy A lên tầng 3, rẽ phải"*).
> **Ghi chú:** Dữ liệu Khoa/Phòng và Chỉ dẫn được cài sẵn bằng **Seed Data** từ đầu, không có chức năng Admin chỉnh sửa qua UI.

**Bảng `MEDICAL_SERVICES`** *(Mới)*: Danh mục dịch vụ y tế, xét nghiệm (Tên, Giá, `is_bhyt_covered`, `bhyt_price_limit`).

**Bảng `VISIT_SERVICES`** *(Mới)*: Chi tiết các dịch vụ bệnh nhân sử dụng trong lần khám. Ghi rõ phần BHYT trả (`bhyt_pay`) và phần bệnh nhân trả (`patient_co_pay`).

**Bảng `MEDICINES`** *(Mới)*: Danh mục thuốc (Tên, Giá, `is_bhyt_covered`, `bhyt_price_limit`).

**Bảng `PRESCRIPTIONS`** *(Cập nhật)*: Chi tiết thuốc bệnh nhân được kê, liên kết với bảng `MEDICINES`. Ghi rõ `bhyt_pay` và `patient_co_pay`.

**Bảng `QUEUE_TICKETS`**: Trái tim của module phân luồng. Số `ticket_number` tự tăng, reset về 1 mỗi ngày.
- `severity`: Mức độ khẩn cấp **do AI dự đoán ban đầu** (`NORMAL` / `MEDIUM` / `EMERGENCY`). Dùng để Y tá ưu tiên gọi số bệnh nhân khẩn cấp lên trước.

**Bảng `AI_ANALYSIS_LOGS`**: Ghi lại kết quả dự đoán của AI và kết quả thực tế (y tá có sửa hay không). Phục vụ báo cáo độ chính xác AI.

**Bảng `CHAT_MESSAGES`**: Cột `sender_role` gồm 3 giá trị:
- `USER`: Tin bệnh nhân gửi.
- `BOT`: Phản hồi từ AI (PhoBERT / FAQ model).
- `SYSTEM`: Thông báo tự động của hệ thống (VD: "Phiếu khám số 12 của bạn đã được tạo!", "Đến lượt bạn rồi!").

---

## 3. THIẾT KẾ API (API Design)

### 3.1. REST API Endpoints (Spring Boot Backend)

#### 🔐 Auth APIs
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/auth/login` | Đăng nhập bằng **Mã BHYT + mật khẩu**, trả về JWT | Public |
| `POST` | `/api/v1/auth/refresh-token` | Làm mới Access Token | JWT |
| `POST` | `/api/v1/auth/logout` | Thu hồi token | JWT |
| `POST` | `/api/v1/auth/change-password` | Bệnh nhân đổi mật khẩu lần đầu đăng nhập | JWT/PATIENT |

#### 👤 Patient Account APIs
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/api/v1/admin/patients` | Danh sách toàn bộ bệnh nhân | ADMIN/NURSE |
| `PUT` | `/api/v1/admin/patients/{id}` | Cập nhật thông tin, mức giảm BH, hạn sử dụng | ADMIN |
| `PUT` | `/api/v1/admin/patients/{id}/deactivate` | Khóa tài khoản bệnh nhân | ADMIN |

> **Ghi chú:** Tài khoản bệnh nhân được **cài sẵn bằng Seed Data** vào CSDL trước khi báo cáo. Không có UI Admin để tạo tài khoản bệnh nhân mới trong phạm vi đồ án này.

#### 👤 Patient Profile APIs (Bệnh nhân tự xem)
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/api/v1/patients/me` | Xem **thông tin cá nhân** (bao gồm mức BH, hạn BH) | PATIENT |
| `PUT` | `/api/v1/patients/me` | Cập nhật SĐT, email, ảnh đại diện | PATIENT |

#### 🏥 Visit History APIs (Lịch sử khám bệnh)
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/api/v1/patients/me/visits` | Danh sách lần khám (tên khám + giá cuối) | PATIENT |
| `GET` | `/api/v1/patients/me/visits/{visitId}` | **Chi tiết lần khám**: bác sĩ, khoa, chẩn đoán, tiền gốc, BH trả, thực trả | PATIENT |
| `GET` | `/api/v1/patients/me/visits/{visitId}/prescriptions` | **Đơn thuốc chi tiết** của lần khám | PATIENT |

#### 💬 Chat APIs
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/chat/sessions` | Tạo phiên chat mới | PATIENT |
| `GET` | `/api/v1/chat/sessions` | Lấy danh sách phiên chat | PATIENT |
| `GET` | `/api/v1/chat/sessions/{id}/messages` | Lấy lịch sử tin nhắn | PATIENT |
| `POST` | `/api/v1/chat/messages/image` | Upload ảnh vào chat | PATIENT |

#### 🎫 Queue APIs
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/tickets` | Phát số, tạo phiếu khám | PATIENT |
| `GET` | `/api/v1/tickets/mine` | Xem phiếu khám hiện tại | PATIENT |
| `DELETE` | `/api/v1/tickets/{id}` | Hủy phiếu khám | PATIENT |
| `GET` | `/api/v1/queue/{deptId}` | Xem hàng đợi của khoa | NURSE |
| `PUT` | `/api/v1/queue/{ticketId}/call` | Gọi số tiếp theo | NURSE |
| `PUT` | `/api/v1/queue/{ticketId}/transfer` | Chuyển khoa | NURSE |

#### 🏥 Departments & Directions APIs
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/api/v1/departments` | Lấy danh sách các khoa phòng đang mở | Public |
| `GET` | `/api/v1/departments/{id}/directions` | Lấy thông tin **Chỉ dẫn đường đi** (Tòa nhà, Tầng, Phòng, `location_guide`) | Public |

#### 🤖 AI Gateway APIs (proxy đến AI Service)
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/ai/analyze-symptom` | Phân tích triệu chứng | Internal |
| `POST` | `/api/v1/ai/faq` | Trả lời câu hỏi FAQ | Internal |
| `POST` | `/api/v1/ai/extract-bhyt` | OCR trích xuất mã BHYT | Internal |
| `POST` | `/api/v1/ai/detect-severity` | Phát hiện mức độ khẩn cấp | Internal |

### 3.2. WebSocket Endpoints (STOMP Protocol)

| Topic/Destination | Chiều | Mô tả |
|-------------------|-------|-------|
| `/app/chat.send` | Client → Server | Gửi tin nhắn chat |
| `/topic/chat/{sessionId}` | Server → Client | Nhận phản hồi từ chatbot |
| `/topic/queue/{deptId}` | Server → Client | Cập nhật hàng đợi realtime |
| `/user/{userId}/queue/notification` | Server → Client | Thông báo đến lượt cá nhân |
| `/topic/display/{deptId}` | Server → Client | Màn hình hành lang |

---

## 4. LUỒNG XỬ LÝ AI (AI Processing Flows)

### 4.1. Luồng Phân tích Triệu chứng & Phân luồng Khoa (PhoBERT)

```
Bệnh nhân nhập mô tả triệu chứng
            │
            ▼
┌───────────────────────┐
│  WebSocket Handler    │
│  (Spring Boot)        │
│  Nhận tin nhắn văn bản│
└──────────┬────────────┘
           │ HTTP POST /analyze-symptom
           ▼
┌───────────────────────────────────────────────────────┐
│               NLP Service (FastAPI - Python)           │
│                                                       │
│  1. Tiền xử lý văn bản                               │
│     ├─ Làm sạch: xóa emoji, ký tự đặc biệt          │
│     ├─ Chuẩn hóa: viết tắt → từ đầy đủ              │
│     └─ Word Segmentation: VnCoreNLP                  │
│                                                       │
│  2. Phân loại Intent                                 │
│     ├─ Intent = FAQ?  → FAQ Handler (PhoBERT FAQ)    │
│     └─ Intent = SYMPTOM? → Triage Model              │
│                                                       │
│  3. PhoBERT Triage Model                             │
│     ├─ Input: tokenized text                         │
│     ├─ Model: vinai/phobert-base fine-tuned          │
│     ├─ Output: [dept_id, confidence_score, severity] │
│     └─ Threshold: confidence < 0.7 → hỏi thêm       │
│                                                       │
│  4. Severity Detection (Multi-task)                  │
│     └─ Output: EMERGENCY 🔴 / MEDIUM 🟡 / NORMAL 🟢 │
└──────────────────────────┬────────────────────────────┘
                           │ JSON Response
                           ▼
              ┌─────────────────────────┐
              │  Spring Boot xử lý      │
              │  • Lưu AI_ANALYSIS_LOGS │
              │  • Nếu EMERGENCY: ưu   │
              │    tiên hàng đợi        │
              │  • Gợi ý xác nhận khoa  │
              └──────────┬──────────────┘
                         │ WebSocket Push
                         ▼
              Bệnh nhân nhận phản hồi:
              "Dựa trên triệu chứng của bạn,
               hệ thống gợi ý khám tại
               Khoa Nội Thần kinh (87% phù hợp).
               Bạn có muốn xác nhận không?"
```

### 4.2. Luồng Xác thực Thẻ BHYT Chính chủ (3 Lớp & Khớp CCCD)

```
Bệnh nhân nhập mã thẻ BHYT 15 ký tự (chuẩn QĐ 1666)
            │
            ▼
┌──────────────────────────────────────────────────┐
│  Spring Boot Backend — BhytController            │
│  POST /api/bhyt/apply                            │
└──────────┬───────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────┐
│  BƯỚC 1: Validate Định dạng 15 ký tự             │
│  ├─ 2 chữ cái đầu: Mã đối tượng (TE, HN, DN...)  │
│  ├─ Ký tự 3: Mức hưởng (1-4 -> 100%, 95%, 80%)   │
│  ├─ Ký tự 4-5: Mã tỉnh/thành phố (01-99)         │
│  └─ 10 số cuối: Số thứ tự BHXH duy nhất          │
└──────────┬───────────────────────────────────────┘
           │ Format hợp lệ
           ▼
┌──────────────────────────────────────────────────┐
│  BƯỚC 2: Tra cứu CSDL & Cổng BHXH               │
│  ├─ Tra cứu User có sẵn trong CSDL nội bộ        │
│  └─ Nếu chưa có: Gọi Mock BHXH Gateway API       │
└──────────┬───────────────────────────────────────┘
           │ Thẻ hợp lệ & còn hạn
           ▼
┌──────────────────────────────────────────────────┐
│  BƯỚC 3: Đối soát Quyền sở hữu (Chống Gian Lận)  │
│  ├─ Lấy số CCCD của tài khoản đang đăng nhập     │
│  ├─ So sánh thẻ BHYT với số liên kết của CCCD    │
│  ├─ NẾU KHÔNG KHỚP -> Trả về HTTP 403 Forbidden  │
│  └─ NẾU KHỚP -> Áp dụng giảm trừ viện phí 100%   │
└──────────────────────────────────────────────────┘
```
