package crm.gui.panels;

import crm.model.entity.Contact;
import crm.model.enums.ContactType;
import crm.model.enums.ExperienceLevel;
import crm.model.enums.LeadSource;
import crm.model.enums.LeadStatus;
import crm.service.contact.ContactService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.List;

public class ContactsPanel extends JPanel {

    private final ContactService contactService = ContactService.getInstance();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Type", "Name", "Email", "Phone", "Status", "Score", "Source"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(24);

    public ContactsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UiSupport.configureTable(table);

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadContacts();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton individualButton = new JButton("New Individual Lead");
        individualButton.addActionListener(e -> openDialog(null, ContactType.INDIVIDUAL));

        JButton corporateButton = new JButton("New Corporate Client");
        corporateButton.addActionListener(e -> openDialog(null, ContactType.CORPORATE));

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelected());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelected());

        JButton hotLeadsButton = new JButton("Hot Leads");
        hotLeadsButton.addActionListener(e -> loadHotLeads());

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchContacts());

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            searchField.setText("");
            loadContacts();
        });

        toolbar.add(individualButton);
        toolbar.add(corporateButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(hotLeadsButton);
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(searchButton);
        toolbar.add(resetButton);
        return toolbar;
    }

    private void loadContacts() {
        try {
            populate(contactService.getAllContacts());
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void searchContacts() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadContacts();
            return;
        }
        try {
            populate(contactService.searchContacts(query, 0, UiSupport.PAGE_SIZE));
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void loadHotLeads() {
        try {
            populate(contactService.getHotLeads(50));
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void populate(List<Contact> contacts) {
        tableModel.setRowCount(0);
        for (Contact contact : contacts) {
            tableModel.addRow(new Object[]{
                    contact.getId(),
                    UiSupport.enumName(contact.getContactType()),
                    UiSupport.contactName(contact),
                    contact.getEmail(),
                    contact.getPhone(),
                    UiSupport.enumName(contact.getLeadStatus()),
                    contact.getLeadScore(),
                    UiSupport.enumName(contact.getLeadSource())
            });
        }
    }

    private void editSelected() {
        Long id = UiSupport.selectedId(table, tableModel);
        if (id == null) {
            UiSupport.showInfo(this, "Select a contact from the table.");
            return;
        }
        try {
            Contact contact = contactService.getById(id);
            openDialog(contact, contact.getContactType());
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void deleteSelected() {
        Long id = UiSupport.selectedId(table, tableModel);
        if (id == null) {
            UiSupport.showInfo(this, "Select a contact from the table.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete contact #" + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            contactService.deleteContact(id);
            loadContacts();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void openDialog(Contact existing, ContactType type) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ContactDialog dialog = new ContactDialog(owner, existing, type);
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        try {
            Contact contact = dialog.getContact();
            if (existing == null) {
                contactService.createContact(contact);
            } else {
                contactService.updateContact(contact);
            }
            loadContacts();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private static class ContactDialog extends JDialog {

        private final Contact existing;
        private final ContactType type;
        private boolean saved;

        private final JTextField firstNameField = new JTextField(24);
        private final JTextField lastNameField = new JTextField(24);
        private final JTextField companyNameField = new JTextField(24);
        private final JTextField fiscalCodeField = new JTextField(24);
        private final JTextField registrationField = new JTextField(24);
        private final JTextField industryField = new JTextField(24);
        private final JTextField employeesField = new JTextField(24);
        private final JTextField emailField = new JTextField(24);
        private final JTextField phoneField = new JTextField(24);
        private final JTextField streetField = new JTextField(24);
        private final JTextField cityField = new JTextField(24);
        private final JTextField countyField = new JTextField(24);
        private final JTextField postalCodeField = new JTextField(24);
        private final JComboBox<LeadSource> sourceCombo = UiSupport.enumCombo(LeadSource.values());
        private final JComboBox<LeadStatus> statusCombo = UiSupport.enumCombo(LeadStatus.values());
        private final JComboBox<ExperienceLevel> levelCombo = UiSupport.enumCombo(ExperienceLevel.values());
        private final JTextArea learningGoalArea = new JTextArea(3, 24);
        private final JCheckBox gdprCheck = new JCheckBox("GDPR consent");
        private final JCheckBox marketingCheck = new JCheckBox("Marketing consent");

        ContactDialog(Frame owner, Contact existing, ContactType type) {
            super(owner, true);
            this.existing = existing;
            this.type = type;

            setTitle(existing == null ? "New Contact" : "Edit Contact");
            setSize(520, type == ContactType.CORPORATE ? 620 : 560);
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;
            if (type == ContactType.INDIVIDUAL) {
                row = addRow(form, gbc, row, "First name:", firstNameField);
                row = addRow(form, gbc, row, "Last name:", lastNameField);
            } else {
                row = addRow(form, gbc, row, "Company name:", companyNameField);
                row = addRow(form, gbc, row, "Fiscal code:", fiscalCodeField);
                row = addRow(form, gbc, row, "Registration number:", registrationField);
                row = addRow(form, gbc, row, "Industry:", industryField);
                row = addRow(form, gbc, row, "Employee count:", employeesField);
            }
            row = addRow(form, gbc, row, "Email:", emailField);
            row = addRow(form, gbc, row, "Phone:", phoneField);
            row = addRow(form, gbc, row, "Street:", streetField);
            row = addRow(form, gbc, row, "City:", cityField);
            row = addRow(form, gbc, row, "County:", countyField);
            row = addRow(form, gbc, row, "Postal code:", postalCodeField);
            row = addRow(form, gbc, row, "Lead source:", sourceCombo);
            row = addRow(form, gbc, row, "Lead status:", statusCombo);
            row = addRow(form, gbc, row, "Experience level:", levelCombo);
            row = addRow(form, gbc, row, "Learning goal:", new JScrollPane(learningGoalArea));
            row = addRow(form, gbc, row, "", gdprCheck);
            addRow(form, gbc, row, "", marketingCheck);

            if (existing != null) {
                populateFields(existing);
            } else {
                gdprCheck.setSelected(true);
            }

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> save());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttons.add(saveButton);
            buttons.add(cancelButton);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(new JScrollPane(form), BorderLayout.CENTER);
            getContentPane().add(buttons, BorderLayout.SOUTH);
        }

        private int addRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            form.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            form.add(field, gbc);
            return row + 1;
        }

        private void populateFields(Contact contact) {
            firstNameField.setText(contact.getFirstName());
            lastNameField.setText(contact.getLastName());
            companyNameField.setText(contact.getCompanyName());
            fiscalCodeField.setText(contact.getFiscalCode());
            registrationField.setText(contact.getRegistrationNumber());
            industryField.setText(contact.getIndustry());
            employeesField.setText(contact.getEmployeeCount() == null ? "" : String.valueOf(contact.getEmployeeCount()));
            emailField.setText(contact.getEmail());
            phoneField.setText(contact.getPhone());
            streetField.setText(contact.getAddressStreet());
            cityField.setText(contact.getAddressCity());
            countyField.setText(contact.getAddressCounty());
            postalCodeField.setText(contact.getAddressPostalCode());
            if (contact.getLeadSource() != null) {
                sourceCombo.setSelectedItem(contact.getLeadSource());
            }
            if (contact.getLeadStatus() != null) {
                statusCombo.setSelectedItem(contact.getLeadStatus());
            }
            if (contact.getExperienceLevel() != null) {
                levelCombo.setSelectedItem(contact.getExperienceLevel());
            }
            learningGoalArea.setText(contact.getLearningGoal());
            gdprCheck.setSelected(Boolean.TRUE.equals(contact.getGdprConsent()));
            marketingCheck.setSelected(Boolean.TRUE.equals(contact.getMarketingConsent()));
        }

        private void save() {
            try {
                buildContact();
                saved = true;
                dispose();
            } catch (Exception ex) {
                UiSupport.showError(this, ex);
            }
        }

        Contact getContact() {
            return buildContact();
        }

        boolean isSaved() {
            return saved;
        }

        private Contact buildContact() {
            Contact contact = existing != null ? existing : new Contact();
            contact.setContactType(type);

            if (type == ContactType.INDIVIDUAL) {
                String firstName = UiSupport.emptyToNull(firstNameField.getText());
                String lastName = UiSupport.emptyToNull(lastNameField.getText());
                if (firstName == null && lastName == null) {
                    throw new IllegalArgumentException("First name or last name is required.");
                }
                contact.setFirstName(firstName);
                contact.setLastName(lastName);
                contact.setCompanyName(null);
            } else {
                contact.setCompanyName(UiSupport.requiredText(companyNameField, "Company name"));
                contact.setFiscalCode(UiSupport.emptyToNull(fiscalCodeField.getText()));
                contact.setRegistrationNumber(UiSupport.emptyToNull(registrationField.getText()));
                contact.setIndustry(UiSupport.emptyToNull(industryField.getText()));
                contact.setEmployeeCount(UiSupport.parseInteger(employeesField.getText(), "Employee count"));
            }

            contact.setEmail(UiSupport.requiredText(emailField, "Email").toLowerCase());
            contact.setPhone(UiSupport.emptyToNull(phoneField.getText()));
            contact.setAddressStreet(UiSupport.emptyToNull(streetField.getText()));
            contact.setAddressCity(UiSupport.emptyToNull(cityField.getText()));
            contact.setAddressCounty(UiSupport.emptyToNull(countyField.getText()));
            contact.setAddressPostalCode(UiSupport.emptyToNull(postalCodeField.getText()));
            contact.setLeadSource((LeadSource) sourceCombo.getSelectedItem());
            contact.setLeadStatus((LeadStatus) statusCombo.getSelectedItem());
            contact.setExperienceLevel((ExperienceLevel) levelCombo.getSelectedItem());
            contact.setLearningGoal(UiSupport.emptyToNull(learningGoalArea.getText()));
            contact.setGdprConsent(gdprCheck.isSelected());
            if (gdprCheck.isSelected() && contact.getGdprConsentDate() == null) {
                contact.setGdprConsentDate(LocalDateTime.now());
            }
            contact.setMarketingConsent(marketingCheck.isSelected());
            if (contact.getFirstContactDate() == null) {
                contact.setFirstContactDate(LocalDateTime.now());
            }
            contact.setLastContactDate(LocalDateTime.now());
            return contact;
        }
    }
}
