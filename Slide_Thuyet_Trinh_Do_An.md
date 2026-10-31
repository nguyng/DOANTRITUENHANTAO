---
marp: true
theme: default
paginate: true
header: 'Báo cáo Đề tài Trí tuệ Nhân tạo'
footer: 'Hệ thống Trợ lý ảo tiếp đón và phân luồng bệnh viện'
---

# 🤖 Báo cáo Định hướng Đồ án TTNT
## Đề tài: Hệ thống Trợ lý ảo hỗ trợ tiếp đón và phân luồng bệnh nhân
**Nhóm thực hiện:** Nhóm nghiên cứu & phát triển (2 thành viên)
**Công nghệ:** Spring Boot 4.1.1 (Java 21) + Local LLM Ollama `qwen2.5-coder:3b` + PostgreSQL 18

---

# 📋 Nội dung trình bày

**Phần 1: Nhận thức & Đặt vấn đề**
1. Thực trạng quá tải khâu tiếp đón tại Bệnh viện
2. Vấn đề phân luồng sai lệch do triệu chứng tương đồng
3. Giới thiệu Đề tài & Mục tiêu giải pháp

**Phần 2: Kiến trúc Hệ thống & Đột phá Kỹ thuật**
4. Đăng nhập định danh CCCD 12 số & Kiểm soát BHYT chính chủ
5. Mô hình Local AI (Ollama) & Bảo mật dữ liệu y tế
6. Quy trình Phân luồng Lâm sàng 2 Giai đoạn (Two-Phase Differential Inquiry)
7. Bẫy Cấp cứu (Red Flag Emergency Bypass)

**Phần 3: Kết quả Kiểm thử & Trình diễn**
8. Kết quả Unit Tests (15/15 PASS) & Integration Tests (9/9 PASS)

---

# 🎯 1. Thực trạng & Đặt vấn đề

- **Quá tải khâu tiếp đón:** Bệnh nhân ùn ứ, nhân viên y tế mất nhiều thời gian trả lời lặp đi lặp lại và nhập liệu thủ công.
- **Thách thức triệu chứng tương đồng:** 
  - Nhiều bệnh lý khác nhau có cùng biểu hiện ban đầu (VD: Đau ngực có thể do *Nhồi máu cơ tim*, *Viêm phổi màng phổi*, hoặc *Trào ngược dạ dày*).
  - Các hệ thống AI 1 lượt (Single-turn) dễ đưa ra phán đoán sai lầm nghiêm trọng nếu không hỏi thêm.
- **Gian lận BHYT:** Nguy cơ mượn thẻ BHYT của người khác để hưởng chế độ giảm giá viện phí.

---

# 💡 2. Giải pháp: MedAssist AI Hệ thống Toàn diện

- 👉 **Định danh điện tử:** Bệnh nhân đăng nhập bằng số CCCD 12 chữ số.
- 👉 **Kiểm soát thẻ BHYT chính chủ:** Thẻ BHYT 15 ký tự bắt buộc phải khớp với CCCD của tài khoản đăng nhập để được áp dụng giảm trừ viện phí.
- 👉 **Local AI (Ollama `qwen2.5-coder:3b`):** 
  - Xử lý cục bộ 100% trên máy chủ bệnh viện, không gửi dữ liệu y tế ra bên ngoài.
  - Hội thoại thông minh đa lượt (Multi-turn conversational triage).

---

# 🤝 3. Phân công Vai trò & Công nghệ

- **Thành viên 1 (Core Backend & Security):**
  - Spring Boot 4.1.1 (Java 21 LTS) + PostgreSQL 18.
  - Phân quyền Spring Security (PATIENT, NURSE, DOCTOR, ADMIN).
  - Module Hàng đợi điều phối khám, Thống kê Dashboard, Màn hình LED Sảnh.
  - 3-Layer BHYT Verification Engine & CCCD Ownership Matching.

- **Thành viên 2 (AI Engine & Clinical Logic):**
  - Tích hợp Ollama Local AI (`qwen2.5-coder:3b` qua endpoint `/api/chat`).
  - Thiết kế System Prompts 2 Giai đoạn (Probe Prompt & Decision Prompt).
  - Module Bẫy Cấp cứu (Red Flag Bypass) & Fallback quy tắc Java nội bộ.
  - Bộ kịch bản kiểm thử tích hợp tự động hóa (Python E2E Suite).

---

# 🎯 4. Đột phá 1: Phân luồng Lâm sàng 2 Giai đoạn

Nhằm hướng tới độ chính xác cao nhất, hệ thống triển khai:

```
[Bệnh nhân: "Tôi bị đau ngực"]
       │
       ▼ (Giai đoạn 1: Thăm dò triệu chứng mấu chốt)
[AI: "Cơn đau có cảm giác thế nào? Đau nhói khi hít thở, đè nặng hay nóng rát? Có lan đi đâu, kèm sốt hay vã mồ hôi không?"]
       │
       ▼ (Bệnh nhân trả lời: "Đau nhói tăng khi hít sâu, có sốt và ho đờm")
[AI: Chẩn đoán phân biệt -> Chốt KHOA HÔ HẤP (Mức M2, Confidence 92%)]
```

- **Giai đoạn 1 (Probe):** Thu thập đủ thông tin phân biệt, tuyệt đối không chốt khoa vội vàng.
- **Giai đoạn 2 (Decision):** Tổng hợp dữ kiện lâm sàng, xuất JSON cấu trúc chuẩn gồm chuyên khoa, mức ưu tiên M1–M5, độ tin cậy và tóm tắt lời khuyên.

---

# 🚨 5. Đột phá 2: Bẫy Cấp cứu (Red Flag Bypass)

- **Nguyên tắc an toàn y tế số 1:** Tính mạng người bệnh là trên hết, không để AI suy luận làm chậm trễ cấp cứu.
- **Cơ chế hoạt động:**
  - Nhận diện các dấu hiệu cực đoan: *"bất tỉnh", "hôn mê", "ngừng thở", "co giật", "nôn ra máu", "đau ngực vã mồ hôi"*.
  - **Tự động ngắt toàn bộ AI (Bypass LLM)**.
  - Cấp ngay phiếu khám vào **Khoa Cấp Cứu** với mức ưu tiên **M1 (Nguy kịch)**.
  - Đẩy thẳng số thứ tự lên TOP 1 của màn hình tiếp nhận Y tá.

---

# 💳 6. Đột phá 3: Xác thực BHYT Chính chủ Chống Gian Lận

- **Chuẩn hóa 15 ký tự:** Tuân thủ chuẩn Quyết định 1666/QĐ-BHXH (VD: `HN4790000000001`).
- **Quy trình thẩm định 3 bước:**
  1. *Kiểm tra Format:* Regex đối tượng, mức hưởng, mã tỉnh và số thứ tự.
  2. *Tra cứu CSDL:* Tìm kiếm thông tin thẻ nội bộ hoặc cổng tra cứu BHXH.
  3. *Đối soát CCCD:* So sánh thẻ BHYT nhập vào với mã thẻ BHYT gắn liền với CCCD đang đăng nhập.
- **Kết quả:** Nếu người dùng nhập thẻ BHYT của người khác -> Hệ thống trả về **HTTP 403 Forbidden** và từ chối áp dụng giảm giá.

---

# 📊 7. Danh mục 11 Chuyên khoa Hợp lệ

1. `KCAPCUU` — Khoa Cấp Cứu
2. `KTIM` — Khoa Tim Mạch
3. `KHOHAP` — Khoa Hô Hấp
4. `KTIEUHOA` — Khoa Tiêu Hóa
5. `KXUONGKHOP` — Khoa Cơ Xương Khớp
6. `KTMH` — Khoa Tai Mũi Họng
7. `KMAT` — Khoa Mắt
8. `KDALIEU` — Khoa Da Liễu
9. `KTHANKINH` — Khoa Thần Kinh
10. `KNHI` — Khoa Nhi
11. `KSAN` — Khoa Phụ Sản

---

# 🧪 8. Kết quả Kiểm thử Toàn diện

- **Unit Tests (Maven Surefire):** **15/15 PASS (100%)**
  - `BhytVerificationServiceTest`: 6 tests
  - `UserCccdAndBhytMatchingTest`: 3 tests
  - `TwoPhaseClinicalTriageTest`: 5 tests
  - `HeThongPhanLuongBenhVienApplicationTests`: 1 test
- **Integration Tests (Python E2E):** **9/9 PASS (100%)**
  - Đăng nhập CCCD, tạo Session, Chat thăm dò Giai đoạn 1, Phán đoán Giai đoạn 2, Red Flag Bypass, Chặn gian lận BHYT 403, Áp dụng BHYT chính chủ 200, Đăng nhập tương thích ngược.

---

# ❓ Q&A

**Cảm ơn Thầy và các bạn đã lắng nghe!**

*Nhóm sẵn sàng trả lời các câu hỏi và trình diễn thực tế hệ thống.*