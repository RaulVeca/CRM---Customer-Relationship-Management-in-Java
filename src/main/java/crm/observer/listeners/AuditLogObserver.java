package crm.observer.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.config.DatabaseConnection;
import crm.observer.CrmEvent;
import crm.patterns.Observer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Observer global care înregistrează TOATE evenimentele în audit_logs.
 */
public class AuditLogObserver implements Observer<CrmEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogObserver.class);

    @Override
    public void update(CrmEvent event) {
        try {
            String sql = "INSERT INTO audit_logs (entity_type, entity_id, action, " +
                    "new_values) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                Object payload = event.getPayload();
                String entityType = event.getSource();
                Long entityId = 0L;

                // Încearcă să extragi ID-ul prin reflexie
                if (payload != null) {
                    try {
                        Object idObj = payload.getClass().getMethod("getId").invoke(payload);
                        if (idObj instanceof Long) entityId = (Long) idObj;
                    } catch (Exception ignored) {}
                }

                ps.setString(1, entityType);
                ps.setLong(2, entityId != null ? entityId : 0);
                ps.setString(3, event.getEventType());
                ps.setString(4, payload != null ? payload.toString() : "");
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Eroare la audit log", e);
        }
    }
}
