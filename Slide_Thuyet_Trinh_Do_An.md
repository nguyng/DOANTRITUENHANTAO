---
marp: true
theme: default
paginate: true
header: 'Báo cáo Đề tài Trí tuệ Nhân tạo'
footer: 'Hệ thống Trợ lý ảo tiếp đón và phân luồng'
---

# 🤖 Báo cáo Định hướng Đồ án TTNT
## Đề tài: Hệ thống Trợ lý ảo hỗ trợ tiếp đón và phân luồng bệnh nhân
**Nhóm thực hiện:** [Tên Nhóm của bạn] (2 thành viên)
**Ngày:** [Ngày/Tháng/Năm]

---

# 📋 Nội dung trình bày

**Phần 1: Nhận thức & Định hướng Đồ án**
1. Hiểu về Yêu cầu môn học & Tiêu chí đánh giá
2. Tổng quan các hướng đề tài & Lý do chọn đề tài
3. Giới thiệu Nhóm & Đề tài sơ bộ

**Phần 2: Xác định vấn đề & Thu thập dữ liệu**
4. Đề cương chi tiết (Vấn đề, Mục tiêu, Phạm vi)
5. Phương pháp tìm & thu thập dataset
6. Các bước tiền xử lý dữ liệu dự kiến

---

# 🎯 1. Yêu cầu & Tiêu chí Đồ án

- **Mục tiêu nhóm hướng đến:** Xây dựng một ứng dụng AI giải quyết bài toán thực tế nhức nhối trong Y tế (Healthcare).
- **Sản phẩm cam kết (Deliverables):**
  - Source code hoàn chỉnh quản lý qua Github.
  - Ứng dụng Web Chat có tích hợp AI (NLP & Computer Vision).
  - Báo cáo chi tiết quá trình huấn luyện mô hình.
- **Đáp ứng tiêu chí đánh giá:** Đề tài có tính thực tiễn cao, kết hợp nhiều thuật toán và mô hình (PhoBERT, YOLOv8).

---

# 💡 2. Các hướng đề tài AI & Lựa chọn

- **Thị giác máy tính (CV):** Nhận diện, trích xuất thông tin ảnh (OCR).
- **Xử lý ngôn ngữ tự nhiên (NLP):** Chatbot, hiểu ngôn ngữ (NLU).
- **Hệ thống gợi ý & Tối ưu hoá:** Phân luồng dữ liệu.
- 👉 **Lý do nhóm chọn đề tài này:** 
  Kết hợp cả **NLP** (Phân tích triệu chứng) và **Computer Vision** (Nhận diện thẻ BHYT) tạo ra một Trợ lý ảo toàn diện và khác biệt cho Bệnh viện.

---

# 🤝 3. Giới thiệu Nhóm & Đề tài

- **Tên đề tài:** Hệ thống Trợ lý ảo hỗ trợ tiếp đón và phân luồng bệnh nhân ứng dụng AI.
- **Quy mô:** Nhóm 2 thành viên.
- **Phân công vai trò:**
  - **Thành viên 1:** AI/ML Engineer (Huấn luyện PhoBERT, YOLOv8) & Data Engineer (Thu thập, làm sạch dữ liệu).
  - **Thành viên 2:** Developer (Xây dựng Web App, Backend Chat Real-time WebSocket) & System Integration.

---

# 📝 4. Đề cương: Xác định Vấn đề (Problem)

- Tại các bệnh viện, quầy tiếp đón luôn trong tình trạng **quá tải**.
- Bệnh nhân thường bối rối không biết **phải đi khám khoa nào** với các triệu chứng của mình.
- Nhân viên y tế tốn nhiều thời gian trả lời các câu hỏi lặp đi lặp lại (FAQ) và **nhập liệu thủ công** thông tin từ thẻ BHYT.

---

# 🎯 5. Đề cương: Mục tiêu (Objectives)

- Xây dựng Trợ lý ảo (Chatbot) hoạt động 24/7 giải đáp các câu hỏi thường gặp (FAQ).
- Ứng dụng NLP (PhoBERT) để phân tích đoạn chat mô tả triệu chứng ➔ **Tự động gợi ý đúng Chuyên khoa**.
- Ứng dụng Computer Vision (YOLOv8 & OCR) để **nhận diện và trích xuất tự động** mã số Thẻ BHYT/Đơn thuốc từ ảnh do bệnh nhân tải lên.

---

# 🚧 6. Đề cương: Phạm vi (Scope)

- **Giới hạn tính năng:** Tập trung vào khâu "Tiếp đón và Phân luồng", không đi sâu vào chẩn đoán bệnh lý y khoa thay cho Bác sĩ.
- **Giới hạn dữ liệu:** 
  - Phân luồng cho khoảng 10-15 chuyên khoa phổ biến (Nội, Ngoại, Nhi, Tai Mũi Họng,...).
  - Trích xuất OCR áp dụng cho form mẫu thẻ BHYT chuẩn hiện hành tại Việt Nam.

---

# 🗂️ 7. Tìm kiếm & Thu thập Dataset

- **1. Dữ liệu văn bản (FAQ & Triệu chứng - Khoa):**
  - *Công khai:* Tìm kiếm các dataset y tế tiếng Việt trên Huggingface, Kaggle.
  - *Tự thu thập:* Dùng Web Scraping lấy dữ liệu hỏi đáp từ website các bệnh viện lớn (Medlatec, Vinmec).
- **2. Dữ liệu hình ảnh (Thẻ BHYT):**
  - Do tính bảo mật, nhóm sẽ tự tạo tập dữ liệu giả lập (synthetic data) thẻ mẫu hoặc dùng ảnh của người thân/tình nguyện viên để huấn luyện YOLOv8.

---

# 🧹 8. Tiền xử lý dữ liệu

- **Tiền xử lý Văn bản (NLP):**
  - Làm sạch nhiễu, chuẩn hóa Unicode.
  - Tách từ (Word Segmentation) bằng `VnCoreNLP`.
  - Loại bỏ Stop-words y tế, gán nhãn câu hỏi thuộc chuyên khoa nào.
- **Tiền xử lý Ảnh (Computer Vision):**
  - Resize ảnh về chuẩn đầu vào của YOLO.
  - Chuyển ảnh sang thang độ xám, tăng độ tương phản để trích xuất OCR tốt hơn.
  - Gán nhãn (Bounding Box Labeling) vùng ảnh chứa thẻ/mã số.

---

# ❓ Q&A

**Cảm ơn Thầy và các bạn đã lắng nghe!**

Nhóm rất mong nhận được góp ý để hoàn thiện đề cương và tiếp tục triển khai dự án.
