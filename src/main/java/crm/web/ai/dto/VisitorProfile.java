package crm.web.ai.dto;

import java.util.List;

/**
 * Profile submitted by an individual website visitor through the public course
 * quiz. Used to generate personalised AI course recommendations (no CRM record
 * is created from it).
 */
public record VisitorProfile(
        List<String> interests,
        String experienceLevel,
        String goal
) {
}
