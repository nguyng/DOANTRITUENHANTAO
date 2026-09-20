
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

Mục tiêu 2 – Phân luồng thông minh theo triệu chứng:
  Ứng dụng mô hình Xử lý Ngôn ngữ Tự nhiên (PhoBERT) để phân tích và hiểu các
  đoạn mô tả triệu chứng bằng tiếng Việt của bệnh nhân, từ đó tự động gợi ý và
  điều hướng bệnh nhân đến đúng Chuyên khoa cần khám.
  Ví dụ: Bệnh nhân nhập "Tôi bị đau bụng quằn quại và nôn mửa liên tục" →
  Hệ thống điều hướng đến Khoa Tiêu hóa hoặc Khoa Cấp Cứu (tùy mức độ khẩn cấp).

Mục tiêu 3 – Tự động hóa trích xuất thông tin thẻ BHYT:
  Ứng dụng Thị giác Máy tính (YOLOv8 + OCR) để tự động nhận diện và đọc thông
  tin từ ảnh chụp thẻ Bảo hiểm Y tế do bệnh nhân gửi vào đoạn chat, thay thế hoàn
  toàn thao tác nhập liệu thủ công tại quầy tiếp đón.

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
  Hệ thống phân loại và điều hướng bệnh nhân vào 12 nhóm chuyên khoa phổ biến:
    1. Khoa Cấp Cứu              7. Khoa Thần Kinh
    2. Khoa Tim Mạch             8. Khoa Tai Mũi Họng
    3. Khoa Tiêu Hóa             9. Khoa Mắt
    4. Khoa Xương Khớp          10. Khoa Da Liễu
    5. Khoa Hô Hấp              11. Khoa Nhi
    6. Khoa Sản – Phụ Khoa      12. Khoa Nội Tổng Quát (mặc định)

Giới hạn về xử lý hình ảnh:
  Module nhận dạng và trích xuất thông tin chỉ được tối ưu hóa cho mẫu thẻ Bảo
  hiểm Y tế (BHYT) theo chuẩn Việt Nam hiện hành. Việc mở rộng sang CCCD hoặc
  các loại giấy tờ tùy thân khác có thể được cân nhắc trong phiên bản nâng cao sau.

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
│ NLP (Xử lý ngôn     │ PhoBERT (Fine-tuned)                  │
│ ngữ tự nhiên)       │ – Text Classification                 │
│                     │ – Intent Recognition                  │
├─────────────────────┼───────────────────────────────────────┤
│ Computer Vision     │ YOLOv8 – Object Detection             │
│ (Thị giác máy tính) │ VietOCR – Optical Character           │
│                     │ Recognition (OCR)                     │
└─────────────────────┴───────────────────────────────────────┘

A. Xử lý Ngôn ngữ Tự nhiên (NLP) – Sử dụng PhoBERT:

  PhoBERT là mô hình ngôn ngữ lớn (Large Language Model) được phát triển bởi
  nhóm nghiên cứu VinAI Research, dựa trên kiến trúc BERT (Bidirectional Encoder
  Representations from Transformers) của Google và được huấn luyện chuyên biệt
  trên kho ngữ liệu tiếng Việt khổng lồ (20GB dữ liệu văn bản thuần Việt).

  Nhóm sử dụng kỹ thuật Fine-tuning (tinh chỉnh) PhoBERT trên tập dữ liệu
  triệu chứng y tế tiếng Việt của nhóm tự xây dựng (~18.000 mẫu) để thực hiện
  hai nhiệm vụ cốt lõi:

  Nhiệm vụ 1 – Phân loại văn bản (Text Classification):
    Đầu vào: Đoạn tin nhắn mô tả triệu chứng của bệnh nhân (ngôn ngữ tự nhiên).
    Đầu ra: Nhãn Chuyên khoa tương ứng (VD: "Khoa Tim Mạch", "Khoa Nhi"...).

  Nhiệm vụ 2 – Nhận diện ý định (Intent Recognition):
    Phân biệt bệnh nhân đang "kể triệu chứng" (SYMPTOM) hay đang "hỏi thủ tục"
    (FAQ) để hệ thống đưa ra luồng phản hồi phù hợp.

  Ưu điểm của PhoBERT so với ML truyền thống:
    - Hiểu được ngữ cảnh hai chiều của câu văn (Bidirectional context).
    - Xử lý tốt các từ đồng âm, đồng nghĩa trong tiếng Việt.
    - Không yêu cầu feature engineering thủ công như SVM, Naive Bayes.
    - Đã được pre-trained sẵn, chỉ cần fine-tune với ít dữ liệu là đạt độ
      chính xác cao.

B. Thị giác Máy tính (Computer Vision) – Sử dụng YOLOv8 & VietOCR:

  Module này gồm hai bước xử lý liên tiếp:

  Bước 1 – Phát hiện đối tượng với YOLOv8 (You Only Look Once version 8):
    YOLOv8 là mô hình Object Detection (Phát hiện vật thể) thế hệ mới nhất của
    Ultralytics, nổi tiếng với tốc độ suy luận (inference) cực nhanh và độ chính
    xác cao. Trong hệ thống này, YOLOv8 được huấn luyện để:
      - Phát hiện và định vị thẻ BHYT trong ảnh bệnh nhân gửi vào chat.
      - Khoanh vùng (Bounding Box) chính xác các trường thông tin quan trọng
        trên thẻ: Mã số BHYT, Họ và tên, Ngày sinh, Nơi đăng ký KCB ban đầu.

  Bước 2 – Nhận dạng ký tự quang học với VietOCR:
    Sau khi YOLOv8 cắt ra các vùng ảnh chứa thông tin, VietOCR (một thư viện
    OCR được tối ưu cho chữ Việt có dấu) sẽ đọc và chuyển đổi các vùng ảnh đó
    thành chuỗi văn bản (Text) có thể xử lý bằng máy tính.
    Ví dụ: [Ảnh chứa mã số thẻ] → "DN4010123456789"
    Kết quả được tự động điền vào form đăng ký khám của hệ thống.

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
