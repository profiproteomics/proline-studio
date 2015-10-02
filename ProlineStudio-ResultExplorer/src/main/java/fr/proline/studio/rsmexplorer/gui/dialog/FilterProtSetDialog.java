package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.task.FilterRSMProtSetsTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class FilterProtSetDialog extends DefaultDialog implements ComponentListener {

    private static FilterProtSetDialog m_singletonDialog = null;

    private ParameterList m_parameterList;
    private AbstractParameter[] m_proteinFilterParameters;
    private FilterProteinSetPanel m_proteinPrefiltersPanel;

    private final static String SETTINGS_KEY = "ProtSetFiltering";

    public static FilterProtSetDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new FilterProtSetDialog(parent);
        }
        return m_singletonDialog;
    }

    public FilterProtSetDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("ProteinSet Filtering");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=prolineconcepts:proteinsetsfilteringandvalidation");

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        m_parameterList = new ParameterList("ProtSet Filtering");
        createParameters();
        m_parameterList.updateIsUsed(NbPreferences.root());

        setInternalComponent(createInternalPanel());

        m_proteinPrefiltersPanel.initProteinFilterPanel();

    }

    private void createParameters() {
        
        m_proteinFilterParameters = new AbstractParameter[FilterRSMProtSetsTask.FILTER_KEYS.length + 1];
        m_proteinFilterParameters[0] = null;
        for (int index = 1; index <= FilterRSMProtSetsTask.FILTER_KEYS.length; index++) {
            m_proteinFilterParameters[index] = new IntegerParameter(FilterRSMProtSetsTask.FILTER_KEYS[index - 1], FilterRSMProtSetsTask.FILTER_NAME[index - 1], new JTextField(6), new Integer(1), new Integer(1), null);
            m_proteinFilterParameters[index].setAssociatedData(">=");
            AbstractParameter p = m_proteinFilterParameters[index];
            if (p != null) {
                p.setUsed(false);
                p.setCompulsory(false);
                m_parameterList.add(p);
            }
        }
    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        m_proteinPrefiltersPanel = new FilterProteinSetPanel(" Filter(s) ", m_proteinFilterParameters);
        m_proteinPrefiltersPanel.addComponentListener((ComponentListener) this);
        internalPanel.add(m_proteinPrefiltersPanel, c);

        return internalPanel;
    }

    public HashMap<String, String> getArguments() {
        return m_parameterList.getValues();
    }

    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParameters()) {
            return false;
        }

        // save parameters
        Preferences preferences = NbPreferences.root();
        saveParameters(preferences);

        return true;
    }

    private void saveParameters(Preferences preferences) {
        // Save Parameters        
        m_parameterList.saveParameters(preferences);

    }

    private boolean checkParameters() {
        // check parameters
        ParameterError error = m_parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        return true;
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

            saveParameters(filePreferences);

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
                m_parameterList.initDefaults();
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

                    m_parameterList.loadParameters(filePreferences);
                    m_proteinPrefiltersPanel.initProteinFilterPanel();                  

                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    setStatus(true, "Parsing of your Settings File failed");
                }
            }
        }

        return false;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        repack();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        
    }

}
