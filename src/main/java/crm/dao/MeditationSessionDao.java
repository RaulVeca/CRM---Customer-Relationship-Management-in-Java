package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.MeditationSession;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/**
 * SINGLETON + DAO PATTERN - JDBC access to the {@code meditation_sessions}
 * table, which records the online sessions contacts book with trainers.
 */
public class MeditationSessionDao extends AbstractDao<MeditationSession> {

    private static volatile MeditationSessionDao instance;

    private MeditationSessionDao() {
    }

    public static MeditationSessionDao getInstance() {
        if (instance == null) {
            synchronized (MeditationSessionDao.class) {
                if (instance == null) {
                    instance = new MeditationSessionDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "meditation_sessions";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO meditation_sessions "
                + "(trainer_id, contact_id, contact_email, session_date, start_hour, end_hour) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE meditation_sessions SET trainer_id=?, contact_id=?, contact_email=?, "
                + "session_date=?, start_hour=?, end_hour=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, MeditationSession s) throws SQLException {
        ps.setLong(1, s.getTrainerId());
        ps.setLong(2, s.getContactId());
        ps.setString(3, s.getContactEmail());
        ps.setDate(4, Date.valueOf(s.getSessionDate()));
        ps.setInt(5, s.getStartHour());
        ps.setInt(6, s.getEndHour());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, MeditationSession s) throws SQLException {
        setInsertParameters(ps, s);
        ps.setLong(7, s.getId());
    }

    @Override
    protected void setEntityId(MeditationSession entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(MeditationSession entity) {
        return entity.getId();
    }

    @Override
    protected MeditationSession mapResultSetToEntity(ResultSet rs) throws SQLException {
        MeditationSession s = new MeditationSession();
        s.setId(rs.getLong("id"));
        s.setTrainerId(rs.getLong("trainer_id"));
        s.setContactId(rs.getLong("contact_id"));
        s.setContactEmail(rs.getString("contact_email"));
        s.setSessionDate(rs.getDate("session_date").toLocalDate());
        s.setStartHour(rs.getInt("start_hour"));
        s.setEndHour(rs.getInt("end_hour"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }

    /**
     * All sessions booked with a trainer between two dates (inclusive), ordered
     * chronologically so callers can lay them out on a calendar.
     */
    public List<MeditationSession> findByTrainerBetween(Long trainerId, LocalDate from, LocalDate to) {
        String sql = "SELECT * FROM meditation_sessions "
                + "WHERE trainer_id = ? AND session_date BETWEEN ? AND ? "
                + "ORDER BY session_date, start_hour";
        return executeQuery(sql, ps -> {
            try {
                ps.setLong(1, trainerId);
                ps.setDate(2, Date.valueOf(from));
                ps.setDate(3, Date.valueOf(to));
            } catch (SQLException e) {
                throw new DataAccessException("Error in findByTrainerBetween", e);
            }
            return ps;
        });
    }

    /**
     * Every session a given contact has ever booked, ordered chronologically, so
     * the contact can review and cancel their own bookings.
     */
    public List<MeditationSession> findByContactId(Long contactId) {
        String sql = "SELECT * FROM meditation_sessions "
                + "WHERE contact_id = ? ORDER BY session_date, start_hour";
        return executeQuery(sql, ps -> {
            try {
                ps.setLong(1, contactId);
            } catch (SQLException e) {
                throw new DataAccessException("Error in findByContactId", e);
            }
            return ps;
        });
    }

    /**
     * Whether the trainer already has a session overlapping the half-open hour
     * range {@code [startHour, endHour)} on the given day. Two sessions overlap
     * when {@code existingStart < newEnd && existingEnd > newStart}.
     */
    public boolean hasOverlap(Long trainerId, LocalDate date, int startHour, int endHour) {
        String sql = "SELECT 1 FROM meditation_sessions "
                + "WHERE trainer_id = ? AND session_date = ? "
                + "AND start_hour < ? AND end_hour > ? LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, trainerId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, endHour);
            ps.setInt(4, startHour);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in hasOverlap (meditation_sessions)", e);
        }
    }
}
