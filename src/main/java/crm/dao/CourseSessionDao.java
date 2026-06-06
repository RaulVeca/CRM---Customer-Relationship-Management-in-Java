package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.CourseSession;
import crm.model.enums.DeliveryMode;
import crm.model.enums.SessionStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CourseSessionDao extends AbstractDao<CourseSession> {

    private static volatile CourseSessionDao instance;

    private CourseSessionDao() {}

    public static CourseSessionDao getInstance() {
        if (instance == null) {
            synchronized (CourseSessionDao.class) {
                if (instance == null) {
                    instance = new CourseSessionDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() { return "course_sessions"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO course_sessions (course_id, session_code, start_date, end_date, " +
                "schedule_description, total_hours, delivery_mode, location, meeting_link, " +
                "trainer_id, max_participants, current_participants, status, is_corporate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE course_sessions SET course_id=?, session_code=?, start_date=?, end_date=?, " +
                "schedule_description=?, total_hours=?, delivery_mode=?, location=?, meeting_link=?, " +
                "trainer_id=?, max_participants=?, current_participants=?, status=?, is_corporate=? " +
                "WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, CourseSession s) throws SQLException {
        int i = 1;
        ps.setLong(i++, s.getCourseId());
        ps.setString(i++, s.getSessionCode());
        ps.setDate(i++, Date.valueOf(s.getStartDate()));
        ps.setDate(i++, Date.valueOf(s.getEndDate()));
        ps.setString(i++, s.getScheduleDescription());
        ps.setInt(i++, s.getTotalHours());
        ps.setString(i++, s.getDeliveryMode() != null ? s.getDeliveryMode().name() : null);
        ps.setString(i++, s.getLocation());
        ps.setString(i++, s.getMeetingLink());
        setLongOrNull(ps, i++, s.getTrainerId());
        setIntOrNull(ps, i++, s.getMaxParticipants());
        setIntOrNull(ps, i++, s.getCurrentParticipants());
        ps.setString(i++, s.getStatus() != null ? s.getStatus().name() : SessionStatus.PLANNED.name());
        ps.setBoolean(i, Boolean.TRUE.equals(s.getIsCorporate()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, CourseSession s) throws SQLException {
        setInsertParameters(ps, s);
        ps.setLong(15, s.getId());
    }

    @Override
    protected void setEntityId(CourseSession entity, Long id) { entity.setId(id); }

    @Override
    protected Long getEntityId(CourseSession entity) { return entity.getId(); }

    @Override
    protected CourseSession mapResultSetToEntity(ResultSet rs) throws SQLException {
        CourseSession s = new CourseSession();
        s.setId(rs.getLong("id"));
        s.setCourseId(rs.getLong("course_id"));
        s.setSessionCode(rs.getString("session_code"));
        Date sd = rs.getDate("start_date");
        if (sd != null) s.setStartDate(sd.toLocalDate());
        Date ed = rs.getDate("end_date");
        if (ed != null) s.setEndDate(ed.toLocalDate());
        s.setScheduleDescription(rs.getString("schedule_description"));
        s.setTotalHours(rs.getInt("total_hours"));
        String dm = rs.getString("delivery_mode");
        if (dm != null) s.setDeliveryMode(DeliveryMode.valueOf(dm));
        s.setLocation(rs.getString("location"));
        s.setMeetingLink(rs.getString("meeting_link"));
        long tid = rs.getLong("trainer_id");
        if (!rs.wasNull()) s.setTrainerId(tid);
        s.setMaxParticipants(rs.getInt("max_participants"));
        s.setCurrentParticipants(rs.getInt("current_participants"));
        String st = rs.getString("status");
        if (st != null) s.setStatus(SessionStatus.valueOf(st));
        s.setAverageRating(rs.getBigDecimal("average_rating"));
        s.setIsCorporate(rs.getBoolean("is_corporate"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }

    public List<CourseSession> findByCourseId(Long courseId) {
        String sql = "SELECT * FROM course_sessions WHERE course_id = ? ORDER BY start_date";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CourseSession> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByCourseId", e);
        }
    }

    public List<CourseSession> findUpcoming() {
        String sql = "SELECT * FROM course_sessions WHERE start_date >= CURDATE() " +
                "AND status IN ('PLANNED', 'OPEN_ENROLLMENT') ORDER BY start_date LIMIT 20";
        return executeQuery(sql);
    }

    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, val);
    }

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, val);
    }
}
