package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.CheckBoxTitledBorder;
import fr.proline.studio.parameter.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Panel to set the different parameters for the XIC Quantitation
 *
 * @author VD225637
 */
public class LabelFreeMSParamsCompletePanel extends AbstractLabelFreeMSParamsPanel {

    private ObjectParameter<String> m_clusteringTimeComputationParameter;
    private ObjectParameter<String> m_clusteringIntensityComputationParameter;
    private ObjectParameter<String> m_featureFilterNameParameter;
    private ObjectParameter<String> m_featureFilterOperatorParameter;
    private ObjectParameter<String> m_normalizationParameter;
    private ObjectParameter<String> m_alignmentMethodParameter;
    private ObjectParameter<String> m_alignmentSmoothingMethodParameter;
    private ObjectParameter<String> m_alignmentFeatureMappMethodParameter;
    private ObjectParameter<String> m_crossAssignStrategyParameter;

    private BooleanParameter m_useLastPeakelDetectionParam = new BooleanParameter("useLastPeakelDetection", "Use last peakel detection", JCheckBox.class, Boolean.FALSE);
    private BooleanParameter m_alnIgnoreErrorsParameter = new BooleanParameter("ignoreErrors", "Ignore Alignment Errors", JCheckBox.class, Boolean.FALSE);

    private JTextField m_extractionMoZTolTF;
    private JTextField m_psmMatchingMoZTolTF;

    //ALIGNEMENT PARAMs
    JPanel m_alignementSettingsPanel;
    CheckBoxTitledBorder m_alignmentSettingsCBoxTitle;

    private JLabel m_algnmentMethodLabel;
    private JComboBox m_alignmentMethodCB;
    private JLabel m_alignmentMaxIteLabel;
    private JLabel m_alignmentFeatureMoZTolLabel;
    private JLabel m_alignmentFeatureTimeTolLabel;
    private JTextField m_alignmentMaxIterationTF;
    private JTextField m_alignmentFeatureMappMoZTolTF;
    private JTextField m_alignmentFeatureMappTimeToleranceTF;

    private JComboBox m_alignmentSmoothingMethodCB;
    private JLabel m_algnmentFeatureMapMethodLabel;
    private JLabel m_algnmentSmoothMethodLabel;
    private JLabel m_alignmentSmoothTimeIntervalLabel;
    private JLabel m_alignmentSmoothWinOverlapLabel;
    private JLabel m_alignmentSmoothNbrLMLabel;
    private JTextField m_alignmentSmoothWinSizeTF;
    private JTextField m_alignmentSmoothMinWinLMTF;
    private JTextField m_alignmentSmoothWinOverlapTF;

    private JComboBox m_alignmentFeatureMappMethodCB;

    //CROSS ASSIGNMENT PARAMs
    private JPanel m_crossAssignSettingsPanel;
    private JLabel m_allowCrossAssignLabel;
    private JLabel m_crossAssignFeatureMappRTTolLabel;
    private JLabel m_crossAssignFeatureMappMoZTolLabel;
    private JTextField m_crossAssignFeatureMappMoZTolTF;
    private JTextField m_crossAssignFeatureMappRTTolTF;
    private CheckBoxTitledBorder m_crossAssignCBoxTitle;
    private JComboBox m_crossAssignStrategyCB;

    private JPanel m_featureFilterPanel;
    private JLabel m_featureFilterNameLabel;
    private JLabel m_featureFilterOperatorLabel;
    private JLabel m_featureFilterValueLabel;
    private CheckBoxTitledBorder m_featureFilterCBTitle;
    private JComboBox m_featureFilterNameCB;
    private JComboBox m_featureFilterOperatorCB;
    private JTextField m_featureFilterValueTF;
    private JCheckBox m_retainOnlyReliableFeatures;

    //CLUSTERING PARAMs
    private JTextField m_clusteringMoZTolTF;
    private JTextField m_clusteringTimeTolTF;
    private JComboBox m_clusteringTimeComputationCB;
    private JComboBox m_clusteringIntensityComputationCB;

    //NORMALIZATION PARAMs
    JPanel m_normalizationSettingsPanel;
    CheckBoxTitledBorder m_normalizationSettingsCBTitle;
    private JComboBox m_normalizationCB;
    private JLabel m_normalizationLabel;

    private JTabbedPane m_tabbedPane;

    public LabelFreeMSParamsCompletePanel(boolean readOnly, boolean readValues) {
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
        return false;
    }

    private void createParameters() {
        m_extractionMoZTolTF = new JTextField();
        DoubleParameter extractionMoZTolParameter = new DoubleParameter("extractionMoZTol", "Extraction moz tolerance", m_extractionMoZTolTF, DEFAULT_EXTRACTION_MOZTOL_VALUE, new Double(0), null);
        m_parameterList.add(extractionMoZTolParameter);

        m_psmMatchingMoZTolTF = new JTextField();
        DoubleParameter psmMatchingMoZTolTFParameter = new DoubleParameter("psmMatchingMoZTolTF", "PSM Matching moz tolerance", m_psmMatchingMoZTolTF, DEFAULT_EXTRACTION_MOZTOL_VALUE, new Double(0), null);
        m_parameterList.add(psmMatchingMoZTolTFParameter);
        m_parameterList.add(m_useLastPeakelDetectionParam);

        ((JCheckBox) m_useLastPeakelDetectionParam.getComponent(null)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JCheckBox) e.getSource()).isSelected()) {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Warning, if last peakel detection used only post peakel extraction parameters will be used !", "Xic Parameters Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        m_parameterList.add(m_alnIgnoreErrorsParameter);

        m_crossAssignStrategyCB = new JComboBox(CROSSASSIGN_STRATEGY_VALUES);
        m_crossAssignStrategyParameter = new ObjectParameter<>("crossAssignStrategy", "Cross Assignment Strategy", m_crossAssignStrategyCB, CROSSASSIGN_STRATEGY_VALUES, CROSSASSIGN_STRATEGY_KEYS, 0, null);
        m_parameterList.add(m_crossAssignStrategyParameter);

        m_clusteringMoZTolTF = new JTextField();
        DoubleParameter m_clusteringMoZTolParameter = new DoubleParameter("clusteringMoZTol", "Clustering moz tolerance", m_clusteringMoZTolTF, DEFAULT_CLUSTER_MOZTOL_VALUE, new Double(0), null);
        m_parameterList.add(m_clusteringMoZTolParameter);

        m_clusteringTimeTolTF = new JTextField();
        DoubleParameter m_clusteringTimeTolParameter = new DoubleParameter("clusteringTimeTol", "Clustering time tolerance", m_clusteringTimeTolTF, DEFAULT_CLUSTER_TIMETOL_VALUE, new Double(0), null);
        m_parameterList.add(m_clusteringTimeTolParameter);

        m_clusteringTimeComputationCB = new JComboBox(CLUSTERING_TIME_COMPUTATION_VALUES);
        m_clusteringTimeComputationParameter = new ObjectParameter<>("clusteringTimeComputation", "Clustering time computation", m_clusteringTimeComputationCB, CLUSTERING_TIME_COMPUTATION_VALUES, CLUSTERING_TIME_COMPUTATION_KEYS, 0, null);
        m_parameterList.add(m_clusteringTimeComputationParameter);

        m_clusteringIntensityComputationCB = new JComboBox(CLUSTERING_INTENSITY_COMPUTATION_VALUES);
        m_clusteringIntensityComputationParameter = new ObjectParameter<>("clusteringIntensityComputation", "Clustering intensity computation", m_clusteringIntensityComputationCB, CLUSTERING_INTENSITY_COMPUTATION_VALUES, CLUSTERING_INTENSITY_COMPUTATION_KEYS, 0, null);
        m_parameterList.add(m_clusteringIntensityComputationParameter);

        m_alignmentMethodCB = new JComboBox(ALIGNMENT_METHOD_VALUES);
        m_alignmentMethodParameter = new ObjectParameter<>("alignmentMethod", "Alignment Method", m_alignmentMethodCB, ALIGNMENT_METHOD_VALUES, ALIGNMENT_METHOD_KEYS, 0, null);
        m_parameterList.add(m_alignmentMethodParameter);

        m_alignmentMaxIterationTF = new JTextField();
        IntegerParameter alignmentMaxIterationParameter = new IntegerParameter("alignmentMaxIteration", "Alignment max iteration", m_alignmentMaxIterationTF, DEFAULT_ALIGN_MAXITE_VALUE, new Integer(1), null);
        m_parameterList.add(alignmentMaxIterationParameter);

        m_alignmentSmoothingMethodCB = new JComboBox(ALIGNMENT_SMOOTHING_METHOD_VALUES);
        m_alignmentSmoothingMethodParameter = new ObjectParameter("alignmentSmoothingMethod", "Alignment Smoothing Method", m_alignmentSmoothingMethodCB, ALIGNMENT_SMOOTHING_METHOD_VALUES, ALIGNMENT_SMOOTHING_METHOD_KEYS, 0, null);
        m_parameterList.add(m_alignmentSmoothingMethodParameter);

        m_alignmentSmoothWinSizeTF = new JTextField();
        IntegerParameter alignmentSmoothingWinSizeParameter = new IntegerParameter("alignmentTimeInterval", "Alignment time interval", m_alignmentSmoothWinSizeTF, DEFAULT_SMOOTH_WINSIZE_VALUE, new Integer(1), null);
        m_parameterList.add(alignmentSmoothingWinSizeParameter);

        m_alignmentSmoothWinOverlapTF = new JTextField();
        IntegerParameter alignmentSmoothingWinOverlapParameter = new IntegerParameter("smoothingSlidingWindowOverlap", "Smoothing sliding window overlap", m_alignmentSmoothWinOverlapTF, DEFAULT_SMOOTH_WINOVERLAP_VALUE, new Integer(1), DEFAULT_SMOOTH_WINOVERLAP_MAX_VALUE);
        m_parameterList.add(alignmentSmoothingWinOverlapParameter);

        m_alignmentSmoothMinWinLMTF = new JTextField();
        IntegerParameter alignmentSmoothingMinWinlandmarksParameter = new IntegerParameter("smoothingMinimumNumberOfLandmarks", "Smoothing minimum number of landmarks", m_alignmentSmoothMinWinLMTF, DEFAULT_SMOOTH_NBRLM_VALUE, new Integer(1), null);
        m_parameterList.add(alignmentSmoothingMinWinlandmarksParameter);

        m_alignmentFeatureMappMoZTolTF = new JTextField();
        DoubleParameter alignmentFeatureMappingMoZTolParameter = new DoubleParameter("featureMappingMozTolerance", "Feature Mapping Moz Tolerance", m_alignmentFeatureMappMoZTolTF, DEFAULT_ALIGN_FEATMAP_MOZTOL_VALUE, new Double(0), null);
        m_parameterList.add(alignmentFeatureMappingMoZTolParameter);

        m_alignmentFeatureMappMethodCB = new JComboBox(FEATURE_MAPPING_METHOD_VALUES);
        m_alignmentFeatureMappMethodParameter = new ObjectParameter("featureMappingMethod", "Feature Mapping Method", m_alignmentFeatureMappMethodCB, FEATURE_MAPPING_METHOD_VALUES, FEATURE_MAPPING_METHOD_KEYS, 0, null);
        m_parameterList.add(m_alignmentFeatureMappMethodParameter);

        m_alignmentFeatureMappTimeToleranceTF = new JTextField();
        DoubleParameter alignmentFeatureMappingTimeToleranceParameter = new DoubleParameter("featureMappingTimeTolerance", "Feature Mapping Time Tolerance", m_alignmentFeatureMappTimeToleranceTF, DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE, new Double(1), null);
        m_parameterList.add(alignmentFeatureMappingTimeToleranceParameter);

        m_featureFilterNameCB = new JComboBox(FEATURE_FILTER_NAME_VALUES);
        m_featureFilterNameParameter = new ObjectParameter<>("featureFilterNameParameter ", "Feature Name Parameter", m_featureFilterNameCB, FEATURE_FILTER_NAME_VALUES, FEATURE_FILTER_NAME_KEYS, 0, null);
        m_parameterList.add(m_featureFilterNameParameter);

        m_featureFilterOperatorCB = new JComboBox(FEATURE_FILTER_OPERATOR_VALUES);
        m_featureFilterOperatorParameter = new ObjectParameter<>("featureFilterOperator ", "Feature Filter Operator", m_featureFilterOperatorCB, FEATURE_FILTER_OPERATOR_VALUES, FEATURE_FILTER_OPERATOR_KEYS, 0, null);
        m_parameterList.add(m_featureFilterOperatorParameter);

        m_featureFilterValueTF = new JTextField();
        DoubleParameter m_featureFilterValueParameter = new DoubleParameter("featureFilterValue", "Feature Filter Value", m_featureFilterValueTF, DEFAULT_CA_FILTER_VALUE, null, null);
        m_parameterList.add(m_featureFilterValueParameter);

        m_crossAssignFeatureMappMoZTolTF = new JTextField();
        DoubleParameter featureMappingMoZTolParameter = new DoubleParameter("featureMoZTol", "moz tolerance ", m_crossAssignFeatureMappMoZTolTF, DEFAULT_CA_FEATMAP_MOZTOL_VALUE, new Double(0), null);
        m_parameterList.add(featureMappingMoZTolParameter);

        m_crossAssignFeatureMappRTTolTF = new JTextField();
        DoubleParameter featureMappingTimeTolParameter = new DoubleParameter("featureTimeTol", "RT tolerance", m_crossAssignFeatureMappRTTolTF, DEFAULT_CA_FEATMAP_RTTOL_VALUE, new Double(0), null);
        m_parameterList.add(featureMappingTimeTolParameter);

        m_normalizationCB = new JComboBox(FEATURE_NORMALIZATION_VALUES);
        m_normalizationParameter = new ObjectParameter<>("normalization", "Normalization", m_normalizationCB, FEATURE_NORMALIZATION_VALUES, FEATURE_NORMALIZATION_KEYS, 0, null);
        m_parameterList.add(m_normalizationParameter);

        m_retainOnlyReliableFeatures = new JCheckBox("Use only confident features");
        BooleanParameter retainOnlyReliableFeaturesParameter = new BooleanParameter("restrainCrossAssignmentToReliableFeatures", "Use only confident features", m_retainOnlyReliableFeatures, Boolean.FALSE);
        m_parameterList.add(retainOnlyReliableFeaturesParameter);

    }

    /**
     * set the quanti params. This method assume parameters are formated as V2
     * Params
     *
     * @param quantParams
     */
    public void setQuantParams(Map<String, Object> quantParams) {
        Map<String, Object> extRactParams = (Map<String, Object>) quantParams.get("extraction_params");
        m_extractionMoZTolTF.setText("" + Double.parseDouble(extRactParams.get("moz_tol").toString()));
        if (quantParams.containsKey("detection_params")) {
            Map<String, Object> detectionParams = (Map<String, Object>) quantParams.get("detection_params");
            if (detectionParams.containsKey("psm_matching_params")) {
                m_psmMatchingMoZTolTF.setText("" + Double.parseDouble(((Map<String, Object>) detectionParams.get("psm_matching_params")).get("moz_tol").toString()));
            }
        }
        // 
        //*** Alignment Params
        // 
        if (quantParams.containsKey(AbstractLabelFreeMSParamsPanel.ALIGNMENT_CONFIG)) {
            Map<String, Object> alignmentConfig = (Map<String, Object>) quantParams.get(AbstractLabelFreeMSParamsPanel.ALIGNMENT_CONFIG);
            m_alignmentMethodParameter.setValue((String) alignmentConfig.getOrDefault(AbstractLabelFreeMSParamsPanel.ALIGNMENT_METHOD_NAME, ALIGNMENT_METHOD_VALUES[0]));
            Map<String, Object> alnParams = (Map<String, Object>) alignmentConfig.getOrDefault("aln_params", new HashMap<>());
            if (alnParams.containsKey("max_iterations")) {
                try {
                    m_alignmentMaxIterationTF.setText("" + Integer.parseInt(alnParams.getOrDefault("max_iterations", DEFAULT_ALIGN_MAXITE_VALUE).toString()));
                } catch (NumberFormatException ex) {
                    m_logger.error("error while settings max_iterations quanti params " + ex);
                    m_alignmentMaxIterationTF.setText(DEFAULT_ALIGN_MAXITE_VALUE.toString());
                }
            }

            m_alnIgnoreErrorsParameter.setValue(alignmentConfig.getOrDefault("ignore_errors", false).toString());
            m_alignmentSmoothingMethodParameter.setValue((String) alignmentConfig.getOrDefault("smoothing_method_name", ALIGNMENT_SMOOTHING_METHOD_VALUES[0]));
            if (alignmentConfig.containsKey("smoothing_params")) {
                Map<String, Object> smootingParams = (Map<String, Object>) alignmentConfig.get("smoothing_method_params");
                try {
                    m_alignmentSmoothWinSizeTF.setText("" + Integer.parseInt(smootingParams.getOrDefault("window_size", DEFAULT_SMOOTH_WINSIZE_VALUE).toString()));
                    m_alignmentSmoothWinOverlapTF.setText("" + Integer.parseInt(smootingParams.getOrDefault("window_overlap", DEFAULT_SMOOTH_WINOVERLAP_VALUE).toString()));
                    m_alignmentSmoothMinWinLMTF.setText("" + Integer.parseInt(smootingParams.getOrDefault("min_window_landmarks", DEFAULT_SMOOTH_NBRLM_VALUE).toString()));
                } catch (NumberFormatException ex) {
                    m_logger.error("error while settings smoothing_params quanti params " + ex);
                    m_alignmentSmoothWinSizeTF.setText(DEFAULT_SMOOTH_WINSIZE_VALUE.toString());
                    m_alignmentSmoothWinOverlapTF.setText(DEFAULT_SMOOTH_WINOVERLAP_VALUE.toString());
                    m_alignmentSmoothMinWinLMTF.setText(DEFAULT_SMOOTH_NBRLM_VALUE.toString());
                }
            }
            m_alignmentFeatureMappMethodParameter.setValue((String) alignmentConfig.getOrDefault("ft_mapping_method_name", FEATURE_MAPPING_METHOD_KEYS[0]));
            Map<String, Object> alnFtParams = (Map<String, Object>) alignmentConfig.get("ft_mapping_method_params");
            try {
                if (alnFtParams.containsKey("moz_tol")) {
                    m_alignmentFeatureMappMoZTolTF.setText("" + Double.parseDouble(alnFtParams.getOrDefault("moz_tol", DEFAULT_ALIGN_FEATMAP_MOZTOL_VALUE).toString()));
                    //enableAlignmentFtMapping(true);
                } else {
                    //enableAlignmentFtMapping(false);
                }
                m_alignmentFeatureMappTimeToleranceTF.setText("" + Double.parseDouble(alnFtParams.getOrDefault("time_tol", DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE).toString()));
            } catch (NumberFormatException ex) {
                m_logger.error("error while settings ft_mapping_params quanti params " + ex);
                m_alignmentFeatureMappMoZTolTF.setText(DEFAULT_ALIGN_FEATMAP_MOZTOL_VALUE.toString());
                m_alignmentFeatureMappTimeToleranceTF.setText(DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE.toString());
            }
            m_alignmentSettingsCBoxTitle.setSelected(true);
            updateAlignment();
        } else {
            m_alignmentSettingsCBoxTitle.setSelected(false);
            updateAlignment();
        }

        //
        //*** Cross Assignment      
        //
        if (quantParams.containsKey("cross_assignment_config")) {
            Map<String, Object> crossAssignmentConfig = (Map<String, Object>) quantParams.get("cross_assignment_config");
            m_crossAssignStrategyParameter.setValue((String) crossAssignmentConfig.getOrDefault("method_name", CROSSASSIGN_STRATEGY_VALUES[0]));

            Map<String, Object> ftMappingParams = (Map<String, Object>) crossAssignmentConfig.getOrDefault("ft_mapping_params", new HashMap<>());
            try {
                m_crossAssignFeatureMappMoZTolTF.setText("" + Double.parseDouble(ftMappingParams.getOrDefault("moz_tol", DEFAULT_CA_FEATMAP_MOZTOL_VALUE).toString()));
                m_crossAssignFeatureMappRTTolTF.setText("" + Double.parseDouble(ftMappingParams.getOrDefault("time_tol", DEFAULT_CA_FEATMAP_RTTOL_VALUE).toString()));
            } catch (NumberFormatException ex) {
                m_logger.error("error while settings CA ft_mapping_params quanti params " + ex);
                m_crossAssignFeatureMappMoZTolTF.setText(DEFAULT_CA_FEATMAP_MOZTOL_VALUE.toString());
                m_crossAssignFeatureMappRTTolTF.setText(DEFAULT_CA_FEATMAP_RTTOL_VALUE.toString());
            }
            if (crossAssignmentConfig.containsKey("ft_filter")) {
                m_featureFilterCBTitle.setSelected(true);
                Map<String, Object> ftParams = (Map<String, Object>) crossAssignmentConfig.get("ft_filter");
                m_featureFilterNameParameter.setValue((String) ftParams.getOrDefault("name", FEATURE_FILTER_NAME_VALUES[0]));
                m_featureFilterOperatorParameter.setValue((String) ftParams.getOrDefault("operator", FEATURE_FILTER_OPERATOR_VALUES[0]));
                try {
                    m_featureFilterValueTF.setText("" + Double.parseDouble(ftParams.getOrDefault("value", DEFAULT_CA_FILTER_VALUE).toString()));
                } catch (NumberFormatException | NullPointerException ex) {
                    m_logger.error("error while settings ft_filter quanti params " + ex);
                    m_featureFilterValueTF.setText(DEFAULT_CA_FILTER_VALUE.toString());
                }
            } else {
                m_featureFilterCBTitle.setSelected(false);
            }
            m_retainOnlyReliableFeatures.setSelected(Boolean.parseBoolean(crossAssignmentConfig.getOrDefault("restrain_to_reliable_features", true).toString()));
            m_crossAssignCBoxTitle.setSelected(true);
            updateCrossAssignment();
        } else {
            m_crossAssignCBoxTitle.setSelected(false);
            updateCrossAssignment();
        }

        m_useLastPeakelDetectionParam.setValue(quantParams.getOrDefault("use_last_peakel_detection", false).toString());

        //
        //*** Clustering Params
        //
        Map<String, Object> clusterParams = (Map<String, Object>) quantParams.get("clustering_params");
        try {
            m_clusteringMoZTolTF.setText("" + Double.parseDouble(clusterParams.getOrDefault("moz_tol", DEFAULT_CLUSTER_MOZTOL_VALUE).toString()));
            m_clusteringTimeTolTF.setText("" + Double.parseDouble(clusterParams.getOrDefault("time_tol", DEFAULT_CLUSTER_TIMETOL_VALUE).toString()));
        } catch (NumberFormatException | NullPointerException ex) {
            m_logger.error("error while settings clustering_params quanti params " + ex);
            m_clusteringMoZTolTF.setText(DEFAULT_CLUSTER_MOZTOL_VALUE.toString());
            m_clusteringTimeTolTF.setText(DEFAULT_CLUSTER_TIMETOL_VALUE.toString());
        }
        m_clusteringIntensityComputationParameter.setValue((String) clusterParams.getOrDefault("intensity_computation", CLUSTERING_INTENSITY_COMPUTATION_VALUES[0]));
        m_clusteringTimeComputationParameter.setValue((String) clusterParams.getOrDefault("time_computation", CLUSTERING_TIME_COMPUTATION_VALUES[0]));

        //
        //*** Normalization Params
        //
        if (quantParams.containsKey("normalization_method") && quantParams.get("normalization_method")!=null) {
            m_normalizationSettingsCBTitle.setSelected(true);
            updateNormalizationSettings();
            m_normalizationParameter.setValue((String) quantParams.getOrDefault("normalization_method", FEATURE_NORMALIZATION_VALUES[0]));
        } else {
            m_normalizationSettingsCBTitle.setSelected(false);
            updateNormalizationSettings();
        }
        updateAlignmentFeatureMapping();
        updateAlignmentMethod();
        updateAlignmentSmoothing();
    }

    @Override
    public Map<String, Object> getQuantParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("config_version", "2.0");

        Map<String, Object> extractionParams = new HashMap<>();
        extractionParams.put("moz_tol", m_extractionMoZTolTF.getText());
        extractionParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
        params.put("extraction_params", extractionParams);

        params.put("use_last_peakel_detection", m_useLastPeakelDetectionParam.getStringValue());

        Map<String, Object> detectionParams = new HashMap<>();
        Map<String, Object> detectionToleranceParams = new HashMap<>();
        detectionToleranceParams.put("moz_tol", m_psmMatchingMoZTolTF.getText());
        detectionToleranceParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
        detectionParams.put("psm_matching_params", detectionToleranceParams);
        detectionParams.put("isotope_matching_params", detectionToleranceParams);

        params.put("detection_params", detectionParams);

        Map<String, Object> clusteringParams = new HashMap<>();
        clusteringParams.put("moz_tol", m_clusteringMoZTolTF.getText());
        clusteringParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
        clusteringParams.put("time_tol", m_clusteringTimeTolTF.getText());
        clusteringParams.put("time_computation", m_clusteringTimeComputationParameter.getStringValue());
        clusteringParams.put("intensity_computation", m_clusteringIntensityComputationParameter.getStringValue());
        params.put("clustering_params", clusteringParams);

        if (m_alignmentSettingsCBoxTitle.isSelected()) {
            Map<String, Object> alignmentConfig = new HashMap<>();
            alignmentConfig.put("method_name", m_alignmentMethodParameter.getStringValue());
            Map<String, Object> alignmentMethodParams = new HashMap<>();
            if (m_alignmentMethodCB.getSelectedItem().equals(ALIGNMENT_METHOD_VALUES[1])) { //param needed
                alignmentMethodParams.put("mass_interval", DEFAULT_ALIGN_MASSINTERVAL_VALUE);
                alignmentMethodParams.put("max_iterations", m_alignmentMaxIterationTF.getText());
            }
            alignmentConfig.put("method_params", alignmentMethodParams);
            alignmentConfig.put("smoothing_method_name", m_alignmentSmoothingMethodParameter.getStringValue());
            if (!m_alignmentSmoothingMethodCB.getSelectedItem().equals(ALIGNMENT_SMOOTHING_METHOD_VALUES[0])) { //param needed
                Map<String, Object> smootingParams = new HashMap<>();
                smootingParams.put("window_size", m_alignmentSmoothWinSizeTF.getText());
                smootingParams.put("window_overlap", m_alignmentSmoothWinOverlapTF.getText());
                if (m_alignmentSmoothingMethodCB.getSelectedItem().equals(ALIGNMENT_SMOOTHING_METHOD_VALUES[2])) {
                    smootingParams.put("min_window_landmarks", m_alignmentSmoothMinWinLMTF.getText());
                }
                alignmentConfig.put("smoothing_method_params", smootingParams);
            }
            alignmentConfig.put("ft_mapping_method_name", m_alignmentFeatureMappMethodParameter.getStringValue());
            Map<String, Object> alnFtParams = new HashMap<>();
            alnFtParams.put("time_tol", m_alignmentFeatureMappTimeToleranceTF.getText());
            if (m_alignmentFeatureMappMethodCB.getSelectedItem().equals(FEATURE_MAPPING_METHOD_VALUES[1])) { //param needed                
                alnFtParams.put("moz_tol", m_alignmentFeatureMappMoZTolTF.getText());
                alnFtParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
            }
            alignmentConfig.put("ft_mapping_method_params", alnFtParams);
            alignmentConfig.put("ignore_errors", m_alnIgnoreErrorsParameter.getStringValue());
            params.put(ALIGNMENT_CONFIG, alignmentConfig);
        } //End if alignment selected 

        boolean crossAssignmentEnabled = m_crossAssignCBoxTitle.isSelected();
        if (crossAssignmentEnabled) {
            Map<String, Object> crossAssignmentConfig = new HashMap<>();
            crossAssignmentConfig.put(ALIGNMENT_METHOD_NAME, m_crossAssignStrategyParameter.getStringValue());
            crossAssignmentConfig.put("restrain_to_reliable_features", m_retainOnlyReliableFeatures.isSelected());
            Map<String, Object> ftMappingParams = new HashMap<>();
            ftMappingParams.put("moz_tol", m_crossAssignFeatureMappMoZTolTF.getText());
            ftMappingParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
            ftMappingParams.put("time_tol", m_crossAssignFeatureMappRTTolTF.getText());
            crossAssignmentConfig.put("ft_mapping_params", ftMappingParams);

            if (m_featureFilterCBTitle.isSelected()) {
                Map<String, Object> ftParams = new HashMap<>();
                ftParams.put("name", m_featureFilterNameParameter.getStringValue());
                ftParams.put("operator", m_featureFilterOperatorParameter.getStringValue());
                ftParams.put("value", m_featureFilterValueTF.getText());
                crossAssignmentConfig.put("ft_filter", ftParams);
            }
            params.put("cross_assignment_config", crossAssignmentConfig);
        }

        // normalization parameter is an option
        if (m_normalizationSettingsCBTitle.isSelected()) {
            params.put("normalization_method", m_normalizationParameter.getStringValue());
        }

        params.put("detection_method_name", DETECTION_METHOD_KEYS[0]);

        return params;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // tabbed pane
        m_tabbedPane = new JTabbedPane();
        m_tabbedPane.addTab("Detection", null, createDetectionPanel(), "Detection");
        m_tabbedPane.addTab("Clustering", null, createClusteringPanel(), "Feature Clustering");
        m_tabbedPane.addTab("Alignment", null, createAlignmentPanel(), "Map Alignment");
        m_tabbedPane.addTab("Normalization", null, createNormalizationPanel(), "Map Normalization");
        m_tabbedPane.addTab("Cross Assignment", null, createCrossAssignmentPanel(), "Cross Assignment");

        mainPanel.add(m_tabbedPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createDetectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel detectionPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel extractionMoZTolLabel = new JLabel("Extraction moz tolerance (ppm):");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        detectionPanel.add(extractionMoZTolLabel, c);

        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        detectionPanel.add(m_extractionMoZTolTF, c);
        c.weightx = 0;

        JLabel psmMatchingMoZTolLabel = new JLabel("PSM/Peakel matching moz tolerance (ppm):");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        detectionPanel.add(psmMatchingMoZTolLabel, c);

        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        detectionPanel.add(m_psmMatchingMoZTolTF, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        detectionPanel.add(m_useLastPeakelDetectionParam.getComponent(), c);
        panel.add(detectionPanel, BorderLayout.NORTH);
        setEnabled(panel, !m_readOnly);
        return panel;
    }

    private JPanel createNormalizationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        m_normalizationSettingsPanel = new JPanel(new GridBagLayout());
        m_normalizationSettingsCBTitle = new CheckBoxTitledBorder("Map Normalization", false);
        m_normalizationSettingsCBTitle.setEnabled(!m_readOnly);
        m_normalizationSettingsCBTitle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEnabled(m_normalizationSettingsPanel, m_normalizationSettingsCBTitle.isSelected());
            }
        });

        panel.setBorder(m_normalizationSettingsCBTitle);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        m_normalizationLabel = new JLabel("Map intensities Normalization ");
        m_normalizationSettingsPanel.add(m_normalizationLabel, c);
        c.gridx++;
        c.weighty = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        m_normalizationSettingsPanel.add(m_normalizationCB, c);
        panel.add(m_normalizationSettingsPanel, BorderLayout.NORTH);
        setEnabled(panel, false);
        return panel;
    }

    private JPanel createClusteringPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel clusteringPanel = new JPanel(new GridBagLayout());
        JPanel clusteringFeaturePanel = new JPanel(new GridBagLayout());
        clusteringFeaturePanel.setBorder(createTitledBorder(" Feature Clusterization rules ", 1));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        clusteringFeaturePanel.add(new JLabel("delta moz (ppm) <= "), c);
        c.gridx++;
        c.weightx = 1;
        clusteringFeaturePanel.add(m_clusteringMoZTolTF, c);
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        clusteringFeaturePanel.add(new JLabel("delta RT (s) <= "), c);
        c.gridx++;
        c.weightx = 1;
        clusteringFeaturePanel.add(m_clusteringTimeTolTF, c);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        clusteringPanel.add(clusteringFeaturePanel, gbc);

        JPanel summarizingPanel = new JPanel(new GridBagLayout());
        summarizingPanel.setBorder(createTitledBorder(" Clustered features summarization", 1));

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        summarizingPanel.add(new JLabel("time computation:"), c);
        c.gridx++;
        c.weightx = 1;
        summarizingPanel.add(m_clusteringTimeComputationCB, c);
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        summarizingPanel.add(new JLabel("intensity computation:"), c);
        c.gridx++;
        c.weightx = 1;
        summarizingPanel.add(m_clusteringIntensityComputationCB, c);
        c.weightx = 0;

        gbc.gridy++;
        clusteringPanel.add(summarizingPanel, gbc);
        panel.add(clusteringPanel, BorderLayout.NORTH);
        setEnabled(panel, !m_readOnly);
        return panel;
    }

    private JPanel createAlignmentPanel() {
        ActionListener assActionListener = new AssignementActionListener();
        JPanel panel = new JPanel(new BorderLayout());
        m_alignmentSettingsCBoxTitle = new CheckBoxTitledBorder("Map Alignment", true);
        m_alignmentSettingsCBoxTitle.setEnabled(!m_readOnly);
        m_alignmentSettingsCBoxTitle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAlignment();
            }
        });
        panel.setBorder(m_alignmentSettingsCBoxTitle);

        m_alignementSettingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        m_algnmentMethodLabel = new JLabel("method:");
        m_alignementSettingsPanel.add(m_algnmentMethodLabel, c);
        c.gridx++;
        c.weightx = 1;
        m_alignmentMethodCB.addActionListener(assActionListener);
        m_alignementSettingsPanel.add(m_alignmentMethodCB, c);
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        m_alignmentMaxIteLabel = new JLabel("max iteration:");
        m_alignementSettingsPanel.add(m_alignmentMaxIteLabel, c);
        c.gridx++;
        c.weightx = 1;
        m_alignementSettingsPanel.add(m_alignmentMaxIterationTF, c);
        c.weightx = 0;

        // Add Alignement Smoothing specific params
        JPanel smootingPanel = new JPanel(new GridBagLayout());
        smootingPanel.setBorder(createTitledBorder(" Smoothing ", 1));
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;
        m_algnmentSmoothMethodLabel = new JLabel("method:");
        smootingPanel.add(m_algnmentSmoothMethodLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_alignmentSmoothingMethodCB.addActionListener(assActionListener);
        smootingPanel.add(m_alignmentSmoothingMethodCB, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentSmoothTimeIntervalLabel = new JLabel("windows size");
        smootingPanel.add(m_alignmentSmoothTimeIntervalLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothWinSizeTF, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentSmoothWinOverlapLabel = new JLabel("sliding window overlap (%):");
        smootingPanel.add(m_alignmentSmoothWinOverlapLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothWinOverlapTF, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentSmoothNbrLMLabel = new JLabel("minimum number of landmarks :");
        smootingPanel.add(m_alignmentSmoothNbrLMLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothMinWinLMTF, c1);
        c1.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        m_alignementSettingsPanel.add(smootingPanel, c);

        // Add Alignement  FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 1));
        c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.FIRST_LINE_START;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;
        m_algnmentFeatureMapMethodLabel = new JLabel("method:");
        ftParamsPanel.add(m_algnmentFeatureMapMethodLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_alignmentFeatureMappMethodCB.addActionListener(assActionListener);
        ftParamsPanel.add(m_alignmentFeatureMappMethodCB, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentFeatureMoZTolLabel = new JLabel("moz tolerance (ppm):");
        ftParamsPanel.add(m_alignmentFeatureMoZTolLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        ftParamsPanel.add(m_alignmentFeatureMappMoZTolTF, c1);
        c1.weightx = 0;

        c1.gridx = 0;
        c1.gridy++;
        m_alignmentFeatureTimeTolLabel = new JLabel("time tolerance (s):");
        ftParamsPanel.add(m_alignmentFeatureTimeTolLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        ftParamsPanel.add(m_alignmentFeatureMappTimeToleranceTF, c1);
        c1.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        m_alignementSettingsPanel.add(ftParamsPanel, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        m_alignementSettingsPanel.add(m_alnIgnoreErrorsParameter.getComponent(null), c);

        panel.add(m_alignementSettingsPanel, BorderLayout.NORTH);
        updateAlignmentFeatureMapping();
        updateAlignmentMethod();
        updateAlignmentSmoothing();

        setEnabled(panel, !m_readOnly);
        return panel;
    }

    private JPanel createCrossAssignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        m_crossAssignCBoxTitle = new CheckBoxTitledBorder("Cross Assignment", true);
        m_crossAssignCBoxTitle.setEnabled(!m_readOnly);
        m_crossAssignCBoxTitle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCrossAssignment();
            }
        });
        panel.setBorder(m_crossAssignCBoxTitle);

        m_crossAssignSettingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cm = new GridBagConstraints();
        cm.anchor = GridBagConstraints.FIRST_LINE_START;
        cm.fill = GridBagConstraints.BOTH;
        cm.insets = new java.awt.Insets(5, 5, 5, 5);

        cm.gridx = 0;
        cm.gridy = 0;
        cm.weightx = 0;
        m_allowCrossAssignLabel = new JLabel("Allow cross assignment ");
        m_crossAssignSettingsPanel.add(m_allowCrossAssignLabel, cm);
        cm.gridx++;
        cm.weightx = 1;
        m_crossAssignSettingsPanel.add(m_crossAssignStrategyCB, cm);

        JPanel ftPanel = new JPanel(new GridBagLayout());
        ftPanel.setBorder(createTitledBorder(" Filtering ", 1));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        ftPanel.add(m_retainOnlyReliableFeatures, c);

        m_featureFilterPanel = new JPanel(new GridBagLayout());
        m_featureFilterCBTitle = new CheckBoxTitledBorder("Intensity Filtering", true);
        m_featureFilterCBTitle.setEnabled(!m_readOnly);
        m_featureFilterCBTitle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCrossAssignmentSettings();
            }
        });
        m_featureFilterPanel.setBorder(m_featureFilterCBTitle);

        m_featureFilterNameLabel = new JLabel("intensity:");
        m_featureFilterOperatorLabel = new JLabel("operator:");
        m_featureFilterValueLabel = new JLabel("value:");
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;
        c1.weightx = 0;
        m_featureFilterPanel.add(m_featureFilterNameLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_featureFilterPanel.add(m_featureFilterNameCB, c1);

        c1.gridy++;
        c1.gridx = 0;
        c1.weightx = 0;
        m_featureFilterPanel.add(m_featureFilterOperatorLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_featureFilterPanel.add(m_featureFilterOperatorCB, c1);

        c1.gridy++;
        c1.gridx = 0;
        c1.weightx = 0;
        m_featureFilterPanel.add(m_featureFilterValueLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_featureFilterPanel.add(m_featureFilterValueTF, c1);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        ftPanel.add(m_featureFilterPanel, c);

        cm.gridy++;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;
        m_crossAssignSettingsPanel.add(ftPanel, cm);

        // Add FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 1));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        m_crossAssignFeatureMappMoZTolLabel = new JLabel("moz tolerance (ppm):");
        c.gridx = 0;
        c.gridy = 0;
        ftParamsPanel.add(m_crossAssignFeatureMappMoZTolLabel, c);
        c.gridx++;
        c.weightx = 1;
        ftParamsPanel.add(m_crossAssignFeatureMappMoZTolTF, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        m_crossAssignFeatureMappRTTolLabel = new JLabel("RT tolerance (s):");
        ftParamsPanel.add(m_crossAssignFeatureMappRTTolLabel, c);
        c.gridx++;
        c.weightx = 1;
        ftParamsPanel.add(m_crossAssignFeatureMappRTTolTF, c);
        c.weightx = 0;

        cm.gridy++;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;
        m_crossAssignSettingsPanel.add(ftParamsPanel, cm);
        panel.add(m_crossAssignSettingsPanel, BorderLayout.NORTH);

        setEnabled(panel, !m_readOnly);
        return panel;
    }

    private void updateCrossAssignment() {
        setEnabled(m_crossAssignSettingsPanel, !m_readOnly && m_crossAssignCBoxTitle.isSelected());
        updateCrossAssignmentSettings();
        if (m_readOnly) {
            m_crossAssignSettingsPanel.setVisible(m_crossAssignCBoxTitle.isSelected());
        }
    }

    private void updateCrossAssignmentSettings() {
        boolean filterEnabled = m_crossAssignCBoxTitle.isSelected() && m_featureFilterCBTitle.isSelected();
        m_featureFilterNameCB.setEnabled(!m_readOnly && filterEnabled);
        m_featureFilterNameLabel.setEnabled(!m_readOnly && filterEnabled);
        m_featureFilterOperatorLabel.setEnabled(!m_readOnly && filterEnabled);
        m_featureFilterValueLabel.setEnabled(!m_readOnly && filterEnabled);
        m_featureFilterOperatorCB.setEnabled(!m_readOnly && filterEnabled);
        m_featureFilterValueTF.setEnabled(!m_readOnly && filterEnabled);

        if (m_readOnly) {
            m_featureFilterNameCB.setVisible(filterEnabled);
            m_featureFilterNameLabel.setVisible(filterEnabled);
            m_featureFilterOperatorLabel.setVisible(filterEnabled);
            m_featureFilterValueLabel.setVisible(filterEnabled);
            m_featureFilterOperatorCB.setVisible(filterEnabled);
            m_featureFilterValueTF.setVisible(filterEnabled);
        }
    }

    private void updateAlignment() {
        setEnabled(m_alignementSettingsPanel, !m_readOnly && m_alignmentSettingsCBoxTitle.isSelected());
        updateAlignmentSettings();
        if (m_readOnly) {
            m_alignementSettingsPanel.setVisible(m_alignmentSettingsCBoxTitle.isSelected());
        }
    }

    private void updateNormalizationSettings() {
        boolean isEnabled = m_normalizationSettingsCBTitle.isSelected();
        m_normalizationCB.setEnabled(!m_readOnly && isEnabled);
        m_normalizationLabel.setEnabled(!m_readOnly && isEnabled);

        if (m_readOnly) {
            m_normalizationCB.setVisible(isEnabled);
            m_normalizationLabel.setVisible(isEnabled);
        }
    }

    private void updateAlignmentSettings() {
        updateAlignmentMethod();
        updateAlignmentSmoothing();
        updateAlignmentFeatureMapping();
    }

    private void updateAlignmentMethod() {
        boolean isIterative = m_alignmentMethodCB.getSelectedItem().equals(ALIGNMENT_METHOD_VALUES[1]);
        m_alignmentMaxIteLabel.setVisible(isIterative);
        m_alignmentMaxIterationTF.setVisible(isIterative);
    }

    private void updateAlignmentSmoothing() {
        boolean isSettingNeeded = !m_alignmentSmoothingMethodCB.getSelectedItem().equals(ALIGNMENT_SMOOTHING_METHOD_VALUES[0]);
        boolean isNbrLMNeeded = m_alignmentSmoothingMethodCB.getSelectedItem().equals(ALIGNMENT_SMOOTHING_METHOD_VALUES[2]);

        m_alignmentSmoothWinSizeTF.setVisible(isSettingNeeded);
        m_alignmentSmoothTimeIntervalLabel.setVisible(isSettingNeeded);
        m_alignmentSmoothWinOverlapTF.setVisible(isSettingNeeded);
        m_alignmentSmoothWinOverlapLabel.setVisible(isSettingNeeded);

        m_alignmentSmoothNbrLMLabel.setVisible(isNbrLMNeeded);
        m_alignmentSmoothMinWinLMTF.setVisible(isNbrLMNeeded);
    }

    private void updateAlignmentFeatureMapping() {
        boolean isSettingNeeded = m_alignmentFeatureMappMethodCB.getSelectedItem().equals(FEATURE_MAPPING_METHOD_VALUES[1]);
        m_alignmentFeatureMoZTolLabel.setVisible(isSettingNeeded);
        m_alignmentFeatureMappMoZTolTF.setVisible(isSettingNeeded);
    }

    private static void setEnabled(Container container, boolean isEnabled) {
        container.setEnabled(isEnabled);
        for (Component children : container.getComponents()) {
            if (Container.class.isAssignableFrom(children.getClass())) {
                setEnabled((Container) children, isEnabled);
            } else {
                children.setEnabled(isEnabled);
            }
        }
    }

    class AssignementActionListener implements ActionListener {
       
        @Override
        public void actionPerformed(ActionEvent e) {
            updateAlignmentSettings();
        }

    }
}
