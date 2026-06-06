package crm.dao;

import crm.model.entity.Trainer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TrainerDao extends AbstractDao<Trainer> {

    private static volatile TrainerDao instance;

    private TrainerDao() {}

    public static TrainerDao getInstance() {
        if (instance == null) {
            synchronized (TrainerDao.class) {
                if (instance == null) {
                    instance = new TrainerDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() { return "trainers"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO trainers (first_name, last_name, email, phone, bio, specializations, " +
                "years_experience, hourly_rate, active, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE trainers SET first_name=?, last_name=?, email=?, phone=?, bio=?, " +
                "specializations=?, years_experience=?, hourly_rate=?, average_rating=?, " +
                "total_sessions=?, active=?, user_id=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Trainer t) throws SQLException {
        ps.setString(1, t.getFirstName());
        ps.setString(2, t.getLastName());
        ps.setString(3, t.getEmail());
        ps.setString(4, t.getPhone());
        ps.setString(5, t.getBio());
        ps.setString(6, t.getSpecializations());
        ps.setInt(7, t.getYearsExperience() != null ? t.getYearsExperience() : 0);
        ps.setBigDecimal(8, t.getHourlyRate());
        ps.setBoolean(9, Boolean.TRUE.equals(t.getActive()));
        if (t.getUserId() == null) ps.setNull(10, java.sql.Types.BIGINT);
        else ps.setLong(10, t.getUserId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Trainer t) throws SQLException {
        ps.setString(1, t.getFirstName());
        ps.setString(2, t.getLastName());
        ps.setString(3, t.getEmail());
        ps.setString(4, t.getPhone());
        ps.setString(5, t.getBio());
        ps.setString(6, t.getSpecializations());
        ps.setInt(7, t.getYearsExperience() != null ? t.getYearsExperience() : 0);
        ps.setBigDecimal(8, t.getHourlyRate());
        ps.setBigDecimal(9, t.getAverageRating());
        ps.setInt(10, t.getTotalSessions() != null ? t.getTotalSessions() : 0);
        ps.setBoolean(11, Boolean.TRUE.equals(t.getActive()));
        if (t.getUserId() == null) ps.setNull(12, java.sql.Types.BIGINT);
        else ps.setLong(12, t.getUserId());
        ps.setLong(13, t.getId());
    }

    @Override
    protected void setEntityId(Trainer entity, Long id) { entity.setId(id); }

    @Override
    protected Long getEntityId(Trainer entity) { return entity.getId(); }

    @Override
    protected Trainer mapResultSetToEntity(ResultSet rs) throws SQLException {
        Trainer t = new Trainer();
        t.setId(rs.getLong("id"));
        t.setFirstName(rs.getString("first_name"));
        t.setLastName(rs.getString("last_name"));
        t.setEmail(rs.getString("email"));
        t.setPhone(rs.getString("phone"));
        t.setBio(rs.getString("bio"));
        t.setSpecializations(rs.getString("specializations"));
        t.setYearsExperience(rs.getInt("years_experience"));
        t.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        t.setAverageRating(rs.getBigDecimal("average_rating"));
        t.setTotalSessions(rs.getInt("total_sessions"));
        t.setActive(rs.getBoolean("active"));
        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) t.setUserId(uid);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }
}
