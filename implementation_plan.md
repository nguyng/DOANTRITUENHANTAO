# Kiến trúc và Công nghệ Đề xuất (Mức độ Hoàn thiện Cao nhất)

Để hệ thống Chat/Tin tức đạt chuẩn chuyên nghiệp, hoạt động mượt mà thời gian thực và không bị nghẽn (bottleneck) khi AI xử lý, dưới đây là **Tech Stack tối ưu nhất** dành cho nhóm 2 người trong 3 tháng. Bộ công nghệ này cân bằng giữa tính hiện đại, hiệu suất cao và khả năng hoàn thành đồ án.

> [!TIP]
> **100% MIỄN PHÍ VÀ MÃ NGUỒN MỞ (FOSS)**
> Tất cả các công nghệ được liệt kê dưới đây (React, Spring Boot, PostgreSQL, Redis, FastAPI, Docker, PhoBERT, YOLOv8) đều là phần mềm mã nguồn mở và **hoàn toàn miễn phí**. Bạn có thể phát triển trên máy cá nhân không tốn một đồng nào. Khi cần báo cáo đồ án, bạn cũng có thể deploy lên các nền tảng có gói Free cho sinh viên (như Vercel cho Frontend, Render/Railway cho Backend, Supabase cho Database).

---

## 1. Frontend (Giao diện Người dùng & Admin)
- **Core:** **React.js** (khởi tạo bằng Vite) + **TypeScript**. (TypeScript giúp code chặt chẽ, bắt lỗi ngay khi gõ, rất hữu ích khi làm việc nhóm).
- **Thiết kế UI:** **Tailwind CSS** kết hợp với **Shadcn UI** (hoặc Ant Design). Các công cụ này giúp tạo ra giao diện cực kỳ hiện đại, cao cấp (glassmorphism, dark mode) một cách nhanh chóng mà không phải viết nhiều CSS thuần.
- **State Management (Quản lý trạng thái):** **Zustand** (nhẹ, cấu hình đơn giản hơn Redux rất nhiều).
- **Giao tiếp mạng:** **Axios** (gọi API thông thường) và **STOMP.js / SockJS** (duy trì kết nối WebSockets với Spring Boot).

## 2. Backend Core (Xử lý Nghiệp vụ & Real-time)
- **Framework Chính:** **Java Spring Boot 3.x**.
- **Cơ sở dữ liệu (Database):** **PostgreSQL**. Mạnh mẽ hơn MySQL trong việc xử lý văn bản phức tạp, dễ dàng mở rộng và được sử dụng rộng rãi trong các hệ thống lớn.
- **Bộ đệm (In-memory Cache):** **Redis**. *Thành phần không thể thiếu để dự án trở nên "hoàn hảo".* Redis giúp:
  - Lưu trạng thái đang online/offline của người dùng.
  - Cache các tin tức nóng để truy xuất cực nhanh.
  - Rate-limiting (Giới hạn số lần gửi tin nhắn liên tục để chống spam).
- **Bảo mật:** **Spring Security + JWT** (JSON Web Token) phân quyền Admin và User.
- **Real-time:** **Spring WebSocket**.

## 3. AI Service (Xử lý Ngôn từ & Hình ảnh)
- **Framework Chính:** **Python + FastAPI**. Cực kỳ nhanh nhẹn, sinh ra để làm API cho các mô hình AI.
- **Thư viện AI:** 
  - *Xử lý văn bản (NLP):* **HuggingFace Transformers** + PyTorch (để chạy PhoBERT).
  - *Xử lý hình ảnh (Vision):* **Ultralytics YOLOv8**.
- **Xử lý nền:** Dùng tính năng `BackgroundTasks` của FastAPI để xử lý ảnh nặng mà không làm đứng (treo) server.

## 4. Cơ chế Giao tiếp (Giải quyết bài toán thắt cổ chai)
Xử lý AI luôn có độ trễ (100ms - 1s). Nếu Spring Boot phải đứng đợi AI thì hệ thống chat sẽ bị lag. Để "hoàn thiện nhất":
- **Giao tiếp qua Message Broker (RabbitMQ hoặc chính Redis Pub/Sub).**
- **Luồng hoạt động:**
  1. Người dùng bấm gửi tin nhắn -> Spring Boot.
  2. Spring Boot đẩy tin nhắn vào Redis Queue, trả về client trạng thái *"Đang kiểm duyệt..."*.
  3. AI Service (FastAPI) liên tục đọc Redis, lấy tin nhắn ra quét qua PhoBERT/YOLOv8.
  4. Quét xong, FastAPI báo lại kết quả cho Spring Boot.
  5. Nếu `Clean`: Spring Boot lưu CSDL và phát (broadcast) cho tất cả mọi người. Nếu `Toxic`: Báo cảnh báo cho người gửi.

## 5. Môi trường Triển khai (DevOps)
- **Docker & Docker Compose:** Tuyệt chiêu để dự án trở nên cực kỳ xịn sò trong mắt giảng viên. Chỉ với 1 file cấu hình `docker-compose.yml`, bạn có thể khởi động ngay lập tức PostgreSQL, Redis, Spring Boot và FastAPI mà không cần cài đặt lắt nhắt từng phần mềm lên máy.

---

## Open Questions

> [!CAUTION]
> Với bộ công nghệ "Perfect" này, hệ thống sẽ rất mạnh mẽ nhưng đòi hỏi nhóm phải sử dụng thêm **Redis** và **Docker** (nếu chưa biết sẽ mất khoảng vài ngày để làm quen). 
> 
> **Câu hỏi cho bạn:** Bạn có muốn chốt sử dụng toàn bộ Tech Stack này không? Nếu bạn đồng ý, tôi sẽ tạo ngay cấu trúc thư mục, khởi tạo các project và viết file `docker-compose.yml` có sẵn PostgreSQL + Redis cho bạn!

## Proposed Changes
Nếu bạn chốt, các bước khởi tạo sẽ bao gồm:
1. Tạo thư mục `frontend` (Vite + React + TS).
2. Tạo thư mục `backend-core` (Spring Boot).
3. Tạo thư mục `ai-service` (FastAPI).
4. Khởi tạo file `docker-compose.yml` ở thư mục gốc chứa DB và Redis.
