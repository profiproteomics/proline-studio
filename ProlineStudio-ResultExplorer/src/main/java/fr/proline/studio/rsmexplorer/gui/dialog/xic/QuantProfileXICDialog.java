/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import java.awt.Dialog;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 * Dialog to compute the quantitation profile
 * @author MB243701
 */
public class QuantProfileXICDialog extends DefaultDialog {
    
    private static QuantProfileXICDialog m_singletonDialog = null;
    
    private QuantProfileXICPanel m_quantProfilePanel;
    
    public final static String SETTINGS_KEY = "QuantProfile";
    
     
    public static QuantProfileXICDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new QuantProfileXICDialog(parent);
        }

        return m_singletonDialog;
    }

    private QuantProfileXICDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Refine Proteins Sets Abundances");

        try {
            setHelpURL(new File(".").getCanonicalPath() + File.separatorChar + "Documentation" + File.separatorChar + "Proline_UserGuide_1.4RC1.docx.html#id.2dlolyb");
        } catch (IOException ex) {
            ;
        }

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);
       
        setResizable(true);
        
        init();
        
    }
    
    
    @Override
    protected boolean saveCalled() {
        // check parameters
        if (!checkParameters()) {
            return false;
        }

        JFileChooser fileChooser = SettingsUtils.getFileChooser(SETTINGS_KEY);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            FilePreferences filePreferences = new FilePreferences(f, null, "");

            // Save Parameters  
            ParameterList parameterList = m_quantProfilePanel.getParameterList();
            parameterList.saveParameters(filePreferences);
            
            SettingsUtils.addSettingsPath(SETTINGS_KEY, f.getAbsolutePath());
            SettingsUtils.writeDefaultDirectory(SETTINGS_KEY, f.getParent());
        }
        
        return false;
    }
    
    @Override
    protected boolean loadCalled() {

        SettingsDialog settingsDialog = new SettingsDialog(this, SETTINGS_KEY);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);
        
        if (settingsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            if (settingsDialog.isDefaultSettingsSelected()) {
                ParameterList parameterList = m_quantProfilePanel.getParameterList();
                parameterList.initDefaults();
            } else {
                try {
                    File settingsFile = settingsDialog.getSelectedFile();
                    FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");

                    
                    Preferences preferences = NbPreferences.root();
                    String[] keys = filePreferences.keys();
                    for (String key : keys) {
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }
                    
                    m_quantProfilePanel.getParameterList().loadParameters(filePreferences);
                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    setStatus(true, "Parsing of your Settings File failed");
                }
            }
        }
        
        return false;
    }
    
    
    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParameters()) {
            return false;
        }

        // save parameters
        Preferences preferences = NbPreferences.root();
        m_quantProfilePanel.getParameterList().saveParameters(preferences);

        return true;

    }

    
    private boolean checkParameters() {
        // check parameters
        ParameterError error = m_quantProfilePanel.getParameterList().checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        
        return true;
    }
    
    private void init() {
        m_quantProfilePanel = new QuantProfileXICPanel(false);
        Preferences preferences = NbPreferences.root();
        m_quantProfilePanel.getParameterList().loadParameters(preferences);

        
        setInternalComponent(m_quantProfilePanel);
    }
    
    public Map<String,Object> getQuantParams(){
        return m_quantProfilePanel.getQuantParams();
    }
}
