package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Enrollment;
import crm.model.enums.EnrollmentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao extends AbstractDao<Enrollment> {

    private static volatile EnrollmentDao instance;

    private EnrollmentDao() {}

    public static EnrollmentDao getInstance() {
        if (instance == null) {
            synchronized (EnrollmentDao.class) {
                if (instance == null) {
                    instance = new EnrollmentDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() { return "enrollments"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO enrollments (session_id, contact_id, status, notes) " +
                "VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE enrollments SET session_id=?, contact_id=?, status=?, " +
                "attended_sessions=?, attendance_rate=?, exam_passed=?, final_grade=?, " +
                "certificate_issued=?, certificate_number=?, rating=?, feedback=?, notes=? " +
                "WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Enrollment e) throws SQLException {
        int i = 1;
        ps.setLong(i++, e.getSessionId());
        ps.setLong(i++, e.getContactId());
        ps.setString(i++, e.getStatus() != null ? e.getStatus().name() : EnrollmentStatus.CONFIRMED.name());
        ps.setString(i, e.getNotes());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Enrollment e) throws SQLException {
        int i = 1;
        ps.setLong(i++, e.getSessionId());
        ps.setLong(i++, e.getContactId());
        ps.setString(i++, e.getStatus() != null ? e.getStatus().name() : null);
        if (e.getAttendedSessions() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, e.getAttendedSessions());
        ps.setBigDecimal(i++, e.getAttendanceRate());
        if (e.getExamPassed() == null) ps.setNull(i++, Types.BOOLEAN);
        else ps.setBoolean(i++, e.getExamPassed());
        ps.setBigDecimal(i++, e.getFinalGrade());
        ps.setBoolean(i++, Boolean.TRUE.equals(e.getCertificateIssued()));
        ps.setString(i++, e.getCertificateNumber());
        if (e.getRating() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, e.getRating());
        ps.setString(i++, e.getFeedback());
        ps.setString(i++, e.getNotes());
        ps.setLong(i, e.getId());
    }

    @Override
    protected void setEntityId(Enrollment entity, Long id) { entity.setId(id); }

    @Override
    protected Long getEntityId(Enrollment entity) { return entity.getId(); }

    @Override
    protected Enrollment mapResultSetToEntity(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getLong("id"));
        e.setSessionId(rs.getLong("session_id"));
        e.setContactId(rs.getLong("contact_id"));
        Timestamp enrollDate = rs.getTimestamp("enrollment_date");
        if (enrollDate != null) e.setEnrollmentDate(enrollDate.toLocalDateTime());
        String status = rs.getString("status");
        if (status != null) e.setStatus(EnrollmentStatus.valueOf(status));
        e.setAttendedSessions(rs.getInt("attended_sessions"));
        e.setAttendanceRate(rs.getBigDecimal("attendance_rate"));
        e.setExamPassed(rs.getBoolean("exam_passed"));
        e.setFinalGrade(rs.getBigDecimal("final_grade"));
        e.setCertificateIssued(rs.getBoolean("certificate_issued"));
        e.setCertificateNumber(rs.getString("certificate_number"));
        e.setRating(rs.getInt("rating"));
        e.setFeedback(rs.getString("feedback"));
        e.setNotes(rs.getString("notes"));
        return e;
    }

    public List<Enrollment> findByContactId(Long contactId) {
        String sql = "SELECT * FROM enrollments WHERE contact_id = ? ORDER BY enrollment_date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Enrollment> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in findByContactId", e);
        }
    }

    public List<Enrollment> findBySessionId(Long sessionId) {
        String sql = "SELECT * FROM enrollments WHERE session_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Enrollment> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in findBySessionId", e);
        }
    }

}
