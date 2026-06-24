package crm.web.ai.dto;

/**
 * One message in a chatbot conversation. {@code role} is "user" or "assistant".
 */
public record ChatMessage(String role, String content) {
}
