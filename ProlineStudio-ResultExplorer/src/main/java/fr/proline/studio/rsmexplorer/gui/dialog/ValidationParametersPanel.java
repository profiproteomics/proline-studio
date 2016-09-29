/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterComboboxRenderer;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author AK249877
 */
public class ValidationParametersPanel extends JPanel implements InternalPanelInterface {

    private Window m_parent;
    private DefaultDialogInterface m_dialog;

    private static ValidationDialog m_singletonDialog = null;

    private final static String SETTINGS_KEY = "Validation";

    private final static String[] FDR_ON_VALUES = {null, "Score", "e-Value", "Adjusted e-Value", "Identity p-Value", "Homology p-Value"};
    private final static String[] FDR_ON_VALUES_ASSOCIATED_KEYS = {null, "SCORE", "MASCOT_EVALUE", "MASCOT_ADJUSTED_EVALUE", "SCORE_IT_P-VALUE", "SCORE_HT_P-VALUE"};

    private final static String[] SCORING_TYPE_OPTIONS = {"Standard", "Mascot Modified Mudpit"};
    private final static String[] SCORING_TYPE_VALUES = {"mascot:standard score", "mascot:modified mudpit score"};

    private AbstractParameter[] m_psmPrefilterParameters;

    private AbstractParameter m_fdrFilterParameter;
    private ObjectParameter<String> m_fdrOnValueParameter;
    private AbstractParameter m_proteinFdrFilterParameter;

    private JPanel m_psmPrefiltersSelectedPanel = null;
    private JComboBox m_psmPrefilterJComboBox = null;
    private JButton m_addPsmPrefilterButton = null;

    private AbstractParameter[] m_proteinPrefilterParameters;
    private FilterProteinSetPanel m_proteinPrefiltersPanel;

    private JLabel m_fdrLabel = null;
    private JTextField m_fdrTextField = null;
    private JLabel m_fdrPercentageLabel = null;

    private JLabel m_proteinFdrLabel = null;
    private JTextField m_proteinFdrTextField = null;
    private JLabel m_proteinFdrPercentageLabel = null;
    private JComboBox m_fdrOnValueComboBox = null;
    private JCheckBox m_fdrCheckbox = null;
    private JCheckBox m_proteinFdrCheckbox = null;

    private ChangeTypicalProteinPanel m_changeTypicalPanel = null;

    private JComboBox m_proteinScoringTypeCbx = null;

    private ParameterList m_parameterList;

    private JCheckBox m_typicalProteinMatchCheckBox;

    public enum DecoyStatus {
        WAITING,
        HAS_DECOY,
        NO_DECOY
    }

    private DecoyStatus m_hasDecoy = DecoyStatus.WAITING;

    public ValidationParametersPanel(Window parent) {

        this.m_parent = parent;

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        this.add(createPSMPanel(), c);

        c.gridy++;
        this.add(createProteinSetFilterPanel(), c);

    }

    private JPanel createPSMPanel() {
        JPanel psmPanel = new JPanel(new GridBagLayout());
        psmPanel.setBorder(BorderFactory.createTitledBorder(" PSM"));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        psmPanel.add(createPsmPreFilterPanel(), c);

        c.gridy++;
        psmPanel.add(createFDRFilterPanel(), c);

        return psmPanel;
    }

    private JPanel createPsmPreFilterPanel() {
        JPanel prefilterPanel = new JPanel(new GridBagLayout());
        prefilterPanel.setBorder(BorderFactory.createTitledBorder(" Prefilter(s) "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_psmPrefiltersSelectedPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        prefilterPanel.add(m_psmPrefiltersSelectedPanel, c);

        m_psmPrefilterJComboBox = new JComboBox(m_psmPrefilterParameters);
        m_psmPrefilterJComboBox.setRenderer(new ParameterComboboxRenderer(null));
        m_addPsmPrefilterButton = new JButton(IconManager.getIcon(IconManager.IconType.PLUS));
        m_addPsmPrefilterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(m_psmPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 0;
        prefilterPanel.add(m_addPsmPrefilterButton, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);

        m_addPsmPrefilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractParameter p = (AbstractParameter) m_psmPrefilterJComboBox.getSelectedItem();
                if (p == null) {
                    return;
                }
                p.setUsed(true);
                initPsmPrefilterPanel();
            }
        });

        return prefilterPanel;
    }

    private JPanel createFDRFilterPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Filter "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        boolean fdrUsed = m_fdrFilterParameter.isUsed();
        m_fdrCheckbox = new JCheckBox();
        m_fdrLabel = new JLabel("Ensure FDR <=");
        m_fdrPercentageLabel = new JLabel("%  on");

        updateFdrObjects(fdrUsed);

        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(m_fdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(m_fdrLabel, c);

        c.gridx++;
        fdrPanel.add(m_fdrTextField, c);

        c.gridx++;

        fdrPanel.add(m_fdrPercentageLabel, c);

        c.gridx++;
        fdrPanel.add(m_fdrOnValueComboBox, c);

        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        m_fdrCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_fdrCheckbox.isSelected());
                updateFdrObjects(enabled);
            }
        });

        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (m_fdrCheckbox.isSelected());
                if (!enabled) {
                    updateFdrObjects(true);

                    if (e.getSource().equals(m_fdrTextField)) {
                        m_fdrTextField.requestFocusInWindow();
                    }

                }
            }
        };

        m_fdrLabel.addMouseListener(actionOnClick);
        m_fdrTextField.addMouseListener(actionOnClick);

        return fdrPanel;
    }

    private JPanel createProteinSetFilterPanel() {
        JPanel proteinSetFilterPanel = new JPanel(new GridBagLayout());
        proteinSetFilterPanel.setBorder(BorderFactory.createTitledBorder(" Protein Set "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        m_proteinPrefiltersPanel = new FilterProteinSetPanel(" Filter(s) ", m_proteinPrefilterParameters);
        m_proteinPrefiltersPanel.addComponentListener((ComponentListener) this);
        proteinSetFilterPanel.add(m_proteinPrefiltersPanel, c);

        c.gridy++;
        proteinSetFilterPanel.add(createProteinFDRFilterPanel(), c);

        c.gridy++;
        proteinSetFilterPanel.add(createScoringTypePanel(), c);

        return proteinSetFilterPanel;
    }

    private void initPsmPrefilterPanel() {

        m_psmPrefiltersSelectedPanel.removeAll();
        m_psmPrefilterJComboBox.removeAllItems();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridy = 0;

        int nbUsed = 0;
        boolean putAndInFront = false;
        int nbParameters = m_psmPrefilterParameters.length;
        for (int i = 0; i < nbParameters; i++) {
            final AbstractParameter p = m_psmPrefilterParameters[i];

            if ((p != null) && (p.isUsed())) {

                c.gridx = 0;
                if (putAndInFront) {
                    m_psmPrefiltersSelectedPanel.add(new JLabel("AND"), c);
                } else {
                    putAndInFront = true;
                    m_psmPrefiltersSelectedPanel.add(new JLabel("   "), c);
                }

                c.gridx++;
                JLabel prefilterNameLabel = new JLabel(p.getName());
                prefilterNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                m_psmPrefiltersSelectedPanel.add(prefilterNameLabel, c);

                if (p.hasComponent()) {
                    c.gridx++;
                    JLabel cmpLabel = new JLabel(((String) p.getAssociatedData()));
                    cmpLabel.setHorizontalAlignment(JLabel.CENTER);
                    m_psmPrefiltersSelectedPanel.add(cmpLabel, c);

                    c.weightx = 1;
                    c.gridx++;
                    m_psmPrefiltersSelectedPanel.add(p.getComponent(), c);
                } else {
                    c.gridx++;
                    c.gridx++;
                }

                c.weightx = 0;
                c.gridx++;
                JButton removeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
                removeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                removeButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        p.setUsed(false);
                        initPsmPrefilterPanel();
                    }
                });
                m_psmPrefiltersSelectedPanel.add(removeButton, c);

                nbUsed++;

                c.gridy++;
            } else {
                m_psmPrefilterJComboBox.addItem(p);
            }

        }

        boolean hasUnusedParameters = (nbUsed != nbParameters);
        m_psmPrefilterJComboBox.setVisible(hasUnusedParameters);
        m_addPsmPrefilterButton.setVisible(hasUnusedParameters);

        if (m_dialog != null) {
            m_dialog.provokeRepack();
        }
    }

    private void updateFdrObjects(boolean enabled) {

        m_fdrCheckbox.setSelected(enabled);

        m_fdrLabel.setEnabled(enabled);
        m_fdrTextField.setEnabled(enabled);
        m_fdrPercentageLabel.setEnabled(enabled);
        m_fdrOnValueComboBox.setEnabled(enabled);
        m_fdrFilterParameter.setUsed(enabled);
        m_fdrOnValueParameter.setUsed(enabled);
    }

    private JPanel createProteinFDRFilterPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Filter "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        boolean parameterUsed = m_proteinFdrFilterParameter.isUsed();
        m_proteinFdrCheckbox = new JCheckBox("");
        m_proteinFdrLabel = new JLabel("Protein FDR <=");
        m_proteinFdrCheckbox.setSelected(parameterUsed);
        m_proteinFdrPercentageLabel = new JLabel(" %");

        updateproteinFdrObjects(parameterUsed);

        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(m_proteinFdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(m_proteinFdrLabel, c);

        c.gridx++;
        fdrPanel.add(m_proteinFdrTextField, c);

        c.gridx++;
        fdrPanel.add(m_proteinFdrPercentageLabel, c);

        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        m_proteinFdrCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_proteinFdrCheckbox.isSelected());
                updateproteinFdrObjects(enabled);
            }
        });

        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (m_proteinFdrCheckbox.isSelected());
                if (!enabled) {

                    updateproteinFdrObjects(true);
                    if (e.getSource().equals(m_proteinFdrTextField)) {
                        m_proteinFdrTextField.requestFocusInWindow();
                    }
                }
            }
        };

        m_proteinFdrLabel.addMouseListener(actionOnClick);
        m_proteinFdrTextField.addMouseListener(actionOnClick);

        return fdrPanel;
    }

    private JPanel createScoringTypePanel() {
        JPanel scoringTypePanel = new JPanel(new GridBagLayout());
        //scoringTypePanel.setBorder(BorderFactory.createTitledBorder(" Scoring "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel proteinScoringTypeLabel = new JLabel("Scoring Type: ");
        m_proteinScoringTypeCbx = new JComboBox(SCORING_TYPE_OPTIONS);

        c.gridx = 0;
        c.gridy = 0;
        scoringTypePanel.add(proteinScoringTypeLabel, c);

        c.gridx++;
        scoringTypePanel.add(m_proteinScoringTypeCbx, c);

        c.gridx++;
        c.weightx = 1.0;
        scoringTypePanel.add(Box.createHorizontalBox(), c);

        return scoringTypePanel;
    }

    private void updateproteinFdrObjects(boolean enabled) {
        m_proteinFdrCheckbox.setSelected(enabled);
        m_proteinFdrLabel.setEnabled(enabled);
        m_proteinFdrTextField.setEnabled(enabled);
        m_proteinFdrPercentageLabel.setEnabled(enabled);
        m_proteinFdrFilterParameter.setUsed(enabled);
    }

    @Override
    public boolean cancelTriggered() {
        return true;
    }

    @Override
    public boolean okTriggered() {
        // check parameters
        if (!checkParameters(true)) {
            return false;
        }

        // save parameters
        Preferences preferences = NbPreferences.root();
        saveParameters(preferences);

        return true;
    }

    @Override
    public boolean defaultTriggered() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean backTriggered() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean saveTriggered() {
        // check parameters
        if (!checkParameters(false)) {
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
    public boolean loadTriggered() {
        SettingsDialog settingsDialog = new SettingsDialog(m_parent, SETTINGS_KEY);
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
                    for (int i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }

                    m_parameterList.loadParameters(filePreferences, true);
                    restoreScoringTypeParameter(filePreferences);
                    restoreTypicalProteinParameters(filePreferences);
                    updateFdrObjects(m_fdrFilterParameter.isUsed());
                    updateproteinFdrObjects(m_proteinFdrFilterParameter.isUsed());
                    initPsmPrefilterPanel();
                    m_proteinPrefiltersPanel.initProteinFilterPanel();

                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    m_dialog.setDialogStatus(true, "Parsing of your Settings File failed");
                }
            }
        }

        return false;
    }

    @Override
    public void setDialog(DefaultDialogInterface dialog) {
        this.m_dialog = dialog;
    }

    @Override
    public void reinitializePanel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

        private boolean checkParameters(boolean checkFDR) {

        if (checkFDR) {
            boolean aFdrSelected = m_fdrCheckbox.isSelected() || m_proteinFdrCheckbox.isSelected();

            if (aFdrSelected && (getHasDecoy() == ValidationDialog.DecoyStatus.WAITING)) {
                // we have not finished to read data for decoy check
                // we are waiting for one second
                m_dialog.provokeBusy(true);
                javax.swing.Timer t = new Timer(1000, new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        okTriggered();
                    }
                });
                t.setRepeats(false);
                t.start();
                return false;
            }

            m_dialog.provokeBusy(false);

            if (aFdrSelected && (getHasDecoy() == ValidationDialog.DecoyStatus.NO_DECOY)) {
                m_dialog.setDialogStatus(true, "A FDR can not be calculated with no Decoy Data");
                if (m_fdrCheckbox.isSelected()) {
                    m_dialog.highlightPanelComponent(m_fdrCheckbox);
                } else {
                    m_dialog.highlightPanelComponent(m_proteinFdrCheckbox);
                }
                return false;
            }
        }
            
        // check parameters
        ParameterError error = m_parameterList.checkParameters();
        if (error != null) {
            m_dialog.setDialogStatus(true, error.getErrorMessage());
            m_dialog.highlightPanelComponent(error.getParameterComponent());
            return false;
        }
        
        return true;
    }
    

    private void saveParameters(Preferences preferences) {
        // Save Parameters        
        m_parameterList.saveParameters(preferences);

        // save scoring type
        preferences.put("ValidationScoringType", m_proteinScoringTypeCbx.getSelectedItem().toString());

        // save specific Typical Protein parameters
        m_changeTypicalPanel.savePreference(preferences);
        preferences.putBoolean("UseTypicalProteinRegex", m_typicalProteinMatchCheckBox.isSelected());
    }

    private void restoreTypicalProteinParameters(Preferences preferences) {

        boolean useTypicalProteinRegex = preferences.getBoolean("UseTypicalProteinRegex", true);

        m_typicalProteinMatchCheckBox.setSelected(useTypicalProteinRegex);
        m_changeTypicalPanel.restoreInitialParameters();
        m_changeTypicalPanel.enableRules(useTypicalProteinRegex);
    }

    private void restoreScoringTypeParameter(Preferences preferences) {

        String scoringType = preferences.get("ValidationScoringType", SCORING_TYPE_OPTIONS[0]);
        m_proteinScoringTypeCbx.setSelectedItem(scoringType);

    }

    
    public synchronized ValidationDialog.DecoyStatus getHasDecoy() {
        return m_hasDecoy;
    }
    

}
