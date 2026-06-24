package crm.gui.panels;

import crm.model.entity.Course;
import crm.model.enums.CourseCategory;
import crm.model.enums.ExperienceLevel;
import crm.repository.CourseRepository;
import crm.service.course.CourseService;

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
import java.math.BigDecimal;
import java.util.List;

public class CoursesPanel extends JPanel {

    private final CourseService courseService = CourseService.getInstance();
    private final CourseRepository courseRepository = CourseRepository.getInstance();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Code", "Name", "Category", "Level", "Hours", "Individual Price", "Active"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public CoursesPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UiSupport.configureTable(table);

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadCourses();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("New Course");
        addButton.addActionListener(e -> openDialog(null));
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelected());
        JButton deactivateButton = new JButton("Deactivate");
        deactivateButton.addActionListener(e -> deactivateSelected());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());
        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deactivateButton);
        toolbar.add(refreshButton);
        return toolbar;
    }

    private void loadCourses() {
        try {
            populate(courseRepository.findAll());
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void populate(List<Course> courses) {
        tableModel.setRowCount(0);
        for (Course course : courses) {
            tableModel.addRow(new Object[]{
                    course.getId(),
                    course.getCode(),
                    course.getName(),
                    UiSupport.enumName(course.getCategory()),
                    UiSupport.enumName(course.getLevel()),
                    course.getDurationHours(),
                    UiSupport.money(course.getPriceIndividual()),
                    Boolean.TRUE.equals(course.getActive()) ? "Yes" : "No"
            });
        }
    }

    private void editSelected() {
        Long id = UiSupport.selectedId(table, tableModel);
        if (id == null) {
            UiSupport.showInfo(this, "Select a course from the table.");
            return;
        }
        try {
            openDialog(courseService.getById(id));
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void deactivateSelected() {
        Long id = UiSupport.selectedId(table, tableModel);
        if (id == null) {
            UiSupport.showInfo(this, "Select a course from the table.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate course #" + id + "? Historical data is preserved.",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            courseService.deactivateCourse(id);
            loadCourses();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private void openDialog(Course existing) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        CourseDialog dialog = new CourseDialog(owner, existing);
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        try {
            Course course = dialog.getCourse();
            if (existing == null) {
                courseService.createCourse(course);
            } else {
                courseService.updateCourse(course);
            }
            loadCourses();
        } catch (Exception ex) {
            UiSupport.showError(this, ex);
        }
    }

    private static class CourseDialog extends JDialog {

        private final Course existing;
        private boolean saved;

        private final JTextField codeField = new JTextField(24);
        private final JTextField nameField = new JTextField(24);
        private final JTextArea descriptionArea = new JTextArea(3, 24);
        private final JTextArea syllabusArea = new JTextArea(3, 24);
        private final JComboBox<CourseCategory> categoryCombo = UiSupport.enumCombo(CourseCategory.values());
        private final JComboBox<ExperienceLevel> levelCombo = UiSupport.enumCombo(ExperienceLevel.values());
        private final JTextField prerequisitesField = new JTextField(24);
        private final JTextField durationField = new JTextField(24);
        private final JTextField individualPriceField = new JTextField(24);
        private final JTextField groupPriceField = new JTextField(24);
        private final JTextField corporatePriceField = new JTextField(24);
        private final JTextField minParticipantsField = new JTextField(24);
        private final JTextField maxParticipantsField = new JTextField(24);
        private final JCheckBox activeCheck = new JCheckBox("Active");

        CourseDialog(Frame owner, Course existing) {
            super(owner, true);
            this.existing = existing;

            setTitle(existing == null ? "New Course" : "Edit Course");
            setSize(560, 650);
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;
            row = addRow(form, gbc, row, "Code:", codeField);
            row = addRow(form, gbc, row, "Name:", nameField);
            row = addRow(form, gbc, row, "Description:", new JScrollPane(descriptionArea));
            row = addRow(form, gbc, row, "Syllabus:", new JScrollPane(syllabusArea));
            row = addRow(form, gbc, row, "Category:", categoryCombo);
            row = addRow(form, gbc, row, "Level:", levelCombo);
            row = addRow(form, gbc, row, "Prerequisites:", prerequisitesField);
            row = addRow(form, gbc, row, "Duration hours:", durationField);
            row = addRow(form, gbc, row, "Individual price:", individualPriceField);
            row = addRow(form, gbc, row, "Group price:", groupPriceField);
            row = addRow(form, gbc, row, "Corporate price/day:", corporatePriceField);
            row = addRow(form, gbc, row, "Min participants:", minParticipantsField);
            row = addRow(form, gbc, row, "Max participants:", maxParticipantsField);
            addRow(form, gbc, row, "", activeCheck);

            if (existing != null) {
                populateFields(existing);
            } else {
                activeCheck.setSelected(true);
                minParticipantsField.setText("3");
                maxParticipantsField.setText("15");
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

        private void populateFields(Course course) {
            codeField.setText(course.getCode());
            nameField.setText(course.getName());
            descriptionArea.setText(course.getDescription());
            syllabusArea.setText(course.getSyllabus());
            if (course.getCategory() != null) {
                categoryCombo.setSelectedItem(course.getCategory());
            }
            if (course.getLevel() != null) {
                levelCombo.setSelectedItem(course.getLevel());
            }
            prerequisitesField.setText(course.getPrerequisites());
            durationField.setText(course.getDurationHours() == null ? "" : String.valueOf(course.getDurationHours()));
            individualPriceField.setText(UiSupport.money(course.getPriceIndividual()));
            groupPriceField.setText(UiSupport.money(course.getPriceGroup()));
            corporatePriceField.setText(UiSupport.money(course.getPriceCorporatePerDay()));
            minParticipantsField.setText(course.getMinParticipants() == null ? "" : String.valueOf(course.getMinParticipants()));
            maxParticipantsField.setText(course.getMaxParticipants() == null ? "" : String.valueOf(course.getMaxParticipants()));
            activeCheck.setSelected(Boolean.TRUE.equals(course.getActive()));
        }

        private void save() {
            try {
                buildCourse();
                saved = true;
                dispose();
            } catch (Exception ex) {
                UiSupport.showError(this, ex);
            }
        }

        Course getCourse() {
            return buildCourse();
        }

        boolean isSaved() {
            return saved;
        }

        private Course buildCourse() {
            Course course = existing != null ? existing : new Course();
            course.setCode(UiSupport.requiredText(codeField, "Code"));
            course.setName(UiSupport.requiredText(nameField, "Name"));
            course.setDescription(UiSupport.emptyToNull(descriptionArea.getText()));
            course.setSyllabus(UiSupport.emptyToNull(syllabusArea.getText()));
            course.setCategory((CourseCategory) categoryCombo.getSelectedItem());
            course.setLevel((ExperienceLevel) levelCombo.getSelectedItem());
            course.setPrerequisites(UiSupport.emptyToNull(prerequisitesField.getText()));
            Integer durationHours = UiSupport.parseInteger(durationField.getText(), "Duration hours");
            if (durationHours == null || durationHours <= 0) {
                throw new IllegalArgumentException("Duration hours must be positive.");
            }
            course.setDurationHours(durationHours);
            course.setPriceIndividual(defaultMoney(UiSupport.parseMoney(individualPriceField.getText(), "Individual price")));
            course.setPriceGroup(defaultMoney(UiSupport.parseMoney(groupPriceField.getText(), "Group price")));
            course.setPriceCorporatePerDay(defaultMoney(UiSupport.parseMoney(corporatePriceField.getText(), "Corporate price/day")));
            course.setMinParticipants(UiSupport.parseInteger(minParticipantsField.getText(), "Min participants"));
            course.setMaxParticipants(UiSupport.parseInteger(maxParticipantsField.getText(), "Max participants"));
            course.setActive(activeCheck.isSelected());
            return course;
        }

        private BigDecimal defaultMoney(BigDecimal value) {
            return value == null ? BigDecimal.ZERO : value;
        }
    }
}
