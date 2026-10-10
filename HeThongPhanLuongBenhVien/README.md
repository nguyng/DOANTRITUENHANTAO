# 🏥 MEDASSIST AI - Hệ thống Trợ lý ảo Phân luồng Bệnh viện

**Mục tiêu cốt lõi:** Giải quyết tình trạng quá tải, ùn ứ tại khâu tiếp đón của bệnh viện bằng cách số hóa hoàn toàn quy trình đăng ký khám. Ứng dụng Trí tuệ Nhân tạo (AI) để lắng nghe triệu chứng, tự động phân luồng bệnh nhân về đúng chuyên khoa, đồng thời trích xuất thông tin thẻ Bảo hiểm Y tế (BHYT) để giảm thiểu thủ tục giấy tờ hành chính.

**Kiến trúc công nghệ (Hybrid System):**
*   **Frontend & Core Backend:** Spring Boot 3 + Thymeleaf (Server-Side Rendering) + Tailwind CSS + Lucide Icons.
*   **Database:** PostgreSQL (Lưu trữ dữ liệu có cấu trúc: Người dùng, Phiếu khám, Hàng đợi, Lịch sử khám).
*   **AI Engine (Microservice):** FastAPI (Python) chịu trách nhiệm xử lý ngôn ngữ tự nhiên (NLP) phân luồng bệnh và xử lý ảnh (OCR) quét thẻ BHYT.

---

## ⚙️ CÁCH HOẠT ĐỘNG & CHỨC NĂNG THEO TỪNG ĐỐI TƯỢNG (ACTORS)

Hệ thống được chia làm 4 phân hệ giao diện chính phục vụ cho 4 luồng người dùng khác nhau:

### 1. Phân hệ Bệnh nhân (Patient Portal)
Đây là phân hệ thiết kế theo chuẩn *Mobile-first*, thân thiện, thao tác như một ứng dụng chat thông thường để bệnh nhân dễ dàng sử dụng.

*   **Chatbot AI Phân luồng (`/chat`):**
    *   Bệnh nhân chat mô tả triệu chứng (VD: "Tôi bị đau đầu, buồn nôn từ sáng").
    *   AI sẽ phân tích và trả về **Kết quả Triage**: Bệnh nhân đang ở mức độ khẩn cấp nào (Bình thường / Ưu tiên / Cấp cứu), cần đi đến Khoa nào, và độ tin cậy của AI là bao nhiêu.
*   **Quét thẻ BHYT tự động (OCR):**
    *   Bệnh nhân chụp ảnh thẻ BHYT gửi vào chat. Hệ thống AI tự động đọc mã số, họ tên, ngày sinh, hạn sử dụng và mức hưởng BHYT (80%, 100%...).
    *   Hệ thống tự động liên kết dữ liệu này vào Hồ sơ bệnh án mà không cần y tá nhập tay.
*   **Nhận Phiếu khám & Chỉ đường điện tử (`/ticket` & `/directions`):**
    *   Chốt khoa khám xong, bệnh nhân nhận Số thứ tự (STT) điện tử ngay trên điện thoại.
    *   Hệ thống cung cấp "Chỉ dẫn đường đi" (VD: Từ sảnh A, đi thang máy lên tầng 3 rẽ trái) để bệnh nhân tự đi đến đúng phòng khám.
*   **Quản lý Hồ sơ & Lịch sử (`/profile`, `/visits`):**
    *   Xem lại thẻ BHYT điện tử (có cảnh báo tự động nếu thẻ sắp hết hạn).
    *   Xem lại đơn thuốc cũ, bác sĩ đã khám, và hóa đơn viện phí chi tiết (hiển thị rõ BHYT đã chi trả bao nhiêu, bệnh nhân cần bù bao nhiêu).

### 2. Phân hệ Y tá / Lễ tân (Nurse/Receptionist Portal)
Dành cho nhân viên trực tại các Khoa, thiết kế tối ưu trên màn hình Tablet/Desktop.

*   **Dashboard Hàng đợi (`/nurse/queue`):**
    *   Theo dõi tổng quan Hàng đợi của Khoa mình quản lý (Bao nhiêu người đang chờ, bao nhiêu ca cấp cứu, bao nhiêu ca đã khám xong).
*   **Điều phối khám bệnh (`/nurse/queue-detail`):**
    *   Bấm nút 📢 **Gọi số tiếp theo**: Hệ thống tự động chuyển trạng thái bệnh nhân từ "Chờ khám" sang "Đang khám" và đẩy thông tin ra Màn hình hành lang.
    *   **Chuyển khoa (Transfer):** Nếu bác sĩ chẩn đoán cần chuyển bệnh nhân sang Khoa khác (hoặc do AI phân luồng sai), Y tá bấm nút chuyển khoa, hệ thống tự động đẩy bệnh nhân sang hàng đợi của Khoa mới mà bệnh nhân không cần đi lấy số lại.
*   **Tra cứu Hồ sơ nhanh (`/nurse/search`):**
    *   Tìm kiếm bệnh nhân bằng Mã BHYT hoặc Tên, xem lịch sử khám trước khi bác sĩ gọi vào phòng.

### 3. Phân hệ Quản trị viên (Admin Dashboard)
Dành cho Ban giám đốc hoặc Trưởng phòng ban.

*   **Thống kê & Biểu đồ (`/admin/dashboard`, `/admin/reports`):**
    *   Theo dõi real-time lưu lượng bệnh nhân của toàn bộ bệnh viện.
    *   Đánh giá độ chính xác của AI (AI Accuracy) dựa trên số lượng bệnh nhân bị y tá "Chuyển khoa" (Nếu AI phân luồng đúng, tỷ lệ chuyển khoa thấp).
    *   Báo cáo doanh thu viện phí và tỷ lệ chi trả của BHYT.
*   **Quản lý Nhân sự (`/admin/staff`):**
    *   Thêm mới, cấp tài khoản, khóa tài khoản cho Bác sĩ và Y tá. Phân bổ họ vào các Khoa cụ thể.
*   **Huấn luyện AI / Cấu hình FAQ (`/admin/faq`):**
    *   Thêm các bộ câu hỏi thường gặp (Giá tiền, giờ làm việc, thủ tục) để Chatbot tự động trả lời cho bệnh nhân.

### 4. Màn hình hiển thị Công cộng (Public Display)
*   **Màn hình Hành lang (`/public/display`):**
    *   Giao diện tivi được treo trước cửa mỗi phòng khám.
    *   Hiển thị cực lớn: Số thứ tự Đang khám và Danh sách chờ tiếp theo (Các ca cấp cứu màu Đỏ sẽ tự động được xếp lên đầu tiên).
    *   Chạy dòng chữ chạy (Marquee) thông báo tin tức của bệnh viện ở dưới cùng. Tự động làm mới (Auto-refresh) real-time.

---

## 💡 ĐIỂM SÁNG CỦA DỰ ÁN (Selling points)
1. **Luồng dữ liệu khép kín:** Từ lúc bệnh nhân bước vào, tương tác với bot, chụp BHYT, lấy số, đi theo hướng dẫn bản đồ, đến lúc bác sĩ khám xong và thanh toán trừ BHYT -> Mọi thứ diễn ra tự động mà không cần đi qua quầy đăng ký vật lý.
2. **Ưu tiên Cấp cứu thông minh:** AI không chỉ đoán Khoa, mà còn phát hiện "Mức độ nghiêm trọng" (Severity). Nếu phát hiện Keyword cực đoan (Đau thắt ngực, chảy máu nhiều), AI đánh cờ `EMERGENCY` và đẩy thẳng số thứ tự của bệnh nhân đó lên TOP 1 của hàng đợi y tá.
3. **Giao diện hiện đại (UI/UX cao cấp):** Khác với các phần mềm quản lý bệnh viện truyền thống khô khan, MedAssist có giao diện thân thiện với bệnh nhân (các thẻ bo góc, gradient, thiết kế theo hướng chat-app).
