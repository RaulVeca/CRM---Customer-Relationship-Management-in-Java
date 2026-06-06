package crm.patterns;

import java.util.List;
import java.util.Optional;

/**
 * DAO PATTERN - Interfață generică pentru Data Access Objects.
 * Definește operațiile CRUD standard pe care orice DAO le trebuie implementa.
 * 
 * @param <T>  tipul entității
 * @param <ID> tipul identificatorului
 */
public interface GenericDao<T, ID> {

    /**
     * Salvează o entitate nouă în baza de date.
     */
    T save(T entity);

    /**
     * Actualizează o entitate existentă.
     */
    T update(T entity);

    /**
     * Găsește o entitate după ID.
     */
    Optional<T> findById(ID id);

    /**
     * Returnează toate entitățile.
     */
    List<T> findAll();

    /**
     * Returnează entitățile paginate.
     */
    List<T> findAll(int offset, int limit);

    /**
     * Numără totalul entităților.
     */
    long count();

    /**
     * Șterge o entitate după ID.
     */
    boolean deleteById(ID id);

    /**
     * Verifică existența unei entități.
     */
    boolean existsById(ID id);
}
