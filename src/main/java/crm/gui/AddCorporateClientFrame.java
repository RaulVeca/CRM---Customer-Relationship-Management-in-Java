package crm.gui;

import javax.swing.*;

import crm.CrmApplication;
import crm.builder.ContactBuilder;
import crm.config.AppConfig;
import crm.config.DatabaseConnection;
import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.enums.LeadSource;
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

public class AddCorporateClientFrame extends JFrame implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);
    private final CrmFacade crm = CrmFacade.getInstance();

    private JPanel mainPanel;

    private JPanel companyNamePanel;
    private JLabel companyNameLabel;
    private JTextField companyNameText;

    private JPanel emailPanel;
    private JLabel emailLabel;
    private JTextField emailText;

    private JPanel fiscalCodePanel;
    private JLabel fiscalCodeLabel;
    private JTextField fiscalCodeText;

    private JPanel industryPanel;
    private JLabel industryLabel;
    private JTextField industryText;

    private JPanel employeesPanel;
    private JLabel employeesLabel;
    private JTextField employeesText;

    private JPanel buttonPanel;
    private JButton button;

    public AddCorporateClientFrame() {

        mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        companyNamePanel = new JPanel();
        companyNameLabel = new JLabel("Nume companie");
        companyNameText = new JTextField(35);

        companyNamePanel.add(companyNameLabel);
        companyNamePanel.add(companyNameText);

        mainPanel.add(companyNamePanel);

        emailPanel = new JPanel();
        emailLabel = new JLabel("Email principal");
        emailText = new JTextField(35);

        emailPanel.add(emailLabel);
        emailPanel.add(emailText);

        mainPanel.add(emailPanel);

        fiscalCodePanel = new JPanel();
        fiscalCodeLabel = new JLabel("Cod fiscal");
        fiscalCodeText = new JTextField(35);

        fiscalCodePanel.add(fiscalCodeLabel);
        fiscalCodePanel.add(fiscalCodeText);

        mainPanel.add(fiscalCodePanel);

        industryPanel = new JPanel();
        industryLabel = new JLabel("Industrie");
        industryText = new JTextField(35);

        industryPanel.add(industryLabel);
        industryPanel.add(industryText);

        mainPanel.add(industryPanel);

        employeesPanel = new JPanel();
        employeesLabel = new JLabel("Număr angajați");
        employeesText = new JTextField(35);

        employeesPanel.add(employeesLabel);
        employeesPanel.add(employeesText);

        mainPanel.add(employeesPanel);

        buttonPanel = new JPanel();
        button = new JButton("Register Corporate Client");
        button.addActionListener(this);
        buttonPanel.add(button);
        mainPanel.add(buttonPanel);

        this.add(mainPanel);
        this.setSize(new Dimension(1400, 800));
        this.setVisible(true);
    }

    public static void main(String... args) {
        AddCorporateClientFrame gui1 = new AddCorporateClientFrame();
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

        String companyName = companyNameText.getText().trim();
        String email = emailText.getText().trim();
        String fiscalCode = fiscalCodeText.getText().trim();
        String industry = industryText.getText().trim();
        int employees = Integer.parseInt(employeesText.getText().trim());

        Contact contact = new ContactBuilder()
                .asCorporate()
                .companyName(companyName)
                .email(email)
                .fiscalDetails(fiscalCode, null)
                .industry(industry, employees)
                .leadSource(LeadSource.REFERRAL)
                .withGdprConsent()
                .build();

        Contact saved = crm.createContact(contact);
        System.out.println("Client corporate creat: " + saved.getId() + ", scor: " + saved.getLeadScore());

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