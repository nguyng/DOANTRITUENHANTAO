-- ==============================================================================
-- CƠ SỞ DỮ LIỆU: medassist
-- HỆ QUẢN TRỊ: PostgreSQL 15+
-- MÔ TẢ: Khởi tạo các bảng và Seed Data (Khoa, Nhân viên, Bệnh nhân)
-- ==============================================================================

-- Bật extension để sinh UUID tự động
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. BẢNG DEPARTMENTS (Khoa / Phòng)
-- ==========================================
CREATE TABLE DEPARTMENTS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    building VARCHAR(50),
    floor INT,
    room_numbers VARCHAR(50),
    location_guide TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. BẢNG USERS (Tài khoản Nhân viên & Bệnh nhân)
-- ==========================================
CREATE TABLE USERS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bhyt_number VARCHAR(15) UNIQUE, -- Username đăng nhập
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    email VARCHAR(100),
    dob DATE,
    gender VARCHAR(10),
    address TEXT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PATIENT', 'NURSE', 'DOCTOR', 'ADMIN')),
    department_id UUID REFERENCES DEPARTMENTS(id),
    avatar_url VARCHAR(255),
    
    -- Thông tin BHYT (Chỉ dành cho Bệnh nhân)
    bhyt_object_code VARCHAR(2),
    bhyt_object_name VARCHAR(50),
    bhyt_discount_rate DECIMAL(3,2), -- VD: 0.80, 0.95, 1.00
    bhyt_expiry_date DATE,
    bhyt_issued_date DATE,
    
    created_by UUID REFERENCES USERS(id), -- Admin tạo
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. BẢNG QUEUE_TICKETS (Phiếu khám / Số thứ tự)
-- ==========================================
CREATE TABLE QUEUE_TICKETS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES USERS(id),
    department_id UUID NOT NULL REFERENCES DEPARTMENTS(id),
    ticket_number INT NOT NULL,
    status VARCHAR(20) DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'CALLED', 'DONE', 'CANCELLED', 'TRANSFERRED')),
    severity VARCHAR(20) DEFAULT 'NORMAL' CHECK (severity IN ('NORMAL', 'MEDIUM', 'EMERGENCY')),
    called_at TIMESTAMP,
    done_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 4. BẢNG VISIT_RECORDS (Lịch sử khám / Bệnh án)
-- ==========================================
CREATE TABLE VISIT_RECORDS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES USERS(id),
    department_id UUID NOT NULL REFERENCES DEPARTMENTS(id),
    doctor_id UUID REFERENCES USERS(id),
    ticket_id UUID REFERENCES QUEUE_TICKETS(id),
    visit_name VARCHAR(200) NOT NULL,
    original_cost DECIMAL(12,2) DEFAULT 0,
    insurance_paid DECIMAL(12,2) DEFAULT 0,
    final_cost DECIMAL(12,2) DEFAULT 0,
    diagnosis TEXT,
    visit_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. BẢNG MEDICAL_SERVICES (Dịch vụ Y tế / Cận lâm sàng)
-- ==========================================
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
    visit_id UUID NOT NULL REFERENCES VISIT_RECORDS(id),
    service_id UUID NOT NULL REFERENCES MEDICAL_SERVICES(id),
    patient_co_pay DECIMAL(12,2) DEFAULT 0,
    bhyt_pay DECIMAL(12,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 6. BẢNG MEDICINES (Thuốc)
-- ==========================================
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
    visit_id UUID NOT NULL REFERENCES VISIT_RECORDS(id),
    medicine_id UUID NOT NULL REFERENCES MEDICINES(id),
    dosage VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    patient_co_pay DECIMAL(12,2) DEFAULT 0,
    bhyt_pay DECIMAL(12,2) DEFAULT 0,
    instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 7. BẢNG CHAT (Chatbot AI)
-- ==========================================
CREATE TABLE CHAT_SESSIONS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES USERS(id),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP
);

CREATE TABLE CHAT_MESSAGES (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES CHAT_SESSIONS(id),
    sender_role VARCHAR(10) NOT NULL CHECK (sender_role IN ('USER', 'BOT', 'SYSTEM')),
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT' CHECK (message_type IN ('TEXT', 'IMAGE')),
    media_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE AI_ANALYSIS_LOGS (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID NOT NULL REFERENCES CHAT_MESSAGES(id),
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


-- ==============================================================================
-- DATA SEEDING (DỮ LIỆU MẪU)
-- ==============================================================================

-- 1. Insert Khoa / Phòng (DEPARTMENTS)
INSERT INTO DEPARTMENTS (id, name, code, description, building, floor, room_numbers, location_guide) VALUES
('d1111111-1111-1111-1111-111111111111', 'Khoa Cấp Cứu', 'KCAPCUU', 'Tiếp nhận bệnh nhân cấp cứu 24/7', 'Tòa nhà A', 1, '101-105', 'Ngay cổng chính bệnh viện, làn xe cấp cứu rẽ phải.'),
('d2222222-2222-2222-2222-222222222222', 'Khoa Sản - Phụ Khoa', 'KSAN', 'Khám thai, phụ khoa, sinh đẻ', 'Tòa nhà D', 2, '201-210', 'Từ sảnh chính Tòa A, đi sang Tòa D (Khu Sản), lên tầng 2 bằng thang cuốn.'),
('d3333333-3333-3333-3333-333333333333', 'Khoa Tim Mạch', 'KTIM', 'Khám bệnh lý tim mạch, huyết áp', 'Tòa nhà B', 3, '301-308', 'Từ sảnh chính Tòa A, đi theo hành lang kính sang Tòa B, lên Tầng 3.'),
('d4444444-4444-4444-4444-444444444444', 'Khoa Tai Mũi Họng', 'KTMH', 'Khám các bệnh lý tai mũi họng', 'Tòa nhà B', 2, '210-215', 'Sang Tòa nhà B, đi thang máy số 4 lên Tầng 2, đi thẳng 5m.'),
('d5555555-5555-5555-5555-555555555555', 'Khoa Nhi', 'KNHI', 'Khám bệnh trẻ em dưới 16 tuổi', 'Tòa nhà C', 1, '101-115', 'Vừa qua cổng chính bệnh viện, Tòa nhà C (Khoa Nhi) nằm ngay bên tay phải.'),
('d6666666-6666-6666-6666-666666666666', 'Khoa Thần Kinh', 'KTHANKINH', 'Khám đau đầu, mất ngủ, đột quỵ', 'Tòa nhà A', 4, '401-410', 'Đi thang máy số 1 hoặc 2 tại Tòa A lên tầng 4, rẽ phải.'),
('d7777777-7777-7777-7777-777777777777', 'Khoa Mắt', 'KMAT', 'Khám các bệnh lý về mắt, đo khúc xạ', 'Tòa nhà B', 2, '201-205', 'Tại Tòa B, tầng 2, ngay cạnh khu vực cầu thang bộ.'),
('d8888888-8888-8888-8888-888888888888', 'Khoa Hô Hấp', 'KHOHAP', 'Khám hen phế quản, hô hấp', 'Tòa nhà A', 3, '310-315', 'Lên tầng 3 Tòa A bằng thang máy, rẽ trái đi đến cuối hành lang.'),
('d9999999-9999-9999-9999-999999999999', 'Khoa Tiêu Hoá', 'KTIEUHOA', 'Khám dạ dày, đại tràng', 'Tòa nhà A', 3, '305-309', 'Từ sảnh chính Tòa A, lên Tầng 3 bằng thang máy số 1, rẽ phải.'),
('daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Khoa Da Liễu', 'KDALIEU', 'Khám các bệnh về da, dị ứng', 'Tòa nhà C', 2, '201-205', 'Tòa nhà C, tầng 2, phía trên Khoa Nhi.'),
('dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Khoa Xương Khớp', 'KXUONGKHOP', 'Khám cơ xương khớp, gout', 'Tòa nhà B', 4, '401-408', 'Từ Tòa B, lên tầng 4 bằng thang máy số 3, đối diện phòng siêu âm khớp.');

-- 2. Insert Tài khoản Nhân viên (Mật khẩu mặc định: Admin@123 => bcrypt format: $2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e)
INSERT INTO USERS (id, bhyt_number, password_hash, full_name, role, department_id, email, phone) VALUES
(uuid_generate_v4(), 'ADMIN_001', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Super Admin', 'ADMIN', NULL, 'admin@medassist.vn', '0901234567'),

-- Khoa Cấp Cứu
(uuid_generate_v4(), 'BS_MINH', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Nguyễn Nhật Minh', 'DOCTOR', 'd1111111-1111-1111-1111-111111111111', 'bs.minh@medassist.vn', '0911111111'),
(uuid_generate_v4(), 'YTA_NGA', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Lê Thu Nga', 'NURSE', 'd1111111-1111-1111-1111-111111111111', 'yta.nga@medassist.vn', '0922222222'),

-- Khoa Sản - Phụ Khoa
(uuid_generate_v4(), 'BS_SAN', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Trần Thị Hạnh', 'DOCTOR', 'd2222222-2222-2222-2222-222222222222', 'bs.hanh@medassist.vn', '0933333333'),
(uuid_generate_v4(), 'YTA_SAN', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Nguyễn Mai', 'NURSE', 'd2222222-2222-2222-2222-222222222222', 'yta.mai@medassist.vn', '0944444444'),

-- Khoa Tim Mạch
(uuid_generate_v4(), 'BS_TIMMACH', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Lê Trọng Tài', 'DOCTOR', 'd3333333-3333-3333-3333-333333333333', 'bs.tai@medassist.vn', '0955555555'),
(uuid_generate_v4(), 'YTA_TIMMACH', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Phan Hồng', 'NURSE', 'd3333333-3333-3333-3333-333333333333', 'yta.hong@medassist.vn', '0966666666'),

-- Khoa Tai Mũi Họng
(uuid_generate_v4(), 'BS_TMH', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Đinh Văn Sang', 'DOCTOR', 'd4444444-4444-4444-4444-444444444444', 'bs.sang@medassist.vn', '0977777777'),
(uuid_generate_v4(), 'YTA_TMH', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Vũ Thủy', 'NURSE', 'd4444444-4444-4444-4444-444444444444', 'yta.thuy@medassist.vn', '0988888888'),

-- Khoa Nhi
(uuid_generate_v4(), 'BS_NHI', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Hoàng Thanh Trúc', 'DOCTOR', 'd5555555-5555-5555-5555-555555555555', 'bs.truc@medassist.vn', '0999999999'),
(uuid_generate_v4(), 'YTA_NHI', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Lâm Thảo', 'NURSE', 'd5555555-5555-5555-5555-555555555555', 'yta.thao@medassist.vn', '0910101010'),

-- Khoa Thần Kinh
(uuid_generate_v4(), 'BS_THANKINH', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Trịnh Gia Minh', 'DOCTOR', 'd6666666-6666-6666-6666-666666666666', 'bs.giaminh@medassist.vn', '0920202020'),
(uuid_generate_v4(), 'YTA_THANKINH', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Cao An', 'NURSE', 'd6666666-6666-6666-6666-666666666666', 'yta.an@medassist.vn', '0930303030'),

-- Khoa Mắt
(uuid_generate_v4(), 'BS_MAT', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Nguyễn Quang Nhãn', 'DOCTOR', 'd7777777-7777-7777-7777-777777777777', 'bs.nhan@medassist.vn', '0940404040'),
(uuid_generate_v4(), 'YTA_MAT', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Lê Thu', 'NURSE', 'd7777777-7777-7777-7777-777777777777', 'yta.thu@medassist.vn', '0950505050'),

-- Khoa Hô Hấp
(uuid_generate_v4(), 'BS_HOHAP', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Phạm Tuấn', 'DOCTOR', 'd8888888-8888-8888-8888-888888888888', 'bs.tuan@medassist.vn', '0960606060'),
(uuid_generate_v4(), 'YTA_HOHAP', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Đào Sen', 'NURSE', 'd8888888-8888-8888-8888-888888888888', 'yta.sen@medassist.vn', '0970707070'),

-- Khoa Tiêu Hoá
(uuid_generate_v4(), 'BS_TIEUHOA', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Ngô Bích', 'DOCTOR', 'd9999999-9999-9999-9999-999999999999', 'bs.bich@medassist.vn', '0980808080'),
(uuid_generate_v4(), 'YTA_TIEUHOA', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Trần Trà', 'NURSE', 'd9999999-9999-9999-9999-999999999999', 'yta.tra@medassist.vn', '0990909090'),

-- Khoa Da Liễu
(uuid_generate_v4(), 'BS_DALIEU', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Đinh Công', 'DOCTOR', 'daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bs.cong@medassist.vn', '0901010101'),
(uuid_generate_v4(), 'YTA_DALIEU', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Lý Linh', 'NURSE', 'daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'yta.linh@medassist.vn', '0912121212'),

-- Khoa Xương Khớp
(uuid_generate_v4(), 'BS_XUONGKHOP', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'BS. Phan Mạnh', 'DOCTOR', 'dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'bs.manh@medassist.vn', '0923232323'),
(uuid_generate_v4(), 'YTA_XUONGKHOP', '$2a$10$R9h/cIPz0gi.URNNX3rubedAKEQ.3p4jKjN5m7a82y5F64mI2Fv6e', 'Y tá Ngô Trâm', 'NURSE', 'dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'yta.tram@medassist.vn', '0934343434');

-- 3. Insert Bệnh nhân mẫu (Mật khẩu mặc định: 123456 => bcrypt format: $2a$10$E1b2vFh3O0r5sK3K1pQ.8.B/L6G7/C6m0O6O8rM1eB3jV1jD6q/qK)
INSERT INTO USERS (id, bhyt_number, password_hash, full_name, role, dob, gender, bhyt_object_code, bhyt_object_name, bhyt_discount_rate, bhyt_expiry_date) VALUES
(uuid_generate_v4(), 'HN47900000001', '$2a$10$E1b2vFh3O0r5sK3K1pQ.8.B/L6G7/C6m0O6O8rM1eB3jV1jD6q/qK', 'Nguyễn Văn Nghèo', 'PATIENT', '1985-05-15', 'Nam', 'HN', 'Hộ nghèo', 1.00, '2028-12-31'),
(uuid_generate_v4(), 'CN37900000001', '$2a$10$E1b2vFh3O0r5sK3K1pQ.8.B/L6G7/C6m0O6O8rM1eB3jV1jD6q/qK', 'Phạm Thị Cận', 'PATIENT', '1990-08-20', 'Nữ', 'CN', 'Cận nghèo', 0.95, '2027-12-31'),
(uuid_generate_v4(), 'TE17900000001', '$2a$10$E1b2vFh3O0r5sK3K1pQ.8.B/L6G7/C6m0O6O8rM1eB3jV1jD6q/qK', 'Trần Bé Nam', 'PATIENT', '2020-01-10', 'Nam', 'TE', 'Trẻ em dưới 6 tuổi', 1.00, '2028-06-30'),
(uuid_generate_v4(), 'HS47900000001', '$2a$10$E1b2vFh3O0r5sK3K1pQ.8.B/L6G7/C6m0O6O8rM1eB3jV1jD6q/qK', 'Nguyễn Học Sinh', 'PATIENT', '2010-09-05', 'Nam', 'HS', 'Học sinh/Sinh viên', 0.80, '2025-08-31'),
(uuid_generate_v4(), 'DN47900000001', '$2a$10$E1b2vFh3O0r5sK3K1pQ.8.B/L6G7/C6m0O6O8rM1eB3jV1jD6q/qK', 'Nguyễn Thị Lan', 'PATIENT', '1992-11-25', 'Nữ', 'DN', 'Người lao động', 0.80, '2026-12-31'),
(uuid_generate_v4(), 'DN47900099001', '$2a$10$E1b2vFh3O0r5sK3K1pQ.8.B/L6G7/C6m0O6O8rM1eB3jV1jD6q/qK', 'Test Thẻ Hết Hạn', 'PATIENT', '1988-02-14', 'Nam', 'DN', 'Người lao động', 0.80, '2023-01-01');

-- 4. Insert Dịch vụ & Thuốc đa dạng hơn
INSERT INTO MEDICAL_SERVICES (name, price, is_bhyt_covered, bhyt_price_limit) VALUES
('Khám lâm sàng chung', 150000, TRUE, 150000),
('Khám dịch vụ yêu cầu Bác sĩ chuyên khoa II', 500000, FALSE, 0),
('Khám cấp cứu ngoài giờ', 300000, TRUE, 200000),
('Xét nghiệm máu cơ bản (Sinh hóa)', 250000, TRUE, 200000),
('Xét nghiệm nước tiểu 10 thông số', 80000, TRUE, 80000),
('Xét nghiệm đường huyết mao mạch', 50000, TRUE, 50000),
('Chụp X-Quang phổi thẳng', 300000, TRUE, 250000),
('Chụp X-Quang xương khớp', 350000, TRUE, 300000),
('Siêu âm ổ bụng tổng quát', 200000, TRUE, 150000),
('Siêu âm tim Doppler màu', 450000, TRUE, 350000),
('Điện tâm đồ (ECG)', 120000, TRUE, 120000),
('Nội soi dạ dày tá tràng không gây mê', 600000, TRUE, 500000),
('Nội soi đại tràng gây mê', 1500000, TRUE, 1000000),
('Chụp cắt lớp vi tính (CT Scanner) sọ não', 1800000, TRUE, 1500000),
('Chụp cộng hưởng từ (MRI) cột sống', 2500000, TRUE, 2000000),
('Khám Tai Mũi Họng qua nội soi', 250000, TRUE, 200000),
('Đo chức năng hô hấp', 180000, TRUE, 150000);

INSERT INTO MEDICINES (name, price, is_bhyt_covered, bhyt_price_limit) VALUES
('Paracetamol 500mg (Giảm đau, hạ sốt)', 2000, TRUE, 2000),
('Ibuprofen 400mg (Kháng viêm, giảm đau)', 3500, TRUE, 3500),
('Diclofenac 50mg (Giảm đau xương khớp)', 4000, TRUE, 4000),
('Amoxicillin 500mg (Kháng sinh phổ rộng)', 5000, TRUE, 5000),
('Azithromycin 250mg (Kháng sinh)', 8000, TRUE, 8000),
('Omeprazole 20mg (Trị viêm loét dạ dày)', 8000, TRUE, 6000),
('Amlodipine 5mg (Thuốc huyết áp)', 3000, TRUE, 3000),
('Losartan 50mg (Thuốc huyết áp)', 4500, TRUE, 4500),
('Metformin 500mg (Thuốc tiểu đường)', 2500, TRUE, 2500),
('Atorvastatin 20mg (Thuốc hạ mỡ máu)', 5500, TRUE, 5000),
('Salbutamol 2mg (Thuốc giãn phế quản/Hen)', 3000, TRUE, 3000),
('Clorpheniramin 4mg (Thuốc chống dị ứng)', 1500, TRUE, 1500),
('Men tiêu hóa Enterogermina (Ống)', 12000, FALSE, 0),
('Siro ho Prospan 100ml', 85000, FALSE, 0),
('Vitamin C Sủi 1000mg', 4000, FALSE, 0),
('Vitamin 3B (B1, B6, B12)', 2500, TRUE, 2000),
('Nước muối sinh lý Natri Clorid 0.9% 500ml', 15000, TRUE, 10000),
('Oresol (Bù nước, điện giải)', 3500, TRUE, 3500),
('Povidine 10% 20ml (Sát trùng ngoài da)', 18000, FALSE, 0),
('Băng gạc y tế vô trùng', 5000, TRUE, 3000);
