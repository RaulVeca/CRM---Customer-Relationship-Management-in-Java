package crm.web.controller;

import crm.web.ai.AiService;
import crm.web.ai.dto.ChatMessage;
import crm.web.ai.dto.CourseRecommendation;
import crm.web.ai.dto.VisitorProfile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the AI features: course recommendations (#4), the chatbot
 * (#6) and the sales assistant (#7) - all powered by Claude.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService ai;

    public AiController(AiService ai) {
        this.ai = ai;
    }

    @GetMapping("/status")
    public Map<String, Boolean> status() {
        return Map.of("enabled", ai.isEnabled());
    }

    @GetMapping("/recommendations/company/{companyId}")
    public List<CourseRecommendation> recommendations(@PathVariable Long companyId) {
        return ai.recommendCoursesForCompany(companyId);
    }

    @PostMapping("/recommendations/visitor")
    public List<CourseRecommendation> visitorRecommendations(@RequestBody VisitorProfile profile) {
        return ai.recommendCoursesForVisitor(profile);
    }

    @GetMapping("/sales/opportunity/{opportunityId}")
    public Map<String, String> salesAssist(@PathVariable Long opportunityId) {
        return Map.of("advice", ai.salesAssistForOpportunity(opportunityId));
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        String reply = ai.chat(request.messages());
        return Map.of("reply", reply);
    }

    public record ChatRequest(List<ChatMessage> messages) {}
}
