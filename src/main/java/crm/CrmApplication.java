package crm;

import crm.config.AppConfig;
import crm.config.DatabaseConnection;
import crm.gui.MainFrame;
import crm.observer.EventBus;
import crm.observer.events.ActivityCompletedEvent;
import crm.observer.events.ContactCreatedEvent;
import crm.observer.events.EnrollmentCreatedEvent;
import crm.observer.listeners.AuditLogObserver;
import crm.observer.listeners.EnrollmentConfirmationObserver;
import crm.observer.listeners.LeadScoreUpdateObserver;
import crm.observer.listeners.WelcomeEmailObserver;
import crm.ui.ConsoleUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CrmApplication {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);

    public static void main(String[] args) {
        logger.info("===================================================");
        logger.info("Starting CRM Training IT");
        logger.info("===================================================");

        try {
            AppConfig config = AppConfig.getInstance();
            logger.info("Application: {} v{}",
                    config.getProperty("app.name"),
                    config.getProperty("app.version"));

            DatabaseConnection.getInstance();
            logger.info("Database connection initialized");

            registerObservers();
            registerShutdownHook();

            if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
                new ConsoleUI().run();
            } else {
                launchDesktopUi();
            }
        } catch (Exception e) {
            logger.error("Fatal error while starting the application", e);
            System.exit(1);
        }
    }

    private static void launchDesktopUi() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep Swing's default look and feel when the native one is unavailable.
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private static void registerObservers() {
        EventBus eventBus = EventBus.getInstance();

        eventBus.registerObserver(new AuditLogObserver());
        eventBus.registerObserver(ContactCreatedEvent.EVENT_TYPE, new WelcomeEmailObserver());
        eventBus.registerObserver(EnrollmentCreatedEvent.EVENT_TYPE,
                new EnrollmentConfirmationObserver());
        eventBus.registerObserver(ActivityCompletedEvent.EVENT_TYPE,
                new LeadScoreUpdateObserver());

        logger.info("Observers registered");
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application resources");
            try {
                DatabaseConnection.getInstance().shutdown();
            } catch (Exception e) {
                logger.error("Cleanup error", e);
            }
            logger.info("Application stopped");
        }));
    }
}
