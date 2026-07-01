package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Trainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * SINGLETON + DAO PATTERN - JDBC access to the {@code trainers} table.
 * Stores the trainers that deliver the courses. Only the name and email are
 * kept.
 */
public class TrainerDao extends AbstractDao<Trainer> {

    private static volatile TrainerDao instance;

    private TrainerDao() {
    }

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
    protected String getTableName() {
        return "trainers";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO trainers (first_name, last_name, email) VALUES (?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE trainers SET first_name=?, last_name=?, email=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Trainer t) throws SQLException {
        ps.setString(1, t.getFirstName());
        ps.setString(2, t.getLastName());
        ps.setString(3, t.getEmail());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Trainer t) throws SQLException {
        setInsertParameters(ps, t);
        ps.setLong(4, t.getId());
    }

    @Override
    protected void setEntityId(Trainer entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(Trainer entity) {
        return entity.getId();
    }

    @Override
    protected Trainer mapResultSetToEntity(ResultSet rs) throws SQLException {
        Trainer t = new Trainer();
        t.setId(rs.getLong("id"));
        t.setFirstName(rs.getString("first_name"));
        t.setLastName(rs.getString("last_name"));
        t.setEmail(rs.getString("email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }

    /**
     * Looks up a trainer by email, case-insensitively.
     */
    public Optional<Trainer> findByEmail(String email) {
        String sql = "SELECT * FROM trainers WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEntity(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in findByEmail (trainers)", e);
        }
    }
}
