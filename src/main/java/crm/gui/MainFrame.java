package crm.gui;

import crm.gui.panels.ActivitiesPanel;
import crm.gui.panels.ContactsPanel;
import crm.gui.panels.CoursesPanel;
import crm.gui.panels.PipelinePanel;
import crm.gui.panels.StatisticsPanel;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("CRM Training IT");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Contacts", new ContactsPanel());
        tabs.addTab("Courses", new CoursesPanel());
        tabs.addTab("Activities", new ActivitiesPanel());
        tabs.addTab("Pipeline", new PipelinePanel());
        tabs.addTab("Statistics", new StatisticsPanel());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBar.add(new JLabel("CRM Training IT | Desktop workspace | Java Swing"));
        return statusBar;
    }
}
