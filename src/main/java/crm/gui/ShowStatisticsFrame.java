package crm.gui;

import javax.swing.*;

import crm.CrmApplication;
import crm.config.AppConfig;
import crm.config.DatabaseConnection;
import crm.facade.CrmFacade;
import crm.model.enums.LeadStatus;
import crm.observer.EventBus;
import crm.observer.events.ActivityCompletedEvent;
import crm.observer.events.ContactCreatedEvent;
import crm.observer.events.EnrollmentCreatedEvent;
import crm.observer.listeners.AuditLogObserver;
import crm.observer.listeners.EnrollmentConfirmationObserver;
import crm.observer.listeners.LeadScoreUpdateObserver;
import crm.observer.listeners.WelcomeEmailObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShowStatisticsFrame extends JFrame implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);
    private final CrmFacade crm = CrmFacade.getInstance();

    private JPanel mainPanel;

    private JPanel buttonPanel;
    private JButton button;

    private JPanel resultPanel;
    private JTextArea resultArea;

    public ShowStatisticsFrame() {

        mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        buttonPanel = new JPanel();
        button = new JButton("Show Statistics");
        button.addActionListener(this);
        buttonPanel.add(button);
        mainPanel.add(buttonPanel);

        resultPanel = new JPanel();
        resultArea = new JTextArea(12, 40);
        resultArea.setEditable(false);
        resultPanel.add(new JScrollPane(resultArea));
        mainPanel.add(resultPanel);

        this.add(mainPanel);
        this.setSize(new Dimension(1400, 800));
        this.setVisible(true);
    }

    public static void main(String... args) {
        ShowStatisticsFrame gui1 = new ShowStatisticsFrame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Funcționează!");

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

        StringBuilder sb = new StringBuilder();
        sb.append("--- Statistici Contacte ---\n");
        sb.append("Total contacte: ").append(crm.getTotalContacts()).append("\n");
        for (LeadStatus status : LeadStatus.values()) {
            sb.append(String.format("  %s: %d%n", status, crm.getContactsByStatus(status)));
        }
        resultArea.setText(sb.toString());
        System.out.print(sb);

        // 4. Hook de cleanup la oprire
        registerShutdownHook();
    }

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

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Oprire aplicație - cleanup resurse...");
            try {
                DatabaseConnection.getInstance().shutdown();
            } catch (Exception ex) {
                logger.error("Eroare la cleanup", ex);
            }
            logger.info("Aplicație oprită.");
        }));
    }
}