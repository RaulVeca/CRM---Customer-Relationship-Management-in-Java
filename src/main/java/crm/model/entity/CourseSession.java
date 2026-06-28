package crm.model.entity;

import lombok.*;
import crm.model.enums.DeliveryMode;
import crm.model.enums.SessionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class CourseSession extends BaseEntity {

    private Long courseId;
    private String sessionCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String scheduleDescription;
    private Integer totalHours;
    private DeliveryMode deliveryMode;
    private String location;
    private String meetingLink;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private SessionStatus status;
    private BigDecimal averageRating;
    private Boolean isCorporate;

    public boolean isFull() {
        return currentParticipants != null 
            && maxParticipants != null 
            && currentParticipants >= maxParticipants;
    }

    public int availableSeats() {
        if (maxParticipants == null) return 0;
        int current = currentParticipants != null ? currentParticipants : 0;
        return Math.max(0, maxParticipants - current);
    }
}
