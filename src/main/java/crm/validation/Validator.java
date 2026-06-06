package crm.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * CHAIN OF RESPONSIBILITY PATTERN - Validator
 * 
 * Permite înlănțuirea de validatori, fiecare verificând o regulă.
 * Validatorul trece la următorul în lanț doar dacă validarea curentă trece.
 * 
 * Avantaje:
 * - Validări modulare, ușor de extins
 * - Ordine flexibilă
 * - Combinare ușoară pentru cazuri complexe
 */
public abstract class Validator<T> {

    protected Validator<T> next;

    /**
     * Setează validatorul următor în lanț.
     */
    public Validator<T> setNext(Validator<T> next) {
        this.next = next;
        return next;
    }

    /**
     * Validează entitatea. Returnează lista de erori (goală dacă totul e ok).
     */
    public final List<String> validate(T entity) {
        List<String> errors = new ArrayList<>();
        doValidate(entity, errors);
        if (next != null) {
            errors.addAll(next.validate(entity));
        }
        return errors;
    }

    /**
     * Implementare specifică în subclase.
     */
    protected abstract void doValidate(T entity, List<String> errors);
}
