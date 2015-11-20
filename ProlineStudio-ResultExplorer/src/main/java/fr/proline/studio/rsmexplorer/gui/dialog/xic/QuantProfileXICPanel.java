/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;

/**
 * panel with the different parameters for computing quantitation profile
 * all params are set to false by default
 * for the public release, we display only parameters linked to the peptide selection and the applyNormalization
 * to do this, we use a display mode (complete or release), that could be changed in the preferences properties
 * Issue #13875: remove Normalized Median Profile option
 * @author MB243701
 */
public class QuantProfileXICPanel extends JPanel {
    private ParameterList m_parameterList;
    
    // for now, for the public release, some parameters are hidden
    private boolean completeMode = false;
    
    private JScrollPane m_scrollPane;    
    private JTabbedPane m_tabbedPane;
    
    private JCheckBox m_useOnlySpecificPeptidesChB;
    private JCheckBox m_discardMissedCleavedPeptidesChB;
    private JCheckBox m_discardOxidizedPeptidesChB;
    
    private JCheckBox m_applyProfileClusteringChB;
    
    private JComboBox<String> m_abundanceSummarizerMethodCB;
    
    private final static String[] ABUNDANCE_SUMMARIZER_METHOD_VALUES = {"Mean", "Mean of top 3 peptides", "Median", "Median Profile", "Sum"};
    private final static String[] ABUNDANCE_SUMMARIZER_METHOD_KEYS = {"MEAN", "MEAN_OF_TOP3", "MEDIAN","MEDIAN_PROFILE", "SUM"};
     
    private JTextField m_peptideStatTestsAlpha;
    private JCheckBox m_applyPepNormalizationChB;
    private JCheckBox m_applyPepMissValInferenceChB;
    private JCheckBox m_applyPepVarianceCorrectionChB;
    private JCheckBox m_applyPepTTestChB;
    private JCheckBox m_applyPepZTestChB;
    
    private JTextField m_proteinStatTestsAlpha;
    private JCheckBox m_applyProtNormalizationChB;
    private JCheckBox m_applyProtMissValInferenceChB;
    private JCheckBox m_applyProtVarianceCorrectionChB;
    private JCheckBox m_applyProtTTestChB;
    private JCheckBox m_applyProtZTestChB;
    
    private DoubleParameter m_peptideStatTestsAlphaParameter;
    private DoubleParameter m_proteinStatTestsAlphaParameter;
    private BooleanParameter m_discardMissedCleavedPeptidesParameter;
    private BooleanParameter m_discardOxidizedPeptidesParameter;
    private BooleanParameter m_applyPepNormalizationParameter;
    private BooleanParameter m_applyProtNormalizationParameter;
    private BooleanParameter m_applyPepMissValInferenceParameter;
    private BooleanParameter m_applyProtMissValInferenceParameter;
    private BooleanParameter m_applyPepVarianceCorrectionParameter;
    private BooleanParameter m_applyProtVarianceCorrectionParameter;
    private BooleanParameter m_applyPepTTestParameter;
    private BooleanParameter m_applyProtTTestParameter;
    private BooleanParameter m_applyPepZTestParameter;
    private BooleanParameter m_applyProtZTestParameter;
    private BooleanParameter m_applyProfileClusteringParameter;
    private BooleanParameter m_useOnlySpecificPeptidesParameter;
    private ObjectParameter<String> m_abundanceSummarizerMethodParameter;
    
    private boolean m_readOnly;
    
    
    public QuantProfileXICPanel(boolean readOnly) {
        super();
        m_readOnly = readOnly;
        init();
    }
    
    private void init() {
        // completeMode can be changed in the preferences file with the Profilizer key
        Preferences preferences = NbPreferences.root();
        this.completeMode = preferences.getBoolean("Profi", false);
        m_parameterList = new ParameterList(QuantProfileXICDialog.SETTINGS_KEY);
        createParameters();
        m_parameterList.updateIsUsed(NbPreferences.root());
        for (AbstractParameter param : m_parameterList) {
            param.setUsed(true);
        }
        initPanel();
    }
    
    
    public final void createParameters() {
        m_peptideStatTestsAlpha = new JTextField();
        m_peptideStatTestsAlpha.setEnabled(!m_readOnly);
        m_peptideStatTestsAlphaParameter = new DoubleParameter("peptideStatTestsAlpha", "Peptide Stat Tests Alpha", m_peptideStatTestsAlpha, new Double(0.01), null, null);
        m_parameterList.add(m_peptideStatTestsAlphaParameter);
       
        m_proteinStatTestsAlpha = new JTextField();
        m_proteinStatTestsAlpha.setEnabled(!m_readOnly);
        m_proteinStatTestsAlphaParameter = new DoubleParameter("proteinStatTestsAlpha", "Protein Stat Tests Alpha", m_proteinStatTestsAlpha, new Double(0.01), null, null);
        m_parameterList.add(m_proteinStatTestsAlphaParameter);
        
        m_discardMissedCleavedPeptidesChB = new JCheckBox("Discard Missed Cleaved Peptides");
        m_discardMissedCleavedPeptidesChB.setEnabled(!m_readOnly);
        m_discardMissedCleavedPeptidesParameter = new BooleanParameter("discardMissedCleavedPeptides", "Discard Missed Cleaved Peptides", m_discardMissedCleavedPeptidesChB, false);
        m_parameterList.add(m_discardMissedCleavedPeptidesParameter);
        
        m_discardOxidizedPeptidesChB = new JCheckBox("Discard Oxidized Peptides");
        m_discardOxidizedPeptidesChB.setEnabled(!m_readOnly);
        m_discardOxidizedPeptidesParameter = new BooleanParameter("discardOxidizedPeptides", "Discard Oxidized Peptides", m_discardOxidizedPeptidesChB, false);
        m_parameterList.add(m_discardOxidizedPeptidesParameter);
        
        m_applyPepNormalizationChB = new JCheckBox("Apply Normalization (median)");
        m_applyPepNormalizationChB.setEnabled(!m_readOnly);
        m_applyPepNormalizationParameter = new BooleanParameter("applyPepNormalization", "Apply Normalization on peptides", m_applyPepNormalizationChB, false);
        m_parameterList.add(m_applyPepNormalizationParameter);

        m_applyProtNormalizationChB = new JCheckBox("Apply Normalization (median)");
        m_applyProtNormalizationChB.setEnabled(!m_readOnly);
        m_applyProtNormalizationParameter = new BooleanParameter("applyProtNormalization", "Apply Normalization on proteins", m_applyProtNormalizationChB, false);
        m_parameterList.add(m_applyProtNormalizationParameter);

        m_applyPepMissValInferenceChB = new JCheckBox("Apply Missing Value Inference");
        m_applyPepMissValInferenceChB.setEnabled(!m_readOnly);
        m_applyPepMissValInferenceParameter = new BooleanParameter("applyPepMissValInference", "Apply Miss Val Inference on peptides", m_applyPepMissValInferenceChB, false);
        m_parameterList.add(m_applyPepMissValInferenceParameter);

        m_applyProtMissValInferenceChB = new JCheckBox("Apply Missing Value Inference");
        m_applyProtMissValInferenceChB.setEnabled(!m_readOnly);
        m_applyProtMissValInferenceParameter = new BooleanParameter("applyProtMissValInference", "Apply Miss Val Inference on proteins", m_applyProtMissValInferenceChB, false);
        m_parameterList.add(m_applyProtMissValInferenceParameter);
        
        m_applyPepVarianceCorrectionChB = new JCheckBox("Apply Variance Correction");
        m_applyPepVarianceCorrectionChB.setEnabled(!m_readOnly);
        m_applyPepVarianceCorrectionParameter = new BooleanParameter("applyPepVarianceCorrection", "Apply Variance Correction on peptides", m_applyPepVarianceCorrectionChB, false);
        m_parameterList.add(m_applyPepVarianceCorrectionParameter);
        
        m_applyProtVarianceCorrectionChB = new JCheckBox("Apply Variance Correction");
        m_applyProtVarianceCorrectionChB.setEnabled(!m_readOnly);
        m_applyProtVarianceCorrectionParameter = new BooleanParameter("applyProtVarianceCorrection", "Apply Variance Correction on proteins", m_applyProtVarianceCorrectionChB, true);
        m_parameterList.add(m_applyProtVarianceCorrectionParameter);
        
        m_applyPepTTestChB = new JCheckBox("Apply T-Test (peptide)");
        m_applyPepTTestChB.setEnabled(!m_readOnly);
        m_applyPepTTestParameter = new BooleanParameter("applyPepTTest", "Apply TTest on peptides", m_applyPepTTestChB, false);
        m_parameterList.add(m_applyPepTTestParameter);
        
        m_applyProtTTestChB = new JCheckBox("Apply T-Test (protein)");
        m_applyProtTTestChB.setEnabled(!m_readOnly);
        m_applyProtTTestParameter = new BooleanParameter("applyProtTTest", "Apply TTest on proteins", m_applyProtTTestChB, false);
        m_parameterList.add(m_applyProtTTestParameter);

        m_applyPepZTestChB = new JCheckBox("Apply Z-Test (peptide)");
        m_applyPepZTestChB.setEnabled(!m_readOnly);
        m_applyPepZTestParameter = new BooleanParameter("applyPepZTest", "Apply ZTest on peptides", m_applyPepZTestChB, false);
        m_parameterList.add(m_applyPepZTestParameter);
        
        m_applyProtZTestChB = new JCheckBox("Apply Z-Test (protein)");
        m_applyProtZTestChB.setEnabled(!m_readOnly);
        m_applyProtZTestParameter = new BooleanParameter("applyProtZTest", "Apply ZTest on proteins", m_applyProtZTestChB, false);
        m_parameterList.add(m_applyProtZTestParameter);

        m_applyProfileClusteringChB = new JCheckBox("Apply Profile Clustering");
        m_applyProfileClusteringChB.setEnabled(!m_readOnly);
        m_applyProfileClusteringParameter = new BooleanParameter("applyProfileClustering", "Apply Profile Clustering", m_applyProfileClusteringChB, false);
        m_parameterList.add(m_applyProfileClusteringParameter);
        
        m_useOnlySpecificPeptidesChB = new JCheckBox("Use Only Specific Peptides");
        m_useOnlySpecificPeptidesChB.setEnabled(!m_readOnly);
        m_useOnlySpecificPeptidesParameter = new BooleanParameter("useOnlySpecificPeptides", "Use Only Specific Peptides", m_useOnlySpecificPeptidesChB, false);
        m_parameterList.add(m_useOnlySpecificPeptidesParameter);
        
        m_abundanceSummarizerMethodCB = new JComboBox(ABUNDANCE_SUMMARIZER_METHOD_VALUES);
        m_abundanceSummarizerMethodCB.setEnabled(!m_readOnly);
        m_abundanceSummarizerMethodParameter = new ObjectParameter<>("abundanceSummarizerMethod", "Abundance Summarizer Method", m_abundanceSummarizerMethodCB, ABUNDANCE_SUMMARIZER_METHOD_VALUES, ABUNDANCE_SUMMARIZER_METHOD_KEYS,  0, null);
        m_parameterList.add(m_abundanceSummarizerMethodParameter);
        
    }
    
    public ParameterList getParameterList() {
        return m_parameterList;
    }
    
    private void initPanel() {
        this.setLayout(new BorderLayout());
        
        m_tabbedPane = new JTabbedPane();
        if (this.completeMode){
            m_tabbedPane.addTab("Pep. selection", null, getPeptidesSelectionPanel(), "Specify peptides to consider for quantitation");
            m_tabbedPane.addTab("Pep. configuration", null, getQuantPeptidesConfigurationPanel(), "Parameters used for peptides quantitation");
            m_tabbedPane.addTab("Prot. configuration", null, getQuantProteinsConfigurationPanel(), "Parameters used for proteins quantitation");
        }else {
            m_tabbedPane.addTab("Pep. selection", null, getPeptidesPanel(),  "Specify peptides to consider for quantitation");
            m_tabbedPane.addTab("Pep. configuration", null, getPeptidesConfigurationPanel(),  "Parameters used for peptides quantitation");
            m_tabbedPane.addTab("Prot. configuration", null, getProteinsConfigurationPanel(), "Parameters used for proteins quantitation");
        }

        m_scrollPane = new JScrollPane();
        m_scrollPane.setViewportView(m_tabbedPane);
        m_scrollPane.createVerticalScrollBar();
        add(m_scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel getPeptidesPanel(){
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel pepPanel = new JPanel();
        pepPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);

        // useOnlySpecificPeptides
        c.gridx = 0;
        c.gridy=0;
        c.weightx = 1;
//        c.gridwidth = 2;
        pepPanel.add(m_useOnlySpecificPeptidesChB, c);
        
        // discardMissedCleavedPeptides
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1;
//        c.gridwidth = 1;
        pepPanel.add(m_discardMissedCleavedPeptidesChB, c);
        
        // discardOxidizedPeptides
        c.gridy++;
//        c.gridx++;
        c.weightx = 1;
//        c.gridwidth = 1;
        pepPanel.add(m_discardOxidizedPeptidesChB, c);
        
        
        northPanel.add(pepPanel, BorderLayout.NORTH);
        return northPanel;        
    }
    
    private JPanel getPeptidesSelectionPanel(){
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel pepSelectionPanel = new JPanel();
        pepSelectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);

        // useOnlySpecificPeptides
        c.gridx = 0;
        c.gridy=0;
        c.weightx = 1;
//        c.gridwidth = 2;
        pepSelectionPanel.add(m_useOnlySpecificPeptidesChB, c);
        
        // discardMissedCleavedPeptides
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1;
//        c.gridwidth = 1;
        pepSelectionPanel.add(m_discardMissedCleavedPeptidesChB, c);
        
        // discardOxidizedPeptides
        c.gridy++;
//        c.gridx++;
        c.weightx = 1;
//        c.gridwidth = 1;
        pepSelectionPanel.add(m_discardOxidizedPeptidesChB, c);
        
        northPanel.add(pepSelectionPanel, BorderLayout.NORTH);
        return northPanel;        
    }
    
     private JPanel getQuantPeptidesConfigurationPanel(){
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel pepQuantConfigPanel = new JPanel();
        pepQuantConfigPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);

     
        // applyNormalization
        c.gridx = 0;
        c.gridy=0;
        c.weightx = 1;
        c.gridwidth = 2;
        pepQuantConfigPanel.add(m_applyPepNormalizationChB, c);
                        
        // applyMissValInference
        c.gridy++;
        pepQuantConfigPanel.add(m_applyPepMissValInferenceChB, c);
        
        // peptideStatTestsAlpha
        c.gridy++;
        c.weightx = 0;
        c.gridwidth = 1;
        pepQuantConfigPanel.add(new JLabel("Peptide Stat Tests Alpha:"), c);
        
        c.gridx++;  
        c.weightx = 1;
        c.gridwidth = 1;
        pepQuantConfigPanel.add(m_peptideStatTestsAlpha, c);
        
        // applyTTest
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        c.gridx = 0;
        pepQuantConfigPanel.add(m_applyPepTTestChB, c);
        m_applyPepTTestChB.addActionListener(new ActionListener() {
         @Override
            public void actionPerformed(ActionEvent e) {
                m_applyPepVarianceCorrectionChB.setEnabled(m_applyPepTTestChB.isSelected());
                if(!m_applyPepTTestChB.isSelected())
                    m_applyPepVarianceCorrectionChB.setSelected(false);                    
                }
            });
        
        // applyVarianceCorrection
        c.gridy++;
        m_applyPepVarianceCorrectionChB.setEnabled(m_applyPepTTestChB.isSelected());
        if(!m_applyPepTTestChB.isSelected())
            m_applyPepVarianceCorrectionChB.setSelected(false);                    
        
        pepQuantConfigPanel.add(m_applyPepVarianceCorrectionChB, c);
        
        // applyZTest
        c.gridy++;
        pepQuantConfigPanel.add(m_applyPepZTestChB, c);
        northPanel.add(pepQuantConfigPanel, BorderLayout.NORTH);
        return northPanel;        
    }
     
     private JPanel getPeptidesConfigurationPanel(){
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel pepQuantConfigPanel = new JPanel();
        pepQuantConfigPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);

     
        // applyNormalization
        c.gridx = 0;
        c.gridy=0;
        c.weightx = 1;
        c.gridwidth = 2;
        pepQuantConfigPanel.add(m_applyPepNormalizationChB, c);
                        
        
        northPanel.add(pepQuantConfigPanel, BorderLayout.NORTH);
        return northPanel;        
    }
     
      private JPanel getQuantProteinsConfigurationPanel(){
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel protQuantConfigPanel = new JPanel();
        protQuantConfigPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy=0;
        c.weightx = 0;
        c.gridwidth = 1;
        JLabel abundanceSummarizerMethodLabel = new JLabel("Abundance Summarizer Method :");
        abundanceSummarizerMethodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        protQuantConfigPanel.add(abundanceSummarizerMethodLabel, c);
        
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        protQuantConfigPanel.add(m_abundanceSummarizerMethodCB, c);       
        
        // applyProfileClustering
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        protQuantConfigPanel.add(m_applyProfileClusteringChB, c);

        
        // applyNormalization
        c.gridy++;
        protQuantConfigPanel.add(m_applyProtNormalizationChB, c);
        
                
        // applyMissValInference
        c.gridy++;
        protQuantConfigPanel.add(m_applyProtMissValInferenceChB, c);
        
        
        // proteinStatTestsAlpha
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        protQuantConfigPanel.add(new JLabel("Protein Stat Tests Alpha:"), c);
        
        c.gridx++;  
        c.weightx = 1;
        protQuantConfigPanel.add(m_proteinStatTestsAlpha, c);
        
        
        // applyTTest
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        c.gridx = 0;
        protQuantConfigPanel.add(m_applyProtTTestChB, c);
        m_applyProtTTestChB.addActionListener(new ActionListener() {
         @Override
            public void actionPerformed(ActionEvent e) {
                m_applyProtVarianceCorrectionChB.setEnabled(m_applyProtTTestChB.isSelected());
            }
        });
                
        // applyVarianceCorrection
        c.gridy++;
        m_applyProtVarianceCorrectionChB.setEnabled(m_applyProtTTestChB.isSelected());
        protQuantConfigPanel.add(m_applyProtVarianceCorrectionChB, c);
        
        // applyZTest
        //c.gridy++;
        //c.gridx = 0;
        c.gridy++;
        protQuantConfigPanel.add(m_applyProtZTestChB, c);

        northPanel.add(protQuantConfigPanel, BorderLayout.NORTH);
        return northPanel;        
        
    }
      
      private JPanel getProteinsConfigurationPanel(){
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel protQuantConfigPanel = new JPanel();
        protQuantConfigPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy=0;
        c.weightx = 0;
        c.gridwidth = 1;
        JLabel abundanceSummarizerMethodLabel = new JLabel("Abundance Summarizer Method :");
        abundanceSummarizerMethodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        protQuantConfigPanel.add(abundanceSummarizerMethodLabel, c);
        
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        protQuantConfigPanel.add(m_abundanceSummarizerMethodCB, c);       
        
        
        // applyNormalization
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        protQuantConfigPanel.add(m_applyProtNormalizationChB, c);
        

        northPanel.add(protQuantConfigPanel, BorderLayout.NORTH);
        return northPanel;        
        
    }
      
    public Map<String,Object> getQuantParams(){
        Map<String,Object> params = new HashMap<>();
        
        params.put("use_only_specific_peptides", m_useOnlySpecificPeptidesChB.isSelected());
        params.put("discard_missed_cleaved_peptides", m_discardMissedCleavedPeptidesChB.isSelected());
        params.put("discard_oxidized_peptides", m_discardOxidizedPeptidesChB.isSelected());
        params.put("apply_profile_clustering", m_applyProfileClusteringChB.isSelected());
        params.put("abundance_summarizer_method", ABUNDANCE_SUMMARIZER_METHOD_KEYS[m_abundanceSummarizerMethodCB.getSelectedIndex()]);
          
        Map<String,Object> peptideStatConfigMap = new HashMap<>();
        peptideStatConfigMap.put("stat_tests_alpha", m_peptideStatTestsAlpha.getText());
        peptideStatConfigMap.put("apply_normalization", m_applyPepNormalizationChB.isSelected());
        peptideStatConfigMap.put("apply_miss_val_inference", m_applyPepMissValInferenceChB.isSelected());
        peptideStatConfigMap.put("apply_variance_correction", m_applyPepVarianceCorrectionChB.isSelected());
        peptideStatConfigMap.put("apply_ttest", m_applyPepTTestChB.isSelected());
        peptideStatConfigMap.put("apply_ztest", m_applyPepZTestChB.isSelected());
        params.put("peptide_stat_config",peptideStatConfigMap);

        Map<String,Object> proteinStatConfigMap = new HashMap<>();
        proteinStatConfigMap.put("stat_tests_alpha", m_proteinStatTestsAlpha.getText());
        proteinStatConfigMap.put("apply_normalization", m_applyProtNormalizationChB.isSelected());
        proteinStatConfigMap.put("apply_miss_val_inference", m_applyProtMissValInferenceChB.isSelected());
        proteinStatConfigMap.put("apply_variance_correction", m_applyProtVarianceCorrectionChB.isSelected());
        proteinStatConfigMap.put("apply_ttest", m_applyProtTTestChB.isSelected());
        proteinStatConfigMap.put("apply_ztest", m_applyProtZTestChB.isSelected());
        params.put("protein_stat_config",proteinStatConfigMap);
        
       return params;
    }
        
    
    public void restoreParameters(Preferences preferences) {

        Float peptideStatTestsAlpha = preferences.getFloat("peptideStatTestsAlpha", 0.01f );
        m_peptideStatTestsAlpha.setText(""+peptideStatTestsAlpha);
        
        Float proteinStatTestsAlpha = preferences.getFloat("proteinStatTestsAlpha", 0.01f );
        m_proteinStatTestsAlpha.setText(""+proteinStatTestsAlpha);
        
        Boolean discardMissedCleavedPeptides = preferences.getBoolean("discardMissedCleavedPeptides", true );
        m_discardMissedCleavedPeptidesChB.setSelected(discardMissedCleavedPeptides);
        
        Boolean discardOxidizedPeptides = preferences.getBoolean("discardOxidizedPeptides", true );
        m_discardOxidizedPeptidesChB.setSelected(discardOxidizedPeptides);
        
        Boolean applyPepNormalization = preferences.getBoolean("applyPepNormalization", true );
        m_applyPepNormalizationChB.setSelected(applyPepNormalization);
        
        Boolean applyPepMissValInference = preferences.getBoolean("applyPepMissValInference", true );
        m_applyPepMissValInferenceChB.setSelected(applyPepMissValInference);
        
        Boolean applyPepVarianceCorrection = preferences.getBoolean("applyPepVarianceCorrection", true );
        m_applyPepVarianceCorrectionChB.setSelected(applyPepVarianceCorrection);
        
        Boolean applyPepTTest = preferences.getBoolean("applyPepTTest", true );
        m_applyPepTTestChB.setSelected(applyPepTTest);
        
        Boolean applyPepZTest = preferences.getBoolean("applyPepZTest", true );
        m_applyPepZTestChB.setSelected(applyPepZTest);

        Boolean applyProtNormalization = preferences.getBoolean("applyProtNormalization", true );
        m_applyProtNormalizationChB.setSelected(applyProtNormalization);
        
        Boolean applyProtMissValInference = preferences.getBoolean("applyProtMissValInference", true );
        m_applyProtMissValInferenceChB.setSelected(applyProtMissValInference);
        
        Boolean applyProtVarianceCorrection = preferences.getBoolean("applyProtVarianceCorrection", true );
        m_applyProtVarianceCorrectionChB.setSelected(applyProtVarianceCorrection);
        
        Boolean applyProtTTest = preferences.getBoolean("applyProtTTest", true );
        m_applyProtTTestChB.setSelected(applyProtTTest);
        
        Boolean applyProtZTest = preferences.getBoolean("applyProtZTest", true );
        m_applyProtZTestChB.setSelected(applyProtZTest);
        
        Boolean applyProfileClustering = preferences.getBoolean("applyProfileClustering", true );
        m_applyProfileClusteringChB.setSelected(applyProfileClustering);
        
        Boolean useOnlySpecificPeptides = preferences.getBoolean("useOnlySpecificPeptides", true );
        m_useOnlySpecificPeptidesChB.setSelected(useOnlySpecificPeptides);
               
        String abundanceSummarizerMethod = preferences.get("abundanceSummarizerMethod", ABUNDANCE_SUMMARIZER_METHOD_VALUES[0] );
        m_abundanceSummarizerMethodCB.setSelectedItem(abundanceSummarizerMethod);
    }
    
    /**
     * set the refined parameters
     * @param refinedParams 
     */
    public void setRefinedParams(Map<String,Object>  refinedParams){
        m_useOnlySpecificPeptidesChB.setSelected(Boolean.valueOf(refinedParams.get("use_only_specific_peptides").toString()));
        m_discardMissedCleavedPeptidesChB.setSelected(Boolean.valueOf(refinedParams.get("discard_missed_cleaved_peptides").toString()));
        m_discardOxidizedPeptidesChB.setSelected(Boolean.valueOf(refinedParams.get("discard_oxidized_peptides").toString()));
        
        m_applyProfileClusteringChB.setSelected(Boolean.valueOf(refinedParams.get("apply_profile_clustering").toString()));
        for(int i=0; i<ABUNDANCE_SUMMARIZER_METHOD_KEYS.length; i++){
            if (ABUNDANCE_SUMMARIZER_METHOD_KEYS[i].equals(refinedParams.get("abundance_summarizer_method").toString())){
                m_abundanceSummarizerMethodCB.setSelectedIndex(i);
                break;
            }
        }
        
        Map<String,Object> peptideStatConfigMap = (Map<String,Object>)refinedParams.get("peptide_stat_config");
        m_peptideStatTestsAlpha.setText(peptideStatConfigMap.get("stat_tests_alpha").toString());
        m_applyPepNormalizationChB.setSelected(Boolean.valueOf(peptideStatConfigMap.get("apply_normalization").toString()));
        m_applyPepMissValInferenceChB.setSelected(Boolean.valueOf(peptideStatConfigMap.get("apply_miss_val_inference").toString()));
        m_applyPepVarianceCorrectionChB.setSelected(Boolean.valueOf(peptideStatConfigMap.get("apply_variance_correction").toString()));
        m_applyPepTTestChB.setSelected(Boolean.valueOf(peptideStatConfigMap.get("apply_ttest").toString()));
        m_applyPepZTestChB.setSelected(Boolean.valueOf(peptideStatConfigMap.get("apply_ztest").toString()));
        
        Map<String,Object> proteinStatConfigMap = (Map<String,Object>)refinedParams.get("protein_stat_config");
        m_proteinStatTestsAlpha.setText(proteinStatConfigMap.get("stat_tests_alpha").toString());
        m_applyProtNormalizationChB.setSelected(Boolean.valueOf(proteinStatConfigMap.get("apply_normalization").toString()));
        m_applyProtMissValInferenceChB.setSelected(Boolean.valueOf(proteinStatConfigMap.get("apply_miss_val_inference").toString()));
        m_applyProtVarianceCorrectionChB.setSelected(Boolean.valueOf(proteinStatConfigMap.get("apply_variance_correction").toString()));
        m_applyProtTTestChB.setSelected(Boolean.valueOf(peptideStatConfigMap.get("apply_ttest").toString()));
        m_applyProtZTestChB.setSelected(Boolean.valueOf(peptideStatConfigMap.get("apply_ztest").toString()));
    }
    

}
