package fr.proline.studio.rserver.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class ImportMsnSetDialog extends DefaultDialog {
    
    
    private static ImportMsnSetDialog m_singletonDialog = null;
    
    private JTextField m_designFileTextField;
    private JTextField m_dataFileTextField;
    
    private JButton m_addDesignFileButton;
    private JButton m_addDataFileButton;
    
    private File m_defaultDirectory = null;
    
    public static ImportMsnSetDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ImportMsnSetDialog(parent);
        }

        m_singletonDialog.reinitialize();

        return m_singletonDialog;
    }
    
    private ImportMsnSetDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Import Msn Set");

        //setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:importmascot"); //JPM.TODO
        
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);
        setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);
        
        JPanel fileSelectionPanel = createFileSelectionPanel();
        
        setInternalComponent(fileSelectionPanel);

        restoreInitialParameters();
    }

    
    private JPanel createFileSelectionPanel() {

        // Creation of Objects for File Selection Panel
        JPanel fileSelectionPanel = new JPanel(new GridBagLayout());
        fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(" Files Selection "));

        m_dataFileTextField = new JTextField(30);
        m_designFileTextField = new JTextField(30);
        
        m_addDataFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        m_addDataFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        
        m_addDesignFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        m_addDesignFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));



        // Placement of Objects for File Selection Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        fileSelectionPanel.add(new JLabel("Data File:"), c);

        c.gridx++;
        c.weightx=1;
        fileSelectionPanel.add(m_dataFileTextField, c);
        
        c.gridx++;
        c.weightx = 0;
        fileSelectionPanel.add(m_addDataFileButton, c);

        
        c.gridx = 0;
        c.gridy++;
        fileSelectionPanel.add(new JLabel("Desgin File:"), c);

        c.gridx++;
        c.weightx=1;
        fileSelectionPanel.add(m_designFileTextField, c);
        
        c.gridx++;
        c.weightx = 0;
        fileSelectionPanel.add(m_addDesignFileButton, c);



        m_addDataFileButton.addActionListener(new AddFileActionListener(m_dataFileTextField));
        m_addDesignFileButton.addActionListener(new AddFileActionListener(m_designFileTextField));

        return fileSelectionPanel;

    }
        
    private class AddFileActionListener implements ActionListener {

        private JTextField m_fileTextField;

        public AddFileActionListener(JTextField fileTextField) {
            m_fileTextField = fileTextField;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fchooser = new JFileChooser();
            if ((m_defaultDirectory != null) && (m_defaultDirectory.isDirectory())) {
                fchooser.setCurrentDirectory(m_defaultDirectory);
            }
            fchooser.setMultiSelectionEnabled(false);

            int result = fchooser.showOpenDialog(m_singletonDialog);
            if (result == JFileChooser.APPROVE_OPTION) {

                File file = fchooser.getSelectedFile();
                if (file != null) {
                    m_fileTextField.setText(file.getAbsolutePath());

                    file = file.getParentFile();
                    if ((file != null) && (file.isDirectory())) {
                        m_defaultDirectory = file;
                    }
                }

            }
        }
    }
        
    private void reinitialize() {
        m_designFileTextField.setText("");
        m_dataFileTextField.setText("");
    }
    
    private void restoreInitialParameters() {
        Preferences preferences = NbPreferences.root();

        // Prefered User File Path
        String filePath = preferences.get("UserMsnSetFilePath", null);
        if (filePath != null) {
            File f = new File(filePath);
            if (f.isDirectory()) {
                m_defaultDirectory = f;
            }
        }
    }
    
    public String getDataFilePath() {
        return m_dataFileTextField.getText().trim();
    }
    public String getDesignFilePath() {
        return m_designFileTextField.getText().trim();
    }
    
    @Override
    protected boolean okCalled() {

        // check that files are existing
        String error = checkFilePath(getDataFilePath());
        if (error != null) {
            setStatus(true, error);
            highlight(m_dataFileTextField);
            return false;
        }
        error = checkFilePath(getDesignFilePath());
        if (error != null) {
            setStatus(true, error);
            highlight(m_designFileTextField);
            return false;
        }
        
        // msn set user file path
        if (m_defaultDirectory != null) {
            Preferences preferences = NbPreferences.root();
            preferences.put("UserIdentificationFilePath", m_defaultDirectory.getAbsolutePath());
        }


        return true;

    }
    
    private String checkFilePath(String path) {
        
        if (path.isEmpty()) {
            return path + "You must select a file";
        }
        
        File f = new File(path);
        if (!f.exists()) {
            return path + " file does not exist";
        } else if (f.isDirectory()) {
            return path + " is a directory";
        } else if (!f.canRead()) {
            return path + " is not readable";
        }

        return null;
    }
        
    @Override
    protected boolean cancelCalled() {
        return true;
    }
    
}
