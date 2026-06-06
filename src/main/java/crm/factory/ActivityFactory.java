package crm.factory;

import crm.model.entity.Activity;
import crm.model.enums.ActivityType;

import java.time.LocalDateTime;

/**
 * FACTORY PATTERN - Activity Factory
 * 
 * Creează diferite tipuri de Activity cu valori implicite potrivite.
 * Ascunde complexitatea creării obiectelor de la client.
 */
public class ActivityFactory {

    private ActivityFactory() {
        // Utility class
    }

    /**
     * Creează o activitate de tip email.
     */
    public static Activity createEmail(Long contactId, String subject, String description, Long createdBy) {
        return Activity.builder()
                .activityType(ActivityType.EMAIL)
                .contactId(contactId)
                .subject(subject)
                .description(description)
                .scheduledDate(LocalDateTime.now())
                .status("SCHEDULED")
                .priority("MEDIUM")
                .createdBy(createdBy)
                .build();
    }

    /**
     * Creează o activitate de tip apel telefonic.
     */
    public static Activity createCall(Long contactId, String subject, LocalDateTime scheduledDate, Long assignedTo) {
        return Activity.builder()
                .activityType(ActivityType.CALL)
                .contactId(contactId)
                .subject(subject)
                .scheduledDate(scheduledDate)
                .durationMinutes(30)
                .assignedTo(assignedTo)
                .status("SCHEDULED")
                .priority("HIGH")
                .build();
    }

    /**
     * Creează o activitate de tip întâlnire.
     */
    public static Activity createMeeting(Long contactId, Long opportunityId, String subject,
                                          LocalDateTime when, int durationMin, Long assignedTo) {
        return Activity.builder()
                .activityType(ActivityType.MEETING)
                .contactId(contactId)
                .opportunityId(opportunityId)
                .subject(subject)
                .scheduledDate(when)
                .durationMinutes(durationMin)
                .assignedTo(assignedTo)
                .status("SCHEDULED")
                .priority("HIGH")
                .build();
    }

    /**
     * Creează un task.
     */
    public static Activity createTask(Long contactId, String subject, String description,
                                       LocalDateTime dueDate, Long assignedTo, String priority) {
        return Activity.builder()
                .activityType(ActivityType.TASK)
                .contactId(contactId)
                .subject(subject)
                .description(description)
                .scheduledDate(dueDate)
                .assignedTo(assignedTo)
                .status("SCHEDULED")
                .priority(priority != null ? priority : "MEDIUM")
                .build();
    }

    /**
     * Creează o notă.
     */
    public static Activity createNote(Long contactId, String subject, String content, Long createdBy) {
        return Activity.builder()
                .activityType(ActivityType.NOTE)
                .contactId(contactId)
                .subject(subject)
                .description(content)
                .completedDate(LocalDateTime.now())
                .status("COMPLETED")
                .priority("LOW")
                .createdBy(createdBy)
                .build();
    }

    /**
     * Creează un follow-up automat (după X zile).
     */
    public static Activity createFollowUp(Long contactId, int daysFromNow, String reason, Long assignedTo) {
        return Activity.builder()
                .activityType(ActivityType.CALL)
                .contactId(contactId)
                .subject("Follow-up: " + reason)
                .description("Follow-up automat - " + reason)
                .scheduledDate(LocalDateTime.now().plusDays(daysFromNow))
                .assignedTo(assignedTo)
                .status("SCHEDULED")
                .priority("MEDIUM")
                .requiresFollowup(true)
                .build();
    }
}
