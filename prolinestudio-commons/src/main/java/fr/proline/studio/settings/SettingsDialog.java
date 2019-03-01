package fr.proline.studio.settings;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Dialog to load settings from a file among a list of previously saved settings
 * @author JM235353
 */
public class SettingsDialog extends DefaultDialog {
    
    private static final String DEFAULT_SETTINGS = "Default Settings";
    
    private JTextField m_fileTextField;
    private JList<String> m_filesList;
    private JButton m_openButton;
    
    private boolean m_defaultSettings = false;
    private File m_selectedFile = null;
    
    private JFileChooser m_fileChooser = null;
    private String m_chooserDefaultDirectory = null;
    private ArrayList<String> m_potentialSettingsFile = null;
    private String m_settingsKey = null;
    
    
    public SettingsDialog(Window parent, String settingsKey) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        
        m_settingsKey = settingsKey;
        
        setTitle("Settings");
        
        setButtonVisible(BUTTON_HELP, false);
        
        m_chooserDefaultDirectory = SettingsUtils.readDefaultDirectory(settingsKey);
        m_potentialSettingsFile = SettingsUtils.readSettingsPaths(settingsKey);
        setInternalComponent(createInternalPanel(m_potentialSettingsFile));
    }
    
    private JPanel createInternalPanel(ArrayList<String> potentialSettingsFile) {
        JPanel internalPanel = new JPanel(new GridBagLayout());
        internalPanel.setBorder(BorderFactory.createTitledBorder(" Settings File Selection "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create graphical objects
        m_openButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        m_fileTextField = new JTextField(30);
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement(DEFAULT_SETTINGS);
        for (int i=0;i<potentialSettingsFile.size();i++) {
            listModel.addElement(potentialSettingsFile.get(i));
        }
        m_filesList = new JList<>(listModel);
        
        JScrollPane filesListScrollPane = new JScrollPane(m_filesList) {

            private final Dimension preferredSize = new Dimension(280, 140);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };
 
        // actions on object
        m_openButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_fileChooser == null) {
                    m_fileChooser = new JFileChooser(m_chooserDefaultDirectory);
                    m_fileChooser.setMultiSelectionEnabled(false);
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Proline Settings File", "settings");
                    m_fileChooser.addChoosableFileFilter(filter);
                    m_fileChooser.setFileFilter(filter);
                }
                int result = m_fileChooser.showOpenDialog(m_openButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    m_fileTextField.setText(m_fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
            
        });
        
        m_filesList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                String value = m_filesList.getSelectedValue();
                if (value != null) {
                    m_fileTextField.setText(m_filesList.getSelectedValue());
                }
            }
        });
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        internalPanel.add(m_fileTextField, c);
        
        c.gridx++;
        c.weightx = 0;
        internalPanel.add(m_openButton, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        internalPanel.add(filesListScrollPane, c);

        return internalPanel;
    }
    
    @Override
    protected boolean okCalled() {
        
        String filePath = m_fileTextField.getText().trim();
        
        if (filePath.isEmpty()) {
            setStatus(true, "You must choose a Settings File");
            highlight(m_fileTextField);
            return false;
        }
        
        if (filePath.compareToIgnoreCase(DEFAULT_SETTINGS) == 0) {
            m_defaultSettings = true;
            return true;
        }
        
        
        File f = new File(filePath);
        if (!f.exists()) {
            setStatus(true, "This file does not exist");
            highlight(m_fileTextField);
            return false;
        }
        if (f.isDirectory()) {
            setStatus(true, "You must select a File, not a Directory");
            highlight(m_fileTextField);
            return false;
        }
        if (!f.canRead()) {
            setStatus(true, "The file is not readable");
            highlight(m_fileTextField);
            return false;
        }

        m_selectedFile = f;
        
        String path = f.getAbsolutePath();
        boolean pathFound = false;
        for (int i=0;i<m_potentialSettingsFile.size();i++) {
            String pathCur = m_potentialSettingsFile.get(i);
            if (pathCur.compareTo(path) == 0) {
                pathFound = true;
                break;
            }
        }
        if (!pathFound) {
            m_potentialSettingsFile.add(0, f.getAbsolutePath());
        }
        SettingsUtils.writeSettingsPath(m_settingsKey, m_potentialSettingsFile);
        SettingsUtils.writeDefaultDirectory(m_settingsKey, f.getParent());
        
        return true;
    }
    
    
    
    @Override
    protected boolean cancelCalled() {
        return true;
    }
    
    public boolean isDefaultSettingsSelected() {
        return m_defaultSettings;
    }
    
    public File getSelectedFile() {
        return m_selectedFile;
    }
    

    
    


}
