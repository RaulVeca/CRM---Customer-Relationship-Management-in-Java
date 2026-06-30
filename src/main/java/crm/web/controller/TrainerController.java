package crm.web.controller;

import crm.dao.ContactDao;
import crm.dao.MeditationSessionDao;
import crm.dao.TrainerDao;
import crm.exception.BusinessException;
import crm.exception.ResourceNotFoundException;
import crm.exception.ValidationException;
import crm.model.entity.MeditationSession;
import crm.model.entity.Trainer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
            throw new ValidationException("Intervalul de date este invalid.");
        }
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new ValidationException("Intervalul cerut este prea mare.");
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
            throw new ValidationException("Contul care face rezervarea este obligatoriu.");
        }
        if (!contactDao.existsById(req.contactId())) {
            throw new ValidationException("Contul nu există.");
        }

        LocalDate date = parseDate(req.date());
        int start = req.startHour() == null ? -1 : req.startHour();
        int duration = req.durationHours() == null ? 0 : req.durationHours();
        int end = start + duration;

        if (!isWorkingDay(date, LocalDate.now())) {
            throw new BusinessException("Trainerul nu este disponibil în ziua aleasă.");
        }
        if (duration < 1) {
            throw new ValidationException("Durata trebuie să fie de cel puțin o oră.");
        }
        if (start < WORK_START || end > WORK_END) {
            throw new ValidationException(
                    "Ședințele se pot programa doar între " + WORK_START + ":00 și " + WORK_END + ":00.");
        }
        if (sessionDao.hasOverlap(trainer.getId(), date, start, end)) {
            throw new BusinessException("Trainerul este deja ocupat în acest interval. Alege alt interval.");
        }

        MeditationSession saved = sessionDao.save(MeditationSession.builder()
                .trainerId(trainer.getId())
                .contactId(req.contactId())
                .contactEmail(req.email())
                .sessionDate(date)
                .startHour(start)
                .endHour(end)
                .build());

        return new BookingResponse(
                saved.getId(), trainer.getId(), trainer.getFullName(),
                date.toString(), start, end,
                "Ședința a fost programată cu succes.");
    }

    // =====================================================
    // Helpers
    // =====================================================

    private Trainer requireTrainer(Long id) {
        return trainerDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainerul nu a fost găsit."));
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            throw new ValidationException("Data aleasă este invalidă.");
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
}
