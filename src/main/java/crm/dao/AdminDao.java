package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * SINGLETON + DAO PATTERN - JDBC access to the {@code admins} table.
 * Holds the trainers that are allowed to sign in through the "Cont admin"
 * option. Authentication is by email only.
 */
public class AdminDao extends AbstractDao<Admin> {

    private static volatile AdminDao instance;

    private AdminDao() {
    }

    public static AdminDao getInstance() {
        if (instance == null) {
            synchronized (AdminDao.class) {
                if (instance == null) {
                    instance = new AdminDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "admins";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO admins (first_name, last_name, email) VALUES (?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE admins SET first_name=?, last_name=?, email=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Admin a) throws SQLException {
        ps.setString(1, a.getFirstName());
        ps.setString(2, a.getLastName());
        ps.setString(3, a.getEmail());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Admin a) throws SQLException {
        setInsertParameters(ps, a);
        ps.setLong(4, a.getId());
    }

    @Override
    protected void setEntityId(Admin entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(Admin entity) {
        return entity.getId();
    }

    @Override
    protected Admin mapResultSetToEntity(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setId(rs.getLong("id"));
        a.setFirstName(rs.getString("first_name"));
        a.setLastName(rs.getString("last_name"));
        a.setEmail(rs.getString("email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }

    /**
     * Looks up an admin by email, case-insensitively, so a trainer can sign in
     * regardless of how they typed their address.
     */
    public Optional<Admin> findByEmail(String email) {
        String sql = "SELECT * FROM admins WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEntity(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Eroare findByEmail (admins)", e);
        }
    }
}
