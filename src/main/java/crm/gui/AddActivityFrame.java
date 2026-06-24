package crm.gui;

import javax.swing.*;

import crm.CrmApplication;
import crm.config.AppConfig;
import crm.config.DatabaseConnection;
import crm.facade.CrmFacade;
import crm.factory.ActivityFactory;
import crm.model.entity.Activity;
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

public class AddActivityFrame extends JFrame implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);
    private final CrmFacade crm = CrmFacade.getInstance();

    private JPanel mainPanel;

    private JPanel contactIdPanel;
    private JLabel contactIdLabel;
    private JTextField contactIdText;

    private JPanel typePanel;
    private JLabel typeLabel;
    private JComboBox<String> typeCombo;

    private JPanel subjectPanel;
    private JLabel subjectLabel;
    private JTextField subjectText;

    private JPanel contentPanel;
    private JLabel contentLabel;
    private JTextField contentText;

    private JPanel buttonPanel;
    private JButton button;

    public AddActivityFrame() {

        mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        contactIdPanel = new JPanel();
        contactIdLabel = new JLabel("Contact ID");
        contactIdText = new JTextField(35);

        contactIdPanel.add(contactIdLabel);
        contactIdPanel.add(contactIdText);

        mainPanel.add(contactIdPanel);

        typePanel = new JPanel();
        typeLabel = new JLabel("Tip activitate");
        String lista_type[] = {"EMAIL", "CALL", "MEETING", "NOTE"};
        typeCombo = new JComboBox<>(lista_type);

        typePanel.add(typeLabel);
        typePanel.add(typeCombo);

        mainPanel.add(typePanel);

        subjectPanel = new JPanel();
        subjectLabel = new JLabel("Subiect");
        subjectText = new JTextField(35);

        subjectPanel.add(subjectLabel);
        subjectPanel.add(subjectText);

        mainPanel.add(subjectPanel);

        contentPanel = new JPanel();
        contentLabel = new JLabel("Conținut (doar pentru NOTE)");
        contentText = new JTextField(35);

        contentPanel.add(contentLabel);
        contentPanel.add(contentText);

        mainPanel.add(contentPanel);

        buttonPanel = new JPanel();
        button = new JButton("Add Activity");
        button.addActionListener(this);
        buttonPanel.add(button);
        mainPanel.add(buttonPanel);

        this.add(mainPanel);
        this.setSize(new Dimension(1400, 800));
        this.setVisible(true);
    }

    public static void main(String... args) {
        AddActivityFrame gui1 = new AddActivityFrame();
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

        long contactId = Long.parseLong(contactIdText.getText().trim());
        String type = ((String) typeCombo.getSelectedItem()).toUpperCase();
        String subject = subjectText.getText().trim();

        Activity activity;
        switch (type) {
            case "EMAIL":
                activity = ActivityFactory.createEmail(contactId, subject, "Email manual", 1L);
                break;
            case "CALL":
                activity = ActivityFactory.createCall(contactId, subject,
                        java.time.LocalDateTime.now().plusHours(1), 1L);
                break;
            case "MEETING":
                activity = ActivityFactory.createMeeting(contactId, null, subject,
                        java.time.LocalDateTime.now().plusDays(1), 60, 1L);
                break;
            case "NOTE":
                activity = ActivityFactory.createNote(contactId, subject, contentText.getText().trim(), 1L);
                break;
            default:
                System.out.println("Tip invalid.");
                return;
        }

        Activity saved = crm.logActivity(activity);
        System.out.println("Activitate creată cu ID: " + saved.getId());

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