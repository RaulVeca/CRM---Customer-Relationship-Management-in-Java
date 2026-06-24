package crm.gui.panels;

import crm.model.entity.Contact;
import crm.model.entity.Opportunity;
import crm.model.enums.DeliveryMode;
import crm.model.enums.OpportunityStage;
import crm.repository.OpportunityRepository;
import crm.service.contact.ContactService;
import crm.service.opportunity.OpportunityService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelinePanel extends JPanel {

    private final OpportunityService opportunityService = OpportunityService.getInstance();
    private final OpportunityRepository opportunityRepository = OpportunityRepository.getInstance();
    private final ContactService contactService = ContactService.getInstance();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Client", "Title", "Stage", "Estimated Value", "Quoted Value", "Probability", "Expected Close"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JCheckBox showClosedCheck = new JCheckBox("Show closed opportunities");

    public PipelinePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UiSupport.configureTable(table);

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadPipeline();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("New Opportunity");
        addButton.addActionListener(e -> openDialog());
        JButton moveButton = new JButton("Move Stage");
        moveButton.addActionListener(e -> moveSelected());
        JButton lostButton = new JButton("Mark Lost");
        lostButton.addActionListener(e -> markLostSelected());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPipeline());
        showClosedCheck.addActionListener(e -> loadPipeline());
        toolbar.add(addButton);
        toolbar.add(moveButton);
        toolbar.add(lostButton);
        toolbar.add(refreshButton);
        toolbar.add(showClosedCheck);
        return toolbar;
    }

    private void loadPipeline() {
        try {
            List<Opportunity> opportunities = showClosedCheck.isSelected()
                    ? opportunityRepository.findAll()
                    : opportunityService.getActivePipeline();
            populate(opportunities);
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void populate(List<Opportunity> opportunities) {
        tableModel.setRowCount(0);
        Map<Long, String> contactNames = new HashMap<Long, String>();
        for (Contact contact : contactService.getAllContacts()) {
            contactNames.put(contact.getId(), UiSupport.contactName(contact));
        }
        for (Opportunity opportunity : opportunities) {
            tableModel.addRow(new Object[]{
                    opportunity.getId(),
                    contactNames.get(opportunity.getClientId()),
                    opportunity.getTitle(),
                    UiSupport.enumName(opportunity.getStage()),
                    UiSupport.money(opportunity.getEstimatedValue()),
                    UiSupport.money(opportunity.getQuotedValue()),
                    opportunity.getProbabilityPercent() == null ? "" : opportunity.getProbabilityPercent() + "%",
                    UiSupport.formatDate(opportunity.getExpectedCloseDate())
            });
        }
    }

    private void openDialog() {
        try {
            List<Contact> contacts = corporateContacts();
            if (contacts.isEmpty()) {
                UiSupport.showInfo(this, "Create at least one corporate contact before creating an opportunity.");
                return;
            }
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            OpportunityDialog dialog = new OpportunityDialog(owner, contacts);
            dialog.setVisible(true);
            if (!dialog.isSaved()) {
                return;
            }
            opportunityService.createOpportunity(dialog.getOpportunity());
            loadPipeline();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private List<Contact> corporateContacts() {
        List<Contact> result = new ArrayList<Contact>();
        for (Contact contact : contactService.getAllContacts()) {
            if (contact.isCorporate()) {
                result.add(contact);
            }
        }
        return result;
    }

    private void moveSelected() {
        Long id = UiSupport.selectedId(table, tableModel);
        if (id == null) {
            UiSupport.showInfo(this, "Select an opportunity from the table.");
            return;
        }
        JComboBox<OpportunityStage> stageCombo = UiSupport.enumCombo(OpportunityStage.values());
        int result = JOptionPane.showConfirmDialog(this, stageCombo, "Move Opportunity Stage",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            opportunityService.moveToStage(id, (OpportunityStage) stageCombo.getSelectedItem());
            loadPipeline();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void markLostSelected() {
        Long id = UiSupport.selectedId(table, tableModel);
        if (id == null) {
            UiSupport.showInfo(this, "Select an opportunity from the table.");
            return;
        }
        String reason = JOptionPane.showInputDialog(this, "Lost reason:");
        if (reason == null) {
            return;
        }
        try {
            opportunityService.markAsLost(id, reason.trim());
            loadPipeline();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private static class OpportunityDialog extends JDialog {

        private final JComboBox<Contact> clientCombo;
        private final JTextField titleField = new JTextField(24);
        private final JTextArea descriptionArea = new JTextArea(3, 24);
        private final JTextField participantsField = new JTextField(24);
        private final JTextArea requirementsArea = new JTextArea(3, 24);
        private final JComboBox<DeliveryMode> deliveryCombo = UiSupport.enumCombo(DeliveryMode.values());
        private final JTextField locationField = new JTextField(24);
        private final JTextField desiredStartField = new JTextField(24);
        private final JTextField estimatedValueField = new JTextField(24);
        private final JTextField quotedValueField = new JTextField(24);
        private final JComboBox<OpportunityStage> stageCombo = UiSupport.enumCombo(OpportunityStage.values());
        private final JTextField expectedCloseField = new JTextField(24);
        private final JTextField competitorsField = new JTextField(24);
        private boolean saved;

        OpportunityDialog(Frame owner, List<Contact> contacts) {
            super(owner, true);
            this.clientCombo = new JComboBox<Contact>(contacts.toArray(new Contact[0]));

            setTitle("New Opportunity");
            setSize(580, 650);
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            clientCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Contact) {
                        setText(UiSupport.contactName((Contact) value));
                    }
                    return this;
                }
            });

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;
            row = addRow(form, gbc, row, "Corporate client:", clientCombo);
            row = addRow(form, gbc, row, "Title:", titleField);
            row = addRow(form, gbc, row, "Description:", new JScrollPane(descriptionArea));
            row = addRow(form, gbc, row, "Estimated participants:", participantsField);
            row = addRow(form, gbc, row, "Custom requirements:", new JScrollPane(requirementsArea));
            row = addRow(form, gbc, row, "Delivery mode:", deliveryCombo);
            row = addRow(form, gbc, row, "Preferred location:", locationField);
            row = addRow(form, gbc, row, "Desired start (yyyy-MM-dd):", desiredStartField);
            row = addRow(form, gbc, row, "Estimated value:", estimatedValueField);
            row = addRow(form, gbc, row, "Quoted value:", quotedValueField);
            row = addRow(form, gbc, row, "Stage:", stageCombo);
            row = addRow(form, gbc, row, "Expected close (yyyy-MM-dd):", expectedCloseField);
            addRow(form, gbc, row, "Competitors:", competitorsField);

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

        private void save() {
            try {
                buildOpportunity();
                saved = true;
                dispose();
            } catch (Exception ex) {
                UiSupport.showError(this, ex);
            }
        }

        boolean isSaved() {
            return saved;
        }

        Opportunity getOpportunity() {
            return buildOpportunity();
        }

        private Opportunity buildOpportunity() {
            Contact client = (Contact) clientCombo.getSelectedItem();
            Opportunity opportunity = new Opportunity();
            opportunity.setClientId(client == null ? null : client.getId());
            opportunity.setTitle(UiSupport.requiredText(titleField, "Title"));
            opportunity.setDescription(UiSupport.emptyToNull(descriptionArea.getText()));
            opportunity.setEstimatedParticipants(UiSupport.parseInteger(participantsField.getText(), "Estimated participants"));
            opportunity.setCustomRequirements(UiSupport.emptyToNull(requirementsArea.getText()));
            opportunity.setDeliveryMode((DeliveryMode) deliveryCombo.getSelectedItem());
            opportunity.setPreferredLocation(UiSupport.emptyToNull(locationField.getText()));
            opportunity.setDesiredStartDate(UiSupport.parseDate(desiredStartField.getText(), "Desired start"));
            opportunity.setEstimatedValue(UiSupport.parseMoney(estimatedValueField.getText(), "Estimated value"));
            opportunity.setQuotedValue(UiSupport.parseMoney(quotedValueField.getText(), "Quoted value"));
            OpportunityStage stage = (OpportunityStage) stageCombo.getSelectedItem();
            opportunity.setStage(stage);
            opportunity.setProbabilityPercent(stage == null ? null : stage.getDefaultProbability());
            opportunity.setExpectedCloseDate(UiSupport.parseDate(expectedCloseField.getText(), "Expected close"));
            opportunity.setCompetitors(UiSupport.emptyToNull(competitorsField.getText()));
            return opportunity;
        }
    }
}
