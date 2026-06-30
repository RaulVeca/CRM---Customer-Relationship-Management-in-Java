package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Employee;
import crm.model.enums.ProfileArea;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SINGLETON + DAO PATTERN - JDBC access to the {@code employees} table.
 * Interest profiles are persisted as a comma-separated string (same convention
 * as {@code trainers.specializations}).
 */
public class EmployeeDao extends AbstractDao<Employee> {

    private static volatile EmployeeDao instance;

    private EmployeeDao() {
    }

    public static EmployeeDao getInstance() {
        if (instance == null) {
            synchronized (EmployeeDao.class) {
                if (instance == null) {
                    instance = new EmployeeDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "employees";
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO employees (company_id, first_name, last_name, email, job_title, " +
               "work_profile, interest_profiles) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE employees SET company_id=?, first_name=?, last_name=?, email=?, " +
               "job_title=?, work_profile=?, interest_profiles=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Employee e) throws SQLException {
        int i = 1;
        setLongOrNull(ps, i++, e.getCompanyId());
        ps.setString(i++, e.getFirstName());
        ps.setString(i++, e.getLastName());
        ps.setString(i++, e.getEmail());
        ps.setString(i++, e.getJobTitle());
        ps.setString(i++, e.getWorkProfile() != null ? e.getWorkProfile().name() : null);
        ps.setString(i, profilesToCsv(e.getInterestProfiles()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Employee e) throws SQLException {
        setInsertParameters(ps, e);
        ps.setLong(8, e.getId());
    }

    @Override
    protected void setEntityId(Employee entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long getEntityId(Employee entity) {
        return entity.getId();
    }

    @Override
    protected Employee mapResultSetToEntity(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getLong("id"));
        e.setCompanyId(getLongOrNull(rs, "company_id"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setEmail(rs.getString("email"));
        e.setJobTitle(rs.getString("job_title"));
        e.setWorkProfile(parseEnum(rs.getString("work_profile"), ProfileArea.class));
        e.setInterestProfiles(csvToProfiles(rs.getString("interest_profiles")));
        e.setCreatedAt(getLocalDateTime(rs, "created_at"));
        e.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
        return e;
    }

    // =====================================================
    // Custom queries
    // =====================================================

    public List<Employee> findByCompanyId(Long companyId) {
        String sql = "SELECT * FROM employees WHERE company_id = ? ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Employee> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Eroare findByCompanyId", ex);
        }
    }

    /**
     * Looks up an employee by email, case-insensitively, so an employee can sign
     * in regardless of how they typed their address. Employees with no email on
     * file simply never match.
     */
    public java.util.Optional<Employee> findByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return java.util.Optional.of(mapResultSetToEntity(rs));
                return java.util.Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Eroare findByEmail (employees)", ex);
        }
    }

    public long countByCompanyId(Long companyId) {
        String sql = "SELECT COUNT(*) FROM employees WHERE company_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Eroare countByCompanyId", ex);
        }
    }

    // =====================================================
    // Helpers
    // =====================================================

    private static String profilesToCsv(List<ProfileArea> profiles) {
        if (profiles == null || profiles.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (ProfileArea p : profiles) {
            if (p == null) continue;
            if (sb.length() > 0) sb.append(",");
            sb.append(p.name());
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static List<ProfileArea> csvToProfiles(String csv) {
        List<ProfileArea> list = new ArrayList<>();
        if (csv == null || csv.isBlank()) return list;
        for (String token : csv.split(",")) {
            String t = token.trim();
            if (t.isEmpty()) continue;
            try {
                list.add(ProfileArea.valueOf(t));
            } catch (IllegalArgumentException ignored) {
                // skip unknown tokens
            }
        }
        return list;
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void setLongOrNull(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, val);
    }

    private Long getLongOrNull(ResultSet rs, String col) throws SQLException {
        long val = rs.getLong(col);
        return rs.wasNull() ? null : val;
    }

    private java.time.LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts == null ? null : ts.toLocalDateTime();
    }
}
