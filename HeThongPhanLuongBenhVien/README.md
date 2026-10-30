# 🏥 MEDASSIST AI - Hệ thống Trợ lý ảo Phân luồng Bệnh viện

**Mục tiêu cốt lõi:** Giải quyết tình trạng quá tải, ùn ứ tại khâu tiếp đón của bệnh viện bằng cách số hóa hoàn toàn quy trình đăng ký khám. Ứng dụng Trí tuệ Nhân tạo Cục bộ (Local AI LLM) để lắng nghe triệu chứng, tự động hỏi sâu triệu chứng mấu chốt và phân luồng bệnh nhân về đúng chuyên khoa, đồng thời kiểm tra tính chính chủ của thẻ Bảo hiểm Y tế (BHYT) liên kết với Căn cước công dân (CCCD).

**Kiến trúc công nghệ:**
*   **Backend & Giao diện:** Spring Boot 4.1.1 — Java 21 LTS, Thymeleaf (Server-Side Rendering), Tailwind CSS, Lucide Icons.
*   **Database:** PostgreSQL 18 (Lưu trữ người dùng CCCD, BHYT 15 ký tự, Phiếu khám, Hàng đợi, Nhật ký AI).
*   **AI Engine (Local LLM):** Ollama chạy mô hình `qwen2.5-coder:3b` cục bộ tại cổng `11434`, không phụ thuộc internet bên ngoài, bảo mật dữ liệu y tế người bệnh.

---

## ⚙️ CÁCH HOẠT ĐỘNG & CHỨC NĂNG THEO TỪNG ĐỐI TƯỢNG (ACTORS)

Hệ thống được chia làm 4 phân hệ giao diện chính phục vụ cho 4 luồng người dùng:

### 1. Phân hệ Bệnh nhân (Patient Portal)
Thiết kế theo chuẩn *Mobile-first*, thao tác trực quan như một ứng dụng chat:

*   **Đăng nhập Định danh CCCD (`/login`):**
    *   Bệnh nhân đăng nhập bằng Số Căn cước công dân 12 số (VD: `079204000001` / Mật khẩu: `123456`).
    *   Hỗ trợ tương thích ngược: Cho phép đăng nhập bằng số thẻ BHYT cũ.
*   **Chatbot AI Phân luồng 2 Giai đoạn (`/chat`):**
    *   *Giai đoạn 1 (Khai thác triệu chứng):* Khi bệnh nhân mô tả triệu chứng ban đầu (VD: *"Tôi bị đau ngực"*), AI không phán đoán vội mà đặt câu hỏi làm rõ các triệu chứng mấu chốt (tính chất đau, hướng lan, sốt, ho đờm,...).
    *   *Giai đoạn 2 (Chẩn đoán phân biệt & Chốt khoa):* Khi bệnh nhân cung cấp thông tin, AI chẩn đoán phân biệt chính xác chuyên khoa (VD: Phân biệt đau ngực do Tim Mạch vs Hô Hấp), đưa ra mức độ ưu tiên (M1–M5), độ tin cậy và tóm tắt lâm sàng.
    *   *Bẫy Cấp cứu (Red Flag Bypass):* Khi phát hiện dấu hiệu nguy kịch (bất tỉnh, ngừng thở, co giật, nôn ra máu,...), hệ thống tự động ngắt AI và định tuyến thẳng vào **Khoa Cấp Cứu** mức **M1**.
*   **Xác thực BHYT Chính chủ Chống Gian lận (`/api/bhyt/apply`):**
    *   Bệnh nhân nhập mã thẻ BHYT 15 ký tự chuẩn QĐ 1666 (VD: `HN4790000000001`).
    *   Hệ thống kiểm tra 3 lớp: Định dạng → Cơ sở dữ liệu → Cổng tra cứu BHXH.
    *   **Quy tắc toàn vẹn:** Thẻ BHYT bắt buộc phải khớp với CCCD của tài khoản đang đăng nhập. Nếu dùng thẻ của người khác, hệ thống lập tức từ chối áp dụng giảm giá (HTTP 403).
*   **Nhận Phiếu khám & Chỉ dẫn đường đi (`/ticket` & `/directions`):**
    *   Xác nhận khoa khám, bệnh nhân nhận Số thứ tự điện tử (STT) ngay trên thiết bị.
    *   Xem hướng dẫn vị trí phòng khám cụ thể (Tòa nhà, tầng, số phòng).
*   **Quản lý Hồ sơ & Lịch sử khám (`/profile`, `/visits`):**
    *   Theo dõi thông tin quyền lợi BHYT (mức hưởng 80%, 95%, 100%).
    *   Xem lại lịch sử các lần khám, đơn thuốc và viện phí đã được BHYT chi trả.

### 2. Phân hệ Y tá / Điều phối phòng khám (Nurse Portal)
Dành cho nhân viên trực tại các Khoa, tối ưu trên màn hình Tablet/Desktop:

*   **Dashboard Hàng đợi chuyên khoa (`/nurse/queue`):**
    *   Theo dõi số lượng bệnh nhân đang chờ, đang khám, và các ca khẩn cấp cần ưu tiên.
*   **Điều phối khám bệnh (`/nurse/queue-detail`):**
    *   📢 **Gọi số tiếp theo (`call-next`):** Chuyển trạng thái bệnh nhân sang "Đang khám", đồng bộ tức thời ra màn hình sảnh.
    *   ✅ **Hoàn tất khám (`done`):** Ghi nhận lượt khám hoàn thành, tự động cập nhật báo cáo.
    *   🔄 **Chuyển khoa (`transfer`):** Khi bác sĩ chỉ định khám thêm chuyên khoa khác, Y tá chuyển bệnh nhân sang khoa mới mà bệnh nhân không cần xếp hàng lấy số lại từ đầu.

### 3. Phân hệ Quản trị viên (Admin Dashboard)
Dành cho Ban giám đốc và Quản trị hệ thống:

*   **Thống kê & Giám sát (`/admin/dashboard`):**
    *   Theo dõi lưu lượng khám bệnh real-time của toàn bệnh viện.
    *   Nhật ký AI (`AiAnalysisLog`): Theo dõi độ chính xác của mô hình AI, thời gian xử lý inference (ms) và tỷ lệ chuyển khoa.
*   **Quản lý Nhân sự & Khoa phòng (`/admin/staff`, `/admin/departments`):**
    *   Quản lý tài khoản Bác sĩ, Y tá và cấu hình phòng khám.

### 4. Màn hình Sảnh Chờ Công cộng (Public Display)
*   **Màn hình Hành lang (`/display`):**
    *   Giao diện tivi treo trước cửa phòng khám, không yêu cầu đăng nhập.
    *   Hiển thị cực lớn: Số thứ tự Đang khám và Danh sách chờ tiếp theo (Các ca cấp cứu M1/M2 tự động đưa lên đầu).
    *   Tự động cập nhật real-time.

---

## 💡 ĐIỂM SÁNG CỦA DỰ ÁN

1. **Local AI Hoàn Toàn & Bảo Mật Dữ Liệu Y Tế:** Ứng dụng Ollama `qwen2.5-coder:3b` chạy trực tiếp trên máy chủ bệnh viện, không phụ thuộc internet và không rò rỉ dữ liệu bệnh nhân.
2. **Quy trình Lâm sàng 2 Giai đoạn Chuyên Sâu:** Tránh sai sót phân luồng do triệu chứng ban đầu giống nhau bằng cách chủ động đặt câu hỏi thăm dò triệu chứng mấu chốt trước khi ra quyết định.
3. **An toàn Tuyệt đối với Bẫy Cấp cứu (Red Flag):** Nhận diện tức thì các từ khóa nguy kịch tính mạng để đẩy thẳng vào Khoa Cấp Cứu M1 mà không cần chờ AI suy luận.
4. **Kiểm Soát Thẻ BHYT Chính Chủ:** Liên kết chặt chẽ CCCD 12 số với mã BHYT 15 ký tự, ngăn chặn hoàn toàn việc sử dụng trái phép thẻ BHYT của người khác.

---

## 🚀 HƯỚNG DẪN CÀI ĐẶT & CHẠY HỆ THỐNG

### Yêu cầu môi trường
* Java 21 LTS (Temurin / Adoptium)
* PostgreSQL 18
* Ollama (đã tải model `qwen2.5-coder:3b`)
* Python 3.10+ (chạy bộ test E2E)

### Các bước khởi chạy
```powershell
# 1. Khởi động PostgreSQL và nạp dữ liệu:
# Khởi tạo database 'hospital_db' và chạy file database.sql

# 2. Khởi động mô hình AI:
ollama run qwen2.5-coder:3b

# 3. Chạy backend Spring Boot:
cd HeThongPhanLuongBenhVien
./mvnw spring-boot:run

# 4. Truy cập web: http://localhost:8081/login
# Đăng nhập bằng CCCD: 079204000001 / Mật khẩu: 123456
```

### Chạy Kiểm thử Tự động
```powershell
# Chạy toàn bộ 15 Unit Tests (Surefire):
./mvnw test

# Chạy 9 kịch bản Kiểm thử Tích hợp End-to-End:
python test_integration.py
```