package crm.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.config.DatabaseConnection;
import crm.exception.DataAccessException;
import crm.patterns.GenericDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * TEMPLATE METHOD PATTERN - AbstractDao
 *
 * Abstract class that defines the skeleton of the data-access algorithms.
 * Sub-classes implement only the specific steps (mapping, table name, queries).
 *
 * Uses plain JDBC, without Hibernate or JPA.
 *
 * @param <T> the entity type
 */
public abstract class AbstractDao<T> implements GenericDao<T, Long> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Returns the table name. Implemented by subclasses.
     */
    protected abstract String getTableName();

    /**
     * Maps a ResultSet to an entity. Implemented by subclasses.
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    /**
     * Returns the SQL for INSERT. Implemented by subclasses.
     */
    protected abstract String getInsertSql();

    /**
     * Returns the SQL for UPDATE. Implemented by subclasses.
     */
    protected abstract String getUpdateSql();

    /**
     * Sets the parameters for INSERT. Implemented by subclasses.
     */
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Sets the parameters for UPDATE. Implemented by subclasses.
     */
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Sets the ID after INSERT. Implemented by subclasses.
     */
    protected abstract void setEntityId(T entity, Long id);

    /**
     * Returns the entity's ID. Implemented by subclasses.
     */
    protected abstract Long getEntityId(T entity);

    // ====================================================
    // TEMPLATE METHODS (the general algorithm)
    // ====================================================

    @Override
    public T save(T entity) {
        String sql = getInsertSql();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setInsertParameters(ps, entity);
            int affected = ps.executeUpdate();

            if (affected == 0) {
                throw new DataAccessException("Insert failed, no rows affected");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    setEntityId(entity, keys.getLong(1));
                }
            }
            logger.debug("Entity saved in {}: ID={}", getTableName(), getEntityId(entity));
            return entity;
        } catch (SQLException e) {
            logger.error("Error while saving in {}", getTableName(), e);
            throw new DataAccessException("Error while saving in " + getTableName(), e);
        }
    }

    @Override
    public T update(T entity) {
        String sql = getUpdateSql();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setUpdateParameters(ps, entity);
            int affected = ps.executeUpdate();

            if (affected == 0) {
                throw new DataAccessException("Update failed, no rows affected");
            }
            logger.debug("Entity updated in {}: ID={}", getTableName(), getEntityId(entity));
            return entity;
        } catch (SQLException e) {
            logger.error("Error while updating in {}", getTableName(), e);
            throw new DataAccessException("Error while updating in " + getTableName(), e);
        }
    }

    @Override
    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Error in findById in {}", getTableName(), e);
            throw new DataAccessException("Error in findById", e);
        }
    }

    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY id DESC";
        return executeQuery(sql);
    }

    @Override
    public List<T> findAll(int offset, int limit) {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY id DESC LIMIT ? OFFSET ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            logger.error("Error in paginated findAll in {}", getTableName(), e);
            throw new DataAccessException("Error in findAll", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getLong(1);
            return 0;
        } catch (SQLException e) {
            logger.error("Error in count in {}", getTableName(), e);
            throw new DataAccessException("Error in count", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            logger.debug("Delete from {} ID={}: {} rows", getTableName(), id, affected);
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Error in deleteById in {}", getTableName(), e);
            throw new DataAccessException("Error while deleting", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM " + getTableName() + " WHERE id = ? LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error in existsById in {}", getTableName(), e);
            throw new DataAccessException("Error in existsById", e);
        }
    }

    // ====================================================
    // Helper methods for subclasses
    // ====================================================

    /**
     * Runs a simple query without parameters and maps the results.
     */
    protected List<T> executeQuery(String sql) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        } catch (SQLException e) {
            logger.error("Error in executeQuery: {}", sql, e);
            throw new DataAccessException("Error in query", e);
        }
    }

    /**
     * Runs a query with parameters and a parameter setter.
     */
    protected List<T> executeQuery(String sql, Function<PreparedStatement, PreparedStatement> paramSetter) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            paramSetter.apply(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            logger.error("Error in executeQuery: {}", sql, e);
            throw new DataAccessException("Error in query", e);
        }
    }
}
