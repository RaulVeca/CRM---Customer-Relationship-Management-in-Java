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

public class ChangeLeadStatusFrame extends JFrame implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);
    private final CrmFacade crm = CrmFacade.getInstance();

    private JPanel mainPanel;

    private JPanel idPanel;
    private JLabel idLabel;
    private JTextField idText;

    private JPanel statusPanel;
    private JLabel statusLabel;
    private JComboBox<String> statusCombo;

    private JPanel buttonPanel;
    private JButton button;

    public ChangeLeadStatusFrame() {

        mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        idPanel = new JPanel();
        idLabel = new JLabel("Contact ID");
        idText = new JTextField(35);

        idPanel.add(idLabel);
        idPanel.add(idText);

        mainPanel.add(idPanel);

        statusPanel = new JPanel();
        statusLabel = new JLabel("Nou status");
        String lista_status[] = new String[LeadStatus.values().length];
        int i = 0;
        for (LeadStatus ls : LeadStatus.values()) {
            lista_status[i++] = ls.toString();
        }
        statusCombo = new JComboBox<>(lista_status);

        statusPanel.add(statusLabel);
        statusPanel.add(statusCombo);

        mainPanel.add(statusPanel);

        buttonPanel = new JPanel();
        button = new JButton("Change Lead Status");
        button.addActionListener(this);
        buttonPanel.add(button);
        mainPanel.add(buttonPanel);

        this.add(mainPanel);
        this.setSize(new Dimension(1400, 800));
        this.setVisible(true);
    }

    public static void main(String... args) {
        ChangeLeadStatusFrame gui1 = new ChangeLeadStatusFrame();
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

        long id = Long.parseLong(idText.getText().trim());
        String status = (String) statusCombo.getSelectedItem();

        crm.changeLeadStatus(id, LeadStatus.valueOf(status.toUpperCase()));
        System.out.println("Status schimbat cu succes.");

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