# 📋 BÁO CÁO KIỂM THỬ VẬN HÀNH HỆ THỐNG PHÂN LUỒNG BỆNH VIỆN
## Đối chiếu End-to-End với Kịch bản Vận hành MedAssist AI

> **Ngày thực hiện:** 07/10/2026  
> **Người thực hiện:** Hệ thống AI Pair-Programming  
> **Mục tiêu:** Kiểm thử thực tế toàn diện luồng vận hành theo tài liệu [KICH_BAN_VAN_HANH.md](file:///d:/DOANTRITUENHANTAO/KICH_BAN_VAN_HANH.md), phát hiện các sai lệch, sửa lỗi triệt để và lập báo cáo.

---

## 1. MÔI TRƯỜNG KIỂM THỬ

| Thành phần | Thông số kỹ thuật | Trạng thái |
|------------|-------------------|-----------|
| **Backend** | Spring Boot 4.1.1 — Java 21 LTS (Temurin-21.0.6) | Port `8081` — Running |
| **Database** | PostgreSQL 18.6 | Port `5432` (`hospital_db`) — Connected |
| **Công cụ kiểm thử** | Python 3.13 E2E Integration Suite (`test_scenario.py`) | 100% Passed |
| **Tài khoản test** | `HN47900000001` (Bệnh nhân), `YTA_NGA` (Y tá), `ADMIN_001` (Admin) | Hợp lệ |

---

## 2. CÁC LỖI ĐÃ PHÁT HIỆN & GIẢI PHÁP ĐÃ KHẮC PHỤC

Trong quá trình đối chiếu luồng thực tế với kịch bản, hệ thống đã phát hiện và xử lý thành công **5 điểm sai lệch kỹ thuật**:

### ❌ Lỗi 1: Hàng đợi Y tá làm "biến mất" bệnh nhân đang khám
* **Hiện tượng:** Khi Y tá bấm "Gọi số tiếp theo" (`call-next`), phiếu chuyển sang `CALLED`. Tuy nhiên, phương thức `findWaitingByDepartment` chỉ query phiếu `WAITING`, dẫn tới bệnh nhân vừa gọi lập tức biến mất khỏi màn hình Y tá. Y tá không thể thấy trạng thái *"Đang khám"* và không có nút *"Hoàn tất khám"* để bấm.
* **Khắc phục:** 
  - Bổ sung query `findByDepartmentIdAndStatuses` trong [TicketRepository.java](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/java/com/ucop/hethongphanluongbenhvien/repository/TicketRepository.java).
  - Viết method `findActiveByDepartment(deptId)` trong [TicketService.java](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/java/com/ucop/hethongphanluongbenhvien/service/TicketService.java) đưa các phiếu `CALLED` (đang khám) lên đầu bảng, tiếp theo là các phiếu `WAITING`.

### ❌ Lỗi 2: Màn hình sảnh (`/display`) không hiển thị ca đang khám
* **Hiện tượng:** Trên màn hình sảnh chờ, ô "Đang khám" luôn báo trống `---` dù đã có bệnh nhân được gọi vào phòng. Đồng thời URL `/display` chưa được cấp quyền trong Spring Security.
* **Khắc phục:**
  - Cập nhật [PublicController.java](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/java/com/ucop/hethongphanluongbenhvien/controller/PublicController.java) ánh xạ cả `/public/display` và `/display`, trích xuất đúng danh sách `called` từ active tickets.
  - Cấu hình [SecurityConfig.java](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/java/com/ucop/hethongphanluongbenhvien/config/SecurityConfig.java) cho phép public `/display` và `/display/**`.

### ❌ Lỗi 3: Lỗi so sánh Enum trong Thymeleaf làm ẩn nút hành động
* **Hiện tượng:** Trong file [queue-detail.html](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/resources/templates/nurse/queue-detail.html), biểu thức SpEL `${t.status == 'WAITING'}` và `${t.status == 'CALLED'}` luôn trả về `false` do so sánh trực tiếp Java Enum với String literal. Hậu quả là toàn bộ nút "Gọi số", "Hoàn tất khám", "Chuyển khoa" không được hiển thị.
* **Khắc phục:** Đổi toàn bộ biểu thức so sánh sang `t.status.name() == '...'` và `t.priority.name() == '...'`, đồng thời bổ sung các getter tiện ích `getStatusName()`, `getPriorityName()`, `getPatientName()`, `getDepartmentName()` vào [Ticket.java](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/java/com/ucop/hethongphanluongbenhvien/entity/Ticket.java).

### ❌ Lỗi 4: Lỗi 500 khi bệnh nhân mở trang Phiếu khám (`/ticket`)
* **Hiện tượng:** Truy cập `/ticket` văng lỗi `EL1008E: Property or field 'queuePosition' cannot be found on object of type Ticket`.
* **Khắc phục:** File [ticket.html](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/resources/templates/patient/ticket.html) gọi nhầm `${ticket.queuePosition}` thay vì biến model `${queuePosition}`. Đã điều chỉnh lại biến model chuẩn xác và kiểm tra render thành công HTTP 200.

### ❌ Lỗi 5: Yêu cầu tham số `deptId` quá khắt khe gây lỗi 400
* **Hiện tượng:** Endpoint `POST /nurse/queue/{ticketId}/done` bắt buộc `@RequestParam UUID deptId`. Nếu form gửi thiếu thì sinh lỗi 400 Bad Request.
* **Khắc phục:** Sửa `@RequestParam(required = false) UUID deptId` trong [NurseController.java](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/src/main/java/com/ucop/hethongphanluongbenhvien/controller/NurseController.java), tự động trích xuất `ticket.getDepartment().getId()` làm fallback an toàn.

---

## 3. KẾT QUẢ KIỂM THỬ THỰC TẾ CHI TIẾT

| STT | Bước kịch bản | Thao tác thực tế | Kết quả mong đợi | Kết quả kiểm thử | Đánh giá |
|:---:|:---|:---|:---|:---|:---:|
| **1** | **Bệnh nhân đăng nhập** | `POST /login` với BHYT `HN47900000001` / `123456` | Đăng nhập thành công, chuyển hướng `/chat` | HTTP 200, redirect `/chat` | **PASS ✅** |
| **2** | **Chat mô tả triệu chứng** | `POST /chat/send` gửi triệu chứng đau ngực, khó thở | Lưu tin nhắn vào DB, Bot phản hồi | HTTP 200, phản hồi từ bot lưu DB | **PASS ✅** |
| **3** | **Phát phiếu khám điện tử** | `POST /chat/confirm` chọn Khoa Cấp Cứu, mức M2 | Sinh phiếu khám số mới, redirect `/ticket` | HTTP 200, phiếu `NB-004` (M2 Khẩn cấp) | **PASS ✅** |
| **4** | **Màn hình sảnh chờ** | `GET /display?deptId=...` không cần đăng nhập | Hiển thị phiếu `NB-004` trong danh sách chờ | HTTP 200, hiển thị `NB-004` realtime | **PASS ✅** |
| **5** | **Y tá mở hàng đợi khoa** | Y tá `YTA_NGA` đăng nhập `GET /nurse/queue/{deptId}` | Hiển thị danh sách chờ có phiếu `NB-004` | HTTP 200, phiếu `NB-004` xuất hiện ở hàng đợi | **PASS ✅** |
| **6** | **Y tá gọi số vào phòng** | `POST /nurse/queue/{deptId}/call-next` | Phiếu chuyển `CALLED`, sảnh hiện tên & số đang khám | Phiếu sang `Đang khám`, sảnh hiện `NB-004` | **PASS ✅** |
| **7** | **Bác sĩ khám & Y tá hoàn tất** | `POST /nurse/queue/{ticketId}/done` | Phiếu chuyển `DONE`, dọn khỏi hàng đợi hoạt động | HTTP 200, phiếu kết thúc, hàng đợi dọn sạch | **PASS ✅** |
| **8** | **Tình huống: Chuyển khoa** | `POST /nurse/queue/transfer` sang Khoa Tim Mạch | Phiếu cũ `TRANSFERRED`, sinh phiếu mới ở Tim Mạch | HTTP 200, chuyển sang Tim Mạch thành công | **PASS ✅** |
| **9** | **Thống kê Admin** | Admin `ADMIN_001` đăng nhập `/admin/dashboard` | Dashboard tải đầy đủ, tăng lượt khám hôm nay | HTTP 200, số liệu thống kê đầy đủ | **PASS ✅** |

---

## 4. KẾT LUẬN & KIẾN NGHỊ

1. **Độ tin cậy của luồng vận hành:**
   * Sau khi sửa chữa 5 lỗi kỹ thuật trên, hệ thống hiện đã **chạy mượt mà 100% đúng như kịch bản đã xây dựng**.
   * Quy trình phân vai: **Bác sĩ khám bệnh, chẩn đoán, ghi sổ khám ➔ Y tá điều phối trên phần mềm (gọi số, hoàn tất, chuyển khoa)** đã hoạt động chính xác cả về mặt dữ liệu backend lẫn giao diện người dùng.

2. **File script tự động hóa:**
   * Script kiểm thử tự động đã được lưu tại [test_scenario.py](file:///d:/DOANTRITUENHANTAO/HeThongPhanLuongBenhVien/scratch/test_scenario.py). Bạn có thể chạy lại bất cứ lúc nào bằng lệnh:
     ```bash
     python d:\DOANTRITUENHANTAO\HeThongPhanLuongBenhVien\scratch\test_scenario.py
     ```
