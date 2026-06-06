package crm.patterns;

/**
 * OBSERVER PATTERN - interfața Subject (Observable)
 * 
 * Obiectele care emit evenimente implementează această interfață.
 * Permite înregistrarea/dezînregistrarea observerilor și
 * notificarea acestora când au loc evenimente.
 */
public interface Subject<E> {

    void registerObserver(Observer<E> observer);

    void removeObserver(Observer<E> observer);

    void notifyObservers(E event);
}
