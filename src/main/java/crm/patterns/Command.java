package crm.patterns;

/**
 * COMMAND PATTERN
 * 
 * Encapsulează o operație ca obiect, permițând:
 * - Parametrizarea clienților cu cereri diferite
 * - Queue-ing și logging operații
 * - Undo/redo operations
 * - Tranzacții
 * 
 * Fiecare operație business (CreateContact, UpdateLead, etc.)
 * va implementa această interfață.
 * 
 * @param <R> tipul rezultatului returnat de comandă
 */
public interface Command<R> {

    /**
     * Execută comanda și returnează rezultatul.
     */
    R execute();

    /**
     * Returnează numele comenzii (pentru logging).
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
