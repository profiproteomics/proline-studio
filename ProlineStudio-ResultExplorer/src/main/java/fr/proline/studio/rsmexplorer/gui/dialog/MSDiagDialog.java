package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import javax.swing.*;
import java.util.prefs.Preferences;

import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * Dialog used to set MSDiag settings
 * @author AW
 */

public class MSDiagDialog extends DefaultDialog {

    private static MSDiagDialog m_singletonDialog = null;
    private static final String MSDIAG_SETTINGS = "General Settings";
  
    private final static String SETTINGS_KEY = "Settings key for msdiag";
  
    private JComboBox m_parserComboBox;
    private ParameterList m_sourceParameterList;
    private JPanel m_parserParametersPanel = null;

    public static MSDiagDialog getDialog(Window parent/*, long projectId*/) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new MSDiagDialog(parent);
        }

        m_singletonDialog.reinitialize();

        return m_singletonDialog;
    }

    private MSDiagDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);


        setTitle("Define settings for Statistical Reports (MSDiag)");
       
        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:msdiag");    
        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);
        
        setResizable(true);
        setMinimumSize(new Dimension(200, 240));

        initInternalPanel();

        restoreInitialParameters(NbPreferences.root());
    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());


        // create all other parameters panel
        JPanel allParametersPanel = createAllParametersPanel();


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridy++;
        c.weighty = 0;
        internalPanel.add(allParametersPanel, c);

        setInternalComponent(internalPanel);
        

    }

    private JPanel createAllParametersPanel() {
        JPanel allParametersPanel = new JPanel(new GridBagLayout());
        allParametersPanel.setBorder(BorderFactory.createTitledBorder(" MSDiag Reports Settings "));
        
        // create parserPanel
        JPanel settingsPanel = createSettingsPanel();
        
       
        // create parameter panel
        m_parserParametersPanel = createMSDiagParametersPanel();
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        allParametersPanel.add(settingsPanel, c);
        
        c.gridy++;
        allParametersPanel.add(m_parserParametersPanel, c);
        
        // init the first parser parameters panel selected
        m_parserComboBox.setSelectedIndex(0);
        
        return allParametersPanel;
    }
  
    private JPanel createSettingsPanel() {
        // Creation of Objects for the Parser Panel
        JPanel parserPanel = new JPanel(new GridBagLayout());

        m_parserComboBox = new JComboBox(createParameters());
        

        
        // Placement of Objects for Parser Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        parserPanel.add(m_parserComboBox, c);

//      

            
        m_parserComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_parserComboBox.getSelectedIndex();

                initParameters();

                // resize the dialog
                repack();
            }
        });
        

        
        return parserPanel;
    }
   
     
    
    private JPanel createMSDiagParametersPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(" MSDiag Settings... "));
        return panel;
    }
    
    private void initParameters() {


        // remove all parameters
        m_parserParametersPanel.removeAll();
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        m_parserParametersPanel.add(parameterList.getPanel(), c);

    }
    


    private void reinitialize() {
        
     
        
        // reinit of some parameters
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.clean();
        
  
    }

    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParametersForOK()) {
            return false;
        }
        
        saveParameters(NbPreferences.root());

        return true;

    }

    

    @Override
    protected boolean cancelCalled() {
        return true;
    }

//    @Override
//    protected boolean saveCalled() {
//        // check parameters
//        if (!checkParametersForSave()) {
//            return false;
//        }
//
//        JFileChooser fileChooser = SettingsUtils.getFileChooser(SETTINGS_KEY);
//        int result = fileChooser.showSaveDialog(this);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            File f = fileChooser.getSelectedFile();
//            FilePreferences filePreferences = new FilePreferences(f, null, "");
//
//            saveParameters(filePreferences);
//            
//            SettingsUtils.addSettingsPath(SETTINGS_KEY, f.getAbsolutePath());
//            SettingsUtils.writeDefaultDirectory(SETTINGS_KEY, f.getParent());
//        }
//
//        return false;
//    }
    
    @Override
    protected boolean loadCalled() {

        SettingsDialog settingsDialog = new SettingsDialog(this, SETTINGS_KEY);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);
        
        if (settingsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            if (settingsDialog.isDefaultSettingsSelected()) {
                ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
                parameterList.initDefaults();
            } else {
                try {
                    File settingsFile = settingsDialog.getSelectedFile();
                    FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");

                    
                    Preferences preferences = NbPreferences.root();
                    String[] keys = filePreferences.keys();
                    for (int i=0;i<keys.length;i++) {
                        String key = keys[i];
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }
                    
                    
                    restoreInitialParameters(preferences);
                    
                    ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
                    parameterList.loadParameters(filePreferences);
                    //m_sourceParameterList.loadParameters(filePreferences);
                    
                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    setStatus(true, "Parsing of your Settings File failed");
                }
            }
        }
        
        return false;
    }
 
    
   
   private boolean checkParametersForOK() {
        // check files selected
        
        // if problem:    return false;
        

        return checkParametersForSave();
   }
    
   private boolean checkParametersForSave() {
        
 //       ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        

        
        // check source parameters
        //ParameterError error = m_sourceParameterList.checkParameters();
        
        
        // check specific parameters
//        if (error == null) {
//            error = parameterList.checkParameters();
//        }
//        
//        // report error
//        if (error != null) {
//            setStatus(true, error.getErrorMessage());
//            highlight(error.getParameterComponent());
//            return false;
//        }
               
        
        return true;
    }
    
    private void saveParameters(Preferences preferences) {
        
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();

        // save parser
        String parserSelected = parameterList.toString();
        preferences.put("IdentificationParser", parserSelected);
        
        // save file path
               
        // Save Other Parameters    
        //m_sourceParameterList.saveParameters(preferences);
        //parameterList.saveParameters(preferences);
        
        

    }
    


    private void restoreInitialParameters(Preferences preferences) {
       

    }


    
    public HashMap<String, String> getMSDiagSettings() {
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        return parameterList.getValues();
    }
    
   
    
    
    private ParameterList[] createParameters() {
        ParameterList[] plArray = new ParameterList[1];
        plArray[0] = createMSDiagSettings();

        return plArray;
    }
    
    private ParameterList createMSDiagSettings() {
        ParameterList parameterList = new ParameterList(MSDIAG_SETTINGS);
        parameterList.add(new StringParameter("score.windows", "Score windows (ex: 20-40-60)", JTextField.class, "20-40-60", null, null));
        parameterList.add(new IntegerParameter("max.rank", "Max rank", JTextField.class, new Integer(1), new Integer(0), null));
        //parameterList.add(new IntegerParameter("scan.groups.size", "Scan groups size", JTextField.class, new Integer(1), new Integer(0), new Integer(0)));
        
        return parameterList;
    }
    

   
    
    public class CertifyIdentificationProgress implements ProgressInterface {

        private boolean m_isLoaded = false;

        @Override
        public boolean isLoaded() {
            return m_isLoaded;
        }

        @Override
        public int getLoadingPercentage() {
            return 0; // progress bar displayed as a waiting bar
        }

        public void setLoaded() {
            m_isLoaded = true;
        }
    };
        
    
}

