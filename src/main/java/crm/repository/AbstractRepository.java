package crm.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.exception.ResourceNotFoundException;
import crm.model.entity.BaseEntity;
import crm.patterns.GenericDao;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY PATTERN
 * 
 * Repository-ul este un layer central de acces la date, care:
 * - Abstractizează sursa de date (DB, cache, API extern)
 * - Oferă o API mai prietenoasă decât DAO
 * - Centralizează logica de acces la entitățile de același tip
 * - Poate combina date din mai multe surse
 * 
 * Diferența față de DAO: Repository lucrează cu obiecte domeniu
 * și expune o API axată pe business, nu pe persistență.
 */
public abstract class AbstractRepository<T extends BaseEntity> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Returnează DAO-ul specific entității.
     */
    protected abstract GenericDao<T, Long> getDao();

    /**
     * Returnează numele entității (pentru exceptii).
     */
    protected abstract String getEntityName();

    public T save(T entity) {
        logger.debug("Saving {} : {}", getEntityName(), entity);
        if (entity.isNew()) {
            return getDao().save(entity);
        } else {
            return getDao().update(entity);
        }
    }

    public Optional<T> findById(Long id) {
        return getDao().findById(id);
    }

    public T getById(Long id) {
        return findById(id).orElseThrow(() -> 
            new ResourceNotFoundException(getEntityName(), id));
    }

    public List<T> findAll() {
        return getDao().findAll();
    }

    public List<T> findAll(int offset, int limit) {
        return getDao().findAll(offset, limit);
    }

    public long count() {
        return getDao().count();
    }

    public boolean deleteById(Long id) {
        logger.debug("Delete {} ID: {}", getEntityName(), id);
        return getDao().deleteById(id);
    }

    public boolean existsById(Long id) {
        return getDao().existsById(id);
    }
}
