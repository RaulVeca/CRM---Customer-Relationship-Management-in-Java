package crm.gui.panels;

import crm.model.entity.Contact;
import crm.model.entity.Opportunity;
import crm.model.enums.LeadStatus;
import crm.model.enums.OpportunityStage;
import crm.repository.OpportunityRepository;
import crm.service.contact.ContactService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class StatisticsPanel extends JPanel {

    private final ContactService contactService = ContactService.getInstance();
    private final OpportunityRepository opportunityRepository = OpportunityRepository.getInstance();
    private final JLabel totalContactsLabel = new JLabel();
    private final JLabel contactDetailLabel = new JLabel("All contacts");
    private final DefaultTableModel statusModel = new DefaultTableModel(new Object[]{"Lead Status", "Contacts"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel pipelineModel = new DefaultTableModel(new Object[]{"Opportunity Stage", "Count"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel contactDetailModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Email", "Score", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable statusTable = new JTable(statusModel);
    private final JTable pipelineTable = new JTable(pipelineModel);
    private final JTable contactDetailTable = new JTable(contactDetailModel);
    private LeadStatus selectedLeadStatus;

    public StatisticsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        UiSupport.configureTable(statusTable);
        UiSupport.configureTable(pipelineTable);
        UiSupport.configureTable(contactDetailTable);
        statusTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedStatus();
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStatistics());
        JButton clearFilterButton = new JButton("Show All Contacts");
        clearFilterButton.addActionListener(e -> {
            selectedLeadStatus = null;
            statusTable.clearSelection();
            loadContactDetails();
        });
        toolbar.add(refreshButton);
        toolbar.add(clearFilterButton);
        toolbar.add(totalContactsLabel);

        JScrollPane statusPane = new JScrollPane(statusTable);
        statusPane.setBorder(BorderFactory.createTitledBorder("Lead Status Summary"));
        JScrollPane pipelinePane = new JScrollPane(pipelineTable);
        pipelinePane.setBorder(BorderFactory.createTitledBorder("Opportunity Pipeline Summary"));

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                statusPane, pipelinePane);
        topSplit.setResizeWeight(0.5);

        JPanel contactDetailsPanel = new JPanel(new BorderLayout(4, 4));
        contactDetailsPanel.setBorder(BorderFactory.createTitledBorder("Contacts Detail"));
        contactDetailsPanel.add(contactDetailLabel, BorderLayout.NORTH);
        contactDetailsPanel.add(new JScrollPane(contactDetailTable), BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                topSplit, contactDetailsPanel);
        mainSplit.setResizeWeight(0.45);

        add(toolbar, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            totalContactsLabel.setText("Total contacts: " + contactService.countTotal());

            statusModel.setRowCount(0);
            for (LeadStatus status : LeadStatus.values()) {
                statusModel.addRow(new Object[]{UiSupport.enumName(status), contactService.countByStatus(status)});
            }

            pipelineModel.setRowCount(0);
            for (OpportunityStage stage : OpportunityStage.values()) {
                List<Opportunity> opportunities = opportunityRepository.findByStage(stage);
                pipelineModel.addRow(new Object[]{UiSupport.enumName(stage), opportunities.size()});
            }

            loadContactDetails();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void updateSelectedStatus() {
        int viewRow = statusTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = statusTable.convertRowIndexToModel(viewRow);
        selectedLeadStatus = leadStatusFromDisplay(String.valueOf(statusModel.getValueAt(modelRow, 0)));
        loadContactDetails();
    }

    private void loadContactDetails() {
        try {
            List<Contact> contacts = selectedLeadStatus == null
                    ? contactService.getAllContacts()
                    : contactService.getContactsByStatus(selectedLeadStatus);

            contactDetailLabel.setText(selectedLeadStatus == null
                    ? "All contacts"
                    : "Contacts with status: " + UiSupport.enumName(selectedLeadStatus));

            contactDetailModel.setRowCount(0);
            for (Contact contact : contacts) {
                contactDetailModel.addRow(new Object[]{
                        contact.getId(),
                        UiSupport.contactName(contact),
                        contact.getEmail(),
                        contact.getLeadScore(),
                        UiSupport.enumName(contact.getLeadStatus())
                });
            }
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private LeadStatus leadStatusFromDisplay(String display) {
        for (LeadStatus status : LeadStatus.values()) {
            if (UiSupport.enumName(status).equals(display)) {
                return status;
            }
        }
        return null;
    }
}
