# DANH SACH CAC LOAI SO DO CAN VE

# Du an: Tro ly ao ho tro tiep don va phan luong benh nhan ung dung AI

---

## TONG QUAN TRANG THAI

| #  | Ten so do                               | Nhom          | Uu tien   | Trang thai |
| -- | --------------------------------------- | ------------- | --------- | ---------- |
| 1  | BFD - Bieu do phan ra chuc nang         | Yeu cau       | BAT BUOC  | Chua lam   |
| 2  | Use Case Diagram                        | Yeu cau       | BAT BUOC  | Chua lam   |
| 3  | Use Case Description (Bang mo ta)       | Yeu cau       | BAT BUOC  | Chua lam   |
| 4  | DFD Muc 0 - Context Diagram             | Luong du lieu | BAT BUOC  | Chua lam   |
| 5  | DFD Muc 1 - Chi tiet luong du lieu      | Luong du lieu | BAT BUOC  | Chua lam   |
| 6  | ERD - Bieu do thuc the quan he          | Co so du lieu | BAT BUOC  | Chua lam   |
| 7  | Luoc do quan he (Relational Schema)     | Co so du lieu | BAT BUOC  | Chua lam   |
| 8  | Activity Diagram - Bieu do hoat dong    | Hanh vi       | BAT BUOC  | Chua lam   |
| 9  | Sequence Diagram - Bieu do tuan tu      | Hanh vi       | BAT BUOC  | Chua lam   |
| 10 | Class Diagram - Bieu do lop             | Cau truc      | NEN CO    | Chua lam   |
| 11 | State Diagram - Bieu do trang thai      | Hanh vi       | NEN CO    | Chua lam   |
| 12 | System Architecture Diagram             | Kien truc     | BAT BUOC  | Chua lam   |
| 13 | Component Diagram - Bieu do thanh phan  | Cau truc      | NEN CO    | Chua lam   |
| 14 | Deployment Diagram - Bieu do trien khai | Kien truc     | NEN CO    | Chua lam   |
| 15 | Sitemap / Navigation Flow               | Giao dien     | NEN CO    | Chua lam   |
| 16 | Wireframe / UI Mockup                   | Giao dien     | THEM DIEM | Chua lam   |

---

## NHOM 1 - SO DO YEU CAU VA CHUC NANG

### 1. BFD - Bieu do phan ra chuc nang

(Business Function Diagram / Functional Decomposition Diagram)

- **Muc dich:** Phan tich toan bo chuc nang he thong tu tong quat den chi tiet, dang cay phan cap. Tra loi cau hoi "He thong lam duoc nhung gi?"
- **Cong cu:** Draw.io, Microsoft Visio
- **Ghi chu cho du an:** Phan ra thanh 3 cap: He thong > Phan he > Chuc nang cu the

```
Cau truc BFD du kien:

HE THONG MEDASSIST
|
+-- 1. Quan ly Nguoi dung
|   +-- 1.1. Dang nhap bang Ma BHYT
|   +-- 1.2. Doi mat khau
|   +-- 1.3. Xem thong tin ca nhan
|   +-- 1.4. Admin cap tai khoan benh nhan
|
+-- 2. Chat AI - Tro ly ao
|   +-- 2.1. Phan tich trieu chung (PhoBERT)
|   +-- 2.2. Tra loi cau hoi FAQ
|   +-- 2.3. OCR nhan dien the BHYT (YOLOv8)
|   +-- 2.4. Phat hien muc do khan cap
|
+-- 3. Quan ly Hang doi
|   +-- 3.1. Boc so thu tu
|   +-- 3.2. Goi so tiep theo (Y ta)
|   +-- 3.3. Chuyen khoa
|   +-- 3.4. Huy phieu kham
|
+-- 4. Lich su Kham benh
|   +-- 4.1. Xem danh sach lan kham
|   +-- 4.2. Xem chi tiet lan kham
|   +-- 4.3. Xem don thuoc
|
+-- 5. Quan tri he thong (Admin)
    +-- 5.1. Quan ly Khoa/Phong
    +-- 5.2. Quan ly nhan vien
    +-- 5.3. Cau hinh cau FAQ
    +-- 5.4. Thong ke & bao cao
    +-- 5.5. Xem Logs he thong
```

---

### 2. Use Case Diagram - Bieu do truong hop su dung

- **Muc dich:** Mo ta tac nhan (Actor) va cac chuc nang ho co the thuc hien trong he thong.
- **Cong cu:** Draw.io, StarUML, PlantUML
- **Ghi chu cho du an:** 4 Actor chinh

```
Actor va Use Case:
- Benh nhan:        UC01 -> UC11 (Dang nhap, Chat AI, Boc so, Xem lich su kham...)
- Y ta / Le tan:    UC12 -> UC15 (Xem hang doi, Goi so, Chuyen khoa, Tra cuu)
- Admin:            UC16 -> UC21 (Cap tai khoan, Quan ly khoa, Bao cao...)
- Man hinh hanh lang: UC22 -> UC23 (Hien thi STT, Text-to-Speech)
```

---

### 3. Use Case Description - Bang mo ta Use Case

- **Muc dich:** Mo ta chi tiet tung Use Case: tac nhan, tien dieu kien, luong chinh, luong thay the, hau dieu kien.
- **Cong cu:** Word, Markdown
- **Ghi chu:** Uu tien mo ta UC06 (Chat AI), UC07 (OCR BHYT), UC13 (Goi so)

```
Mau bang mo ta:
+----------------+---------------------------------------------+
| Truong         | Noi dung                                    |
+----------------+---------------------------------------------+
| Use Case ID    | UC-06                                       |
| Ten UC         | Chat voi Tro ly ao AI                       |
| Tac nhan       | Benh nhan                                   |
| Tien dieu kien | Benh nhan da dang nhap                      |
| Luong chinh    | 1. BN nhap trieu chung                      |
|                | 2. He thong goi PhoBERT phan tich           |
|                | 3. Tra ket qua khoa + do tin cay            |
|                | 4. BN xac nhan                              |
|                | 5. Tao phieu kham, cap STT                  |
| Luong thay the | Confidence < 70%: hoi them trieu chung     |
| Hau dieu kien  | Phieu kham duoc tao, STT duoc cap           |
+----------------+---------------------------------------------+
```

---

## NHOM 2 - SO DO LUONG DU LIEU (DFD)

### 4. DFD Muc 0 - Context Diagram (Bieu do ngu canh)

- **Muc dich:** Nhin tong quan toan bo he thong nhu mot hop den. Chi the hien trao doi du lieu voi ben ngoai.
- **Cong cu:** Draw.io
- **Ghi chu:** 1 hinh tron duy nhat (He thong MedAssist) + 4 thuc the ngoai

```
Thuc the ngoai:
[Benh nhan]          --trieu chung, anh BHYT--> (MEDASSIST) --ket qua, STT--> [Benh nhan]
[Y ta / Le tan]      --goi so, tra cuu-------->             --hang doi-----> [Y ta]
[Admin]              --quan ly tai khoan------->             --bao cao------> [Admin]
[Man hinh hanh lang] <--STT hien tai, ten goi--
```

---

### 5. DFD Muc 1 - Chi tiet luong du lieu

- **Muc dich:** Phan ra tien trinh ben trong he thong, the hien du lieu di qua tung buoc xu ly.
- **Cong cu:** Draw.io
- **Ghi chu:** Ve 4 luong rieng biet

```
Luong 1 - Phan tich trieu chung & phan luong:
  [Benh nhan] -> tin nhan -> (1.1 Phan loai Intent)
    -> SYMPTOM -> (1.2 PhoBERT phan tich) -> =AI_ANALYSIS_LOGS=
    -> (1.3 Tao phieu kham) -> =QUEUE_TICKETS= -> STT -> [Benh nhan]

Luong 2 - OCR the BHYT:
  [Benh nhan] -> anh BHYT -> (2.1 Upload MinIO)
    -> (2.2 YOLOv8 detect) -> (2.3 VietOCR doc ma)
    -> ma BHYT -> [Benh nhan]

Luong 3 - Quan ly hang doi:
  [Y ta] -> lenh goi so -> (3.1 Lay STT tiep theo) -> =QUEUE_TICKETS=
    -> (3.2 Broadcast WebSocket) -> [Man hinh hanh lang] + [Benh nhan]

Luong 4 - Quan ly benh nhan (Admin):
  [Admin] -> thong tin BN -> (4.1 Tao tai khoan) -> sinh Ma BHYT
    -> =USERS= -> ma BHYT -> [Benh nhan]
```

---

## NHOM 3 - SO DO CO SO DU LIEU

### 6. ERD - Bieu do thuc the quan he

(Entity Relationship Diagram)

- **Muc dich:** Thiet ke CSDL: cac bang, thuoc tinh, khoa chinh, khoa ngoai va quan he (1-1, 1-N, N-N).
- **Cong cu:** dbdiagram.io (khuyen dung), Draw.io
- **Ghi chu:** 8 bang chinh trong du an

```
Cac Entity va quan he:
- USERS (id[PK], bhyt_number[UNIQUE], password_hash, full_name, role, bhyt_discount_rate, bhyt_expiry_date, ...)
- DEPARTMENTS (id[PK], name, code, floor, room_numbers, ...)
- QUEUE_TICKETS (id[PK], patient_id[FK->USERS], department_id[FK->DEPARTMENTS], ticket_number, status, ...)
- VISIT_RECORDS (id[PK], patient_id[FK->USERS], department_id[FK->DEPARTMENTS], doctor_id[FK->USERS], diagnosis, original_cost, insurance_paid, final_cost, ...)
- PRESCRIPTIONS (id[PK], visit_id[FK->VISIT_RECORDS], medicine_name, dosage, quantity, unit_price, total_price, ...)
- CHAT_SESSIONS (id[PK], user_id[FK->USERS], started_at, ended_at)
- CHAT_MESSAGES (id[PK], session_id[FK->CHAT_SESSIONS], sender_role, content, message_type, ...)
- AI_ANALYSIS_LOGS (id[PK], message_id[FK->CHAT_MESSAGES], predicted_dept, confidence_score, severity, ...)

Quan he:
USERS (1) ---< QUEUE_TICKETS (N)
USERS (1) ---< VISIT_RECORDS (N)
VISIT_RECORDS (1) ---< PRESCRIPTIONS (N)
DEPARTMENTS (1) ---< QUEUE_TICKETS (N)
USERS (1) ---< CHAT_SESSIONS (N)
CHAT_SESSIONS (1) ---< CHAT_MESSAGES (N)
CHAT_MESSAGES (1) ---< AI_ANALYSIS_LOGS (1)
```

---

### 7. Luoc do quan he (Relational Schema)

- **Muc dich:** The hien ERD duoi dang ki hieu text chuan, ro rang tung truong, kieu du lieu, rang buoc.
- **Cong cu:** dbdiagram.io (xuat tu dong tu ERD)
- **Ghi chu:** Moi bang ghi ro ten cot, kieu du lieu (UUID, VARCHAR, DECIMAL, TIMESTAMP...), NOT NULL, UNIQUE, FK

---

## NHOM 4 - SO DO HANH VI (UML BEHAVIORAL)

### 8. Activity Diagram - Bieu do hoat dong

- **Muc dich:** Mo ta luong hoat dong (cac buoc) cua mot chuc nang. Tuong tu flowchart nhung co swimlane phan biet actor.
- **Cong cu:** Draw.io, PlantUML
- **Ghi chu:** Can ve 5 Activity Diagram

```
AD-01: Luong dang nhap bang Ma BHYT
AD-02: Luong Chat AI phan tich trieu chung -> boc so
AD-03: Luong OCR nhan dien the BHYT
AD-04: Luong Y ta goi so va chuyen khoa
AD-05: Luong Admin tao tai khoan benh nhan
```

---

### 9. Sequence Diagram - Bieu do tuan tu

- **Muc dich:** Mo ta su tuong tac giua cac doi tuong/thanh phan theo thu tu thoi gian.
- **Cong cu:** PlantUML, Mermaid, Draw.io
- **Ghi chu:** Can ve 3 Sequence Diagram

```
SD-01: Benh nhan gui trieu chung -> nhan STT
       [Client -> WebSocket -> Spring Boot -> FastAPI NLP -> Redis -> PostgreSQL]

SD-02: Benh nhan gui anh BHYT -> OCR tra ma so
       [Client -> Spring Boot -> MinIO -> FastAPI CV (YOLOv8 + VietOCR)]

SD-03: Y ta goi so -> thong bao WebSocket den man hinh hanh lang
       [Y ta -> Spring Boot -> Redis -> WebSocket -> Man hinh hanh lang]
```

---

### 10. State Diagram - Bieu do trang thai

- **Muc dich:** Mo ta cac trang thai cua mot doi tuong va su chuyen doi khi co su kien xay ra.
- **Cong cu:** Draw.io, PlantUML
- **Ghi chu:** Phu hop mo ta vong doi cua QUEUE_TICKET va CHAT_SESSION

```
Trang thai QUEUE_TICKET:
  [Bat dau] -> WAITING  (boc so xong)
  WAITING   -> CALLED   (y ta goi so)
  CALLED    -> DONE     (kham xong)
  CALLED    -> WAITING  (BN khong co mat, cho vao lai)
  WAITING   -> CANCELLED (benh nhan huy)
  DONE      -> [Ket thuc]
  CANCELLED -> [Ket thuc]

Trang thai CHAT_SESSION:
  [Bat dau] -> ACTIVE    (bat dau chat)
  ACTIVE    -> COMPLETED (da dang ky kham thanh cong)
  ACTIVE    -> ABANDONED (bo giua chung / timeout)
  COMPLETED -> [Ket thuc]
  ABANDONED -> [Ket thuc]
```

---

## NHOM 5 - SO DO CAU TRUC (UML STRUCTURAL)

### 11. Class Diagram - Bieu do lop

- **Muc dich:** Mo ta cac lop (class) trong he thong, thuoc tinh, phuong thuc va quan he ke thua/ket hop. Anh xa truc tiep sang code.
- **Cong cu:** Draw.io, StarUML, PlantUML
- **Ghi chu:** Ve rieng cho Backend (Java) va AI Service (Python)

```
Backend Java Spring Boot:
  Entity:     User, Department, QueueTicket, VisitRecord, Prescription, ChatSession, ChatMessage, AiAnalysisLog
  Repository: UserRepository, QueueTicketRepository, ...
  Service:    AuthService, ChatService, QueueService, AiGatewayService, ...
  Controller: AuthController, ChatController, QueueController, AdminController, ...

AI Service Python:
  NLPService:   analyze_symptom(), classify_intent(), detect_severity()
  CVService:    detect_bhyt_card(), extract_text_ocr()
  PhoBERTModel, YOLOv8Model, VietOCREngine
```

---

### 12. Component Diagram - Bieu do thanh phan

- **Muc dich:** Mo ta cac thanh phan doc lap cua he thong va cach chung giao tiep qua interface.
- **Cong cu:** Draw.io
- **Cac thanh phan chinh:**

```
[React Frontend]        --HTTP REST / WebSocket-->  [Spring Boot Backend]
[Spring Boot Backend]   --REST API-->               [FastAPI NLP Service]
[Spring Boot Backend]   --REST API-->               [FastAPI CV Service]
[Spring Boot Backend]   --JDBC-->                   [PostgreSQL]
[Spring Boot Backend]   --Redis Client-->            [Redis]
[FastAPI CV Service]    --S3 API-->                  [MinIO]
```

---

## NHOM 6 - SO DO KIEN TRUC & TRIEN KHAI

### 13. System Architecture Diagram - So do kien truc he thong

- **Muc dich:** The hien tong quan kien truc 3-tier, cach cac tang ket noi, giao thuc truyen thong.
- **Cong cu:** Draw.io
- **Tang kien truc:** CLIENT LAYER -> APPLICATION LAYER (Spring Boot) -> AI SERVICE LAYER (FastAPI) -> DATA LAYER

---

### 14. Deployment Diagram - Bieu do trien khai

- **Muc dich:** Mo ta server/container nao chay service nao, port nao duoc mo.
- **Cong cu:** Draw.io
- **Docker Compose containers:**

```
Container: postgres:15-alpine   -> port 5432
Container: redis:7-alpine       -> port 6379
Container: minio/minio          -> port 9000
Container: spring-boot-app      -> port 8080
Container: fastapi-nlp          -> port 8001
Container: fastapi-cv           -> port 8002
Container: nginx + react build  -> port 80 / 3000
```

---

## NHOM 7 - SO DO GIAO DIEN

### 15. Sitemap / Navigation Flow - So do dieu huong

- **Muc dich:** Liet ke tat ca man hinh (page/route) va luong dieu huong giua chung.
- **Cong cu:** Draw.io, Figma (FigJam)
- **Routes theo role:**

```
PUBLIC:   /login | /display/:deptId

PATIENT:  /chat | /ticket | /profile | /visits | /visits/:id

NURSE:    /nurse/queue | /nurse/queue/:deptId | /nurse/search

ADMIN:    /admin/dashboard | /admin/patients | /admin/departments
          /admin/staff | /admin/faq | /admin/reports | /admin/logs
```

---

### 16. Wireframe / UI Mockup - Ban phac thao giao dien

- **Muc dich:** Ban phac thao layout giao dien tung man hinh truoc khi code.
- **Cong cu:** Figma (khuyen dung), Adobe XD, Balsamiq
- **Man hinh can wireframe:**

```
WF-01: /login           -- Form dang nhap Ma BHYT + mat khau
WF-02: /chat            -- Giao dien chat AI (man hinh chinh)
WF-03: /ticket          -- Xem phieu kham & STT hien tai
WF-04: /profile         -- Thong tin ca nhan + thong tin BHYT
WF-05: /visits/:id      -- Chi tiet lan kham + don thuoc + chi phi
WF-06: /nurse/queue     -- Dashboard hang doi cua Y ta
WF-07: /display/:deptId -- Man hinh hanh lang (public display)
WF-08: /admin/patients  -- Quan ly & cap tai khoan benh nhan
```

---

## CONG CU GOI Y

| Cong cu               | Link                 | Dung cho                                                            |
| --------------------- | -------------------- | ------------------------------------------------------------------- |
| Draw.io (khuyen dung) | https://draw.io      | BFD, DFD, ERD, Activity, State, Architecture, Component, Deployment |
| dbdiagram.io          | https://dbdiagram.io | ERD + Relational Schema (co the xuat SQL truc tiep)                 |
| PlantUML              | https://plantuml.com | Sequence, Class, Use Case, State (code-based)                       |
| Mermaid Live          | https://mermaid.live | Sequence, ERD, Flowchart (inline Markdown)                          |
| Figma                 | https://figma.com    | Wireframe, UI Mockup, Sitemap                                       |
| StarUML               | https://staruml.io   | Use Case, Class, Sequence (phan mem desktop)                        |

---

## THU TU VE GOI Y THEO TUAN

```
TUAN 1 - Nen tang phan tich:
  [ ] BFD (phan ra chuc nang)
  [ ] Use Case Diagram
  [ ] Use Case Description (bang mo ta 5-7 UC quan trong nhat)

TUAN 2 - Luong du lieu & CSDL:
  [ ] DFD Muc 0 (Context Diagram)
  [ ] DFD Muc 1 (4 luong chinh)
  [ ] ERD
  [ ] Luoc do quan he (Relational Schema)

TUAN 3 - Hanh vi & Cau truc:
  [ ] Activity Diagram (5 luong)
  [ ] Sequence Diagram (3 luong)
  [ ] State Diagram (Queue Ticket + Chat Session)
  [ ] Class Diagram (Backend Java + AI Python)

TUAN 4 - Kien truc & Giao dien:
  [ ] System Architecture Diagram
  [ ] Component Diagram
  [ ] Deployment Diagram
  [ ] Sitemap / Navigation Flow
  [ ] Wireframe (8 man hinh)
```

---

Cap nhat lan cuoi: 13/09/2026
