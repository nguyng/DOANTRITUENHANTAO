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

        // 2. Goi Local AI va do thoi gian
        long startTime = System.currentTimeMillis();
        List<Map<String, String>> history = getHistory(sessionId);
        AiResponse aiResult = callAiService(userText, sessionId.toString(), history);
        int processingMs = (int) (System.currentTimeMillis() - startTime);

        // 3. Luu tin nhan cua BOT
        ChatMessage botMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.BOT)
                .content(aiResult.reply())
                .messageType(ChatMessage.MessageType.TEXT)
                .build();
        botMsg = chatMessageRepository.save(botMsg);

        // 4. Chuan bi ket qua phan luong (neu AI da thu thap du thong tin mau chot)
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

                // Luu vao nhat ky AI
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

        // Luu tin SYSTEM
        ChatMessage sysMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.SYSTEM)
                .content("✅ Đã đăng ký khám thành công. Phiếu số: " + ticket.getTicketNumber())
                .build();
        chatMessageRepository.save(sysMsg);

        return ticket;
    }

    // ======================== Multi-Turn Clinical Triage Prompts ========================

    /**
     * PROMPT GIAI DOAN 1: Khai thac chuyen sau trieu chung mau chot de phan biet benh.
     * Bat buoc khong duoc chot khoa voi vang. Phai dat tu 2 den 3 cau hoi phan biet.
     */
    private static final String SYSTEM_PROMPT_PROBE = """
            Bạn là Trợ Lý Tiếp Đón & Phân Luồng Phòng Khám của Bệnh viện MedAssist.
            Mục tiêu: Đảm bảo phân luồng vào ĐÚNG CHUYÊN KHOA với độ chính xác cao nhất.
            Rất nhiều căn bệnh có triệu chứng giống nhau (Ví dụ: đau ngực có thể do Tim mạch, Hô hấp, Tiêu hóa hoặc Cơ xương khớp; đau đầu có thể do Thần kinh, Huyết áp hoặc Xoang; đau bụng có thể do Tiêu hóa, Cấp cứu hoặc Phụ khoa).

            NGUYÊN TẮC BẮT BUỘC Ở BƯỚC NÀY:
            1. TUYỆT ĐỐI CHƯA CHỐT KHOA KHÁM (Bắt buộc: has_triage_result = false, triage = null).
            2. Trong `reply`: Lắng nghe ân cần và đặt từ 2 đến 3 câu hỏi lâm sàng THEN CHỐT để thu thập TRIỆU CHỨNG MẤU CHỐT:
               - Vị trí cụ thể và tính chất cảm giác (đau nhói khi hít thở, nóng rát hay đè nặng lồng ngực; đau quặn từng cơn hay âm ỉ)?
               - Hoàn cảnh xuất hiện hoặc yếu tố tăng/giảm (khi hít thở sâu, khi vận động gắng sức, sau khi ăn no, hay khi xoay vặn người)?
               - Triệu chứng đi kèm (sốt, ho có đờm, khó thở, vã mồ hôi, ợ chua, buồn nôn, tê bì tay chân)?
               - Thời gian kéo dài bao lâu (mới xuất hiện vài giờ, vài ngày hay đã lâu)?

            CHỈ TRẢ VỀ DUY NHẤT ĐỐI TƯỢNG JSON:
            {
              "reply": "câu hỏi khai thác triệu chứng mấu chốt ân cần, súc tích",
              "has_triage_result": false,
              "triage": null
            }
            """;

    /**
     * PROMPT GIAI DOAN 2: Tong hop cac trieu chung mau chot da thu thap va ra quyet dinh phan luong chinh xac.
     */
    private static final String SYSTEM_PROMPT_DECISION = """
            Bạn là Trợ Lý Tiếp Đón & Phân Luồng Phòng Khám của Bệnh viện MedAssist.
            Bệnh nhân đã cung cấp thông tin chi tiết qua các câu hỏi làm rõ. Bây giờ hãy TỔNG HỢP CÁC TRIỆU CHỨNG MẤU CHỐT và RA QUYẾT ĐỊNH PHÂN LUỒNG CHÍNH XÁC.

            DANH MỤC 11 CHUYÊN KHOA BẮT BUỘC CHỌN:
            - KTIM: Tim mạch (đau thắt ngực khi gắng sức/lan cánh tay, hồi hộp, tim đập nhanh, tăng huyết áp)
            - KHOHAP: Hô hấp (đau nhói ngực tăng khi ho/hít sâu, ho có đờm, ho khan kéo dài, sốt kèm ho, viêm phế quản/phổi)
            - KTIEUHOA: Tiêu hóa (cảm giác nóng rát sau xương ức sau ăn/nằm, ợ chua, trào ngược, đau dạ dày, đau bụng, buồn nôn, tiêu chảy)
            - KXUONGKHOP: Xương khớp (đau thành ngực/sụn sườn tăng khi ấn hoặc xoay người, đau khớp gối, thoái hóa khớp, đau lưng)
            - KTMH: Tai Mũi Họng (đau rát họng khi nuốt, nghẹt mũi, viêm xoang, amidan, ù tai)
            - KDALIEU: Da liễu (ngứa da, mề đay, mẩn đỏ, dị ứng ngoài da)
            - KMAT: Mắt (đau mắt, đỏ mắt, mờ mắt, cộm ngứa mắt)
            - KTHANKINH: Thần kinh (đau đầu dữ dội, chóng mặt tiền đình, mất ngủ mạn tính, tê yếu nửa người)
            - KCAPCUU: Cấp cứu (bất tỉnh, ngạt thở, đau ngực dữ dội kèm vã mồ hôi, co giật, tai nạn nặng)
            - KNHI: Trẻ em dưới 16 tuổi (mọi triệu chứng ở trẻ em)
            - KSAN: Sản phụ khoa (khám thai, phụ khoa, rối loạn kinh nguyệt)

            MỨC ĐỘ ƯU TIÊN:
            - M1: Cấp cứu ngay (đe dọa tính mạng)
            - M2: Khẩn cấp (sốt rất cao > 39 độ, đau nhiều)
            - M3: Ưu tiên (triệu chứng rõ ràng cần khám trong ngày)
            - M4: Thông thường (triệu chứng nhẹ, bệnh mạn tính)
            - M5: Không khẩn (khám định kỳ)

            YÊU CẦU ĐẦU RA:
            1. has_triage_result = true
            2. Điền đầy đủ triage:
               - department_code: mã khoa phù hợp nhất trong 11 mã trên
               - priority: M1/M2/M3/M4/M5
               - confidence: số nguyên 85-98
               - summary: tóm tắt ngắn gọn các triệu chứng mấu chốt đã thu thập được
               - advice: lời dặn chuẩn bị trước khi vào phòng khám
            3. Trong `reply`: Nêu rõ các triệu chứng mấu chốt đã giúp hệ thống quyết định đưa vào chuyên khoa này.

            CHỈ TRẢ VỀ DUY NHẤT ĐỐI TƯỢNG JSON:
            {
              "reply": "Dựa trên các triệu chứng mấu chốt bạn chia sẻ (...), hệ thống hướng dẫn bạn đăng ký tại chuyên khoa (...)",
              "has_triage_result": true,
              "triage": {
                "department_code": "MÃ_KHOA",
                "priority": "M3",
                "confidence": 92,
                "summary": "tóm tắt triệu chứng mấu chốt",
                "advice": "lời dặn trước khi khám"
              }
            }
            """;

    private AiResponse callAiService(String userText, String sessionId, List<Map<String, String>> history) {
        try {
            // 1. Kiem tra co do Cap cuu nguy kich (Red Flag)
            if (isEmergencyRedFlag(userText)) {
                return emergencyResponse(userText);
            }

            // 2. Xac dinh giai doan hoi thoai:
            // - Neu history.size() < 2 (moi la luot dau tien) va mo ta chua qua day du -> Giai doan 1 (Khai thac trieu chung mau chot)
            // - Neu history.size() >= 2 (benh nhan da tra loi cau hoi lam ro) hoac da mo ta rat chi tiet (> 120 ky tu) -> Giai doan 2 (Tong hop va chot khoa)
            boolean isInitialBrief = history.size() < 2 && !isVeryDetailed(userText);
            String selectedSystemPrompt = isInitialBrief ? SYSTEM_PROMPT_PROBE : SYSTEM_PROMPT_DECISION;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", selectedSystemPrompt));

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
            body.put("options", Map.of(
                    "temperature", 0.1,
                    "num_predict", 350
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String url = aiServiceUrl.replaceAll("/+$", "") + "/api/chat";

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getBody();

                @SuppressWarnings("unchecked")
                Map<String, Object> msg = (Map<String, Object>) data.get("message");
                if (msg != null && msg.containsKey("content")) {
                    String contentStr = String.valueOf(msg.get("content")).trim();

                    // Parse JSON tra ve tu LLM
                    Map<String, Object> parsed = objectMapper.readValue(
                            contentStr, new TypeReference<Map<String, Object>>() {});

                    String reply = String.valueOf(parsed.getOrDefault(
                            "reply", "Tôi đã ghi nhận thông tin của bạn."));
                    boolean hasTriage = Boolean.TRUE.equals(parsed.get("has_triage_result"));

                    @SuppressWarnings("unchecked")
                    Map<String, Object> triageRaw = (Map<String, Object>) parsed.get("triage");

                    return new AiResponse(reply, hasTriage, triageRaw);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết nối Local AI Ollama ({}) tại {}: {}", aiModelName, aiServiceUrl, e.getMessage());
        }

        // Fallback an toan khi Ollama offline
        return fallbackResponse(userText, history);
    }

    private boolean isEmergencyRedFlag(String text) {
        String lower = text.toLowerCase();
        return lower.contains("bất tỉnh") || lower.contains("ngất") || lower.contains("co giật")
                || lower.contains("ngừng thở") || lower.contains("tím tái") || lower.contains("hôn mê")
                || (lower.contains("đau ngực") && lower.contains("vã mồ hôi"))
                || (lower.contains("nôn") && lower.contains("ra máu"))
                || lower.contains("tai nạn") || lower.contains("mất máu nhiều");
    }

    private boolean isVeryDetailed(String text) {
        // Neu benh nhan da chu dong mo ta day du tu dau (> 120 ky tu va co tu noi trieu chung)
        String lower = text.toLowerCase();
        int countMarkers = 0;
        if (lower.contains("kèm") || lower.contains("và") || lower.contains("từ hôm") || lower.contains("khi")) countMarkers++;
        if (lower.contains("đau") || lower.contains("sốt") || lower.contains("ho") || lower.contains("rát")) countMarkers++;
        return text.length() > 120 && countMarkers >= 2;
    }

    private AiResponse emergencyResponse(String userText) {
        Map<String, Object> triage = new HashMap<>();
        triage.put("department_code", "KCAPCUU");
        triage.put("priority", "M1");
        triage.put("confidence", 99);
        triage.put("summary", "CẢNH BÁO NGUY CẤP: " + userText);
        triage.put("advice", "Di chuyển ngay lập tức vào Khoa Cấp Cứu tại cổng chính Bệnh viện.");

        return new AiResponse("CẢNH BÁO: Tình trạng của bạn có dấu hiệu nguy kịch cần được bác sĩ xử trí ngay lập tức! Vui lòng vào thẳng Khoa Cấp Cứu hoặc nhờ người nhà hỗ trợ gấp.", true, triage);
    }

    private AiResponse fallbackResponse(String userText, List<Map<String, String>> history) {
        String lower = userText.toLowerCase();

        // Neu moi la cau chao hoi
        if (history.isEmpty() && (lower.contains("chào") || lower.contains("hello") || lower.length() < 15)) {
            return new AiResponse("Xin chào bạn! Tôi là Trợ lý Y tế AI tiếp đón của Bệnh viện MedAssist. Bạn đang gặp những triệu chứng khó chịu nào? Hãy mô tả cụ thể để tôi hỗ trợ nhé.", false, null);
        }

        // Neu la luot dau va chua du thong tin -> dat cau hoi khai thac them
        if (history.size() < 2 && !isVeryDetailed(userText)) {
            if (lower.contains("ngực")) {
                return new AiResponse("Cơn đau ngực của bạn có cảm giác như thế nào (đau nhói khi hít thở, nóng rát sau xương ức, hay đè nặng lồng ngực)? Cơn đau có lan đi đâu và có kèm sốt, ho đờm hay khó thở không?", false, null);
            } else if (lower.contains("bụng")) {
                return new AiResponse("Bạn đau ở vị trí nào của bụng (thượng vị gần ức, quanh rốn, hay bụng dưới)? Cơn đau quặn từng cơn hay âm ỉ, có kèm buồn nôn, ợ chua hay tiêu chảy không?", false, null);
            } else if (lower.contains("đầu") || lower.contains("chóng mặt")) {
                return new AiResponse("Cơn chóng mặt của bạn có cảm giác nhà cửa quay cuồng không? Đau đầu ở vùng nào (nửa đầu, trán hay sau gáy), có kèm buồn nôn, mất ngủ hay tê bì tay chân không?", false, null);
            } else {
                return new AiResponse("Bạn có thể chia sẻ thêm: Triệu chứng này xuất hiện từ khi nào? Có cảm giác tăng lên khi nào và có đi kèm sốt hay triệu chứng bất thường nào khác không?", false, null);
            }
        }

        // Luot thu 2 tro di: Tong hop phan luong
        String dept = "KHOHAP";
        if (lower.contains("bụng") || lower.contains("dạ dày") || lower.contains("nôn") || lower.contains("tiêu chảy") || lower.contains("ợ chua") || lower.contains("nóng rát")) {
            dept = "KTIEUHOA";
        } else if (lower.contains("ho") || lower.contains("phổi") || lower.contains("đờm") || lower.contains("hít sâu")) {
            dept = "KHOHAP";
        } else if (lower.contains("họng") || lower.contains("xoang") || lower.contains("tai") || lower.contains("mũi") || lower.contains("amidan")) {
            dept = "KTMH";
        } else if (lower.contains("xương") || lower.contains("khớp") || lower.contains("lưng") || lower.contains("gối") || lower.contains("sụn sườn")) {
            dept = "KXUONGKHOP";
        } else if (lower.contains("da") || lower.contains("ngứa") || lower.contains("mẩn") || lower.contains("dị ứng")) {
            dept = "KDALIEU";
        } else if (lower.contains("mắt") || lower.contains("nhìn")) {
            dept = "KMAT";
        } else if (lower.contains("ngực") || lower.contains("tim") || lower.contains("huyết áp") || lower.contains("hồi hộp")) {
            dept = "KTIM";
        } else if (lower.contains("đầu") || lower.contains("chóng mặt") || lower.contains("ngủ") || lower.contains("tê bì")) {
            dept = "KTHANKINH";
        }

        Map<String, Object> triage = new HashMap<>();
        triage.put("department_code", dept);
        triage.put("priority", "M3");
        triage.put("confidence", 85);
        triage.put("summary", "Đăng ký khám theo triệu chứng mấu chốt: " + userText);
        triage.put("advice", "Vui lòng giữ gìn sức khỏe và di chuyển đến phòng khám theo chỉ dẫn.");

        return new AiResponse("Dựa trên các triệu chứng mấu chốt bạn đã chia sẻ, hệ thống hướng dẫn bạn đăng ký tại chuyên khoa phù hợp dưới đây để bác sĩ chuyên khoa kiểm tra trực tiếp.", true, triage);
    }

    private String normalizeDeptCode(String code, String userText) {
        if (code == null) return "KHOHAP";
        String c = code.trim().toUpperCase().replace("KHOA_", "").replace("KHOA", "").replace("_", "");

        String lowerText = userText.toLowerCase();
        if ((lowerText.contains("bụng") || lowerText.contains("dạ dày") || lowerText.contains("tiêu chảy") || lowerText.contains("ợ chua") || lowerText.contains("trào ngược"))
                && !c.contains("TIEUHOA")) {
            return "KTIEUHOA";
        }

        return switch (c) {
            case "CAPCUU", "KCAPCUU" -> "KCAPCUU";
            case "SAN", "KSAN" -> "KSAN";
            case "TIM", "KTIM", "TIMMACH" -> "KTIM";
            case "COXUONGKHOP" -> "KXUONGKHOP";
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
            try {
                return departmentService.findByCode("K" + deptCode);
            } catch (Exception e2) {
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