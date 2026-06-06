package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Opportunity;
import crm.model.enums.DeliveryMode;
import crm.model.enums.OpportunityStage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OpportunityDao extends AbstractDao<Opportunity> {

    private static volatile OpportunityDao instance;

    private OpportunityDao() {}

    public static OpportunityDao getInstance() {
        if (instance == null) {
            synchronized (OpportunityDao.class) {
                if (instance == null) {
                    instance = new OpportunityDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() { return "opportunities"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO opportunities (client_id, title, description, estimated_participants, " +
                "custom_requirements, delivery_mode, preferred_location, desired_start_date, " +
                "estimated_value, quoted_value, probability_percent, stage, expected_close_date, " +
                "assigned_to, competitors) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE opportunities SET client_id=?, title=?, description=?, " +
                "estimated_participants=?, custom_requirements=?, delivery_mode=?, " +
                "preferred_location=?, desired_start_date=?, estimated_value=?, " +
                "quoted_value=?, probability_percent=?, stage=?, expected_close_date=?, " +
                "actual_close_date=?, assigned_to=?, competitors=?, lost_reason=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Opportunity o) throws SQLException {
        int i = 1;
        ps.setLong(i++, o.getClientId());
        ps.setString(i++, o.getTitle());
        ps.setString(i++, o.getDescription());
        setIntOrNull(ps, i++, o.getEstimatedParticipants());
        ps.setString(i++, o.getCustomRequirements());
        ps.setString(i++, o.getDeliveryMode() != null ? o.getDeliveryMode().name() : null);
        ps.setString(i++, o.getPreferredLocation());
        setDateOrNull(ps, i++, o.getDesiredStartDate() == null ? null : Date.valueOf(o.getDesiredStartDate()));
        ps.setBigDecimal(i++, o.getEstimatedValue());
        ps.setBigDecimal(i++, o.getQuotedValue());
        setIntOrNull(ps, i++, o.getProbabilityPercent());
        ps.setString(i++, o.getStage() != null ? o.getStage().name() : OpportunityStage.LEAD_QUALIFICATION.name());
        setDateOrNull(ps, i++, o.getExpectedCloseDate() == null ? null : Date.valueOf(o.getExpectedCloseDate()));
        setLongOrNull(ps, i++, o.getAssignedTo());
        ps.setString(i, o.getCompetitors());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Opportunity o) throws SQLException {
        int i = 1;
        ps.setLong(i++, o.getClientId());
        ps.setString(i++, o.getTitle());
        ps.setString(i++, o.getDescription());
        setIntOrNull(ps, i++, o.getEstimatedParticipants());
        ps.setString(i++, o.getCustomRequirements());
        ps.setString(i++, o.getDeliveryMode() != null ? o.getDeliveryMode().name() : null);
        ps.setString(i++, o.getPreferredLocation());
        setDateOrNull(ps, i++, o.getDesiredStartDate() == null ? null : Date.valueOf(o.getDesiredStartDate()));
        ps.setBigDecimal(i++, o.getEstimatedValue());
        ps.setBigDecimal(i++, o.getQuotedValue());
        setIntOrNull(ps, i++, o.getProbabilityPercent());
        ps.setString(i++, o.getStage() != null ? o.getStage().name() : null);
        setDateOrNull(ps, i++, o.getExpectedCloseDate() == null ? null : Date.valueOf(o.getExpectedCloseDate()));
        setDateOrNull(ps, i++, o.getActualCloseDate() == null ? null : Date.valueOf(o.getActualCloseDate()));
        setLongOrNull(ps, i++, o.getAssignedTo());
        ps.setString(i++, o.getCompetitors());
        ps.setString(i++, o.getLostReason());
        ps.setLong(i, o.getId());
    }

    @Override
    protected void setEntityId(Opportunity entity, Long id) { entity.setId(id); }

    @Override
    protected Long getEntityId(Opportunity entity) { return entity.getId(); }

    @Override
    protected Opportunity mapResultSetToEntity(ResultSet rs) throws SQLException {
        Opportunity o = new Opportunity();
        o.setId(rs.getLong("id"));
        o.setClientId(rs.getLong("client_id"));
        o.setTitle(rs.getString("title"));
        o.setDescription(rs.getString("description"));
        o.setEstimatedParticipants(rs.getInt("estimated_participants"));
        o.setCustomRequirements(rs.getString("custom_requirements"));
        String dm = rs.getString("delivery_mode");
        if (dm != null) o.setDeliveryMode(DeliveryMode.valueOf(dm));
        o.setPreferredLocation(rs.getString("preferred_location"));
        Date dsd = rs.getDate("desired_start_date");
        if (dsd != null) o.setDesiredStartDate(dsd.toLocalDate());
        o.setEstimatedValue(rs.getBigDecimal("estimated_value"));
        o.setQuotedValue(rs.getBigDecimal("quoted_value"));
        o.setProbabilityPercent(rs.getInt("probability_percent"));
        String stage = rs.getString("stage");
        if (stage != null) o.setStage(OpportunityStage.valueOf(stage));
        Date ecd = rs.getDate("expected_close_date");
        if (ecd != null) o.setExpectedCloseDate(ecd.toLocalDate());
        Date acd = rs.getDate("actual_close_date");
        if (acd != null) o.setActualCloseDate(acd.toLocalDate());
        long at = rs.getLong("assigned_to");
        if (!rs.wasNull()) o.setAssignedTo(at);
        o.setCompetitors(rs.getString("competitors"));
        o.setLostReason(rs.getString("lost_reason"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
        return o;
    }

    public List<Opportunity> findByClientId(Long clientId) {
        String sql = "SELECT * FROM opportunities WHERE client_id = ? ORDER BY created_at DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Opportunity> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByClientId", e);
        }
    }

    public List<Opportunity> findByStage(OpportunityStage stage) {
        String sql = "SELECT * FROM opportunities WHERE stage = ? ORDER BY expected_close_date";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stage.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<Opportunity> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByStage", e);
        }
    }

    public List<Opportunity> findActivePipeline() {
        String sql = "SELECT * FROM opportunities WHERE stage NOT IN ('WON', 'LOST') " +
                     "ORDER BY expected_close_date";
        return executeQuery(sql);
    }

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, val);
    }

    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, val);
    }

    private void setDateOrNull(PreparedStatement ps, int idx, Date val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.DATE);
        else ps.setDate(idx, val);
    }
}
