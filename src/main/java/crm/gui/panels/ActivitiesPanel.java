package crm.gui.panels;

import crm.model.entity.Activity;
import crm.model.entity.Contact;
import crm.model.enums.ActivityType;
import crm.repository.ActivityRepository;
import crm.service.activity.ActivityService;
import crm.service.contact.ContactService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiesPanel extends JPanel {

    private final ActivityService activityService = ActivityService.getInstance();
    private final ActivityRepository activityRepository = ActivityRepository.getInstance();
    private final ContactService contactService = ContactService.getInstance();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Type", "Contact", "Subject", "Scheduled", "Status", "Priority", "Outcome"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public ActivitiesPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UiSupport.configureTable(table);

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadActivities();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Log Activity");
        addButton.addActionListener(e -> openDialog());
        JButton completeButton = new JButton("Complete");
        completeButton.addActionListener(e -> completeSelected());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadActivities());
        toolbar.add(addButton);
        toolbar.add(completeButton);
        toolbar.add(refreshButton);
        return toolbar;
    }

    private void loadActivities() {
        try {
            populate(activityRepository.findAll());
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void populate(List<Activity> activities) {
        tableModel.setRowCount(0);
        Map<Long, String> contactNames = new HashMap<Long, String>();
        for (Contact contact : contactService.getAllContacts()) {
            contactNames.put(contact.getId(), UiSupport.contactName(contact));
        }
        for (Activity activity : activities) {
            tableModel.addRow(new Object[]{
                    activity.getId(),
                    UiSupport.enumName(activity.getActivityType()),
                    contactNames.get(activity.getContactId()),
                    activity.getSubject(),
                    UiSupport.formatDateTime(activity.getScheduledDate()),
                    activity.getStatus(),
                    activity.getPriority(),
                    activity.getOutcome()
            });
        }
    }

    private void openDialog() {
        try {
            List<Contact> contacts = contactService.getAllContacts();
            if (contacts.isEmpty()) {
                UiSupport.showInfo(this, "Create at least one contact before logging an activity.");
                return;
            }
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            ActivityDialog dialog = new ActivityDialog(owner, contacts);
            dialog.setVisible(true);
            if (!dialog.isSaved()) {
                return;
            }
            activityService.createActivity(dialog.getActivity());
            loadActivities();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void completeSelected() {
        Long id = UiSupport.selectedId(table, tableModel);
        if (id == null) {
            UiSupport.showInfo(this, "Select an activity from the table.");
            return;
        }

        JTextArea outcomeArea = new JTextArea(3, 24);
        JTextArea nextStepsArea = new JTextArea(3, 24);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Outcome:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(outcomeArea), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Next steps:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(nextStepsArea), gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Complete Activity",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            activityService.completeActivity(id, outcomeArea.getText().trim(), nextStepsArea.getText().trim());
            loadActivities();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private static class ActivityDialog extends JDialog {

        private final JComboBox<Contact> contactCombo;
        private final JComboBox<ActivityType> typeCombo = UiSupport.enumCombo(ActivityType.values());
        private final JTextField subjectField = new JTextField(24);
        private final JTextArea descriptionArea = new JTextArea(4, 24);
        private final JTextField scheduledField = new JTextField(24);
        private final JTextField durationField = new JTextField(24);
        private final JComboBox<String> priorityCombo = new JComboBox<String>(new String[]{"LOW", "MEDIUM", "HIGH"});
        private final JTextField assignedToField = new JTextField(24);
        private boolean saved;

        ActivityDialog(Frame owner, List<Contact> contacts) {
            super(owner, true);
            this.contactCombo = new JComboBox<Contact>(contacts.toArray(new Contact[0]));

            setTitle("Log Activity");
            setSize(520, 460);
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            contactCombo.setRenderer(new DefaultListCellRenderer() {
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
            scheduledField.setText(UiSupport.formatDateTime(LocalDateTime.now().plusHours(1)));
            durationField.setText("30");
            assignedToField.setText("1");

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;
            row = addRow(form, gbc, row, "Contact:", contactCombo);
            row = addRow(form, gbc, row, "Type:", typeCombo);
            row = addRow(form, gbc, row, "Subject:", subjectField);
            row = addRow(form, gbc, row, "Description:", new JScrollPane(descriptionArea));
            row = addRow(form, gbc, row, "Scheduled (yyyy-MM-dd HH:mm):", scheduledField);
            row = addRow(form, gbc, row, "Duration minutes:", durationField);
            row = addRow(form, gbc, row, "Priority:", priorityCombo);
            addRow(form, gbc, row, "Assigned to user ID:", assignedToField);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> save());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttons.add(saveButton);
            buttons.add(cancelButton);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(form, BorderLayout.CENTER);
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
                buildActivity();
                saved = true;
                dispose();
            } catch (Exception ex) {
                UiSupport.showError(this, ex);
            }
        }

        boolean isSaved() {
            return saved;
        }

        Activity getActivity() {
            return buildActivity();
        }

        private Activity buildActivity() {
            Contact contact = (Contact) contactCombo.getSelectedItem();
            Activity activity = new Activity();
            activity.setContactId(contact == null ? null : contact.getId());
            activity.setActivityType((ActivityType) typeCombo.getSelectedItem());
            activity.setSubject(UiSupport.requiredText(subjectField, "Subject"));
            activity.setDescription(UiSupport.emptyToNull(descriptionArea.getText()));
            activity.setScheduledDate(UiSupport.parseDateTime(scheduledField.getText(), "Scheduled date"));
            activity.setDurationMinutes(UiSupport.parseInteger(durationField.getText(), "Duration minutes"));
            activity.setPriority((String) priorityCombo.getSelectedItem());
            activity.setAssignedTo(UiSupport.parseLong(assignedToField.getText(), "Assigned to user ID"));
            activity.setCreatedBy(activity.getAssignedTo());
            activity.setStatus("SCHEDULED");
            return activity;
        }
    }
}
