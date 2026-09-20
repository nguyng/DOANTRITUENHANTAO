# 📐 Tất cả Sơ đồ — Dự án MedAssist AI

---

## 1. BFD — Biểu đồ phân rã chức năng

```mermaid
graph TD
    ROOT["🏥 HỆ THỐNG MEDASSIST AI"]

    ROOT --> A["1. Quản lý Người dùng"]
    ROOT --> B["2. Chat AI - Trợ lý ảo"]
    ROOT --> C["3. Quản lý Hàng đợi"]
    ROOT --> D["4. Lịch sử Khám bệnh"]
    ROOT --> E["5. Quản trị hệ thống"]

    A --> A1["1.1 Đăng nhập bằng Mã BHYT"]
    A --> A2["1.2 Đổi mật khẩu"]
    A --> A3["1.3 Xem thông tin cá nhân & BHYT"]
    A --> A4["1.4 Admin cấp tài khoản bệnh nhân"]

    B --> B1["2.1 Phân tích triệu chứng (PhoBERT)"]
    B --> B2["2.2 Trả lời câu hỏi FAQ"]
    B --> B3["2.3 OCR nhận diện thẻ BHYT (YOLOv8)"]
    B --> B4["2.4 Phát hiện mức độ khẩn cấp"]

    C --> C1["3.1 Bốc số thứ tự khám"]
    C --> C2["3.2 Y tá gọi số tiếp theo"]
    C --> C3["3.3 Chuyển khoa"]
    C --> C4["3.4 Hủy phiếu khám"]

    D --> D1["4.1 Xem danh sách lần khám"]
    D --> D2["4.2 Xem chi tiết lần khám"]
    D --> D3["4.3 Xem đơn thuốc & chi phí"]

    E --> E1["5.1 Quản lý Khoa/Phòng"]
    E --> E2["5.2 Quản lý tài khoản nhân viên"]
    E --> E3["5.3 Cấu hình câu FAQ"]
    E --> E4["5.4 Thống kê & báo cáo"]
    E --> E5["5.5 Xem Logs hệ thống"]

    style ROOT fill:#1a73e8,color:#fff,font-weight:bold
    style A fill:#34a853,color:#fff
    style B fill:#ea4335,color:#fff
    style C fill:#fbbc04,color:#000
    style D fill:#9c27b0,color:#fff
    style E fill:#ff7043,color:#fff
```

---

## 2. Use Case Diagram

```mermaid
graph LR
    subgraph ACTORS["👥 ACTORS"]
        BN["👤 Bệnh nhân"]
        YT["👨‍⚕️ Y tá / Lễ tân"]
        AD["🔧 Admin"]
        MH["🖥️ Màn hình Hành lang"]
    end

    subgraph SYS["🏥 HỆ THỐNG MEDASSIST"]
        UC01["UC01: Đăng nhập Mã BHYT"]
        UC02["UC02: Đổi mật khẩu lần đầu"]
        UC03["UC03: Xem thông tin cá nhân"]
        UC04["UC04: Xem lịch sử khám"]
        UC05["UC05: Xem chi tiết khám + đơn thuốc"]
        UC06["UC06: Chat với Trợ lý AI"]
        UC07["UC07: Gửi ảnh BHYT (OCR)"]
        UC08["UC08: Nhận phân luồng Khoa từ AI"]
        UC09["UC09: Bốc số / Xem phiếu khám"]
        UC10["UC10: Nhận thông báo đến lượt"]
        UC11["UC11: Hủy phiếu khám"]
        UC12["UC12: Xem hàng đợi theo Khoa"]
        UC13["UC13: Gọi số tiếp theo"]
        UC14["UC14: Chuyển bệnh nhân sang Khoa khác"]
        UC15["UC15: Tra cứu hồ sơ bệnh nhân"]
        UC16["UC16: Tạo & cấp tài khoản bệnh nhân"]
        UC17["UC17: Quản lý Khoa/Phòng"]
        UC18["UC18: Quản lý nhân viên"]
        UC19["UC19: Xem thống kê & báo cáo"]
        UC20["UC20: Cấu hình câu FAQ"]
        UC22["UC22: Hiển thị STT hành lang"]
    end

    BN --> UC01
    BN --> UC02
    BN --> UC03
    BN --> UC04
    BN --> UC05
    BN --> UC06
    BN --> UC07
    BN --> UC08
    BN --> UC09
    BN --> UC10
    BN --> UC11

    YT --> UC12
    YT --> UC13
    YT --> UC14
    YT --> UC15

    AD --> UC16
    AD --> UC17
    AD --> UC18
    AD --> UC19
    AD --> UC20

    MH --> UC22

    style BN fill:#2196f3,color:#fff
    style YT fill:#4caf50,color:#fff
    style AD fill:#ff5722,color:#fff
    style MH fill:#9c27b0,color:#fff
```

---

## 3. DFD Mức 0 — Context Diagram

```mermaid
graph LR
    BN["👤 Bệnh nhân"]
    YT["👨‍⚕️ Y tá / Lễ tân"]
    AD["🔧 Admin"]
    MH["🖥️ Màn hình Hành lang"]
    SYS(["⭕ MEDASSIST\nAI SYSTEM"])

    BN -->|"Triệu chứng, Ảnh BHYT, Xác nhận khám"| SYS
    SYS -->|"Gợi ý Khoa, STT, Thông báo đến lượt"| BN

    YT -->|"Lệnh gọi số, Chuyển khoa, Tra cứu"| SYS
    SYS -->|"Danh sách hàng đợi, Hồ sơ bệnh nhân"| YT

    AD -->|"Thông tin BN, Cấu hình hệ thống"| SYS
    SYS -->|"Báo cáo, Logs, Mã BHYT sinh ra"| AD

    SYS -->|"STT đang gọi, Tên bệnh nhân"| MH

    style SYS fill:#1a73e8,color:#fff,font-size:16px
    style BN fill:#34a853,color:#fff
    style YT fill:#fbbc04,color:#000
    style AD fill:#ea4335,color:#fff
    style MH fill:#9c27b0,color:#fff
```

---

## 4. DFD Mức 1 — Luồng 1: Phân tích triệu chứng

```mermaid
graph TD
    BN["👤 Bệnh nhân"]
    P1(["1.1\nPhân loại Intent"])
    P2(["1.2\nPhoBERT\nPhân tích triệu chứng"])
    P3(["1.3\nTạo phiếu khám\n& cấp STT"])
    DS1[("=AI_ANALYSIS\n_LOGS=")]
    DS2[("=QUEUE\n_TICKETS=")]
    DS3[("=DEPARTMENTS=")]

    BN -->|"Tin nhắn triệu chứng"| P1
    P1 -->|"Intent = SYMPTOM"| P2
    P2 -->|"Khoa dự đoán + Độ tin cậy + Mức độ"| DS1
    P2 -->|"Khoa gợi ý"| P3
    DS3 -->|"Thông tin Khoa"| P3
    P3 -->|"Tạo phiếu"| DS2
    P3 -->|"STT + Khoa + Phòng"| BN

    style BN fill:#34a853,color:#fff
    style P1 fill:#1a73e8,color:#fff
    style P2 fill:#ea4335,color:#fff
    style P3 fill:#fbbc04,color:#000
    style DS1 fill:#e8f5e9,color:#000
    style DS2 fill:#e8f5e9,color:#000
    style DS3 fill:#e8f5e9,color:#000
```

---

## 5. DFD Mức 1 — Luồng 2: OCR thẻ BHYT

```mermaid
graph TD
    BN["👤 Bệnh nhân"]
    P1(["2.1\nUpload & Lưu\nvào MinIO"])
    P2(["2.2\nYOLOv8\nDetect vùng BHYT"])
    P3(["2.3\nVietOCR\nĐọc mã số"])
    P4(["2.4\nValidate &\nXác nhận với BN"])
    DS1[("=MinIO\nFile Storage=")]

    BN -->|"Ảnh thẻ BHYT"| P1
    P1 -->|"image_url"| DS1
    DS1 -->|"image_url"| P2
    P2 -->|"Bounding box vùng mã"| P3
    P3 -->|"Chuỗi mã BHYT thô"| P4
    P4 -->|"Mã BHYT đã xác thực"| BN

    style BN fill:#34a853,color:#fff
    style P1 fill:#1a73e8,color:#fff
    style P2 fill:#ea4335,color:#fff
    style P3 fill:#9c27b0,color:#fff
    style P4 fill:#fbbc04,color:#000
    style DS1 fill:#e8f5e9,color:#000
```

---

## 6. ERD — Biểu đồ thực thể quan hệ

```mermaid
erDiagram
    USERS {
        uuid id PK
        varchar bhyt_number UK
        varchar password_hash
        varchar full_name
        varchar phone
        varchar email
        date dob
        varchar gender
        varchar address
        varchar role
        decimal bhyt_discount_rate
        date bhyt_expiry_date
        boolean is_active
        uuid created_by FK
        timestamp created_at
    }

    DEPARTMENTS {
        uuid id PK
        varchar name
        varchar code UK
        varchar description
        int floor
        varchar room_numbers
        boolean is_active
    }

    QUEUE_TICKETS {
        uuid id PK
        uuid patient_id FK
        uuid department_id FK
        int ticket_number
        varchar status
        timestamp called_at
        timestamp done_at
        timestamp created_at
    }

    MEDICAL_SERVICES {
        uuid id PK
        varchar name
        decimal price
        boolean is_bhyt_covered
        decimal bhyt_price_limit
    }

    MEDICINES {
        uuid id PK
        varchar name
        decimal price
        boolean is_bhyt_covered
        decimal bhyt_price_limit
    }

    VISIT_RECORDS {
        uuid id PK
        uuid patient_id FK
        uuid department_id FK
        uuid doctor_id FK
        varchar visit_name
        text diagnosis
        date visit_date
        varchar status
        varchar severity_level
        decimal original_cost
        decimal insurance_paid
        decimal final_cost
        text notes
        timestamp created_at
    }

    VISIT_SERVICES {
        uuid id PK
        uuid visit_id FK
        uuid service_id FK
        int quantity
        decimal patient_co_pay
        decimal bhyt_pay
    }

    PRESCRIPTIONS {
        uuid id PK
        uuid visit_id FK
        uuid medicine_id FK
        varchar dosage
        int quantity
        varchar unit
        decimal patient_co_pay
        decimal bhyt_pay
        text instructions
    }

    CHAT_SESSIONS {
        uuid id PK
        uuid user_id FK
        timestamp started_at
        timestamp ended_at
    }

    CHAT_MESSAGES {
        uuid id PK
        uuid session_id FK
        varchar sender_role
        text content
        varchar message_type
        varchar media_url
        timestamp created_at
    }

    AI_ANALYSIS_LOGS {
        uuid id PK
        uuid message_id FK
        varchar ai_model_used
        text input_text
        varchar predicted_dept
        decimal confidence_score
        varchar severity
        boolean is_corrected
        varchar corrected_dept
        int processing_ms
        timestamp created_at
    }

    USERS ||--o{ QUEUE_TICKETS : "patient_id"
    USERS ||--o{ VISIT_RECORDS : "patient_id"
    USERS ||--o{ VISIT_RECORDS : "doctor_id"
    USERS ||--o{ CHAT_SESSIONS : "user_id"
    USERS ||--o{ USERS : "created_by"
    DEPARTMENTS ||--o{ QUEUE_TICKETS : "department_id"
    DEPARTMENTS ||--o{ VISIT_RECORDS : "department_id"
    VISIT_RECORDS ||--o{ VISIT_SERVICES : "visit_id"
    MEDICAL_SERVICES ||--o{ VISIT_SERVICES : "service_id"
    VISIT_RECORDS ||--o{ PRESCRIPTIONS : "visit_id"
    MEDICINES ||--o{ PRESCRIPTIONS : "medicine_id"
    CHAT_SESSIONS ||--o{ CHAT_MESSAGES : "session_id"
    CHAT_MESSAGES ||--o| AI_ANALYSIS_LOGS : "message_id"
```

---

## 7. Activity Diagram — Luồng Chat AI phân tích triệu chứng

```mermaid
flowchart TD
    START(["🟢 Bắt đầu"])
    A["Bệnh nhân nhập mô tả triệu chứng"]
    B["WebSocket gửi tin nhắn đến Spring Boot"]
    C["Spring Boot lưu CHAT_MESSAGE vào DB"]
    D["Gọi FastAPI NLP: POST /analyze-symptom"]
    E{"Phân loại Intent"}
    F["Gọi PhoBERT Triage Model"]
    G["Trả lời FAQ từ database"]
    H{"Confidence\n>= 70%?"}
    I["Hỏi thêm triệu chứng"]
    J{"Mức độ khẩn cấp?"}
    K["Ưu tiên hàng đợi EMERGENCY"]
    L["Hàng đợi thông thường"]
    M["Lưu AI_ANALYSIS_LOG"]
    N["Gửi gợi ý Khoa qua WebSocket"]
    O{"Bệnh nhân\nxác nhận?"}
    P["Tạo QUEUE_TICKET, cấp STT"]
    Q["Kết thúc — Đợi xác nhận"]
    END(["🔴 Kết thúc"])

    START --> A
    A --> B
    B --> C
    C --> D
    D --> E
    E -->|"FAQ"| G
    E -->|"SYMPTOM"| F
    G --> END
    F --> H
    H -->|"Không"| I
    I --> A
    H -->|"Có"| J
    J -->|"EMERGENCY 🔴"| K
    J -->|"MEDIUM 🟡 / NORMAL 🟢"| L
    K --> M
    L --> M
    M --> N
    N --> O
    O -->|"Xác nhận"| P
    O -->|"Đổi Khoa"| Q
    P --> END

    style START fill:#34a853,color:#fff
    style END fill:#ea4335,color:#fff
    style E fill:#1a73e8,color:#fff
    style H fill:#fbbc04,color:#000
    style J fill:#ff7043,color:#fff
    style K fill:#ea4335,color:#fff
```

---

## 8. Sequence Diagram — Bệnh nhân gửi triệu chứng → nhận STT

```mermaid
sequenceDiagram
    actor BN as 👤 Bệnh nhân
    participant WS as WebSocket Handler
    participant SB as Spring Boot
    participant AI as FastAPI NLP
    participant DB as PostgreSQL
    participant RD as Redis Queue

    BN->>WS: Gửi tin nhắn triệu chứng
    WS->>SB: Forward message
    SB->>DB: Lưu CHAT_MESSAGE
    SB->>AI: POST /analyze-symptom {text}

    AI->>AI: Tiền xử lý văn bản
    AI->>AI: Phân loại Intent (SYMPTOM)
    AI->>AI: PhoBERT dự đoán Khoa
    AI-->>SB: {dept_id, confidence: 0.89, severity: MEDIUM}

    SB->>DB: Lưu AI_ANALYSIS_LOG
    SB->>WS: Push gợi ý khoa
    WS-->>BN: "Gợi ý: Khoa Thần kinh (89%)\nBạn có muốn xác nhận?"

    BN->>WS: Xác nhận đăng ký
    WS->>SB: Confirm request
    SB->>RD: INCR queue:{dept_id}:date
    RD-->>SB: ticket_number = 47
    SB->>DB: Tạo QUEUE_TICKET #{47}
    SB->>WS: Push phiếu khám
    WS-->>BN: "STT #47 - Khoa Thần kinh - Phòng 302"

    SB->>WS: Broadcast to Nurse Dashboard
    WS-->>SB: Y tá nhận thông báo "Có BN mới"
```

---

## 9. Sequence Diagram — OCR thẻ BHYT

```mermaid
sequenceDiagram
    actor BN as 👤 Bệnh nhân
    participant SB as Spring Boot
    participant MN as MinIO Storage
    participant CV as FastAPI CV Service

    BN->>SB: POST /chat/messages/image {file}
    SB->>MN: Upload ảnh, lưu tại bucket "bhyt-images"
    MN-->>SB: image_url

    SB->>CV: POST /extract-bhyt {image_url}

    CV->>CV: Bước 1: Tiền xử lý (resize 640×640, normalize)
    CV->>CV: Bước 2: YOLOv8 detect "bhyt_card_region"
    CV->>CV: Bước 3: Crop vùng mã số
    CV->>CV: Bước 4: VietOCR đọc chuỗi ký tự
    CV->>CV: Bước 5: Validate regex ^[A-Z]{2}[0-9]{10}$

    CV-->>SB: {bhyt_code: "DN4123456789", confidence: 0.97}

    SB-->>BN: "Đã nhận diện Mã BHYT: DN4 1234 5678 9\nXác nhận đúng không?"
```

---

## 10. Sequence Diagram — Y tá gọi số

```mermaid
sequenceDiagram
    actor YT as 👨‍⚕️ Y tá
    participant SB as Spring Boot
    participant DB as PostgreSQL
    participant RD as Redis
    participant WS as WebSocket
    actor BN as 👤 Bệnh nhân
    actor MH as 🖥️ Màn hình Hành lang

    YT->>SB: PUT /queue/{ticketId}/call
    SB->>DB: UPDATE ticket SET status='CALLED', called_at=NOW()
    SB->>RD: Cập nhật current_number:{dept_id}
    DB-->>SB: OK

    SB->>WS: Broadcast to /topic/queue/{deptId}
    WS-->>YT: Dashboard cập nhật "Đang gọi: #47"

    SB->>WS: Push to /user/{patientId}/queue/notification
    WS-->>BN: "🔔 Đến lượt của bạn! STT #47 - Phòng 302"

    SB->>WS: Broadcast to /topic/display/{deptId}
    WS-->>MH: Hiển thị "Số 47 - Nguyễn Thị Lan"
```

---

## 11. State Diagram — Vòng đời QUEUE_TICKET

```mermaid
stateDiagram-v2
    [*] --> WAITING : Bệnh nhân bốc số (POST /tickets)

    WAITING --> CALLED : Y tá gọi số\n(PUT /queue/ticketId/call)
    WAITING --> CANCELLED : Bệnh nhân hủy\n(DELETE /tickets/id)

    CALLED --> DONE : Khám xong\n(Y tá cập nhật hoàn tất)
    CALLED --> WAITING : BN không có mặt\n(Y tá cho vào lại hàng)
    CALLED --> TRANSFERRED : Chuyển sang Khoa khác\n(PUT /queue/ticketId/transfer)

    TRANSFERRED --> WAITING : Tạo phiếu mới tại Khoa mới

    DONE --> [*]
    CANCELLED --> [*]
```

---

## 12. State Diagram — Vòng đời CHAT_SESSION

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Bệnh nhân bắt đầu chat\n(POST /chat/sessions)

    ACTIVE --> COMPLETED : Đã đăng ký khám thành công\n(Tạo QUEUE_TICKET)
    ACTIVE --> ABANDONED : Không hoạt động > 30 phút\n(Server timeout)

    COMPLETED --> [*]
    ABANDONED --> [*]
```

---

## 13. System Architecture Diagram

```mermaid
graph TB
    subgraph CLIENT["🖥️ CLIENT LAYER (Tier 1)"]
        BNW["React.js\nGiao diện Bệnh nhân"]
        NUR["React.js\nDashboard Y tá"]
        DIS["Public Display\nMàn hình Hành lang"]
    end

    subgraph APP["⚙️ APPLICATION LAYER (Tier 2) — Spring Boot Java 17"]
        AUTH["Auth Module\n(JWT/Spring Security)"]
        CHAT["Chat Module\n(WebSocket/STOMP)"]
        QUEUE["Queue Module\n(Redis)"]
        NOTIF["Notification Module"]
        GW["AI Gateway\n(Route to AI Services)"]
    end

    subgraph AI["🤖 AI SERVICE LAYER (Python FastAPI)"]
        NLP["NLP Service :8001\n• PhoBERT Triage\n• FAQ Handler\n• Severity Detection"]
        CV["CV Service :8002\n• YOLOv8 Detection\n• VietOCR\n• Image Moderation"]
    end

    subgraph DATA["🗄️ DATA LAYER (Tier 3)"]
        PG[("PostgreSQL 15\nMain Database")]
        RD[("Redis 7\nCache & Queue")]
        MN[("MinIO\nFile Storage")]
        RMQ[("RabbitMQ\nMessage Broker")]
    end

    BNW <-->|"REST / WebSocket"| AUTH
    BNW <-->|"WebSocket STOMP"| CHAT
    NUR <-->|"REST / WebSocket"| QUEUE
    DIS <-->|"WebSocket"| NOTIF

    CHAT --> GW
    GW -->|"HTTP POST"| NLP
    GW -->|"HTTP POST"| CV

    AUTH --> PG
    CHAT --> PG
    QUEUE --> RD
    NOTIF --> RMQ
    CV --> MN

    style CLIENT fill:#e3f2fd
    style APP fill:#e8f5e9
    style AI fill:#fff3e0
    style DATA fill:#fce4ec
```

---

## 14. Component Diagram

```mermaid
graph LR
    subgraph FE["Frontend"]
        RC["React.js\n(Vite + TailwindCSS)"]
    end

    subgraph BE["Backend"]
        SB["Spring Boot\n:8080"]
    end

    subgraph AISVR["AI Services"]
        NLP["FastAPI NLP\n:8001"]
        CV["FastAPI CV\n:8002"]
    end

    subgraph STORAGE["Storage & Cache"]
        PG[("PostgreSQL\n:5432")]
        RD[("Redis\n:6379")]
        MN[("MinIO\n:9000")]
    end

    RC <-->|"HTTP REST\nWebSocket STOMP"| SB
    SB <-->|"HTTP REST"| NLP
    SB <-->|"HTTP REST"| CV
    SB <-->|"JDBC / JPA"| PG
    SB <-->|"Redis Client"| RD
    CV <-->|"S3 API"| MN

    style FE fill:#e3f2fd
    style BE fill:#e8f5e9
    style AISVR fill:#fff3e0
    style STORAGE fill:#fce4ec
```

---

## 15. Deployment Diagram

```mermaid
graph TB
    subgraph SERVER["🖥️ Server / Docker Host"]
        subgraph DC["Docker Compose Network: medassist-net"]
            NGINX["nginx:alpine\nport: 80, 443\n→ serve React build"]
            SB["spring-boot-app\nport: 8080"]
            NLP["fastapi-nlp\nport: 8001\n(PhoBERT model)"]
            CV["fastapi-cv\nport: 8002\n(YOLOv8 + VietOCR)"]
            PG["postgres:15-alpine\nport: 5432\nvolume: pgdata"]
            RD["redis:7-alpine\nport: 6379"]
            MN["minio/minio\nport: 9000 (API)\nport: 9001 (Console)"]
        end
    end

    USER["👤 Người dùng\n(Browser)"] -->|"HTTPS :443"| NGINX
    NGINX -->|"proxy_pass :8080"| SB
    SB -->|":8001"| NLP
    SB -->|":8002"| CV
    SB -->|":5432"| PG
    SB -->|":6379"| RD
    CV -->|":9000"| MN

    style SERVER fill:#f5f5f5
    style DC fill:#e8f5e9
    style USER fill:#2196f3,color:#fff
```

---

## 16. Sitemap — Navigation Flow theo Role

```mermaid
graph TD
    LOGIN["/login\n🔐 Đăng nhập Mã BHYT"]

    LOGIN -->|"role: PATIENT"| P_CHAT
    LOGIN -->|"role: NURSE"| N_QUEUE
    LOGIN -->|"role: ADMIN"| A_DASH
    LOGIN -->|"Public"| DISPLAY

    subgraph PATIENT["👤 BỆNH NHÂN"]
        P_CHAT["/chat\n💬 Chat với AI"]
        P_TICKET["/ticket\n🎫 Phiếu khám & STT"]
        P_PROFILE["/profile\n👤 Thông tin cá nhân"]
        P_VISITS["/visits\n🏥 Lịch sử khám"]
        P_VISIT_DETAIL["/visits/:id\n📋 Chi tiết khám + Đơn thuốc"]
    end

    subgraph NURSE["👨‍⚕️ Y TÁ"]
        N_QUEUE["/nurse/queue\n📊 Dashboard hàng đợi"]
        N_DEPT["/nurse/queue/:deptId\n📋 Hàng đợi chi tiết Khoa"]
        N_SEARCH["/nurse/search\n🔍 Tra cứu bệnh nhân"]
    end

    subgraph ADMIN["🔧 ADMIN"]
        A_DASH["/admin/dashboard\n📈 Thống kê tổng quan"]
        A_PAT["/admin/patients\n👥 Quản lý & cấp TK bệnh nhân"]
        A_DEPT["/admin/departments\n🏥 Quản lý Khoa/Phòng"]
        A_STAFF["/admin/staff\n👨‍💼 Quản lý nhân viên"]
        A_FAQ["/admin/faq\n❓ Cấu hình FAQ"]
        A_RPT["/admin/reports\n📊 Báo cáo & xuất file"]
        A_LOG["/admin/logs\n📜 Logs hệ thống"]
    end

    DISPLAY["/display/:deptId\n🖥️ Màn hình hành lang"]

    P_CHAT --> P_TICKET
    P_VISITS --> P_VISIT_DETAIL
    N_QUEUE --> N_DEPT

    style LOGIN fill:#1a73e8,color:#fff
    style DISPLAY fill:#9c27b0,color:#fff
```

---

## 17. Activity Diagram — Luồng tính Viện phí BHYT

```mermaid
flowchart TD
    START(["🟢 Bắt đầu thanh toán"])
    GET_VISIT["Lấy thông tin VISIT_RECORDS"]
    GET_RATE["Lấy bhyt_discount_rate của Bệnh nhân"]
    
    LOOP_ITEMS{"Còn Dịch vụ/Thuốc\nchưa duyệt?"}
    
    CHECK_COVERED{"Có thuộc DM\nBHYT chi trả?"}
    
    CALC_NO_BHYT["patient_co_pay = price\nbhyt_pay = 0"]
    
    CALC_BASE_PRICE["Giá cơ sở = Min(price, bhyt_price_limit)"]
    CALC_BHYT["bhyt_pay = Giá cơ sở × bhyt_discount_rate\npatient_co_pay = price - bhyt_pay"]
    
    SUM_TOTAL["Cộng dồn vào Tổng: original_cost, insurance_paid, final_cost"]
    
    SAVE_DB["Lưu CSDL & Xuất hóa đơn"]
    END(["🔴 Kết thúc"])

    START --> GET_VISIT
    GET_VISIT --> GET_RATE
    GET_RATE --> LOOP_ITEMS
    
    LOOP_ITEMS -->|"Có"| CHECK_COVERED
    
    CHECK_COVERED -->|"Không (false)"| CALC_NO_BHYT
    CHECK_COVERED -->|"Có (true)"| CALC_BASE_PRICE
    CALC_BASE_PRICE --> CALC_BHYT
    
    CALC_NO_BHYT --> SUM_TOTAL
    CALC_BHYT --> SUM_TOTAL
    
    SUM_TOTAL --> LOOP_ITEMS
    
    LOOP_ITEMS -->|"Hết"| SAVE_DB
    SAVE_DB --> END

    style START fill:#34a853,color:#fff
    style END fill:#ea4335,color:#fff
    style LOOP_ITEMS fill:#fbbc04,color:#000
    style CHECK_COVERED fill:#fbbc04,color:#000
    style GET_RATE fill:#1a73e8,color:#fff
```
