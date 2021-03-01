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
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
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

    private final static String[] FDR_METHODS_VALUES = {"Target/Decoy", "BH"};
    private final static String[] FDR_METHODS_ASSOCIATED_KEYS = {"TARGET_DECOY", "BH"};

    private final static String[] TD_ANALYZER_VALUES = {"Basic", "Gorshkov"};
    private final static String[] TD_ANALYZER_VALUES_ASSOCIATED_KEYS = {"BASIC", "GORSHKOV"};
    private final static String[] FDR_ON_VALUES = {null, "Score", "e-Value", "Adjusted e-Value", "Identity p-Value", "Homology p-Value"};
    private final static String[] FDR_ON_VALUES_ASSOCIATED_KEYS = {null, "SCORE", "MASCOT_EVALUE", "MASCOT_ADJUSTED_EVALUE", "SCORE_IT_P-VALUE", "SCORE_HT_P-VALUE"};

    private final static String[] PROTEIN_SCORING_TYPE_OPTIONS = {"Standard","Mascot Mudpit", "Mascot Modified Mudpit", "Fisher"};
    private final static String[] PROTEIN_SCORING_TYPE_VALUES = {"mascot:standard score", "mascot:mudpit score", "mascot:modified mudpit score", "proline:fisher score"};

    private final static String SETTINGS_KEY = "Validation";

    private final ParameterList m_parameterList;

    private AbstractParameter[] m_psmPrefilterParameters;
    private ParametersComboPanel m_psmPrefiltersPanel;

    private AbstractParameter[] m_peptidePrefilterParameters;
    private ParametersComboPanel m_peptidePrefiltersPanel;

    private AbstractParameter m_fdrFilterParameter;
    private ObjectParameter<String> m_fdrPropertyParameter;
    private ObjectParameter<String> m_fdrMethodParameter;
    private AbstractParameter m_proteinFdrFilterParameter;
    private AbstractParameter m_peptideFdrFilterParameter;
    
    private ObjectParameter<String> m_tdAnalyzerName;
    private AbstractParameter m_tdAnalyzerParam;
            
    private AbstractParameter m_propagatePsmFiltersParameter;
    private JCheckBox m_propagatePsmFiltersCheckBox = null;
    
    private AbstractParameter[] m_proteinPrefilterParameters;
    private ParametersComboPanel m_proteinPrefiltersPanel;
    
    private AbstractParameter m_propagateProtSetFiltersParameter;
    private JCheckBox m_propagateProtSetFiltersCheckBox = null;
    
    // psm FDR components
    private JLabel m_fdrLabelEnsure = null;
    private JLabel m_fdrLabelFDRLessThan = null;
    private JTextField m_fdrTextField = null;
    private JLabel m_fdrPercentageLabel = null;
    private JComboBox<String> m_fdrPropertyComboBox = null;
    private JComboBox<String> m_fdrMethodComboBox = null;
    private JCheckBox m_fdrCheckbox = null;
    private JComboBox<String> m_tdAnalyzerComboBox = null;
    private JPanel m_tdParamsPanel;
    private JLabel m_tdAnalyzerParamLabel = null;
    private JTextField m_tdAnalyzerParamTF = null;
    
    // protein FDR components
    private JLabel m_proteinFdrLabel = null;
    private JTextField m_proteinFdrTextField = null;
    private JLabel m_proteinFdrPercentageLabel = null;
    private JComboBox<String> m_proteinFdrMethodComboBox = null;
    private JCheckBox m_proteinFdrCheckbox = null;
    
    // peptide FDR components
    private JLabel m_peptideFdrLabel = null;
    private JTextField m_peptideFdrTextField = null;
    private JLabel m_peptideFdrPercentageLabel = null;
    private JComboBox<String> m_peptideFdrMethodComboBox = null;
    private JCheckBox m_peptideFdrCheckbox = null;
    
    private JCheckBox m_typicalProteinMatchCheckBox;
    private ChangeTypicalProteinPanel m_changeTypicalPanel = null;

    private JComboBox<String> m_proteinScoringTypeCbx = null;

    public enum DecoyStatus {
        WAITING,
        HAS_DECOY,
        NO_DECOY
    }

    private DecoyStatus m_hasDecoy = DecoyStatus.WAITING;
    private String m_softwareName = "UNKNOWN";

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
        // force fdr method usage to true
        m_fdrMethodParameter.setUsed(true);
        setInternalComponent(createInternalPanel());
        
        updatePSMFDRObjects(m_fdrFilterParameter.isUsed());
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

    public synchronized void setSoftwareName(String softwareName) {
        m_softwareName = softwareName;
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

                ValidationDialog.DecoyStatus decoyStatus = DecoyStatus.HAS_DECOY;
                Set<String> softwareNames = new HashSet<>();

                for (DDataset dataset: datasetList) {
                    ResultSet rset = dataset.getResultSet();
                    if (rset.getDecoyResultSet() == null) {
                        decoyStatus = DecoyStatus.NO_DECOY;
                    }
                    if (rset.getMsiSearch() != null) {
                        softwareNames.add(rset.getMsiSearch().getSearchSetting().getSoftwareName().toUpperCase());
                    } else {
                        softwareNames.add("UNKNOWN");
                    }
                }

                setHasDecoy(decoyStatus);
                setSoftwareName((softwareNames.size() == 1) ? softwareNames.iterator().next() : "MIXED");

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

      c.gridy++;
      panel.add(createPeptideFDRPanel(), c);

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
        panel.add(createPSMFDRPanel(), c);

        return panel;
    }

    private JPanel createPSMFDRPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR PSM Filter "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 2, 5, 2);

        m_fdrCheckbox = new JCheckBox();
        m_fdrLabelEnsure = new JLabel("ensure");
        m_fdrLabelFDRLessThan = new JLabel("FDR <=");
        m_fdrPercentageLabel = new JLabel("%");
 
        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(m_fdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(m_fdrLabelEnsure, c);

        c.gridx++;
        fdrPanel.add(m_fdrMethodComboBox, c);
        
        c.gridx++;
        fdrPanel.add(m_fdrLabelFDRLessThan, c);

        c.gridx++;
        fdrPanel.add(m_fdrTextField, c);

        c.gridx++;
        fdrPanel.add(m_fdrPercentageLabel, c);
        
        c.gridx++;
        c.weightx = 0.1;
        fdrPanel.add(Box.createHorizontalBox(), c);
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.gridwidth = 6;
        fdrPanel.add(createTDPSMParamsPanel(), c);
        
        c.gridx++;
        c.weightx = 0.1;
        m_tdParamsPanel.add(Box.createHorizontalBox(), c);
        
        m_fdrCheckbox.addActionListener((ActionEvent e) -> {
          updatePSMFDRObjects(m_fdrCheckbox.isSelected());
        });

        // enable frd by clicking on any component 
        MouseListener actionOnClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!m_fdrCheckbox.isSelected()) {
                    updatePSMFDRObjects(true);
                    if (JComponent.class.isAssignableFrom(e.getSource().getClass())) {
                        ((JComponent)e.getSource()).requestFocusInWindow();
                    }
                }
            }
        };
        m_fdrLabelEnsure.addMouseListener(actionOnClick);
        m_fdrLabelFDRLessThan.addMouseListener(actionOnClick);
        m_fdrTextField.addMouseListener(actionOnClick);
        m_fdrMethodComboBox.addMouseListener(actionOnClick);
        return fdrPanel;
    }

    private JPanel createTDPSMParamsPanel() {
      
        m_tdParamsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 2, 5, 2);
        
        m_tdAnalyzerParamLabel = new JLabel("ratio");

        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.1;
        m_tdParamsPanel.add(Box.createHorizontalBox(), c);
        
        c.gridx++;
        c.weightx = 0;
        m_tdParamsPanel.add(new JLabel("Optimisation based on "), c);
        
        c.gridx++;                
        m_tdParamsPanel.add(m_fdrPropertyComboBox, c);

        c.gridx++;
        m_tdParamsPanel.add(m_tdAnalyzerComboBox, c);

        m_tdAnalyzerComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePSMFDRObjects(m_fdrCheckbox.isSelected());
            }
        });

        c.gridx++;
        m_tdParamsPanel.add(m_tdAnalyzerParamLabel, c);

        c.gridx++;
        m_tdParamsPanel.add(m_tdAnalyzerParamTF, c);
        
        c.gridx++;
        c.weightx = 0.1;
        m_tdParamsPanel.add(Box.createHorizontalBox(), c);

        return m_tdParamsPanel;
    }
    
    private void updatePSMFDRObjects(boolean enabled) {

        m_fdrCheckbox.setSelected(enabled);
        m_fdrLabelEnsure.setEnabled(enabled);
        m_fdrLabelFDRLessThan.setEnabled(enabled);
        m_fdrTextField.setEnabled(enabled);
        m_fdrPercentageLabel.setEnabled(enabled);
        setPanelEnabled(m_tdParamsPanel, enabled);
        m_fdrMethodComboBox.setEnabled(enabled);

        if (enabled) {
            Object analyzer = m_tdAnalyzerComboBox.getSelectedItem();
            boolean GorshkovParamsVisibility = (analyzer != null) && (((String)analyzer).equalsIgnoreCase("Gorshkov"));
            m_tdAnalyzerParamLabel.setVisible(GorshkovParamsVisibility);
            m_tdAnalyzerParamTF.setVisible(GorshkovParamsVisibility);

            Object fdrMethod = m_fdrMethodComboBox.getSelectedItem();
            boolean isTargetDecoyFdr = (fdrMethod != null) && (((String)fdrMethod).toLowerCase().contains("decoy"));
            setPanelEnabled(m_tdParamsPanel, isTargetDecoyFdr);
            m_tdAnalyzerName.setUsed(isTargetDecoyFdr);
            m_tdAnalyzerParam.setUsed(isTargetDecoyFdr);
            m_fdrPropertyParameter.setUsed(isTargetDecoyFdr);

        } else {
          m_tdAnalyzerName.setUsed(false);
          m_tdAnalyzerParam.setUsed(false);
          m_fdrPropertyParameter.setUsed(false);
        }
          m_fdrFilterParameter.setUsed(enabled);
        
    }

        private JPanel createPeptideFDRPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Peptide Filter "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        boolean parameterUsed = m_peptideFdrFilterParameter.isUsed();
        m_peptideFdrCheckbox = new JCheckBox("");
        m_peptideFdrLabel = new JLabel("Peptide FDR <=");
        m_peptideFdrCheckbox.setSelected(parameterUsed);
        m_peptideFdrPercentageLabel = new JLabel(" %");

        updatePeptideFDRObjects(parameterUsed);

        c.gridx = 0;
        c.gridy = 0;
        fdrPanel.add(m_peptideFdrCheckbox, c);

        c.gridx++;
        fdrPanel.add(m_peptideFdrMethodComboBox, c);
        
        m_peptideFdrMethodComboBox.addActionListener((ActionEvent e) -> {
          synchFDRMethods((JComboBox<String>)e.getSource());
        });
        
        c.gridx++;
        fdrPanel.add(m_peptideFdrLabel, c);

        c.gridx++;
        fdrPanel.add(m_peptideFdrTextField, c);

        c.gridx++;
        fdrPanel.add(m_peptideFdrPercentageLabel, c);

        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        m_peptideFdrCheckbox.addActionListener((ActionEvent e) -> {
          updatePeptideFDRObjects(m_peptideFdrCheckbox.isSelected());
        });

        MouseListener actionOnClick = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!m_peptideFdrCheckbox.isSelected()) {
                    updatePeptideFDRObjects(true);
                    if (JComponent.class.isAssignableFrom(e.getSource().getClass())) {
                        ((JComponent)e.getSource()).requestFocusInWindow();
                    }
                }
            }
        };

        m_peptideFdrLabel.addMouseListener(actionOnClick);
        m_peptideFdrTextField.addMouseListener(actionOnClick);
        m_peptideFdrMethodComboBox.addMouseListener(actionOnClick);
        
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
        if(!m_allowPropagateFilters)
            m_propagateProtSetFiltersCheckBox.setSelected(false);
        m_propagateProtSetFiltersCheckBox.setEnabled(m_allowPropagateFilters);
        proteinSetFilterPanel.add(m_propagateProtSetFiltersCheckBox, c);
        
        m_propagateProtSetFiltersCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_propagateProtSetFiltersParameter.setUsed(m_propagateProtSetFiltersCheckBox.isSelected());
            }
        });
        
        c.gridy++;
        c.weightx = 1.0;
        m_proteinPrefiltersPanel = new ParametersComboPanel(" Filter(s) ", m_proteinPrefilterParameters);
        m_proteinPrefiltersPanel.addComponentListener(this);
        proteinSetFilterPanel.add(m_proteinPrefiltersPanel, c);

        c.gridy++;
        proteinSetFilterPanel.add(createProteinFDRPanel(), c);

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
                m_changeTypicalPanel.enableRules(m_typicalProteinMatchCheckBox.isSelected());
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

    private JPanel createProteinFDRPanel() {
        JPanel fdrPanel = new JPanel(new GridBagLayout());
        fdrPanel.setBorder(BorderFactory.createTitledBorder(" FDR Protein Filter "));

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
        fdrPanel.add(m_proteinFdrMethodComboBox, c);
        
        m_proteinFdrMethodComboBox.addActionListener((ActionEvent e) -> {
          synchFDRMethods((JComboBox<String>)e.getSource());
        });
        
        c.gridx++;
        fdrPanel.add(m_proteinFdrLabel, c);

        c.gridx++;
        fdrPanel.add(m_proteinFdrTextField, c);

        c.gridx++;
        fdrPanel.add(m_proteinFdrPercentageLabel, c);

        c.gridx++;
        c.weightx = 1.0;
        fdrPanel.add(Box.createHorizontalBox(), c);

        m_proteinFdrCheckbox.addActionListener((ActionEvent e) -> {
          updateProteinFDRObjects(m_proteinFdrCheckbox.isSelected());
        });

        MouseListener actionOnClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!m_proteinFdrCheckbox.isSelected()) {
                    updateProteinFDRObjects(true);
                     if (JComponent.class.isAssignableFrom(e.getSource().getClass())) {
                        ((JComponent)e.getSource()).requestFocusInWindow();
                    }
                }
            }
        };

        m_proteinFdrLabel.addMouseListener(actionOnClick);
        m_proteinFdrTextField.addMouseListener(actionOnClick);
        m_proteinFdrMethodComboBox.addMouseListener(actionOnClick);
        
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
        m_proteinScoringTypeCbx = new JComboBox<>(PROTEIN_SCORING_TYPE_OPTIONS);

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

        String scoringType = preferences.get("ValidationScoringType", PROTEIN_SCORING_TYPE_OPTIONS[0]);
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
        m_proteinFdrMethodComboBox.setEnabled(enabled);
        m_proteinFdrFilterParameter.setUsed(enabled);
    }

    private void synchFDRMethods(JComboBox<String> source) {
      if (source != m_fdrMethodComboBox) {
        m_fdrMethodComboBox.setSelectedItem(source.getSelectedItem());
      }
      if (source != m_peptideFdrMethodComboBox) {
        m_peptideFdrMethodComboBox.setSelectedItem(source.getSelectedItem());
      }
      if (source != m_proteinFdrMethodComboBox) {
        m_proteinFdrMethodComboBox.setSelectedItem(source.getSelectedItem());
      }
    }
    
    private void updatePeptideFDRObjects(boolean enabled) {
        m_peptideFdrCheckbox.setSelected(enabled);
        m_peptideFdrLabel.setEnabled(enabled);
        m_peptideFdrTextField.setEnabled(enabled);
        m_peptideFdrPercentageLabel.setEnabled(enabled);
        m_peptideFdrMethodComboBox.setEnabled(enabled);
        
        m_peptideFdrFilterParameter.setUsed(enabled);
        
    }

    private void createParameters() {
        m_psmPrefilterParameters = new AbstractParameter[11]; //<< get sync
        m_psmPrefilterParameters[0] = null;
        m_psmPrefilterParameters[1] = new IntegerParameter("PSM_" + ValidationTask.PSMFilter.RANK.key, ValidationTask.PSMFilter.RANK.name, new JTextField(6), new Integer(1), new Integer(0), new Integer(10));
        m_psmPrefilterParameters[1].setAssociatedData("<=");
        m_psmPrefilterParameters[1].addBackwardCompatibleKey("Rank");
        m_psmPrefilterParameters[1].addBackwardCompatibleKey("PSM_RANK");
        m_psmPrefilterParameters[2] = new IntegerParameter("PSM_" + ValidationTask.PSMFilter.PEP_LENGTH.key, ValidationTask.PSMFilter.PEP_LENGTH.name, new JTextField(6), new Integer(4), new Integer(4), null);
        m_psmPrefilterParameters[2].setAssociatedData(">=");
        m_psmPrefilterParameters[3] = new DoubleParameter("PSM_" + ValidationTask.PSMFilter.SCORE.key, ValidationTask.PSMFilter.SCORE.name, new JTextField(6), new Double(0), new Double(0), null);
        m_psmPrefilterParameters[3].setAssociatedData(">=");
        m_psmPrefilterParameters[4] = new DoubleParameter("PSM_" + ValidationTask.PSMFilter.MASCOT_EVAL.key, ValidationTask.PSMFilter.MASCOT_EVAL.name, new JTextField(6), new Double(1), new Double(0), new Double(1));
        m_psmPrefilterParameters[4].setAssociatedData("<=");
        m_psmPrefilterParameters[5] = new DoubleParameter("PSM_" + ValidationTask.PSMFilter.MASCOT_ADJUSTED_EVALUE.key, ValidationTask.PSMFilter.MASCOT_ADJUSTED_EVALUE.name, new JTextField(6), new Double(1), new Double(0), new Double(1));
        m_psmPrefilterParameters[5].setAssociatedData("<=");
        m_psmPrefilterParameters[6] = new DoubleParameter("PSM_" + ValidationTask.PSMFilter.MASCOT_IT_SCORE.key, ValidationTask.PSMFilter.MASCOT_IT_SCORE.name, new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        m_psmPrefilterParameters[6].setAssociatedData("=");
        m_psmPrefilterParameters[7] = new DoubleParameter("PSM_" + ValidationTask.PSMFilter.MASCOT_HT_SCORE.key, ValidationTask.PSMFilter.MASCOT_HT_SCORE.name, new JTextField(6), new Double(0.05), new Double(0), new Double(1));
        m_psmPrefilterParameters[7].setAssociatedData("=");
        m_psmPrefilterParameters[8] = new NoneParameter("PSM_" + ValidationTask.PSMFilter.SINGLE_PSM_QUERY.key, ValidationTask.PSMFilter.SINGLE_PSM_QUERY.name);
        m_psmPrefilterParameters[8].setAssociatedData(":");
        m_psmPrefilterParameters[9] = new NoneParameter("PSM_" + ValidationTask.PSMFilter.SINGLE_PSM_RANK.key, ValidationTask.PSMFilter.SINGLE_PSM_RANK.name);
        m_psmPrefilterParameters[9].setAssociatedData(":");
        m_psmPrefilterParameters[10] = new IntegerParameter("PSM_" + ValidationTask.PSMFilter.ISOTOPE_OFFSET.key, ValidationTask.PSMFilter.ISOTOPE_OFFSET.name, new JTextField(6), new Integer(1), new Integer(0), null);
        m_psmPrefilterParameters[10].setAssociatedData("<=");
        
        for (AbstractParameter p : m_psmPrefilterParameters) {
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            p.setCompulsory(false);
            m_parameterList.add(p);
        }

        m_fdrTextField = new JTextField(5);
        m_fdrFilterParameter = new DoubleParameter(ValidationTask.ValidationParameters.EXPECTED_FDR.key,ValidationTask.ValidationParameters.EXPECTED_FDR.name, m_fdrTextField, new Double(1), new Double(0), new Double(10));
        m_fdrFilterParameter.setUsed(false);
        m_fdrFilterParameter.setCompulsory(false);
        m_parameterList.add(m_fdrFilterParameter);

        m_fdrPropertyComboBox = new JComboBox<>(FDR_ON_VALUES);
        m_fdrPropertyParameter = new ObjectParameter<>(ValidationTask.ValidationParameters.EXPECTED_FDR_PARAM.key,ValidationTask.ValidationParameters.EXPECTED_FDR_PARAM.name, m_fdrPropertyComboBox, FDR_ON_VALUES, FDR_ON_VALUES_ASSOCIATED_KEYS, 0, null);
        m_fdrPropertyParameter.setUsed(false);
        m_fdrPropertyParameter.setCompulsory(false);
        m_parameterList.add(m_fdrPropertyParameter);

        m_fdrMethodComboBox = new JComboBox<>(FDR_METHODS_VALUES);
        m_peptideFdrMethodComboBox = new JComboBox<>(FDR_METHODS_VALUES);
        m_proteinFdrMethodComboBox = new JComboBox<>(FDR_METHODS_VALUES);
        m_fdrMethodComboBox.addActionListener((ActionEvent e) -> {
          if (m_fdrCheckbox != null) {
            updatePSMFDRObjects(m_fdrCheckbox.isSelected());
          }
          synchFDRMethods((JComboBox<String>)e.getSource());
        });

        m_fdrMethodParameter = new ObjectParameter<>(ValidationTask.ValidationParameters.EXPECTED_FDR_METHOD.key,ValidationTask.ValidationParameters.EXPECTED_FDR_METHOD.name, m_fdrMethodComboBox, FDR_METHODS_VALUES, FDR_METHODS_ASSOCIATED_KEYS, 0, null);
        m_fdrMethodParameter.setUsed(true);
        m_fdrMethodParameter.setCompulsory(false);
        m_parameterList.add(m_fdrMethodParameter);
        
        m_tdAnalyzerComboBox = new JComboBox<>(TD_ANALYZER_VALUES);
        m_tdAnalyzerName = new ObjectParameter<>(ValidationTask.ValidationParameters.TD_ANALYZER.key, ValidationTask.ValidationParameters.TD_ANALYZER.name, m_tdAnalyzerComboBox, TD_ANALYZER_VALUES, TD_ANALYZER_VALUES_ASSOCIATED_KEYS, 0, null);
        m_tdAnalyzerName.setUsed(false);
        m_tdAnalyzerName.setCompulsory(false);
        m_parameterList.add(m_tdAnalyzerName);

        m_tdAnalyzerParamTF = new JTextField(5);
        m_tdAnalyzerParam = new DoubleParameter("db_ratio", "Ratio", m_tdAnalyzerParamTF, new Double(1), new Double(0), Double.MAX_VALUE);
        m_tdAnalyzerParam.setUsed(false);
        m_tdAnalyzerParam.setCompulsory(false);
        m_parameterList.add(m_tdAnalyzerParam);

        m_peptidePrefilterParameters = new AbstractParameter[1];
        m_peptidePrefilterParameters[0] = null;

        for (AbstractParameter p : m_peptidePrefilterParameters) {
            if (p == null) {
                continue;
            }
            p.setUsed(false);
            p.setCompulsory(false);
            m_parameterList.add(p);
        }
        
        
        m_peptideFdrTextField = new JTextField(5);
        m_peptideFdrFilterParameter = new DoubleParameter(ValidationTask.ValidationParameters.PEPTIDE_EXPECTED_FDR.key, ValidationTask.ValidationParameters.PEPTIDE_EXPECTED_FDR.name, m_peptideFdrTextField, new Double(1), new Double(0), new Double(10));
        m_peptideFdrFilterParameter.setUsed(false);
        m_peptideFdrFilterParameter.setCompulsory(false);
        m_parameterList.add(m_peptideFdrFilterParameter);
        
        m_proteinPrefilterParameters = FilterProteinSetPanel.createProteinSetFilterParameters("PROT_", m_parameterList);
        
        m_proteinFdrTextField = new JTextField(5);
        m_proteinFdrFilterParameter = new DoubleParameter(ValidationTask.ValidationParameters.PROTEIN_EXPECTED_FDR.key, ValidationTask.ValidationParameters.PROTEIN_EXPECTED_FDR.name, m_proteinFdrTextField, new Double(5), new Double(0), new Double(10));
        m_proteinFdrFilterParameter.setUsed(false);
        m_proteinFdrFilterParameter.setCompulsory(false);
        m_parameterList.add(m_proteinFdrFilterParameter);

        

        m_propagatePsmFiltersCheckBox = new JCheckBox();
        m_propagatePsmFiltersCheckBox.setText("Propagate PSM filtering to child Search Results");
        m_propagatePsmFiltersParameter = new BooleanParameter("propagate_pep_match_filters", "Propagate PSM PSMFilter", m_propagatePsmFiltersCheckBox, false);
        m_propagatePsmFiltersParameter.setUsed(false);
        m_propagatePsmFiltersParameter.setCompulsory(false);
        m_parameterList.add(m_propagatePsmFiltersParameter);

        m_propagateProtSetFiltersCheckBox = new JCheckBox();
        m_propagateProtSetFiltersCheckBox.setText("Propagate ProteinSets filtering to child Search Results (Warning FDR Validation will not be propagated !");
        m_propagateProtSetFiltersParameter = new BooleanParameter("propagate_prot_set_filters", "Propagate ProteinSet PSMFilter", m_propagateProtSetFiltersCheckBox, false);
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
        return PROTEIN_SCORING_TYPE_VALUES[m_proteinScoringTypeCbx.getSelectedIndex()];
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
            boolean aTDFdrSelected = (m_fdrCheckbox.isSelected() || m_proteinFdrCheckbox.isSelected() || m_peptideFdrCheckbox.isSelected()) &&
                ((String)m_fdrMethodComboBox.getSelectedItem()).toLowerCase().endsWith("decoy");

            if (aTDFdrSelected && (getHasDecoy() == DecoyStatus.WAITING)) {
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

            if (aTDFdrSelected && (getHasDecoy() == DecoyStatus.NO_DECOY)) {
                setStatus(true, "A Target/Decoy FDR can not be calculated without decoy data");
                if (m_fdrCheckbox.isSelected()) {
                    highlight(m_fdrCheckbox);
                } else if (m_proteinFdrCheckbox.isSelected()) {
                    highlight(m_proteinFdrCheckbox);
                } else {
                  highlight(m_peptideFdrCheckbox);
                }
                return false;
            }

            boolean bhProtFdrSelected = m_proteinFdrCheckbox.isSelected() && ((String)m_fdrMethodComboBox.getSelectedItem()).toLowerCase().startsWith("bh");
            
            if (bhProtFdrSelected) {
              if (!getScoringType().toLowerCase().startsWith("proline:fisher")) {
                    setStatus(true, "Protein BH FDR is only compatible with Fisher protein scoring");
                    highlight(m_proteinScoringTypeCbx);
                    return false;
                }
            }

            // check filters & scoring consistency
            if (!( m_softwareName.equalsIgnoreCase("MASCOT") || (m_softwareName.equals("UNKNOWN"))) ) {

              
                AbstractParameter parameter = m_parameterList.getParameter(ValidationTask.ValidationParameters.EXPECTED_FDR_METHOD.key);
                if (parameter.isUsed() && parameter.getStringValue().equals(FDR_METHODS_VALUES[1])) {
                    setStatus(true, "Peptide's BH FDR filtering is only allowed for Mascot datasets");
                    highlight(parameter.getComponent());
                    return false;
                }

                if (!getScoringType().equals(PROTEIN_SCORING_TYPE_VALUES[0])) {
                    setStatus(true, "Mascot and Fisher scoring can only be used for Mascot datasets");
                    highlight(m_proteinScoringTypeCbx);
                    return false;
                }

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
                    for (String key : keys) {
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }

                    m_parameterList.loadParameters(filePreferences);
                    restoreScoringTypeParameter(filePreferences);
                    restoreTypicalProteinParameters(filePreferences);
                    updatePSMFDRObjects(m_fdrFilterParameter.isUsed());
                    updateProteinFDRObjects(m_proteinFdrFilterParameter.isUsed());
                    updatePeptideFDRObjects(m_peptideFdrFilterParameter.isUsed());
                    //
                    //Init PSM/Prot filters Propagation if possible
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

    void setPanelEnabled(JPanel panel, Boolean isEnabled) {
    panel.setEnabled(isEnabled);

    Component[] components = panel.getComponents();
    for(int i = 0; i < components.length; i++) {
        if(JPanel.class.isAssignableFrom(components[i].getClass())) {
            setPanelEnabled((JPanel) components[i], isEnabled);
        }

        components[i].setEnabled(isEnabled);
    }
}
}
