package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Activity;
import crm.model.enums.ActivityType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityDao extends AbstractDao<Activity> {

    private static volatile ActivityDao instance;

    private ActivityDao() {}

    public static ActivityDao getInstance() {
        if (instance == null) {
            synchronized (ActivityDao.class) {
                if (instance == null) {
                    instance = new ActivityDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() { return "activities"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO activities (activity_type, contact_id, opportunity_id, subject, " +
                "description, scheduled_date, completed_date, duration_minutes, assigned_to, " +
                "status, priority, outcome, next_steps, requires_followup, followup_date, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE activities SET activity_type=?, contact_id=?, opportunity_id=?, subject=?, " +
                "description=?, scheduled_date=?, completed_date=?, duration_minutes=?, " +
                "assigned_to=?, status=?, priority=?, outcome=?, next_steps=?, " +
                "requires_followup=?, followup_date=?, created_by=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Activity a) throws SQLException {
        int i = 1;
        ps.setString(i++, a.getActivityType() != null ? a.getActivityType().name() : null);
        setLongOrNull(ps, i++, a.getContactId());
        setLongOrNull(ps, i++, a.getOpportunityId());
        ps.setString(i++, a.getSubject());
        ps.setString(i++, a.getDescription());
        setDateTimeOrNull(ps, i++, a.getScheduledDate());
        setDateTimeOrNull(ps, i++, a.getCompletedDate());
        setIntOrNull(ps, i++, a.getDurationMinutes());
        setLongOrNull(ps, i++, a.getAssignedTo());
        ps.setString(i++, a.getStatus() != null ? a.getStatus() : "SCHEDULED");
        ps.setString(i++, a.getPriority() != null ? a.getPriority() : "MEDIUM");
        ps.setString(i++, a.getOutcome());
        ps.setString(i++, a.getNextSteps());
        ps.setBoolean(i++, Boolean.TRUE.equals(a.getRequiresFollowup()));
        if (a.getFollowupDate() == null) ps.setNull(i++, Types.DATE);
        else ps.setDate(i++, Date.valueOf(a.getFollowupDate()));
        setLongOrNull(ps, i, a.getCreatedBy());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Activity a) throws SQLException {
        setInsertParameters(ps, a);
        ps.setLong(17, a.getId());
    }

    @Override
    protected void setEntityId(Activity entity, Long id) { entity.setId(id); }

    @Override
    protected Long getEntityId(Activity entity) { return entity.getId(); }

    @Override
    protected Activity mapResultSetToEntity(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setId(rs.getLong("id"));
        String type = rs.getString("activity_type");
        if (type != null) a.setActivityType(ActivityType.valueOf(type));
        long cid = rs.getLong("contact_id");
        if (!rs.wasNull()) a.setContactId(cid);
        long oid = rs.getLong("opportunity_id");
        if (!rs.wasNull()) a.setOpportunityId(oid);
        a.setSubject(rs.getString("subject"));
        a.setDescription(rs.getString("description"));
        Timestamp sd = rs.getTimestamp("scheduled_date");
        if (sd != null) a.setScheduledDate(sd.toLocalDateTime());
        Timestamp cd = rs.getTimestamp("completed_date");
        if (cd != null) a.setCompletedDate(cd.toLocalDateTime());
        a.setDurationMinutes(rs.getInt("duration_minutes"));
        long at = rs.getLong("assigned_to");
        if (!rs.wasNull()) a.setAssignedTo(at);
        a.setStatus(rs.getString("status"));
        a.setPriority(rs.getString("priority"));
        a.setOutcome(rs.getString("outcome"));
        a.setNextSteps(rs.getString("next_steps"));
        a.setRequiresFollowup(rs.getBoolean("requires_followup"));
        Date fd = rs.getDate("followup_date");
        if (fd != null) a.setFollowupDate(fd.toLocalDate());
        long cb = rs.getLong("created_by");
        if (!rs.wasNull()) a.setCreatedBy(cb);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }

    public List<Activity> findByContactId(Long contactId) {
        String sql = "SELECT * FROM activities WHERE contact_id = ? ORDER BY scheduled_date DESC";
        return findBy(sql, contactId);
    }

    public List<Activity> findByOpportunityId(Long oppId) {
        String sql = "SELECT * FROM activities WHERE opportunity_id = ? ORDER BY scheduled_date DESC";
        return findBy(sql, oppId);
    }

    public List<Activity> findUpcoming(Long userId, int days) {
        String sql = "SELECT * FROM activities WHERE assigned_to = ? AND scheduled_date BETWEEN NOW() " +
                "AND DATE_ADD(NOW(), INTERVAL ? DAY) AND status = 'SCHEDULED' ORDER BY scheduled_date";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                List<Activity> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findUpcoming", e);
        }
    }

    private List<Activity> findBy(String sql, Long param) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                List<Activity> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare query", e);
        }
    }

    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, val);
    }

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, val);
    }

    private void setDateTimeOrNull(PreparedStatement ps, int idx, LocalDateTime val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.TIMESTAMP);
        else ps.setTimestamp(idx, Timestamp.valueOf(val));
    }
}
