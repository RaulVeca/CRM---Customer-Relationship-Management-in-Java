package crm.web.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single AI-generated course recommendation for a company.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CourseRecommendation(
        Long courseId,
        String courseName,
        String reason,
        Integer matchScore
) {
}
