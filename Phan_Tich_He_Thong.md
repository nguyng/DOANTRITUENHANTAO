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
│  │  (React.js)     │  │ (React.js)     │  │ (Public Display)     │  │
│  └────────┬────────┘  └───────┬────────┘  └──────────┬───────────┘  │
└───────────┼───────────────────┼──────────────────────┼──────────────┘
            │ REST/WebSocket    │ REST/WebSocket        │ WebSocket
┌───────────▼───────────────────▼──────────────────────▼──────────────┐
│                      APPLICATION LAYER (Tier 2)                      │
│                    Spring Boot Backend (Java 17)                      │
│  ┌─────────────┐  ┌───────────────┐  ┌────────────┐  ┌──────────┐  │
│  │ Auth Module │  │  Chat Module  │  │Queue Module│  │ Notif.   │  │
│  │  (JWT/OAuth)│  │  (WebSocket)  │  │  (Redis)   │  │ Module   │  │
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
│  │  • PhoBERT (Phân luồng) │   │  • YOLOv8 (Detect BHYT)      │    │
│  │  • PhoBERT (FAQ)         │   │  • Tesseract/VietOCR (OCR)   │    │
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
| **Frontend** | React.js + TypeScript + Tailwind | Component-based, dễ quản lý state phức tạp |
| **Backend** | Spring Boot 3.x (Java 17) | Robust, hỗ trợ WebSocket tốt, phân quyền dễ |
| **AI Service** | FastAPI (Python 3.10+) | Native với thư viện ML (PyTorch, HuggingFace) |
| **Database** | PostgreSQL 15 | ACID, JSON support, tốt cho dữ liệu quan hệ |
| **Cache & Queue** | Redis 7 | Lưu session, quản lý hàng đợi STT |
| **File Storage** | MinIO (self-hosted S3) | Lưu ảnh thẻ BHYT, OCR output |
| **Real-time** | WebSocket (STOMP/SockJS) | Thông báo đẩy, cập nhật hàng đợi |
| **Auth** | JWT + Spring Security | Phân quyền đa cấp, stateless |
| **Containerize** | Docker + Docker Compose | Dễ deploy, isolate các service |

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU (Database Design)

> [!IMPORTANT]
> **Thay đổi thiết kế:** Hệ thống KHÔNG có chức năng tự đăng ký. Admin (bệnh viện) là người **cấp tài khoản** cho bệnh nhân dựa trên **Mã BHYT do hệ thống sinh ra**, tương tự mô hình nhà nước cấp CCCD. Bệnh nhân chỉ thực hiện **đăng nhập**.

### 2.1. ERD — Entity Relationship Diagram (Đã cập nhật)

```
┌──────────────────────────┐        ┌────────────────────┐       ┌──────────────────┐
│          USERS           │        │  VISIT_RECORDS     │       │   DEPARTMENTS    │
├──────────────────────────┤        ├────────────────────┤       ├──────────────────┤
│ PK  id (UUID)            │───────<│ PK  id (UUID)      │>──────│ PK  id (UUID)    │
│     bhyt_number  [UNIQUE]│        │ FK  patient_id     │       │     name         │
│  ★  (Mã BHYT hệ thống   │        │ FK  department_id  │       │     code         │
│      cấp, dùng đăng nhập)│        │ FK  doctor_id      │       │     description  │
│     password_hash        │        │     visit_name     │       │     floor        │
│     full_name            │        │  ★ (VD: Khám tổng  │       │     room_numbers │
│     phone                │        │      quát, Khám    │       │     is_active    │
│     email                │        │      ngoại trú)    │       │     created_at   │
│     dob                  │        │     diagnosis      │       └──────────────────┘
│     gender               │        │     visit_date     │
│     address              │        │     status         │       ┌──────────────────┐
│     role                 │        │     severity_level │       │   QUEUE_TICKETS  │
│   (PATIENT/NURSE/        │        │     original_cost  │       ├──────────────────┤
│    DOCTOR/ADMIN)         │        │  ★ (Giá gốc trước │       │ PK  id (UUID)    │
│     avatar_url           │        │      khi trừ BH)   │       │ FK  patient_id   │
│                          │        │     insurance_paid │       │ FK  department_id│
│  === Thông tin BHYT ===  │        │  ★ (BH trả bao    │       │     ticket_number│
│  ★  bhyt_discount_rate  │        │      nhiêu)        │       │     status       │
│  ★  (Mức % giảm của BH  │        │     final_cost     │       │ (WAITING/CALLED/ │
│      VD: 80%, 95%, 100%) │        │  ★ (Số tiền BN    │       │  DONE/CANCELLED) │
│  ★  bhyt_expiry_date    │        │      thực trả)     │       │     called_at    │
│  ★  (Hạn sử dụng thẻ BH)│        │     notes          │       │     done_at      │
│     is_active            │        │     created_at     │       │     created_at   │
│     created_by (FK Admin)│        └────────┬───────────┘       └──────────────────┘
│     created_at           │                 │
└──────────────────────────┘                 │ 1:N
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
                        │   (USER/BOT)     │       │     input_text       │
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

**Bảng `VISIT_RECORDS`** *(Đổi tên từ MEDICAL_RECORDS, cập nhật)*: Mỗi bản ghi là một lần khám bệnh. Các trường quan trọng:
- `visit_name`: Tên lần khám.
- `original_cost`: Giá gốc tổng cộng.
- `insurance_paid`: Số tiền bảo hiểm đã chi trả (dựa trên chi tiết từng dịch vụ/thuốc).
- `final_cost`: Số tiền bệnh nhân **thực trả**.
- `diagnosis`: Chẩn đoán bệnh.

**Bảng `MEDICAL_SERVICES`** *(Mới)*: Danh mục dịch vụ y tế, xét nghiệm (Tên, Giá, `is_bhyt_covered`, `bhyt_price_limit`).

**Bảng `VISIT_SERVICES`** *(Mới)*: Chi tiết các dịch vụ bệnh nhân sử dụng trong lần khám. Ghi rõ phần BHYT trả (`bhyt_pay`) và phần bệnh nhân trả (`patient_co_pay`).

**Bảng `MEDICINES`** *(Mới)*: Danh mục thuốc (Tên, Giá, `is_bhyt_covered`, `bhyt_price_limit`).

**Bảng `PRESCRIPTIONS`** *(Cập nhật)*: Chi tiết thuốc bệnh nhân được kê, liên kết với bảng `MEDICINES`. Ghi rõ `bhyt_pay` và `patient_co_pay`.

**Bảng `QUEUE_TICKETS`**: Trái tim của module phân luồng. Số `ticket_number` tự tăng, reset về 1 mỗi ngày.

**Bảng `AI_ANALYSIS_LOGS`**: Ghi lại kết quả dự đoán của AI và kết quả thực tế (y tá có sửa hay không). Phục vụ báo cáo độ chính xác AI.

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

#### 👤 Patient Account APIs (Admin quản lý)
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/admin/patients` | **Admin tạo tài khoản** bệnh nhân mới, hệ thống sinh Mã BHYT | ADMIN |
| `PUT` | `/api/v1/admin/patients/{id}` | Cập nhật thông tin, mức giảm BH, hạn sử dụng | ADMIN |
| `PUT` | `/api/v1/admin/patients/{id}/deactivate` | Khóa tài khoản bệnh nhân | ADMIN |
| `GET` | `/api/v1/admin/patients` | Danh sách toàn bộ bệnh nhân | ADMIN/NURSE |

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

### 4.2. Luồng OCR Trích xuất Thẻ BHYT (YOLOv8 + OCR)

```
Bệnh nhân gửi ảnh thẻ BHYT
            │
            ▼
┌───────────────────────┐
│  API Controller       │
│  POST /chat/image     │
│  Lưu ảnh lên MinIO    │
└──────────┬────────────┘
           │ POST image_url
           ▼
┌──────────────────────────────────────────────────┐
│          CV Service (FastAPI - Python)            │
│                                                  │
│  BƯỚC 1: Image Pre-processing                   │
│  ├─ Resize về 640×640                           │
│  ├─ Normalize pixel values                      │
│  └─ Augment: contrast, sharpening               │
│                                                  │
│  BƯỚC 2: YOLOv8 Detection                      │
│  ├─ Phát hiện class "bhyt_card"                │
│  ├─ Crop bounding box vùng mã số               │
│  └─ Confidence threshold: > 0.85               │
│                                                  │
│  BƯỚC 3: OCR (VietOCR / Tesseract-vie)         │
│  ├─ Input: Cropped card image                   │
│  ├─ Pre-process: grayscale, threshold, denoise  │
│  ├─ Extract text: "DN4123456789"               │
│  └─ Validate: regex pattern thẻ BHYT            │
│                                                  │
│  BƯỚC 4: Post-processing                       │
│  └─ Trả về: { bhyt_code, confidence, raw_text }│
└──────────────────────┬───────────────────────────┘
                       │ JSON
                       ▼
           Spring Boot:
           • Tự điền bhyt_number vào form
           • Xác nhận với bệnh nhân qua chat
```

---

## 5. THIẾT KẾ GIAO DIỆN (UI/UX Design)

### 5.1. Sơ đồ màn hình (Screen Map)

```
PUBLIC (Chưa đăng nhập)
├── /login                ← Đăng nhập bằng Mã BHYT + mật khẩu
└── /display/:deptId      ← Màn hình hành lang (Public)

BỆNH NHÂN (PATIENT role) — Sau khi đăng nhập
├── /chat                 ← Giao diện Chat chính với AI
├── /ticket               ← Xem phiếu khám & STT hiện tại
│
├── /profile              ← 📋 THÔNG TIN CÁ NHÂN
│   ├── Họ tên, SĐT, Email, Ngày sinh, Giới tính, Địa chỉ
│   ├── ★ Mã BHYT (do hệ thống cấp, không đổi được)
│   ├── ★ Mức giảm bảo hiểm (VD: 80%)
│   └── ★ Hạn sử dụng thẻ BHYT (có cảnh báo sắp hết hạn)
│
└── /visits               ← 🏥 LỊCH SỬ KHÁM BỆNH
    ├── [Danh sách] Mỗi item: Tên lần khám + Ngày + Giá thực trả
    └── /visits/:visitId  ← Chi tiết lần khám
        ├── Tên lần khám (VD: Khám tổng quát)
        ├── Khoa khám
        ├── Bác sĩ phụ trách
        ├── Chẩn đoán bệnh
        ├── Giá gốc (trước BH)
        ├── Bảo hiểm trả (giá gốc × mức %)
        ├── Số tiền bệnh nhân thực trả
        └── Đơn thuốc chi tiết (tên thuốc, liều dùng, số lượng, giá)

Y TÁ / LỄ TÂN (NURSE role)
├── /nurse/queue          ← Dashboard hàng đợi chính
├── /nurse/queue/:deptId  ← Hàng đợi chi tiết từng khoa
└── /nurse/search         ← Tra cứu bệnh nhân

QUẢN TRỊ VIÊN (ADMIN role)
├── /admin/dashboard      ← Thống kê tổng quan
├── /admin/patients       ← ★ Quản lý & CẤP tài khoản bệnh nhân
├── /admin/departments    ← Quản lý Khoa/Phòng
├── /admin/staff          ← Quản lý tài khoản nhân viên
├── /admin/faq            ← Cấu hình câu trả lời tự động
├── /admin/reports        ← Báo cáo & xuất file
└── /admin/logs           ← Nhật ký hệ thống
```

### 5.2. Mô tả Màn hình chính — Giao diện Chat AI

```
┌──────────────────────────────────────────────────────────┐
│  🏥 MedAssist AI        [Bệnh viện Đa khoa X]  [👤 Lan]  │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │                                                    │  │
│  │  🤖 Xin chào Nguyễn Thị Lan! Tôi là MedBot,      │  │
│  │     trợ lý ảo của bệnh viện. Hôm nay bạn có      │  │
│  │     thể mô tả triệu chứng hoặc đặt câu hỏi.      │  │
│  │                                          11:20 AM  │  │
│  │                                                    │  │
│  │          [👤] Tôi bị đau đầu từ sáng, kèm         │  │
│  │               sốt nhẹ 38 độ và chóng mặt         │  │
│  │                                          11:21 AM  │  │
│  │                                                    │  │
│  │  🤖 Tôi đã phân tích triệu chứng của bạn.         │  │
│  │     ─────────────────────────────────────         │  │
│  │     🟡 Mức độ: Trung bình                         │  │
│  │     🏥 Gợi ý khoa: Khoa Nội Thần kinh            │  │
│  │     📊 Độ phù hợp: 89%                           │  │
│  │     ─────────────────────────────────────         │  │
│  │     Bạn có muốn đăng ký khám tại Khoa này?       │  │
│  │     [✅ Xác nhận đăng ký] [🔄 Đổi khoa]          │  │
│  │                                          11:21 AM  │  │
│  │                                                    │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  Câu hỏi gợi ý:                                         │
│  [Giờ làm việc?] [Cần mang giấy tờ gì?] [Chi phí?]     │
│                                                          │
├──────────────────────────────────────────────────────────┤
│  [📎 Gửi ảnh]  [    Nhập tin nhắn...        ] [➤ Gửi]  │
└──────────────────────────────────────────────────────────┘
```

---

## 6. SƠ ĐỒ USE CASE

### 6.1. Use Case Tổng quát

```
                         ┌──────────────────────────────────────────────────┐
                         │              HỆ THỐNG MedAssist                   │
                         │                                                  │
  ┌─────────────┐        │  ┌────────────────────────────────────────────┐  │
  │             │        │  │  UC-01: Đăng nhập bằng Mã BHYT + mật khẩu │  │
  │  BỆNH NHÂN  │───────►│  │  UC-02: Đổi mật khẩu lần đầu đăng nhập    │  │
  │             │        │  │  UC-03: Xem thông tin cá nhân & BH         │  │
  │             │───────►│  │  UC-04: Xem lịch sử khám bệnh (danh sách)  │  │
  │             │───────►│  │  UC-05: Xem chi tiết lần khám + đơn thuốc  │  │
  │             │───────►│  │  UC-06: Chat với Trợ lý ảo AI              │  │
  │             │───────►│  │  UC-07: Gửi ảnh thẻ BHYT (OCR nhận diện)  │  │
  │             │───────►│  │  UC-08: Nhận phân luồng Khoa từ AI         │  │
  │             │───────►│  │  UC-09: Bốc số / Xem phiếu khám            │  │
  │             │◄───────│  │  UC-10: Nhận thông báo đến lượt            │  │
  └─────────────┘        │  │  UC-11: Hủy phiếu khám                     │  │
                         │  └────────────────────────────────────────────┘  │
  ┌─────────────┐        │  ┌────────────────────────────────────────────┐  │
  │             │        │  │  UC-12: Xem hàng đợi theo khoa             │  │
  │   Y TÁ /   │───────►│  │  UC-13: Gọi số tiếp theo                   │  │
  │  LỄ TÂN    │───────►│  │  UC-14: Điều chuyển bệnh nhân sang khoa    │  │
  │             │───────►│  │  UC-15: Tra cứu hồ sơ bệnh nhân            │  │
  └─────────────┘        │  └────────────────────────────────────────────┘  │
                         │  ┌────────────────────────────────────────────┐  │
  ┌─────────────┐        │  │  ★UC-16: Tạo & cấp tài khoản bệnh nhân   │  │
  │             │───────►│  │         (sinh Mã BHYT, nhập mức BH, hạn BH)│ │
  │   ADMIN     │───────►│  │  UC-17: Quản lý Khoa/Phòng                │  │
  │             │───────►│  │  UC-18: Quản lý tài khoản nhân viên        │  │
  │             │───────►│  │  UC-19: Xem thống kê & báo cáo            │  │
  │             │───────►│  │  UC-20: Cấu hình câu FAQ                   │  │
  └─────────────┘        │  │  UC-21: Xem Logs hệ thống                  │  │
                         │  └────────────────────────────────────────────┘  │
  ┌─────────────┐        │  ┌────────────────────────────────────────────┐  │
  │  MÀN HÌNH  │◄───────│  │  UC-22: Hiển thị STT hành lang             │  │
  │  HÀNH LANG  │        │  │  UC-23: Text-to-Speech gọi số              │  │
  └─────────────┘        │  └────────────────────────────────────────────┘  │
                         └──────────────────────────────────────────────────┘
```

### 6.2. Mô tả chi tiết — Màn hình Thông tin cá nhân & Lịch sử khám

**Màn hình Thông tin cá nhân (`/profile`):**
```
┌───────────────────────────────────────────────────────┐
│  👤 Thông tin cá nhân                                │
├───────────────────────────────────────────────────────┤
│  [Ảnh đại diện]                                       │
│  Họ và tên:  Nguyễn Thị Lan                          │
│  Ngày sinh:  15/03/1990  |  Giới tính: Nữ            │
│  SĐT:        0901234567                               │
│  Email:      lan.nguyen@email.com                     │
│  Địa chỉ:   123 Lý Thường Kiệt, Q.10, TP.HCM        │
├───────────────────────────────────────────────────────┤
│  🏥 Thông tin Bảo hiểm Y tế                          │
│  Mã BHYT:       DN4 1234567890  [Không đổi được]     │
│  Mức giảm BH:   80%                                  │
│  Hạn sử dụng:   31/12/2026  ⚠️ Còn 3 tháng         │
└───────────────────────────────────────────────────────┘
```

**Màn hình Lịch sử khám (`/visits`):**
```
┌───────────────────────────────────────────────────────┐
│  🏥 Lịch sử Khám bệnh                               │
├───────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐ │
│  │  Khám tổng quát                    20/08/2026  │ │
│  │  Khoa Nội tổng quát               150,000 đ ►  │ │
│  └─────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────┐ │
│  │  Khám chuyên khoa Thần kinh        05/06/2026  │ │
│  │  Khoa Thần kinh                    60,000 đ ►  │ │
│  └─────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────┘

--- Khi nhấn vào một lần khám (Chi tiết /visits/:id) ---

┌───────────────────────────────────────────────────────┐
│  ← Khám tổng quát  —  20/08/2026                     │
├───────────────────────────────────────────────────────┤
│  Khoa:    Nội tổng quát — Phòng 201                  │
│  Bác sĩ:  BS. Trần Văn Minh                          │
│  Chẩn đoán: Viêm họng cấp, sốt virus                 │
├───────────────────────────────────────────────────────┤
│  💊 Đơn thuốc                                        │
│  ┌────────────────────────────┬──────┬───────────┐   │
│  │ Tên thuốc                  │ SL   │ Thành tiền│   │
│  ├────────────────────────────┼──────┼───────────┤   │
│  │ Paracetamol 500mg          │ 20 v │  40,000 đ │   │
│  │ Amoxicillin 500mg          │ 21 v │  63,000 đ │   │
│  │ Loratadine 10mg            │ 10 v │  25,000 đ │   │
│  └────────────────────────────┴──────┴───────────┘   │
├───────────────────────────────────────────────────────┤
│  💰 Chi phí                                          │
│  Giá gốc (khám + thuốc):      750,000 đ             │
│  Bảo hiểm chi trả (80%):    - 600,000 đ             │
│  ─────────────────────────────────────               │
│  Bệnh nhân thực trả:          150,000 đ             │
└───────────────────────────────────────────────────────┘
```

---

## 7. SEQUENCE DIAGRAM — Luồng chính

### 7.1. Bệnh nhân gửi triệu chứng → Nhận số thứ tự

```
Bệnh nhân    WebSocket     Spring Boot    NLP Service    Database    Redis Queue
     │            │              │               │             │           │
     │──send msg─►│              │               │             │           │
     │            │──save msg───►│               │             │           │
     │            │              │──POST analyze─►│             │           │
     │            │              │◄──{dept, conf}─│             │           │
     │            │              │──save AI log──►│             │           │
     │            │              │──push bot reply►              │           │
     │◄───────────────────────── "Khoa Thần kinh (89%)"        │           │
     │            │              │               │             │           │
     │──confirm──►│              │               │             │           │
     │            │──create ticket──────────────►│             │           │
     │            │              │               │──get next───────────────►│
     │            │              │               │◄──ticket_no #47──────────│
     │            │              │──save ticket──►│             │           │
     │            │              │──incr queue───────────────────────────── ►│
     │            │──push ticket►│               │             │           │
     │◄─────────── "STT #47 - Khoa TK - Phòng 302"            │           │
     │            │              │               │             │           │
     │            │──broadcast──►│               │     [Dashboard Y tá]    │
     │            │              │─────────────────── WebSocket push ──────►│
     │            │              │               │    "Có bệnh nhân mới"   │
```

---

## 8. CÁC MÔ HÌNH AI — Chi tiết Kỹ thuật

### 8.1. Mô hình NLP — PhoBERT Fine-tuned

| Thông số | Chi tiết |
|---------|----------|
| **Base Model** | `vinai/phobert-base` (HuggingFace) |
| **Task 1** | Text Classification: Phân luồng Khoa (10–15 classes) |
| **Task 2** | Sequence Classification: Phân loại Intent (FAQ / SYMPTOM / OTHER) |
| **Task 3** | Sentiment Analysis: Phát hiện lo lắng/hoảng loạn |
| **Framework** | PyTorch + HuggingFace Transformers |
| **Input** | Chuỗi văn bản tiếng Việt đã segment (max 256 tokens) |
| **Output** | `{class_label, confidence_score, top_3_predictions}` |
| **Training** | Fine-tune 3–5 epochs trên dataset triệu chứng y tế tiếng Việt |
| **Metrics** | Accuracy, F1-Score, Confusion Matrix |

### 8.2. Mô hình Computer Vision — YOLOv8

| Thông số | Chi tiết |
|---------|----------|
| **Base Model** | `yolov8n` hoặc `yolov8s` (Ultralytics) |
| **Task** | Object Detection: detect vùng thẻ BHYT và vùng mã số |
| **Input Size** | 640 × 640 pixels |
| **Classes** | `bhyt_card_region`, `code_region` |
| **Dataset** | ~500 ảnh thẻ BHYT, augmented ×5 = ~2500 samples |
| **OCR Engine** | VietOCR (Transformer-based, tối ưu tiếng Việt) |
| **Output** | `{bounding_box, bhyt_code (string), confidence}` |
| **Validation** | Regex: `^[A-Z]{2}[0-9]{10}$` |

---

## 9. BẢO MẬT & PHI CÔNG NGHỆ (Security Design)

### 9.1. Chiến lược Bảo mật

| Vấn đề | Giải pháp |
|--------|-----------|
| **Xác thực người dùng** | JWT Access Token (15 phút) + Refresh Token (7 ngày) |
| **Phân quyền** | Spring Security với `@PreAuthorize`, Role-Based Access Control |
| **Dữ liệu nhạy cảm** | Mã hóa CCCD, mã BHYT trong DB (AES-256) |
| **Upload ảnh** | Giới hạn size (5MB), chỉ chấp nhận JPEG/PNG, scan virus |
| **Kiểm duyệt nội dung** | YOLOv8 phát hiện ảnh phản cảm trước khi lưu |
| **SQL Injection** | JPA/Hibernate parameterized queries |
| **CORS** | Chỉ cho phép origin của frontend |
| **Rate Limiting** | Giới hạn 60 requests/phút/IP trên API Gateway |

### 9.2. Tuân thủ quy định dữ liệu y tế

- Dữ liệu bệnh nhân tuân theo **Nghị định 13/2023/NĐ-CP** về bảo vệ dữ liệu cá nhân tại Việt Nam.
- Ảnh thẻ BHYT sau khi OCR xong cần được xóa sau 24 giờ (có thể cấu hình).
- Admin có thể export dữ liệu ở dạng ẩn danh (anonymized) cho mục đích báo cáo thống kê.

---

## 10. KẾ HOẠCH TRIỂN KHAI (Deployment Plan)

### 10.1. Môi trường Development (Docker Compose)

```yaml
# docker-compose.yml (tóm tắt)
services:
  postgres:     # PostgreSQL database
  redis:        # Redis cache + queue
  minio:        # File storage
  backend:      # Spring Boot app (port 8080)
  nlp-service:  # FastAPI NLP (port 8001)
  cv-service:   # FastAPI CV (port 8002)
  frontend:     # React.js (port 3000)
  nginx:        # Reverse proxy (port 80/443)
```

### 10.2. Cấu trúc thư mục dự án

```
medassist/
├── frontend/               # React.js App
│   ├── src/
│   │   ├── pages/          # Các trang chính
│   │   ├── components/     # Component dùng chung
│   │   ├── hooks/          # Custom hooks (useWebSocket...)
│   │   ├── store/          # Redux/Zustand store
│   │   └── services/       # API calls
├── backend/                # Spring Boot App
│   ├── src/main/java/
│   │   ├── auth/           # Module xác thực
│   │   ├── chat/           # Module chat + WebSocket
│   │   ├── queue/          # Module quản lý hàng đợi
│   │   ├── admin/          # Module quản trị
│   │   └── ai/             # AI Gateway
│   └── src/main/resources/
├── ai-services/
│   ├── nlp_service/        # FastAPI NLP
│   │   ├── models/         # PhoBERT weights
│   │   ├── routers/        # API routes
│   │   └── utils/          # Preprocessing utils
│   └── cv_service/         # FastAPI Computer Vision
│       ├── models/         # YOLOv8 weights
│       ├── routers/
│       └── utils/
└── docker-compose.yml
```

---

## 11. THIẾT KẾ DỮ LIỆU MÔ PHỎNG BHYT (Mock Data Design)

> [!NOTE]
> Hệ thống sử dụng **dữ liệu BHYT giả** cho mục đích mô phỏng và kiểm thử. Admin (người phát triển) đóng vai trò nhà nước — tự sinh mã BHYT và cấp tài khoản cho người dùng test. Trên thực tế, dữ liệu BHYT sẽ được nhập từ cơ quan BHYT cấp.

### 11.1. Cấu trúc Mã BHYT Mô phỏng

Mã BHYT được thiết kế theo chuẩn gần giống thực tế để dễ nhận biết nhóm đối tượng:

```
[ MÃ ĐỐI TƯỢNG ] [ MỨC HƯỞNG ] [ MÃ TỈNH ] [ SỐ THỨ TỰ ]
       HN               1             79         000000001
       CN               3             79         000000001
       DN               4             79         000000001
                        │
                        ├── 1 → Chi trả 100% (miễn phí hoàn toàn)
                        ├── 3 → Chi trả 95%  (bệnh nhân trả 5%)
                        └── 4 → Chi trả 80%  (bệnh nhân trả 20%)

Mã tỉnh: 79 = TP. Hồ Chí Minh (theo chuẩn BHXH Việt Nam)
```

### 11.2. Phân loại 9 Nhóm Đối tượng BHYT

| # | Mã | Nhóm đối tượng | Mức chi trả | Ví dụ Mã BHYT | Hạn thẻ | Ghi chú đặc biệt |
|---|----|----|:---:|---|---|---|
| 1 | `HN` | **Hộ nghèo** | **100%** | `HN17900000001` | Hàng năm | Bệnh nhân **không trả đồng nào** |
| 2 | `CN` | **Cận nghèo** | **95%** | `CN37900000001` | Hàng năm | Chỉ trả 5% phần còn lại |
| 3 | `NN` | Nông, lâm, ngư nghiệp | **80%** | `NN47900000001` | Hàng năm | |
| 4 | `TE` | Trẻ em dưới 6 tuổi | **100%** | `TE17900000001` | Theo sinh nhật | Miễn phí đến 6 tuổi |
| 5 | `HS` | Học sinh / Sinh viên | **80%** | `HS47900000001` | Theo năm học | |
| 6 | `DN` | Người lao động (DN đóng) | **80%** | `DN47900000001` | Hàng năm | Nhóm phổ biến nhất |
| 7 | `VC` | Người có công cách mạng | **100%** | `VC17900000001` | Vĩnh viễn | Không hết hạn |
| 8 | `NQ` | Người cao tuổi ≥ 80 tuổi | **100%** | `NQ17900000001` | Vĩnh viễn | Không hết hạn |
| 9 | `BT` | Hộ gia đình tự nguyện | **80%** | `BT47900000001` | Hàng năm | |

### 11.3. Logic Tính Tiền Theo Mức Hưởng (Có xét Danh mục BHYT và Giá trần)

*Giả định: Hệ thống luôn coi bệnh nhân khám Đúng tuyến (Lý tưởng nhất).*

**Thuật toán quét từng dịch vụ và thuốc:**
- Nếu `is_bhyt_covered = false` (Không thuộc danh mục BHYT chi trả):
  $\rightarrow$ Bệnh nhân tự trả 100%. `patient_co_pay = price`, `bhyt_pay = 0`.
- Nếu `is_bhyt_covered = true` (BHYT chi trả):
  $\rightarrow$ Xác định giá cơ sở để tính BHYT: $Price_{base} = \min(price, bhyt\_price\_limit)$
  $\rightarrow$ `bhyt_pay` = $Price_{base} \times bhyt\_discount\_rate$
  $\rightarrow$ `patient_co_pay` = $price - bhyt\_pay$

**Tổng kết hóa đơn (`VISIT_RECORDS`):**
- `original_cost` = Tổng `price` của tất cả dịch vụ + thuốc.
- `insurance_paid` = Tổng `bhyt_pay`.
- `final_cost` = Tổng `patient_co_pay`.

Ví dụ với một ca khám: Tiền khám 100k (BHYT trần 100k), Xét nghiệm ngoài danh mục 300k, Thuốc A 200k (BHYT trần 150k). Bệnh nhân hưởng mức 80% (Người LĐ):
- Tiền khám: BH trả 100k × 80% = 80k. Bệnh nhân trả 20k.
- Xét nghiệm (Ngoài DM): BH trả 0đ. Bệnh nhân trả 300k.
- Thuốc A: Giá tính BH là min(200k, 150k) = 150k. BH trả 150k × 80% = 120k. Bệnh nhân trả: 200k - 120k = 80k.
$\rightarrow$ **Tổng thanh toán:** Giá gốc = 600k. BH trả = 200k. Bệnh nhân trả = 400k.

### 11.4. Trường hợp Test Đặc biệt

| Loại test | Điều kiện | Kết quả hiển thị |
|-----------|-----------|------------------|
| **Hộ nghèo khám** | `bhyt_discount_rate = 1.00` | Badge "Miễn phí 100%" 🟢, `final_cost = 0đ` |
| **Cận nghèo khám** | `bhyt_discount_rate = 0.95` | Số tiền rất nhỏ, hiển thị rõ BH trả |
| **Thẻ hết hạn** | `bhyt_expiry_date < NOW()` | Cảnh báo đỏ 🔴, không áp dụng BH |
| **Sắp hết hạn** | `bhyt_expiry_date - 90 ngày` | Cảnh báo vàng ⚠️ "Còn N ngày" |
| **Thẻ vĩnh viễn** | `bhyt_expiry_date = 2099-12-31` | Không hiển thị hạn dùng |

### 11.5. Danh sách Tài khoản Test (Phát cho người test)

> [!IMPORTANT]
> Mật khẩu mặc định tất cả tài khoản bệnh nhân: **`Test@123`**
> Mật khẩu nhân viên: **`Admin@123`**
> Lần đầu đăng nhập bệnh nhân sẽ được yêu cầu **đổi mật khẩu**.

**Tài khoản Bệnh nhân test:**

| Nhóm | Mã BHYT (dùng để đăng nhập) | Họ tên | BH% | Hạn BH |
|---|---|---|:---:|---|
| Hộ nghèo | `HN17900000001` | Nguyễn Văn Nghèo | 100% | 31/12/2027 |
| Hộ nghèo | `HN17900000002` | Trần Thị Khổ | 100% | 30/06/2027 |
| Cận nghèo | `CN37900000001` | Phạm Thị Cận | 95% | 31/12/2027 |
| Cận nghèo | `CN37900000002` | Võ Văn Bần | 95% | 30/06/2026 |
| Trẻ em <6t | `TE17900000001` | Trần Bé Nam | 100% | 30/06/2028 |
| Học sinh/SV | `HS47900000001` | Nguyễn Học Sinh | 80% | 31/08/2025 |
| Người LĐ | `DN47900000001` | Nguyễn Thị Lan | 80% | 31/12/2026 |
| Người LĐ | `DN47900000002` | Trần Văn Bình | 80% | 30/06/2027 |
| Người có công | `VC17900000001` | Nguyễn Cựu Chiến | 100% | Vĩnh viễn |
| Cao tuổi ≥80 | `NQ17900000001` | Cụ Lê Văn Thọ | 100% | Vĩnh viễn |
| Tự nguyện | `BT47900000001` | Nguyễn Tự Nguyện | 80% | 31/12/2027 |
| ⚠️ Thẻ hết hạn | `DN47900099001` | Test Thẻ Hết Hạn | 80% | 01/01/2025 |
| ⚠️ Sắp hết hạn | `CN37900099002` | Test Sắp Hết Hạn | 95% | 15/10/2026 |

**Tài khoản Nhân viên:**

| Role | Email đăng nhập | Mật khẩu |
|---|---|---|
| Super Admin | `admin@medassist.vn` | `Admin@123` |
| Bác sĩ | `bs.minh@medassist.vn` | `Admin@123` |
| Y tá / Lễ tân | `yta.nga@medassist.vn` | `Admin@123` |

### 11.6. Trường bổ sung vào bảng USERS cho BHYT

```sql
-- Các cột cần thêm vào bảng USERS so với thiết kế ban đầu
ALTER TABLE users ADD COLUMN bhyt_object_code   VARCHAR(2)   -- Mã đối tượng: HN, CN, DN, TE...
ALTER TABLE users ADD COLUMN bhyt_object_name   VARCHAR(50)  -- Tên đối tượng: "Hộ nghèo", "Cận nghèo"...
ALTER TABLE users ADD COLUMN bhyt_discount_rate DECIMAL(3,2) -- Mức hưởng: 1.00, 0.95, 0.80
ALTER TABLE users ADD COLUMN bhyt_expiry_date   DATE         -- Hạn sử dụng thẻ
ALTER TABLE users ADD COLUMN bhyt_issued_date   DATE         -- Ngày cấp thẻ
ALTER TABLE users ADD COLUMN created_by         UUID         -- Admin đã tạo tài khoản này
```
