package org.protege.osgi.jdbc.prefs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.RegistryException;

public class EditorPanel extends JDialog {
    private static final long serialVersionUID = -8958695683502439830L;

    private Logger log = Logger.getLogger(EditorPanel.class);
    
    private ServiceTracker jdbcRegistryTracker;
    
    private JLabel status = new JLabel();
    private JTextField nameField;
    private JTextField classField;
    private JTextField fileField;
    private JButton fileButton;
    
    private DriverInfo info;
    
    private File defaultDir;
    private Preferences prefs;
    
    public EditorPanel(ServiceTracker jdbcRegistryTracker) {
        this.jdbcRegistryTracker = jdbcRegistryTracker;
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createStatus(), BorderLayout.NORTH);
        getContentPane().add(createCenterPane(), BorderLayout.CENTER);
        getContentPane().add(createButtons(), BorderLayout.SOUTH);
        prefs = PreferencesManager.getInstance().getPreferencesForSet(PreferencesPanel.PREFERENCES_SET, PreferencesPanel.DEFAULT_DRIVER_DIR);
        String dirName = prefs.getString(PreferencesPanel.DEFAULT_DRIVER_DIR, null);
        if (dirName != null) {
            defaultDir = new File(dirName);
            if (!defaultDir.exists()) {
                defaultDir = null;
            }
        }
    }
    
    public EditorPanel(ServiceTracker jdbcRegistryTracker,
                       String description,
                       String className,
                       File file)  {
        this(jdbcRegistryTracker);
        nameField.setText(description);
        classField.setText(className);
        fileField.setText(file.getAbsolutePath());
    }
    
    private JComponent createStatus() {
        return status = new JLabel();
    }
    
    private JComponent createCenterPane() {
        JPanel centerPane = new JPanel();
        centerPane.setLayout(new GridLayout(0,2));
        
        centerPane.add(new JLabel("Description:"));
        centerPane.add(nameField = new JTextField());
        
        centerPane.add(new JLabel("Class Name:"));
        centerPane.add(classField = new JTextField());
        
        centerPane.add(new JLabel("Driver File (jar):"));
        fileField = new JTextField();
        JLabel sample = new JLabel("/home/tredmond/dev/workspaces/protege4");
        Dimension size = sample.getPreferredSize();
        fileField.setPreferredSize(size);
        centerPane.add(fileField);
        
        centerPane.add(new JLabel());
        fileButton = new JButton("Browse");
        centerPane.add(fileButton);
        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc  = new JFileChooser(defaultDir);
                int retVal = fc.showOpenDialog(EditorPanel.this);
                if (retVal == JFileChooser.APPROVE_OPTION)  {
                    File file = fc.getSelectedFile();
                    defaultDir = file.getParentFile();
                    prefs.putString(PreferencesPanel.DEFAULT_DRIVER_DIR, defaultDir.getAbsolutePath());
                    fileField.setText(file.getPath());
                }
            }
        });
        return centerPane;
    }
    
    private JComponent createButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String className = classField.getText();
                File f  = new File(fileField.getText());
                try {
                    jdbcRegistryTracker.open();
                    for (Object o : jdbcRegistryTracker.getServices()) {
                        JdbcRegistry registry = (JdbcRegistry) o;
                        try {
                            registry.addJdbcDriver(className, f.toURI().toURL());
                            info = new DriverInfo(nameField.getText(), className, f);
                            dispose();
                            return;
                        }
                        catch (RegistryException re) {
                            log.info("Could not add driver to jdbc",re);
                            status.setText(re.getMessage());
                        } catch (MalformedURLException ex) {
                            log.error("Unexpected URL misconfiguration", ex);
                            status.setText(ex.getMessage());
                        }
                    }
                }
                finally {
                    jdbcRegistryTracker.close();
                }
            }
        });
        panel.add(ok);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                info = null;
                dispose();
            }
        });
        panel.add(cancel);
        return panel;
    }
    
    public DriverInfo askUserForDriverInfo() {
        setModal(true);
        pack();
        setVisible(true);
        return info;
    }

}
