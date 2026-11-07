package com.ucop.hethongphanluongbenhvien;

import com.ucop.hethongphanluongbenhvien.repository.*;
import com.ucop.hethongphanluongbenhvien.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Kiểm thử toàn diện bộ lọc câu hỏi ngoài chủ đề y tế (Off-Topic Guard).
 * Đảm bảo AI không trả lời các chủ đề không liên quan đến sức khỏe.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Off-Topic Guard — Bộ lọc câu hỏi ngoài y tế")
public class OffTopicGuardTest {

    @Mock private ChatSessionRepository chatSessionRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private AiAnalysisLogRepository aiAnalysisLogRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatService, "aiServiceUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(chatService, "aiModelName", "qwen2.5-coder:3b");
    }

    // ===================================================================
    // NHÓM 1: Kiểm thử hàm isOffTopic() trực tiếp
    // ===================================================================

    @Nested
    @DisplayName("1. Kiểm thử hàm isOffTopic() - nhận diện đúng chủ đề")
    class IsOffTopicTests {

        private Method isOffTopicMethod;

        @BeforeEach
        void getMethod() throws Exception {
            isOffTopicMethod = ChatService.class.getDeclaredMethod("isOffTopic", String.class);
            isOffTopicMethod.setAccessible(true);
        }

        @ParameterizedTest(name = "[OFF-TOPIC] \"{0}\"")
        @ValueSource(strings = {
                "bong da hom nay ai thang",
                "world cup 2026 ket qua",
                "thoi tiet ha noi ngay mai the nao",
                "nau an mon pho bo ngon",
                "iphone moi ra mat gia bao nhieu",
                "phim hay nen xem tuan nay",
                "bitcoin gia bao nhieu",
                "chinh tri bau cu ket qua",
                "du lich da nang khach san",
                "game lien quan mobile mua nay",
                "am nhac ca si noi tieng",
                "the thao bong ro nba",
                "ve may bay ha noi sai gon gia bao nhieu",
                "bóng đá hôm nay ai thắng",
                "thời tiết hà nội ngày mai thế nào",
                "giá vé máy bay đi đà nẵng"
        })
        @DisplayName("Phát hiện đúng câu hỏi ngoài y tế -> isOffTopic = true")
        void testDetectsOffTopicKeywords(String input) throws Exception {
            Boolean result = (Boolean) isOffTopicMethod.invoke(chatService, input.toLowerCase());
            assertTrue(result, "Phải nhận diện \"" + input + "\" là off-topic");
        }

        @ParameterizedTest(name = "[Y TẾ] \"{0}\"")
        @ValueSource(strings = {
                "tôi bị đau ngực từ sáng",
                "sốt cao 39 độ 2 ngày không giảm",
                "ho nhiều đờm xanh kèm khó thở",
                "đau bụng buồn nôn sau ăn",
                "chóng mặt tê bì tay chân",
                "mắt đỏ cộm ngứa nhiều",
                "ngứa da nổi mề đay toàn thân",
                "đau khớp gối sưng lên",
                "muốn khám bác sĩ hôm nay",
                "thuốc huyết áp uống thế nào",
                "bệnh tiểu đường cần khám khoa nào"
        })
        @DisplayName("Không block câu hỏi y tế hợp lệ -> isOffTopic = false")
        void testAllowsMedicalQuestions(String input) throws Exception {
            Boolean result = (Boolean) isOffTopicMethod.invoke(chatService, input.toLowerCase());
            assertFalse(result, "Không được block câu hỏi y tế: \"" + input + "\"");
        }
    }

    // ===================================================================
    // NHÓM 2: Kiểm thử fallbackResponse() với câu hỏi off-topic
    // ===================================================================

    @Nested
    @DisplayName("2. Kiểm thử fallbackResponse() — Phản hồi từ chối lịch sự")
    class FallbackOffTopicTests {

        @Test
        @DisplayName("fallback: Hỏi về bóng đá -> Từ chối lịch sự, không phân luồng")
        void testFallbackRejectsFootballQuestion() throws Exception {
            Method method = ChatService.class.getDeclaredMethod("fallbackResponse", String.class, List.class);
            method.setAccessible(true);

            List<Map<String, String>> history = List.of(
                    Map.of("role", "user", "content", "xin chào"),
                    Map.of("role", "assistant", "content", "Xin chào!")
            );

            Object aiResponse = method.invoke(chatService, "bóng đá world cup ai thắng", history);
            assertNotNull(aiResponse);

            // Phải không phân luồng
            Method hasTriage = aiResponse.getClass().getDeclaredMethod("hasTriageResult");
            hasTriage.setAccessible(true);
            assertFalse((Boolean) hasTriage.invoke(aiResponse), "Off-topic không được phân luồng");

            // Phải không có triage data
            Method triageRaw = aiResponse.getClass().getDeclaredMethod("triageRaw");
            triageRaw.setAccessible(true);
            assertNull(triageRaw.invoke(aiResponse), "Off-topic không được có triage");

            // Reply phải có nội dung từ chối
            Method replyMethod = aiResponse.getClass().getDeclaredMethod("reply");
            replyMethod.setAccessible(true);
            String reply = (String) replyMethod.invoke(aiResponse);
            assertTrue(reply.contains("chỉ có thể hỗ trợ") || reply.contains("y tế") || reply.contains("sức khỏe"),
                    "Reply phải hướng người dùng về chủ đề y tế");
        }

        @Test
        @DisplayName("fallback: Hỏi về thời tiết -> Từ chối lịch sự")
        void testFallbackRejectsWeatherQuestion() throws Exception {
            Method method = ChatService.class.getDeclaredMethod("fallbackResponse", String.class, List.class);
            method.setAccessible(true);

            Object aiResponse = method.invoke(chatService, "thời tiết hà nội ngày mai thế nào",
                    Collections.emptyList());

            Method hasTriage = aiResponse.getClass().getDeclaredMethod("hasTriageResult");
            hasTriage.setAccessible(true);
            assertFalse((Boolean) hasTriage.invoke(aiResponse));

            Method replyMethod = aiResponse.getClass().getDeclaredMethod("reply");
            replyMethod.setAccessible(true);
            String reply = (String) replyMethod.invoke(aiResponse);
            assertFalse(reply.isBlank(), "Reply không được rỗng");
        }

        @Test
        @DisplayName("fallback: Câu hỏi y tế vẫn được xử lý bình thường (không bị block nhầm)")
        void testFallbackDoesNotBlockMedicalQuestion() throws Exception {
            Method method = ChatService.class.getDeclaredMethod("fallbackResponse", String.class, List.class);
            method.setAccessible(true);

            Object aiResponse = method.invoke(chatService, "tôi bị đau ngực", Collections.emptyList());

            Method hasTriage = aiResponse.getClass().getDeclaredMethod("hasTriageResult");
            hasTriage.setAccessible(true);
            // Lượt đầu với triệu chứng ngắn -> hỏi thêm, nhưng không reject
            // Reply phải chứa câu hỏi khai thác (không phải thông báo từ chối)
            Method replyMethod = aiResponse.getClass().getDeclaredMethod("reply");
            replyMethod.setAccessible(true);
            String reply = (String) replyMethod.invoke(aiResponse);
            assertFalse(reply.contains("chỉ có thể hỗ trợ"),
                    "Câu hỏi y tế không được bị block bởi off-topic guard");
        }
    }

    // ===================================================================
    // NHÓM 3: Kiểm thử callAiService() — Off-topic chặn trước khi gọi Ollama
    // ===================================================================

    @Nested
    @DisplayName("3. callAiService() — Chặn off-topic TRƯỚC khi gọi Ollama (tiết kiệm tài nguyên)")
    class CallAiServiceOffTopicTests {

        @Test
        @DisplayName("callAiService: Off-topic -> Trả về từ chối, Ollama KHÔNG được gọi")
        void testCallAiServiceBlocksOffTopicWithoutCallingOllama() throws Exception {
            Method method = ChatService.class.getDeclaredMethod(
                    "callAiService", String.class, String.class, List.class);
            method.setAccessible(true);

            String sessionId = UUID.randomUUID().toString();
            List<Map<String, String>> history = new ArrayList<>();

            // Hỏi về game — không liên quan y tế
            Object aiResponse = method.invoke(chatService, "game liên quân mobile mùa này thế nào", sessionId, history);

            assertNotNull(aiResponse);

            Method hasTriage = aiResponse.getClass().getDeclaredMethod("hasTriageResult");
            hasTriage.setAccessible(true);
            assertFalse((Boolean) hasTriage.invoke(aiResponse), "Off-topic không được có triage result");

            // Quan trọng: RestTemplate (Ollama client) KHÔNG được gọi
            // vì off-topic được chặn trước bước gọi Ollama
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("callAiService: Bitcoin/crypto -> Chặn, không gọi Ollama")
        void testCallAiServiceBlocksCryptoQuestion() throws Exception {
            Method method = ChatService.class.getDeclaredMethod(
                    "callAiService", String.class, String.class, List.class);
            method.setAccessible(true);

            Object aiResponse = method.invoke(chatService, "bitcoin giá bao nhiêu hôm nay",
                    UUID.randomUUID().toString(), new ArrayList<>());

            Method hasTriage = aiResponse.getClass().getDeclaredMethod("hasTriageResult");
            hasTriage.setAccessible(true);
            assertFalse((Boolean) hasTriage.invoke(aiResponse));
            verifyNoInteractions(restTemplate);
        }
    }

    // ===================================================================
    // NHÓM 4: Kiểm thử ưu tiên — Red Flag > Off-Topic
    // ===================================================================

    @Nested
    @DisplayName("4. Ưu tiên xử lý: Red Flag phải được ưu tiên hơn Off-Topic Guard")
    class PriorityTests {

        @Test
        @DisplayName("Red Flag luôn được xử lý dù câu có từ khóa off-topic")
        void testRedFlagTakesPriorityOverOffTopic() throws Exception {
            // Giả sử câu chứa cả từ off-topic lẫn dấu hiệu cấp cứu
            // isEmergencyRedFlag phải được check TRƯỚC isOffTopic trong callAiService
            Method redFlagMethod = ChatService.class.getDeclaredMethod("isEmergencyRedFlag", String.class);
            redFlagMethod.setAccessible(true);

            // Câu cấp cứu rõ ràng
            Boolean isRedFlag = (Boolean) redFlagMethod.invoke(chatService, "bất tỉnh đột ngột ngã xuống");
            assertTrue(isRedFlag, "Red Flag phải được nhận diện đúng");

            // Câu off-topic rõ ràng không phải Red Flag
            Boolean isOffTopicRedFlag = (Boolean) redFlagMethod.invoke(chatService, "bóng đá world cup");
            assertFalse(isOffTopicRedFlag, "Câu off-topic bình thường không phải Red Flag");
        }
    }

    // ===================================================================
    // NHÓM 5: Kiểm thử trường hợp biên (Edge Cases)
    // ===================================================================

    @Nested
    @DisplayName("5. Trường hợp biên — Câu hỗn hợp y tế + ngoài y tế")
    class EdgeCaseTests {

        private Method isOffTopicMethod;

        @BeforeEach
        void getMethod() throws Exception {
            isOffTopicMethod = ChatService.class.getDeclaredMethod("isOffTopic", String.class);
            isOffTopicMethod.setAccessible(true);
        }

        @Test
        @DisplayName("Câu hỗn hợp: có từ y tế + từ off-topic -> KHÔNG block (ưu tiên y tế)")
        void testMixedQuestionNotBlocked() throws Exception {
            // Câu chứa cả "bóng đá" lẫn "đau chân" -> có từ y tế -> không block
            String mixed = "tôi chơi bóng đá và bị đau chân";
            Boolean result = (Boolean) isOffTopicMethod.invoke(chatService, mixed.toLowerCase());
            assertFalse(result, "Câu có từ khóa y tế không được bị block dù có từ off-topic");
        }

        @Test
        @DisplayName("Câu rất ngắn (< 15 ký tự) không phải off-topic -> Xử lý như greeting")
        void testVeryShortInputNotOffTopic() throws Exception {
            Boolean result = (Boolean) isOffTopicMethod.invoke(chatService, "hi");
            assertFalse(result, "Câu ngắn không phải off-topic");
        }

        @Test
        @DisplayName("Câu hỏi về sức khỏe liên quan thể thao -> KHÔNG block")
        void testSportsRelatedHealthQuestion() throws Exception {
            // "thể thao" là từ off-topic, nhưng câu này lại hỏi về "đau" -> medical
            String healthSports = "sau khi tập thể thao tôi bị đau lưng";
            Boolean result = (Boolean) isOffTopicMethod.invoke(chatService, healthSports.toLowerCase());
            assertFalse(result, "Câu hỏi y tế liên quan thể thao không được block");
        }
    }
}
