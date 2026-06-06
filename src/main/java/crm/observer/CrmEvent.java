package crm.observer;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OBSERVER PATTERN - Event base class
 * 
 * Toate evenimentele din sistem extind această clasă.
 * Furnizează informații comune: timestamp, sursa.
 */
@Getter
public abstract class CrmEvent {

    private final LocalDateTime timestamp;
    private final String source;
    private final Object payload;

    protected CrmEvent(String source, Object payload) {
        this.timestamp = LocalDateTime.now();
        this.source = source;
        this.payload = payload;
    }

    public abstract String getEventType();

    @SuppressWarnings("unchecked")
    public <T> T getPayloadAs(Class<T> clazz) {
        if (payload == null) return null;
        return (T) payload;
    }
}
