/* ==========================================================================
   MedCore HMS — Mock data (giả lập, KHÔNG phải dữ liệu bệnh nhân thật)
   Sinh bằng seeded RNG nên ổn định giữa các lần tải trang.
   ========================================================================== */
(function (H) {
  'use strict';
  const R = H.rand, U = H.u;
  const NOW = new Date();
  const TODAY = U.startOfDay(NOW);

  /* ---------------- Danh mục ---------------- */
  const DEPARTMENTS = [
    { id: 'cc', name: 'Cấp cứu', icon: 'siren', color: '#EF4565', building: 'A', floor: 1, beds: 24 },
    { id: 'tm', name: 'Tim mạch', icon: 'heart-pulse', color: '#E11D48', building: 'B', floor: 3, beds: 40 },
    { id: 'th', name: 'Tiêu hóa', icon: 'activity', color: '#F59F0B', building: 'B', floor: 2, beds: 32 },
    { id: 'xk', name: 'Xương khớp', icon: 'bone', color: '#64748B', building: 'C', floor: 2, beds: 30 },
    { id: 'hh', name: 'Hô hấp', icon: 'wind', color: '#0FB5C9', building: 'B', floor: 4, beds: 36 },
    { id: 'sp', name: 'Sản – Phụ khoa', icon: 'flower-2', color: '#EC4899', building: 'D', floor: 2, beds: 38 },
    { id: 'tk', name: 'Thần kinh', icon: 'brain', color: '#7C5CFC', building: 'C', floor: 3, beds: 28 },
    { id: 'tmh', name: 'Tai Mũi Họng', icon: 'ear', color: '#14B8A6', building: 'A', floor: 3, beds: 16 },
    { id: 'mat', name: 'Mắt', icon: 'eye', color: '#3B82F6', building: 'A', floor: 4, beds: 14 },
    { id: 'dl', name: 'Da liễu', icon: 'hand', color: '#F97316', building: 'A', floor: 2, beds: 12 },
    { id: 'nhi', name: 'Nhi', icon: 'baby', color: '#12B886', building: 'D', floor: 3, beds: 44 },
    { id: 'noi', name: 'Nội tổng quát', icon: 'stethoscope', color: '#1E6FF0', building: 'B', floor: 1, beds: 48 }
  ];
  const deptById = Object.fromEntries(DEPARTMENTS.map((d) => [d.id, d]));

  const HO = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý', 'Trương', 'Đinh', 'Mai', 'Lâm'];
  const DEM_NAM = ['Văn', 'Minh', 'Đức', 'Quốc', 'Hữu', 'Thành', 'Gia', 'Hoàng', 'Anh', 'Tuấn', 'Công', 'Trọng'];
  const DEM_NU = ['Thị', 'Ngọc', 'Thu', 'Thanh', 'Minh', 'Phương', 'Bảo', 'Mỹ', 'Khánh', 'Diệu', 'Hải', 'Kim'];
  const TEN_NAM = ['An', 'Bình', 'Cường', 'Dũng', 'Đạt', 'Hải', 'Hiếu', 'Hùng', 'Khang', 'Khoa', 'Long', 'Minh', 'Nam', 'Phúc', 'Quân', 'Sơn', 'Tài', 'Thắng', 'Trí', 'Tùng', 'Việt', 'Vinh', 'Huy', 'Kiên', 'Lộc', 'Nhân'];
  const TEN_NU = ['Anh', 'Chi', 'Dung', 'Giang', 'Hà', 'Hạnh', 'Hằng', 'Hoa', 'Hương', 'Lan', 'Linh', 'Mai', 'My', 'Nga', 'Ngân', 'Nhung', 'Oanh', 'Phương', 'Quỳnh', 'Thảo', 'Trang', 'Trâm', 'Uyên', 'Vy', 'Yến', 'Tâm'];
  const STREETS = ['Lê Lợi', 'Nguyễn Huệ', 'Hai Bà Trưng', 'Trần Hưng Đạo', 'Lý Thường Kiệt', 'Điện Biên Phủ', 'Cách Mạng Tháng 8', 'Võ Văn Tần', 'Nguyễn Thị Minh Khai', 'Pasteur', 'Nam Kỳ Khởi Nghĩa', 'Phan Xích Long', 'Nguyễn Văn Linh', 'Lê Văn Sỹ'];
  const DISTRICTS = ['Quận 1', 'Quận 3', 'Quận 5', 'Quận 7', 'Quận 10', 'Bình Thạnh', 'Phú Nhuận', 'Tân Bình', 'Gò Vấp', 'TP. Thủ Đức', 'Bình Tân', 'Tân Phú'];
  const BLOOD = ['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-'];
  const ALLERGIES = ['Penicillin', 'Aspirin', 'Hải sản', 'Đậu phộng', 'Sulfonamid', 'Phấn hoa', 'Latex', 'Ibuprofen'];
  const RELATIONS = ['Vợ', 'Chồng', 'Con trai', 'Con gái', 'Mẹ', 'Bố', 'Anh trai', 'Chị gái'];

  const personName = (gender) => gender === 'Nam'
    ? `${R.pick(HO)} ${R.pick(DEM_NAM)} ${R.pick(TEN_NAM)}`
    : `${R.pick(HO)} ${R.pick(DEM_NU)} ${R.pick(TEN_NU)}`;
  const phone = () => '0' + R.pick(['903', '908', '912', '918', '935', '938', '977', '983', '987', '868', '388', '345']) + ' ' + R.int(100, 999) + ' ' + R.int(100, 999);
  const address = () => `${R.int(1, 420)} ${R.pick(STREETS)}, ${R.pick(DISTRICTS)}, TP. HCM`;
  const bhyt = () => R.pick(['DN', 'HT', 'GD', 'TE', 'HS', 'CN', 'BT']) + R.int(1, 4) + '79' + String(R.int(1e9, 9.99e9)).slice(0, 10);

  /* ---------------- Bác sĩ ---------------- */
  const DEGREES = [['BS.', 1], ['ThS.BS.', 3], ['BSCKII.', 2], ['TS.BS.', 1.4], ['PGS.TS.BS.', .6]];
  const doctors = [];
  DEPARTMENTS.forEach((d, di) => {
    const n = d.id === 'noi' || d.id === 'cc' || d.id === 'nhi' ? 3 : 2;
    for (let i = 0; i < n; i++) {
      const gender = R.chance(.55) ? 'Nam' : 'Nữ';
      const deg = R.weighted(DEGREES);
      const exp = deg === 'PGS.TS.BS.' ? R.int(20, 32) : deg === 'TS.BS.' ? R.int(14, 24) : deg === 'BS.' ? R.int(3, 8) : R.int(7, 18);
      const plain = personName(gender);
      doctors.push({
        id: 'BS-' + U.pad(doctors.length + 101, 3),
        plainName: plain,
        name: deg + ' ' + plain,
        degree: deg,
        gender,
        dept: d.id,
        specialty: d.name,
        experience: exp,
        patients: R.int(180, 1600),
        rating: Math.round((4.3 + R.next() * .7) * 10) / 10,
        reviews: R.int(40, 620),
        status: R.weighted([['online', 5], ['busy', 3], ['offline', 2]]),
        room: d.building + '.' + d.floor + U.pad(R.int(1, 12)),
        phone: phone(),
        email: U.norm(plain).split(' ').pop() + '.' + U.norm(plain).split(' ')[0] + di + i + '@medcore.vn',
        schedule: R.shuffle([1, 2, 3, 4, 5, 6]).slice(0, R.int(3, 5)).sort(),
        shift: R.pick(['Sáng (7:00 – 11:30)', 'Chiều (13:00 – 17:00)', 'Cả ngày', 'Trực đêm']),
        education: R.pick(['ĐH Y Dược TP.HCM', 'ĐH Y Hà Nội', 'ĐH Y Phạm Ngọc Thạch', 'ĐH Y Dược Huế', 'ĐH Y Dược Cần Thơ']),
        todayAppts: R.int(4, 18),
        satisfaction: R.int(88, 99)
      });
    }
  });
  const docById = Object.fromEntries(doctors.map((d) => [d.id, d]));
  const docsByDept = U.groupBy(doctors, 'dept');

  /* ---------------- Bệnh nhân ---------------- */
  const PATIENT_STATUS = [['treatment', 38], ['waiting', 18], ['observation', 12], ['emergency', 6], ['discharged', 26]];
  const DIAGNOSES = {
    cc: ['Đa chấn thương do TNGT', 'Ngộ độc thực phẩm', 'Đau ngực cấp', 'Sốc phản vệ', 'Chấn thương sọ não nhẹ'],
    tm: ['Tăng huyết áp độ II', 'Suy tim NYHA II', 'Rung nhĩ', 'Bệnh mạch vành ổn định', 'Nhồi máu cơ tim cũ'],
    th: ['Viêm dạ dày cấp', 'Trào ngược dạ dày thực quản', 'Viêm đại tràng', 'Sỏi túi mật', 'Xơ gan còn bù'],
    xk: ['Thoái hóa khớp gối', 'Thoát vị đĩa đệm L4-L5', 'Gãy xương cẳng tay', 'Viêm khớp dạng thấp', 'Gout cấp'],
    hh: ['Viêm phổi cộng đồng', 'Hen phế quản', 'COPD đợt cấp', 'Viêm phế quản cấp', 'Lao phổi'],
    sp: ['Thai 32 tuần', 'Theo dõi tiền sản giật', 'U xơ tử cung', 'Viêm phần phụ', 'Hậu sản thường'],
    tk: ['Đau nửa đầu Migraine', 'Nhồi máu não', 'Động kinh', 'Rối loạn tiền đình', 'Parkinson'],
    tmh: ['Viêm xoang mạn', 'Viêm amidan cấp', 'Viêm tai giữa', 'Polyp mũi', 'Viêm thanh quản'],
    mat: ['Đục thủy tinh thể', 'Glôcôm góc mở', 'Viêm kết mạc', 'Tật khúc xạ', 'Bệnh võng mạc ĐTĐ'],
    dl: ['Viêm da cơ địa', 'Vảy nến', 'Mày đay cấp', 'Zona thần kinh', 'Nấm da'],
    nhi: ['Sốt xuất huyết Dengue', 'Viêm tiểu phế quản', 'Tay chân miệng', 'Tiêu chảy cấp', 'Viêm phổi trẻ em'],
    noi: ['Đái tháo đường type 2', 'Rối loạn lipid máu', 'Thiếu máu thiếu sắt', 'Suy thận mạn giai đoạn 3', 'Cường giáp']
  };
  const patients = [];
  for (let i = 0; i < 128; i++) {
    const gender = R.chance(.5) ? 'Nam' : 'Nữ';
    const dept = R.weighted(DEPARTMENTS.map((d) => [d.id, d.id === 'noi' ? 3 : d.id === 'nhi' || d.id === 'tm' ? 2 : 1]));
    const age = dept === 'nhi' ? R.int(1, 15) : dept === 'sp' ? R.int(22, 42) : R.weighted([[R.int(16, 30), 2], [R.int(31, 50), 3], [R.int(51, 70), 4], [R.int(71, 92), 2]]);
    const name = personName(dept === 'sp' ? 'Nữ' : gender);
    const status = R.weighted(PATIENT_STATUS);
    const admit = U.addMinutes(U.addDays(TODAY, -R.int(0, 26)), R.int(6 * 60, 20 * 60));
    const dob = new Date(NOW.getFullYear() - age, R.int(0, 11), R.int(1, 28));
    const doc = R.pick(docsByDept[dept]);
    patients.push({
      id: 'BN-' + (240100 + i * 7 + R.int(0, 6)),
      name, gender: dept === 'sp' ? 'Nữ' : gender, age, dob,
      phone: phone(), address: address(), email: U.norm(name).replace(/\s+/g, '.') + '@gmail.com',
      dept, doctorId: doc.id,
      status, admitDate: admit,
      dischargeDate: status === 'discharged' ? U.addDays(admit, R.int(1, 7)) : null,
      blood: R.pick(BLOOD),
      allergies: R.chance(.35) ? R.shuffle(ALLERGIES).slice(0, R.int(1, 2)) : [],
      diagnosis: R.pick(DIAGNOSES[dept]),
      insurance: R.chance(.86) ? { number: bhyt(), rate: R.pick([80, 80, 80, 95, 100]), hospital: 'BV MedCore Sài Gòn', expiry: U.addDays(TODAY, R.int(-20, 400)) } : null,
      emergencyContact: { name: personName(R.chance(.5) ? 'Nam' : 'Nữ'), relation: R.pick(RELATIONS), phone: phone() },
      height: age < 15 ? R.int(80, 155) : R.int(150, 180), weight: age < 15 ? R.int(10, 45) : R.int(45, 85),
      occupation: age < 18 ? 'Học sinh' : age > 62 ? 'Hưu trí' : R.pick(['Nhân viên văn phòng', 'Kỹ sư', 'Giáo viên', 'Kinh doanh tự do', 'Công nhân', 'Nội trợ', 'Lái xe', 'Kế toán']),
      room: status === 'discharged' || status === 'waiting' ? null : deptById[dept].building + '.' + deptById[dept].floor + U.pad(R.int(1, 12)) + '-' + R.pick(['A', 'B', 'C', 'D'])
    });
  }
  patients.sort((a, b) => b.admitDate - a.admitDate);
  const patById = Object.fromEntries(patients.map((p) => [p.id, p]));

  /* ---------------- Nhân viên ---------------- */
  const STAFF_ROLES = [['Điều dưỡng', 10], ['Lễ tân', 3], ['Dược sĩ', 3], ['Kỹ thuật viên XN', 3], ['Kỹ thuật viên CĐHA', 2], ['Hộ lý', 3], ['Kế toán', 2], ['IT', 1]];
  const staff = [];
  for (let i = 0; i < 46; i++) {
    const gender = R.chance(.35) ? 'Nam' : 'Nữ';
    const role = R.weighted(STAFF_ROLES);
    const prefix = role === 'Điều dưỡng' ? 'ĐD. ' : role.startsWith('Kỹ thuật') ? 'KTV. ' : role === 'Dược sĩ' ? 'DS. ' : '';
    const n = personName(gender);
    staff.push({
      id: 'NV-' + U.pad(1001 + i * 3, 4), name: prefix + n, plainName: n, gender, role,
      dept: role === 'Dược sĩ' ? 'Khoa Dược' : role.includes('XN') ? 'Khoa Xét nghiệm' : role.includes('CĐHA') ? 'Khoa CĐHA' : role === 'Lễ tân' ? 'Tiếp đón' : role === 'Kế toán' ? 'Tài chính' : role === 'IT' ? 'Phòng CNTT' : R.pick(DEPARTMENTS).name,
      shift: R.pick(['Ca sáng', 'Ca chiều', 'Ca đêm', 'Hành chính']),
      status: R.weighted([['onduty', 6], ['off', 3], ['leave', 1]]),
      phone: phone(), joined: U.addDays(TODAY, -R.int(120, 4200)),
      email: U.norm(n).split(' ').pop() + U.pad(i, 2) + '@medcore.vn'
    });
  }

  /* ---------------- Lịch khám ---------------- */
  const APPT_TYPES = ['Khám mới', 'Tái khám', 'Tư vấn', 'Khám sức khỏe', 'Theo dõi sau mổ', 'Đọc kết quả XN'];
  const appointments = [];
  const startRange = U.addDays(U.startOfWeek(new Date(NOW.getFullYear(), NOW.getMonth(), 1)), 0);
  for (let d = 0; d < 42; d++) {
    const day = U.addDays(startRange, d);
    if (day.getDay() === 0 && R.chance(.7)) continue;
    const n = U.sameDay(day, TODAY) ? 16 : R.int(3, 9);
    for (let k = 0; k < n; k++) {
      const p = R.pick(patients);
      const doc = R.pick(docsByDept[p.dept]);
      const hour = R.weighted([[7, 2], [8, 3], [9, 3], [10, 3], [11, 1], [13, 2], [14, 3], [15, 3], [16, 2]]);
      const start = new Date(day); start.setHours(hour, R.pick([0, 15, 30, 45]), 0, 0);
      const past = start < NOW;
      const status = past
        ? R.weighted([['completed', 8], ['cancelled', 1], ['emergency', .3]])
        : R.weighted([['confirmed', 6], ['pending', 3], ['cancelled', .6], ['emergency', .4]]);
      appointments.push({
        id: 'LK-' + U.pad(5000 + appointments.length, 5),
        patientId: p.id, doctorId: doc.id, dept: p.dept,
        start, duration: R.pick([15, 20, 30, 30, 45, 60]),
        type: R.pick(APPT_TYPES), status,
        room: doc.room, note: R.chance(.4) ? R.pick(['Mang theo kết quả XN cũ', 'Nhịn ăn trước 8 tiếng', 'Bệnh nhân cao tuổi, cần hỗ trợ xe lăn', 'Tái khám sau 2 tuần dùng thuốc', 'Có BHYT tuyến tỉnh']) : ''
      });
    }
  }
  appointments.sort((a, b) => a.start - b.start);

  /* ---------------- Phòng bệnh ---------------- */
  const rooms = [];
  const ROOM_TYPES = [['Thường', 6], ['Dịch vụ', 2], ['VIP', 1], ['ICU', 1], ['Cách ly', .6]];
  const floorDepts = { 1: ['cc', 'noi'], 2: ['th', 'xk', 'sp', 'dl'], 3: ['tm', 'tk', 'nhi', 'tmh'], 4: ['hh', 'mat'], 5: ['noi', 'tm'] };
  [1, 2, 3, 4, 5].forEach((floor) => {
    for (let r = 1; r <= 12; r++) {
      const type = floor === 1 && r <= 3 ? 'ICU' : R.weighted(ROOM_TYPES);
      const dept = R.pick(floorDepts[floor]);
      const nBeds = type === 'VIP' ? 1 : type === 'ICU' ? 2 : type === 'Dịch vụ' ? 2 : type === 'Cách ly' ? 1 : R.pick([3, 4]);
      const beds = [];
      for (let b = 0; b < nBeds; b++) {
        const status = R.weighted([['occupied', 6], ['available', 3], ['reserved', 1.2], ['maintenance', .5]]);
        const p = status === 'occupied' ? R.pick(patients.filter((x) => x.dept === dept) .concat(patients.slice(0, 3))) : null;
        beds.push({
          id: `${floor}${U.pad(r)}-${'ABCD'[b]}`, label: 'Giường ' + 'ABCD'[b], status,
          patientId: p ? p.id : null,
          doctorId: p ? p.doctorId : null,
          since: p ? U.addDays(TODAY, -R.int(0, 9)) : null,
          reservedFor: status === 'reserved' ? R.pick(patients).id : null
        });
      }
      rooms.push({ id: floor + U.pad(r), number: floor + U.pad(r), floor, dept, type, beds });
    }
  });

  /* ---------------- Thuốc ---------------- */
  const MEDS = [
    ['Paracetamol 500mg', 'Giảm đau – hạ sốt', 'Viên'], ['Amoxicillin 500mg', 'Kháng sinh', 'Viên'], ['Omeprazol 20mg', 'Tiêu hóa', 'Viên'],
    ['Metformin 850mg', 'Nội tiết', 'Viên'], ['Amlodipin 5mg', 'Tim mạch', 'Viên'], ['Atorvastatin 20mg', 'Tim mạch', 'Viên'],
    ['Losartan 50mg', 'Tim mạch', 'Viên'], ['Cefuroxim 500mg', 'Kháng sinh', 'Viên'], ['Ibuprofen 400mg', 'Giảm đau – hạ sốt', 'Viên'],
    ['Salbutamol 2mg', 'Hô hấp', 'Viên'], ['Ceftriaxon 1g', 'Kháng sinh', 'Lọ'], ['Insulin Glargine 100UI/ml', 'Nội tiết', 'Bút'],
    ['Prednisolon 5mg', 'Kháng viêm', 'Viên'], ['Loratadin 10mg', 'Dị ứng', 'Viên'], ['Esomeprazol 40mg', 'Tiêu hóa', 'Viên'],
    ['Azithromycin 250mg', 'Kháng sinh', 'Viên'], ['Clopidogrel 75mg', 'Tim mạch', 'Viên'], ['Bisoprolol 2.5mg', 'Tim mạch', 'Viên'],
    ['Glucose 5% 500ml', 'Dịch truyền', 'Chai'], ['Natri clorid 0.9% 500ml', 'Dịch truyền', 'Chai'], ['Ringer lactat 500ml', 'Dịch truyền', 'Chai'],
    ['Oresol 4.1g', 'Tiêu hóa', 'Gói'], ['Domperidon 10mg', 'Tiêu hóa', 'Viên'], ['Alpha chymotrypsin 4.2mg', 'Kháng viêm', 'Viên'],
    ['Acyclovir 400mg', 'Kháng virus', 'Viên'], ['Fluconazol 150mg', 'Kháng nấm', 'Viên'], ['Gliclazid 30mg', 'Nội tiết', 'Viên'],
    ['Furosemid 40mg', 'Tim mạch', 'Viên'], ['Methylprednisolon 16mg', 'Kháng viêm', 'Viên'], ['Cetirizin 10mg', 'Dị ứng', 'Viên'],
    ['Ambroxol 30mg', 'Hô hấp', 'Viên'], ['Diosmectit 3g', 'Tiêu hóa', 'Gói'], ['Morphin 10mg/ml', 'Gây nghiện (kiểm soát)', 'Ống'],
    ['Adrenalin 1mg/ml', 'Cấp cứu', 'Ống'], ['Vitamin C 500mg', 'Vitamin', 'Viên'], ['Enoxaparin 40mg', 'Chống đông', 'Bơm tiêm'],
    ['Budesonid 0.5mg/2ml', 'Hô hấp', 'Ống'], ['Pantoprazol 40mg (tiêm)', 'Tiêu hóa', 'Lọ']
  ];
  const SUPPLIERS = ['DHG Pharma', 'Traphaco', 'Imexpharm', 'Pymepharco', 'Sanofi Việt Nam', 'Stada Việt Nam', 'Domesco', 'OPC', 'Vidipha', 'Mekophar'];
  const medicines = MEDS.map((m, i) => {
    const min = R.pick([100, 200, 300, 500]);
    const kind = R.weighted([['ok', 7], ['low', 2], ['out', .8], ['expiring', 1.2]]);
    const qty = kind === 'out' ? 0 : kind === 'low' ? R.int(10, min - 10) : R.int(min + 50, min * 8);
    const expiry = kind === 'expiring' ? U.addDays(TODAY, R.int(8, 55)) : U.addDays(TODAY, R.int(120, 900));
    return {
      code: 'TH-' + U.pad(i + 1, 4), name: m[0], category: m[1], unit: m[2],
      qty, minQty: min, maxQty: min * 8,
      price: R.pick([800, 1200, 1500, 2500, 3200, 4500, 6800, 12000, 18500, 25000, 46000, 125000, 285000]),
      supplier: R.pick(SUPPLIERS), expiry, batch: 'L' + R.int(230000, 259999),
      location: 'Kệ ' + 'ABCDEF'[R.int(0, 5)] + R.int(1, 12),
      status: kind === 'ok' && qty < min ? 'low' : kind
    };
  });

  /* ---------------- Xét nghiệm ---------------- */
  const LAB_PANELS = {
    'Công thức máu (CBC)': [['WBC', 'G/L', 4, 10], ['RBC', 'T/L', 3.8, 5.5], ['HGB', 'g/L', 120, 160], ['HCT', '%', 36, 48], ['PLT', 'G/L', 150, 400], ['NEU%', '%', 45, 75]],
    'Sinh hóa máu': [['Glucose', 'mmol/L', 3.9, 6.4], ['Ure', 'mmol/L', 2.5, 7.5], ['Creatinin', 'µmol/L', 62, 120], ['AST (GOT)', 'U/L', 0, 40], ['ALT (GPT)', 'U/L', 0, 40], ['Acid uric', 'µmol/L', 150, 420]],
    'Mỡ máu': [['Cholesterol TP', 'mmol/L', 3.9, 5.2], ['Triglycerid', 'mmol/L', .46, 1.88], ['HDL-C', 'mmol/L', .9, 2.5], ['LDL-C', 'mmol/L', 0, 3.4]],
    'HbA1c': [['HbA1c', '%', 4, 6.4]],
    'Troponin I hs': [['Troponin I hs', 'ng/L', 0, 26]],
    'Điện giải đồ': [['Na+', 'mmol/L', 135, 145], ['K+', 'mmol/L', 3.5, 5], ['Cl-', 'mmol/L', 98, 106], ['Ca++', 'mmol/L', 1.1, 1.3]],
    'Đông máu cơ bản': [['PT', 'giây', 11, 14], ['INR', '', .8, 1.2], ['APTT', 'giây', 26, 38], ['Fibrinogen', 'g/L', 2, 4]],
    'Tổng phân tích nước tiểu': [['pH', '', 5, 8], ['Tỷ trọng', '', 1.005, 1.03], ['Protein', 'g/L', 0, .15], ['Glucose niệu', 'mmol/L', 0, .8]],
    'CRP định lượng': [['CRP', 'mg/L', 0, 5]],
    'Chức năng tuyến giáp': [['TSH', 'mIU/L', .4, 4], ['FT4', 'pmol/L', 12, 22], ['FT3', 'pmol/L', 3.1, 6.8]]
  };
  const labTechs = staff.filter((s) => s.role === 'Kỹ thuật viên XN');
  const genResults = (panel, critical) => LAB_PANELS[panel].map(([name, unit, lo, hi], idx) => {
    const span = hi - lo || hi || 1;
    let v;
    const r = R.next();
    if (critical && idx === 0) v = hi + span * (1.2 + R.next());
    else if (r < .18) v = hi + span * R.next() * .4;
    else if (r < .26) v = Math.max(0, lo - span * R.next() * .3);
    else v = lo + span * (.15 + R.next() * .7);
    const dec = hi < 2 ? 3 : hi < 20 ? 2 : hi < 200 ? 1 : 0;
    v = +v.toFixed(dec);
    const flag = v > hi ? (critical && idx === 0 ? 'HH' : 'H') : v < lo ? 'L' : '';
    return { name, unit, value: v, ref: `${lo} – ${hi}`, flag };
  });
  const labs = [];
  for (let i = 0; i < 64; i++) {
    const p = R.pick(patients);
    const panel = R.pick(Object.keys(LAB_PANELS));
    const status = R.weighted([['completed', 6], ['processing', 2.5], ['waiting', 2], ['critical', 1]]);
    const ordered = U.addMinutes(NOW, -R.int(10, 60 * 72));
    labs.push({
      id: 'XN-' + U.pad(88000 + i * 13, 6), patientId: p.id, test: panel,
      doctorId: p.doctorId, technician: R.pick(labTechs).name,
      ordered, resultAt: status === 'completed' || status === 'critical' ? U.addMinutes(ordered, R.int(25, 240)) : null,
      status, priority: status === 'critical' ? 'stat' : R.weighted([['routine', 6], ['urgent', 2.5], ['stat', 1]]),
      sample: R.pick(['Máu tĩnh mạch', 'Huyết thanh', 'Nước tiểu', 'Huyết tương']),
      results: status === 'completed' || status === 'critical' ? genResults(panel, status === 'critical') : null,
      progress: status === 'processing' ? R.int(20, 85) : status === 'waiting' ? 0 : 100
    });
  }
  labs.sort((a, b) => b.ordered - a.ordered);

  /* ---------------- Hình ảnh y tế ---------------- */
  const IMG_TYPES = [
    ['X-quang', 'Ngực thẳng'], ['X-quang', 'Cột sống thắt lưng'], ['CT', 'Sọ não không cản quang'], ['CT', 'Ngực liều thấp'],
    ['MRI', 'Cột sống cổ'], ['MRI', 'Khớp gối'], ['Siêu âm', 'Ổ bụng tổng quát'], ['Siêu âm', 'Tim (Doppler)'], ['Siêu âm', 'Thai 3D'], ['X-quang', 'Khung chậu']
  ];
  const IMG_CONCL = ['Chưa phát hiện bất thường', 'Hình ảnh viêm phổi thùy dưới phải', 'Thoái hóa đốt sống L4-L5', 'Gan nhiễm mỡ độ I', 'Thai sống, phát triển phù hợp tuổi thai', 'Hở van hai lá nhẹ', 'Thoát vị đĩa đệm C5-C6', 'Tràn dịch khớp gối lượng ít'];
  const radiologists = doctors.slice(0, 4);
  const imaging = Array.from({ length: 20 }, (_, i) => {
    const t = R.pick(IMG_TYPES), p = R.pick(patients);
    const status = R.weighted([['reported', 5], ['acquired', 2], ['scheduled', 1.5], ['urgent', 1]]);
    return {
      id: 'HA-' + U.pad(30120 + i * 7, 6), patientId: p.id, modality: t[0], part: t[1],
      date: U.addMinutes(NOW, -R.int(30, 60 * 24 * 10)), status,
      radiologist: R.pick(radiologists).name, images: R.int(1, t[0] === 'CT' || t[0] === 'MRI' ? 240 : 6),
      conclusion: status === 'reported' ? R.pick(IMG_CONCL) : status === 'urgent' ? 'Nghi ngờ tổn thương — cần đọc gấp' : ''
    };
  });

  /* ---------------- Hóa đơn / Viện phí ---------------- */
  const SERVICES = [['Khám chuyên khoa', 150000], ['Công thức máu', 90000], ['Sinh hóa máu', 280000], ['X-quang ngực', 180000], ['Siêu âm ổ bụng', 250000], ['CT sọ não', 1250000], ['MRI cột sống', 2650000], ['Giường bệnh (ngày)', 450000], ['Thuốc điều trị', 380000], ['Nội soi dạ dày', 850000], ['Điện tim', 70000], ['Truyền dịch', 120000]];
  const invoices = Array.from({ length: 56 }, (_, i) => {
    const p = R.pick(patients);
    const items = R.shuffle(SERVICES).slice(0, R.int(2, 5)).map(([n, price]) => ({ name: n, qty: n.includes('ngày') ? R.int(1, 6) : 1, price }));
    const total = U.sum(items, (x) => x.qty * x.price);
    const ins = p.insurance ? Math.round(total * p.insurance.rate / 100 * R.pick([.7, .85, 1])) : 0;
    const status = R.weighted([['paid', 7], ['pending', 2.5], ['overdue', .8], ['refunded', .4]]);
    return {
      id: 'HD-' + U.pad(260400 + i * 3, 7), patientId: p.id, date: U.addMinutes(NOW, -R.int(10, 60 * 24 * 20)),
      items, total, insurance: ins, due: total - ins, status,
      method: R.pick(['Tiền mặt', 'Chuyển khoản', 'Thẻ ngân hàng', 'Ví MoMo', 'VNPay QR']), cashier: R.pick(staff.filter((s) => s.role === 'Kế toán' || s.role === 'Lễ tân')).name
    };
  }).sort((a, b) => b.date - a.date);

  const claims = invoices.filter((x) => x.insurance > 0).slice(0, 34).map((inv, i) => {
    const p = patById[inv.patientId];
    return {
      id: 'BH-' + U.pad(7700 + i * 5, 6), invoiceId: inv.id, patientId: p.id, card: p.insurance.number, rate: p.insurance.rate,
      amount: inv.insurance, submitted: U.addDays(inv.date, R.int(0, 2)), status: R.weighted([['approved', 6], ['pending', 3], ['submitted', 1.5], ['rejected', .7]]),
      reason: ''
    };
  });
  claims.forEach((c) => { if (c.status === 'rejected') c.reason = R.pick(['Thẻ BHYT hết hạn', 'Sai tuyến không có giấy chuyển', 'Thiếu chứng từ chỉ định']); });

  /* ---------------- Thiết bị y tế ---------------- */
  const EQUIP = [
    ['Máy thở Hamilton C3', 'wind', 'cc'], ['Monitor Philips IntelliVue MX450', 'monitor', 'cc'], ['Máy sốc tim Zoll R Series', 'zap', 'cc'],
    ['Máy CT Siemens SOMATOM 128', 'scan', 'noi'], ['Máy MRI GE Signa 1.5T', 'scan-line', 'tk'], ['X-quang di động Fujifilm', 'radiation', 'xk'],
    ['Siêu âm GE Voluson E10', 'audio-waveform', 'sp'], ['Máy điện tim Nihon Kohden 12 kênh', 'heart-pulse', 'tm'], ['Máy lọc thận Fresenius 5008S', 'droplets', 'noi'],
    ['Bơm tiêm điện Terumo TE-SS700', 'syringe', 'hh'], ['Máy sinh hóa Roche Cobas c311', 'flask-conical', 'noi'], ['Nội soi Olympus EVIS X1', 'video', 'th'],
    ['Lồng ấp sơ sinh Dräger Babyleo', 'baby', 'nhi'], ['Máy gây mê Dräger Perseus', 'gauge', 'cc'], ['Monitor sản khoa Edan F9', 'activity', 'sp'],
    ['Máy đo khúc xạ Topcon KR-800', 'eye', 'mat'], ['Máy nội soi TMH Karl Storz', 'video', 'tmh'], ['Máy laser CO2 Lumenis', 'sparkles', 'dl'],
    ['Máy thở không xâm lấn ResMed', 'wind', 'hh'], ['Máy truyền dịch B.Braun', 'droplet', 'th'], ['Máy điện não Natus', 'brain', 'tk'], ['Máy đo loãng xương Hologic', 'bone', 'xk']
  ];
  const equipment = EQUIP.map((e, i) => {
    const status = R.weighted([['active', 6], ['ready', 2.5], ['maintenance', 1.2], ['broken', .5]]);
    const last = U.addDays(TODAY, -R.int(10, 170));
    return {
      id: 'TB-' + U.pad(i + 201, 4), name: e[0], icon: e[1], dept: e[2], status,
      serial: 'SN' + R.int(10000000, 99999999), utilization: status === 'active' ? R.int(55, 96) : status === 'ready' ? R.int(10, 45) : 0,
      lastMaint: last, nextMaint: U.addDays(last, 180), warranty: U.addDays(TODAY, R.int(-200, 900)),
      value: R.pick([85, 120, 350, 680, 1200, 4500, 18000, 26000]) * 1e6
    };
  });

  /* ---------------- Đơn thuốc ---------------- */
  const FREQ = ['Sáng 1 – Tối 1', 'Sáng 1', 'Ngày 3 lần, mỗi lần 1 viên', 'Tối 1 viên trước ngủ', 'Khi sốt > 38.5°C', 'Sáng 1 – Trưa 1 – Tối 1'];
  const prescriptions = Array.from({ length: 36 }, (_, i) => {
    const p = R.pick(patients);
    const items = R.shuffle(medicines.filter((m) => m.unit === 'Viên' || m.unit === 'Gói')).slice(0, R.int(2, 5)).map((m) => {
      const days = R.pick([3, 5, 7, 10, 14, 30]);
      return { code: m.code, name: m.name, freq: R.pick(FREQ), days, qty: days * R.int(1, 3), note: R.chance(.3) ? R.pick(['Uống sau ăn', 'Uống trước ăn 30 phút', 'Không dùng với rượu bia']) : '' };
    });
    return {
      id: 'DT-' + U.pad(41200 + i * 9, 6), patientId: p.id, doctorId: p.doctorId, date: U.addMinutes(NOW, -R.int(20, 60 * 24 * 14)),
      diagnosis: p.diagnosis, items, status: R.weighted([['dispensed', 6], ['pending', 3], ['cancelled', .5]]),
      total: U.sum(items, (it) => it.qty * (medicines.find((m) => m.code === it.code) || { price: 1000 }).price)
    };
  }).sort((a, b) => b.date - a.date);

  /* ---------------- Cấp cứu ---------------- */
  const COMPLAINTS = ['Đau ngực trái lan tay, vã mồ hôi', 'Khó thở cấp, SpO2 86%', 'Chấn thương đầu sau TNGT', 'Sốt cao co giật (trẻ 3 tuổi)', 'Đau bụng hố chậu phải', 'Yếu nửa người trái đột ngột', 'Ngộ độc rượu, lơ mơ', 'Gãy hở xương cẳng chân', 'Phản vệ sau tiêm kháng sinh', 'Nôn ra máu lượng nhiều'];
  const emergencyCases = COMPLAINTS.map((c, i) => {
    const esi = [1, 2, 2, 2, 3, 1, 3, 3, 2, 2][i];
    const p = patients[i * 3];
    return {
      id: 'CC-' + U.pad(9120 + i, 5), patientId: p.id, name: p.name, age: p.age, gender: p.gender, complaint: c, esi,
      arrived: U.addMinutes(NOW, -R.int(4, 140)), status: R.pick(['Đang hồi sức', 'Đang xử trí', 'Chờ bác sĩ', 'Chờ kết quả CLS', 'Chuẩn bị chuyển khoa']),
      bed: 'CC-' + U.pad(i + 1), doctorId: R.pick(docsByDept.cc).id,
      vitals: { hr: R.int(58, 138), bp: `${R.int(85, 175)}/${R.int(50, 105)}`, spo2: R.int(84, 99), temp: (36 + R.next() * 3).toFixed(1) },
      mode: R.pick(['Xe cấp cứu 115', 'Tự đến', 'Chuyển viện', 'Xe cấp cứu nội viện'])
    };
  }).sort((a, b) => a.esi - b.esi || a.arrived - b.arrived);
  const ambulances = [
    { id: 'XE-01', plate: '51B-115.01', status: 'enroute', eta: 6, location: 'Ngã tư Hàng Xanh', crew: 'BS. Lê Minh Khoa, ĐD. Thu Hà', progress: 62 },
    { id: 'XE-02', plate: '51B-115.02', status: 'transporting', eta: 11, location: 'Cầu Sài Gòn → BV', crew: 'BS. Phạm Đức Long, ĐD. Ngọc Mai', progress: 38 },
    { id: 'XE-03', plate: '51B-115.03', status: 'ready', eta: 0, location: 'Bãi xe khu A', crew: 'Kíp trực 3', progress: 0 },
    { id: 'XE-04', plate: '51B-115.04', status: 'ready', eta: 0, location: 'Bãi xe khu A', crew: 'Kíp trực 4', progress: 0 },
    { id: 'XE-05', plate: '51B-115.05', status: 'maintenance', eta: 0, location: 'Xưởng bảo dưỡng', crew: '—', progress: 0 }
  ];

  /* ---------------- Thông báo & tin nhắn ---------------- */
  const notifications = [
    { id: 'n1', type: 'emergency', title: 'Ca cấp cứu ESI 1 vừa tiếp nhận', desc: 'BN nam 58 tuổi, đau ngực cấp — giường CC-01. Yêu cầu bác sĩ tim mạch hỗ trợ.', time: U.addMinutes(NOW, -3), read: false, link: '#/emergency' },
    { id: 'n2', type: 'lab', title: 'Kết quả xét nghiệm nguy hiểm', desc: `Troponin I hs = 412 ng/L (${labs.find((l) => l.status === 'critical')?.id || 'XN-088013'}). Cần xử trí ngay.`, time: U.addMinutes(NOW, -9), read: false, link: '#/lab' },
    { id: 'n3', type: 'stock', title: 'Thuốc sắp hết tồn kho', desc: `${medicines.filter((m) => m.status === 'low').length} mặt hàng dưới ngưỡng tối thiểu, gồm ${medicines.find((m) => m.status === 'low')?.name || 'Amoxicillin'}.`, time: U.addMinutes(NOW, -26), read: false, link: '#/pharmacy' },
    { id: 'n4', type: 'appointment', title: 'Nhắc lịch khám 10:30', desc: `${patients[4].name} — ${deptById[patients[4].dept].name}, phòng ${docById[patients[4].doctorId].room}.`, time: U.addMinutes(NOW, -41), read: false, link: '#/appointments' },
    { id: 'n5', type: 'patient', title: 'Bệnh nhân mới nhập viện', desc: `${patients[1].name} (${patients[1].id}) nhập khoa ${deptById[patients[1].dept].name}.`, time: U.addMinutes(NOW, -58), read: false, link: '#/patients/' + patients[1].id },
    { id: 'n6', type: 'payment', title: 'Thanh toán thành công', desc: `Hóa đơn ${invoices[0].id} — ${U.money(invoices[0].due)} qua ${invoices[0].method}.`, time: U.addMinutes(NOW, -75), read: true, link: '#/billing' },
    { id: 'n7', type: 'lab', title: 'Đã có kết quả Công thức máu', desc: `Bệnh nhân ${patients[7].name} — kết quả trong giới hạn bình thường.`, time: U.addMinutes(NOW, -130), read: true, link: '#/lab' },
    { id: 'n8', type: 'stock', title: 'Lô thuốc sắp hết hạn', desc: `${medicines.filter((m) => m.status === 'expiring').length} lô thuốc hết hạn trong 60 ngày tới.`, time: U.addMinutes(NOW, -240), read: true, link: '#/pharmacy' },
    { id: 'n9', type: 'appointment', title: 'Lịch khám bị hủy', desc: `${patients[12].name} đã hủy lịch tái khám ngày mai.`, time: U.addMinutes(NOW, -320), read: true, link: '#/appointments' },
    { id: 'n10', type: 'payment', title: 'Hóa đơn quá hạn thanh toán', desc: `${invoices.filter((x) => x.status === 'overdue').length} hóa đơn quá hạn cần nhắc nhở.`, time: U.addMinutes(NOW, -60 * 20), read: true, link: '#/billing' },
    { id: 'n11', type: 'patient', title: 'Bệnh nhân xuất viện', desc: `${patients[20].name} đã hoàn tất thủ tục xuất viện.`, time: U.addMinutes(NOW, -60 * 26), read: true, link: '#/patients' },
    { id: 'n12', type: 'emergency', title: 'Xe cấp cứu XE-02 đang về viện', desc: 'Dự kiến 11 phút — BN chấn thương đa cơ quan.', time: U.addMinutes(NOW, -60 * 30), read: true, link: '#/emergency' }
  ];
  const NOTIF_META = {
    appointment: { icon: 'calendar-clock', c: 'primary', label: 'Lịch khám' },
    emergency: { icon: 'siren', c: 'danger', label: 'Cấp cứu' },
    stock: { icon: 'package-x', c: 'warning', label: 'Kho thuốc' },
    patient: { icon: 'user-plus', c: 'accent', label: 'Bệnh nhân' },
    lab: { icon: 'flask-conical', c: 'purple', label: 'Xét nghiệm' },
    payment: { icon: 'wallet', c: 'success', label: 'Thanh toán' }
  };
  const messages = [
    { from: doctors[2].name, text: 'Anh xem giúp phim CT của BN giường 305-B nhé.', time: U.addMinutes(NOW, -6), unread: true, online: true },
    { from: staff[0].name, text: 'Khoa Nội đã nhận bàn giao 3 bệnh nhân ca chiều.', time: U.addMinutes(NOW, -22), unread: true, online: true },
    { from: doctors[7].name, text: 'Hội chẩn lúc 14:00 tại phòng họp tầng 3.', time: U.addMinutes(NOW, -95), unread: false, online: false },
    { from: staff[5].name, text: 'Đã cập nhật tồn kho Ceftriaxon 1g.', time: U.addMinutes(NOW, -180), unread: false, online: true }
  ];

  /* ---------------- Audit log & Users ---------------- */
  const ACTIONS = [['Đăng nhập', 'log-in', 6], ['Xem hồ sơ', 'eye', 5], ['Cập nhật', 'pencil', 4], ['Tạo mới', 'plus', 3], ['Xóa', 'trash-2', .8], ['Xuất báo cáo', 'download', 1.2], ['Đổi quyền', 'shield', .5], ['Đăng xuất', 'log-out', 2]];
  const MODULES = ['Bệnh nhân', 'Lịch khám', 'Hồ sơ bệnh án', 'Đơn thuốc', 'Xét nghiệm', 'Thanh toán', 'Kho thuốc', 'Quản trị', 'Báo cáo', 'Hệ thống'];
  const ROLES = ['Super Admin', 'Admin', 'Doctor', 'Nurse', 'Receptionist', 'Pharmacist', 'Laboratory Staff'];
  const ROLE_VI = { 'Super Admin': 'Quản trị cấp cao', 'Admin': 'Quản trị viên', 'Doctor': 'Bác sĩ', 'Nurse': 'Điều dưỡng', 'Receptionist': 'Lễ tân', 'Pharmacist': 'Dược sĩ', 'Laboratory Staff': 'Kỹ thuật viên XN' };
  const users = [
    { id: 'U-0001', name: 'Trần Quốc Bảo', email: 'bao.tran@medcore.vn', role: 'Super Admin', status: 'active', lastLogin: U.addMinutes(NOW, -2), twofa: true },
    { id: 'U-0002', name: 'Lê Thị Thu Hà', email: 'ha.le@medcore.vn', role: 'Admin', status: 'active', lastLogin: U.addMinutes(NOW, -48), twofa: true }
  ];
  doctors.slice(0, 14).forEach((d, i) => users.push({ id: 'U-' + U.pad(i + 3, 4), name: d.name, email: d.email, role: 'Doctor', status: R.weighted([['active', 9], ['locked', .5], ['invited', .5]]), lastLogin: U.addMinutes(NOW, -R.int(5, 6000)), twofa: R.chance(.6) }));
  staff.slice(0, 22).forEach((s, i) => {
    const role = s.role === 'Điều dưỡng' ? 'Nurse' : s.role === 'Lễ tân' ? 'Receptionist' : s.role === 'Dược sĩ' ? 'Pharmacist' : s.role === 'Kỹ thuật viên XN' ? 'Laboratory Staff' : 'Nurse';
    users.push({ id: 'U-' + U.pad(i + 17, 4), name: s.name, email: s.email, role, status: R.weighted([['active', 9], ['locked', .6], ['invited', .8]]), lastLogin: U.addMinutes(NOW, -R.int(5, 9000)), twofa: R.chance(.4) });
  });
  const audit = Array.from({ length: 96 }, (_, i) => {
    const u = R.pick(users);
    const [action, icon] = R.weighted(ACTIONS.map((a) => [[a[0], a[1]], a[2]]));
    const status = action === 'Đăng nhập' && R.chance(.12) ? 'failed' : action === 'Xóa' || action === 'Đổi quyền' ? R.weighted([['success', 3], ['warning', 1]]) : R.weighted([['success', 12], ['failed', .5], ['warning', .5]]);
    const module = action.includes('Đăng') ? 'Hệ thống' : R.pick(MODULES);
    return {
      id: 'LOG-' + U.pad(120000 - i * 7, 6), user: u.name, role: u.role, action, icon, module,
      target: module === 'Bệnh nhân' || module === 'Hồ sơ bệnh án' ? R.pick(patients).id : module === 'Đơn thuốc' ? R.pick(prescriptions).id : module === 'Thanh toán' ? R.pick(invoices).id : module === 'Kho thuốc' ? R.pick(medicines).code : '—',
      time: U.addMinutes(NOW, -i * R.int(3, 25) - R.int(0, 3)), ip: `10.${R.int(0, 20)}.${R.int(0, 255)}.${R.int(2, 254)}`,
      device: R.pick(['Chrome · Windows', 'Edge · Windows', 'Safari · iPad', 'Chrome · Android', 'Firefox · Ubuntu']), status
    };
  });

  const PERM_MODULES = ['Bệnh nhân', 'Lịch khám', 'Hồ sơ bệnh án', 'Đơn thuốc', 'Xét nghiệm', 'Hình ảnh y tế', 'Thanh toán', 'Kho thuốc', 'Báo cáo', 'Quản trị hệ thống'];
  const PERM_ACTIONS = ['Xem', 'Tạo', 'Sửa', 'Xóa', 'Xuất'];
  const permDefault = {
    'Super Admin': () => true,
    'Admin': (m, a) => m !== 'Quản trị hệ thống' || a !== 'Xóa',
    'Doctor': (m, a) => ['Bệnh nhân', 'Lịch khám', 'Hồ sơ bệnh án', 'Đơn thuốc', 'Xét nghiệm', 'Hình ảnh y tế'].includes(m) ? a !== 'Xóa' : m === 'Báo cáo' && a === 'Xem',
    'Nurse': (m, a) => ['Bệnh nhân', 'Lịch khám', 'Hồ sơ bệnh án'].includes(m) ? ['Xem', 'Sửa'].includes(a) : ['Đơn thuốc', 'Xét nghiệm'].includes(m) && a === 'Xem',
    'Receptionist': (m, a) => ['Bệnh nhân', 'Lịch khám'].includes(m) ? a !== 'Xóa' : m === 'Thanh toán' ? ['Xem', 'Tạo'].includes(a) : false,
    'Pharmacist': (m, a) => m === 'Kho thuốc' ? true : m === 'Đơn thuốc' ? ['Xem', 'Sửa'].includes(a) : false,
    'Laboratory Staff': (m, a) => m === 'Xét nghiệm' ? a !== 'Xóa' : m === 'Bệnh nhân' && a === 'Xem'
  };
  const permissions = {};
  ROLES.forEach((r) => { permissions[r] = {}; PERM_MODULES.forEach((m) => { permissions[r][m] = {}; PERM_ACTIONS.forEach((a) => { permissions[r][m][a] = permDefault[r](m, a); }); }); });

  /* ---------------- AI Phân luồng (tích hợp module cũ) ---------------- */
  const SYMPTOMS = [
    ['Tôi bị đau ngực trái, khó thở khi leo cầu thang', 'tm', 'NORMAL'], ['Con tôi sốt 39 độ 2 ngày nay, ho nhiều', 'nhi', 'NORMAL'],
    ['Đau bụng quằn quại, nôn mửa liên tục từ sáng', 'cc', 'EMERGENCY'], ['Đau khớp gối, sưng khi đi lại nhiều', 'xk', 'NORMAL'],
    ['Ngứa và nổi mẩn đỏ khắp người sau khi ăn hải sản', 'dl', 'NORMAL'], ['Chóng mặt, buồn nôn, ù tai bên phải', 'tk', 'NORMAL'],
    ['Mắt mờ dần, nhìn xa không rõ', 'mat', 'NORMAL'], ['Đau họng, nuốt vướng, sốt nhẹ', 'tmh', 'NORMAL'],
    ['Ho kéo dài 3 tuần, có đờm', 'hh', 'NORMAL'], ['Yếu tay chân bên trái, nói ngọng đột ngột', 'cc', 'EMERGENCY'],
    ['Cho hỏi giờ làm việc của bệnh viện ngày thứ 7?', null, 'FAQ'], ['Đi khám có cần mang thẻ BHYT bản gốc không?', null, 'FAQ'],
    ['Ợ chua, nóng rát vùng thượng vị sau ăn', 'th', 'NORMAL'], ['Trễ kinh 2 tuần, đau bụng dưới', 'sp', 'NORMAL'], ['Mệt mỏi, khát nước nhiều, tiểu đêm', 'noi', 'NORMAL']
  ];
  const triageLogs = SYMPTOMS.map((s, i) => ({
    id: 'AI-' + U.pad(55000 + i, 6), text: s[0], dept: s[1], severity: s[2], intent: s[2] === 'FAQ' ? 'FAQ' : 'SYMPTOM',
    confidence: s[2] === 'FAQ' ? .9 + R.next() * .09 : .72 + R.next() * .27, time: U.addMinutes(NOW, -i * R.int(2, 9) - 1),
    ticket: s[2] === 'FAQ' ? null : 'STT ' + U.pad(R.int(1, 90), 3)
  }));
  const ocrScans = Array.from({ length: 6 }, (_, i) => {
    const p = patients[i * 5 + 2];
    return { id: 'OCR-' + U.pad(3300 + i, 5), patient: p.name, card: p.insurance ? p.insurance.number : bhyt(), confidence: .86 + R.next() * .13, time: U.addMinutes(NOW, -i * 11 - 2), status: i === 3 ? 'review' : 'ok' };
  });
  const queues = DEPARTMENTS.map((d) => ({ dept: d.id, waiting: R.int(2, 28), serving: 'STT ' + U.pad(R.int(5, 70), 3), avgWait: R.int(6, 48), called: R.int(0, 4) }));

  /* ---------------- Chart series theo khoảng thời gian ---------------- */
  function seriesFor(kind, range) {
    const cfg = { day: 24, week: 7, month: 30, year: 12 }[range] || 7;
    const labels = range === 'day' ? Array.from({ length: 12 }, (_, i) => U.pad(i * 2) + 'h')
      : range === 'week' ? Array.from({ length: 7 }, (_, i) => U.weekdayShort(U.addDays(TODAY, i - 6)))
      : range === 'month' ? Array.from({ length: 30 }, (_, i) => U.dateShort(U.addDays(TODAY, i - 29)))
      : Array.from({ length: 12 }, (_, i) => 'T' + (i + 1));
    const n = labels.length;
    const k = { day: .25, week: 1, month: 1, year: 30 }[range] || 1;
    const sub = H.rand;
    if (kind === 'patients') return { labels, series: [
      { name: 'Ngoại trú', data: sub.series(n, 320 * k, 60 * k, 2 * k) },
      { name: 'Nội trú', data: sub.series(n, 120 * k, 25 * k, 1 * k) },
      { name: 'Cấp cứu', data: sub.series(n, 38 * k, 10 * k) }
    ] };
    if (kind === 'revenue') return { labels, series: [
      { name: 'Doanh thu', type: 'column', data: sub.series(n, 1.8e9 * k / 1e6, 4e8 * k / 1e6, 0.02e9 * k / 1e6) },
      { name: 'Chi phí', type: 'column', data: sub.series(n, 1.1e9 * k / 1e6, 2e8 * k / 1e6) },
      { name: 'Lợi nhuận', type: 'line', data: [] }
    ] };
    return { labels, series: [] };
  }

  /* ---------------- Medical record (sinh theo bệnh nhân) ---------------- */
  function recordFor(pid) {
    const p = patById[pid] || patients[0];
    const seed = U.hash(pid);
    const r = (a, b, o = 0) => a + ((seed >> o) % (b - a + 1));
    const doc = docById[p.doctorId];
    const days = Array.from({ length: Math.min(6, Math.max(3, r(3, 6))) }, (_, i) => U.addDays(p.admitDate, i));
    const symptomsByDept = {
      tm: ['Đau tức ngực trái', 'Khó thở khi gắng sức', 'Hồi hộp, đánh trống ngực', 'Phù nhẹ hai chi dưới'],
      hh: ['Ho có đờm', 'Sốt 38.5°C', 'Khó thở nhẹ', 'Đau ngực khi ho'], nhi: ['Sốt cao liên tục', 'Ho', 'Biếng ăn', 'Quấy khóc'],
      th: ['Đau thượng vị', 'Ợ chua', 'Buồn nôn', 'Đầy hơi'], default: ['Mệt mỏi', 'Chán ăn', 'Đau đầu nhẹ', 'Mất ngủ']
    };
    return {
      patient: p, doctor: doc,
      icd: ['I10', 'J18.9', 'K29.7', 'E11.9', 'M17.1', 'A97.0', 'I48.9', 'K21.0'][seed % 8],
      diagnosis: p.diagnosis,
      secondary: ['Rối loạn lipid máu', 'Thiếu máu nhẹ', 'Tăng acid uric máu'][seed % 3],
      symptoms: symptomsByDept[p.dept] || symptomsByDept.default,
      history: ['Tăng huyết áp 5 năm, điều trị không đều', 'Đái tháo đường type 2 (2019)', 'Mổ ruột thừa năm 2012', 'Không hút thuốc, uống rượu bia xã giao'].slice(0, 2 + (seed % 3)),
      family: 'Bố: tăng huyết áp, Mẹ: đái tháo đường type 2',
      vitals: { hr: r(66, 98), sys: r(110, 152, 2), dia: r(68, 96, 3), temp: (36.4 + (seed % 14) / 10).toFixed(1), spo2: r(94, 99, 4), rr: r(14, 22, 5), weight: p.weight, height: p.height },
      vitalsTrend: Array.from({ length: 7 }, (_, i) => ({ d: U.dateShort(U.addDays(TODAY, i - 6)), hr: r(68, 95, i) + (i % 3), sys: 150 - i * 4 + (seed % 5), dia: 95 - i * 2, temp: +(37.8 - i * .2 + ((seed >> i) % 3) / 10).toFixed(1) })),
      labs: labs.filter((l) => l.patientId === pid && l.results).slice(0, 3).concat(labs.filter((l) => l.results).slice(0, 2)).slice(0, 3),
      imaging: imaging.filter((x) => x.patientId === pid).concat(imaging.filter((x) => x.status === 'reported').slice(0, 2)).slice(0, 2),
      prescription: prescriptions.find((x) => x.patientId === pid) || prescriptions[seed % prescriptions.length],
      notes: `Bệnh nhân tỉnh, tiếp xúc tốt. Sinh hiệu ổn định. Đáp ứng điều trị tốt sau ${days.length - 1} ngày. Tiếp tục phác đồ hiện tại, theo dõi sát huyết áp và đường huyết. Hẹn tái khám sau 2 tuần hoặc khi có dấu hiệu bất thường.`,
      timeline: days.map((d, i) => ({
        date: d,
        title: i === 0 ? 'Nhập viện & khám ban đầu' : i === days.length - 1 ? (p.status === 'discharged' ? 'Xuất viện' : 'Đánh giá đáp ứng điều trị') : ['Làm xét nghiệm & CĐHA', 'Điều chỉnh phác đồ', 'Hội chẩn chuyên khoa', 'Theo dõi diễn tiến'][i % 4],
        desc: i === 0 ? `Tiếp nhận tại ${deptById[p.dept].name}. Chẩn đoán sơ bộ: ${p.diagnosis}.` : ['Chỉ định CTM, sinh hóa máu, X-quang ngực.', 'Tăng liều thuốc hạ áp, bổ sung men tiêu hóa.', 'Hội chẩn với khoa Nội tiết về kiểm soát đường huyết.', 'Bệnh nhân giảm triệu chứng, ăn uống khá.'][i % 4],
        by: i % 2 ? doc.name : (staff[i] || staff[0]).name,
        type: i === 0 ? 'admit' : i === days.length - 1 ? 'eval' : ['lab', 'rx', 'consult', 'note'][i % 4]
      }))
    };
  }

  /* ---------------- KPI hôm nay ---------------- */
  const occupiedBeds = U.sum(rooms, (r) => r.beds.filter((b) => b.status === 'occupied').length);
  const totalBeds = U.sum(rooms, (r) => r.beds.length);
  const kpi = {
    totalPatients: 12847, inTreatment: patients.filter((p) => p.status === 'treatment' || p.status === 'observation').length + 248,
    apptToday: appointments.filter((a) => U.sameDay(a.start, TODAY)).length + 172, doctorsOnDuty: doctors.filter((d) => d.status !== 'offline').length + 41,
    emergency: emergencyCases.length + 9, revenueToday: 486500000, roomsUsed: occupiedBeds, totalBeds, discharged: 37
  };

  H.data = {
    NOW, TODAY, DEPARTMENTS, deptById, doctors, docById, docsByDept, patients, patById, staff, appointments, rooms,
    medicines, labs, imaging, invoices, claims, equipment, prescriptions, emergencyCases, ambulances,
    notifications, NOTIF_META, messages, users, audit, ROLES, ROLE_VI, PERM_MODULES, PERM_ACTIONS, permissions,
    triageLogs, ocrScans, queues, kpi, seriesFor, recordFor, LAB_PANELS, SERVICES, APPT_TYPES, DIAGNOSES,
    currentUser: { name: 'Trần Quốc Bảo', role: 'Super Admin', roleVi: 'Quản trị cấp cao', email: 'bao.tran@medcore.vn', dept: 'Ban Giám đốc' }
  };
  H.data.dept = (id) => deptById[id] || { name: id, color: '#64748B', icon: 'building-2' };
  H.data.patient = (id) => patById[id];
  H.data.doctor = (id) => docById[id];
})(window.HMS);
