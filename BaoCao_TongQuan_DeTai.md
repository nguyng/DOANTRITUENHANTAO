
TRƯỜNG ĐẠI HỌC [TÊN TRƯỜNG]
KHOA CÔNG NGHỆ THÔNG TIN
-----------

BÁO CÁO ĐỒ ÁN MÔN HỌC
TRÍ TUỆ NHÂN TẠO

Tên đề tài:
HỆ THỐNG TRỢ LÝ ẢO HỖ TRỢ TIẾP ĐÓN VÀ PHÂN LUỒNG BỆNH NHÂN
ỨNG DỤNG TRÍ TUỆ NHÂN TẠO

Nhóm thực hiện:    [Tên Nhóm / Mã Nhóm]
Thành viên 1:      [Họ và Tên] – MSSV: [Mã số sinh viên]
Thành viên 2:      [Họ và Tên] – MSSV: [Mã số sinh viên]
Giảng viên hướng dẫn: [Họ và Tên Giảng Viên]

Năm học: 2025 – 2026

═══════════════════════════════════════════════════════════

CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI

1.1. Giới thiệu & Đặt vấn đề

Trong bối cảnh hệ thống y tế Việt Nam đang chịu áp lực quá tải ngày càng gia tăng,
khâu tiếp đón bệnh nhân tại các bệnh viện và phòng khám luôn là điểm nghẽn hàng đầu.
Nhân viên lễ tân phải đồng thời xử lý hàng trăm lượt bệnh nhân mỗi ngày, trả lời các
câu hỏi lặp đi lặp lại về quy trình khám, giải thích bệnh nhân cần đến khám tại khoa
nào, và nhập liệu thủ công thông tin từ thẻ Bảo hiểm Y tế (BHYT).

Thực trạng đó dẫn đến nhiều hệ quả tiêu cực:
  - Bệnh nhân phải chờ đợi lâu, gây bức xúc và giảm sự hài lòng.
  - Nhân viên y tế bị phân tán bởi các công việc hành chính đơn giản, không
    tập trung được cho nghiệp vụ chuyên môn.
  - Sai sót trong nhập liệu thông tin BHYT thủ công, gây khó khăn trong thanh
    toán và lưu trữ hồ sơ bệnh án.

Chính vì vậy, nhóm chúng em đề xuất và triển khai đề tài: "Hệ thống Trợ lý ảo hỗ
trợ tiếp đón và phân luồng bệnh nhân ứng dụng Trí tuệ nhân tạo" nhằm giải quyết
triệt để các vấn đề trên thông qua việc ứng dụng các kỹ thuật Deep Learning hiện đại.

───────────────────────────────────────────────────────────

1.2. Mục tiêu của đề tài (Objectives)

Đề tài đặt ra bốn mục tiêu cụ thể như sau:

Mục tiêu 1 – Tự động hóa khâu tiếp đón:
  Xây dựng một Trợ lý ảo (Chatbot AI) thông minh, hoạt động liên tục 24/7, có khả
  năng tiếp nhận và tương tác với bệnh nhân qua kênh chat ngay từ bước đầu tiên,
  trước khi bệnh nhân đến trực tiếp tại cơ sở y tế.

Mục tiêu 2 – Phân luồng lâm sàng thông minh 2 giai đoạn:
  Ứng dụng Mô hình Ngôn ngữ Lớn Cục bộ (Local LLM via Ollama qwen2.5-coder:3b)
  kết hợp quy trình Phân luồng Lâm sàng 2 Giai đoạn (Two-Phase Differential Inquiry):
  - Giai đoạn 1: Chủ động hỏi sâu các triệu chứng mấu chốt để phân biệt bệnh lý.
  - Giai đoạn 2: Chẩn đoán phân biệt và chốt chuyên khoa kèm mức độ ưu tiên (M1-M5).
  - Tích hợp Bẫy Cấp cứu (Red Flag Bypass) tự động ngắt AI khi có nguy cơ tử vong.

Mục tiêu 3 – Tự động hóa xác thực và kiểm soát BHYT chính chủ:
  Xây dựng module xác thực thẻ BHYT 15 ký tự (chuẩn Quyết định 1666/QĐ-BHXH) qua 3 lớp
  (Định dạng -> CSDL Nội bộ -> Cổng giả lập BHXH), đồng thời đối soát tính toàn vẹn
  với CCCD 12 số của tài khoản đang đăng nhập nhằm ngăn chặn triệt để gian lận viện phí.

Mục tiêu 4 – Giải đáp câu hỏi thường gặp (FAQ) tự động:
  Xây dựng bộ phản hồi tự động cho các câu hỏi phổ biến về lịch làm việc, quy trình
  khám chữa bệnh, giấy tờ cần mang theo, chi phí khám... giúp giảm tải đáng kể cho
  đội ngũ nhân viên tiếp đón.

───────────────────────────────────────────────────────────

1.3. Đối tượng hướng đến (Target Audience)

Hệ thống được thiết kế để phục vụ ba nhóm đối tượng chính:

Đối tượng 1 – Bệnh nhân và Người nhà bệnh nhân:
  Đây là nhóm đối tượng sử dụng trực tiếp hệ thống. Hệ thống đặc biệt hướng đến
  những người có nhu cầu khám bệnh nhưng còn bỡ ngỡ với quy trình bệnh viện,
  không biết mình nên khám tại chuyên khoa nào với các triệu chứng đang có, hoặc
  người cao tuổi, người ở vùng ngoại tỉnh lần đầu tiếp cận hệ thống y tế hiện đại.

Đối tượng 2 – Nhân viên Y tế tại quầy tiếp đón (Lễ tân / Điều dưỡng):
  Hệ thống đóng vai trò như một "trợ lý đắc lực", tự động phân loại và điều hướng
  các trường hợp đơn giản, chỉ chuyển những trường hợp phức tạp cần phán đoán
  của con người đến nhân viên. Từ đó giúp nhân viên y tế giảm tải, nâng cao năng
  suất và tập trung vào công việc chuyên môn.

Đối tượng 3 – Cơ sở Y tế (Bệnh viện / Phòng khám):
  Với tư cách là đơn vị triển khai hệ thống, các cơ sở y tế sẽ được hưởng lợi từ
  việc nâng cao trải nghiệm bệnh nhân, giảm thời gian chờ đợi trung bình tại quầy,
  chuẩn hóa dữ liệu đầu vào, và từng bước hướng tới mô hình Bệnh viện Thông minh
  (Smart Hospital) hiện đại.

───────────────────────────────────────────────────────────

1.4. Phạm vi của đề tài (Scope)

Để đảm bảo tính khả thi trong khuôn khổ đồ án môn học, nhóm xác định rõ các
giới hạn (Constraints) của hệ thống như sau:

Giới hạn về chức năng:
  - Hệ thống CHỈ đóng vai trò là "Trợ lý điều hướng và hỗ trợ hành chính".
  - Hệ thống KHÔNG thực hiện chẩn đoán bệnh lý chuyên sâu về mặt y khoa.
  - Hệ thống KHÔNG kê đơn thuốc hoặc đưa ra liệu trình điều trị thay cho Bác sĩ.
  - Mọi kết quả gợi ý của hệ thống chỉ mang tính chất tham khảo và điều hướng,
    quyết định y khoa cuối cùng luôn thuộc về đội ngũ bác sĩ chuyên môn.

Giới hạn về phân luồng chuyên khoa:
  Hệ thống phân loại và điều hướng bệnh nhân vào 11 nhóm chuyên khoa chuẩn của bệnh viện:
    1. Khoa Cấp Cứu (KCAPCUU)       7. Khoa Mắt (KMAT)
    2. Khoa Tim Mạch (KTIM)         8. Khoa Tai Mũi Họng (KTMH)
    3. Khoa Hô Hấp (KHOHAP)         9. Khoa Thần Kinh (KTHANKINH)
    4. Khoa Tiêu Hóa (KTIEUHOA)    10. Khoa Nhi (KNHI)
    5. Khoa Cơ Xương Khớp (KXUONGKHOP) 11. Khoa Phụ Sản (KSAN)
    6. Khoa Da Liễu (KDALIEU)

Giới hạn về phạm vi nghiệp vụ và định danh:
  - Hệ thống sử dụng số Căn cước công dân (CCCD) 12 số làm mã định danh duy nhất cho
    bệnh nhân, đồng thời liên kết chặt chẽ với mã thẻ BHYT 15 ký tự để giảm trừ chi phí.
  - Toàn bộ quá trình suy luận AI diễn ra cục bộ (Local AI), không gửi dữ liệu ra cloud.

Giới hạn về ngôn ngữ:
  Hệ thống được xây dựng và tối ưu hóa hoàn toàn cho tiếng Việt. Không hỗ trợ
  ngôn ngữ khác trong phiên bản hiện tại.

───────────────────────────────────────────────────────────

1.5. Công nghệ AI sử dụng (AI Technologies Applied)

Đề tài ứng dụng kỹ thuật Deep Learning (Học sâu) – một nhánh nâng cao của
Machine Learning – thông qua việc kết hợp sức mạnh của hai lĩnh vực AI hiện đại:

┌─────────────────────────────────────────────────────────────┐
│              Kiến trúc Công nghệ AI của Hệ thống            │
├─────────────────────┬───────────────────────────────────────┤
│ Lĩnh vực            │ Mô hình / Công nghệ                   │
├─────────────────────┼───────────────────────────────────────┤
│ Local LLM Engine    │ Ollama qwen2.5-coder:3b               │
│ (Trí tuệ nhân tạo)  │ – Phân luồng lâm sàng 2 giai đoạn     │
│                     │ – Xuất kết quả JSON có cấu trúc       │
├─────────────────────┼───────────────────────────────────────┤
│ An toàn & Định tuyến│ Red Flag Emergency Bypass Rule        │
│ khẩn cấp            │ – Phát hiện từ khóa sinh tồn cực đoan │
│                     │ – Định tuyến thẳng Cấp Cứu M1        │
├─────────────────────┼───────────────────────────────────────┤
│ Khớp định danh      │ 3-Layer BHYT Verification Engine      │
│ & Chống gian lận    │ – Đối soát BHYT 15 số khớp CCCD 12 số │
└─────────────────────┴───────────────────────────────────────┘

A. Mô hình Ngôn ngữ Lớn Cục bộ (Local LLM) – Sử dụng Ollama qwen2.5-coder:3b:

  Thay vì phụ thuộc vào các API đám mây có nguy cơ lộ lọt dữ liệu y tế của bệnh nhân
  hoặc các mô hình NLP cũ chỉ phân loại tĩnh 1 lượt, hệ thống ứng dụng mô hình
  qwen2.5-coder:3b được tối ưu hóa chạy cục bộ trên hạ tầng bệnh viện qua Ollama.

  Quy trình Phân luồng Lâm sàng 2 Giai đoạn (Two-Phase Differential Inquiry):
    - Giai đoạn 1 (SYSTEM_PROMPT_PROBE): Khi bệnh nhân mô tả triệu chứng chung chung,
      mô hình đóng vai trò Trợ lý tiếp đón phòng khám, chủ động đặt câu hỏi thăm dò
      các triệu chứng mấu chốt (tính chất đau, hướng lan, sốt, thời gian khởi phát).
    - Giai đoạn 2 (SYSTEM_PROMPT_DECISION): Khi có dữ liệu lâm sàng làm rõ, mô hình
      thực hiện chẩn đoán phân biệt, xác định chính xác 1 trong 11 Chuyên khoa kèm mức độ
      ưu tiên M1–M5, độ tin cậy và tóm tắt lời khuyên dưới dạng JSON chuẩn hóa.

  Ưu điểm vượt trội:
    - Bảo mật tuyệt đối: Toàn bộ dữ liệu bệnh án không rời khỏi mạng nội bộ.
    - Khả năng suy luận ngữ cảnh nhiều lượt (multi-turn context understanding).
    - Tốc độ phản hồi cực nhanh (< 1.5 giây với tham số num_predict: 350).
    - Tránh được các ca phân luồng sai lệch do triệu chứng ban đầu gây nhầm lẫn.

B. Bẫy Cấp cứu (Red Flag Bypass) & Chống gian lận BHYT:

  - Red Flag Emergency Bypass: Các tình huống nguy kịch (bất tỉnh, ngừng thở, co giật,
    nôn ra máu, đau ngực vã mồ hôi lạnh) được nhận diện bằng bộ quy tắc ưu tiên cao nhất,
    tự động bypass toàn bộ AI để cấp số khám Cấp cứu M1 ngay lập tức.
  - Xác thực BHYT Chính chủ: Kiểm tra thẻ BHYT 15 ký tự đối soát tự động với số CCCD
    của tài khoản bệnh nhân, ngăn chặn triệt để hành vi mượn thẻ BHYT trái phép.

───────────────────────────────────────────────────────────

1.6. Tóm tắt kiến trúc hệ thống

Luồng xử lý tổng quát của hệ thống như sau:

  Bệnh nhân gửi tin nhắn/ảnh
           │
           ▼
  [WebSocket Server – Real-time Chat]
           │
           ├── Tin nhắn văn bản ──► [PhoBERT Model]
           │                              │
           │                    ┌─────────┴──────────┐
           │                    ▼                    ▼
           │              Phân loại              Nhận diện
           │              Chuyên khoa            ý định
           │              (Text Clf)             (Intent)
           │                    │                    │
           │                    └────────┬───────────┘
           │                             ▼
           │                     Trả lời thông minh
           │
           └── Ảnh thẻ BHYT ──► [YOLOv8 Detection]
                                         │
                                         ▼
                                 [VietOCR – Đọc chữ]
                                         │
                                         ▼
                                 Tự động điền form

═══════════════════════════════════════════════════════════

(Hết Chương 1 – Tổng quan Đề tài)
