package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Course;
import crm.model.enums.CourseCategory;
import crm.model.enums.ExperienceLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CourseDao - acces JDBC pentru tabela courses
 */
public class CourseDao extends AbstractDao<Course> {

    private static volatile CourseDao instance;

    private CourseDao() {}

    public static CourseDao getInstance() {
        if (instance == null) {
            synchronized (CourseDao.class) {
                if (instance == null) {
                    instance = new CourseDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() { return "courses"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO courses (code, name, description, syllabus, category, level, " +
                "prerequisites, duration_hours, min_participants, max_participants, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE courses SET code=?, name=?, description=?, syllabus=?, category=?, " +
                "level=?, prerequisites=?, duration_hours=?, min_participants=?, " +
                "max_participants=?, active=? WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Course c) throws SQLException {
        int i = 1;
        ps.setString(i++, c.getCode());
        ps.setString(i++, c.getName());
        ps.setString(i++, c.getDescription());
        ps.setString(i++, c.getSyllabus());
        ps.setString(i++, c.getCategory() != null ? c.getCategory().name() : null);
        ps.setString(i++, c.getLevel() != null ? c.getLevel().name() : null);
        ps.setString(i++, c.getPrerequisites());
        ps.setInt(i++, c.getDurationHours() != null ? c.getDurationHours() : 0);
        ps.setInt(i++, c.getMinParticipants() != null ? c.getMinParticipants() : 3);
        ps.setInt(i++, c.getMaxParticipants() != null ? c.getMaxParticipants() : 15);
        ps.setBoolean(i, Boolean.TRUE.equals(c.getActive()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Course c) throws SQLException {
        setInsertParameters(ps, c);
        ps.setLong(12, c.getId());
    }

    @Override
    protected void setEntityId(Course entity, Long id) { entity.setId(id); }

    @Override
    protected Long getEntityId(Course entity) { return entity.getId(); }

    @Override
    protected Course mapResultSetToEntity(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getLong("id"));
        c.setCode(rs.getString("code"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setSyllabus(rs.getString("syllabus"));
        String cat = rs.getString("category");
        if (cat != null) c.setCategory(CourseCategory.valueOf(cat));
        String lvl = rs.getString("level");
        if (lvl != null) c.setLevel(ExperienceLevel.valueOf(lvl));
        c.setPrerequisites(rs.getString("prerequisites"));
        c.setDurationHours(rs.getInt("duration_hours"));
        c.setMinParticipants(rs.getInt("min_participants"));
        c.setMaxParticipants(rs.getInt("max_participants"));
        c.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }

    public Optional<Course> findByCode(String code) {
        String sql = "SELECT * FROM courses WHERE code = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEntity(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in findByCode", e);
        }
    }

    public List<Course> findActiveCourses() {
        String sql = "SELECT * FROM courses WHERE active = TRUE ORDER BY name";
        return executeQuery(sql);
    }

    public List<Course> findByCategory(CourseCategory category) {
        String sql = "SELECT * FROM courses WHERE category = ? AND active = TRUE ORDER BY name";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<Course> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in findByCategory", e);
        }
    }
}
