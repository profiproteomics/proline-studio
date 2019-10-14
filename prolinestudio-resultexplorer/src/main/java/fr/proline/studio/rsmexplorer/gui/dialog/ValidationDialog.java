/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.dpm.task.jms.FilterRSMProtSetsTask;
import fr.proline.studio.dpm.task.jms.ValidationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.parameter.*;
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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;

/**
 * Dialog used to validate an identification and ask the creation of a Result
 * Summary from a Result Set
 *
 * @author JM235353
 */
public class ValidationDialog extends DefaultDialog implements ComponentListener {

    private static ValidationDialog m_singletonDialog = null;
    
    private boolean m_allowPropagateFilters = true;

//    private final static String[] FDR_ESTIMATOR_VALUES = {null, "Default", "Competition Based"};
//    private final static String[] FDR_ESTIMATOR_VALUES_ASSOCIATED_KEYS = {null, "false", "true"};
    private final static String[] FDR_ON_VALUES = {null, "Score", "e-Value", "Adjusted e-Value", "Identity p-Value", "Homology p-Value"};
    private final static String[] FDR_ON_VALUES_ASSOCIATED_KEYS = {null, "SCORE", "MASCOT_EVALUE", "MASCOT_ADJUSTED_EVALUE", "SCORE_IT_P-VALUE", "SCORE_HT_P-VALUE"};

    private final static String[] SCORING_TYPE_OPTIONS = {"Standard","Mascot Mudpit", "Mascot Modified Mudpit"};
    private final static String[] SCORING_TYPE_VALUES = {"mascot:standard score", "mascot:mudpit score", "mascot:modified mudpit score"};

    private final static String SETTINGS_KEY = "Validation";

    private ParameterList m_parameterList;

    private AbstractParameter[] m_psmPrefilterParameters;

    private AbstractParameter m_fdrFilterParameter;
    private ObjectParameter<String> m_fdrOnValueParameter;
    private AbstractParameter m_proteinFdrFilterParameter;

    private JPanel m_psmPrefiltersSelectedPanel = null;
    private JComboBox m_psmPrefilterJComboBox = null;
    private AbstractParameter m_propagatePsmFiltersParameter;
    private JCheckBox m_propagatePsmFiltersCheckBox = null;
    
    private AbstractParameter[] m_proteinPrefilterParameters;
    private FilterProteinSetPanel m_proteinPrefiltersPanel;
    
    private AbstractParameter m_propagateProtSetFiltersParameter;
    private JCheckBox m_propagateProtSetFiltersCheckBox = null;
    
    private JLabel m_fdrLabel = null;
    private JTextField m_fdrTextField = null;
    private JLabel m_fdrPercentageLabel = null;

    private JLabel m_proteinFdrLabel = null;
    private JTextField m_proteinFdrTextField = null;
    private JLabel m_proteinFdrPercentageLabel = null;
    private JComboBox m_fdrOnValueComboBox = null;
    private JCheckBox m_fdrCheckbox = null;
    private JCheckBox m_proteinFdrCheckbox = null;

    private JCheckBox m_typicalProteinMatchCheckBox;
    private ChangeTypicalProteinPanel m_changeTypicalPanel = null;

    private JComboBox m_proteinScoringTypeCbx = null;

    public enum DecoyStatus {

        WAITING,
        HAS_DECOY,
        NO_DECOY
    }

    private DecoyStatus m_hasDecoy = DecoyStatus.WAITING;

    public static ValidationDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ValidationDialog(parent);
        }

        return m_singletonDialog;
    }

    public ValidationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Identification Validation");

        setDocumentationSuffix("id.46r0co2");

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        m_parameterList = new ParameterList("Validation");
        createParameters();
        m_parameterList.updateValues(NbPreferences.root());
        
        setInternalComponent(createInternalPanel());

        initPsmPrefilterPanel();
        m_proteinPrefiltersPanel.initProteinFilterPanel();

        restoreScoringTypeParameter(NbPreferences.root());

        restoreTypicalProteinParameters(NbPreferences.root());

    }

    public synchronized void setHasDecoy(DecoyStatus hasDecoy) {
        m_hasDecoy = hasDecoy;
    }

    public synchronized DecoyStatus getHasDecoy() {
        return m_hasDecoy;
    }

    public void setAllowPropagateFilters(boolean allowPropagateFilters){
        if(m_allowPropagateFilters != allowPropagateFilters){
            if(!allowPropagateFilters){
                m_propagatePsmFiltersCheckBox.setSelected(false);
                m_propagateProtSetFiltersCheckBox.setSelected(false);
                m_propagateProtSetFiltersParameter.setUsed(false);
                m_propagatePsmFiltersParameter.setUsed(false);
            }
            
            m_propagateProtSetFiltersCheckBox.setEnabled(allowPropagateFilters);
            m_propagatePsmFiltersCheckBox.setEnabled(allowPropagateFilters);            
        }
        m_allowPropagateFilters=allowPropagateFilters;
        revalidate();
        repack();
    }
    
    public boolean isPropagateFiltersSelected(){
        return ((m_propagatePsmFiltersCheckBox.isEnabled() && m_propagatePsmFiltersCheckBox.isSelected()) 
                || (m_propagateProtSetFiltersCheckBox.isEnabled() && m_propagateProtSetFiltersCheckBox.isSelected()));
    }
            
    public void setDatasetList(final ArrayList<DDataset> datasetList) {

        setHasDecoy(DecoyStatus.WAITING);

        // we need to load ResultSet of dataset to be sure if we
        // can validate on Decoy Resultset or not
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                int nb = datasetList.size();
                for (int i = 0; i < nb; i++) {
                    DDataset dataset = datasetList.get(i);
                    ResultSet rset = dataset.getResultSet();

                    if (rset.getDecoyResultSet() == null) {
                        setHasDecoy(DecoyStatus.NO_DECOY);
                        return;
                    }
                }
                setHasDecoy(DecoyStatus.HAS_DECOY);
            }
        };

        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadRsetAndRsm(datasetList);
        task.setPriority(AbstractDatabaseTask.Priority.HIGH_3); // must be done as fast as possible to avoid to let the use wait
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Validation Parameters", null, createPSMTabPanel(), null);
        tabbedPane.addTab("Typical Protein Parameters", null, createTypicalProteinTabPanel(), null);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        internalPanel.add(tabbedPane, c);

        return internalPanel;
    }

    private JPanel createPSMTabPanel() {

        JPanel psmTabPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        psmTabPanel.add(createPSMPanel(), c);

        c.gridy++;
        psmTabPanel.add(createProteinSetFilterPanel(), c);

        return psmTabPanel;
    }
    
    private JPanel createTypicalProteinTabPanel() {

        JPanel tabPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        tabPanel.add(createTypicalProteinPanel(), c);

        c.gridy++;
        c.weighty = 1.0;
        tabPanel.add(Box.createVerticalGlue(), c);

        return tabPanel;
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
        if(!m_allowPropagateFilters)
           m_propagatePsmFiltersCheckBox.setSelected(false);
        m_propagatePsmFiltersCheckBox.setEnabled(m_allowPropagateFilters);
        psmPanel.add(m_propagatePsmFiltersCheckBox, c);
        m_propagatePsmFiltersCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_propagatePsmFiltersCheckBox.isSelected());
                m_propagatePsmFiltersParameter.setUsed(enabled);
            }
        });
        
        c.gridy++;   
        c.weightx = 1.0;
        psmPanel.add(createPsmPreFilterPanel(), c);

        c.gridy++;
        psmPanel.add(createFDRFilterPanel(), c);

//        c.gridy++;
//        psmPanel.add(createFDREstimatorPanel(), c);
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

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        prefilterPanel.add(m_psmPrefilterJComboBox, c);

        c.gridx++;
        c.weightx = 1.0;
        prefilterPanel.add(Box.createHorizontalBox(), c);
        
        m_psmPrefilterJComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
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

        repack();
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

    private void updateFdrObjects(boolean enabled) {

        m_fdrCheckbox.setSelected(enabled);

        m_fdrLabel.setEnabled(enabled);
        m_fdrTextField.setEnabled(enabled);
        m_fdrPercentageLabel.setEnabled(enabled);
        m_fdrOnValueComboBox.setEnabled(enabled);
        m_fdrFilterParameter.setUsed(enabled);
        m_fdrOnValueParameter.setUsed(enabled);
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
        if(!m_allowPropagateFilters)
            m_propagateProtSetFiltersCheckBox.setSelected(false);
        m_propagateProtSetFiltersCheckBox.setEnabled(m_allowPropagateFilters);
        proteinSetFilterPanel.add(m_propagateProtSetFiltersCheckBox, c);
        
        m_propagateProtSetFiltersCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_propagateProtSetFiltersCheckBox.isSelected());
                m_propagateProtSetFiltersParameter.setUsed(enabled);
            }
        });
        
        c.gridy++;
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

    private JPanel createTypicalProteinPanel() {
        JPanel typicalProteinPanel = new JPanel(new GridBagLayout());
        typicalProteinPanel.setBorder(BorderFactory.createTitledBorder("Set Typical Protein Match "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        m_typicalProteinMatchCheckBox = new JCheckBox("Using rules (in priority order):");
        m_typicalProteinMatchCheckBox.setHorizontalAlignment(SwingConstants.RIGHT);
        typicalProteinPanel.add(m_typicalProteinMatchCheckBox, c);

        m_typicalProteinMatchCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = m_typicalProteinMatchCheckBox.isSelected();
                m_changeTypicalPanel.enableRules(enabled);
            }
        });

        c.gridx = 0;
        c.gridy++;
        m_changeTypicalPanel = new ChangeTypicalProteinPanel();
        typicalProteinPanel.add(m_changeTypicalPanel, c);

        c.gridy++;
        c.weighty = 1;
        typicalProteinPanel.add(Box.createVerticalGlue(), c);

        return typicalProteinPanel;
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

    private void restoreScoringTypeParameter(Preferences preferences) {

        String scoringType = preferences.get("ValidationScoringType", SCORING_TYPE_OPTIONS[0]);
        m_proteinScoringTypeCbx.setSelectedItem(scoringType);

    }

    private void restoreTypicalProteinParameters(Preferences preferences) {

        boolean useTypicalProteinRegex = preferences.getBoolean("UseTypicalProteinRegex", true);

        m_typicalProteinMatchCheckBox.setSelected(useTypicalProteinRegex);
        m_changeTypicalPanel.restoreInitialParameters();
        m_changeTypicalPanel.enableRules(useTypicalProteinRegex);
    }

    private void updateproteinFdrObjects(boolean enabled) {
        m_proteinFdrCheckbox.setSelected(enabled);

        m_proteinFdrLabel.setEnabled(enabled);
        m_proteinFdrTextField.setEnabled(enabled);
        m_proteinFdrPercentageLabel.setEnabled(enabled);
        m_proteinFdrFilterParameter.setUsed(enabled);
    }

    private void createParameters() {
        m_psmPrefilterParameters = new AbstractParameter[11]; //<< get sync
        m_psmPrefilterParameters[0] = null;
        m_psmPrefilterParameters[1] = new IntegerParameter("PSM_" + ValidationTask.RANK_FILTER_KEY, ValidationTask.RANK_FILTER_NAME, new JTextField(6), new Integer(5), new Integer(0), new Integer(10));
        m_psmPrefilterParameters[1].setAssociatedData("<=");
        m_psmPrefilterParameters[1].addBackwardCompatibleKey("Rank");
        m_psmPrefilterParameters[1].addBackwardCompatibleKey("PSM_RANK");
        m_psmPrefilterParameters[2] = new IntegerParameter("PSM_" + ValidationTask.PEP_LENGTH_FILTER_KEY, ValidationTask.PEP_LENGTH_FILTER_NAME, new JTextField(6), new Integer(4), new Integer(4), null);
        m_psmPrefilterParameters[2].setAssociatedData(">=");
        m_psmPrefilterParameters[3] = new DoubleParameter("PSM_" + ValidationTask.SCORE_FILTER_KEY, ValidationTask.SCORE_FILTER_NAME, new JTextField(6), new Double(0), new Double(0), null);
        m_psmPrefilterParameters[3].setAssociatedData(">=");
        m_psmPrefilterParameters[4] = new DoubleParameter("PSM_" + ValidationTask.MASCOT_EVAL_FILTER_KEY, ValidationTask.MASCOT_EVAL_FILTER_NAME, new JTextField(6), new Double(1), new Double(0), new Double(1));
        m_psmPrefilterParameters[4].setAssociatedData("<=");
        m_psmPrefilterParameters[5] = new DoubleParameter("PSM_" + ValidationTask.MASCOT_ADJUSTED_EVAL_FILTER_KEY, ValidationTask.MASCOT_ADJUSTED_EVAL_FILTER_NAME, new JTextField(6), new Double(1), new Double(0), new Double(1));
        m_psmPrefilterParameters[5].setAssociatedData("<=");
        m_psmPrefilterParameters[6] = new DoubleParameter("PSM_" + ValidationTask.MASCOT_IT_SCORE_FILTER_KEY, ValidationTask.MASCOT_IT_SCORE_FILTER_NAME, new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        m_psmPrefilterParameters[6].setAssociatedData("=");
        m_psmPrefilterParameters[7] = new DoubleParameter("PSM_" + ValidationTask.MASCOT_HT_SCORE_FILTER_KEY, ValidationTask.MASCOT_HT_SCORE_FILTER_NAME, new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        m_psmPrefilterParameters[7].setAssociatedData("=");
        JCheckBox singlePerQueryCB = new JCheckBox("post FDR");
        m_psmPrefilterParameters[8] = new BooleanParameter("PSM_" + ValidationTask.SINGLE_PSM_QUERY_FILTER_KEY, ValidationTask.SINGLE_PSM_QUERY_FILTER_NAME, singlePerQueryCB, false);
        m_psmPrefilterParameters[8].setAssociatedData(":");
        JCheckBox singlePerRankCB = new JCheckBox("post FDR");
        m_psmPrefilterParameters[9] = new BooleanParameter("PSM_" + ValidationTask.SINGLE_PSM_RANK_FILTER_KEY, ValidationTask.SINGLE_PSM_RANK_FILTER_NAME, singlePerRankCB, false);
        m_psmPrefilterParameters[9].setAssociatedData(":");
        m_psmPrefilterParameters[10] = new IntegerParameter("PSM_" + ValidationTask.ISOTOPE_OFFSET_FILTER_KEY, ValidationTask.ISOTOPE_OFFSET_FILTER_NAME, new JTextField(6), new Integer(1), new Integer(0), null);
        m_psmPrefilterParameters[10].setAssociatedData("<=");
        
        for (AbstractParameter p : m_psmPrefilterParameters) {
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            p.setCompulsory(false);
            m_parameterList.add(p);
        }

        m_proteinPrefilterParameters = new AbstractParameter[FilterRSMProtSetsTask.FILTER_KEYS.length + 1];
        m_proteinPrefilterParameters[0] = null;
        for (int index = 1; index <= FilterRSMProtSetsTask.FILTER_KEYS.length; index++) {
            String filterKey = "PROT_" + FilterRSMProtSetsTask.FILTER_KEYS[index - 1];
            switch (filterKey) {
                case "SCORE":
                    m_proteinPrefilterParameters[index] = new DoubleParameter(filterKey, FilterRSMProtSetsTask.FILTER_NAME[index - 1], new JTextField(6), new Double(10), new Double(1), null);
                    m_proteinPrefilterParameters[index].setAssociatedData(">=");
                    break;
                default:
                    m_proteinPrefilterParameters[index] = new IntegerParameter(filterKey, FilterRSMProtSetsTask.FILTER_NAME[index - 1], new JTextField(6), new Integer(1), new Integer(1), null);
                    m_proteinPrefilterParameters[index].setAssociatedData(">=");
                    break;
            }

            AbstractParameter p = m_proteinPrefilterParameters[index];
            if (p != null) {
                p.setUsed(false);
                p.setCompulsory(false);
                m_parameterList.add(p);
            }
        }

        m_fdrTextField = new JTextField(5);
        m_fdrFilterParameter = new DoubleParameter("expected_fdr", "FDR", m_fdrTextField, new Double(5), new Double(0), new Double(10));
        m_fdrFilterParameter.setUsed(false);
        m_fdrFilterParameter.setCompulsory(false);
        m_parameterList.add(m_fdrFilterParameter);

        m_fdrOnValueComboBox = new JComboBox(FDR_ON_VALUES);
        m_fdrOnValueParameter = new ObjectParameter<>("expected_fdr_parameter", "FDR Variable", m_fdrOnValueComboBox, FDR_ON_VALUES, FDR_ON_VALUES_ASSOCIATED_KEYS, 0, null);
        m_fdrOnValueParameter.setUsed(false);
        m_fdrOnValueParameter.setCompulsory(false);
        m_parameterList.add(m_fdrOnValueParameter);

//        m_fdrEstimatorComboBox = new JComboBox(FDR_ESTIMATOR_VALUES);
//        ObjectParameter<String> fdrEstimatorParameter = new ObjectParameter<>("use_td_competition", "FDR Estimator", m_fdrEstimatorComboBox, FDR_ESTIMATOR_VALUES, FDR_ESTIMATOR_VALUES_ASSOCIATED_KEYS, 0, null);
//        m_parameterList.add(fdrEstimatorParameter);
        m_proteinFdrTextField = new JTextField(5);
        m_proteinFdrFilterParameter = new DoubleParameter("protein_expected_fdr", "Protein FDR", m_proteinFdrTextField, new Double(5), new Double(0), new Double(10));
        m_proteinFdrFilterParameter.setUsed(false);
        m_proteinFdrFilterParameter.setCompulsory(false);
        m_parameterList.add(m_proteinFdrFilterParameter);

        m_propagatePsmFiltersCheckBox = new JCheckBox();
        m_propagatePsmFiltersCheckBox.setText("Propagate PSM filtering to child Search Results");
        m_propagatePsmFiltersParameter = new BooleanParameter("propagate_pep_match_filters", "Propagate PSM Filters", m_propagatePsmFiltersCheckBox, false);
        m_propagatePsmFiltersParameter.setUsed(false);
        m_propagatePsmFiltersParameter.setCompulsory(false);
        m_parameterList.add(m_propagatePsmFiltersParameter);

        m_propagateProtSetFiltersCheckBox = new JCheckBox();
        m_propagateProtSetFiltersCheckBox.setText("Propagate ProteinSets filtering to child Search Results (Warning FDR Validation will not be propagated !");
        m_propagateProtSetFiltersParameter = new BooleanParameter("propagate_prot_set_filters", "Propagate ProteinSet Filters", m_propagateProtSetFiltersCheckBox, false);
        m_propagateProtSetFiltersParameter.setUsed(false);
        m_propagateProtSetFiltersParameter.setCompulsory(false);        
        m_parameterList.add(m_propagateProtSetFiltersParameter);    
    }

    public HashMap<String, String> getArguments() {
        return m_parameterList.getValues();
    }

    public List<ChangeTypicalRule> getChangeTypicalRules() {
		if(m_typicalProteinMatchCheckBox.isSelected())
			return m_changeTypicalPanel.getChangeTypicalRules();
		 else 
            return new ArrayList<>();
    }

    public String getScoringType() {
        return SCORING_TYPE_VALUES[m_proteinScoringTypeCbx.getSelectedIndex()];
    }

    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParameters(true)) {
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

        // save scoring type
        preferences.put("ValidationScoringType", m_proteinScoringTypeCbx.getSelectedItem().toString());

        // save specific Typical Protein parameters
        m_changeTypicalPanel.savePreference(preferences);
        preferences.putBoolean("UseTypicalProteinRegex", m_typicalProteinMatchCheckBox.isSelected());
    }

    private boolean checkParameters(boolean checkFDR) {

        if (getArguments().isEmpty() && !m_fdrCheckbox.isSelected() && !m_proteinFdrCheckbox.isSelected()) {

            InfoDialog emptyArgumentsDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "You should not validate. Important arguments are not set.\nAre you sure you want to validate?");
            emptyArgumentsDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
            emptyArgumentsDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
            emptyArgumentsDialog.centerToWindow(m_singletonDialog);
            emptyArgumentsDialog.setVisible(true);

            if (emptyArgumentsDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
                return false;
            }

        }

        if (checkFDR) {
            boolean aFdrSelected = m_fdrCheckbox.isSelected() || m_proteinFdrCheckbox.isSelected();

            if (aFdrSelected && (getHasDecoy() == DecoyStatus.WAITING)) {
                // we have not finished to read data for decoy check
                // we are waiting for one second
                setBusy(true);
                javax.swing.Timer t = new Timer(1000, new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        okCalled();
                    }
                });
                t.setRepeats(false);
                t.start();
                return false;
            }

            setBusy(false);

            if (aFdrSelected && (getHasDecoy() == DecoyStatus.NO_DECOY)) {
                setStatus(true, "A FDR can not be calculated with no Decoy Data");
                if (m_fdrCheckbox.isSelected()) {
                    highlight(m_fdrCheckbox);
                } else {
                    highlight(m_proteinFdrCheckbox);
                }
                return false;
            }
        }

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
                    for (int i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }

                    m_parameterList.loadParameters(filePreferences);
                    restoreScoringTypeParameter(filePreferences);
                    restoreTypicalProteinParameters(filePreferences);
                    updateFdrObjects(m_fdrFilterParameter.isUsed());
                    updateproteinFdrObjects(m_proteinFdrFilterParameter.isUsed());
                    //
                    //Initi PSM/Prot filters Propagation if possible
                    if(m_allowPropagateFilters){
                        m_propagatePsmFiltersCheckBox.setSelected(m_propagatePsmFiltersParameter.isUsed());
                        m_propagateProtSetFiltersCheckBox.setSelected(m_propagateProtSetFiltersParameter.isUsed());
                    }        
                    initPsmPrefilterPanel();
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
    protected boolean cancelCalled() {
        return true;
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
