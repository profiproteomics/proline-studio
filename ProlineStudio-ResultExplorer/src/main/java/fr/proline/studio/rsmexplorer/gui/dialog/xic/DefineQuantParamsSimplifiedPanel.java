package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.CheckBoxTitledBorder;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class DefineQuantParamsSimplifiedPanel extends AbstractGenericQuantParamsPanel {
    
    private ObjectParameter<String> m_crossAssignStrategyParameter;

    JPanel m_crossAssignSettingsPanel;
    
    private JTextField m_extractionMoZTolTF;    
    private JTextField m_crossAssignFeatureMappRTTolTF; 
    private CheckBoxTitledBorder m_crossAssignCBoxTitle;
    private JComboBox m_crossAssignStrategyCB;   
    private JLabel m_allowCrossAssLabel;
    private JLabel m_crossAssRTToleranceLabel;
    private JLabel m_alignmentFeatureTimeTolLabel;
    private JTextField m_alignmentFeatureMappTimeToleranceTF;     
    protected JCheckBox m_alignRTCB;
        
    public DefineQuantParamsSimplifiedPanel(boolean readOnly, boolean readValues) {
        super(readOnly);

        createParameters();
        
        if (readValues) {
            m_parameterList.updateIsUsed(NbPreferences.root());
        } else {
            m_parameterList.setDefaultValues();
        }
        for (AbstractParameter param : m_parameterList) {
            param.setUsed(true);
        }

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
        m_extractionMoZTolTF.setEnabled(!m_readOnly);
        DoubleParameter extractionMoZTolParameter = new DoubleParameter("extractionMoZTol", "Extraction moz tolerance", m_extractionMoZTolTF, DEFAULT_EXTRACTION_MOZTOL_VALUE, new Double(0), null);
        m_parameterList.add(extractionMoZTolParameter);
        
        m_crossAssignStrategyCB = new JComboBox(CROSSASSIGN_STRATEGY_VALUES);
        m_crossAssignStrategyCB.setEnabled(!m_readOnly);
        m_crossAssignStrategyParameter = new ObjectParameter<>("crossAssignStrategy", "Cross Assignement Strategy", m_crossAssignStrategyCB, CROSSASSIGN_STRATEGY_VALUES, CROSSASSIGN_STRATEGY_KEYS,  0, null);
        m_parameterList.add(m_crossAssignStrategyParameter);
        
        m_crossAssignFeatureMappRTTolTF = new JTextField();
        m_crossAssignFeatureMappRTTolTF.setEnabled(!m_readOnly);
        DoubleParameter featureMappingTimeTolParameter = new DoubleParameter("featureTimeTol", "RT tolerance", m_crossAssignFeatureMappRTTolTF, DEFAULT_CA_FEATMAP_RTTOL_VALUE, new Double(0), null);
        m_parameterList.add(featureMappingTimeTolParameter);

        m_alignRTCB = new JCheckBox("Align RT");
        m_alignRTCB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                enableAlignment(m_alignRTCB.isSelected());
            }
        });
        m_alignRTCB.setEnabled(!m_readOnly);
        BooleanParameter alignRTParameter = new BooleanParameter("alignRT", "Align RT", m_alignRTCB, Boolean.TRUE);
        m_parameterList.add(alignRTParameter);    
        
        m_alignmentFeatureMappTimeToleranceTF = new JTextField();
        m_alignmentFeatureMappTimeToleranceTF.setEnabled(!m_readOnly);
        DoubleParameter alignmentFeatureMappingTimeToleranceParameter = new DoubleParameter("featureMappingTimeTolerance", "Feature Mapping Time Tolerance", m_alignmentFeatureMappTimeToleranceTF, DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE, new Double(1), null);
        m_parameterList.add(alignmentFeatureMappingTimeToleranceParameter);
        
    }
    
    
    private void enableAlignment(boolean isEnabled){
        m_alignmentFeatureTimeTolLabel.setEnabled(isEnabled);
        m_alignmentFeatureMappTimeToleranceTF.setEnabled(isEnabled);
    }
    
    /**
     * set the quanti params. This method assume parameter are formated as V2 Params
     * Some params, not be visible here, may not be taken into account .... 
     * @param quantParams 
     */
    @Override
    public void setQuantParams(Map<String,Object>  quantParams){  //VDS TODO : CALLED only if params are V2 compliant !?
       
        //update GUI
        Map<String,Object> extRactParams = (Map<String,Object>) quantParams.get("extraction_params");
        m_extractionMoZTolTF.setText(""+Double.parseDouble(extRactParams.get("moz_tol").toString()));

        if(quantParams.containsKey("align_config")) {
            Map<String,Object> alnConfig = (Map<String,Object>) quantParams.get("align_config");
            m_alignRTCB.setSelected(true);
            
            try{
                //VDS : If align_config specified,  ALIGNMENT_METHOD_KEYS exist. As we are in simplified param, don't care which one             
                Map<String,Object> alnFtParams = (Map<String,Object>) ((Map<String,Object>) alnConfig.get("aln_params")).get("ft_mapping_params");
                String timeTolval = alnFtParams.getOrDefault("time_tol",DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE).toString();                            
                m_alignmentFeatureMappTimeToleranceTF.setText(timeTolval);             
            }catch(Exception ex){
                m_logger.error("error while settings quanti params "+ex);
                m_alignmentFeatureMappTimeToleranceTF.setText(DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE.toString());
            }
            
        } //End Alignment defined
                
        Map<String,Object> ftMappingParams =(Map<String,Object>)((Map<String,Object>) ((Map<String,Object>) quantParams.get("cross_assign_config")).get("cross_assign_params")).get("ft_mapping_params");
        try{
            m_crossAssignFeatureMappRTTolTF.setText(""+Double.parseDouble(ftMappingParams.get("time_tol").toString()));
        }catch(NumberFormatException ex){
            m_logger.error("error while settings quanti params "+ex);
            m_crossAssignFeatureMappRTTolTF.setText(DEFAULT_CA_FEATMAP_RTTOL_VALUE.toString());
        }
        
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
            crossAssignmentConfig.put("restrain_to_reliable_features", true);                     
            Map<String,Object> ftMappingParams = new HashMap<>();
            ftMappingParams.put("moz_tol", m_extractionMoZTolTF.getText());
            ftMappingParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
            ftMappingParams.put("time_tol", m_crossAssignFeatureMappRTTolTF.getText());        
            crossAssignmentConfig.put("ft_mapping_params", ftMappingParams);
            Map<String,Object> ftParams = new HashMap<>();     
            ftParams.put("name", DEFAULT_CA_FILTER_NAME_VALUE);
            ftParams.put("operator", DEFAULT_CA_FILTER_OPERATOR_VALUE);
            ftParams.put("value", DEFAULT_CA_FILTER_VALUE);
            crossAssignmentConfig.put("ft_filter", ftParams); 
            params.put("cross_assignment_config", crossAssignmentConfig);
            
            if(m_alignRTCB.isSelected()){
                Map<String,Object> alignmentConfig = new HashMap<>();
                alignmentConfig.put(AbstractGenericQuantParamsPanel.ALIGNMENT_METHOD_NAME, ALIGNMENT_METHOD_KEYS[0]);
                alignmentConfig.put("smoothing_method_name", ALIGNMENT_SMOOTHING_METHOD_KEYS[0]);   
                alignmentConfig.put("ft_mapping_method_name", FEATURE_MAPPING_METHOD_KEYS[0]);
                Map<String,Object> alnFtParams = new HashMap<>();
                alnFtParams.put("time_tol",  m_alignmentFeatureMappTimeToleranceTF.getText());
                alignmentConfig.put("ft_mapping_method_params", alnFtParams);                
                alignmentConfig.put("ignore_errors", false);
                params.put(AbstractGenericQuantParamsPanel.ALIGNMENT_CONFIG, alignmentConfig);
            }
            
        }
//        else
//            JOptionPane.showMessageDialog(this, "No CA not YI","Settings err", JOptionPane.ERROR);
        
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
        mainPanel.add(createHeaderPanel(), c);

        c.gridy++;
        mainPanel.add(createCrossAssignmentPanel(), c);
        

        c.gridy++;
        c.weighty = 1;
        mainPanel.add(Box.createVerticalGlue(), c);
        
        return mainPanel;
    }

    
    private JPanel createHeaderPanel() {
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
        m_crossAssignFeatureMappRTTolTF.setEnabled(isEnabled);
        m_alignRTCB.setEnabled(isEnabled);
        m_alignmentFeatureTimeTolLabel.setEnabled(isEnabled);
        m_alignmentFeatureMappTimeToleranceTF.setEnabled(isEnabled);
    }
    

    private JPanel createCrossAssignmentPanel(){
        JPanel panelA = new JPanel(new BorderLayout());
        m_crossAssignCBoxTitle = new CheckBoxTitledBorder("Cross Assignement", true);
        m_crossAssignCBoxTitle.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                enableCrossAssignment(m_crossAssignCBoxTitle.isSelected());
            }
        });
        panelA.setBorder(m_crossAssignCBoxTitle);
        
        m_crossAssignSettingsPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        m_allowCrossAssLabel = new JLabel("Allow cross assignement ");
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
        m_crossAssignSettingsPanel.add(m_crossAssignFeatureMappRTTolTF, c);
        
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
        m_alignmentFeatureTimeTolLabel = new JLabel("time tolerance (s):");
        m_crossAssignSettingsPanel.add(m_alignmentFeatureTimeTolLabel, c);
        c.gridx++;   
        c.weightx = 1;
        m_crossAssignSettingsPanel.add(m_alignmentFeatureMappTimeToleranceTF,c);
        panelA.add(m_crossAssignSettingsPanel, BorderLayout.NORTH);
        return panelA ;                 
    }           
    
   
    
}
