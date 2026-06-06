package crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.config.AppConfig;
import crm.config.DatabaseConnection;
import crm.observer.EventBus;
import crm.observer.events.*;
import crm.observer.listeners.*;
import crm.ui.ConsoleUI;

/**
 * Punctul de intrare în aplicație.
 * 
 * Realizează:
 * 1. Inițializare configurare (Singleton)
 * 2. Inițializare conexiune DB (Singleton + HikariCP)
 * 3. Înregistrare Observer-i la Event Bus
 * 4. Pornire UI consolă
 * 5. Înregistrare shutdown hook pentru cleanup
 */
public class CrmApplication {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);

    public static void main(String[] args) {
        logger.info("===================================================");
        logger.info("Pornire CRM Training IT");
        logger.info("===================================================");

        try {
            // 1. Inițializare configurare
            AppConfig config = AppConfig.getInstance();
            logger.info("Aplicație: {} v{}", 
                    config.getProperty("app.name"), 
                    config.getProperty("app.version"));

            // 2. Inițializare DB connection pool
            DatabaseConnection.getInstance();
            logger.info("Conexiune DB inițializată");

            // 3. Înregistrare Observer-i
            registerObservers();

            // 4. Hook de cleanup la oprire
            registerShutdownHook();

            // 5. Pornire UI
            ConsoleUI ui = new ConsoleUI();
            ui.run();

        } catch (Exception e) {
            logger.error("Eroare fatală la pornirea aplicației", e);
            System.exit(1);
        }
    }

    /**
     * Înregistrează Observer-ii la EventBus.
     * Aceștia vor fi notificați automat când evenimentele au loc.
     */
    private static void registerObservers() {
        EventBus eventBus = EventBus.getInstance();

        // Observer global: audit log pentru toate evenimentele
        eventBus.registerObserver(new AuditLogObserver());

        // Observer specifici pe tipuri de evenimente
        eventBus.registerObserver(ContactCreatedEvent.EVENT_TYPE, new WelcomeEmailObserver());
        eventBus.registerObserver(EnrollmentCreatedEvent.EVENT_TYPE, 
                new EnrollmentConfirmationObserver());
        eventBus.registerObserver(ActivityCompletedEvent.EVENT_TYPE, 
                new LeadScoreUpdateObserver());

        logger.info("Observer-i înregistrați cu succes");
    }

    /**
     * Hook care se execută la oprirea JVM-ului pentru cleanup.
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Oprire aplicație - cleanup resurse...");
            try {
                DatabaseConnection.getInstance().shutdown();
            } catch (Exception e) {
                logger.error("Eroare la cleanup", e);
            }
            logger.info("Aplicație oprită.");
        }));
    }
}
