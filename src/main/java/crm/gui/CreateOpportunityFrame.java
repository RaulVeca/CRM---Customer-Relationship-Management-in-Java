package crm.gui;

import javax.swing.*;

import crm.CrmApplication;
import crm.builder.OpportunityBuilder;
import crm.config.AppConfig;
import crm.config.DatabaseConnection;
import crm.facade.CrmFacade;
import crm.model.entity.Opportunity;
import crm.model.enums.DeliveryMode;
import crm.model.enums.OpportunityStage;
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
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateOpportunityFrame extends JFrame implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);
    private final CrmFacade crm = CrmFacade.getInstance();

    private JPanel mainPanel;

    private JPanel clientIdPanel;
    private JLabel clientIdLabel;
    private JTextField clientIdText;

    private JPanel titlePanel;
    private JLabel titleLabel;
    private JTextField titleText;

    private JPanel participantsPanel;
    private JLabel participantsLabel;
    private JTextField participantsText;

    private JPanel valuePanel;
    private JLabel valueLabel;
    private JTextField valueText;

    private JPanel buttonPanel;
    private JButton button;

    public CreateOpportunityFrame() {

        mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        clientIdPanel = new JPanel();
        clientIdLabel = new JLabel("Client ID");
        clientIdText = new JTextField(35);

        clientIdPanel.add(clientIdLabel);
        clientIdPanel.add(clientIdText);

        mainPanel.add(clientIdPanel);

        titlePanel = new JPanel();
        titleLabel = new JLabel("Titlu oportunitate");
        titleText = new JTextField(35);

        titlePanel.add(titleLabel);
        titlePanel.add(titleText);

        mainPanel.add(titlePanel);

        participantsPanel = new JPanel();
        participantsLabel = new JLabel("Estimat participanți");
        participantsText = new JTextField(35);

        participantsPanel.add(participantsLabel);
        participantsPanel.add(participantsText);

        mainPanel.add(participantsPanel);

        valuePanel = new JPanel();
        valueLabel = new JLabel("Valoare estimată (RON)");
        valueText = new JTextField(35);

        valuePanel.add(valueLabel);
        valuePanel.add(valueText);

        mainPanel.add(valuePanel);

        buttonPanel = new JPanel();
        button = new JButton("Create Opportunity");
        button.addActionListener(this);
        buttonPanel.add(button);
        mainPanel.add(buttonPanel);

        this.add(mainPanel);
        this.setSize(new Dimension(1400, 800));
        this.setVisible(true);
    }

    public static void main(String... args) {
        CreateOpportunityFrame gui1 = new CreateOpportunityFrame();
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

        long clientId = Long.parseLong(clientIdText.getText().trim());
        String title = titleText.getText().trim();
        int participants = Integer.parseInt(participantsText.getText().trim());
        BigDecimal value = new BigDecimal(valueText.getText().trim());

        Opportunity opp = new OpportunityBuilder()
                .forClient(clientId)
                .title(title)
                .participants(participants)
                .estimatedValue(value)
                .deliveryMode(DeliveryMode.ON_SITE)
                .stage(OpportunityStage.LEAD_QUALIFICATION)
                .expectedClose(LocalDate.now().plusMonths(2))
                .build();

        Opportunity saved = crm.createOpportunity(opp);
        System.out.println("Oportunitate creată: " + saved.getId());

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