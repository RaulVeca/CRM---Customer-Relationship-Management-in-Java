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
 * Clasă abstractă care definește scheletul algoritmilor de acces la date.
 * Sub-clasele implementează doar pașii specifici (mapping, table name, queries).
 * 
 * Folosește JDBC pur, fără Hibernate sau JPA.
 * 
 * @param <T> tipul entității
 */
public abstract class AbstractDao<T> implements GenericDao<T, Long> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Returnează numele tabelei. Implementat de subclase.
     */
    protected abstract String getTableName();

    /**
     * Mapează un ResultSet la o entitate. Implementat de subclase.
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    /**
     * Returnează SQL pentru INSERT. Implementat de subclase.
     */
    protected abstract String getInsertSql();

    /**
     * Returnează SQL pentru UPDATE. Implementat de subclase.
     */
    protected abstract String getUpdateSql();

    /**
     * Setează parametrii pentru INSERT. Implementat de subclase.
     */
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Setează parametrii pentru UPDATE. Implementat de subclase.
     */
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;

    /**
     * Setează ID-ul după INSERT. Implementat de subclase.
     */
    protected abstract void setEntityId(T entity, Long id);

    /**
     * Returnează ID-ul entității. Implementat de subclase.
     */
    protected abstract Long getEntityId(T entity);

    // ====================================================
    // TEMPLATE METHODS (algoritmul general)
    // ====================================================

    @Override
    public T save(T entity) {
        String sql = getInsertSql();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setInsertParameters(ps, entity);
            int affected = ps.executeUpdate();

            if (affected == 0) {
                throw new DataAccessException("Inserare eșuată, niciun rând afectat");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    setEntityId(entity, keys.getLong(1));
                }
            }
            logger.debug("Entitate salvată în {}: ID={}", getTableName(), getEntityId(entity));
            return entity;
        } catch (SQLException e) {
            logger.error("Eroare la salvare în {}", getTableName(), e);
            throw new DataAccessException("Eroare la salvare în " + getTableName(), e);
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
                throw new DataAccessException("Update eșuat, niciun rând afectat");
            }
            logger.debug("Entitate actualizată în {}: ID={}", getTableName(), getEntityId(entity));
            return entity;
        } catch (SQLException e) {
            logger.error("Eroare la update în {}", getTableName(), e);
            throw new DataAccessException("Eroare la update în " + getTableName(), e);
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
            logger.error("Eroare la findById în {}", getTableName(), e);
            throw new DataAccessException("Eroare la findById", e);
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
            logger.error("Eroare la findAll paginat în {}", getTableName(), e);
            throw new DataAccessException("Eroare la findAll", e);
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
            logger.error("Eroare la count în {}", getTableName(), e);
            throw new DataAccessException("Eroare la count", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            logger.debug("Ștergere din {} ID={}: {} rânduri", getTableName(), id, affected);
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Eroare la deleteById în {}", getTableName(), e);
            throw new DataAccessException("Eroare la ștergere", e);
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
            logger.error("Eroare la existsById în {}", getTableName(), e);
            throw new DataAccessException("Eroare la existsById", e);
        }
    }

    // ====================================================
    // Helper methods pentru subclase
    // ====================================================

    /**
     * Execută o interogare simplă fără parametri și mapează rezultatele.
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
            logger.error("Eroare la executeQuery: {}", sql, e);
            throw new DataAccessException("Eroare la query", e);
        }
    }

    /**
     * Execută o interogare cu parametri și un parameter setter.
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
            logger.error("Eroare la executeQuery: {}", sql, e);
            throw new DataAccessException("Eroare la query", e);
        }
    }
}
