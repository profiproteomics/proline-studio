package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.CheckBoxTitledBorder;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class LabelFreeMSParamsSimplifiedPanel extends AbstractLabelFreeMSParamsPanel {
    
    private ObjectParameter<String> m_crossAssignStrategyParameter;

    JPanel m_crossAssignSettingsPanel;
    
    private JTextField m_extractionMoZTolTF;    
    private JTextField m_crossAssignFeatureMapRTTolTF;
    private CheckBoxTitledBorder m_crossAssignCBoxTitle;
    private JComboBox m_crossAssignStrategyCB;   
    private JLabel m_allowCrossAssLabel;
    private JLabel m_crossAssRTToleranceLabel;
    private JLabel m_alignmentFeatureTimeTolLabel;
    private JTextField m_alignmentFeatureMapTimeToleranceTF;
    protected JCheckBox m_alignRTCB;
        
    public LabelFreeMSParamsSimplifiedPanel() {
        super(false);

        createParameters();
        m_parameterList.updateValues(NbPreferences.root());
        setLayout(new GridBagLayout());
        
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        JPanel mainPanel = createMainPanel();
        m_scrollPane = new JScrollPane();
        m_scrollPane.setViewportView(mainPanel);
        m_scrollPane.createVerticalScrollBar();
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(m_scrollPane, c);

    }

    @Override
    public boolean isSimplifiedPanel() {
        return true;
    }
    
    private void createParameters() {
        
        m_extractionMoZTolTF = new JTextField();
        DoubleParameter extractionMoZTolParameter = new DoubleParameter("extractionMoZTol", "Extraction moz tolerance", m_extractionMoZTolTF, DEFAULT_EXTRACTION_MOZTOL_VALUE, new Double(0), null);
        m_parameterList.add(extractionMoZTolParameter);

        m_crossAssignCBoxTitle = new CheckBoxTitledBorder("Cross Assignment", DEFAULT_CROSS_ASSIGN_VALUE);
        m_crossAssignCBoxTitle.addChangeListener(e -> enableCrossAssignment(((JToggleButton)e.getSource()).isSelected()));
        BooleanParameter crossAssignParameter = new BooleanParameter("crossAssignment", "Cross Assignment", m_crossAssignCBoxTitle.getInternalCheckBox(), DEFAULT_CROSS_ASSIGN_VALUE);
        m_parameterList.add(crossAssignParameter);

        m_crossAssignStrategyCB = new JComboBox(CROSSASSIGN_STRATEGY_VALUES);
        m_crossAssignStrategyParameter = new ObjectParameter<>("crossAssignStrategy", "Cross Assignement Strategy", m_crossAssignStrategyCB, CROSSASSIGN_STRATEGY_VALUES, CROSSASSIGN_STRATEGY_KEYS,  0, null);
        m_parameterList.add(m_crossAssignStrategyParameter);
        
        m_crossAssignFeatureMapRTTolTF = new JTextField();
        DoubleParameter featureMappingTimeTolParameter = new DoubleParameter("featureTimeTol", "RT tolerance", m_crossAssignFeatureMapRTTolTF, DEFAULT_CA_FEATMAP_RTTOL_VALUE, new Double(0), null);
        m_parameterList.add(featureMappingTimeTolParameter);

        m_alignRTCB = new JCheckBox("Align RT", DEFAULT_ALIGN_VALUE);
        m_alignRTCB.addChangeListener(e -> enableAlignment(((JToggleButton)e.getSource()).isSelected()));
        BooleanParameter alignRTParameter = new BooleanParameter("alignRT", "Align RT", m_alignRTCB, DEFAULT_ALIGN_VALUE);
        m_parameterList.add(alignRTParameter);    
        
        m_alignmentFeatureMapTimeToleranceTF = new JTextField();
        DoubleParameter alignmentFeatureMappingTimeToleranceParameter = new DoubleParameter("featureMappingTimeTolerance", "Feature Mapping Time Tolerance", m_alignmentFeatureMapTimeToleranceTF, DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE, new Double(1), null);
        m_parameterList.add(alignmentFeatureMappingTimeToleranceParameter);
        
    }
    
    
    private void enableAlignment(boolean isEnabled){
        m_alignmentFeatureTimeTolLabel.setEnabled(isEnabled);
        m_alignmentFeatureMapTimeToleranceTF.setEnabled(isEnabled);
    }
    
    /**
     * set the quantitation params. This method assume parameter are formatted as V2 Params
     * Some params, not be visible here, may not be taken into account .... 
     * @param quantParams 
     */
    @Override
    public void setQuantParams(Map<String,Object>  quantParams){
        throw new UnsupportedOperationException("setQuantParams is not supported in SimplifiedPanel  ");
    }
    
    
    @Override
    public Map<String,Object> getQuantParams(){
        Map<String,Object> params = new HashMap<>();
        params.put("config_version", "2.0");
        
        Map<String,Object> extractionParams = new HashMap<>();
        extractionParams.put("moz_tol", m_extractionMoZTolTF.getText());
        extractionParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
        params.put("extraction_params", extractionParams);
                        
        //-- Set default values
        Map<String,Object> clusteringParams = new HashMap<>();
        clusteringParams.put("moz_tol", m_extractionMoZTolTF.getText());
        clusteringParams.put("moz_tol_unit",DEFAULT_MOZTOL_UNIT);
        clusteringParams.put("time_tol", DEFAULT_CLUSTER_TIMETOL_VALUE);
        clusteringParams.put("time_computation", DEFAULT_CLUSTER_TIMECOMPUT_VALUE);
        clusteringParams.put("intensity_computation", DEFAULT_CLUSTER_INTENSITYCOMPUT_VALUE);
        params.put("clustering_params", clusteringParams);   

        //should specify default params... (JSON String deserialization => set all to None/null/false...)
        params.put("detection_method_name", DETECTION_METHOD_KEYS[0]);
        Map<String,Object> detectionParams = new HashMap<>();
        Map<String,Object> detectionToleranceParams = new HashMap<>();
        detectionToleranceParams.put("moz_tol", m_extractionMoZTolTF.getText());
        detectionToleranceParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
        detectionParams.put("psm_matching_params",detectionToleranceParams);
        detectionParams.put("isotope_matching_params",detectionToleranceParams);

        params.put("detection_params", detectionParams);
                
        if( m_crossAssignCBoxTitle.isSelected()){ // Param defined only if CrossAssignement enabled
            Map<String,Object> crossAssignmentConfig = new HashMap<>();            
            crossAssignmentConfig.put("method_name",m_crossAssignStrategyParameter.getStringValue());
            crossAssignmentConfig.put("restrain_to_reliable_features", DEFAULT_CA_USE_RELIABLE_FEAT);
            Map<String,Object> ftMappingParams = new HashMap<>();
            ftMappingParams.put("moz_tol", m_extractionMoZTolTF.getText());
            ftMappingParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
            ftMappingParams.put("time_tol", m_crossAssignFeatureMapRTTolTF.getText());
            crossAssignmentConfig.put("ft_mapping_params", ftMappingParams);
            Map<String,Object> ftParams = new HashMap<>();     
            ftParams.put("name", DEFAULT_CA_FILTER_NAME_VALUE);
            ftParams.put("operator", DEFAULT_CA_FILTER_OPERATOR_VALUE);
            ftParams.put("value", DEFAULT_CA_FILTER_VALUE);
            crossAssignmentConfig.put("ft_filter", ftParams); 
            params.put("cross_assignment_config", crossAssignmentConfig);
            
            if(m_alignRTCB.isSelected()){
                Map<String,Object> alignmentConfig = new HashMap<>();
                alignmentConfig.put(AbstractLabelFreeMSParamsPanel.ALIGNMENT_METHOD_NAME, ALIGNMENT_METHOD_KEYS[0]);
                alignmentConfig.put("smoothing_method_name", ALIGNMENT_SMOOTHING_METHOD_KEYS[0]);   
                alignmentConfig.put("ft_mapping_method_name", FEATURE_MAPPING_METHOD_KEYS[0]);
                Map<String,Object> alnFtParams = new HashMap<>();
                alnFtParams.put("time_tol",  m_alignmentFeatureMapTimeToleranceTF.getText());
                alignmentConfig.put("ft_mapping_method_params", alnFtParams);                
                alignmentConfig.put("ignore_errors", false);
                params.put(AbstractLabelFreeMSParamsPanel.ALIGNMENT_CONFIG, alignmentConfig);
            }
            
        }

        return params;
    }
    

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        mainPanel.add(createMzTolerancePanel(), c);

        c.gridy++;
        mainPanel.add(createCrossAssignmentPanel(), c);
        

        c.gridy++;
        c.weighty = 1;
        mainPanel.add(Box.createVerticalGlue(), c);
        
        return mainPanel;
    }

    
    private JPanel createMzTolerancePanel() {
        JPanel headerPanel = new JPanel(new GridBagLayout());        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel extractionMoZTolLabel = new JLabel("Moz tolerance (ppm):");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        headerPanel.add(extractionMoZTolLabel, c);

        c.gridx++;
        c.gridwidth=1;
        c.weightx = 1;
        headerPanel.add(m_extractionMoZTolTF, c);
        
        return headerPanel;
    }

    private void enableCrossAssignment(boolean isEnabled){
        m_crossAssignSettingsPanel.setEnabled(isEnabled);        
        m_allowCrossAssLabel.setEnabled(isEnabled);
        m_crossAssignStrategyCB.setEnabled(isEnabled);
        m_crossAssRTToleranceLabel.setEnabled(isEnabled);
        m_crossAssignFeatureMapRTTolTF.setEnabled(isEnabled);
        m_alignRTCB.setEnabled(isEnabled);
        enableAlignment(m_alignRTCB.isSelected()&& isEnabled);
    }
    

    private JPanel createCrossAssignmentPanel(){
        JPanel panelA = new JPanel(new BorderLayout());
        panelA.setBorder(m_crossAssignCBoxTitle);
        
        m_crossAssignSettingsPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        m_allowCrossAssLabel = new JLabel("Allow cross assignment ");
        m_crossAssignSettingsPanel.add(m_allowCrossAssLabel, c);        
        c.gridx++;     
        c.weightx = 1;
        m_crossAssignSettingsPanel.add(m_crossAssignStrategyCB, c);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        m_crossAssRTToleranceLabel = new JLabel("RT tolerance (s):");
        m_crossAssignSettingsPanel.add(m_crossAssRTToleranceLabel, c);
        c.gridx++;   
        c.weightx = 1;
        m_crossAssignSettingsPanel.add(m_crossAssignFeatureMapRTTolTF, c);
        
        // Add Alignement & FT mapping specific params
        c.gridy++;
        c.gridx = 0;
        c.weightx = 2;
        c.gridwidth=2;
        m_crossAssignSettingsPanel.add(m_alignRTCB, c);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth=1;
        m_alignmentFeatureTimeTolLabel = new JLabel("Time tolerance (s):");
        m_alignmentFeatureTimeTolLabel.setEnabled(m_alignRTCB.isSelected()&&m_alignRTCB.isEnabled());
        m_crossAssignSettingsPanel.add(m_alignmentFeatureTimeTolLabel, c);
        c.gridx++;   
        c.weightx = 1;
        m_alignmentFeatureMapTimeToleranceTF.setEnabled(m_alignRTCB.isSelected()&&m_alignRTCB.isEnabled());
        m_crossAssignSettingsPanel.add(m_alignmentFeatureMapTimeToleranceTF,c);
        panelA.add(m_crossAssignSettingsPanel, BorderLayout.NORTH);
        return panelA ;                 
    }           
    
   
    
}
