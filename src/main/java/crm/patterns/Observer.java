package crm.patterns;

/**
 * OBSERVER PATTERN - interfața Observer
 * 
 * Definește contractul pentru obiecte care vor să fie notificate
 * atunci când se întâmplă evenimente specifice în sistem.
 * 
 * Folosit pentru:
 * - Notificări email/SMS pe evenimente
 * - Lead scoring automat
 * - Audit logging
 * - Update statistici dashboard
 */
public interface Observer<E> {

    /**
     * Metoda apelată când are loc un eveniment.
     * 
     * @param event evenimentul care a avut loc
     */
    void update(E event);

    /**
     * Returnează tipul evenimentului pentru filtrare.
     */
    default String getObserverName() {
        return this.getClass().getSimpleName();
    }
}
