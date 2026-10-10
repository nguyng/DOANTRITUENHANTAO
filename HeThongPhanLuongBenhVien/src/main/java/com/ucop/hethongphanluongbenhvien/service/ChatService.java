package com.ucop.hethongphanluongbenhvien.service;

import com.ucop.hethongphanluongbenhvien.entity.ChatMessage;
import com.ucop.hethongphanluongbenhvien.entity.ChatSession;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.ChatMessageRepository;
import com.ucop.hethongphanluongbenhvien.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;

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

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       TicketService ticketService,
                       UserService userService,
                       DepartmentService departmentService,
                       RestTemplateBuilder restTemplateBuilder) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.ticketService = ticketService;
        this.userService = userService;
        this.departmentService = departmentService;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    /** URL FastAPI AI service — cấu hình trong application.properties */
    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    // ── Session ─────────────────────────────────────────────────────────────────

    /** Tạo phiên chat mới khi bệnh nhân mở trang /chat */
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
                .orElseThrow(() -> new RuntimeException("Session không tồn tại: " + id));
    }

    public List<ChatMessage> getMessages(UUID sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    // ── Nhắn tin ────────────────────────────────────────────────────────────────

    /**
     * Bệnh nhân gửi tin — tự động gọi AI rồi lưu cả 2 tin (USER + BOT).
     * @return tin nhắn trả lời của BOT
     */
    @Transactional
    public ChatSendResult sendMessage(UUID sessionId, String userText) {
        ChatSession session = findSessionById(sessionId);

        // 1. Lưu tin nhắn của bệnh nhân
        ChatMessage userMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.USER)
                .content(userText)
                .build();
        chatMessageRepository.save(userMsg);

        // 2. Gọi AI và lấy phản hồi
        AiResponse aiResult = callAiService(userText, sessionId.toString(), getHistory(sessionId));

        // 3. Lưu tin nhắn của BOT
        ChatMessage botMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.BOT)
                .content(aiResult.reply())
                .messageType(aiResult.hasTriageResult() ? ChatMessage.MessageType.TEXT : ChatMessage.MessageType.TEXT)
                .build();
        botMsg = chatMessageRepository.save(botMsg);

        // 4. Chuẩn bị kết quả trả về
        TriageResult triageResult = null;
        if (aiResult.hasTriageResult() && aiResult.triageRaw() != null) {
            Map<String, Object> t = aiResult.triageRaw();
            String deptCode = (String) t.get("department_code");
            String priority = (String) t.getOrDefault("priority", "M3");
            Integer confidence = t.get("confidence") != null ? ((Number) t.get("confidence")).intValue() : 0;
            String summary = (String) t.get("summary");
            String advice = (String) t.get("advice");

            try {
                var dept = departmentService.findByCode(deptCode);
                triageResult = new TriageResult(
                        dept.getId(),
                        dept.getName(),
                        dept.getName() + " · Phòng " + dept.getRoomNumber() + " · " + dept.getLocation(),
                        dept.getLocationGuide(),
                        priority,
                        confidence,
                        summary,
                        advice
                );
            } catch (Exception e) {
                log.warn("Không tìm thấy khoa với mã: {}", deptCode);
            }
        }

        return new ChatSendResult(botMsg, triageResult);
    }

    private List<Map<String, String>> getHistory(UUID sessionId) {
        return getMessages(sessionId).stream()
                .map(msg -> Map.of(
                        "role", msg.getSenderRole() == ChatMessage.SenderRole.USER ? "user" : "bot",
                        "content", msg.getContent()
                ))
                .toList();
    }

    /**
     * Bệnh nhân xác nhận đăng ký sau khi AI đề xuất phân luồng.
     * Tạo Ticket chính thức và gắn vào session.
     */
    @Transactional
    public Ticket confirmRegistration(UUID sessionId, UUID departmentId,
                                      Ticket.Priority priority, String aiSummary, Integer confidence) {
        ChatSession session = findSessionById(sessionId);
        User patient = session.getUser();

        // Phát phiếu khám
        Ticket ticket = ticketService.issueTicket(patient, departmentId, priority, aiSummary, confidence);

        // Gắn ticket vào session
        session.setTicket(ticket);
        session.setEndedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        // Lưu tin SYSTEM thông báo hoàn tất
        ChatMessage sysMsg = ChatMessage.builder()
                .session(session)
                .senderRole(ChatMessage.SenderRole.SYSTEM)
                .content("✅ Đã đăng ký khám thành công. Phiếu số: " + ticket.getTicketNumber())
                .build();
        chatMessageRepository.save(sysMsg);

        return ticket;
    }

    // ── Gọi FastAPI AI ──────────────────────────────────────────────────────────

    private AiResponse callAiService(String userText, String sessionId, List<Map<String, String>> history) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("message", userText);
            body.put("session_id", sessionId);
            body.put("history", history);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/chat", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getBody();
                String reply       = String.valueOf(data.getOrDefault("reply", "Xin lỗi, tôi chưa hiểu. Bạn có thể mô tả lại triệu chứng không?"));
                boolean hasTriage  = Boolean.TRUE.equals(data.get("has_triage_result"));
                
                @SuppressWarnings("unchecked")
                Map<String, Object> triageRaw = (Map<String, Object>) data.get("triage");

                return new AiResponse(reply, hasTriage, triageRaw);
            }
        } catch (Exception e) {
            log.error("Lỗi kết nối AI service: {}", e.getMessage());
        }
        // Fallback khi AI service offline
        return new AiResponse("Xin chào! Bạn đang gặp triệu chứng gì? Hãy mô tả để tôi hỗ trợ phân luồng khám bệnh nhé.", false, null);
    }

    // ── DTO nội bộ ──────────────────────────────────────────────────────────────

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
