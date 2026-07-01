package crm.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.patterns.Observer;
import crm.patterns.Subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SINGLETON + OBSERVER PATTERN - EventBus
 * 
 * Bus central de evenimente. Componentele publică evenimente,
 * observer-ii înregistrați primesc notificări.
 * 
 * Folosește CopyOnWriteArrayList pentru thread-safety la iterare,
 * fără nevoia de blocare explicită.
 */
public class EventBus implements Subject<CrmEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    private static volatile EventBus instance;
    private final Map<String, List<Observer<CrmEvent>>> observersByType;
    private final List<Observer<CrmEvent>> globalObservers;

    private EventBus() {
        this.observersByType = new HashMap<>();
        this.globalObservers = new CopyOnWriteArrayList<>();
    }

    public static EventBus getInstance() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    @Override
    public void registerObserver(Observer<CrmEvent> observer) {
        globalObservers.add(observer);
        logger.info("Global observer registered: {}", observer.getObserverName());
    }

    /**
     * Înregistrează un observer pentru un tip specific de eveniment.
     */
    public void registerObserver(String eventType, Observer<CrmEvent> observer) {
        observersByType.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(observer);
        logger.info("Observer registered for event '{}': {}",
                eventType, observer.getObserverName());
    }

    @Override
    public void removeObserver(Observer<CrmEvent> observer) {
        globalObservers.remove(observer);
        observersByType.values().forEach(list -> list.remove(observer));
    }

    @Override
    public void notifyObservers(CrmEvent event) {
        logger.debug("Notificare eveniment: {} de la {}", 
                event.getEventType(), event.getSource());

        // Notifică observer-ii globali
        for (Observer<CrmEvent> obs : globalObservers) {
            notifyObserver(obs, event);
        }

        // Notifică observer-ii specifici tipului
        List<Observer<CrmEvent>> specificObservers = observersByType.get(event.getEventType());
        if (specificObservers != null) {
            for (Observer<CrmEvent> obs : specificObservers) {
                notifyObserver(obs, event);
            }
        }
    }

    private void notifyObserver(Observer<CrmEvent> obs, CrmEvent event) {
        try {
            obs.update(event);
        } catch (Exception e) {
            logger.error("Error in observer {}: {}", obs.getObserverName(), e.getMessage(), e);
        }
    }

    /**
     * Publică un eveniment (alias pentru notifyObservers).
     */
    public void publish(CrmEvent event) {
        notifyObservers(event);
    }
}
