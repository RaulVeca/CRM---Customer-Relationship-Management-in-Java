package crm.gui;

import javax.swing.*;
import crm.model.enums.ContactType;
import crm.model.enums.LeadSource;
import crm.model.enums.LeadStatus;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;

public class AddContactFrame extends JFrame {

    private JPanel mainPanel;

    private JPanel contactTypePanel;
    private JLabel contactTypeLabel;
    private JComboBox<String> contactTypeCombo;

    private JPanel firstNamePanel;
    private JLabel firstNameLabel;
    private JTextField firstNameText;

    private JPanel lastNamePanel;
    private JLabel lastNameLabel;
    private JTextField lastNameText;

    private JPanel panelDateBirth;
    private JLabel labelDateBirth;
    private JDatePickerImpl datePicker;

    private JPanel emailPanel;
    private JLabel emailLabel;
    private JTextField emailText;

    private JPanel phonePanel;
    private JLabel phoneLabel;
    private JTextField phoneText;

    private JPanel addressStreetPanel;
    private JLabel addressStreetLabel;
    private JTextField addressStreetText;

    private JPanel addressCityPanel;
    private JLabel addressCityLabel;
    private JTextField addressCityText;

    private JPanel addressCountyPanel;
    private JLabel addressCountyLabel;
    private JTextField addressCountyText;

    private JPanel addressPostalCodePanel;
    private JLabel addressPostalCodeLabel;
    private JTextField addressPostalCodeText;

    private JPanel LeadSourcePanel;
    private JLabel LeadSourceLabel;
    private JComboBox<String> LeadSourceCombo;

    private JPanel LeadStatusPanel;
    private JLabel LeadStatusLabel;
    private JComboBox<String> LeadStatusCombo;

    private JPanel LeadScorePanel;
    private JLabel LeadScoreLabel;
    private JTextField LeadScoreText;

    private JPanel ExperienceLevelPanel;
    private JLabel ExperienceLevelLabel;
    private JComboBox<String> ExperienceLevelCombo;

    private JPanel learningGoalPanel;
    private JLabel learningGoalLabel;
    private JTextField learningGoalText;

    private JPanel firstContactDatePanel;
    private JLabel firstContactDateLabel;
    private JDatePickerImpl firstContactDatePicker;

    private JPanel lastContactDatePanel;
    private JLabel lastContactDateLabel;
    private JDatePickerImpl lastContactDatePicker;

    private JPanel assignedToPanel;
    private JLabel assignedToLabel;
    private JTextField assignedToText;

    private JPanel gdprConsentPanel;
    private JLabel gdprConsentLabel;
    private JCheckBox ckYesGDPR;

    private JPanel marketingConsentPanel;
    private JLabel marketingConsentLabel;
    private JCheckBox ckYesMarketingGDPR;

    public AddContactFrame() {


        mainPanel = new JPanel();
        BoxLayout layout=new BoxLayout(mainPanel,BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        contactTypePanel = new JPanel();
        contactTypeLabel = new JLabel("Contact Type");
        String lista[] = new String[2];
        int i=0;
        for (ContactType c:ContactType.values()) {
            lista[i++] = c.toString();
        }
        contactTypeCombo = new JComboBox<>(lista);


        contactTypePanel.add(contactTypeLabel);
        contactTypePanel.add(contactTypeCombo);

        mainPanel.add(contactTypePanel);

        firstNamePanel = new JPanel();
        firstNameLabel = new JLabel("First Name");
        firstNameText = new JTextField(35);

        firstNamePanel.add(firstNameLabel);
        firstNamePanel.add(firstNameText);

        mainPanel.add(firstNamePanel);

        lastNamePanel = new JPanel();
        lastNameLabel = new JLabel("Last Name");
        lastNameText = new JTextField(35);

        lastNamePanel.add(lastNameLabel);
        lastNamePanel.add(lastNameText);

        mainPanel.add(lastNamePanel);

        this.panelDateBirth = new JPanel();
        this.labelDateBirth = new JLabel("Date Of Birth");

        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model,p);
        this.datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        this.panelDateBirth.add(this.labelDateBirth);
        this.panelDateBirth.add(this.datePicker);
        this.mainPanel.add(this.panelDateBirth);

        emailPanel = new JPanel();
        emailLabel = new JLabel("Email");
        emailText = new JTextField(35);

        emailPanel.add(emailLabel);
        emailPanel.add(emailText);

        mainPanel.add(emailPanel);

        phonePanel = new JPanel();
        phoneLabel = new JLabel("Phone");
        phoneText = new JTextField(35);

        phonePanel.add(phoneLabel);
        phonePanel.add(phoneText);

        mainPanel.add(phonePanel);

        addressStreetPanel = new JPanel();
        addressStreetLabel = new JLabel("Street address");
        addressStreetText = new JTextField(35);

        addressStreetPanel.add(addressStreetLabel);
        addressStreetPanel.add(addressStreetText);

        mainPanel.add(addressStreetPanel);

        addressCityPanel = new JPanel();
        addressCityLabel = new JLabel("City");
        addressCityText = new JTextField(35);

        addressCityPanel.add(addressCityLabel);
        addressCityPanel.add(addressCityText);

        mainPanel.add(addressCityPanel);

        addressCountyPanel = new JPanel();
        addressCountyLabel = new JLabel("County");
        addressCountyText = new JTextField(35);

        addressCountyPanel.add(addressCountyLabel);
        addressCountyPanel.add(addressCountyText);

        mainPanel.add(addressCountyPanel);

        addressPostalCodePanel = new JPanel();
        addressPostalCodeLabel = new JLabel("Postal code");
        addressPostalCodeText = new JTextField(35);

        addressPostalCodePanel.add(addressPostalCodeLabel);
        addressPostalCodePanel.add(addressPostalCodeText);

        mainPanel.add(addressPostalCodePanel);

        LeadSourcePanel = new JPanel();
        LeadSourceLabel = new JLabel("Lead Source");
        String lista_LeadSource[] = new String[10];
        int j=0;
        for (LeadSource l:LeadSource.values()) {
            lista_LeadSource[j++] = l.toString();
        }
        LeadSourceCombo = new JComboBox<>(lista_LeadSource);

        LeadSourcePanel.add(LeadSourceLabel);
        LeadSourcePanel.add(LeadSourceCombo);

        mainPanel.add(LeadSourcePanel);

        LeadStatusPanel = new JPanel();
        LeadStatusLabel = new JLabel("Lead Status");
        String lista_LeadStatus[] = new String[6];
        int k=0;
        for (LeadStatus ls:LeadStatus.values()) {
            lista_LeadStatus[k++] = ls.toString();
        }
        LeadStatusCombo = new JComboBox<>(lista_LeadStatus);

        LeadStatusPanel.add(LeadStatusLabel);
        LeadStatusPanel.add(LeadStatusCombo);

        mainPanel.add(LeadStatusPanel);

        LeadScorePanel = new JPanel();
        LeadScoreLabel = new JLabel("Lead Score");
        LeadScoreText = new JTextField(35);

        LeadScorePanel.add(LeadScoreLabel);
        LeadScorePanel.add(LeadScoreText);

        mainPanel.add(LeadScorePanel);

        ExperienceLevelPanel = new JPanel();
        ExperienceLevelLabel = new JLabel("Lead Status");
        String lista_ExperienceLevel[] = new String[3];
        int m=0;
        for (LeadStatus el:LeadStatus.values()) {
            lista_LeadStatus[m++] = el.toString();
        }
        ExperienceLevelCombo = new JComboBox<>(lista_ExperienceLevel);

        ExperienceLevelPanel.add(ExperienceLevelLabel);
        ExperienceLevelPanel.add(ExperienceLevelCombo);

        mainPanel.add(ExperienceLevelPanel);

        learningGoalPanel = new JPanel();
        learningGoalLabel = new JLabel("Learning Goal");
        learningGoalText = new JTextField(35);

        learningGoalPanel.add(learningGoalLabel);
        learningGoalPanel.add(learningGoalText);

        mainPanel.add(learningGoalPanel);

        this.firstContactDatePanel = new JPanel();
        this.firstContactDateLabel = new JLabel("First Contact Date");

        UtilDateModel model2 = new UtilDateModel();
        Properties p2 = new Properties();
        p2.put("text.today", "Today");
        p2.put("text.month", "Month");
        p2.put("text.year", "Year");
        JDatePanelImpl datePanel2 = new JDatePanelImpl(model2,p2);
        this.firstContactDatePicker = new JDatePickerImpl(datePanel2, new DateLabelFormatter());
        this.firstContactDatePanel.add(this.firstContactDateLabel);
        this.firstContactDatePanel.add(this.firstContactDatePicker);
        this.mainPanel.add(this.firstContactDatePanel);

        this.lastContactDatePanel = new JPanel();
        this.lastContactDateLabel = new JLabel("Last Contact Date");

        UtilDateModel model3 = new UtilDateModel();
        Properties p3 = new Properties();
        p3.put("text.today", "Today");
        p3.put("text.month", "Month");
        p3.put("text.year", "Year");
        JDatePanelImpl datePanel3 = new JDatePanelImpl(model3,p3);
        this.lastContactDatePicker = new JDatePickerImpl(datePanel3, new DateLabelFormatter());
        this.lastContactDatePanel.add(this.lastContactDateLabel);
        this.lastContactDatePanel.add(this.lastContactDatePicker);
        this.mainPanel.add(this.lastContactDatePanel);

        assignedToPanel = new JPanel();
        assignedToLabel = new JLabel("Assigned to: ");
        assignedToText = new JTextField(35);

        assignedToPanel.add(assignedToLabel);
        assignedToPanel.add(assignedToText);

        mainPanel.add(assignedToPanel);

        gdprConsentPanel = new JPanel();
        gdprConsentLabel = new JLabel("Consent GDPR");
        JCheckBox ckYesGDPR = new JCheckBox("Yes");

        gdprConsentPanel.add(gdprConsentLabel);
        gdprConsentPanel.add(ckYesGDPR);

        mainPanel.add(gdprConsentPanel);

        marketingConsentPanel = new JPanel();
        marketingConsentLabel = new JLabel("Marketing consent GDPR");
        JCheckBox ckYesMarketingGDPR = new JCheckBox("Yes");

        marketingConsentPanel.add(marketingConsentLabel);
        marketingConsentPanel.add(ckYesMarketingGDPR);

        mainPanel.add(marketingConsentPanel);

        this.add(mainPanel);
        this.setSize(new Dimension(1400,800));
        this.setVisible(true);
    }

    public static void main(String ...args) {
        AddContactFrame gui1 = new AddContactFrame();
    }
}
