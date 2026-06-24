package crm.gui;

import javax.swing.*;

import crm.CrmApplication;
import crm.config.AppConfig;
import crm.config.DatabaseConnection;
import crm.facade.CrmFacade;
import crm.model.entity.Contact;
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
import java.util.List;

public class ListHotLeadsFrame extends JFrame implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(CrmApplication.class);
    private final CrmFacade crm = CrmFacade.getInstance();

    private JPanel mainPanel;

    private JPanel limitPanel;
    private JLabel limitLabel;
    private JTextField limitText;

    private JPanel buttonPanel;
    private JButton button;

    private JPanel resultPanel;
    private JTextArea resultArea;

    public ListHotLeadsFrame() {

        mainPanel = new JPanel();
        BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        limitPanel = new JPanel();
        limitLabel = new JLabel("Câte lead-uri (default 10)");
        limitText = new JTextField(35);

        limitPanel.add(limitLabel);
        limitPanel.add(limitText);

        mainPanel.add(limitPanel);

        buttonPanel = new JPanel();
        button = new JButton("List Hot Leads");
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
        ListHotLeadsFrame gui1 = new ListHotLeadsFrame();
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

        int limit = Integer.parseInt(limitText.getText().trim());

        List<Contact> hot = crm.getHotLeads(limit);
        StringBuilder sb = new StringBuilder();
        sb.append("--- Lead-uri Fierbinți ---\n");
        hot.forEach(c -> sb.append(String.format("  [%d] %s | scor: %d | status: %s%n",
                c.getId(), c.getFullName().orElse("?"), c.getLeadScore(), c.getLeadStatus())));
        if (hot.isEmpty()) {
            sb.append("(niciun lead fierbinte)\n");
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