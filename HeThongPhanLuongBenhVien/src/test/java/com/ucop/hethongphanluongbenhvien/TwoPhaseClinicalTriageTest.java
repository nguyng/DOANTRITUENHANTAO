package com.ucop.hethongphanluongbenhvien;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.*;
import com.ucop.hethongphanluongbenhvien.service.ChatService;
import com.ucop.hethongphanluongbenhvien.service.ChatService.ChatSendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TwoPhaseClinicalTriageTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AiAnalysisLogRepository aiAnalysisLogRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatService, "aiServiceUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(chatService, "aiModelName", "qwen2.5-coder:3b");
    }

    @Test
    @DisplayName("Red Flag: Phát hiện dấu hiệu cấp cứu nguy kịch (bất tỉnh, nôn ra máu, đau ngực vã mồ hôi)")
    void testIsEmergencyRedFlag() throws Exception {
        Method method = ChatService.class.getDeclaredMethod("isEmergencyRedFlag", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(chatService, "Bệnh nhân đột ngột bất tỉnh"));
        assertTrue((Boolean) method.invoke(chatService, "Tôi bị co giật liên tục"));
        assertTrue((Boolean) method.invoke(chatService, "Đau ngực dữ dội kèm vã mồ hôi lạnh"));
        assertTrue((Boolean) method.invoke(chatService, "Nôn ra máu tươi nhiều lần"));

        // Không phải cấp cứu
        assertFalse((Boolean) method.invoke(chatService, "Tôi bị đau ngực âm ỉ từ hôm qua"));
        assertFalse((Boolean) method.invoke(chatService, "Tôi hơi đau đầu và sổ mũi"));
    }

    @Test
    @DisplayName("Đánh giá mô tả chi tiết: isVeryDetailed (> 120 ký tự + 2 markers lâm sàng)")
    void testIsVeryDetailed() throws Exception {
        Method method = ChatService.class.getDeclaredMethod("isVeryDetailed", String.class);
        method.setAccessible(true);

        // Chuỗi ngắn -> false
        assertFalse((Boolean) method.invoke(chatService, "Tôi bị ho và đau họng"));

        // Chuỗi dài > 120 ký tự có chứa từ nối 'và', 'kèm' và triệu chứng 'đau', 'sốt' -> true
        String detailed = "Tôi bị đau nhói vùng ngực phải từ 3 hôm nay, đau tăng lên rõ rệt khi hít thở sâu hoặc ho, kèm theo có sốt nhẹ 38 độ và mệt mỏi nhiều.";
        assertTrue((Boolean) method.invoke(chatService, detailed));
    }

    @Test
    @DisplayName("Chuẩn hoá mã khoa: normalizeDeptCode ánh xạ chính xác các biến thể")
    void testNormalizeDeptCode() throws Exception {
        Method method = ChatService.class.getDeclaredMethod("normalizeDeptCode", String.class, String.class);
        method.setAccessible(true);

        assertEquals("KCAPCUU", method.invoke(chatService, "CAPCUU", ""));
        assertEquals("KHOHAP", method.invoke(chatService, "KHOA_HOHAP", "ho nhiều đờm"));
        assertEquals("KTIEUHOA", method.invoke(chatService, "TIEU_HOA", "đau dạ dày"));
        assertEquals("KTIM", method.invoke(chatService, "TIM_MACH", "hồi hộp tim đập nhanh"));
        assertEquals("KXUONGKHOP", method.invoke(chatService, "CO_XUONG_KHOP", "đau khớp gối"));
    }

    @Test
    @DisplayName("Giai đoạn 1: Fallback khi người bệnh mô tả ngắn gọn -> Đặt câu hỏi khai thác chuyên sâu")
    void testFallbackPhase1Probe() throws Exception {
        Method method = ChatService.class.getDeclaredMethod("fallbackResponse", String.class, List.class);
        method.setAccessible(true);

        // Lượt 1 với triệu chứng đau ngực
        Object aiResponse = method.invoke(chatService, "Tôi bị đau ngực", Collections.emptyList());
        assertNotNull(aiResponse);

        Method hasTriage = aiResponse.getClass().getDeclaredMethod("hasTriageResult");
        hasTriage.setAccessible(true);
        assertFalse((Boolean) hasTriage.invoke(aiResponse), "Lượt đầu chưa được chốt khoa, phải hỏi thêm");

        Method replyMethod = aiResponse.getClass().getDeclaredMethod("reply");
        replyMethod.setAccessible(true);
        String reply = (String) replyMethod.invoke(aiResponse);
        assertTrue(reply.contains("Cơn đau ngực của bạn có cảm giác như thế nào"));
    }

    @Test
    @DisplayName("Giai đoạn 2: Fallback khi đã qua hỏi đáp -> Phân luồng chính xác vào Khoa Hô hấp")
    void testFallbackPhase2Decision() throws Exception {
        Method method = ChatService.class.getDeclaredMethod("fallbackResponse", String.class, List.class);
        method.setAccessible(true);

        List<Map<String, String>> history = List.of(
                Map.of("role", "user", "content", "Tôi bị đau ngực"),
                Map.of("role", "assistant", "content", "Cơn đau ngực của bạn có cảm giác như thế nào?")
        );

        // Lượt 2: Người bệnh làm rõ đau tăng khi hít sâu, ho có đờm
        Object aiResponse = method.invoke(chatService, "Đau nhói khi hít sâu, có kèm ho và sốt", history);
        assertNotNull(aiResponse);

        Method hasTriage = aiResponse.getClass().getDeclaredMethod("hasTriageResult");
        hasTriage.setAccessible(true);
        assertTrue((Boolean) hasTriage.invoke(aiResponse), "Lượt 2 đã có thông tin mấu chốt, phải phân luồng");

        Method triageRawMethod = aiResponse.getClass().getDeclaredMethod("triageRaw");
        triageRawMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> triage = (Map<String, Object>) triageRawMethod.invoke(aiResponse);
        assertEquals("KHOHAP", triage.get("department_code"));
    }
}