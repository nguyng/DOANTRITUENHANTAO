# 🏥 KỊCH BẢN VẬN HÀNH HỆ THỐNG PHÂN LUỒNG BỆNH VIỆN
## MedAssist AI — Hospital Queue Management System

> **Tài liệu này mô tả luồng vận hành thực tế của hệ thống khi một bệnh nhân bước vào bệnh viện để khám bệnh.**

---

## 👤 NHÂN VẬT TRONG KỊCH BẢN

| Nhân vật | Vai trò | Tài khoản hệ thống |
|----------|---------|-------------------|
| **Nguyễn Văn Nghèo** | Bệnh nhân BHYT hộ nghèo | `HN47900000001` / `123456` |
| **Y tá Lê Thu Nga** | Y tá Khoa Cấp Cứu | `YTA_NGA` / `123456` |
| **ADMIN Hệ thống** | Quản trị viên | `ADMIN_001` / `Admin@123` |
| **Màn hình Sảnh** | Màn hình hiển thị công cộng | `/display` (không cần đăng nhập) |

---

## 📋 KỊCH BẢN: "BỆNH NHÂN ĐAU NGỰC — KHẨN CẤP"

### 🕘 08:30 — Bệnh nhân bước vào bệnh viện

Ông **Nguyễn Văn Nghèo** (65 tuổi) bước vào sảnh chờ bệnh viện MedAssist với triệu chứng:
- Đau tức ngực trái từ sáng sớm
- Khó thở nhẹ khi leo cầu thang
- Toát mồ hôi lạnh

Ông được nhân viên lễ tân hướng dẫn ngồi vào khu vực Kiosk điện tử và **đăng nhập hệ thống**.

---

### 🖥️ BƯỚC 1 — Bệnh nhân đăng nhập hệ thống
**URL:** `http://localhost:8081/login`

```
Người dùng nhập:
  - Tên đăng nhập: HN47900000001  (Số BHYT)
  - Mật khẩu:      123456
```

✅ Hệ thống xác thực thành công → chuyển hướng tự động đến trang **Chat AI**.

---

### 🤖 BƯỚC 2 — Mô tả triệu chứng với AI (Phân luồng thông minh)
**URL:** `http://localhost:8081/chat`

Giao diện hiển thị:
> *"Xin chào Nguyễn Văn Nghèo! Hôm nay bạn cảm thấy thế nào? Hãy mô tả triệu chứng của bạn."*

Ông Nghèo nhập vào hộp chat:
> *"Tôi bị đau tức ngực bên trái từ sáng đến giờ, thỉnh thoảng khó thở, toát mồ hôi lạnh."*

**AI (MedBot) xử lý:**
```
[AI gửi request đến FastAPI service phân tích triệu chứng]

Phân tích:
  - Triệu chứng: đau ngực + khó thở + toát mồ hôi
  - Nhóm bệnh có thể: Tim mạch, Hô hấp
  - Mức độ ưu tiên: M2 - KHẨN CẤP (<=15 phút)
  - Khoa phù hợp: Khoa Tim Mạch / Khoa Cấp Cứu
  - Độ tin cậy: 92%
```

AI trả lời:
> *"Triệu chứng của bạn có thể liên quan đến tim mạch. Tôi sẽ chuyển bạn đến Khoa Cấp Cứu ngay để được thăm khám khẩn cấp."*

---

### 🎫 BƯỚC 3 — Hệ thống phát phiếu khám điện tử
**URL:** `http://localhost:8081/ticket`

Hệ thống tự động tạo **Phiếu Khám Điện Tử**:

```
╔══════════════════════════════════════╗
║         PHIẾU KHÁM BỆNH ĐIỆN TỬ     ║
╠══════════════════════════════════════╣
║  Số thứ tự:   CC-007                 ║
║  Bệnh nhân:   Nguyễn Văn Nghèo       ║
║  Khoa:        Khoa Cấp Cứu           ║
║  Địa điểm:    Tòa A - Tầng 1 - P.101 ║
║  Mức ưu tiên: M2 - KHẨN CẤP         ║
║  Giờ tạo:     08:31                  ║
╚══════════════════════════════════════╝

  AI phân tích: "Đau ngực trái, khó thở,
  toát mồ hôi - nghi ngờ vấn đề tim mạch"
  Độ tin cậy: 92%
```

Bệnh nhân nhận thông báo:
> *"Phiếu của bạn đã được tạo! Vui lòng đến Tòa A - Tầng 1 - Phòng 101-105 và chờ được gọi số."*

---

### 📺 BƯỚC 4 — Màn hình sảnh hiển thị hàng đợi
**URL:** `http://localhost:8081/display`

Màn hình LED tại sảnh chờ Khoa Cấp Cứu hiển thị:

```
╔══════════════════════════════════════════════════╗
║   KHOA CẤP CỨU — ĐANG GỌI KHÁM                  ║
╠══════════════════════════════════════════════════╣
║                                                  ║
║   Số đang khám:    CC-005                        ║
║   Đang chờ:        CC-006, CC-007, CC-008        ║
║                                                  ║
║   Tổng đang chờ: 3 bệnh nhân                    ║
╚══════════════════════════════════════════════════╝
```

---

### 👩‍⚕️ BƯỚC 5 — Y tá quản lý hàng đợi
**URL:** `http://localhost:8081/nurse/queue`

Y tá **Lê Thu Nga** đăng nhập hệ thống:
```
  Tài khoản: YTA_NGA
  Mật khẩu:  123456
```

Dashboard Y tá hiển thị tổng quan toàn bệnh viện:

```
┌─────────────────────────────────────────────────┐
│  DASHBOARD HÀNG ĐỢI — Y TÁ LÊ THU NGA          │
│  Khoa phụ trách: Khoa Cấp Cứu                   │
├──────────────┬──────────────┬───────────────────┤
│ Đang chờ: 3  │ Đang khám: 1 │ Hoàn tất hôm nay: 4│
├──────────────┴──────────────┴───────────────────┤
│ DANH SÁCH KHOA                                  │
│  [DO] Cấp Cứu       — 3 chờ, 1 đang khám       │
│  [VG] Tim Mạch      — 5 chờ, 2 đang khám       │
│  [OK] Nội Tổng Hợp  — 2 chờ, 0 đang khám       │
└─────────────────────────────────────────────────┘
```

Y tá vào trang chi tiết Khoa Cấp Cứu:
**URL:** `http://localhost:8081/nurse/queue/d1111111-1111-1111-1111-111111111111`

```
┌──────┬──────────────────────┬──────────┬──────────┬───────────┐
│ STT  │ Bệnh nhân            │ Mã BHYT  │ Ưu tiên  │ Trạng thái│
├──────┼──────────────────────┼──────────┼──────────┼───────────┤
│CC-005│ Trần Thị Lan         │HN4790... │ M3       │ ĐANG KHÁM │
│CC-006│ Lê Minh Tuấn         │HT3810... │ M3       │ CHỜ       │
│CC-007│ Nguyễn Văn Nghèo     │HN4790... │ M2 !!!   │ CHỜ       │
│CC-008│ Phạm Thị Hoa         │CT5920... │ M4       │ CHỜ       │
└──────┴──────────────────────┴──────────┴──────────┴───────────┘
```

> Chú ý: Phiếu CC-007 (ông Nghèo) có ưu tiên M2 - Khẩn cấp.
> Y tá có thể gọi trước số thứ tự bình thường nếu cần thiết.

---

### 📣 BƯỚC 6 — Y tá gọi bệnh nhân

Y tá nhấn **"Gọi số tiếp theo"** (hoặc gọi riêng CC-007 ưu tiên).

```
[POST] /nurse/queue/d1111111-1111-1111-1111-111111111111/call-next
```

Hành động hệ thống:
- Phiếu CC-007 chuyển trạng thái: `WAITING --> CALLED`
- Ghi nhận: `called_by_nurse = YTA_NGA`, `called_at = 08:45`
- Màn hình sảnh cập nhật số đang được gọi

Màn hình sảnh cập nhật:
```
╔══════════════════════════════════════════════════╗
║  MỜI BỆNH NHÂN NGUYỄN VĂN NGHÈO                 ║
║  Số thứ tự: CC-007                               ║
║  Vui lòng đến Phòng 101 — Tòa A, Tầng 1         ║
╚══════════════════════════════════════════════════╝
```

---

### 🩺 BƯỚC 7 — Quy trình phối hợp tại phòng khám giữa Bác sĩ & Y tá

Tại mỗi phòng khám chuyên khoa, mô hình vận hành chuẩn gồm có:
* **01 Bác sĩ chuyên khoa:** Đảm nhiệm chuyên môn y tế.
* **01 Y tá / Điều dưỡng phòng khám:** Đảm nhiệm điều phối bệnh nhân, quản lý hàng đợi và hỗ trợ hành chính.

#### 📋 Chi tiết các bước diễn ra trong phòng khám:

1. **Đón tiếp & Chuẩn bị:**
   * Khi bệnh nhân (ông Nghèo — CC-007) bước vào phòng, **Y tá** tiếp nhận sổ khám bệnh, kiểm tra thông tin đối chiếu với số thứ tự trên màn hình.
   * Y tá đo dấu hiệu sinh tồn sơ bộ (huyết áp, nhịp tim, nhiệt độ) nếu cần thiết, sau đó mời bệnh nhân ngồi vào bàn khám của Bác sĩ.

2. **Bác sĩ thăm khám & Chẩn đoán:**
   * **Bác sĩ** tiến hành hỏi bệnh sử, khám lâm sàng và chẩn đoán.
   * Bác sĩ kê đơn thuốc, ghi lời dặn và ký tên trực tiếp vào **Sổ khám bệnh** (hoặc phiếu bệnh án). Bác sĩ không cần thao tác phức tạp trên máy tính để tập trung 100% vào chuyên môn y khoa.

3. **Làm sao Y tá biết Bác sĩ đã khám xong?**
   * Sau khi hoàn tất chẩn đoán và kê đơn, Bác sĩ **gấp sổ bệnh án và bàn giao lại cho Y tá** (hoặc thông báo bằng lời: *"Đã khám xong ca này"*).
   * Bác sĩ đưa ra chỉ dẫn tiếp theo:
     * *Phương án A:* Đã xong, cho bệnh nhân lấy thuốc về điều trị.
     * *Phương án B:* Ca này cần chuyển sang khoa khác hoặc làm xét nghiệm chuyên sâu.

4. **Y tá hoàn tất lượt khám trên hệ thống:**
   * **Y tá** ngồi tại bàn máy tính phòng khám, nhấn nút **"Hoàn tất khám"** (icon tick xanh <i class="lucide lucide-check-circle"></i>) trên giao diện điều hành hàng đợi:

   ```http
   POST /nurse/queue/{ticketId}/done
   ```

   * **Hệ thống ghi nhận:**
     * Phiếu CC-007 chuyển trạng thái: `CALLED (Đang khám) --> DONE (Hoàn tất)`
     * Ghi nhận: `done_at = 09:20`
     * Màn hình sảnh gỡ số CC-007 khỏi danh sách đang khám
     * Thống kê Dashboard Admin cập nhật (+1 lượt khám hoàn thành hôm nay)

5. **Hướng dẫn bệnh nhân & Mời ca tiếp theo:**
   * Y tá dặn dò bệnh nhân: cầm sổ khám và thẻ BHYT ra quầy thanh toán / nhà thuốc BHYT để nhận thuốc ra về.
   * Sau khi bệnh nhân bước ra, Y tá nhấn **"Gọi số tiếp theo"** (`call-next`), loa và màn hình sảnh tự động gọi bệnh nhân tiếp theo vào phòng.

---

### 🔄 TÌNH HUỐNG NGOẠI LỆ — Chuyển khoa

Nếu sau khi thăm khám, Bác sĩ Cấp Cứu nhận định ông Nghèo cần điều trị chuyên sâu hơn tại **Khoa Tim Mạch**:
1. Bác sĩ chỉ định: *"Chuyển bệnh nhân sang Khoa Tim Mạch để đo điện tâm đồ và siêu âm tim chuyên sâu"*.
2. **Y tá** nhấn nút **"Chuyển khoa"** (icon mũi tên đảo chiều <i class="lucide lucide-arrow-right-left"></i>) ngay trên dòng bệnh nhân trong bảng hàng đợi:

```http
POST /nurse/queue/transfer
  ticketId:        {uuid-cc-007}
  newDepartmentId: d3333333-3333-3333-3333-333333333333  (Khoa Tim Mạch)
  reason:          "Nghi ngờ nhồi máu cơ tim — cần điện tâm đồ chuyên sâu"
```

Hệ thống tự động:
1. Phiếu CC-007 cũ chuyển trạng thái: `TRANSFERRED` (ghi nhận lý do chuyển khoa để kiểm toán y khoa).
2. Tự động sinh phiếu mới tại Khoa Tim Mạch: **TM-012** (kế thừa mức ưu tiên M2).
3. Màn hình Khoa Tim Mạch lập tức cập nhật ông Nghèo vào danh sách chờ khám.
4. Y tá bàn giao sổ, hướng dẫn ông Nghèo di chuyển sang Phòng 204 — Tòa B (Khoa Tim Mạch).

---

## 📊 TỔNG KẾT LUỒNG VẬN HÀNH

```
 BỆNH NHÂN           HỆ THỐNG AI / SẢNH           Y TÁ PHÒNG KHÁM            BÁC SĨ CHUYÊN KHOA
     │                       │                           │                           │
     ├─ Đăng nhập BHYT ─────>│                           │                           │
     │   (/login)            │                           │                           │
     ├─ Chat triệu chứng ───>│                           │                           │
     │   (/chat)             │                           │                           │
     │                       │── Phân tích M1-M5 ────────┤                           │
     │                       │   & Phân luồng khoa       │                           │
     │<── Nhận phiếu khám ───┤                           │                           │
     │    (CC-007)           │                           │                           │
     │                       │                           │                           │
     │   Chờ tại sảnh        │── Hiển thị hàng đợi ─────>│                           │
     │   (/display)          │   (/nurse/queue)          │                           │
     │                       │                           │                           │
     │<── Nghe gọi số ───────┼── Hiển thị mời vào ───────┼── Bấm "Gọi số tiếp theo"  │
     │    (CC-007)           │   (Màn hình sảnh)         │   (/call-next)            │
     │                       │                           │                           │
     │── Bước vào phòng ────────────────────────────────>│── Đón bệnh nhân, nhận sổ  │
     │                       │                           │── Mời vào bàn bác sĩ ────>│
     │                       │                           │                           │
     │                       │                           │                           ├── Thăm khám lâm sàng
     │                       │                           │                           ├── Kê đơn & ghi sổ khám
     │                       │                           │<── Bàn giao sổ khám bệnh ─┤   "Đã xong ca này"
     │                       │                           │                           │
     │                       │<── Bấm "Hoàn tất khám" ───┤                           │
     │                       │    (/done) --> DONE       │                           │
     │<── Nhận lại sổ khám ──────────────────────────────┤                           │
     │    và hướng dẫn thuốc │                           │                           │
     │                       │                           ├── Bấm "Gọi số tiếp theo"  │
     │   Ra quầy thuốc       │                           │   đón ca mới              │
     v                       v                           v                           v
 Hoàn tất khám          Cập nhật thống kê         Hàng đợi chuyển sang ca sau   Sẵn sàng khám ca sau
```

---

## ⚙️ CÁC THÀNH PHẦN KỸ THUẬT LIÊN QUAN

| Thành phần | Vai trò | Địa chỉ |
|-----------|---------|---------|
| **Spring Boot Backend** | Xử lý nghiệp vụ chính | Port `8081` |
| **FastAPI AI Service** | Phân tích triệu chứng, gợi ý khoa | Python service |
| **PostgreSQL** | Lưu trữ users, tickets, departments | `hospital_db` |
| **Thymeleaf Templates** | Giao diện web server-side render | `src/main/resources/templates/` |
| **Spring Security** | Xác thực & phân quyền (PATIENT/NURSE/DOCTOR/ADMIN) | `HmsUserDetailsService` |

---

## 🔐 PHÂN QUYỀN TRONG HỆ THỐNG

| Role | Màn hình truy cập / Vai trò | URL Pattern |
|------|-----------------------------|-------------|
| `PATIENT` | Chat AI, xem phiếu khám cá nhân | `/chat`, `/ticket` |
| `NURSE` | Quản lý hàng đợi, gọi số, chuyển khoa, xác nhận hoàn tất khám | `/nurse/**` |
| `DOCTOR` | Thăm khám lâm sàng, kê đơn sổ khám bệnh (hoặc xem hàng đợi khoa) | `/nurse/**` (hoặc phân hệ EMR tương lai) |
| `ADMIN` | Quản trị tài khoản, khoa phòng, thống kê báo cáo | `/admin/**` |
| Public | Màn hình hiển thị hàng đợi sảnh chờ | `/display` |

---

## 📈 MỨC ƯU TIÊN PHÂN LUỒNG AI (Thang M1–M5)

| Mức | Tên | Thời gian chờ tối đa | Ví dụ triệu chứng |
|-----|-----|---------------------|------------------|
| **M1** | Ngay lập tức | Không chờ | Ngừng tim, ngừng thở, hôn mê |
| **M2** | Khẩn cấp | <= 15 phút | Đau ngực, khó thở nặng |
| **M3** | Ưu tiên | <= 30 phút | Gãy xương, đau bụng dữ dội |
| **M4** | Thông thường | <= 60 phút | Sốt, cảm cúm thông thường |
| **M5** | Không khẩn | Có thể hẹn lịch | Khám định kỳ, tư vấn |

---

*Tài liệu được tạo ngày 06/10/2026 — MedAssist AI Hospital Queue System v1.0*
