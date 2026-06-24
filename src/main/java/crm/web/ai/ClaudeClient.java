package crm.web.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thin wrapper around the official Anthropic Java SDK.
 *
 * <p>The API key is read from configuration (which in turn reads the
 * {@code ANTHROPIC_API_KEY} environment variable). If no key is present the
 * client is disabled and callers receive a clear error instead of a crash, so
 * the rest of the application keeps working without AI.</p>
 */
@Component
public class ClaudeClient {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeClient.class);

    private final String model;
    private final long maxTokens;
    private final AnthropicClient client; // null when no API key configured

    public ClaudeClient(
            @Value("${crm.ai.api-key:}") String apiKey,
            @Value("${crm.ai.model:claude-opus-4-8}") String model,
            @Value("${crm.ai.max-tokens:2048}") long maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
        if (apiKey == null || apiKey.isBlank()) {
            this.client = null;
            logger.warn("ANTHROPIC_API_KEY not set - AI features are disabled");
        } else {
            this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
            logger.info("Claude client initialized (model={})", model);
        }
    }

    public boolean isEnabled() {
        return client != null;
    }

    /** A single conversation turn. */
    public record Turn(String role, String content) {
        public static Turn user(String c) { return new Turn("user", c); }
        public static Turn assistant(String c) { return new Turn("assistant", c); }
    }

    /**
     * Single-shot completion: one system prompt + one user message.
     */
    public String complete(String system, String userMessage) {
        return chat(system, List.of(Turn.user(userMessage)));
    }

    /**
     * Multi-turn chat. The history must start with a user turn and alternate
     * user/assistant.
     */
    public String chat(String system, List<Turn> history) {
        ensureEnabled();
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens);
        if (system != null && !system.isBlank()) {
            builder.system(system);
        }
        for (Turn turn : history) {
            if ("assistant".equalsIgnoreCase(turn.role())) {
                builder.addAssistantMessage(turn.content());
            } else {
                builder.addUserMessage(turn.content());
            }
        }
        Message response = client.messages().create(builder.build());
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(text -> text.text())
                .collect(Collectors.joining());
    }

    private void ensureEnabled() {
        if (!isEnabled()) {
            throw new AiUnavailableException(
                "AI features are not configured. Set the ANTHROPIC_API_KEY environment variable.");
        }
    }

    /** Raised when an AI call is attempted but no API key is configured. */
    public static class AiUnavailableException extends RuntimeException {
        public AiUnavailableException(String message) {
            super(message);
        }
    }
}
