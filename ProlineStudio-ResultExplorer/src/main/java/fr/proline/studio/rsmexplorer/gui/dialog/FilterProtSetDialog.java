package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.task.FilterRSMProtSetsTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterComboboxRenderer;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class FilterProtSetDialog extends DefaultDialog {

    private static FilterProtSetDialog m_singletonDialog = null;

    private ParameterList m_parameterList;
    private AbstractParameter[] m_proteinFilterParameters;

    private JPanel m_proteinPrefiltersSelectedPanel = null;
    private JComboBox m_proteinPrefilterJComboBox = null;
    private JButton m_addProteinPrefilterButton = null;

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

        initProteinFilterPanel();

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
        internalPanel.add(createProteinSetFilterPanel(), c);

        return internalPanel;
    }

    private JPanel createProteinSetFilterPanel() {
        JPanel prefilterPanel = new JPanel(new GridBagLayout());
        prefilterPanel.setBorder(BorderFactory.createTitledBorder(" Filter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_proteinPrefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        prefilterPanel.add(m_proteinPrefiltersSelectedPanel, c);

        m_proteinPrefilterJComboBox = new JComboBox(m_proteinFilterParameters);
        m_proteinPrefilterJComboBox.setRenderer(new ParameterComboboxRenderer(null));
        m_addProteinPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        m_addProteinPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(m_proteinPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        prefilterPanel.add(m_addProteinPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);

        m_addProteinPrefilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractParameter p = (AbstractParameter) m_proteinPrefilterJComboBox.getSelectedItem();
                if (p == null) {
                    return;
                }
                p.setUsed(true);
                initProteinFilterPanel();
            }
        });

        return prefilterPanel;
    }

    private void initProteinFilterPanel() {

        m_proteinPrefiltersSelectedPanel.removeAll();
        m_proteinPrefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        boolean putAndInFront = false;
        for (final AbstractParameter p : m_proteinFilterParameters) {
            if ((p != null) && (p.isUsed())) {

                c.gridx = 0;
                if (putAndInFront) {
                    m_proteinPrefiltersSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    m_proteinPrefiltersSelectedPanel.add(new JLabel("   "), c);
                }

                c.gridx++;
                JLabel prefilterNameLabel = new JLabel(p.getName());
                prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                m_proteinPrefiltersSelectedPanel.add(prefilterNameLabel, c);

                c.gridx++;
                JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
                cmpLabel.setHorizontalAlignment(JLabel.CENTER);
                m_proteinPrefiltersSelectedPanel.add(cmpLabel, c);

                c.weightx = 1;
                c.gridx++;
                m_proteinPrefiltersSelectedPanel.add(p.getComponent(), c);

                c.weightx = 0;
                c.gridx++;
                JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                removeButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        p.setUsed(false);
                        initProteinFilterPanel();
                    }
                });
                m_proteinPrefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                m_proteinPrefilterJComboBox.addItem(p);
            }
        }

        boolean hasUnusedParameters = (nbUsed != m_proteinFilterParameters.length);
        m_proteinPrefilterJComboBox.setVisible(hasUnusedParameters);
        m_addProteinPrefilterButton.setVisible(hasUnusedParameters);

        repack();
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
                    initProteinFilterPanel();

                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    setStatus(true, "Parsing of your Settings File failed");
                }
            }
        }

        return false;
    }

}
