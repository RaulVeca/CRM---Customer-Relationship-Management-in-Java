package crm.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uniform error body returned by the REST API.
 *
 * @param timestamp when the error was produced
 * @param status    HTTP status code
 * @param error     short reason phrase
 * @param message   human-readable description
 * @param path      request path that triggered the error
 * @param details   optional list of field/validation details
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, details);
    }
}
