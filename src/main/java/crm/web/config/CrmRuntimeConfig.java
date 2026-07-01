package crm.web.config;

import crm.config.DatabaseConnection;
import crm.facade.CrmFacade;
import crm.observer.EventBus;
import crm.observer.events.ActivityCompletedEvent;
import crm.observer.events.ContactCreatedEvent;
import crm.observer.events.EnrollmentCreatedEvent;
import crm.observer.events.SessionBookedEvent;
import crm.observer.listeners.AuditLogObserver;
import crm.observer.listeners.EnrollmentConfirmationObserver;
import crm.observer.listeners.InvoiceGenerationObserver;
import crm.observer.listeners.LeadScoreUpdateObserver;
import crm.observer.listeners.WelcomeEmailObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the existing (pattern-based) domain layer into the Spring context.
 *
 * <p>The services are Singletons accessed through {@code getInstance()}; here we
 * simply bootstrap them and expose the {@link CrmFacade} as a Spring bean so the
 * REST controllers can have it injected. This mirrors the startup sequence of
 * the original {@link crm.CrmApplication} desktop launcher.</p>
 */
@Configuration
public class CrmRuntimeConfig {

    private static final Logger logger = LoggerFactory.getLogger(CrmRuntimeConfig.class);

    @PostConstruct
    public void initDomain() {
        logger.info("Bootstrapping CRM domain layer for the web application");
        DatabaseConnection.getInstance();
        WebSchemaInitializer.ensureTables();
        registerObservers();
        logger.info("CRM domain layer ready");
    }

    private void registerObservers() {
        EventBus eventBus = EventBus.getInstance();
        eventBus.registerObserver(new AuditLogObserver());
        eventBus.registerObserver(ContactCreatedEvent.EVENT_TYPE, new WelcomeEmailObserver());
        eventBus.registerObserver(EnrollmentCreatedEvent.EVENT_TYPE, new EnrollmentConfirmationObserver());
        eventBus.registerObserver(SessionBookedEvent.EVENT_TYPE, new InvoiceGenerationObserver());
        eventBus.registerObserver(ActivityCompletedEvent.EVENT_TYPE, new LeadScoreUpdateObserver());
        logger.info("Domain observers registered");
    }

    @Bean
    public CrmFacade crmFacade() {
        return CrmFacade.getInstance();
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down CRM domain resources");
        try {
            DatabaseConnection.getInstance().shutdown();
        } catch (Exception e) {
            logger.error("Error while shutting down the connection pool", e);
        }
    }
}
