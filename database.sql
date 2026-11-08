-- ==============================================================================
-- CƠ SỞ DỮ LIỆU: medassist / hospital_db
-- HỆ QUẢN TRỊ: PostgreSQL 15+ / 18+
-- MÔ TẢ: Schema DDL hoàn chỉnh, DROP CASCADE an toàn, và Bộ Seed Data chuẩn
--        phục vụ kiểm thử toàn diện hệ thống tiếp đón, phân luồng AI & BHYT
-- ==============================================================================

-- 0. Dọn dẹp schema cũ nếu có (đảm bảo chạy lại script không bị lỗi khóa ngoại)
DROP TABLE IF EXISTS AI_ANALYSIS_LOGS CASCADE;
DROP TABLE IF EXISTS CHAT_MESSAGES CASCADE;
DROP TABLE IF EXISTS CHAT_SESSIONS CASCADE;
DROP TABLE IF EXISTS PRESCRIPTIONS CASCADE;
DROP TABLE IF EXISTS MEDICINES CASCADE;
DROP TABLE IF EXISTS VISIT_SERVICES CASCADE;
DROP TABLE IF EXISTS MEDICAL_SERVICES CASCADE;
DROP TABLE IF EXISTS VISIT_RECORDS CASCADE;
DROP TABLE IF EXISTS QUEUE_TICKETS CASCADE;
DROP TABLE IF EXISTS FAQS CASCADE;
DROP TABLE IF EXISTS USERS CASCADE;
DROP TABLE IF EXISTS DEPARTMENTS CASCADE;

-- Bật extension để sinh UUID tự động
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- 1. BẢNG DEPARTMENTS (Khoa / Phòng khám chuyên khoa)
-- ==============================================================================
CREATE TABLE DEPARTMENTS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    building VARCHAR(50),
    floor INT,
    room_numbers VARCHAR(50),
    location_guide TEXT,
    ticket_prefix VARCHAR(10) DEFAULT 'NB',
    avg_consult_minutes INT DEFAULT 15,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_departments_code ON DEPARTMENTS(code);
CREATE INDEX idx_departments_active ON DEPARTMENTS(is_active);

-- ==============================================================================
-- 2. BẢNG USERS (Tài khoản Người dùng: Bệnh nhân, Y tá, Bác sĩ, Admin)
-- ==============================================================================
CREATE TABLE USERS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone VARCHAR(15) UNIQUE,
    cccd VARCHAR(20) UNIQUE,
    bhyt_number VARCHAR(30) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    dob DATE,
    gender VARCHAR(10),
    address TEXT,
    avatar_url VARCHAR(255),
    role VARCHAR(20) NOT NULL CHECK (role IN ('PATIENT', 'NURSE', 'DOCTOR', 'ADMIN')),
    department_id UUID REFERENCES DEPARTMENTS(id) ON DELETE SET NULL,

    -- Thông tin BHYT (dành cho bệnh nhân theo Quyết định 1666/QĐ-BHXH)
    bhyt_object_code VARCHAR(2),
    bhyt_object_name VARCHAR(50),
    bhyt_discount_rate DECIMAL(3,2),
    bhyt_expiry_date DATE,
    bhyt_issued_date DATE,

    created_by UUID REFERENCES USERS(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_phone ON USERS(phone);
CREATE INDEX idx_users_cccd ON USERS(cccd);
CREATE INDEX idx_users_bhyt ON USERS(bhyt_number);
CREATE INDEX idx_users_role ON USERS(role);

-- ==============================================================================
-- 3. BẢNG QUEUE_TICKETS (Phiếu khám / Số thứ tự hàng đợi phân luồng)
-- ==============================================================================
CREATE TABLE QUEUE_TICKETS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES USERS(id) ON DELETE CASCADE,
    department_id UUID NOT NULL REFERENCES DEPARTMENTS(id) ON DELETE RESTRICT,
    ticket_number VARCHAR(20) NOT NULL,
    priority VARCHAR(5) DEFAULT 'M3' CHECK (priority IN ('M1', 'M2', 'M3', 'M4', 'M5')),
    ai_confidence INT,
    ai_summary TEXT,
    status VARCHAR(20) DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'CALLED', 'DONE', 'CANCELLED', 'TRANSFERRED')),
    location_guide TEXT,
    transferred_to_dept_id UUID REFERENCES DEPARTMENTS(id) ON DELETE SET NULL,
    transfer_reason TEXT,
    called_by_nurse_id UUID REFERENCES USERS(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP,
    done_at TIMESTAMP
);

CREATE INDEX idx_tickets_dept_status ON QUEUE_TICKETS(department_id, status);
CREATE INDEX idx_tickets_patient ON QUEUE_TICKETS(patient_id);
CREATE INDEX idx_tickets_created ON QUEUE_TICKETS(created_at);

-- ==============================================================================
-- 4. BẢNG VISIT_RECORDS (Lịch sử lượt khám / Hồ sơ bệnh án viện phí)
-- ==============================================================================
CREATE TABLE VISIT_RECORDS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES USERS(id) ON DELETE CASCADE,
    department_id UUID NOT NULL REFERENCES DEPARTMENTS(id) ON DELETE RESTRICT,
    doctor_id UUID REFERENCES USERS(id) ON DELETE SET NULL,
    ticket_id UUID REFERENCES QUEUE_TICKETS(id) ON DELETE SET NULL,
    visit_name VARCHAR(200) NOT NULL,
    diagnosis TEXT,
    visit_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    notes TEXT,
    original_cost DECIMAL(12,2) DEFAULT 0,
    insurance_paid DECIMAL(12,2) DEFAULT 0,
    final_cost DECIMAL(12,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_visit_patient ON VISIT_RECORDS(patient_id);
CREATE INDEX idx_visit_ticket ON VISIT_RECORDS(ticket_id);
CREATE INDEX idx_visit_created ON VISIT_RECORDS(created_at);

-- ==============================================================================
-- 5. BẢNG MEDICAL_SERVICES & VISIT_SERVICES (Dịch vụ y tế & Cận lâm sàng)
-- ==============================================================================
CREATE TABLE MEDICAL_SERVICES (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    is_bhyt_covered BOOLEAN DEFAULT TRUE,
    bhyt_price_limit DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE VISIT_SERVICES (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    visit_id UUID NOT NULL REFERENCES VISIT_RECORDS(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES MEDICAL_SERVICES(id) ON DELETE RESTRICT,
    patient_co_pay DECIMAL(12,2) DEFAULT 0,
    bhyt_pay DECIMAL(12,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_visitservice_visit ON VISIT_SERVICES(visit_id);

-- ==============================================================================
-- 6. BẢNG MEDICINES & PRESCRIPTIONS (Danh mục thuốc & Đơn thuốc điều trị)
-- ==============================================================================
CREATE TABLE MEDICINES (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    is_bhyt_covered BOOLEAN DEFAULT TRUE,
    bhyt_price_limit DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE PRESCRIPTIONS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    visit_id UUID NOT NULL REFERENCES VISIT_RECORDS(id) ON DELETE CASCADE,
    medicine_id UUID NOT NULL REFERENCES MEDICINES(id) ON DELETE RESTRICT,
    dosage VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    patient_co_pay DECIMAL(12,2) DEFAULT 0,
    bhyt_pay DECIMAL(12,2) DEFAULT 0,
    instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prescription_visit ON PRESCRIPTIONS(visit_id);

-- ==============================================================================
-- 7. BẢNG CHAT_SESSIONS, CHAT_MESSAGES & AI_ANALYSIS_LOGS (Trợ lý ảo AI)
-- ==============================================================================
CREATE TABLE CHAT_SESSIONS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES USERS(id) ON DELETE CASCADE,
    ticket_id UUID REFERENCES QUEUE_TICKETS(id) ON DELETE SET NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP
);

CREATE INDEX idx_session_user ON CHAT_SESSIONS(user_id);
CREATE INDEX idx_session_started ON CHAT_SESSIONS(started_at);

CREATE TABLE CHAT_MESSAGES (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES CHAT_SESSIONS(id) ON DELETE CASCADE,
    sender_role VARCHAR(10) NOT NULL CHECK (sender_role IN ('USER', 'BOT', 'SYSTEM')),
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT' CHECK (message_type IN ('TEXT', 'IMAGE')),
    media_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_session ON CHAT_MESSAGES(session_id);
CREATE INDEX idx_message_created ON CHAT_MESSAGES(created_at);

CREATE TABLE AI_ANALYSIS_LOGS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID NOT NULL REFERENCES CHAT_MESSAGES(id) ON DELETE CASCADE,
    ai_model_used VARCHAR(50),
    input_text TEXT,
    predicted_dept VARCHAR(100),
    confidence_score DECIMAL(5,4),
    sentiment VARCHAR(20),
    is_corrected BOOLEAN DEFAULT FALSE,
    corrected_dept VARCHAR(100),
    processing_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_log_message ON AI_ANALYSIS_LOGS(message_id);
CREATE INDEX idx_log_created ON AI_ANALYSIS_LOGS(created_at);

-- ==============================================================================
-- 8. BẢNG FAQS (Câu hỏi thường gặp và giải đáp tự động)
-- ==============================================================================
CREATE TABLE FAQS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    keywords TEXT,
    category VARCHAR(30),
    department_id UUID REFERENCES DEPARTMENTS(id) ON DELETE SET NULL,
    sort_order INT DEFAULT 0,
    show_as_quick_reply BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    hit_count BIGINT DEFAULT 0,
    created_by UUID REFERENCES USERS(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_faq_active ON FAQS(is_active);
CREATE INDEX idx_faq_category ON FAQS(category);
CREATE INDEX idx_faq_dept ON FAQS(department_id);


-- ==============================================================================
-- DỮ LIỆU KHỞI TẠO (SEED DATA HOÀN CHỈNH CHO KIỂM THỬ)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Insert 11 Chuyên khoa chuẩn bệnh viện đa khoa
-- ------------------------------------------------------------------------------
INSERT INTO DEPARTMENTS (id, name, code, description, building, floor, room_numbers, location_guide, ticket_prefix, avg_consult_minutes) VALUES
('d1111111-1111-1111-1111-111111111111', 'Khoa Cấp Cứu', 'KCAPCUU', 'Tiếp nhận xử trí bệnh nhân cấp cứu nguy kịch 24/7', 'Tòa nhà A', 1, '101-105', 'Ngay cổng chính bệnh viện, làn xe cấp cứu rẽ phải.', 'CC', 10),
('d2222222-2222-2222-2222-222222222222', 'Khoa Sản - Phụ Khoa', 'KSAN', 'Khám thai, theo dõi sản khoa, phụ khoa và sinh đẻ', 'Tòa nhà D', 2, '201-210', 'Từ sảnh chính Tòa A, đi sang Tòa D (Khu Sản), lên tầng 2 bằng thang cuốn.', 'SAN', 15),
('d3333333-3333-3333-3333-333333333333', 'Khoa Tim Mạch', 'KTIM', 'Khám chẩn đoán bệnh lý tim mạch, mạch vành, tăng huyết áp', 'Tòa nhà B', 3, '301-308', 'Từ sảnh chính Tòa A, đi theo hành lang kính sang Tòa B, lên Tầng 3.', 'TM', 15),
('d4444444-4444-4444-4444-444444444444', 'Khoa Tai Mũi Họng', 'KTMH', 'Khám và nội soi các bệnh lý tai mũi họng, thanh quản', 'Tòa nhà B', 2, '210-215', 'Sang Tòa nhà B, đi thang máy số 4 lên Tầng 2, đi thẳng 5m.', 'TMH', 12),
('d5555555-5555-5555-5555-555555555555', 'Khoa Nhi', 'KNHI', 'Khám và chăm sóc sức khỏe trẻ em dưới 16 tuổi', 'Tòa nhà C', 1, '101-115', 'Vừa qua cổng chính bệnh viện, Tòa nhà C (Khoa Nhi) nằm ngay bên tay phải.', 'NHI', 15),
('d6666666-6666-6666-6666-666666666666', 'Khoa Thần Kinh', 'KTHANKINH', 'Khám đau đầu, mất ngủ mạn tính, rối loạn tiền đình, đột quỵ', 'Tòa nhà A', 4, '401-410', 'Đi thang máy số 1 hoặc 2 tại Tòa A lên tầng 4, rẽ phải.', 'TK', 20),
('d7777777-7777-7777-7777-777777777777', 'Khoa Mắt', 'KMAT', 'Khám bệnh lý nhãn khoa, đo thị lực khúc xạ, cườm mắt', 'Tòa nhà B', 2, '201-205', 'Tại Tòa B, tầng 2, ngay cạnh khu vực cầu thang bộ.', 'MAT', 12),
('d8888888-8888-8888-8888-888888888888', 'Khoa Hô Hấp', 'KHOHAP', 'Khám hen phế quản, viêm phổi, COPD, ho kéo dài', 'Tòa nhà A', 3, '310-315', 'Lên tầng 3 Tòa A bằng thang máy, rẽ trái đi đến cuối hành lang.', 'HH', 15),
('d9999999-9999-9999-9999-999999999999', 'Khoa Tiêu Hoá', 'KTIEUHOA', 'Khám dạ dày, đại tràng, trào ngược thực quản, gan mật', 'Tòa nhà A', 3, '305-309', 'Từ sảnh chính Tòa A, lên Tầng 3 bằng thang máy số 1, rẽ phải.', 'TH', 15),
('daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Khoa Da Liễu', 'KDALIEU', 'Khám bệnh ngoài da, dị ứng, viêm da cơ địa, mề đay', 'Tòa nhà C', 2, '201-205', 'Tòa nhà C, tầng 2, phía trên Khoa Nhi.', 'DL', 15),
('dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Khoa Xương Khớp', 'KXUONGKHOP', 'Khám thoái hóa khớp, cột sống, gút, đau cơ xương khớp', 'Tòa nhà B', 4, '401-408', 'Từ Tòa B, lên tầng 4 bằng thang máy số 3, đối diện phòng siêu âm khớp.', 'XK', 15);

-- ------------------------------------------------------------------------------
-- 2. Insert Tài khoản Cán bộ Quản trị, Bác sĩ & Y tá
--    • Mật khẩu ADMIN_001 : Admin@123 -> $2a$10$xVXFF2NiTowRX52LNFt/SuI0xwTgJJ3asvlXWkpbo87YSfdQubemq
--    • Mật khẩu Y tá/Bác sĩ: 123456    -> $2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG
-- ------------------------------------------------------------------------------

-- Quản trị viên hệ thống
INSERT INTO USERS (id, bhyt_number, password_hash, full_name, role, department_id, email, phone) VALUES
('a0000000-0000-0000-0000-000000000001', 'ADMIN_001', '$2a$10$xVXFF2NiTowRX52LNFt/SuI0xwTgJJ3asvlXWkpbo87YSfdQubemq', 'Super Admin', 'ADMIN', NULL, 'admin@medassist.vn', '0901234567');

-- Y tá trực hàng đợi tại các khoa (Đăng nhập bằng mã: YTA_NGA, YTA_HOHAP, v.v. / Pass: 123456)
INSERT INTO USERS (id, bhyt_number, password_hash, full_name, role, department_id, email, phone) VALUES
('b0000000-0000-0000-0000-000000000001', 'YTA_NGA',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Lê Thu Nga',       'NURSE', 'd1111111-1111-1111-1111-111111111111', 'yta.nga@medassist.vn', '0922222222'),
('b0000000-0000-0000-0000-000000000002', 'YTA_SAN',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Nguyễn Mai',       'NURSE', 'd2222222-2222-2222-2222-222222222222', 'yta.mai@medassist.vn', '0944444444'),
('b0000000-0000-0000-0000-000000000003', 'YTA_TIMMACH',   '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Phan Hồng',        'NURSE', 'd3333333-3333-3333-3333-333333333333', 'yta.hong@medassist.vn', '0966666666'),
('b0000000-0000-0000-0000-000000000004', 'YTA_TMH',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Vũ Thủy',          'NURSE', 'd4444444-4444-4444-4444-444444444444', 'yta.thuy@medassist.vn', '0988888888'),
('b0000000-0000-0000-0000-000000000005', 'YTA_NHI',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Lâm Thảo',         'NURSE', 'd5555555-5555-5555-5555-555555555555', 'yta.thao@medassist.vn', '0910101010'),
('b0000000-0000-0000-0000-000000000006', 'YTA_THANKINH',  '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Cao An',           'NURSE', 'd6666666-6666-6666-6666-666666666666', 'yta.an@medassist.vn', '0930303030'),
('b0000000-0000-0000-0000-000000000007', 'YTA_MAT',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Lê Thu',           'NURSE', 'd7777777-7777-7777-7777-777777777777', 'yta.thu@medassist.vn', '0950505050'),
('b0000000-0000-0000-0000-000000000008', 'YTA_HOHAP',     '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Đào Sen',          'NURSE', 'd8888888-8888-8888-8888-888888888888', 'yta.sen@medassist.vn', '0970707070'),
('b0000000-0000-0000-0000-000000000009', 'YTA_TIEUHOA',   '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Trần Trà',         'NURSE', 'd9999999-9999-9999-9999-999999999999', 'yta.tra@medassist.vn', '0990909090'),
('b0000000-0000-0000-0000-000000000010', 'YTA_DALIEU',    '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Lý Linh',          'NURSE', 'daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'yta.linh@medassist.vn', '0912121212'),
('b0000000-0000-0000-0000-000000000011', 'YTA_XUONGKHOP', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Y tá Ngô Trâm',         'NURSE', 'dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'yta.tram@medassist.vn', '0934343434');

-- Bác sĩ chuyên khoa khám lâm sàng (Pass: 123456)
INSERT INTO USERS (id, bhyt_number, password_hash, full_name, role, department_id, email, phone) VALUES
('b0000000-0000-0000-0000-000000000101', 'BS_MINH',      '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Nguyễn Nhật Minh',  'DOCTOR', 'd1111111-1111-1111-1111-111111111111', 'bs.minh@medassist.vn', '0911111111'),
('b0000000-0000-0000-0000-000000000102', 'BS_SAN',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Trần Thị Hạnh',     'DOCTOR', 'd2222222-2222-2222-2222-222222222222', 'bs.hanh@medassist.vn', '0933333333'),
('b0000000-0000-0000-0000-000000000103', 'BS_TIMMACH',   '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Lê Trọng Tài',      'DOCTOR', 'd3333333-3333-3333-3333-333333333333', 'bs.tai@medassist.vn', '0955555555'),
('b0000000-0000-0000-0000-000000000104', 'BS_TMH',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Đinh Văn Sang',     'DOCTOR', 'd4444444-4444-4444-4444-444444444444', 'bs.sang@medassist.vn', '0977777777'),
('b0000000-0000-0000-0000-000000000105', 'BS_NHI',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Hoàng Thanh Trúc',  'DOCTOR', 'd5555555-5555-5555-5555-555555555555', 'bs.truc@medassist.vn', '0999999999'),
('b0000000-0000-0000-0000-000000000106', 'BS_THANKINH',  '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Trịnh Gia Minh',    'DOCTOR', 'd6666666-6666-6666-6666-666666666666', 'bs.giaminh@medassist.vn', '0920202020'),
('b0000000-0000-0000-0000-000000000107', 'BS_MAT',       '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Nguyễn Quang Nhãn', 'DOCTOR', 'd7777777-7777-7777-7777-777777777777', 'bs.nhan@medassist.vn', '0940404040'),
('b0000000-0000-0000-0000-000000000108', 'BS_HOHAP',     '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Phạm Tuấn',         'DOCTOR', 'd8888888-8888-8888-8888-888888888888', 'bs.tuan@medassist.vn', '0960606060'),
('b0000000-0000-0000-0000-000000000109', 'BS_TIEUHOA',   '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Ngô Bích',          'DOCTOR', 'd9999999-9999-9999-9999-999999999999', 'bs.bich@medassist.vn', '0980808080'),
('b0000000-0000-0000-0000-000000000110', 'BS_DALIEU',    '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Đinh Công',          'DOCTOR', 'daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bs.cong@medassist.vn', '0901010101'),
('b0000000-0000-0000-0000-000000000111', 'BS_XUONGKHOP', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'BS. Phan Mạnh',         'DOCTOR', 'dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'bs.manh@medassist.vn', '0923232323');

-- ------------------------------------------------------------------------------
-- 3. Insert Danh mục Bệnh nhân mẫu kiểm thử (10 đối tượng chuẩn theo QĐ 1666)
--    • Mật khẩu tất cả bệnh nhân: 123456 -> $2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG
--    • Đăng nhập bằng CCCD (12 chữ số) hoặc số BHYT (15 ký tự)
-- ------------------------------------------------------------------------------
INSERT INTO USERS (id, cccd, bhyt_number, password_hash, full_name, role, dob, gender, phone, email, address, bhyt_object_code, bhyt_object_name, bhyt_discount_rate, bhyt_expiry_date, bhyt_issued_date) VALUES
-- 1. Bệnh nhân Hộ nghèo (HN: Hưởng 100% KCB, không giới hạn)
('c0000000-0000-0000-0000-000000000001', '079204000001', 'HN4790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Nguyễn Văn Nghèo', 'PATIENT', '1959-05-15', 'Nam', '0912000001', 'ngheo.nguyen@gmail.com', '123 Đường Hải Phòng, P. Thạch Thang, Q. Hải Châu, Đà Nẵng', 'HN', 'Hộ nghèo', 1.00, '2028-12-31', '2024-01-01'),

-- 2. Bệnh nhân Cận nghèo (CN: Hưởng 95% KCB)
('c0000000-0000-0000-0000-000000000002', '079204000002', 'CN3790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Phạm Thị Cận', 'PATIENT', '1990-08-20', 'Nữ', '0912000002', 'can.pham@gmail.com', '45 Lê Duẩn, Q. Hải Châu, Đà Nẵng', 'CN', 'Cận nghèo', 0.95, '2027-12-31', '2024-01-01'),

-- 3. Trẻ em dưới 6 tuổi (TE: Hưởng 100% KCB)
('c0000000-0000-0000-0000-000000000003', '079204000003', 'TE1790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Trần Bé Nam', 'PATIENT', '2021-01-10', 'Nam', '0912000003', 'benam.tran@gmail.com', '78 Nguyễn Thị Minh Khai, Đà Nẵng', 'TE', 'Trẻ em dưới 6 tuổi', 1.00, '2028-06-30', '2024-01-01'),

-- 4. Học sinh / Sinh viên (HS: Hưởng 80% KCB)
('c0000000-0000-0000-0000-000000000004', '079204000004', 'HS4790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Nguyễn Học Sinh', 'PATIENT', '2005-09-05', 'Nam', '0912000004', 'hocsinh@gmail.com', 'Ký túc xá Đại học Kiến Trúc Đà Nẵng', 'HS', 'Học sinh/Sinh viên', 0.80, '2026-08-31', '2024-09-01'),

-- 5. Người lao động doanh nghiệp (DN: Hưởng 80% KCB)
('c0000000-0000-0000-0000-000000000005', '079204000005', 'DN4790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Nguyễn Thị Lan', 'PATIENT', '1992-11-25', 'Nữ', '0912000005', 'lan.nguyen@fpt.com', 'Khu Công nghệ cao Đà Nẵng', 'DN', 'Người lao động', 0.80, '2026-12-31', '2024-01-01'),

-- 6. Cán bộ Hưu trí (HT: Hưởng 95% KCB)
('c0000000-0000-0000-0000-000000000006', '079204000006', 'HT2790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Bùi Hưu Trí', 'PATIENT', '1955-03-12', 'Nam', '0912000006', 'tri.bui@gmail.com', '12 Quang Trung, Đà Nẵng', 'HT', 'Hưu trí', 0.95, '2028-12-31', '2023-01-01'),

-- 7. Hộ gia đình tự nguyện (GD: Hưởng 80% KCB)
('c0000000-0000-0000-0000-000000000007', '079204000007', 'GD4790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Đinh Công Viên', 'PATIENT', '1982-07-19', 'Nam', '0912000007', 'vien.dinh@gmail.com', '34 Điện Biên Phủ, Đà Nẵng', 'GD', 'Hộ gia đình', 0.80, '2026-12-31', '2024-01-01'),

-- 8. Thân nhân sĩ quan công an/quân đội (TN: Hưởng 80% KCB)
('c0000000-0000-0000-0000-000000000008', '079204000008', 'TN4790000000001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Vũ Thân Nhân', 'PATIENT', '1987-12-01', 'Nữ', '0912000008', 'nhan.vu@gmail.com', '56 Nguyễn Văn Linh, Đà Nẵng', 'TN', 'Thân nhân sĩ quan', 0.80, '2027-12-31', '2024-01-01'),

-- 9. Bệnh nhân có thẻ BHYT HẾT HẠN (để test tính năng cảnh báo thẻ quá hạn)
('c0000000-0000-0000-0000-000000000009', '079204000009', 'DN4790009900001', '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Test Thẻ Hết Hạn', 'PATIENT', '1988-02-14', 'Nam', '0912000009', 'hethan@gmail.com', '99 Núi Thành, Đà Nẵng', 'DN', 'Người lao động', 0.80, '2023-01-01', '2022-01-01'),

-- 10. Bệnh nhân Khám Dịch vụ thuần túy (Không có thẻ BHYT -> Tự chi trả 100%)
('c0000000-0000-0000-0000-000000000010', '079204000010', NULL,               '$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG', 'Hoàng Khám Dịch Vụ', 'PATIENT', '1995-10-30', 'Nam', '0912000010', 'dichvu@gmail.com', '88 Bạch Đằng, Đà Nẵng', NULL, 'Khám Dịch Vụ', 0.00, NULL, NULL);

-- ------------------------------------------------------------------------------
-- 4. Insert Danh mục Dịch vụ Kỹ thuật & Cận lâm sàng (Có giá BHYT theo Thông tư)
-- ------------------------------------------------------------------------------
INSERT INTO MEDICAL_SERVICES (id, name, price, is_bhyt_covered, bhyt_price_limit) VALUES
('e0000000-0000-0000-0000-000000000001', 'Khám lâm sàng chuyên khoa',                150000, TRUE,  150000),
('e0000000-0000-0000-0000-000000000002', 'Khám cấp cứu ngoài giờ',                   300000, TRUE,  200000),
('e0000000-0000-0000-0000-000000000003', 'Khám theo yêu cầu Giáo sư / Tiến sĩ',      500000, FALSE, 0),
('e0000000-0000-0000-0000-000000000004', 'Tổng phân tích tế bào máu ngoại vi (24 TS)', 180000, TRUE,  150000),
('e0000000-0000-0000-0000-000000000005', 'Xét nghiệm sinh hóa máu (Glucose, Men gan)', 250000, TRUE,  200000),
('e0000000-0000-0000-0000-000000000006', 'Xét nghiệm nước tiểu 10 thông số',         80000,  TRUE,  80000),
('e0000000-0000-0000-0000-000000000007', 'Chụp X-quang tim phổi thẳng',              300000, TRUE,  250000),
('e0000000-0000-0000-0000-000000000008', 'Chụp X-quang khớp gối / cột sống',         350000, TRUE,  300000),
('e0000000-0000-0000-0000-000000000009', 'Siêu âm ổ bụng tổng quát màu',             200000, TRUE,  150000),
('e0000000-0000-0000-0000-000000000010', 'Siêu âm tim Doppler màu chuyên sâu',        450000, TRUE,  350000),
('e0000000-0000-0000-0000-000000000011', 'Điện tâm đồ (ECG) 12 chuyển đạo',          120000, TRUE,  120000),
('e0000000-0000-0000-0000-000000000012', 'Nội soi Tai Mũi Họng ống mềm',             250000, TRUE,  200000),
('e0000000-0000-0000-0000-000000000013', 'Đo chức năng hô hấp (Spirometry)',          220000, TRUE,  180000),
('e0000000-0000-0000-0000-000000000014', 'Nội soi dạ dày tá tràng không gây mê',      650000, TRUE,  500000),
('e0000000-0000-0000-0000-000000000015', 'Nội soi dạ dày gây mê không đau',          1500000, FALSE, 0),
('e0000000-0000-0000-0000-000000000016', 'Chụp cắt lớp vi tính (CT Scanner) sọ não', 1800000, TRUE,  1500000),
('e0000000-0000-0000-0000-000000000017', 'Chụp cộng hưởng từ (MRI) khớp gối',        2500000, TRUE,  2000000);

-- ------------------------------------------------------------------------------
-- 5. Insert Danh mục Thuốc thiết yếu BHYT
-- ------------------------------------------------------------------------------
INSERT INTO MEDICINES (id, name, price, is_bhyt_covered, bhyt_price_limit) VALUES
('f0000000-0000-0000-0000-000000000001', 'Paracetamol 500mg (Hạ sốt, giảm đau)',       2000,  TRUE,  2000),
('f0000000-0000-0000-0000-000000000002', 'Ibuprofen 400mg (Kháng viêm non-steroid)',   3500,  TRUE,  3500),
('f0000000-0000-0000-0000-000000000003', 'Diclofenac 50mg (Giảm đau khớp gút)',        4000,  TRUE,  4000),
('f0000000-0000-0000-0000-000000000004', 'Amoxicillin 500mg (Kháng sinh đường hô hấp)', 5000,  TRUE,  5000),
('f0000000-0000-0000-0000-000000000005', 'Azithromycin 250mg (Kháng sinh phổ rộng)',   8500,  TRUE,  8000),
('f0000000-0000-0000-0000-000000000006', 'Omeprazole 20mg (Ức chế tiết acid dạ dày)', 7500,  TRUE,  6000),
('f0000000-0000-0000-0000-000000000007', 'Amlodipine 5mg (Hạ huyết áp chẹn canxi)',    3200,  TRUE,  3000),
('f0000000-0000-0000-0000-000000000008', 'Losartan 50mg (Thuốc hạ huyết áp ARB)',      4800,  TRUE,  4500),
('f0000000-0000-0000-0000-000000000009', 'Salbutamol 2mg (Giãn phế quản cắt cơn hen)', 3000,  TRUE,  3000),
('f0000000-0000-0000-0000-000000000010', 'Clorpheniramin 4mg (Kháng histamin dị ứng)', 1500,  TRUE,  1500),
('f0000000-0000-0000-0000-000000000011', 'Men vi sinh Enterogermina (Hộp 20 ống)',    12000, FALSE, 0),
('f0000000-0000-0000-0000-000000000012', 'Siro trị ho Prospan chai 100ml',            85000, FALSE, 0),
('f0000000-0000-0000-0000-000000000013', 'Vitamin 3B (B1, B6, B12 bổ thần kinh)',     2500,  TRUE,  2000),
('f0000000-0000-0000-0000-000000000014', 'Nước muối sinh lý NaCl 0.9% 500ml',         15000, TRUE,  10000);

-- ------------------------------------------------------------------------------
-- 6. Insert Phiếu khám mẫu (QUEUE_TICKETS) để kiểm thử Dashboard Y tá & Màn hình sảnh
-- ------------------------------------------------------------------------------
INSERT INTO QUEUE_TICKETS (id, patient_id, department_id, ticket_number, priority, ai_confidence, ai_summary, status, location_guide, created_at) VALUES
-- Khoa Cấp Cứu: 1 ca đang khám, 2 ca đang chờ (trong đó có 1 ca khẩn cấp)
('90000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000006', 'd1111111-1111-1111-1111-111111111111', 'CC-004', 'M2', 95, 'Khó thở dữ dội, tiền sử huyết áp cao', 'CALLED', 'Ngay cổng chính bệnh viện, làn xe cấp cứu rẽ phải.', NOW() - INTERVAL '30 MINUTE'),
('90000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000007', 'd1111111-1111-1111-1111-111111111111', 'CC-005', 'M2', 91, 'Đau tức ngực trái, vã mồ hôi lạnh', 'WAITING', 'Ngay cổng chính bệnh viện, làn xe cấp cứu rẽ phải.', NOW() - INTERVAL '15 MINUTE'),
('90000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000008', 'd1111111-1111-1111-1111-111111111111', 'CC-006', 'M3', 88, 'Vết thương rách da cánh tay chảy máu', 'WAITING', 'Ngay cổng chính bệnh viện, làn xe cấp cứu rẽ phải.', NOW() - INTERVAL '5 MINUTE'),

-- Khoa Hô Hấp: 1 ca đang khám, 1 ca đang chờ
('90000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004', 'd8888888-8888-8888-8888-888888888888', 'HH-011', 'M3', 90, 'Ho khan nhiều về đêm, thở khò khè', 'CALLED', 'Lên tầng 3 Tòa A bằng thang máy, rẽ trái đi đến cuối hành lang.', NOW() - INTERVAL '20 MINUTE'),
('90000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000005', 'd8888888-8888-8888-8888-888888888888', 'HH-012', 'M4', 86, 'Ho có đờm trắng, sổ mũi nhẹ 2 ngày', 'WAITING', 'Lên tầng 3 Tòa A bằng thang máy, rẽ trái đi đến cuối hành lang.', NOW() - INTERVAL '10 MINUTE'),

-- Khoa Tim Mạch: 1 ca đang chờ
('90000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000002', 'd3333333-3333-3333-3333-333333333333', 'TM-008', 'M3', 92, 'Hồi hộp đánh trống ngực, mệt khi leo cầu thang', 'WAITING', 'Từ sảnh chính Tòa A, đi theo hành lang kính sang Tòa B, lên Tầng 3.', NOW() - INTERVAL '12 MINUTE');

-- Cập nhật thời gian gọi cho các vé đang khám
UPDATE QUEUE_TICKETS SET called_at = NOW() - INTERVAL '10 MINUTE', called_by_nurse_id = 'b0000000-0000-0000-0000-000000000001' WHERE id = '90000000-0000-0000-0000-000000000001';
UPDATE QUEUE_TICKETS SET called_at = NOW() - INTERVAL '8 MINUTE', called_by_nurse_id = 'b0000000-0000-0000-0000-000000000008' WHERE id = '90000000-0000-0000-0000-000000000004';

-- ------------------------------------------------------------------------------
-- 7. Insert Lịch sử khám bệnh đã hoàn tất (VISIT_RECORDS)
--    • Phục vụ kiểm thử xem Lịch sử khám (/visits) và Hồ sơ cá nhân (/profile)
-- ------------------------------------------------------------------------------

-- Lần khám trước của Nguyễn Văn Nghèo (Hưởng 100% BHYT Hộ nghèo)
INSERT INTO VISIT_RECORDS (id, patient_id, department_id, doctor_id, visit_name, diagnosis, visit_date, status, notes, original_cost, insurance_paid, final_cost, created_at) VALUES
('80000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'd8888888-8888-8888-8888-888888888888', 'b0000000-0000-0000-0000-000000000108', 'Khám Viêm phế quản cấp', 'Viêm phế quản cấp bội nhiễm, sốt nhẹ, ho đờm đục', CURRENT_DATE - INTERVAL '15 DAY', 'COMPLETED', 'Bệnh nhân tái khám sau 7 ngày nếu không đỡ ho.', 680000, 680000, 0, NOW() - INTERVAL '15 DAY');

-- Chi tiết dịch vụ khám của Nguyễn Văn Nghèo
INSERT INTO VISIT_SERVICES (visit_id, service_id, patient_co_pay, bhyt_pay) VALUES
('80000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 0, 150000),
('80000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000004', 0, 180000),
('80000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000007', 0, 300000);

-- Đơn thuốc của Nguyễn Văn Nghèo
INSERT INTO PRESCRIPTIONS (visit_id, medicine_id, dosage, quantity, unit_price, total_price, patient_co_pay, bhyt_pay, instructions) VALUES
('80000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000004', '500mg', 20, 5000, 100000, 0, 100000, 'Uống 1 viên x 2 lần/ngày sau bữa ăn sáng và tối.'),
('80000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', '500mg', 10, 2000, 20000,  0, 20000,  'Uống 1 viên khi sốt trên 38.5 độ C, cách tối thiểu 4-6 tiếng.');

-- Lần khám trước của Phạm Thị Cận (Hưởng 95% BHYT Cận nghèo -> cùng chi trả 5%)
INSERT INTO VISIT_RECORDS (id, patient_id, department_id, doctor_id, visit_name, diagnosis, visit_date, status, notes, original_cost, insurance_paid, final_cost, created_at) VALUES
('80000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'd9999999-9999-9999-9999-999999999999', 'b0000000-0000-0000-0000-000000000109', 'Khám Trào ngược dạ dày thực quản', 'Viêm loét dạ dày tá tràng thể nhẹ, trào ngược dịch mật', CURRENT_DATE - INTERVAL '30 DAY', 'COMPLETED', 'Kiêng đồ cay nóng, không nằm ngay sau khi ăn.', 950000, 902500, 47500, NOW() - INTERVAL '30 DAY');

INSERT INTO VISIT_SERVICES (visit_id, service_id, patient_co_pay, bhyt_pay) VALUES
('80000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 7500,  142500),
('80000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000014', 32500, 617500);

INSERT INTO PRESCRIPTIONS (visit_id, medicine_id, dosage, quantity, unit_price, total_price, patient_co_pay, bhyt_pay, instructions) VALUES
('80000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000006', '20mg', 28, 7500, 210000, 10500, 199500, 'Uống 1 viên trước bữa ăn sáng 30 phút trong 4 tuần.');

-- ------------------------------------------------------------------------------
-- 8. Insert Danh mục Câu hỏi thường gặp (FAQS)
-- ------------------------------------------------------------------------------
INSERT INTO FAQS (id, question, answer, keywords, category, sort_order, show_as_quick_reply, is_active) VALUES
(uuid_generate_v4(), 'Giờ làm việc và tiếp đón bệnh nhân của Bệnh viện như thế nào?',
 'Bệnh viện tiếp nhận khám chữa bệnh ngoại trú từ Thứ Hai đến Thứ Bảy: Sáng từ 07:00 - 11:30, Chiều từ 13:30 - 17:00. Riêng Khoa Cấp Cứu hoạt động 24/24 tất cả các ngày trong tuần kể cả ngày Lễ, Tết.',
 'giờ, mở cửa, làm việc, mấy giờ, thời gian', 'GENERAL', 1, TRUE, TRUE),

(uuid_generate_v4(), 'Đi khám bệnh tôi cần mang theo những giấy tờ tùy thân gì?',
 'Khi đến khám, bạn cần mang theo: (1) Bản gốc Căn cước công dân (CCCD) có gắn chip hoặc tài khoản VNeID mức 2; (2) Thẻ BHYT giấy hoặc hình ảnh thẻ BHYT trên ứng dụng VssID; (3) Giấy chuyển viện (nếu có); (4) Sổ khám bệnh và đơn thuốc cũ của các lần khám trước.',
 'giấy tờ, thủ tục, mang theo, hồ sơ, cccd, mang gì', 'DOCUMENT', 2, TRUE, TRUE),

(uuid_generate_v4(), 'Thẻ BHYT trái tuyến hoặc thông tuyến được hưởng quyền lợi thế nào?',
 'Theo Luật BHYT sửa đổi, bệnh nhân đi khám chữa bệnh tại các bệnh viện tuyến huyện và bệnh viện đa khoa trên toàn quốc được thông tuyến hưởng 100% mức quyền lợi trên thẻ BHYT mà không cần giấy chuyển tuyến. Trường hợp điều trị nội trú trái tuyến tỉnh được hưởng 100% chi phí theo phạm vi quyền lợi.',
 'bhyt, bảo hiểm, trái tuyến, thông tuyến, quyền lợi, chiết khấu', 'INSURANCE', 3, TRUE, TRUE),

(uuid_generate_v4(), 'Bệnh viện có hỗ trợ các phương thức thanh toán viện phí nào?',
 'Bệnh viện hỗ trợ đa dạng phương thức thanh toán: Tiền mặt tại quầy viện phí, Quét mã QR chuyển khoản ngân hàng (VietQR / Napas247), Quẹt thẻ ngân hàng qua máy POS (ATM/Visa/MasterCard), và Thanh toán viện phí không tiền mặt qua ứng dụng di động.',
 'thanh toán, viện phí, chuyển khoản, qr, pos, thẻ tín dụng, tiền mặt', 'PAYMENT', 4, TRUE, TRUE),

(uuid_generate_v4(), 'Làm sao để biết tôi nên khám tại Chuyên khoa nào?',
 'Bạn chỉ cần mô tả triệu chứng cụ thể vào khung trò chuyện này (ví dụ: vị trí đau, thời gian xuất hiện, triệu chứng kèm theo). Trợ lý AI MedAssist sẽ phân tích lâm sàng và hướng dẫn bạn đăng ký đúng chuyên khoa kèm hướng dẫn đường đi chi tiết.',
 'chuyên khoa, khám khoa nào, chọn khoa, tư vấn khoa, triệu chứng', 'APPOINTMENT', 5, TRUE, TRUE),

(uuid_generate_v4(), 'Trường hợp cấp cứu nguy kịch thì làm thế nào?',
 'Trong trường hợp khẩn cấp (bất tỉnh, ngừng thở, co giật, đau ngực dữ dội, vã mồ hôi, tai nạn mất máu), hệ thống sẽ lập tức kích hoạt bẫy cấp cứu M1 và hướng dẫn di chuyển thẳng vào Khoa Cấp Cứu tại cổng chính Bệnh viện để các bác sĩ cấp cứu can thiệp ngay lập tức mà không cần chờ đợi.',
 'cấp cứu, khẩn cấp, nguy kịch, ngất, ngừng thở, bất tỉnh', 'GENERAL', 6, FALSE, TRUE);

-- ==============================================================================
-- HOÀN TẤT KHỞI TẠO CƠ SỞ DỮ LIỆU
-- ==============================================================================
