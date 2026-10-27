package com.ucop.hethongphanluongbenhvien.service;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * ══════════════════════════════════════════════════════════════════════
 *  BhytVerificationService — Dịch vụ xác thực Bảo Hiểm Y Tế (BHYT)
 * ══════════════════════════════════════════════════════════════════════
 *
 * Mô phỏng 3 lớp xác thực của hệ thống giám định BHYT thực tế tại VN:
 *
 *  LỚP 1 — VALIDATE FORMAT
 *      Kiểm tra cấu trúc 15 ký tự theo QĐ 1666/QĐ-BHXH:
 *      [2 chữ cái mã ĐT][1 số mức hưởng][2 số mã tỉnh][10 số thứ tự]
 *      VD: "HN479XXXXXXXXXX" → Hộ nghèo, mức 4 (80%), TP.HCM
 *
 *  LỚP 2 — TRA CỨU DB NỘI BỘ
 *      Nếu mã BHYT đã có trong USERS DB → xác thực ngay từ DB.
 *      Kiểm tra ngày hết hạn, trạng thái tài khoản.
 *
 *  LỚP 3 — MOCK BHXH API
 *      Giả lập cổng gateway.baohiemxahoi.gov.vn.
 *      Dựa trên cấu trúc mã để suy ra: đối tượng, mức hưởng, tỉnh.
 *      Dùng seed từ mã số để tạo ngày hết hạn giả định nhất quán.
 *
 *  Trong môi trường production thật → thay mock bằng RestTemplate/WebClient
 *  gọi API: POST https://gateway.baohiemxahoi.gov.vn/api/thekbhyt/tracuu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BhytVerificationService {

    private final UserRepository userRepository;

    // ── Bảng mã đối tượng chuẩn BHXH Việt Nam ────────────────────────────────

    /** Mã đối tượng → Tên hiển thị */
    private static final Map<String, String> DT_NAMES = Map.ofEntries(
        Map.entry("TE", "Trẻ em dưới 6 tuổi"),
        Map.entry("HT", "Hưu trí / Mất sức LĐ"),
        Map.entry("HN", "Hộ nghèo"),
        Map.entry("CN", "Cận nghèo"),
        Map.entry("DT", "Đồng bào dân tộc thiểu số vùng KK"),
        Map.entry("DN", "Người lao động (DN đóng)"),
        Map.entry("TN", "Người lao động tự nguyện"),
        Map.entry("HS", "Học sinh / Sinh viên"),
        Map.entry("GD", "Thân nhân người lao động"),
        Map.entry("NO", "Đối tượng khác do NSNN"),
        Map.entry("BT", "Bảo trợ xã hội"),
        Map.entry("PO", "Người có công cách mạng"),
        Map.entry("SY", "Cán bộ xã phường"),
        Map.entry("TK", "Thương binh, liệt sĩ"),
        Map.entry("HC", "Hành chính sự nghiệp")
    );

    /** Mã tỉnh → Tên tỉnh/thành */
    private static final Map<String, String> TINH_NAMES = Map.ofEntries(
        Map.entry("01", "Hà Nội"),   Map.entry("02", "Hà Giang"),
        Map.entry("04", "Cao Bằng"), Map.entry("06", "Bắc Kạn"),
        Map.entry("08", "Tuyên Quang"), Map.entry("10", "Lào Cai"),
        Map.entry("11", "Điện Biên"), Map.entry("12", "Lai Châu"),
        Map.entry("14", "Sơn La"),   Map.entry("15", "Yên Bái"),
        Map.entry("17", "Hoà Bình"), Map.entry("19", "Thái Nguyên"),
        Map.entry("20", "Lạng Sơn"), Map.entry("22", "Quảng Ninh"),
        Map.entry("24", "Bắc Giang"), Map.entry("25", "Phú Thọ"),
        Map.entry("26", "Vĩnh Phúc"), Map.entry("27", "Bắc Ninh"),
        Map.entry("30", "Hải Dương"), Map.entry("31", "Hải Phòng"),
        Map.entry("33", "Hưng Yên"), Map.entry("34", "Thái Bình"),
        Map.entry("35", "Hà Nam"),   Map.entry("36", "Nam Định"),
        Map.entry("37", "Ninh Bình"), Map.entry("38", "Thanh Hóa"),
        Map.entry("40", "Nghệ An"),  Map.entry("42", "Hà Tĩnh"),
        Map.entry("44", "Quảng Bình"), Map.entry("45", "Quảng Trị"),
        Map.entry("46", "Thừa Thiên Huế"), Map.entry("48", "Đà Nẵng"),
        Map.entry("49", "Quảng Nam"), Map.entry("51", "Quảng Ngãi"),
        Map.entry("52", "Bình Định"), Map.entry("54", "Phú Yên"),
        Map.entry("56", "Khánh Hòa"), Map.entry("58", "Ninh Thuận"),
        Map.entry("60", "Bình Thuận"), Map.entry("62", "Kon Tum"),
        Map.entry("64", "Gia Lai"),  Map.entry("66", "Đắk Lắk"),
        Map.entry("67", "Đắk Nông"), Map.entry("68", "Lâm Đồng"),
        Map.entry("70", "Bình Phước"), Map.entry("72", "Tây Ninh"),
        Map.entry("74", "Bình Dương"), Map.entry("75", "Đồng Nai"),
        Map.entry("77", "Bà Rịa-Vũng Tàu"), Map.entry("79", "TP. Hồ Chí Minh"),
        Map.entry("80", "Long An"),  Map.entry("82", "Tiền Giang"),
        Map.entry("83", "Bến Tre"),  Map.entry("84", "Trà Vinh"),
        Map.entry("86", "Vĩnh Long"), Map.entry("87", "Đồng Tháp"),
        Map.entry("89", "An Giang"), Map.entry("91", "Kiên Giang"),
        Map.entry("92", "Cần Thơ"), Map.entry("93", "Hậu Giang"),
        Map.entry("94", "Sóc Trăng"), Map.entry("95", "Bạc Liêu"),
        Map.entry("96", "Cà Mau")
    );

    // ═══════════════════════════════════════════════════════════════════════════
    //  RECORD KẾT QUẢ XÁC THỰC
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Kết quả xác thực BHYT — tương đương response body của cổng BHXH thực.
     *
     * @param valid          true nếu thẻ hợp lệ và đang hoạt động
     * @param maThe          mã BHYT chuẩn hoá (15 ký tự, in hoa)
     * @param hoTen          họ tên trên thẻ (từ DB hoặc suy đoán)
     * @param maDoiTuong     mã đối tượng 2 ký tự (VD "DN", "TE")
     * @param tenDoiTuong    tên đối tượng đầy đủ (VD "Người lao động")
     * @param mucHuong       mức hưởng % (80, 95, 100)
     * @param maTinh         mã tỉnh 2 số (VD "79")
     * @param tenTinh        tên tỉnh (VD "TP. Hồ Chí Minh")
     * @param tuNgay         ngày bắt đầu hiệu lực
     * @param denNgay        ngày hết hạn thẻ
     * @param dungTuyen      true = đúng tuyến (hưởng đủ %), false = trái tuyến
     * @param errorCode      mã lỗi nếu invalid (E01, E02, E03, E04)
     * @param errorMessage   mô tả lỗi
     * @param source         nguồn xác thực: "DB_LOCAL" | "MOCK_BHXH_API"
     */
    public record BhytVerifyResult(
            boolean    valid,
            String     maThe,
            String     hoTen,
            String     maDoiTuong,
            String     tenDoiTuong,
            int        mucHuong,
            String     maTinh,
            String     tenTinh,
            LocalDate  tuNgay,
            LocalDate  denNgay,
            boolean    dungTuyen,
            String     errorCode,
            String     errorMessage,
            String     source
    ) {
        // Convenience: tỷ lệ dạng BigDecimal (0.80, 0.95, 1.00)
        public BigDecimal mucHuongDecimal() {
            return BigDecimal.valueOf(mucHuong).divide(BigDecimal.valueOf(100));
        }

        // Kiểm tra thẻ còn hạn không
        public boolean conHan() {
            return denNgay != null && !LocalDate.now().isAfter(denNgay);
        }

        // Kết quả lỗi nhanh
        public static BhytVerifyResult error(String maThe, String code, String msg) {
            return new BhytVerifyResult(false, maThe, null, null, null,
                    0, null, null, null, null, false, code, msg, "VALIDATION");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUBLIC API — verify()
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Xác thực mã BHYT — điểm vào chính của service.
     * Chạy qua 3 lớp: Format → DB → Mock API.
     *
     * @param maThe mã BHYT nhập vào (có thể có dấu cách, hoa/thường)
     * @return BhytVerifyResult chứa đầy đủ thông tin xác thực
     */
    public BhytVerifyResult verify(String maThe) {
        // ── Chuẩn hoá ───────────────────────────────────────────────────────
        if (maThe == null || maThe.isBlank()) {
            return BhytVerifyResult.error("", "E00", "Mã BHYT không được để trống");
        }
        String normalized = maThe.replaceAll("\\s+", "").toUpperCase();

        // ── LỚP 1: Validate Format ──────────────────────────────────────────
        BhytVerifyResult formatCheck = validateFormat(normalized);
        if (!formatCheck.valid()) {
            log.info("[BHYT] Format lỗi: {} — {}", normalized, formatCheck.errorMessage());
            return formatCheck;
        }

        // ── LỚP 2: Tra cứu DB nội bộ ───────────────────────────────────────
        Optional<User> userOpt = userRepository.findByBhytNumber(normalized);
        if (userOpt.isPresent()) {
            log.info("[BHYT] Tìm thấy trong DB: {}", normalized);
            return buildResultFromDb(normalized, userOpt.get());
        }

        // ── LỚP 3: Mock BHXH API ────────────────────────────────────────────
        log.info("[BHYT] Không có trong DB, gọi Mock BHXH API: {}", normalized);
        return mockBhxhApiCall(normalized);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LỚP 1 — VALIDATE FORMAT (15 ký tự, chuẩn QĐ 1666/QĐ-BHXH)
    // ═══════════════════════════════════════════════════════════════════════════

    private BhytVerifyResult validateFormat(String ma) {

        // Kiểm tra độ dài
        if (ma.length() != 15) {
            return BhytVerifyResult.error(ma, "E01",
                    "Mã BHYT phải đúng 15 ký tự (hiện tại: " + ma.length() + " ký tự)");
        }

        // 2 ký tự đầu: mã đối tượng (chữ cái)
        String maDt = ma.substring(0, 2);
        if (!maDt.matches("[A-Z]{2}")) {
            return BhytVerifyResult.error(ma, "E01",
                    "2 ký tự đầu phải là chữ cái (mã đối tượng). Nhận được: '" + maDt + "'");
        }
        if (!DT_NAMES.containsKey(maDt)) {
            return BhytVerifyResult.error(ma, "E01",
                    "Mã đối tượng '" + maDt + "' không hợp lệ. "
                    + "Các mã hợp lệ: TE, HT, HN, CN, DN, TN, HS, GD, DT, BT, PO...");
        }

        // Ký tự 3: mức hưởng (1-4)
        char mucChar = ma.charAt(2);
        if (mucChar < '1' || mucChar > '4') {
            return BhytVerifyResult.error(ma, "E01",
                    "Ký tự thứ 3 là mức hưởng, phải từ 1 đến 4. Nhận được: '" + mucChar + "'");
        }

        // Ký tự 4-5: mã tỉnh (2 chữ số)
        String maTinh = ma.substring(3, 5);
        if (!maTinh.matches("\\d{2}")) {
            return BhytVerifyResult.error(ma, "E01",
                    "Ký tự 4-5 phải là mã tỉnh (2 chữ số). Nhận được: '" + maTinh + "'");
        }
        if (!TINH_NAMES.containsKey(maTinh)) {
            return BhytVerifyResult.error(ma, "E01",
                    "Mã tỉnh '" + maTinh + "' không tồn tại trong danh mục BHXH.");
        }

        // Ký tự 6-15: 10 chữ số thứ tự
        String soThuTu = ma.substring(5);
        if (!soThuTu.matches("\\d{10}")) {
            return BhytVerifyResult.error(ma, "E01",
                    "10 ký tự cuối phải là số thứ tự (0-9). Nhận được: '" + soThuTu + "'");
        }

        // Format hợp lệ — trả về kết quả trung gian (chưa xác thực tồn tại)
        return new BhytVerifyResult(true, ma, null, maDt, DT_NAMES.get(maDt),
                parseMucHuong(mucChar), maTinh, TINH_NAMES.get(maTinh),
                null, null, true, null, "Format hợp lệ", "FORMAT_OK");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LỚP 2 — TRA CỨU DB NỘI BỘ
    // ═══════════════════════════════════════════════════════════════════════════

    private BhytVerifyResult buildResultFromDb(String ma, User user) {
        // Kiểm tra tài khoản có active không
        if (Boolean.FALSE.equals(user.getIsActive())) {
            return BhytVerifyResult.error(ma, "E03",
                    "Thẻ BHYT này đã bị khoá trong hệ thống. Vui lòng liên hệ BHXH.");
        }

        // Kiểm tra ngày hết hạn
        LocalDate denNgay = user.getBhytExpiryDate();
        boolean conHan = (denNgay == null) || !LocalDate.now().isAfter(denNgay);
        if (!conHan) {
            return BhytVerifyResult.error(ma, "E04",
                    "Thẻ BHYT đã hết hạn vào ngày " + denNgay + ". Vui lòng gia hạn tại BHXH.");
        }

        // Suy ra mã đối tượng từ 2 ký tự đầu
        String maDt    = ma.substring(0, 2);
        char   mucChar = ma.charAt(2);
        String maTinh  = ma.substring(3, 5);

        // Ưu tiên dùng thông tin đã lưu trong DB, fallback về suy luận từ mã
        String tenDt   = (user.getBhytObjectName() != null)
                ? user.getBhytObjectName() : DT_NAMES.getOrDefault(maDt, "Không xác định");
        int    mucHuong = (user.getBhytDiscountRate() != null)
                ? user.getBhytDiscountRate().multiply(BigDecimal.valueOf(100)).intValue()
                : parseMucHuong(mucChar);

        return new BhytVerifyResult(
                true,
                ma,
                user.getFullName(),
                maDt,   tenDt,
                mucHuong,
                maTinh, TINH_NAMES.getOrDefault(maTinh, "Không xác định"),
                user.getBhytIssuedDate(),
                denNgay,
                true,   // dungTuyen — giả định đúng tuyến (cần tích hợp BHXH thật để kiểm tra)
                null,
                "Xác thực từ cơ sở dữ liệu nội bộ",
                "DB_LOCAL"
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LỚP 3 — MOCK BHXH API
    //  Giả lập cổng: https://gateway.baohiemxahoi.gov.vn
    //
    //  Logic mock: dựa trên cấu trúc mã để suy ra thông tin hợp lý.
    //  Dùng hash của số thứ tự để quyết định thẻ có tồn tại hay không
    //  (khoảng 90% thẻ có format đúng sẽ được coi là hợp lệ — mô phỏng thực tế).
    //
    //  Khi deploy production thật → thay phần này bằng:
    //    restTemplate.postForObject(BHXH_GATEWAY_URL, request, BhxhResponse.class)
    // ═══════════════════════════════════════════════════════════════════════════

    private BhytVerifyResult mockBhxhApiCall(String ma) {
        String maDt    = ma.substring(0, 2);
        char   mucChar = ma.charAt(2);
        String maTinh  = ma.substring(3, 5);
        String soThuTu = ma.substring(5);

        // Giả lập độ trễ mạng (không cần trong test, comment ra khi cần)
        // Thread.sleep(200);

        // Quyết định thẻ có tồn tại không — dùng hash để nhất quán:
        // Cùng một mã luôn trả về cùng một kết quả (deterministic mock)
        long hashSeed = Long.parseLong(soThuTu) % 100;
        if (hashSeed < 8) { // ~8% thẻ bị coi là không tồn tại (số thứ tự chưa được cấp)
            return BhytVerifyResult.error(ma, "E02",
                    "Mã BHYT '" + ma + "' không tìm thấy trong hệ thống BHXH. "
                    + "Vui lòng kiểm tra lại hoặc liên hệ BHXH tỉnh " + TINH_NAMES.getOrDefault(maTinh, ""));
        }

        // Sinh ngày hạn thẻ giả định nhất quán từ seed
        // Mô phỏng: thẻ được cấp từ 1-3 năm trước, hạn 1-3 năm tới
        long yearOffset  = (hashSeed % 3);          // 0, 1, hoặc 2 năm
        long monthOffset = (hashSeed % 12) + 1;      // tháng 1–12
        LocalDate tuNgay = LocalDate.now()
                .minusYears(yearOffset + 1)
                .withMonth((int) monthOffset)
                .withDayOfMonth(1);
        LocalDate denNgay = tuNgay.plusYears(3);     // thẻ hợp lệ 3 năm

        // Kiểm tra thẻ hết hạn (theo ngày sinh giả lập)
        if (LocalDate.now().isAfter(denNgay)) {
            return BhytVerifyResult.error(ma, "E04",
                    "Thẻ BHYT hết hạn ngày " + denNgay + ". Vui lòng gia hạn tại cơ quan BHXH.");
        }

        int mucHuong = parseMucHuong(mucChar);

        // Xác định đúng tuyến: giả lập bệnh viện MedCore thuộc TP.HCM (mã 79)
        boolean dungTuyen = maTinh.equals("79");

        return new BhytVerifyResult(
                true,
                ma,
                null,                                         // họ tên chưa biết (cần nhập thêm)
                maDt,  DT_NAMES.getOrDefault(maDt, "Khác"),
                mucHuong,
                maTinh, TINH_NAMES.getOrDefault(maTinh, "Không xác định"),
                tuNgay,
                denNgay,
                dungTuyen,
                null,
                dungTuyen
                    ? "Hợp lệ — Đúng tuyến. Được hưởng " + mucHuong + "% chi phí."
                    : "Hợp lệ — Trái tuyến ngoại trú. Chỉ được hưởng 40% theo quy định.",
                "MOCK_BHXH_API"
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Chuyển ký tự mức hưởng (1-4) thành % tương ứng */
    private int parseMucHuong(char c) {
        return switch (c) {
            case '1' -> 100;   // Trẻ em dưới 6, Hộ nghèo, Người có công...
            case '2' -> 100;   // Hưu trí, MSLĐ (có giới hạn trần chi phí)
            case '3' -> 95;    // Cận nghèo, Dân tộc thiểu số vùng KK
            case '4' -> 80;    // Người lao động, Học sinh, SV
            default  -> 80;
        };
    }

    /**
     * Áp dụng kết quả xác thực vào User entity (cập nhật thông tin BHYT từ API).
     * Dùng khi bệnh nhân nhập mã BHYT mới lần đầu.
     */
    public void applyVerifyResult(User user, BhytVerifyResult result) {
        if (!result.valid()) return;
        user.setBhytObjectCode(result.maDoiTuong());
        user.setBhytObjectName(result.tenDoiTuong());
        user.setBhytDiscountRate(result.mucHuongDecimal());
        user.setBhytExpiryDate(result.denNgay());
        user.setBhytIssuedDate(result.tuNgay());
    }
}
