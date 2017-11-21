package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.*;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;

/**
 * Panel to set the different parameters for the XIC Quantitation
 * @author VD225637
 */
public class DefineQuantParamsCompletePanel extends AbstractDefineQuantParamsPanel {
    

    private final static String[] CLUSTERING_TIME_COMPUTATION_VALUES = {"Most Intense", "Median"};
    private final static String[] CLUSTERING_TIME_COMPUTATION_KEYS = {"MOST_INTENSE", "MEDIAN"};
    
    private final static String[] CLUSTERING_INTENSITY_COMPUTATION_VALUES = {"Most Intense", "Sum"};
    private final static String[] CLUSTERING_INTENSITY_COMPUTATION_KEYS = {"MOST_INTENSE", "SUM"};

    
    private final static String[] FEATURE_FILTER_NAME_VALUES = {"Intensity", "Relative Intensity"};
    private final static String[] FEATURE_FILTER_NAME_KEYS = {"INTENSITY", "RELATIVE_INTENSITY"};

    private final static String[] FEATURE_FILTER_OPERATOR_VALUES = {">", "<"};
    private final static String[] FEATURE_FILTER_OPERATOR_KEYS = {"GT", "LT"};

    private final static String[] FEATURE_NORMALIZATION_VALUES = {"No", "Intensity Sum", "Median Intensity", "Median Ratio"};
    private final static String[] FEATURE_NORMALIZATION_KEYS = {"NONE", "INTENSITY_SUM", "MEDIAN_INTENSITY", "MEDIAN_RATIO"};  
    
    
    private ObjectParameter<String> m_clusteringTimeComputationParameter;
    private ObjectParameter<String> m_clusteringIntensityComputationParameter;
    private ObjectParameter<String> m_featureFilterNameParameter;
    private ObjectParameter<String> m_featureFilterOperatorParameter;
    private ObjectParameter<String> m_normalizationParameter;

    protected ObjectParameter<String> m_featureMappingMethodParameter;
    
    private JTextField m_clusteringMoZTolTF;
    private JTextField m_clusteringTimeTolTF;
    private JComboBox  m_clusteringTimeComputationCB;
    private JComboBox  m_clusteringIntensityComputationCB;
     
    private JCheckBox m_useLastPeakelDetectionCB;

    private JTextField m_alignmentMassIntervalTF;
    private JTextField m_alignmentSmoothingWinOverlapTF;
    private JTextField m_alignmentFeatureMappingMoZTolTF;
    
    private JComboBox m_featureFilterNameCB;
    private JComboBox m_featureFilterOperatorCB;
    private JTextField m_featureFilterValueTF;
    
    
    protected JComboBox  m_featureMappingMethodCB;
    private JTextField m_featureMappingMoZTolTF;
    
    private JComboBox m_normalizationCB;

    private JTabbedPane m_tabbedPane;

    
    public DefineQuantParamsCompletePanel(boolean readOnly, boolean readValues) {
        super(readOnly);

        m_parameterList = new ParameterList("XicParameters");
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
        return false;
    }
    
    private void createParameters() {
        
        m_extractionMoZTolTF = new JTextField();
        m_extractionMoZTolTF.setEnabled(!m_readOnly);
        DoubleParameter extractionMoZTolParameter = new DoubleParameter("extractionMoZTol", "Extraction moz tolerance", m_extractionMoZTolTF, new Double(5), new Double(0), null);
        m_parameterList.add(extractionMoZTolParameter);
        
        m_useLastPeakelDetectionCB = new JCheckBox("Use last peakel detection");
        Preferences preferences = NbPreferences.root();
        m_useLastPeakelDetectionCB.setEnabled(!m_readOnly && preferences.getBoolean("Profi", false));
        BooleanParameter useLastPeakelDetectionParameter = new BooleanParameter("useLastPeakelDetection", "Use last peakel detection", m_useLastPeakelDetectionCB, Boolean.FALSE);
        m_parameterList.add(useLastPeakelDetectionParameter);

        
        m_clusteringMoZTolTF = new JTextField();
        m_clusteringMoZTolTF.setEnabled(!m_readOnly);
        DoubleParameter m_clusteringMoZTolParameter = new DoubleParameter("clusteringMoZTol", "Clustering moz tolerance", m_clusteringMoZTolTF, new Double(5), new Double(0), null);
        m_parameterList.add(m_clusteringMoZTolParameter);
        
        m_clusteringTimeTolTF = new JTextField();
        m_clusteringTimeTolTF.setEnabled(!m_readOnly);
        DoubleParameter m_clusteringTimeTolParameter = new DoubleParameter("clusteringTimeTol", "Clustering time tolerance", m_clusteringTimeTolTF, new Double(15), new Double(0), null);
        m_parameterList.add(m_clusteringTimeTolParameter);

        m_clusteringTimeComputationCB = new JComboBox(CLUSTERING_TIME_COMPUTATION_VALUES);
        m_clusteringTimeComputationCB.setEnabled(!m_readOnly);
        m_clusteringTimeComputationParameter = new ObjectParameter<>("clusteringTimeComputation", "Clustering time computation", m_clusteringTimeComputationCB, CLUSTERING_TIME_COMPUTATION_VALUES, CLUSTERING_TIME_COMPUTATION_KEYS,  0, null);
        m_parameterList.add(m_clusteringTimeComputationParameter);
        

        
        m_clusteringIntensityComputationCB = new JComboBox(CLUSTERING_INTENSITY_COMPUTATION_VALUES);
        m_clusteringIntensityComputationCB.setEnabled(!m_readOnly);
        m_clusteringIntensityComputationParameter = new ObjectParameter<>("clusteringIntensityComputation", "Clustering intensity computation", m_clusteringIntensityComputationCB, CLUSTERING_INTENSITY_COMPUTATION_VALUES, CLUSTERING_INTENSITY_COMPUTATION_KEYS, 0, null);
        m_parameterList.add(m_clusteringIntensityComputationParameter);

        
        m_alignmentMethodCB = new JComboBox(ALIGNMENT_METHOD_VALUES);
        m_alignmentMethodCB.setEnabled(!m_readOnly);
        m_alignmentMethodParameter = new ObjectParameter<>("alignmentMethod", "Alignment Method", m_alignmentMethodCB, ALIGNMENT_METHOD_VALUES, ALIGNMENT_METHOD_KEYS,  1, null);
        m_parameterList.add(m_alignmentMethodParameter);

        m_alignmentMaxIterationTF = new JTextField();
        m_alignmentMaxIterationTF.setEnabled(!m_readOnly);
        IntegerParameter alignmentMaxIterationParameter = new IntegerParameter("alignmentMaxIteration", "Alignment max iteration", m_alignmentMaxIterationTF, new Integer(3), new Integer(1), null);
        m_parameterList.add(alignmentMaxIterationParameter);
        
        m_alignmentSmoothingMethodCB = new JComboBox(ALIGNMENT_SMOOTHING_METHOD_VALUES);
        m_alignmentSmoothingMethodCB.setEnabled(!m_readOnly);
        m_alignmentSmoothingMethodParameter = new ObjectParameter("alignmentSmoothingMethod", "Alignment Smoothing Method", m_alignmentSmoothingMethodCB, ALIGNMENT_SMOOTHING_METHOD_VALUES, ALIGNMENT_SMOOTHING_METHOD_KEYS,  0, null);
        m_parameterList.add(m_alignmentSmoothingMethodParameter);

        
        m_alignmentSmoothingWinSizeTF = new JTextField();
        m_alignmentSmoothingWinSizeTF.setEnabled(!m_readOnly);
        IntegerParameter alignmentSmoothingWinSizeParameter = new IntegerParameter("alignmentTimeInterval", "Alignment time interval", m_alignmentSmoothingWinSizeTF, new Integer(200), new Integer(1), null);
        m_parameterList.add(alignmentSmoothingWinSizeParameter);
        
        m_alignmentSmoothingWinOverlapTF = new JTextField();
        m_alignmentSmoothingWinOverlapTF.setEnabled(!m_readOnly);
        IntegerParameter alignmentSmoothingWinOverlapParameter = new IntegerParameter("smoothingSlidingWindowOverlap", "Smoothing sliding window overlap", m_alignmentSmoothingWinOverlapTF, new Integer(20), new Integer(1), new Integer(100));
        m_parameterList.add(alignmentSmoothingWinOverlapParameter);
        
         m_alignmentSmoothingMinWinlandmarksTF = new JTextField();
         m_alignmentSmoothingMinWinlandmarksTF.setEnabled(!m_readOnly);
        IntegerParameter alignmentSmoothingMinWinlandmarksParameter = new IntegerParameter("smoothingMinimumNumberOfLandmarks", "Smoothing minimum number of landmarks", m_alignmentSmoothingMinWinlandmarksTF, new Integer(50), new Integer(1), null);
        m_parameterList.add(alignmentSmoothingMinWinlandmarksParameter);
        
        m_alignmentFeatureMappingMoZTolTF = new JTextField();
        m_alignmentFeatureMappingMoZTolTF.setEnabled(!m_readOnly);
        DoubleParameter alignmentFeatureMappingMoZTolParameter = new DoubleParameter("featureMappingMozTolerance", "Feature Mapping Moz Tolerance", m_alignmentFeatureMappingMoZTolTF, new Double(5), new Double(0), null);
        m_parameterList.add(alignmentFeatureMappingMoZTolParameter);
        
        m_featureMappingMethodCB = new JComboBox(FEATURE_MAPPING_METHOD_VALUES);
        m_featureMappingMethodCB.setEnabled(!m_readOnly);
        m_featureMappingMethodParameter = new ObjectParameter("featureMappingMethod", "Feature Mapping Method", m_featureMappingMethodCB, FEATURE_MAPPING_METHOD_VALUES, FEATURE_MAPPING_METHOD_KEYS, 0, null);
        m_parameterList.add(m_featureMappingMethodParameter);
        
        m_alignmentFeatureMappingTimeToleranceTF = new JTextField();
        m_alignmentFeatureMappingTimeToleranceTF.setEnabled(!m_readOnly);
        DoubleParameter alignmentFeatureMappingTimeToleranceParameter = new DoubleParameter("featureMappingTimeTolerance", "Feature Mapping Time Tolerance", m_alignmentFeatureMappingTimeToleranceTF, new Double(600), new Double(1), null);
        m_parameterList.add(alignmentFeatureMappingTimeToleranceParameter);
        
        m_featureFilterNameCB  = new JComboBox(FEATURE_FILTER_NAME_VALUES);
        m_featureFilterNameCB.setEnabled(!m_readOnly);
        m_featureFilterNameParameter = new ObjectParameter<>("featureFilterNameParameter ", "Feature Name Parameter", m_featureFilterNameCB, FEATURE_FILTER_NAME_VALUES, FEATURE_FILTER_NAME_KEYS,  0, null);
        m_parameterList.add(m_featureFilterNameParameter);
        
        m_featureFilterOperatorCB = new JComboBox(FEATURE_FILTER_OPERATOR_VALUES);
        m_featureFilterOperatorCB.setEnabled(!m_readOnly);
        m_featureFilterOperatorParameter = new ObjectParameter<>("featureFilterOperator ", "Feature Filter Operator", m_featureFilterOperatorCB, FEATURE_FILTER_OPERATOR_VALUES, FEATURE_FILTER_OPERATOR_KEYS,  0, null);
        m_parameterList.add(m_featureFilterOperatorParameter);
        

        m_featureFilterValueTF = new JTextField();
        m_featureFilterValueTF.setEnabled(!m_readOnly);
        DoubleParameter m_featureFilterValueParameter = new DoubleParameter("featureFilterValue", "Feature Filter Value", m_featureFilterValueTF, new Double(0), null, null);
        m_parameterList.add(m_featureFilterValueParameter);
        
        m_featureMappingMoZTolTF = new JTextField();
        m_featureMappingMoZTolTF.setEnabled(!m_readOnly);
        DoubleParameter featureMappingMoZTolParameter = new DoubleParameter("featureMoZTol", "Feature moz tolerance", m_featureMappingMoZTolTF, new Double(5), new Double(0), null);
        m_parameterList.add(featureMappingMoZTolParameter);
        
        
        m_featureMappingTimeTolTF = new JTextField();
        m_featureMappingTimeTolTF.setEnabled(!m_readOnly);
        DoubleParameter featureMappingTimeTolParameter = new DoubleParameter("featureTimeTol", "Feature time tolerance", m_featureMappingTimeTolTF, new Double(60), new Double(0), null);
        m_parameterList.add(featureMappingTimeTolParameter);
        
        
        m_normalizationCB = new JComboBox(FEATURE_NORMALIZATION_VALUES);
        m_normalizationCB.setEnabled(!m_readOnly);
        m_normalizationParameter = new ObjectParameter<>("normalization", "Normalization", m_normalizationCB, FEATURE_NORMALIZATION_VALUES, FEATURE_NORMALIZATION_KEYS,  0, null);
        m_parameterList.add(m_normalizationParameter);

    }
    


    
    /**
     * set the quanti params
     * @param quantParams 
     */
    @Override
    public void setQuantParams(Map<String,Object>  quantParams){
        Map<String,Object> extRactParams = (Map<String,Object>) quantParams.get("extraction_params");
        m_extractionMoZTolTF.setText(""+Double.parseDouble(extRactParams.get("moz_tol").toString()));
        
        m_useLastPeakelDetectionCB.setSelected(Boolean.parseBoolean(quantParams.get("use_last_peakel_detection").toString()));
        
        Map<String,Object> clusterParams = (Map<String,Object>) quantParams.get("clustering_params");
        m_clusteringMoZTolTF.setText(""+Double.parseDouble(clusterParams.get("moz_tol").toString()));
        m_clusteringTimeTolTF.setText(""+Double.parseDouble(clusterParams.get("time_tol").toString()));
        for(int i=0; i<CLUSTERING_TIME_COMPUTATION_KEYS.length; i++){
            if (CLUSTERING_TIME_COMPUTATION_KEYS[i].equals(clusterParams.get("time_computation").toString())){
                m_clusteringTimeComputationCB.setSelectedIndex(i);
                break;
            }
        }
        for(int i=0; i<CLUSTERING_INTENSITY_COMPUTATION_KEYS.length; i++){
            if (CLUSTERING_INTENSITY_COMPUTATION_KEYS[i].equals(clusterParams.get("intensity_computation").toString())){
                m_clusteringIntensityComputationCB.setSelectedIndex(i);
                break;
            }
        }
        for(int i=0; i<ALIGNMENT_METHOD_KEYS.length; i++){
            if (ALIGNMENT_METHOD_KEYS[i].equals((String) quantParams.get("aln_method_name"))){
                m_alignmentMethodCB.setSelectedIndex(i);
                break;
            }
        }
        
        Map<String,Object> alnParams =(Map<String,Object>) quantParams.get("aln_params");
        m_alignmentMassIntervalTF.setText(""+Integer.parseInt(alnParams.get("mass_interval").toString()));
        m_alignmentMaxIterationTF.setText(""+Integer.parseInt(alnParams.get("max_iterations").toString()));
        for(int i=0; i<ALIGNMENT_SMOOTHING_METHOD_KEYS.length; i++){
            if (ALIGNMENT_SMOOTHING_METHOD_KEYS[i].equals((String) alnParams.get("smoothing_method_name"))){
                m_alignmentSmoothingMethodCB.setSelectedIndex(i);
                break;
            }
        }
        Map<String,Object> smootingParams =(Map<String,Object>) alnParams.get("smoothing_params");
        m_alignmentSmoothingWinSizeTF.setText(""+Integer.parseInt(smootingParams.get("window_size").toString()));
        m_alignmentSmoothingWinOverlapTF.setText(""+Integer.parseInt(smootingParams.get("window_overlap").toString()));
        m_alignmentSmoothingMinWinlandmarksTF.setText(""+Integer.parseInt(smootingParams.get("min_window_landmarks").toString()));
        Map<String,Object> alnFtParams = (Map<String,Object>) alnParams.get("ft_mapping_params");
        m_alignmentFeatureMappingMoZTolTF.setText(""+Double.parseDouble(alnFtParams.get("moz_tol").toString()));
        m_alignmentFeatureMappingTimeToleranceTF.setText(""+Double.parseDouble(alnFtParams.get("time_tol").toString()));
        
        for (int i = 0; i < FEATURE_MAPPING_METHOD_KEYS.length; i++) {
            if (FEATURE_MAPPING_METHOD_KEYS[i].equals((String) quantParams.get("ft_mapping_method_name"))) {
                m_featureMappingMethodCB.setSelectedIndex(i);
                break;
            }
        }
        
        Map<String,Object> ftParams =(Map<String,Object>) quantParams.get("ft_filter");
        for(int i=0; i<FEATURE_FILTER_NAME_KEYS.length; i++){
            if (FEATURE_FILTER_NAME_KEYS[i].equals((String) ftParams.get("name"))){
                m_featureFilterNameCB.setSelectedIndex(i);
                break;
            }
        }
        for(int i=0; i<FEATURE_FILTER_OPERATOR_KEYS.length; i++){
            if (FEATURE_FILTER_OPERATOR_KEYS[i].equals((String) ftParams.get("operator"))){
                m_featureFilterOperatorCB.setSelectedIndex(i);
                break;
            }
        }
        try{
            m_featureFilterValueTF.setText(""+Double.parseDouble(ftParams.get("value").toString()));
        }catch(NumberFormatException | NullPointerException ex){
            m_logger.error("error while settings quanti params "+ex);
            m_featureFilterValueTF.setText("0.0");
        }
        
        Map<String,Object> ftMappingParams =(Map<String,Object>) quantParams.get("ft_mapping_params");
        try{
            m_featureMappingMoZTolTF.setText(""+Double.parseDouble(ftMappingParams.get("moz_tol").toString()));
            m_featureMappingTimeTolTF.setText(""+Double.parseDouble(ftMappingParams.get("time_tol").toString()));
        }catch(NumberFormatException ex){
            m_logger.error("error while settings quanti params "+ex);
            m_featureMappingMoZTolTF.setText("10.0");
            m_featureMappingTimeTolTF.setText("120.0");
        }
        
        if (quantParams.containsKey("normalization_method")){
            for(int i=0; i<FEATURE_NORMALIZATION_KEYS.length; i++){
                if (FEATURE_NORMALIZATION_KEYS[i].equals((String) quantParams.get("normalization_method"))){
                    m_normalizationCB.setSelectedIndex(i);
                    break;
                }
            }
        }else{
            m_normalizationCB.setSelectedIndex(0);
        }

    }
    
    
    @Override
    public Map<String,Object> getQuantParams(){
        Map<String,Object> params = new HashMap<>();
        
        Map<String,Object> extRactParams = new HashMap<>();
        String extractionTolStr = m_extractionMoZTolTF.getText();
        
        params.put("use_last_peakel_detection", m_useLastPeakelDetectionCB.isSelected());
        
        extRactParams.put("moz_tol", extractionTolStr);
        extRactParams.put("moz_tol_unit", "PPM");
        params.put("extraction_params", extRactParams);
        
        Map<String,Object> clusterParams = new HashMap<>();
        clusterParams.put("moz_tol", m_clusteringMoZTolTF.getText());
        clusterParams.put("moz_tol_unit","PPM");
        clusterParams.put("time_tol", m_clusteringTimeTolTF.getText());
        clusterParams.put("time_computation", m_clusteringTimeComputationParameter.getStringValue());
        clusterParams.put("intensity_computation", m_clusteringIntensityComputationParameter.getStringValue());
        params.put("clustering_params", clusterParams);    
        
        params.put("aln_method_name", m_alignmentMethodParameter.getStringValue());
        Map<String,Object> alnParams = new HashMap<>();
        alnParams.put("mass_interval", m_alignmentMassIntervalTF.getText());
        alnParams.put("max_iterations", m_alignmentMaxIterationTF.getText());
        alnParams.put("smoothing_method_name", m_alignmentSmoothingMethodParameter.getStringValue());   
        Map<String,Object> smootingParams = new HashMap<>();
        smootingParams.put("window_size", m_alignmentSmoothingWinSizeTF.getText());
        smootingParams.put("window_overlap", m_alignmentSmoothingWinOverlapTF.getText());
        smootingParams.put("min_window_landmarks", m_alignmentSmoothingMinWinlandmarksTF.getText());
        alnParams.put("smoothing_params", smootingParams);     
        
        
        params.put("ft_mapping_method_name", m_featureMappingMethodParameter.getStringValue());
        Map<String,Object> alnFtParams = new HashMap<>();
        alnFtParams.put("moz_tol", m_alignmentFeatureMappingMoZTolTF.getText());
        alnFtParams.put("moz_tol_unit", "PPM");
        alnFtParams.put("time_tol",  m_alignmentFeatureMappingTimeToleranceTF.getText());
        alnParams.put("ft_mapping_params", alnFtParams);
        params.put("aln_params", alnParams); 

        Map<String,Object> ftParams = new HashMap<>();     
        ftParams.put("name", m_featureFilterNameParameter.getStringValue());
        ftParams.put("operator", m_featureFilterOperatorParameter.getStringValue());
        ftParams.put("value", m_featureFilterValueTF.getText());
        params.put("ft_filter", ftParams); 
         
        Map<String,Object> ftMappingParams = new HashMap<>();
        ftMappingParams.put("moz_tol", m_featureMappingMoZTolTF.getText());
        ftMappingParams.put("moz_tol_unit", "PPM");
        ftMappingParams.put("time_tol", m_featureMappingTimeTolTF.getText());        
        params.put("ft_mapping_params", ftMappingParams); 
        // normalization parameter is an option
        if (m_normalizationParameter.getStringValue() != null && !m_normalizationParameter.getStringValue().equalsIgnoreCase("NONE")) {
            params.put("normalization_method", m_normalizationParameter.getStringValue());
        }

        params.put("detect_features", false);
        params.put("start_from_validated_peptides", true);
        params.put("detect_peakels", true);

        return params;
    }
        

    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel extractionMoZTolLabel = new JLabel("Extraction moz tolerance (ppm):");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        headerPanel.add(extractionMoZTolLabel, c);

        c.gridx++;
        c.gridwidth=1;
        c.weightx = 1;
        headerPanel.add(m_extractionMoZTolTF, c);

        c.gridx++;
        c.gridwidth=1;
        c.weightx = 0.4;
        headerPanel.add(m_useLastPeakelDetectionCB, c);

        
        mainPanel.add(headerPanel, BorderLayout.PAGE_START);
        
        // tabbed pane
        m_tabbedPane = new JTabbedPane();
        m_tabbedPane.addTab("Clustering", null, createClusteringPanel(), "Feature Clustering");
        m_tabbedPane.addTab("Alignment", null, createAlignmentPanel(), "Map Alignment");
        m_tabbedPane.addTab("Normalization", null, createNormalizationPanel(), "Map Normalization");
        m_tabbedPane.addTab("Master Map", null, createFTPanel(), "Master Map");
        
        mainPanel.add(m_tabbedPane, BorderLayout.CENTER);
        
        
        return mainPanel;
    }
    
    
    private JPanel createNormalizationPanel() {
        JPanel panelN = new JPanel(new BorderLayout());
        JPanel normalizationPanel = new JPanel(new GridBagLayout());
        normalizationPanel.setBorder(createTitledBorder(" Map Normalization ", 1));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.weighty= 0;
        c.gridx = 0;
        c.gridy = 0;   
        c.gridwidth = 1;
        c.gridheight = 1;
        normalizationPanel.add(new JLabel("Map intensities Normalization "), c);
        c.gridx++;
        c.weighty= 0;
        c.weightx = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        normalizationPanel.add(m_normalizationCB, c);
        
        panelN.add(normalizationPanel, BorderLayout.NORTH);
        return panelN ;
    }
    
    
    private JPanel createClusteringPanel(){
        JPanel panelC = new JPanel(new BorderLayout());
        JPanel clusteringPanel = new JPanel(new GridBagLayout());
        clusteringPanel.setBorder(createTitledBorder(" Feature Clustering ", 1));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;                
        clusteringPanel.add(new JLabel("moz tolerance (ppm):"), c);        
        c.gridx++;  
        c.weightx = 1;
        clusteringPanel.add(m_clusteringMoZTolTF, c);
        c.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        clusteringPanel.add(new JLabel("time tolerance (s):"), c);
        c.gridx++;       
        c.weightx = 1;
        clusteringPanel.add(m_clusteringTimeTolTF, c);
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        clusteringPanel.add(new JLabel("time computation:"), c);
        c.gridx++;    
        c.weightx = 1;
        clusteringPanel.add(m_clusteringTimeComputationCB, c);
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        clusteringPanel.add(new JLabel("intensity computation:"), c);
        c.gridx++;       
        c.weightx = 1;
        clusteringPanel.add(m_clusteringIntensityComputationCB, c);
        c.weightx = 0;

        panelC.add(clusteringPanel, BorderLayout.NORTH);
        return panelC ;               
    }
    
    private JPanel createAlignmentPanel(){
        JPanel panelA = new JPanel(new BorderLayout());
        
        JPanel alignmentPanel = new JPanel(new GridBagLayout());
        alignmentPanel.setBorder(createTitledBorder(" Map Alignment ", 1));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;                
        alignmentPanel.add(new JLabel("method:"), c);        
        c.gridx++;     
        c.weightx = 1;
        alignmentPanel.add(m_alignmentMethodCB, c);
        c.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        alignmentPanel.add(new JLabel("mass interval:"), c);
        c.gridx++;     
        c.weightx = 1;
        m_alignmentMassIntervalTF = new JTextField();
        m_alignmentMassIntervalTF.setText("20000"); //VDS TODO : hide ? see DBO description
        m_alignmentMassIntervalTF.setEditable(false);//VDS - DBO info :  Mass Interval (hidden / non-editable)
        m_alignmentMassIntervalTF.setEnabled(!m_readOnly);
        alignmentPanel.add(m_alignmentMassIntervalTF, c);
        c.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        alignmentPanel.add(new JLabel("max iteration:"), c);
        c.gridx++;   
        c.weightx = 1;
        alignmentPanel.add(m_alignmentMaxIterationTF, c);
        c.weightx = 0;

        // Add Alignement Smoothing specific params
        JPanel smootingPanel = new JPanel(new GridBagLayout());
        smootingPanel.setBorder(createTitledBorder(" Smoothing ", 2));

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;                
        smootingPanel.add(new JLabel("method:"), c1);        
        c1.gridx++;     
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothingMethodCB, c1);
        c1.weightx = 0;
                
        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("alignment time interval (s):"), c1);
        c1.gridx++;  
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothingWinSizeTF, c1);
        c1.weightx = 0;
    
        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("sliding window overlap (%):"), c1);
        c1.gridx++;   
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothingWinOverlapTF, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("minimum number of landmarks :"), c1);
        c1.gridx++; 
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothingMinWinlandmarksTF, c1);
        c1.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        alignmentPanel.add(smootingPanel, c);

        
        // Add Alignement  FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 2));

        c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.FIRST_LINE_START;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);


        
        c1.gridx = 0;
        c1.gridy = 0;    
        ftParamsPanel.add(new JLabel("method:"), c1);        
        c1.gridx++;     
        c1.weightx = 1;
        ftParamsPanel.add(m_featureMappingMethodCB, c1);
        c1.weightx = 0;
        
        c1.gridy++;
        c1.gridx = 0;
        ftParamsPanel.add(new JLabel("moz tolerance (ppm):"), c1);        
        c1.gridx++;       
        c1.weightx = 1;
        ftParamsPanel.add(m_alignmentFeatureMappingMoZTolTF, c1);
        c1.weightx = 0;
        
        c1.gridx = 0;
        c1.gridy++;                
        ftParamsPanel.add(new JLabel("time tolerance (s):"), c1);        
        c1.gridx++;   
        c1.weightx = 1;
        ftParamsPanel.add(m_alignmentFeatureMappingTimeToleranceTF, c1);
        c1.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        alignmentPanel.add(ftParamsPanel, c);                

        panelA.add(alignmentPanel, BorderLayout.NORTH);
        return panelA ;                 
    }           
    
    private JPanel createFTPanel(){
        JPanel panelF = new JPanel(new BorderLayout());
        
        JPanel masterMapPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cm = new GridBagConstraints();
        cm.anchor = GridBagConstraints.FIRST_LINE_START ;
        cm.fill = GridBagConstraints.BOTH;
        cm.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JPanel ftPanel = new JPanel(new GridBagLayout());
        ftPanel.setBorder(createTitledBorder(" Feature Filter ", 1));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        ftPanel.add(new JLabel("name:"), c);        
        c.gridx++;   
        c.weightx = 1;
        ftPanel.add(m_featureFilterNameCB, c);
               
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        ftPanel.add(new JLabel("operator:"), c);
        c.gridx++;    
        c.weightx = 1;
        ftPanel.add(m_featureFilterOperatorCB, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        ftPanel.add(new JLabel("value:"), c);
        c.gridx++;   
        c.weightx = 1;
        ftPanel.add(m_featureFilterValueTF, c);
        
        cm.gridy = 0;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;
        masterMapPanel.add(ftPanel, cm);    

         // Add FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 1));

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST ;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;                
        ftParamsPanel.add(new JLabel("moz tolerance (ppm):"), c1);        
        c1.gridx++;       
        c1.weightx = 1;
        ftParamsPanel.add(m_featureMappingMoZTolTF, c1);
        c1.weightx = 0;
        
        c1.gridx = 0;
        c1.gridy++;                
        ftParamsPanel.add(new JLabel("time tolerance (s):"), c1);        
        c1.gridx++;     
        c1.weightx = 1;
        ftParamsPanel.add(m_featureMappingTimeTolTF, c1);
        c1.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth=2;
        
        cm.gridy++;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;
        masterMapPanel.add(ftParamsPanel, cm);

        panelF.add(masterMapPanel, BorderLayout.NORTH);
        return panelF;                
    }

    
    
}
