# 📋 BÁO CÁO KIỂM THỬ VẬN HÀNH HỆ THỐNG PHÂN LUỒNG BỆNH VIỆN
## Đối chiếu End-to-End với Kịch bản Vận hành MedAssist AI & Local LLM

> **Ngày thực hiện:** 30–31/10/2026  
> **Người thực hiện:** Hệ thống AI Pair-Programming  
> **Mục tiêu:** Kiểm thử thực tế toàn diện luồng vận hành sau khi nâng cấp kiến trúc: **Đăng nhập định danh CCCD, Xác thực thẻ BHYT chính chủ 15 ký tự, Phân luồng lâm sàng 2 giai đoạn qua Ollama `qwen2.5-coder:3b`** và xử lý cấp cứu khẩn cấp.

---

## 1. MÔI TRƯỜNG KIỂM THỬ

| Thành phần | Thông số kỹ thuật | Trạng thái |
|------------|-------------------|:---:|
| **Backend Core** | Spring Boot 4.1.1 — Java 21 LTS (Temurin-21.0.6) | Port `8081` — Running |
| **Cơ sở dữ liệu** | PostgreSQL 18.6 | Port `5432` (`hospital_db`) — Connected |
| **Mô hình AI** | Local LLM via Ollama (`qwen2.5-coder:3b`, 1.9 GB) | Port `11434` — Active |
| **Công cụ kiểm thử** | Maven Surefire Test Runner + Python 3.13 E2E Suite | 100% Passed |
| **Tài khoản kiểm thử** | CCCD `079204000001` (Bệnh nhân), `YTA_NGA` (Y tá), `ADMIN_001` (Admin) | Hợp lệ |

---

## 2. KẾT QUẢ KIỂM THỬ ĐƠN VỊ (UNIT TESTS) — 15/15 PASS (100%)

Toàn bộ 15 ca kiểm thử đơn vị được thực thi tự động qua Maven Surefire, kiểm soát chặt chẽ các tầng xử lý nghiệp vụ:

| Test Suite | Số ca | Kết quả | Nội dung kiểm thử chi tiết |
|---|:---:|:---:|---|
| **BhytVerificationServiceTest** | 6 | **PASS ✅** | • Mã thẻ rỗng (báo lỗi `E00`)<br>• Độ dài không đúng 15 ký tự (`E01`)<br>• Mã đối tượng không hợp lệ (`E01`)<br>• Mã tỉnh không tồn tại (`E01`)<br>• Tra cứu thẻ hợp lệ từ Mock BHXH API (TE hưởng 100%)<br>• Tra cứu thẻ nội bộ từ CSDL bệnh viện (HN hưởng 100%) |
| **UserCccdAndBhytMatchingTest** | 3 | **PASS ✅** | • Tra cứu người dùng bằng số CCCD 12 chữ số<br>• Cơ chế tra cứu đa định danh `findByAnyIdentifier`<br>• Tính toàn vẹn: BHYT nhập vào bắt buộc phải khớp với CCCD của tài khoản |
| **TwoPhaseClinicalTriageTest** | 5 | **PASS ✅** | • Red Flag: Nhận diện từ khóa nguy kịch (bất tỉnh, nôn ra máu, ngừng thở)<br>• Đánh giá mô tả chi tiết `isVeryDetailed` (>120 ký tự + 2 markers)<br>• Chuẩn hóa mã chuyên khoa `normalizeDeptCode` đa biến thể<br>• Fallback Giai đoạn 1: Đặt câu hỏi khai thác chuyên sâu khi mô tả ngắn<br>• Fallback Giai đoạn 2: Phán đoán phân biệt chính xác vào Khoa Hô Hấp |
| **HeThongPhanLuongBenhVienApplicationTests** | 1 | **PASS ✅** | • Nạp toàn diện Spring Boot Context kết nối trực tiếp PostgreSQL 18 |

---

## 3. KẾT QUẢ KIỂM THỬ TÍCH HỢP END-TO-END (E2E INTEGRATION TEST) — 9/9 PASS (100%)

Kịch bản được tự động hóa thông qua `test_integration.py` tương tác trực tiếp với máy chủ HTTP đang vận hành:

| STT | Bước kiểm thử | Thao tác thực tế | Kết quả mong đợi | Kết quả kiểm thử thực tế | Đánh giá |
|:---:|:---|:---|:---|:---|:---:|
| **1** | **Kiểm tra Trang Đăng nhập** | `GET /login` | Trang login tải thành công, có nhãn "Số CCCD / Tài khoản" | HTTP 200, hiển thị input CCCD | **PASS ✅** |
| **2** | **Đăng nhập bằng CCCD** | `POST /login` với CCCD `079204000001` / `123456` | Xác thực thành công, thiết lập session, chuyển hướng `/chat` | HTTP 200, redirect `/chat`, nhận diện đúng bệnh nhân Nguyễn Văn Nghèo | **PASS ✅** |
| **3** | **Khởi tạo Chat Session** | `GET /chat` khi đã đăng nhập | Sinh định danh phiên khám UUID duy nhất trong DB | HTTP 200, Session ID khởi tạo thành công | **PASS ✅** |
| **4** | **AI Giai đoạn 1 (Thăm dò)** | Gửi: *"Tôi bị đau ngực"* | AI không chốt khoa vội, đặt câu hỏi làm rõ triệu chứng mấu chốt | AI phản hồi hỏi tính chất đau, hướng lan, sốt, không sinh thẻ phân luồng | **PASS ✅** |
| **5** | **AI Giai đoạn 2 (Chẩn đoán phân biệt)** | Gửi: *"Đau nhói tăng khi hít sâu, ho có đờm và sốt 38.5 độ"* | AI chẩn đoán phân biệt chính xác, đưa ra kết quả Khoa Hô Hấp | AI phân luồng chính xác **Khoa Hô Hấp** (Mức M2 Khẩn cấp, Độ tin cậy > 90%) | **PASS ✅** |
| **6** | **Red Flag Bypass Cấp cứu** | Gửi: *"Bệnh nhân đột ngột bất tỉnh và ngừng thở"* | Tự động kích hoạt bẫy cấp cứu, chuyển thẳng Khoa Cấp Cứu M1 | Phân luồng ngay **Khoa Cấp Cứu**, Mức **M1 Nguy kịch**, bypass AI | **PASS ✅** |
| **7** | **Chặn Gian lận BHYT** | Tài khoản `079204000001` nhập thẻ BHYT của người khác `CN3790000000001` | Hệ thống từ chối áp dụng giảm giá do không khớp CCCD | **HTTP 403 Forbidden**: *"Số BHYT không khớp với CCCD đang đăng nhập"* | **PASS ✅** |
| **8** | **Áp dụng BHYT Chính chủ** | Bệnh nhân nhập đúng thẻ chính chủ `HN4790000000001` | Xác thực thành công, cập nhật mức giảm giá BHYT | **HTTP 200 OK**: Đối tượng Hộ nghèo, mức hưởng 100% chi phí KCB | **PASS ✅** |
| **9** | **Tương thích ngược BHYT** | `POST /login` với mã BHYT `CN3790000000001` / `123456` | Hệ thống tự động fallback tra cứu theo BHYT và cho phép đăng nhập | HTTP 200, đăng nhập thành công vào hệ thống | **PASS ✅** |

---

## 4. CÁC ĐIỂM SỬA LỖI & TỐI ƯU HÓA KỸ THUẬT ĐÃ HOÀN TẤT

Trong đợt nâng cấp và kiểm thử này, hệ thống đã hoàn thiện các giải pháp kỹ thuật trọng tâm:

1. **Chuẩn hóa Định dạng Thẻ BHYT 15 Ký tự:**  
   - Nâng cấp từ định dạng cũ (13 ký tự) lên đúng chuẩn Quyết định 1666/QĐ-BHXH (2 chữ cái đối tượng + 1 số mức hưởng + 2 số tỉnh + 10 số thứ tự cá nhân = 15 ký tự).
   - Cập nhật toàn bộ dữ liệu mẫu trong PostgreSQL và `database.sql`.

2. **Cơ chế Kiểm soát BHYT Chính chủ:**  
   - Bổ sung xác thực chéo tại `BhytController.java`: Khi bệnh nhân nhấn áp dụng quyền lợi BHYT, hệ thống đối soát thẻ nhập vào với mã thẻ BHYT liên kết của CCCD đang đăng nhập trong SecurityContext, triệt tiêu nguy cơ mượn thẻ gian lận viện phí.

3. **Tối ưu Prompt Local AI Ollama chống từ chối:**  
   - Thiết lập vai trò *"Trợ Lý Tiếp Đón & Phân Luồng Phòng Khám"*, tránh xưng "Bác sĩ chẩn đoán" để không kích hoạt bộ lọc Safety Refusal của `qwen2.5-coder:3b`.
   - Bổ sung tham số `num_predict: 350` và `temperature: 0.1` giúp phản hồi nhanh, cô đọng và định dạng JSON tuyệt đối chuẩn xác.

4. **Nâng cấp Xử lý Chuỗi `normalizeDeptCode`:**  
   - Tự động chuẩn hóa các biến thể mã khoa (ví dụ: `TIM_MACH` -> `KTIM`, `CO_XUONG_KHOP` -> `KXUONGKHOP`, `KHOA_HOHAP` -> `KHOHAP`), đảm bảo không bị sai lệch liên kết bảng chuyên khoa.

---

## 5. KẾT LUẬN

Hệ thống **MedAssist AI** đã vượt qua toàn diện cả **15 ca Unit Tests** và **9 ca Integration Tests**, chứng minh tính ổn định, độ an toàn y tế và tính năng bảo mật định danh người bệnh sẵn sàng cho việc nghiệm thu và vận hành thực tế.