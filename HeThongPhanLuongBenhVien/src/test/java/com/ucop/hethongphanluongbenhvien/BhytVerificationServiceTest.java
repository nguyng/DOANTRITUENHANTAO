package com.ucop.hethongphanluongbenhvien;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import com.ucop.hethongphanluongbenhvien.service.BhytVerificationService;
import com.ucop.hethongphanluongbenhvien.service.BhytVerificationService.BhytVerifyResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BhytVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BhytVerificationService bhytVerificationService;

    @Test
    @DisplayName("Kiểm tra mã BHYT rỗng -> Báo lỗi E00")
    void testEmptyMaThe() {
        BhytVerifyResult result = bhytVerificationService.verify("");
        assertFalse(result.valid());
        assertEquals("E00", result.errorCode());
    }

    @Test
    @DisplayName("Kiểm tra mã BHYT sai độ dài (< 15 ký tự) -> Báo lỗi E01")
    void testInvalidLength() {
        BhytVerifyResult result = bhytVerificationService.verify("DN479001");
        assertFalse(result.valid());
        assertEquals("E01", result.errorCode());
    }

    @Test
    @DisplayName("Kiểm tra mã đối tượng không tồn tại (XX) -> Báo lỗi E01")
    void testInvalidObjectCode() {
        BhytVerifyResult result = bhytVerificationService.verify("XX4790000000001");
        assertFalse(result.valid());
        assertEquals("E01", result.errorCode());
    }

    @Test
    @DisplayName("Kiểm tra mã tỉnh không hợp lệ (00) -> Báo lỗi E01")
    void testInvalidProvinceCode() {
        BhytVerifyResult result = bhytVerificationService.verify("DN4000000000001");
        assertFalse(result.valid());
        assertEquals("E01", result.errorCode());
    }

    @Test
    @DisplayName("Kiểm tra thẻ hợp lệ từ Mock API BHXH (TE: Trẻ em hưởng 100%)")
    void testValidCardMockBhxh() {
        // Seed 15 -> 15 % 100 = 15 >= 8 -> Hợp lệ
        String card = "TE1790000000015";
        when(userRepository.findByBhytNumber(card)).thenReturn(Optional.empty());

        BhytVerifyResult result = bhytVerificationService.verify(card);
        assertTrue(result.valid());
        assertEquals("TE", result.maDoiTuong());
        assertEquals(100, result.mucHuong());
        assertEquals("MOCK_BHXH_API", result.source());
    }

    @Test
    @DisplayName("Kiểm tra thẻ BHYT có sẵn trong Database người dùng nội bộ")
    void testValidCardFromDb() {
        String card = "HN4790000000001";
        User user = User.builder()
                .bhytNumber(card)
                .fullName("Nguyễn Văn Nghèo")
                .bhytObjectCode("HN")
                .bhytObjectName("Hộ nghèo")
                .bhytDiscountRate(BigDecimal.valueOf(1.00))
                .bhytExpiryDate(LocalDate.now().plusYears(2))
                .build();

        when(userRepository.findByBhytNumber(card)).thenReturn(Optional.of(user));

        BhytVerifyResult result = bhytVerificationService.verify(card);
        assertTrue(result.valid());
        assertEquals("HN", result.maDoiTuong());
        assertEquals(100, result.mucHuong());
        assertEquals("DB_LOCAL", result.source());
    }
}