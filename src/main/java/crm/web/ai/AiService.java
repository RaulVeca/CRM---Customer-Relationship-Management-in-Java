package crm.web.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.entity.Course;
import crm.model.entity.Employee;
import crm.model.entity.Opportunity;
import crm.model.enums.ProfileArea;
import crm.web.ai.dto.ChatMessage;
import crm.web.ai.dto.CourseRecommendation;
import crm.web.ai.dto.VisitorProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the prompts and gathers the domain context for the three AI features:
 * course recommendations, the sales assistant, and the chatbot.
 */
@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final ClaudeClient claude;
    private final CrmFacade facade;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService(ClaudeClient claude, CrmFacade facade) {
        this.claude = claude;
        this.facade = facade;
    }

    public boolean isEnabled() {
        return claude.isEnabled();
    }

    // =====================================================
    // Feature #4 - AI course recommendations
    // =====================================================

    public List<CourseRecommendation> recommendCoursesForCompany(Long companyId) {
        Contact company = facade.getContact(companyId);
        List<Employee> employees = facade.getEmployeesForCompany(companyId);
        List<Course> courses = facade.getActiveCourses();

        String system = """
                You are a training-needs analyst for an IT training company. Given a corporate
                client, the profiles of its employees, and the available course catalog, you
                recommend the courses that best fit the company and its employees' development
                goals. Respond with ONLY a JSON array (no prose, no markdown fences) where each
                element is an object with keys: courseId (number, from the catalog), courseName
                (string), reason (string, one sentence on why it fits), matchScore (integer 0-100).
                Recommend at most 5 courses, ordered by matchScore descending.""";

        StringBuilder user = new StringBuilder();
        user.append("COMPANY:\n")
            .append("- Name: ").append(company.getCompanyName()).append("\n")
            .append("- Industry: ").append(company.getIndustry()).append("\n\n");

        user.append("EMPLOYEES (").append(employees.size()).append("):\n");
        if (employees.isEmpty()) {
            user.append("- (no employee profiles recorded)\n");
        } else {
            for (Employee e : employees) {
                user.append("- ").append(e.getFullName())
                    .append(" | role: ").append(nullSafe(e.getJobTitle()))
                    .append(" | works in: ").append(e.getWorkProfile() == null ? "?" : e.getWorkProfile().getLabel())
                    .append(" | interested in: ").append(profileLabels(e.getInterestProfiles()))
                    .append("\n");
            }
        }

        user.append("\nCOURSE CATALOG:\n");
        for (Course c : courses) {
            user.append("- id=").append(c.getId())
                .append(" | ").append(c.getName())
                .append(" | category: ").append(c.getCategory() == null ? "?" : c.getCategory().getLabel())
                .append(" | level: ").append(c.getLevel())
                .append("\n");
        }

        String response = claude.complete(system, user.toString());
        return parseRecommendations(response, courses);
    }

    /**
     * Personalised recommendations for an individual website visitor, based on a
     * short self-reported profile (interests, experience level, goal). Reuses the
     * same Claude completion + JSON parsing as the company recommendations.
     */
    public List<CourseRecommendation> recommendCoursesForVisitor(VisitorProfile profile) {
        List<Course> courses = facade.getActiveCourses();

        String system = """
                You are a friendly IT career advisor for an IT training company. Given an individual
                visitor's interests, experience level and learning goal, recommend the courses from
                the catalog that best fit them. Respond with ONLY a JSON array (no prose, no markdown
                fences) where each element is an object with keys: courseId (number, from the catalog),
                courseName (string), reason (string, one friendly sentence addressed directly to the
                visitor explaining why it fits), matchScore (integer 0-100). Recommend at most 4
                courses, ordered by matchScore descending.""";

        StringBuilder user = new StringBuilder();
        user.append("VISITOR PROFILE:\n")
            .append("- Interested in: ")
            .append(profile.interests() == null || profile.interests().isEmpty()
                    ? "-" : String.join(", ", profile.interests())).append("\n")
            .append("- Experience level: ").append(nullSafe(profile.experienceLevel())).append("\n")
            .append("- Learning goal: ").append(nullSafe(profile.goal())).append("\n\n");

        user.append("COURSE CATALOG:\n");
        for (Course c : courses) {
            user.append("- id=").append(c.getId())
                .append(" | ").append(c.getName())
                .append(" | category: ").append(c.getCategory() == null ? "?" : c.getCategory().getLabel())
                .append(" | level: ").append(c.getLevel())
                .append("\n");
        }

        String response = claude.complete(system, user.toString());
        return parseRecommendations(response, courses);
    }

    // =====================================================
    // Feature #7 - AI sales assistant
    // =====================================================

    public String salesAssistForOpportunity(Long opportunityId) {
        Opportunity opp = facade.getOpportunity(opportunityId);
        Contact client = opp.getClientId() != null ? safeContact(opp.getClientId()) : null;

        String system = """
                You are an experienced B2B sales coach for an IT training company. Given a sales
                opportunity, produce a short, actionable plan to move it forward and win the deal.
                Structure your answer as: 1) Situation summary, 2) Recommended next steps (3-4
                bullets), 3) A short draft follow-up email. Be concrete and concise.""";

        StringBuilder user = new StringBuilder();
        user.append("OPPORTUNITY:\n")
            .append("- Title: ").append(opp.getTitle()).append("\n")
            .append("- Stage: ").append(opp.getStage()).append("\n")
            .append("- Estimated value: ").append(opp.getEstimatedValue()).append("\n")
            .append("- Quoted value: ").append(opp.getQuotedValue()).append("\n")
            .append("- Estimated participants: ").append(opp.getEstimatedParticipants()).append("\n")
            .append("- Probability: ").append(opp.getProbabilityPercent()).append("%\n")
            .append("- Description: ").append(nullSafe(opp.getDescription())).append("\n");
        if (client != null) {
            user.append("\nCLIENT:\n")
                .append("- Company: ").append(nullSafe(client.getCompanyName())).append("\n")
                .append("- Industry: ").append(nullSafe(client.getIndustry())).append("\n");
        }

        return claude.complete(system, user.toString());
    }

    // =====================================================
    // Feature #6 - Chatbot
    // =====================================================

    public String chat(List<ChatMessage> history) {
        List<Course> courses = facade.getActiveCourses();
        String catalog = courses.stream()
                .map(c -> "- " + c.getName() + " (" + (c.getCategory() == null ? "?" : c.getCategory().getLabel()) + ")")
                .collect(Collectors.joining("\n"));

        String system = """
                You are the friendly virtual assistant of an IT training company. You help website
                visitors and staff with questions about the courses, enrollment, and corporate
                training. Be concise and helpful. If asked about something outside training, gently
                steer back. Here is the current course catalog:
                """ + "\n" + catalog;

        List<ClaudeClient.Turn> turns = new ArrayList<>();
        for (ChatMessage m : history) {
            turns.add(new ClaudeClient.Turn(m.role(), m.content()));
        }
        // The Anthropic API requires the conversation to start with a user turn.
        if (turns.isEmpty() || !"user".equalsIgnoreCase(turns.get(0).role())) {
            throw new IllegalArgumentException("Conversation must start with a user message");
        }
        return claude.chat(system, turns);
    }

    // =====================================================
    // Translation - live UI translation into any language
    // =====================================================

    /**
     * Translates a batch of UI strings into {@code targetLanguage} (an English
     * language name such as "French", "Japanese", "Arabic"). Returns a list the
     * same size and order as the input; on any parsing problem the original
     * strings are returned so the UI never breaks.
     */
    public List<String> translate(List<String> texts, String targetLanguage) {
        if (texts == null || texts.isEmpty()) return List.of();
        if (targetLanguage == null || targetLanguage.isBlank()) return texts;

        String system = """
                You are a professional translator localizing a web application's user interface.
                You are given a JSON array of UI strings. Translate every element into %s.

                Rules:
                - Return ONLY a JSON array of strings — no prose, no markdown fences, no keys.
                - The output array MUST have exactly the same length and order as the input.
                - Translate naturally and idiomatically, as a native speaker would phrase UI text.
                - Do NOT translate: proper nouns, brand/product names (e.g. "TrainingIT"),
                  people's names, company names, email addresses, URLs, code, or file names.
                - Keep numbers, dates, currency symbols, punctuation and emoji unchanged.
                - Preserve any placeholder tokens (e.g. {name}, %%s, :id) exactly.
                - If a string has no meaningful translation, return it unchanged.
                - Match the capitalization style and trailing punctuation of the source.
                """.formatted(targetLanguage);

        String user;
        try {
            user = objectMapper.writeValueAsString(texts);
        } catch (Exception e) {
            logger.warn("Could not serialize texts for translation", e);
            return texts;
        }

        String response = claude.complete(system, user);
        String json = extractJsonArray(response);
        try {
            String[] items = objectMapper.readValue(json, String[].class);
            if (items.length != texts.size()) {
                logger.warn("Translation length mismatch ({} in, {} out) — returning originals",
                        texts.size(), items.length);
                return texts;
            }
            List<String> out = new ArrayList<>(items.length);
            for (int i = 0; i < items.length; i++) {
                out.add(items[i] == null ? texts.get(i) : items[i]);
            }
            return out;
        } catch (Exception e) {
            logger.warn("Could not parse translation response as JSON array — returning originals. Raw: {}", response, e);
            return texts;
        }
    }

    // =====================================================
    // Helpers
    // =====================================================

    private List<CourseRecommendation> parseRecommendations(String response, List<Course> courses) {
        String json = extractJsonArray(response);
        try {
            CourseRecommendation[] items = objectMapper.readValue(json, CourseRecommendation[].class);
            return List.of(items);
        } catch (Exception e) {
            logger.warn("Could not parse AI recommendations as JSON, returning empty list. Raw: {}", response, e);
            return List.of();
        }
    }

    private static String extractJsonArray(String text) {
        if (text == null) return "[]";
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "[]";
    }

    private Contact safeContact(Long id) {
        try {
            return facade.getContact(id);
        } catch (Exception e) {
            return null;
        }
    }

    private static String profileLabels(List<ProfileArea> profiles) {
        if (profiles == null || profiles.isEmpty()) return "-";
        return profiles.stream().map(ProfileArea::getLabel).collect(Collectors.joining(", "));
    }

    private static String nullSafe(String s) {
        return s == null ? "-" : s;
    }
}
