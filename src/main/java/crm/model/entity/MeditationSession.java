package crm.model.entity;

import lombok.*;

import java.time.LocalDate;

/**
 * A one-on-one online meditation (tutoring) session a contact books with a
 * trainer. Backed by the {@code meditation_sessions} table.
 *
 * <p>A session occupies a whole number of consecutive hours on a single day,
 * between 08:00 and 20:00. {@code startHour}/{@code endHour} are stored as the
 * hour of day (e.g. 8 and 11 for an 08:00-11:00 booking), so the duration is
 * simply {@code endHour - startHour}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class MeditationSession extends BaseEntity {

    private Long trainerId;
    private Long contactId;
    private String contactEmail;
    private LocalDate sessionDate;
    private int startHour;
    private int endHour;

    public int getDurationHours() {
        return endHour - startHour;
    }
}
