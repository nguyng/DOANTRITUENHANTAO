-- Tao bang DUTY_SCHEDULES
CREATE TABLE IF NOT EXISTS DUTY_SCHEDULES (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id UUID NOT NULL REFERENCES USERS(id) ON DELETE CASCADE,
    department_id UUID NOT NULL REFERENCES DEPARTMENTS(id) ON DELETE CASCADE,
    shift_date DATE NOT NULL,
    shift_type VARCHAR(30) NOT NULL CHECK (shift_type IN ('MORNING', 'AFTERNOON', 'NIGHT', 'FULL_DAY')),
    room_number VARCHAR(50),
    duty_role VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ON_DUTY' CHECK (status IN ('ON_DUTY', 'SCHEDULED', 'COMPLETED', 'OFF')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_duty_staff ON DUTY_SCHEDULES(staff_id);
CREATE INDEX IF NOT EXISTS idx_duty_dept ON DUTY_SCHEDULES(department_id);
CREATE INDEX IF NOT EXISTS idx_duty_date ON DUTY_SCHEDULES(shift_date);

-- Xoa du lieu ca truc cu neu co de seed lai sach se
DELETE FROM DUTY_SCHEDULES;

-- Seed ca truc cho HOM NAY (CURRENT_DATE) va cac ngay trong tuan (-2 den +4 ngay)
-- 1. Khoa Cap Cuu (d1111111-1111-1111-1111-111111111111)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000101', 'd1111111-1111-1111-1111-111111111111', CURRENT_DATE, 'FULL_DAY', 'P.101 - Phòng Hồi sức Cấp cứu', 'Bác sĩ trực cấp cứu trưởng ca', 'ON_DUTY', 'Trực 24/7 đón nhận bệnh nhân M1/M2'),
('b0000000-0000-0000-0000-000000000001', 'd1111111-1111-1111-1111-111111111111', CURRENT_DATE, 'MORNING',  'Quầy tiếp đón Cấp cứu', 'Điều dưỡng tiếp đón & đo sinh hiệu', 'ON_DUTY', 'Phân loại bệnh nhân khẩn cấp');

-- 2. Khoa San (d2222222-2222-2222-2222-222222222222)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000102', 'd2222222-2222-2222-2222-222222222222', CURRENT_DATE, 'MORNING', 'P.202 - Khám Thai & Phụ khoa', 'Bác sĩ khám chính', 'ON_DUTY', 'Khám thai định kỳ và siêu âm'),
('b0000000-0000-0000-0000-000000000002', 'd2222222-2222-2222-2222-222222222222', CURRENT_DATE, 'MORNING', 'Bàn tiếp đón Khoa Sản', 'Nữ hộ sinh điều phối', 'ON_DUTY', 'Chuẩn bị hồ sơ sản phụ');

-- 3. Khoa Tim Mach (d3333333-3333-3333-3333-333333333333)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000103', 'd3333333-3333-3333-3333-333333333333', CURRENT_DATE, 'MORNING', 'P.301 - Phòng Khám Tim Mạch', 'Bác sĩ chuyên khoa Tim Mạch', 'ON_DUTY', 'Đo điện tim ECG và tư vấn mạch vành'),
('b0000000-0000-0000-0000-000000000003', 'd3333333-3333-3333-3333-333333333333', CURRENT_DATE, 'MORNING', 'Bàn tiếp đón Tim Mạch', 'Y tá đo huyết áp & SpO2', 'ON_DUTY', 'Đo huyết áp trước khi vào khám');

-- 4. Khoa Tai Mui Hong (d4444444-4444-4444-4444-444444444444)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000104', 'd4444444-4444-4444-4444-444444444444', CURRENT_DATE, 'MORNING', 'P.401 - Phòng Nội soi TMH', 'Bác sĩ khám chính', 'ON_DUTY', 'Nội soi tai mũi họng'),
('b0000000-0000-0000-0000-000000000004', 'd4444444-4444-4444-4444-444444444444', CURRENT_DATE, 'MORNING', 'Bàn tiếp nhận TMH', 'Y tá phụ nội soi', 'ON_DUTY', 'Khử khuẩn dụng cụ nội soi');

-- 5. Khoa Nhi (d5555555-5555-5555-5555-555555555555)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000105', 'd5555555-5555-5555-5555-555555555555', CURRENT_DATE, 'MORNING', 'P.205 - Phòng Khám Nhi', 'Bác sĩ Nhi khoa', 'ON_DUTY', 'Khám tổng quát và dinh dưỡng trẻ em'),
('b0000000-0000-0000-0000-000000000005', 'd5555555-5555-5555-5555-555555555555', CURRENT_DATE, 'MORNING', 'Khu vực đón tiếp Nhi', 'Y tá đo nhiệt độ & cân nặng', 'ON_DUTY', 'Đo cân nặng chiều cao trẻ');

-- 6. Khoa Than Kinh (d6666666-6666-6666-6666-666666666666)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000106', 'd6666666-6666-6666-6666-666666666666', CURRENT_DATE, 'MORNING', 'P.501 - Phòng Khám Thần Kinh', 'Bác sĩ khám chính', 'ON_DUTY', 'Khám tiền đình và đau nửa đầu'),
('b0000000-0000-0000-0000-000000000006', 'd6666666-6666-6666-6666-666666666666', CURRENT_DATE, 'MORNING', 'Bàn tiếp nhận Thần Kinh', 'Y tá hướng dẫn', 'ON_DUTY', 'Hướng dẫn bệnh nhân chụp CT/MRI');

-- 7. Khoa Mat (d7777777-7777-7777-7777-777777777777)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000107', 'd7777777-7777-7777-7777-777777777777', CURRENT_DATE, 'MORNING', 'P.405 - Phòng Khám Mắt', 'Bác sĩ khám chính', 'ON_DUTY', 'Đo thị lực và soi đáy mắt'),
('b0000000-0000-0000-0000-000000000007', 'd7777777-7777-7777-7777-777777777777', CURRENT_DATE, 'MORNING', 'Bàn tiếp đón Mắt', 'Y tá khúc xạ', 'ON_DUTY', 'Kiểm tra thị lực sơ bộ');

-- 8. Khoa Ho Hap (d8888888-8888-8888-8888-888888888888)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000108', 'd8888888-8888-8888-8888-888888888888', CURRENT_DATE, 'MORNING', 'P.303 - Phòng Khám Hô Hấp', 'Bác sĩ chuyên khoa Hô Hấp', 'ON_DUTY', 'Khám viêm phế quản và đo chức năng hô hấp'),
('b0000000-0000-0000-0000-000000000008', 'd8888888-8888-8888-8888-888888888888', CURRENT_DATE, 'MORNING', 'Bàn tiếp đón Hô Hấp', 'Y tá phụ trách buồng khí dung', 'ON_DUTY', 'Đo SpO2 và lấy đờm xét nghiệm');

-- 9. Khoa Tieu Hoa (d9999999-9999-9999-9999-999999999999)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000109', 'd9999999-9999-9999-9999-999999999999', CURRENT_DATE, 'MORNING', 'P.305 - Phòng Khám Tiêu Hóa', 'Bác sĩ chuyên khoa Tiêu Hóa', 'ON_DUTY', 'Khám dạ dày tá tràng và đại tràng'),
('b0000000-0000-0000-0000-000000000009', 'd9999999-9999-9999-9999-999999999999', CURRENT_DATE, 'MORNING', 'Bàn tiếp nhận Tiêu Hóa', 'Y tá điều dưỡng', 'ON_DUTY', 'Hướng dẫn nhịn ăn xét nghiệm máu/nội soi');

-- 10. Khoa Da Lieu (daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000110', 'daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', CURRENT_DATE, 'MORNING', 'P.505 - Phòng Khám Da Liễu', 'Bác sĩ Da Liễu', 'ON_DUTY', 'Khám dị ứng và bệnh ngoài da'),
('b0000000-0000-0000-0000-000000000010', 'daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', CURRENT_DATE, 'MORNING', 'Bàn tiếp nhận Da Liễu', 'Y tá chăm sóc da', 'ON_DUTY', 'Phát số thứ tự soi da');

-- 11. Khoa Xuong Khop (dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb)
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes) VALUES
('b0000000-0000-0000-0000-000000000111', 'dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', CURRENT_DATE, 'MORNING', 'P.105 - Phòng Khám Cơ Xương Khớp', 'Bác sĩ Xương Khớp', 'ON_DUTY', 'Khám thoái hóa khớp và cột sống'),
('b0000000-0000-0000-0000-000000000011', 'dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', CURRENT_DATE, 'MORNING', 'Bàn tiếp đón Xương Khớp', 'Y tá hướng dẫn nạng & xe lăn', 'ON_DUTY', 'Hỗ trợ bệnh nhân khó đi lại');

-- Seed them ca Chieu cho ngay mai va cac ngay toi de tao lich bieu trong tuan day du
INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes)
SELECT staff_id, department_id, CURRENT_DATE + INTERVAL '1 DAY', 'AFTERNOON', room_number, duty_role, 'SCHEDULED', 'Ca làm việc chiều ngày mai'
FROM DUTY_SCHEDULES WHERE shift_date = CURRENT_DATE;

INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes)
SELECT staff_id, department_id, CURRENT_DATE + INTERVAL '2 DAY', 'MORNING', room_number, duty_role, 'SCHEDULED', 'Ca làm việc sáng ngày kia'
FROM DUTY_SCHEDULES WHERE shift_date = CURRENT_DATE;

INSERT INTO DUTY_SCHEDULES (staff_id, department_id, shift_date, shift_type, room_number, duty_role, status, notes)
SELECT staff_id, department_id, CURRENT_DATE - INTERVAL '1 DAY', 'MORNING', room_number, duty_role, 'COMPLETED', 'Ca làm việc hôm qua'
FROM DUTY_SCHEDULES WHERE shift_date = CURRENT_DATE;
