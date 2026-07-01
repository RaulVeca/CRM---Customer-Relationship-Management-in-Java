package crm.web.controller;

import crm.dao.ContactDao;
import crm.dao.MeditationSessionDao;
import crm.dao.TrainerDao;
import crm.exception.BusinessException;
import crm.exception.ResourceNotFoundException;
import crm.exception.ValidationException;
import crm.model.entity.MeditationSession;
import crm.model.entity.Trainer;
import crm.observer.EventBus;
import crm.observer.events.SessionBookedEvent;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints that let a contact book a one-on-one online meditation
 * (tutoring) session with one of the trainers.
 *
 * <p>Business rules enforced here:</p>
 * <ul>
 *   <li>Trainers work Monday to Saturday only (never Sunday) and never in the
 *       past.</li>
 *   <li>A session may run between {@value #WORK_START}:00 and
 *       {@value #WORK_END}:00, for a whole number of consecutive hours.</li>
 *   <li>Sessions for the same trainer may not overlap.</li>
 *   <li>A day with no free hour left is reported as {@code full}, so the
 *       front-end can grey it out.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    /** Earliest hour a session may start (08:00). */
    private static final int WORK_START = 8;
    /** Latest hour a session may end (20:00). */
    private static final int WORK_END = 20;
    /** Hardest cap on how wide an availability window may be requested. */
    private static final int MAX_RANGE_DAYS = 92;

    private final TrainerDao trainerDao = TrainerDao.getInstance();
    private final MeditationSessionDao sessionDao = MeditationSessionDao.getInstance();
    private final ContactDao contactDao = ContactDao.getInstance();

    /** The (three) trainers a contact can choose from. */
    @GetMapping
    public List<TrainerDto> list() {
        return trainerDao.findAll().stream()
                .sorted(Comparator.comparing(Trainer::getId))
                .map(TrainerDto::from)
                .toList();
    }

    /**
     * The trainer's availability for every calendar day in {@code [from, to]}.
     * Each day reports whether it is a working day, whether it is fully booked,
     * and the already-booked hour intervals so the client can offer the free
     * start times.
     */
    @GetMapping("/{id}/availability")
    public List<DayAvailabilityDto> availability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Trainer trainer = requireTrainer(id);
        if (to.isBefore(from)) {
            throw new ValidationException("The date range is invalid.");
        }
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new ValidationException("The requested range is too large.");
        }

        List<MeditationSession> sessions = sessionDao.findByTrainerBetween(trainer.getId(), from, to);
        LocalDate today = LocalDate.now();

        List<DayAvailabilityDto> days = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            boolean working = isWorkingDay(date, today);
            List<Interval> booked = bookedIntervals(sessions, date);
            boolean full = working && freeStartHours(booked).isEmpty();
            days.add(new DayAvailabilityDto(date.toString(), working, full, booked));
        }
        return days;
    }

    /**
     * Books a session for the logged-in contact. Re-validates every rule on the
     * server so a crafted request cannot bypass the calendar UI.
     */
    @PostMapping("/{id}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@PathVariable Long id, @RequestBody BookingRequest req) {
        Trainer trainer = requireTrainer(id);

        if (req == null || req.contactId() == null) {
            throw new ValidationException("The account making the booking is required.");
        }
        if (!contactDao.existsById(req.contactId())) {
            throw new ValidationException("The account does not exist.");
        }

        LocalDate date = parseDate(req.date());
        int start = req.startHour() == null ? -1 : req.startHour();
        int duration = req.durationHours() == null ? 0 : req.durationHours();
        int end = start + duration;

        if (!isWorkingDay(date, LocalDate.now())) {
            throw new BusinessException("The trainer is not available on the chosen day.");
        }
        if (duration < 1) {
            throw new ValidationException("The duration must be at least one hour.");
        }
        if (start < WORK_START || end > WORK_END) {
            throw new ValidationException(
                    "Sessions can only be scheduled between " + WORK_START + ":00 and " + WORK_END + ":00.");
        }
        if (sessionDao.hasOverlap(trainer.getId(), date, start, end)) {
            throw new BusinessException("The trainer is already busy in this interval. Choose another interval.");
        }

        MeditationSession saved = sessionDao.save(MeditationSession.builder()
                .trainerId(trainer.getId())
                .contactId(req.contactId())
                .contactEmail(req.email())
                .sessionDate(date)
                .startHour(start)
                .endHour(end)
                .build());

        // OBSERVER - notifică rezervarea; generează automat factura.
        EventBus.getInstance().publish(new SessionBookedEvent(saved, "TrainerController"));

        return new BookingResponse(
                saved.getId(), trainer.getId(), trainer.getFullName(),
                date.toString(), start, end,
                "The session has been scheduled successfully.");
    }

    /**
     * Every session the given contact has booked (across all trainers), ordered
     * chronologically. The front-end only shows the "my sessions" window when
     * this list is non-empty.
     */
    @GetMapping("/sessions")
    public List<MySessionDto> mySessions(@RequestParam Long contactId) {
        if (contactId == null) {
            throw new ValidationException("The account is required.");
        }
        Map<Long, String> trainerNames = new HashMap<>();
        return sessionDao.findByContactId(contactId).stream()
                .map(s -> {
                    String name = trainerNames.computeIfAbsent(s.getTrainerId(),
                            tid -> trainerDao.findById(tid).map(Trainer::getFullName).orElse("Trainer"));
                    return new MySessionDto(s.getId(), s.getTrainerId(), name,
                            s.getSessionDate().toString(), s.getStartHour(), s.getEndHour());
                })
                .toList();
    }

    /**
     * Cancels one of the contact's own sessions: the row is removed from the
     * database, which in turn frees that hour range in the trainer's calendar. A
     * contact may only cancel a session it actually booked.
     */
    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSession(@PathVariable Long sessionId, @RequestParam Long contactId) {
        MeditationSession session = sessionDao.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("The session was not found."));
        if (contactId == null || !contactId.equals(session.getContactId())) {
            throw new ValidationException("You cannot cancel a session that isn't yours.");
        }
        sessionDao.deleteById(sessionId);
    }

    // =====================================================
    // Helpers
    // =====================================================

    private Trainer requireTrainer(Long id) {
        return trainerDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("The trainer was not found."));
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            throw new ValidationException("The chosen date is invalid.");
        }
    }

    private boolean isWorkingDay(LocalDate date, LocalDate today) {
        return !date.isBefore(today) && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private List<Interval> bookedIntervals(List<MeditationSession> sessions, LocalDate date) {
        return sessions.stream()
                .filter(s -> date.equals(s.getSessionDate()))
                .map(s -> new Interval(s.getStartHour(), s.getEndHour()))
                .sorted(Comparator.comparingInt(Interval::start))
                .toList();
    }

    /** The hours in [WORK_START, WORK_END) at which a 1-hour session could start. */
    private List<Integer> freeStartHours(List<Interval> booked) {
        boolean[] occupied = new boolean[WORK_END];
        for (Interval i : booked) {
            for (int h = Math.max(i.start(), WORK_START); h < Math.min(i.end(), WORK_END); h++) {
                occupied[h] = true;
            }
        }
        List<Integer> free = new ArrayList<>();
        for (int h = WORK_START; h < WORK_END; h++) {
            if (!occupied[h]) free.add(h);
        }
        return free;
    }

    // =====================================================
    // DTOs
    // =====================================================

    public record TrainerDto(Long id, String firstName, String lastName, String fullName, String email) {
        static TrainerDto from(Trainer t) {
            return new TrainerDto(t.getId(), t.getFirstName(), t.getLastName(), t.getFullName(), t.getEmail());
        }
    }

    /** A booked hour range, half-open: covers [start, end). */
    public record Interval(int start, int end) {}

    public record DayAvailabilityDto(String date, boolean working, boolean full, List<Interval> booked) {}

    public record BookingRequest(Long contactId, String email, String date, Integer startHour, Integer durationHours) {}

    public record BookingResponse(Long id, Long trainerId, String trainerName,
                                  String date, int startHour, int endHour, String message) {}

    public record MySessionDto(Long id, Long trainerId, String trainerName,
                               String date, int startHour, int endHour) {}
}
