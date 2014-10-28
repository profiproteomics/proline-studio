/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ParameterList;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;

/**
 * panel with the different parameters for computing quantitation profile
 * @author MB243701
 */
public class QuantProfileXICPanel extends JPanel {

    private ParameterList m_parameterList;
    
    private JTextField m_peptideStatTestsAlpha;
    private JTextField m_proteinStatTestsAlpha;
    private JCheckBox m_discardMissedCleavedPeptidesChB;
    private JCheckBox m_discardOxidizedPeptidesChB;
    private JCheckBox m_applyNormalizationChB;
    private JCheckBox m_applyMissValInferenceChB;
    private JCheckBox m_applyVarianceCorrectionChB;
    private JCheckBox m_applyTTestChB;
    private JCheckBox m_applyZTestChB;
    private JCheckBox m_applyProfileClusteringChB;
    private JCheckBox m_useOnlySpecificPeptidesChB;
    
    public QuantProfileXICPanel() {
        super();
        init();
    }
    
    private void init() {
        m_parameterList = new ParameterList("XicProfileParameters");
        createParameters();
        m_parameterList.updateIsUsed(NbPreferences.root());
        initPanel();
        restoreParameters(NbPreferences.root());
    }
    
    
    public final void createParameters() {
        m_peptideStatTestsAlpha = new JTextField();
        DoubleParameter m_peptideStatTestsAlphaParameter = new DoubleParameter("peptideStatTestsAlpha", "Peptide Stat Tests Alpha", m_peptideStatTestsAlpha, new Double(0.01), null, null);
        m_parameterList.add(m_peptideStatTestsAlphaParameter);
       
        m_proteinStatTestsAlpha = new JTextField();
        DoubleParameter m_proteinStatTestsAlphaParameter = new DoubleParameter("proteinStatTestsAlpha", "Protein Stat Tests Alpha", m_proteinStatTestsAlpha, new Double(0.01), null, null);
        m_parameterList.add(m_proteinStatTestsAlphaParameter);
        
        m_discardMissedCleavedPeptidesChB = new JCheckBox("Discard Missed Cleaved Peptides");
        BooleanParameter discardMissedCleavedPeptidesParameter = new BooleanParameter("discardMissedCleavedPeptides", "Discard Missed Cleaved Peptides", m_discardMissedCleavedPeptidesChB, true);
        m_parameterList.add(discardMissedCleavedPeptidesParameter);
        
        m_discardOxidizedPeptidesChB = new JCheckBox("Discard Oxidized Peptides");
        BooleanParameter discardOxidizedPeptidesParameter = new BooleanParameter("discardOxidizedPeptides", "Discard Oxidized Peptides", m_discardOxidizedPeptidesChB, true);
        m_parameterList.add(discardOxidizedPeptidesParameter);
        
        m_applyNormalizationChB = new JCheckBox("Apply Normalization");
        BooleanParameter applyNormalizationParameter = new BooleanParameter("applyNormalization", "Apply Normalization", m_applyNormalizationChB, true);
        m_parameterList.add(applyNormalizationParameter);
        
        m_applyMissValInferenceChB = new JCheckBox("Apply Miss Val Inference");
        BooleanParameter applyMissValInferenceParameter = new BooleanParameter("applyMissValInference", "Apply Miss Val Inference", m_applyMissValInferenceChB, true);
        m_parameterList.add(applyMissValInferenceParameter);
        
        m_applyVarianceCorrectionChB = new JCheckBox("Apply Variance Correction");
        BooleanParameter applyVarianceCorrectionParameter = new BooleanParameter("applyVarianceCorrection", "Apply Variance Correction", m_applyVarianceCorrectionChB, true);
        m_parameterList.add(applyVarianceCorrectionParameter);
        
        m_applyTTestChB = new JCheckBox("Apply T Test");
        BooleanParameter applyTTestParameter = new BooleanParameter("applyTTest", "Apply TTest", m_applyTTestChB, true);
        m_parameterList.add(applyTTestParameter);
        
        m_applyZTestChB = new JCheckBox("Apply Z Test");
        BooleanParameter applyZTestParameter = new BooleanParameter("applyZTest", "Apply ZTest", m_applyZTestChB, true);
        m_parameterList.add(applyZTestParameter);
        
        m_applyProfileClusteringChB = new JCheckBox("Apply Profile Clustering");
        BooleanParameter applyProfileClusteringParameter = new BooleanParameter("applyProfileClustering", "Apply Profile Clustering", m_applyProfileClusteringChB, true);
        m_parameterList.add(applyProfileClusteringParameter);
        
        m_useOnlySpecificPeptidesChB = new JCheckBox("Use Only Specific Peptides");
        BooleanParameter useOnlySpecificPeptidesParameter = new BooleanParameter("useOnlySpecificPeptides", "Use Only Specific Peptides", m_useOnlySpecificPeptidesChB, true);
        m_parameterList.add(useOnlySpecificPeptidesParameter);
    }
    
    private void initPanel() {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder("<html> <b> Parameters </b></html>"));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);

        // peptideStatTestsAlpha
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        this.add(new JLabel("Peptide Stat Tests Alpha:"), c);
        
        c.gridx++;  
        c.weightx = 1;
        this.add(m_peptideStatTestsAlpha, c);
        
        
        // proteinStatTestsAlpha
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        this.add(new JLabel("Protein Stat Tests Alpha:"), c);
        
        c.gridx++;  
        c.weightx = 1;
        this.add(m_proteinStatTestsAlpha, c);
        
        
        // discardMissedCleavedPeptides
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1;
        this.add(m_discardMissedCleavedPeptidesChB, c);
        
        // discardOxidizedPeptides
        //c.gridy++;
        //c.gridx = 0;
        c.gridx++;
        this.add(m_discardOxidizedPeptidesChB, c);
        
        // applyNormalization
        c.gridwidth = 2;
        c.gridy++;
        c.gridx = 0;
        this.add(m_applyNormalizationChB, c);
        
        // applyMissValInference
        c.gridy++;
        c.gridx = 0;
        this.add(m_applyMissValInferenceChB, c);
        
        // applyVarianceCorrection
        c.gridy++;
        c.gridx = 0;
        this.add(m_applyVarianceCorrectionChB, c);
        
        // applyTTest
        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        this.add(m_applyTTestChB, c);
        
        // applyZTest
        //c.gridy++;
        //c.gridx = 0;
        c.gridx++;
        this.add(m_applyZTestChB, c);
        
        // applyProfileClustering
        c.gridwidth = 2;
        c.gridy++;
        c.gridx = 0;
        this.add(m_applyProfileClusteringChB, c);
        
        // useOnlySpecificPeptides
        c.gridy++;
        c.gridx = 0;
        this.add(m_useOnlySpecificPeptidesChB, c);
    }
    
    public ParameterList getParameterList() {
        return m_parameterList;
    }
    
    public Map<String,Object> getQuantParams(){
        Map<String,Object> params = new HashMap<>();
        
        params.put("peptideStatTestsAlpha", m_peptideStatTestsAlpha.getText());
        params.put("proteinStatTestsAlpha", m_proteinStatTestsAlpha.getText());
        params.put("discardMissedCleavedPeptides", m_discardMissedCleavedPeptidesChB.isSelected());
        params.put("discardOxidizedPeptides", m_discardOxidizedPeptidesChB.isSelected());
        params.put("applyNormalization", m_applyNormalizationChB.isSelected());
        params.put("applyMissValInference", m_applyMissValInferenceChB.isSelected());
        params.put("applyVarianceCorrection", m_applyVarianceCorrectionChB.isSelected());
        params.put("applyTTest", m_applyTTestChB.isSelected());
        params.put("applyZTest", m_applyZTestChB.isSelected());
        params.put("applyProfileClustering", m_applyProfileClusteringChB.isSelected());
        params.put("useOnlySpecificPeptides", m_useOnlySpecificPeptidesChB.isSelected());
        
       return params;
    }
    
    private void restoreParameters(Preferences preferences) {

        Float peptideStatTestsAlpha = preferences.getFloat("peptideStatTestsAlpha", 0.01f );
        m_peptideStatTestsAlpha.setText(""+peptideStatTestsAlpha);
        
        Float proteinStatTestsAlpha = preferences.getFloat("proteinStatTestsAlpha", 0.01f );
        m_proteinStatTestsAlpha.setText(""+proteinStatTestsAlpha);
        
        Boolean discardMissedCleavedPeptides = preferences.getBoolean("discardMissedCleavedPeptides", true );
        m_discardMissedCleavedPeptidesChB.setSelected(discardMissedCleavedPeptides);
        
        Boolean discardOxidizedPeptides = preferences.getBoolean("discardOxidizedPeptides", true );
        m_discardOxidizedPeptidesChB.setSelected(discardOxidizedPeptides);
        
        Boolean applyNormalization = preferences.getBoolean("applyNormalization", true );
        m_applyNormalizationChB.setSelected(applyNormalization);
        
        Boolean applyMissValInference = preferences.getBoolean("applyMissValInference", true );
        m_applyMissValInferenceChB.setSelected(applyMissValInference);
        
        Boolean applyVarianceCorrection = preferences.getBoolean("applyVarianceCorrection", true );
        m_applyVarianceCorrectionChB.setSelected(applyVarianceCorrection);
        
        Boolean applyTTest = preferences.getBoolean("applyTTest", true );
        m_applyTTestChB.setSelected(applyTTest);
        
        Boolean applyZTest = preferences.getBoolean("applyZTest", true );
        m_applyZTestChB.setSelected(applyZTest);
        
        Boolean applyProfileClustering = preferences.getBoolean("applyProfileClustering", true );
        m_applyProfileClusteringChB.setSelected(applyProfileClustering);
        
        Boolean useOnlySpecificPeptides = preferences.getBoolean("useOnlySpecificPeptides", true );
        m_useOnlySpecificPeptidesChB.setSelected(useOnlySpecificPeptides);
        
        
    }
}
