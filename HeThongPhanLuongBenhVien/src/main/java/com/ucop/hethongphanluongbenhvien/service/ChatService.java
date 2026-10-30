package com.ucop.hethongphanluongbenhvien.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.ucop.hethongphanluongbenhvien.entity.*;
import com.ucop.hethongphanluongbenhvien.repository.AiAnalysisLogRepository;
import com.ucop.hethongphanluongbenhvien.repository.ChatMessageRepository;
import com.ucop.hethongphanluongbenhvien.repository.ChatSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final RestTemplate restTemplate;
    private final AiAnalysisLogRepository aiAnalysisLogRepository;
    private final ObjectMapper objectMapper;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       TicketService ticketService,
                       UserService userService,
                       DepartmentService departmentService,
                       RestTemplate restTemplate,
                       AiAnalysisLogRepository aiAnalysisLogRepository,
                       ObjectMapper objectMapper) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.ticketService = ticketService;
        this.userService = userService;
        this.departmentService = departmentService;
        this.restTemplate = restTemplate;
        this.aiAnalysisLogRepository = aiAnalysisLogRepository;
        this.objectMapper = objectMapper;
    }

    /** URL Ollama AI service - cau hinh trong application.properties */
    @Value("${ai.service.url:http://localhost:11434}")
    private String aiServiceUrl;

    /** Ten model Ollama dang dung */
    @Value("${ai.model.name:qwen2.5-coder:3b}")
    private String aiModelName;

    // ======================== Session ========================

    /** Tao phien chat moi khi benh nhan mo trang /chat */
    @Transactional
    public ChatSession startSession(User patient) {
        ChatSession session = ChatSession.builder()
                .user(patient)
                .build();
        return chatSessionRepository.save(session);
    }

    @Transactional
    public ChatSession closeSession(UUID sessionId) {
        ChatSession session = findSessionById(sessionId);
        session.setEndedAt(LocalDateTime.now());
        return chatSessionRepository.save(session);
    }

    public ChatSession findSessionById(UUID id) {
        return chatSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session khong ton tai: " + id));
    }

    public List<ChatMessage> getMessages(UUID sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    // ======================== Nhan tin ========================

    /**
     * Benh nhan gui tin - tu dong goi Local AI (Ollama qwen2.5-coder:3b) roi luu ca 2 tin (USER + BOT).
     * @return tin nhan tra loi cua BOT
     */
    @Transactional
    public ChatSendResult sendMessage(UUID sessionId, String userText) {
        ChatSession session = findSessionById(sessionId);

        // 1. Luu tin nhan cua benh nhan
        ChatMessage userMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.USER)
                .content(userText)
                .build();
        chatMessageRepository.save(userMsg);

        // 2. Goi Local AI Ollama va do thoi gian xu ly
        long startTime = System.currentTimeMillis();
        AiResponse aiResult = callAiService(userText, sessionId.toString(), getHistory(sessionId));
        int processingMs = (int) (System.currentTimeMillis() - startTime);

        // 3. Luu tin nhan cua BOT
        ChatMessage botMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.BOT)
                .content(aiResult.reply())
                .messageType(ChatMessage.MessageType.TEXT)
                .build();
        botMsg = chatMessageRepository.save(botMsg);

        // 4. Chuan bi ket qua phan luong (neu co)
        TriageResult triageResult = null;
        if (aiResult.hasTriageResult() && aiResult.triageRaw() != null) {
            Map<String, Object> t = aiResult.triageRaw();
            String rawDeptCode = String.valueOf(t.getOrDefault("department_code", ""));
            String deptCode = normalizeDeptCode(rawDeptCode, userText);
            String priority = String.valueOf(t.getOrDefault("priority", "M3"));
            int confidence = 85;
            try {
                if (t.get("confidence") != null) {
                    confidence = ((Number) t.get("confidence")).intValue();
                }
            } catch (Exception ignored) {}

            String summary = String.valueOf(t.getOrDefault("summary", ""));
            String advice = String.valueOf(t.getOrDefault("advice", ""));

            try {
                Department dept = resolveDepartment(deptCode);
                String roomPart = (dept.getRoomNumbers() != null && !dept.getRoomNumbers().isBlank())
                        ? " · Phòng " + dept.getRoomNumbers() : "";
                String locPart = (dept.getBuilding() != null && !dept.getBuilding().isBlank())
                        ? " · " + dept.getBuilding() : "";

                triageResult = new TriageResult(
                        dept.getId(),
                        dept.getName(),
                        dept.getName() + roomPart + locPart,
                        dept.getLocationGuide(),
                        priority,
                        confidence,
                        summary,
                        advice
                );

                // Luu log phan tich vao AI_ANALYSIS_LOGS de Admin co the theo doi tren /admin/logs
                try {
                    AiAnalysisLog logEntity = AiAnalysisLog.builder()
                            .message(botMsg)
                            .aiModelUsed(aiModelName)
                            .inputText(userText)
                            .predictedDept(dept.getName() + " (" + dept.getCode() + ")")
                            .confidenceScore(BigDecimal.valueOf(confidence).divide(BigDecimal.valueOf(100)))
                            .sentiment("NEUTRAL")
                            .processingMs(processingMs)
                            .build();
                    aiAnalysisLogRepository.save(logEntity);
                } catch (Exception ex) {
                    log.warn("Khong the luu AiAnalysisLog: {}", ex.getMessage());
                }

            } catch (Exception e) {
                log.warn("Khong tim thay khoa phu hop cho ma: {}", deptCode);
            }
        }

        return new ChatSendResult(botMsg, triageResult);
    }

    private List<Map<String, String>> getHistory(UUID sessionId) {
        return getMessages(sessionId).stream()
                .map(msg -> Map.of(
                        "role", msg.getSenderRole() == ChatMessage.SenderRole.USER ? "user" : "assistant",
                        "content", msg.getContent()
                ))
                .toList();
    }

    /**
     * Benh nhan xac nhan dang ky sau khi AI de xuat phan luong.
     * Tao Ticket chinh thuc va gan vao session.
     */
    @Transactional
    public Ticket confirmRegistration(UUID sessionId, UUID departmentId,
                                      Ticket.Priority priority, String aiSummary, Integer confidence) {
        ChatSession session = findSessionById(sessionId);
        User patient = session.getUser();

        // Phat phieu kham
        Ticket ticket = ticketService.issueTicket(patient, departmentId, priority, aiSummary, confidence);

        // Gan ticket vao session
        session.setTicket(ticket);
        session.setEndedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        // Luu tin SYSTEM thong bao hoan tat
        ChatMessage sysMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.SYSTEM)
                .content("✅ Đã đăng ký khám thành công. Phiếu số: " + ticket.getTicketNumber())
                .build();
        chatMessageRepository.save(sysMsg);

        return ticket;
    }

    // ======================== Goi Local AI (Ollama) ========================

    private static final String SYSTEM_PROMPT = """
            Bạn là Trợ lý Y tế AI tiếp đón của Bệnh viện MedAssist.
            Nhiệm vụ: Lắng nghe bệnh nhân mô tả triệu chứng, trả lời ân cần, giải thích và phân luồng vào đúng chuyên khoa của bệnh viện.

            DANH SÁCH MÃ CHUYÊN KHOA BẮT BUỘC CHỌN:
            1. KTIEUHOA: Đau dạ dày, đau bụng, buồn nôn, tiêu chảy, ợ chua, trào ngược, ngộ độc thức ăn, táo bón.
            2. KHOHAP: Ho có đờm, ho khan, viêm phế quản, viêm phổi, khó thở, sốt kèm ho.
            3. KTMH: Đau rát họng, ngứa họng, viêm xoang, nghẹt mũi, viêm amidan, ù tai, chảy mủ tai.
            4. KXUONGKHOP: Đau nhức xương, đau khớp gối, thoái hóa khớp, đau lưng, thoát vị đĩa đệm, mỏi vai gáy.
            5. KDALIEU: Ngứa da, dị ứng da, nổi mề đay, mẩn đỏ, vảy nến, chàm, mụn nhọt ngoài da.
            6. KMAT: Đau mắt, đỏ mắt, mờ mắt, cộm ngứa mắt, viêm giác mạc, đo khúc xạ.
            7. KTIM: Đau tức ngực, hồi hộp, đánh trống ngực, tim đập nhanh, tăng huyết áp, suy tim.
            8. KTHANKINH: Đau đầu dữ dội, đau nửa đầu, chóng mặt mất thăng bằng, mất ngủ kinh niên, tê bì tay chân.
            9. KCAPCUU: Bất tỉnh, ngất xỉu, co giật, khó thở dữ dội tím tái, đau ngực dữ dội vã mồ hôi, tai nạn thương tích nặng.
            10. KNHI: Tất cả bệnh nhân là trẻ em dưới 16 tuổi.
            11. KSAN: Khám thai, phụ khoa, rối loạn kinh nguyệt.

            MỨC ĐỘ ƯU TIÊN (priority):
            - M1: Cấp cứu ngay (đe dọa tính mạng)
            - M2: Khẩn cấp (triệu chứng nặng, sốt cao > 39 độ, đau dữ dội)
            - M3: Ưu tiên (triệu chứng rõ ràng cần khám trong ngày)
            - M4: Thông thường (triệu chứng nhẹ, ổn định)
            - M5: Không khẩn (khám sức khỏe định kỳ)

            QUY TẮC PHẢN HỒI:
            - Nếu chỉ chào hỏi hoặc chưa có triệu chứng bệnh: has_triage_result = false, triage = null, reply ân cần hỏi thêm triệu chứng.
            - Nếu có triệu chứng bệnh: has_triage_result = true, triage bắt buộc chứa:
              + department_code: một trong các mã chính xác ở trên (KTIEUHOA, KHOHAP, KTMH, KXUONGKHOP, KDALIEU, KMAT, KTIM, KTHANKINH, KCAPCUU, KNHI, KSAN)
              + priority: M1/M2/M3/M4/M5
              + confidence: số nguyên 75 - 98
              + summary: tóm tắt triệu chứng ngắn gọn
              + advice: lời khuyên chăm sóc ban đầu

            BẮT BUỘC TRẢ VỀ DUY NHẤT 1 ĐỐI TƯỢNG JSON:
            {
              "reply": "câu trả lời ân cần cho bệnh nhân",
              "has_triage_result": true/false,
              "triage": {
                "department_code": "MÃ_KHOA",
                "priority": "M1|M2|M3|M4|M5",
                "confidence": 85,
                "summary": "tóm tắt",
                "advice": "lời khuyên"
              }
            }
            """;

    private AiResponse callAiService(String userText, String sessionId, List<Map<String, String>> history) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Xay dung danh sach message cho Ollama chat API
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

            // Lay toi da 6 tin nhan gan nhat de giu ngu canh
            int startIdx = Math.max(0, history.size() - 6);
            for (int i = startIdx; i < history.size(); i++) {
                messages.add(history.get(i));
            }
            messages.add(Map.of("role", "user", "content", userText));

            Map<String, Object> body = new HashMap<>();
            body.put("model", aiModelName);
            body.put("messages", messages);
            body.put("format", "json");
            body.put("stream", false);
            body.put("options", Map.of("temperature", 0.1));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String url = aiServiceUrl.replaceAll("/+$", "") + "/api/chat";

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getBody();

                // Ollama tra ve: { "message": { "role": "assistant", "content": "{...}" } }
                @SuppressWarnings("unchecked")
                Map<String, Object> msg = (Map<String, Object>) data.get("message");
                if (msg != null && msg.containsKey("content")) {
                    String contentStr = String.valueOf(msg.get("content")).trim();

                    // Parse JSON ben trong content cua LLM
                    Map<String, Object> parsed = objectMapper.readValue(
                            contentStr, new TypeReference<Map<String, Object>>() {});

                    String reply = String.valueOf(parsed.getOrDefault(
                            "reply", "Tôi đã ghi nhận triệu chứng của bạn và đề xuất phân luồng khám bệnh."));
                    boolean hasTriage = Boolean.TRUE.equals(parsed.get("has_triage_result"));

                    @SuppressWarnings("unchecked")
                    Map<String, Object> triageRaw = (Map<String, Object>) parsed.get("triage");

                    return new AiResponse(reply, hasTriage, triageRaw);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết nối Local AI Ollama ({}) tại {}: {}", aiModelName, aiServiceUrl, e.getMessage());
        }

        // Fallback khi Ollama offline hoac chua khoi dong
        return fallbackResponse(userText);
    }

    private AiResponse fallbackResponse(String userText) {
        String lower = userText.toLowerCase();
        boolean hasSymptom = lower.contains("đau") || lower.contains("sốt") || lower.contains("ho")
                || lower.contains("mệt") || lower.contains("khó thở") || lower.contains("ngứa")
                || lower.contains("bệnh") || lower.contains("khám");

        if (!hasSymptom) {
            return new AiResponse("Xin chào! Tôi là Trợ lý Y tế AI của bệnh viện. Bạn đang gặp triệu chứng khó chịu gì? Hãy mô tả để tôi hỗ trợ phân luồng khám bệnh nhé.", false, null);
        }

        // Heuristic fallback don gian dua tren tu khoa
        String dept = "KHOA_NOI";
        if (lower.contains("bụng") || lower.contains("dạ dày") || lower.contains("nôn") || lower.contains("tiêu chảy")) {
            dept = "KTIEUHOA";
        } else if (lower.contains("ho") || lower.contains("phổi") || lower.contains("đờm") || lower.contains("thở")) {
            dept = "KHOHAP";
        } else if (lower.contains("họng") || lower.contains("xoang") || lower.contains("tai") || lower.contains("mũi")) {
            dept = "KTMH";
        } else if (lower.contains("xương") || lower.contains("khớp") || lower.contains("lưng") || lower.contains("gối")) {
            dept = "KXUONGKHOP";
        } else if (lower.contains("da") || lower.contains("ngứa") || lower.contains("mẩn") || lower.contains("dị ứng")) {
            dept = "KDALIEU";
        } else if (lower.contains("mắt") || lower.contains("nhìn")) {
            dept = "KMAT";
        } else if (lower.contains("ngực") || lower.contains("tim") || lower.contains("huyết áp")) {
            dept = "KTIM";
        } else if (lower.contains("đầu") || lower.contains("chóng mặt") || lower.contains("ngủ")) {
            dept = "KTHANKINH";
        }

        Map<String, Object> triage = new HashMap<>();
        triage.put("department_code", dept);
        triage.put("priority", "M3");
        triage.put("confidence", 80);
        triage.put("summary", "Đăng ký khám theo triệu chứng: " + userText);
        triage.put("advice", "Vui lòng giữ gìn sức khỏe và đến phòng khám theo chỉ dẫn.");

        return new AiResponse("Dựa trên mô tả triệu chứng của bạn, tôi đề xuất bạn đăng ký khám tại chuyên khoa phù hợp dưới đây.", true, triage);
    }

    /**
     * Chuan hoa va sua loi ma khoa (neu LLM sinh ra ma alias nhu KHOA_HOHAP, HOHAP...)
     */
    private String normalizeDeptCode(String code, String userText) {
        if (code == null) return "KHOHAP";
        String c = code.trim().toUpperCase().replace("KHOA_", "").replace("KHOA", "");

        // Kiem tra tu khoa de phong ngua LLM nham lan
        String lowerText = userText.toLowerCase();
        if ((lowerText.contains("bụng") || lowerText.contains("dạ dày") || lowerText.contains("tiêu chảy"))
                && !c.contains("TIEUHOA")) {
            return "KTIEUHOA";
        }

        return switch (c) {
            case "CAPCUU", "KCAPCUU" -> "KCAPCUU";
            case "SAN", "KSAN" -> "KSAN";
            case "TIM", "KTIM", "TIMMACH" -> "KTIM";
            case "TMH", "KTMH", "TAIMUIHONG" -> "KTMH";
            case "NHI", "KNHI" -> "KNHI";
            case "THANKINH", "KTHANKINH" -> "KTHANKINH";
            case "MAT", "KMAT" -> "KMAT";
            case "HOHAP", "KHOHAP" -> "KHOHAP";
            case "TIEUHOA", "KTIEUHOA" -> "KTIEUHOA";
            case "DALIEU", "KDALIEU" -> "KDALIEU";
            case "XUONGKHOP", "KXUONGKHOP" -> "KXUONGKHOP";
            default -> code.trim().toUpperCase();
        };
    }

    private Department resolveDepartment(String deptCode) {
        try {
            return departmentService.findByCode(deptCode);
        } catch (Exception e) {
            // Neu ma khoa chua co tien to K
            try {
                return departmentService.findByCode("K" + deptCode);
            } catch (Exception e2) {
                // Lay khoa hoat dong dau tien lam fallback
                List<Department> active = departmentService.findActive();
                if (!active.isEmpty()) {
                    return active.get(0);
                }
                throw e;
            }
        }
    }

    // ======================== DTO noi bo ========================

    record AiResponse(String reply, boolean hasTriageResult, Map<String, Object> triageRaw) {}

    public record ChatSendResult(ChatMessage message, TriageResult triage) {}

    public record TriageResult(
            UUID departmentId,
            String departmentName,
            String departmentFull,
            String locationGuide,
            String priority,
            Integer confidence,
            String summary,
            String advice
    ) {}
}