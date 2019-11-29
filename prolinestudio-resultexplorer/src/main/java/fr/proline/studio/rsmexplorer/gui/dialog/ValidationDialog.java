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
import fr.proline.studio.dpm.task.jms.ValidationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Dialog used to validate an identification and ask the creation of a Result
 * Summary from a Result Set
 *
 * @author JM235353
 */
public class ValidationDialog extends DefaultDialog implements ComponentListener {

    private static ValidationDialog m_singletonDialog = null;
    
    private boolean m_allowPropagateFilters = true;

    private final static String[] TD_ANALYZER_VALUES = {"Basic", "Gorshkov"};
    private final static String[] TD_ANALYZER_VALUES_ASSOCIATED_KEYS = {"BASIC", "GORSHKOV"};
    private final static String[] FDR_ON_VALUES = {null, "Score", "e-Value", "Adjusted e-Value", "Identity p-Value", "Homology p-Value"};
    private final static String[] FDR_ON_VALUES_ASSOCIATED_KEYS = {null, "SCORE", "MASCOT_EVALUE", "MASCOT_ADJUSTED_EVALUE", "SCORE_IT_P-VALUE", "SCORE_HT_P-VALUE"};

    private final static String[] SCORING_TYPE_OPTIONS = {"Standard","Mascot Mudpit", "Mascot Modified Mudpit", "Fisher"};
    private final static String[] SCORING_TYPE_VALUES = {"mascot:standard score", "mascot:mudpit score", "mascot:modified mudpit score", "proline:fisher score"};

    private final static String SETTINGS_KEY = "Validation";

    private ParameterList m_parameterList;

    private AbstractParameter[] m_psmPrefilterParameters;
    private ParametersComboPanel m_psmPrefiltersPanel;

    private AbstractParameter[] m_peptidePrefilterParameters;
    private ParametersComboPanel m_peptidePrefiltersPanel;

    private AbstractParameter m_fdrFilterParameter;
    private ObjectParameter<String> m_fdrPropertyParameter;
    private AbstractParameter m_proteinFdrFilterParameter;
    private ObjectParameter<String> m_tdAnalyzerName;
    private AbstractParameter m_tdAnalyzerParam;
            
    private AbstractParameter m_propagatePsmFiltersParameter;
    private JCheckBox m_propagatePsmFiltersCheckBox = null;
    
    private AbstractParameter[] m_proteinPrefilterParameters;
    private ParametersComboPanel m_proteinPrefiltersPanel;
    
    private AbstractParameter m_propagateProtSetFiltersParameter;
    private JCheckBox m_propagateProtSetFiltersCheckBox = null;
    
    private JLabel m_fdrLabel = null;
    private JTextField m_fdrTextField = null;
    private JLabel m_fdrPercentageLabel = null;
    private JLabel m_tdAnalyzerParamLabel = null;
    private JTextField m_tdAnalyzerParamTF = null;
    
    private JLabel m_proteinFdrLabel = null;
    private JTextField m_proteinFdrTextField = null;
    private JLabel m_proteinFdrPercentageLabel = null;
    private JComboBox m_fdrPropertyComboBox = null;
    private JCheckBox m_fdrCheckbox = null;
    private JCheckBox m_proteinFdrCheckbox = null;
    private JComboBox m_tdAnalyzerComboBox = null;
    
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

        updateSelectedFilters();

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

    private void updateSelectedFilters() {
        m_psmPrefiltersPanel.updatePanel();
        m_proteinPrefiltersPanel.updatePanel();
        m_peptidePrefiltersPanel.updatePanel();
    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Validation Parameters", null, createValidationPanel(), null);
        tabbedPane.addTab("Typical Protein Parameters", null, createTypicalProteinPanel(), null);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        internalPanel.add(tabbedPane, c);

        return internalPanel;
    }

    private JPanel createValidationPanel() {

        JPanel psmTabPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        psmTabPanel.add(createPSMFilterPanel(), c);

        c.gridy++;
        psmTabPanel.add(createPeptideFilterPanel(), c);

        c.gridy++;
        psmTabPanel.add(createProteinSetFilterPanel(), c);

        return psmTabPanel;
    }
    
    private JPanel createPeptideFilterPanel() {
      JPanel panel = new JPanel(new GridBagLayout());
       panel.setBorder(BorderFactory.createTitledBorder(" Peptide"));
       
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.NORTHWEST;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new java.awt.Insets(5, 5, 5, 5);

      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1.0;

      m_peptidePrefiltersPanel =  new ParametersComboPanel(" Filter(s) ", m_peptidePrefilterParameters);
      panel.add(m_peptidePrefiltersPanel, c);

      return panel;
    }
    
    private JPanel createPSMFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(" PSM"));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;           
        if(!m_allowPropagateFilters)
           m_propagatePsmFiltersCheckBox.setSelected(false);
        m_propagatePsmFiltersCheckBox.setEnabled(m_allowPropagateFilters);
        panel.add(m_propagatePsmFiltersCheckBox, c);
        m_propagatePsmFiltersCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_propagatePsmFiltersCheckBox.isSelected());
                m_propagatePsmFiltersParameter.setUsed(enabled);
            }
        });
        
        c.gridy++;   
        c.weightx = 1.0;
        m_psmPrefiltersPanel =  new ParametersComboPanel(" Prefilter(s) ", m_psmPrefilterParameters);
        panel.add(m_psmPrefiltersPanel, c);

        m_psmPrefiltersPanel.addComponentListener(this);
        c.gridy++;
        panel.add(createPSMFDRFilterPanel(), c);

        return panel;
    }

    private JPanel createPSMFDRFilterPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Filter "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 2, 5, 2);

        boolean fdrUsed = m_fdrFilterParameter.isUsed();
        m_fdrCheckbox = new JCheckBox();
        m_fdrLabel = new JLabel("Ensure FDR <=");
        m_tdAnalyzerParamLabel = new JLabel("Ratio");
        m_fdrPercentageLabel = new JLabel("%  on");

        updateFDRObjects(fdrUsed);

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
        fdrPanel.add(m_fdrPropertyComboBox, c);

        c.gridx++;
        fdrPanel.add(m_tdAnalyzerComboBox, c);

        m_tdAnalyzerComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean visbility = ((String)m_tdAnalyzerComboBox.getSelectedItem()).equalsIgnoreCase("Gorshkov");
                m_tdAnalyzerParamLabel.setVisible(visbility);
                m_tdAnalyzerParamTF.setVisible(visbility);
            }
        });
        
        c.gridx++;
        fdrPanel.add(m_tdAnalyzerParamLabel, c);
        
        c.gridx++;
        fdrPanel.add(m_tdAnalyzerParamTF, c);
        
        c.gridx++;
        c.weightx = 0.1;
        fdrPanel.add(Box.createHorizontalBox(), c);

        m_fdrCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = (m_fdrCheckbox.isSelected());
                updateFDRObjects(enabled);
            }
        });

        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (m_fdrCheckbox.isSelected());
                if (!enabled) {
                    updateFDRObjects(true);

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

    private void updateFDRObjects(boolean enabled) {

        m_fdrCheckbox.setSelected(enabled);
        m_fdrLabel.setEnabled(enabled);
        m_fdrTextField.setEnabled(enabled);
        m_fdrPercentageLabel.setEnabled(enabled);
        m_fdrPropertyComboBox.setEnabled(enabled);
        m_tdAnalyzerComboBox.setEnabled(enabled);
        m_tdAnalyzerParamLabel.setEnabled(enabled);
        m_tdAnalyzerParamTF.setEnabled(enabled);
        if (enabled) {
            Object analyzer = m_tdAnalyzerComboBox.getSelectedItem();
            boolean visibility = (analyzer != null) && (((String)analyzer).equalsIgnoreCase("Gorshkov"));
            m_tdAnalyzerParamLabel.setVisible(visibility);
            m_tdAnalyzerParamTF.setVisible(visibility);
        }
        m_fdrFilterParameter.setUsed(enabled);
        m_tdAnalyzerName.setUsed(enabled);
        m_tdAnalyzerParam.setUsed(enabled);
        m_fdrPropertyParameter.setUsed(enabled);
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
        m_proteinPrefiltersPanel = new ParametersComboPanel(" Filter(s) ", m_proteinPrefilterParameters);
        m_proteinPrefiltersPanel.addComponentListener(this);
        proteinSetFilterPanel.add(m_proteinPrefiltersPanel, c);

        c.gridy++;
        proteinSetFilterPanel.add(createProteinFDRFilterPanel(), c);

        c.gridy++;
        proteinSetFilterPanel.add(createScoringTypePanel(), c);

        return proteinSetFilterPanel;
    }

    private JPanel createTypicalProteinPanel() {
      
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;
        c1.weightx = 1.0;
        
        
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
        
        panel.add(typicalProteinPanel, c1);

        c1.gridy++;
        c1.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), c1);
        
        return panel;
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

        updateProteinFDRObjects(parameterUsed);

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
                updateProteinFDRObjects(enabled);
            }
        });

        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                boolean enabled = (m_proteinFdrCheckbox.isSelected());
                if (!enabled) {

                    updateProteinFDRObjects(true);
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

    private void updateProteinFDRObjects(boolean enabled) {
        m_proteinFdrCheckbox.setSelected(enabled);

        m_proteinFdrLabel.setEnabled(enabled);
        m_proteinFdrTextField.setEnabled(enabled);
        m_proteinFdrPercentageLabel.setEnabled(enabled);
        m_proteinFdrFilterParameter.setUsed(enabled);
    }

    private void createParameters() {
        m_psmPrefilterParameters = new AbstractParameter[12]; //<< get sync
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
        m_psmPrefilterParameters[11] = new DoubleParameter("PSM_" + ValidationTask.BH_ADJUSTED_PVALUE_FILTER_KEY, ValidationTask.BH_ADJUSTED_PVALUE_FILTER_NAME, new JTextField(6), new Double(1.0), new Double(0), new Double(100));
        m_psmPrefilterParameters[11].setAssociatedData("<=");
        
        for (AbstractParameter p : m_psmPrefilterParameters) {
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            p.setCompulsory(false);
            m_parameterList.add(p);
        }

        m_peptidePrefilterParameters = new AbstractParameter[2];
        m_peptidePrefilterParameters[0] = null;
        m_peptidePrefilterParameters[1] = new DoubleParameter("PEPTIDE_" + ValidationTask.BH_ADJUSTED_PVALUE_FILTER_KEY, ValidationTask.BH_ADJUSTED_PVALUE_FILTER_NAME, new JTextField(6), new Double(1.0), new Double(0), new Double(100));
        m_peptidePrefilterParameters[1].setAssociatedData("<=");
        for (AbstractParameter p : m_peptidePrefilterParameters) {
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            p.setCompulsory(false);
            m_parameterList.add(p);
        }
        
        m_proteinPrefilterParameters = FilterProteinSetPanel.createProteinSetFilterParameters("PROT_", m_parameterList);

        m_fdrTextField = new JTextField(5);
        m_fdrFilterParameter = new DoubleParameter("expected_fdr", "FDR", m_fdrTextField, new Double(5), new Double(0), new Double(10));
        m_fdrFilterParameter.setUsed(false);
        m_fdrFilterParameter.setCompulsory(false);
        m_parameterList.add(m_fdrFilterParameter);

        m_fdrPropertyComboBox = new JComboBox(FDR_ON_VALUES);
        m_fdrPropertyParameter = new ObjectParameter<>("expected_fdr_parameter", "FDR Variable", m_fdrPropertyComboBox, FDR_ON_VALUES, FDR_ON_VALUES_ASSOCIATED_KEYS, 0, null);
        m_fdrPropertyParameter.setUsed(false);
        m_fdrPropertyParameter.setCompulsory(false);
        m_parameterList.add(m_fdrPropertyParameter);

        m_tdAnalyzerComboBox = new JComboBox(TD_ANALYZER_VALUES);
        m_tdAnalyzerName = new ObjectParameter<>("td_analyzer", "FDR Estimator", m_tdAnalyzerComboBox, TD_ANALYZER_VALUES, TD_ANALYZER_VALUES_ASSOCIATED_KEYS, 0, null);
        m_tdAnalyzerName.setUsed(false);
        m_tdAnalyzerName.setCompulsory(false);
        m_parameterList.add(m_tdAnalyzerName);
        
        m_tdAnalyzerParamTF = new JTextField(5);
        m_tdAnalyzerParam = new DoubleParameter("db_ratio", "Ratio", m_tdAnalyzerParamTF, new Double(1), new Double(0), Double.MAX_VALUE);
        m_tdAnalyzerParam.setUsed(false);
        m_tdAnalyzerParam.setCompulsory(false);
        m_parameterList.add(m_tdAnalyzerParam);
        
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
                    updateFDRObjects(m_fdrFilterParameter.isUsed());
                    updateProteinFDRObjects(m_proteinFdrFilterParameter.isUsed());
                    //
                    //Initi PSM/Prot filters Propagation if possible
                    if(m_allowPropagateFilters){
                        m_propagatePsmFiltersCheckBox.setSelected(m_propagatePsmFiltersParameter.isUsed());
                        m_propagateProtSetFiltersCheckBox.setSelected(m_propagateProtSetFiltersParameter.isUsed());
                    }

                    updateSelectedFilters();

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
