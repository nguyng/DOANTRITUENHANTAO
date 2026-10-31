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

    A --> A1["1.1 Đăng nhập bằng CCCD (hỗ trợ BHYT)"]
    A --> A2["1.2 Đổi mật khẩu"]
    A --> A3["1.3 Xem thông tin cá nhân & BHYT"]
    A --> A4["1.4 Tra cứu chỉ dẫn đường đi"]

    B --> B1["2.1 Phân luồng 2 giai đoạn (Local AI Ollama)"]
    B --> B2["2.2 Trả lời câu hỏi FAQ"]
    B --> B3["2.3 Xác thực BHYT chính chủ khớp CCCD"]
    B --> B4["2.4 Bẫy cấp cứu Red Flag Bypass (M1)"]

    C --> C1["3.1 Bốc số thứ tự khám"]
    C --> C2["3.2 Y tá gọi số tiếp theo"]
    C --> C3["3.3 Chuyển khoa"]
    C --> C4["3.4 Hủy phiếu khám"]

    D --> D1["4.1 Xem danh sách lần khám"]
    D --> D2["4.2 Xem chi tiết lần khám"]
    D --> D3["4.3 Xem đơn thuốc & chi phí"]

    E --> E1["5.1 Quản lý tài khoản nhân viên"]
    E --> E2["5.2 Cấu hình câu FAQ"]
    E --> E3["5.3 Thống kê & báo cáo"]
    E --> E4["5.4 Xem Logs hệ thống"]

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
        UC18["UC18: Quản lý nhân viên"]
        UC19["UC19: Xem thống kê & báo cáo"]
        UC20["UC20: Cấu hình câu FAQ"]
        UC22["UC22: Hiển thị STT hành lang"]
        UC23["UC23: Tra cứu chỉ dẫn đường đi"]
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
    BN --> UC23

    YT --> UC12
    YT --> UC13
    YT --> UC14
    YT --> UC15

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

## 5. DFD Mức 1 — Luồng 2: Xác thực & Đối soát Thẻ BHYT Chính chủ

```mermaid
graph TD
    BN["👤 Bệnh nhân"]
    P1(["2.1\nNhập mã BHYT\n15 ký tự"])
    P2(["2.2\nKiểm tra Format\n& Tra cứu DB/BHXH"])
    P3(["2.3\nĐối soát Khớp\nCCCD Đăng nhập"])
    P4(["2.4\nÁp dụng Giảm trừ\n& Cập nhật Ticket"])
    DS1[("=PostgreSQL 18\nUsers & Tickets=")]

    BN -->|"Mã BHYT 15 ký tự"| P1
    P1 --> P2
    P2 -->|"Thẻ hợp lệ & còn hạn"| P3
    DS1 -->|"Số CCCD của tài khoản"| P3
    P3 -->|"Khớp CCCD: Hợp lệ"| P4
    P4 -->|"Áp dụng mức hưởng 80-100%"| DS1
    P4 -->|"Xác nhận giảm trừ BHYT"| BN

    style BN fill:#34a853,color:#fff
    style P1 fill:#1a73e8,color:#fff
    style P2 fill:#1a73e8,color:#fff
    style P3 fill:#ea4335,color:#fff
    style P4 fill:#34a853,color:#fff
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
    D["Gọi Local Ollama AI: POST /analyze-symptom"]
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
    participant AI as Local Ollama AI
    participant DB as PostgreSQL
    participant RD as Redis Queue

    BN->>WS: Gửi tin nhắn triệu chứng
    WS->>SB: Forward message
    SB->>DB: Lưu CHAT_MESSAGE
    SB->>AI: POST /analyze-symptom {text}

    AI->>AI: Tiền xử lý văn bản
    AI->>AI: Phân loại Intent (SYMPTOM)
    AI->>AI: Local AI Ollama chốt Khoa (Giai đoạn 2)
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

## 9. Sequence Diagram — Xác thực & Áp dụng BHYT Chính chủ Chống Gian lận

```mermaid
sequenceDiagram
    autonumber
    actor BN as 👤 Bệnh nhân
    participant SB as Spring Boot (:8081)
    participant BHYT as BhytVerificationService
    participant DB as PostgreSQL 18

    BN->>SB: POST /api/bhyt/apply {maThe: "HN4790000000001"}
    SB->>BHYT: verify(maThe)
    BHYT->>BHYT: Kiểm tra Format 15 ký tự (QĐ 1666)
    BHYT->>DB: findByBhytNumber(maThe)
    DB-->>BHYT: Trả về thông tin thẻ (Hộ nghèo, 100%)
    BHYT-->>SB: BhytVerifyResult (valid = true)

    SB->>DB: Lấy CCCD người dùng trong SecurityContext
    DB-->>SB: CCCD "079204000001" liên kết BHYT "HN4790000000001"

    alt Số BHYT không khớp CCCD đăng nhập
        SB-->>BN: HTTP 403 Forbidden ("Số BHYT không khớp với CCCD")
    else Số BHYT khớp chính chủ
        SB->>DB: applyVerifyResult (cập nhật mức hưởng 100%)
        SB-->>BN: HTTP 200 OK ("Đã áp dụng giảm giá BHYT 100%")
    end
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
        BNW["Thymeleaf\nGiao diện Bệnh nhân"]
        NUR["Thymeleaf\nDashboard Y tá"]
        DIS["Public Display\nMàn hình Hành lang"]
    end

    subgraph APP["⚙️ APPLICATION LAYER (Tier 2) — Spring Boot 4.1.1 (Java 21)"]
        AUTH["Auth Module\n(JWT/Spring Security)"]
        CHAT["Chat Module\n(WebSocket/STOMP)"]
        QUEUE["Queue Module\n(Redis)"]
        NOTIF["Notification Module"]
        GW["AI Gateway\n(Route to AI Services)"]
    end

    subgraph AI["🤖 LOCAL AI LAYER (Ollama :11434)"]
        OLLAMA["Ollama Engine: qwen2.5-coder:3b\n• Giai đoạn 1: Probe Symptom Inquiry\n• Giai đoạn 2: Differential Clinical Triage\n• Red Flag Emergency Bypass (M1)"]
    end

    subgraph DATA["🗄️ DATA LAYER (Tier 3)"]
        PG[("PostgreSQL 18\nMain Database")]
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
        RC["Thymeleaf\n(HTML + Tailwind)"]
    end

    subgraph BE["Backend"]
        SB["Spring Boot\n:8081"]
    end

    subgraph AISVR["AI Services"]
        NLP["Local Ollama AI\n:8001"]
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
            NGINX["nginx:alpine\nport: 80, 443\n→ Reverse Proxy"]
            SB["spring-boot-app\nport: 8080"]
            NLP["spring-boot-core\nport: 8081\n(Java 21 LTS)"]
            CV["ollama-service\nport: 11434\n(qwen2.5-coder:3b)"]
            PG["postgres:15-alpine\nport: 5432\nvolume: pgdata"]
            RD["redis:7-alpine\nport: 6379"]
            MN["minio/minio\nport: 9000 (API)\nport: 9001 (Console)"]
        end
    end

    USER["👤 Người dùng\n(Browser)"] -->|"HTTPS :443"| NGINX
    NGINX -->|"proxy_pass :8081"| SB
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

```mermaid
graph TD
    ROOT["🏥 HỆ THỐNG MEDASSIST AI"]

    ROOT --> A["1. Quản lý Người dùng"]
    ROOT --> B["2. Chat AI - Trợ lý ảo"]
    ROOT --> C["3. Quản lý Hàng đợi"]
    ROOT --> D["4. Lịch sử Khám bệnh"]
    ROOT --> E["5. Quản trị hệ thống"]

    A --> A1["1.1 Đăng nhập bằng CCCD (hỗ trợ BHYT)"]
    A --> A2["1.2 Đổi mật khẩu"]
    A --> A3["1.3 Xem thông tin cá nhân & BHYT"]

    B --> B1["2.1 Phân luồng 2 giai đoạn (Local AI Ollama)"]
    B --> B2["2.2 Trả lời câu hỏi FAQ"]
    B --> B3["2.3 Xác thực BHYT chính chủ khớp CCCD"]
    B --> B4["2.4 Bẫy cấp cứu Red Flag Bypass (M1)"]

    C --> C1["3.1 Bốc số thứ tự khám"]
    C --> C2["3.2 Y tá gọi số tiếp theo"]
    C --> C3["3.3 Chuyển khoa"]
    C --> C4["3.4 Hủy phiếu khám"]

    D --> D1["4.1 Xem danh sách lần khám"]
    D --> D2["4.2 Xem chi tiết lần khám"]
    D --> D3["4.3 Xem đơn thuốc & chi phí"]

    E --> E1["5.1 Quản lý Khoa/Phòng & Chỉ dẫn đường đi"]
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

## 18. Flowchart — Quy trình Phân luồng Lâm sàng 2 Giai đoạn (Two-Phase Differential Inquiry)

```mermaid
flowchart TD
    START(["👤 Bệnh nhân mô tả triệu chứng ban đầu"]) --> REDFLAG{"🚨 Phát hiện Red Flag Cấp cứu?<br/>(Bất tỉnh, ngừng thở, co giật, nôn ra máu...)"}

    %% Nhánh Cấp cứu khẩn cấp
    REDFLAG -- "CÓ (Nguy kịch)" --> BYPASS["⚡ KÍCH HOẠT RED FLAG BYPASS<br/>(Ngắt toàn bộ AI suy luận)"]
    BYPASS --> EMER_TICKET["🎫 Cấp ngay Phiếu Khám: Khoa Cấp Cứu (KCAPCUU)<br/>Mức ưu tiên: M1 (Ngay lập tức)"]
    EMER_TICKET --> TOP_QUEUE["📢 Đẩy thẳng lên TOP 1 Hàng đợi Cấp Cứu"]

    %% Nhánh Phân luồng 2 Giai đoạn
    REDFLAG -- "KHÔNG" --> EVAL_DETAIL{"Mô tả ban đầu đã rất chi tiết?<br/>(> 120 ký tự + đa triệu chứng)"}

    EVAL_DETAIL -- "Chưa đủ (Mô tả ngắn gọn)" --> PHASE1["🩺 GIAI ĐOẠN 1: Thăm dò Triệu chứng Mấu chốt<br/>(System Prompt: SYSTEM_PROMPT_PROBE)"]
    PHASE1 --> AI_PROBE["🤖 AI đóng vai Trợ lý tiếp đón:<br/>Đặt câu hỏi làm rõ tính chất đau, hướng lan, sốt, khó thở..."]
    AI_PROBE --> USER_ANSWER["👤 Bệnh nhân trả lời câu hỏi làm rõ"]
    USER_ANSWER --> PHASE2

    EVAL_DETAIL -- "Đã chi tiết" --> PHASE2["🎯 GIAI ĐOẠN 2: Chẩn đoán Phân biệt & Chốt Khoa<br/>(System Prompt: SYSTEM_PROMPT_DECISION)"]
    PHASE2 --> AI_DECISION["🤖 AI phân tích dữ kiện lâm sàng:<br/>• Đối chiếu Ma trận Phân biệt<br/>• Loại trừ các khoa không phù hợp<br/>• Chốt 1 trong 11 Chuyên khoa chuẩn"]

    AI_DECISION --> JSON_OUT["📋 Xuất JSON có cấu trúc:<br/>department_code, priority M1-M5, confidence, advice"]
    JSON_OUT --> CARD["💳 Hiển thị Thẻ Tiếp Đón & Chuyên Khoa Gợi Ý"]
    CARD --> CONFIRM["✅ Bệnh nhân xác nhận nhận phiếu"]
    CONFIRM --> TICKET["🎫 Cấp Phiếu Khám Điện Tử & Chỉ Dẫn Đường Đi"]

    style START fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style REDFLAG fill:#ffebee,stroke:#c62828,stroke-width:2px,color:#c62828
    style BYPASS fill:#ffcdd2,stroke:#b71c1c,stroke-width:3px,color:#b71c1c
    style EMER_TICKET fill:#d32f2f,stroke:#b71c1c,color:#fff,font-weight:bold
    style TOP_QUEUE fill:#b71c1c,stroke:#b71c1c,color:#fff,font-weight:bold
    style PHASE1 fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    style PHASE2 fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    style AI_DECISION fill:#c8e6c9,stroke:#2e7d32,stroke-width:2px
    style TICKET fill:#1b5e20,stroke:#1b5e20,color:#fff,font-weight:bold
```
