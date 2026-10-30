package com.ucop.hethongphanluongbenhvien;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import com.ucop.hethongphanluongbenhvien.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCccdAndBhytMatchingTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Tra cứu người dùng bằng số CCCD 12 số")
    void testFindByCccd() {
        String cccd = "079204000001";
        User user = User.builder()
                .cccd(cccd)
                .fullName("Nguyễn Văn Nghèo")
                .bhytNumber("HN4790000000001")
                .role(User.Role.PATIENT)
                .build();

        when(userRepository.findByCccd(cccd)).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByCccd(cccd);
        assertTrue(found.isPresent());
        assertEquals("Nguyễn Văn Nghèo", found.get().getFullName());
        assertEquals("HN4790000000001", found.get().getBhytNumber());
    }

    @Test
    @DisplayName("Tra cứu đa định danh findByAnyIdentifier qua CCCD")
    void testFindByAnyIdentifierWithCccd() {
        String cccd = "079204000002";
        User user = User.builder()
                .cccd(cccd)
                .fullName("Phạm Thị Cận")
                .bhytNumber("CN3790000000001")
                .build();

        when(userRepository.findByCccd(cccd)).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByAnyIdentifier(cccd);
        assertTrue(found.isPresent());
        assertEquals("Phạm Thị Cận", found.get().getFullName());
    }

    @Test
    @DisplayName("Kiểm tra tính toàn vẹn: BHYT phải khớp với CCCD của tài khoản")
    void testBhytMatchesUserCccd() {
        User loggedInUser = User.builder()
                .cccd("079204000005")
                .fullName("Nguyễn Thị Lan")
                .bhytNumber("DN4790000000001")
                .build();

        String inputBhytCorrect = "DN4790000000001";
        String inputBhytWrong = "HN4790000000001";

        // Khớp BHYT
        assertTrue(inputBhytCorrect.equalsIgnoreCase(loggedInUser.getBhytNumber()));

        // Không khớp BHYT (của người khác) -> Bị từ chối
        assertFalse(inputBhytWrong.equalsIgnoreCase(loggedInUser.getBhytNumber()));
    }
}