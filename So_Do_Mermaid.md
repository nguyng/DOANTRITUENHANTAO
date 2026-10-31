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
    DS1[("=PostgreSQL\nUsers & Tickets=")]

    BN -->|"Mã BHYT 15 ký tự"| P1
    P1 --> P2
    P2 -->|"Thẻ hợp lệ"| P3
    DS1 -->|"Số CCCD của tài khoản"| P3
    P3 -->|"Khớp CCCD: Hợp lệ"| P4
    P4 -->|"Áp dụng mức hưởng 80-100%"| DS1
    P4 -->|"Xác nhận giảm trừ BHYT"| BN
```
