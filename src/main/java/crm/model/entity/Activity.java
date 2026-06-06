package crm.model.entity;

import lombok.*;
import crm.model.enums.ActivityType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Activity extends BaseEntity {

    private ActivityType activityType;
    private Long contactId;
    private Long opportunityId;
    private String subject;
    private String description;
    private LocalDateTime scheduledDate;
    private LocalDateTime completedDate;
    private Integer durationMinutes;
    private Long assignedTo;
    private String status;
    private String priority;
    private String outcome;
    private String nextSteps;
    private Boolean requiresFollowup;
    private LocalDate followupDate;
    private Long createdBy;

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isOverdue() {
        return scheduledDate != null 
            && scheduledDate.isBefore(LocalDateTime.now()) 
            && !isCompleted();
    }
}
