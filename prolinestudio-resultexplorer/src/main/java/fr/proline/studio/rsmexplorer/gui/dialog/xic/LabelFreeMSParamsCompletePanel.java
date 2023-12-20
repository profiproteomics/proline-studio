/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.gui.CheckBoxTitledBorder;
import fr.proline.studio.parameter.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import fr.proline.studio.NbPreferences;
import fr.proline.studio.WindowManager;

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
    private ObjectParameter<String> m_mozCalibSmoothingMethodParameter;

    private BooleanParameter m_useLastPeakelDetectionParam = new BooleanParameter("useLastPeakelDetection", "Use last peakel detection", JCheckBox.class, Boolean.FALSE);
    private BooleanParameter m_alnIgnoreErrorsParameter = new BooleanParameter("ignoreErrors", "Ignore Alignment Errors", JCheckBox.class, Boolean.FALSE);
    private BooleanParameter m_crossAssignUseMozCalibrationParameter = new BooleanParameter("useMozCalibration", "Use Moz Calibration", JCheckBox.class, Boolean.TRUE);
    private BooleanParameter m_crossAssignUseAutoTimeTolParameter = new BooleanParameter("useAutomaticTimeTol", "Use Automatic RT Tolerance", JCheckBox.class, Boolean.FALSE);

    private JTextField m_extractionMoZTolTF;
    private JTextField m_psmMatchingMoZTolTF;

    //RT ALIGNMENT PARAMs
    private JPanel m_alignmentSettingsPanel;
    private CheckBoxTitledBorder m_alignmentSettingsCBoxTitle;

    private JComboBox m_alignmentMethodCB;
    private JLabel m_alignmentMaxIteLabel;
    private JLabel m_alignmentFeatureMoZTolLabel;
    private JTextField m_alignmentMaxIterationTF;
    private JTextField m_alignmentFeatureMapMoZTolTF;
    private JTextField m_alignmentFeatureMapTimeToleranceTF;

    private JComboBox m_alignmentSmoothingMethodCB;
    private JLabel m_alignmentSmoothTimeIntervalLabel;
    private JLabel m_alignmentSmoothWinOverlapLabel;
    private JLabel m_alignmentSmoothNbrLMLabel;
    private JTextField m_alignmentSmoothWinSizeTF;
    private JTextField m_alignmentSmoothMinWinLMTF;
    private JTextField m_alignmentSmoothWinOverlapTF;
    private JComboBox m_alignmentFeatureMapMethodCB;

    //MOZ CALIBRATION PARAMs
    private JComboBox m_mozCalibSmoothingMethodCB;
    private JTextField m_mozCalibSmoothWinSizeTF;
    private JTextField m_mozCalibSmoothMinWinLMTF;
    private JTextField m_mozCalibSmoothWinOverlapTF;
    private JLabel m_mozCalibSmoothTimeIntervalLabel;
    private JLabel m_mozCalibSmoothWinOverlapLabel;
    private JLabel m_mozCalibSmoothNbrLMLabel;

    //CROSS ASSIGNMENT PARAMs
    private JPanel m_crossAssignSettingsPanel;
    private JTextField m_crossAssignFeatureMappMoZTolTF;
    private JTextField m_crossAssignFeatureMappRTTolTF;
    private JTextField m_crossAssignFeatureMappMaxAutoRTTolTF;
    private JTextField m_crossAssignFeatureMappMinAutoRTTolTF;
    private JCheckBox m_crossAssignFeatureMappUseAutoRTTolCB;
    private JRadioButton m_crossAssignFeatureMappBestFeatMozRB;
    private JRadioButton m_crossAssignFeatureMappCalibMozRB;
    private ButtonGroup m_crossAssignFeatureMappMozUsedBG;
    private JLabel m_crossAssignFeatureMappAutoRTMinTolLabel;
    private JLabel m_crossAssignFeatureMappAutoRTMaxTolLabel;
    private JLabel m_crossAssignFeatureMappRTTolLabel;


    private CheckBoxTitledBorder m_crossAssignCBoxTitle;
    private JComboBox m_crossAssignStrategyCB;

    private JLabel m_crossAssignFeatureMappUsedMozLabel;
    private JLabel m_crossAssignFeatureMappUsedAutoRTLabel;
    private JLabel m_crossAssignFeatureMappMozTolLabel;
    private JLabel m_featureFilterNameLabel;
    private JLabel m_featureFilterOperatorLabel;
    private JLabel m_featureFilterValueLabel;
    private CheckBoxTitledBorder m_featureFilterCBTitle;
    private JComboBox m_featureFilterNameCB;
    private JComboBox m_featureFilterOperatorCB;
    private JTextField m_featureFilterValueTF;
    private JCheckBox m_retainOnlyReliableFeaturesCB;

    //CLUSTERING PARAMs
    private JTextField m_clusteringMoZTolTF;
    private JTextField m_clusteringTimeTolTF;
    private JComboBox m_clusteringTimeComputationCB;
    private JComboBox m_clusteringIntensityComputationCB;

    //NORMALIZATION PARAMs
    private JPanel m_normalizationSettingsPanel;
    private CheckBoxTitledBorder m_normalizationSettingsCBTitle;
    private JComboBox m_normalizationCB;
    private JLabel m_normalizationLabel;

    public LabelFreeMSParamsCompletePanel(boolean readOnly, boolean readValues, String labelFreeParamVersion) {
        super(readOnly, labelFreeParamVersion);

        createParameters();
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
        
        if (readValues) {
            m_parameterList.updateValues(NbPreferences.root());
        } else {
            m_parameterList.setDefaultValues();
        }
        for (AbstractParameter param : m_parameterList) {
            param.setUsed(true);
        }

    }

    @Override
    public boolean isSimplifiedPanel() {
        return false;
    }

    private void createParameters() {
        m_extractionMoZTolTF = new JTextField();
        DoubleParameter extractionMoZTolParameter = new DoubleParameter("extractionMoZTol", "Extraction moz tolerance", m_extractionMoZTolTF, DEFAULT_EXTRACTION_MOZTOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(extractionMoZTolParameter);

        m_psmMatchingMoZTolTF = new JTextField();
        DoubleParameter psmMatchingMoZTolTFParameter = new DoubleParameter("psmMatchingMoZTolTF", "PSM Matching moz tolerance", m_psmMatchingMoZTolTF, DEFAULT_EXTRACTION_MOZTOL_VALUE, Double.valueOf(0), null);
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

        m_crossAssignCBoxTitle = new CheckBoxTitledBorder("Cross Assignment", DEFAULT_CROSS_ASSIGN_VALUE);
        m_crossAssignCBoxTitle.setEnabled(!m_readOnly);
        m_crossAssignCBoxTitle.addChangeListener(e -> updateCrossAssignment());
        BooleanParameter crossAssignParameter = new BooleanParameter("crossAssignment", "Cross Assignment", m_crossAssignCBoxTitle.getInternalCheckBox(), DEFAULT_CROSS_ASSIGN_VALUE);
        m_parameterList.add(crossAssignParameter);

        m_crossAssignStrategyCB = new JComboBox(CROSSASSIGN_STRATEGY_VALUES);
        m_crossAssignStrategyParameter = new ObjectParameter<>("crossAssignStrategy", "Cross Assignment Strategy", m_crossAssignStrategyCB, CROSSASSIGN_STRATEGY_VALUES, CROSSASSIGN_STRATEGY_KEYS, 0, null);
        m_parameterList.add(m_crossAssignStrategyParameter);

        m_clusteringMoZTolTF = new JTextField();
        DoubleParameter m_clusteringMoZTolParameter = new DoubleParameter("clusteringMoZTol", "Clustering moz tolerance", m_clusteringMoZTolTF, DEFAULT_CLUSTER_MOZTOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(m_clusteringMoZTolParameter);

        m_clusteringTimeTolTF = new JTextField();
        DoubleParameter m_clusteringTimeTolParameter = new DoubleParameter("clusteringTimeTol", "Clustering time tolerance", m_clusteringTimeTolTF, DEFAULT_CLUSTER_TIMETOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(m_clusteringTimeTolParameter);

        m_clusteringTimeComputationCB = new JComboBox(CLUSTERING_TIME_COMPUTATION_VALUES);
        m_clusteringTimeComputationParameter = new ObjectParameter<>("clusteringTimeComputation", "Clustering time computation", m_clusteringTimeComputationCB, CLUSTERING_TIME_COMPUTATION_VALUES, CLUSTERING_TIME_COMPUTATION_KEYS, 0, null);
        m_parameterList.add(m_clusteringTimeComputationParameter);

        m_clusteringIntensityComputationCB = new JComboBox(CLUSTERING_INTENSITY_COMPUTATION_VALUES);
        m_clusteringIntensityComputationParameter = new ObjectParameter<>("clusteringIntensityComputation", "Clustering intensity computation", m_clusteringIntensityComputationCB, CLUSTERING_INTENSITY_COMPUTATION_VALUES, CLUSTERING_INTENSITY_COMPUTATION_KEYS, 0, null);
        m_parameterList.add(m_clusteringIntensityComputationParameter);

        m_alignmentSettingsCBoxTitle = new CheckBoxTitledBorder("Map Alignment", true);
        m_alignmentSettingsCBoxTitle.setEnabled(!m_readOnly);
        m_alignmentSettingsCBoxTitle.addChangeListener(e -> updateAlignment());
        BooleanParameter alignRTParameter = new BooleanParameter("alignRT", "Align RT", m_alignmentSettingsCBoxTitle.getInternalCheckBox(), DEFAULT_ALIGN_VALUE);
        m_parameterList.add(alignRTParameter);


        m_alignmentMethodCB = new JComboBox(ALIGNMENT_METHOD_VALUES);
        m_alignmentMethodParameter = new ObjectParameter<>("alignmentMethod", "Alignment Method", m_alignmentMethodCB, ALIGNMENT_METHOD_VALUES, ALIGNMENT_METHOD_KEYS, 0, null);
        m_parameterList.add(m_alignmentMethodParameter);

        m_alignmentMaxIterationTF = new JTextField();
        IntegerParameter alignmentMaxIterationParameter = new IntegerParameter("alignmentMaxIteration", "Alignment max iteration", m_alignmentMaxIterationTF, DEFAULT_ALIGN_MAXITE_VALUE, Integer.valueOf(1), null);
        m_parameterList.add(alignmentMaxIterationParameter);

        m_alignmentSmoothingMethodCB = new JComboBox(ALIGNMENT_SMOOTHING_METHOD_VALUES);
        m_alignmentSmoothingMethodParameter = new ObjectParameter("alignmentSmoothingMethod", "Alignment Smoothing Method", m_alignmentSmoothingMethodCB, ALIGNMENT_SMOOTHING_METHOD_VALUES, ALIGNMENT_SMOOTHING_METHOD_KEYS, 0, null);
        m_parameterList.add(m_alignmentSmoothingMethodParameter);

        m_alignmentSmoothWinSizeTF = new JTextField();
        IntegerParameter alignmentSmoothingWinSizeParameter = new IntegerParameter("alignmentTimeInterval", "Alignment time interval", m_alignmentSmoothWinSizeTF, DEFAULT_SMOOTH_WINSIZE_VALUE, Integer.valueOf(1), null);
        m_parameterList.add(alignmentSmoothingWinSizeParameter);

        m_alignmentSmoothWinOverlapTF = new JTextField();
        IntegerParameter alignmentSmoothingWinOverlapParameter = new IntegerParameter("smoothingSlidingWindowOverlap", "Smoothing sliding window overlap", m_alignmentSmoothWinOverlapTF, DEFAULT_SMOOTH_WINOVERLAP_VALUE, Integer.valueOf(1), DEFAULT_SMOOTH_WINOVERLAP_MAX_VALUE);
        m_parameterList.add(alignmentSmoothingWinOverlapParameter);

        m_alignmentSmoothMinWinLMTF = new JTextField();
        IntegerParameter alignmentSmoothingMinWinlandmarksParameter = new IntegerParameter("smoothingMinimumNumberOfLandmarks", "Smoothing minimum number of landmarks", m_alignmentSmoothMinWinLMTF, DEFAULT_SMOOTH_NBRLM_VALUE, Integer.valueOf(1), null);
        m_parameterList.add(alignmentSmoothingMinWinlandmarksParameter);

        m_alignmentFeatureMapMoZTolTF = new JTextField();
        DoubleParameter alignmentFeatureMappingMoZTolParameter = new DoubleParameter("featureMappingMozTolerance", "Feature Mapping Moz Tolerance", m_alignmentFeatureMapMoZTolTF, DEFAULT_ALIGN_FEATMAP_MOZTOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(alignmentFeatureMappingMoZTolParameter);

        m_alignmentFeatureMapMethodCB = new JComboBox(FEATURE_MAPPING_METHOD_VALUES);
        m_alignmentFeatureMappMethodParameter = new ObjectParameter("featureMappingMethod", "Feature Mapping Method", m_alignmentFeatureMapMethodCB, FEATURE_MAPPING_METHOD_VALUES, FEATURE_MAPPING_METHOD_KEYS, 0, null);
        m_parameterList.add(m_alignmentFeatureMappMethodParameter);

        m_alignmentFeatureMapTimeToleranceTF = new JTextField();
        DoubleParameter alignmentFeatureMappingTimeToleranceParameter = new DoubleParameter("featureMappingTimeTolerance", "Feature Mapping Time Tolerance", m_alignmentFeatureMapTimeToleranceTF, DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE, Double.valueOf(1), null);
        m_parameterList.add(alignmentFeatureMappingTimeToleranceParameter);


        m_featureFilterCBTitle = new CheckBoxTitledBorder("Intensity Filtering", false);
        m_featureFilterCBTitle.setEnabled(!m_readOnly);
        m_featureFilterCBTitle.addChangeListener(e -> updateCrossAssignmentSettings());
        BooleanParameter featureFilteringParameter = new BooleanParameter("intensityFiltering", "Intensity Filtering", m_featureFilterCBTitle.getInternalCheckBox(), Boolean.FALSE);
        m_parameterList.add(featureFilteringParameter);

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
        DoubleParameter featureMappingMoZTolParameter = new DoubleParameter("featureMoZTol", "moz tolerance ", m_crossAssignFeatureMappMoZTolTF, DEFAULT_CA_FEATMAP_MOZTOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(featureMappingMoZTolParameter);

        m_crossAssignFeatureMappRTTolTF = new JTextField();
        DoubleParameter featureMappingTimeTolParameter = new DoubleParameter("featureTimeTol", "RT tolerance", m_crossAssignFeatureMappRTTolTF, DEFAULT_CA_FEATMAP_RTTOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(featureMappingTimeTolParameter);

        m_crossAssignFeatureMappMinAutoRTTolTF = new JTextField();
        DoubleParameter featureMappingMinAutoRTTolParameter = new DoubleParameter("minAutoRTTol", "min auto RT tolerance ", m_crossAssignFeatureMappMinAutoRTTolTF, DEFAULT_CA_FEATMAP_AUTO_RT_MIN_TOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(featureMappingMinAutoRTTolParameter);

        m_crossAssignFeatureMappMaxAutoRTTolTF = new JTextField();
        DoubleParameter featureMappingMaxAutoRTTolParameter = new DoubleParameter("maxAutoRTTol", "max auto RT tolerance ", m_crossAssignFeatureMappMaxAutoRTTolTF, DEFAULT_CA_FEATMAP_AUTO_RT_MAX_TOL_VALUE, Double.valueOf(0), null);
        m_parameterList.add(featureMappingMaxAutoRTTolParameter);

        m_crossAssignFeatureMappUseAutoRTTolCB = new JCheckBox("Use automatic RT tolerance");
        m_crossAssignUseAutoTimeTolParameter = new BooleanParameter("crossAssignUseAutoRTTol", "Use automatic RT tolerance", m_crossAssignFeatureMappUseAutoRTTolCB, DEFAULT_CA_FEATMAP_USE_AUTO_TIME_TOL);
        m_parameterList.add(m_crossAssignUseAutoTimeTolParameter);

        m_crossAssignFeatureMappCalibMozRB = new JRadioButton("Recalibrated theoretical moz");
        m_crossAssignUseMozCalibrationParameter = new BooleanParameter("crossAssignUseMozCalib", "Use moz calibration", m_crossAssignFeatureMappCalibMozRB, DEFAULT_CA_FEATMAP_USE_MOZ_CALIBRATION);
        m_crossAssignFeatureMappBestFeatMozRB = new JRadioButton("Best feature moz (uncalibrated)");
        m_crossAssignFeatureMappBestFeatMozRB.setSelected(!m_crossAssignFeatureMappCalibMozRB.isSelected());
        m_crossAssignFeatureMappMozUsedBG = new ButtonGroup();
        m_crossAssignFeatureMappMozUsedBG.add(m_crossAssignFeatureMappCalibMozRB);
        m_crossAssignFeatureMappMozUsedBG.add(m_crossAssignFeatureMappBestFeatMozRB);
        m_parameterList.add(m_crossAssignUseMozCalibrationParameter);


        m_normalizationSettingsCBTitle = new CheckBoxTitledBorder("Map Normalization", DEFAULT_NORMALIZATION_VALUE);
        m_normalizationSettingsCBTitle.setEnabled(!m_readOnly);
        m_normalizationSettingsCBTitle.addChangeListener(e -> setEnabled(m_normalizationSettingsPanel, m_normalizationSettingsCBTitle.isSelected()));
        BooleanParameter normalizationParameter = new BooleanParameter("mapNormalization", "Map Normalization", m_normalizationSettingsCBTitle.getInternalCheckBox(), DEFAULT_NORMALIZATION_VALUE);
        m_parameterList.add(normalizationParameter);

        m_normalizationCB = new JComboBox(FEATURE_NORMALIZATION_VALUES);
        m_normalizationParameter = new ObjectParameter<>("normalization", "Normalization", m_normalizationCB, FEATURE_NORMALIZATION_VALUES, FEATURE_NORMALIZATION_KEYS, 0, null);
        m_parameterList.add(m_normalizationParameter);

        m_retainOnlyReliableFeaturesCB = new JCheckBox("Use only confident features");
        BooleanParameter retainOnlyReliableFeaturesParameter = new BooleanParameter("restrainCrossAssignmentToReliableFeatures", "Use only confident features", m_retainOnlyReliableFeaturesCB, Boolean.TRUE);
        m_parameterList.add(retainOnlyReliableFeaturesParameter);

        //MozSmooting
        m_mozCalibSmoothingMethodCB = new JComboBox(MOZ_CALIBRATION_SMOOTHING_METHOD_VALUES);
        m_mozCalibSmoothingMethodParameter = new ObjectParameter("mozCalibSmoothingMethod", "moz Calibration Smoothing Method", m_mozCalibSmoothingMethodCB, MOZ_CALIBRATION_SMOOTHING_METHOD_VALUES, MOZ_CALIBRATION_SMOOTHING_METHOD_KEYS, 0, null);
        m_parameterList.add(m_mozCalibSmoothingMethodParameter);

        m_mozCalibSmoothWinSizeTF = new JTextField();
        IntegerParameter mozCalibSmoothingWinSizeParameter = new IntegerParameter("mozCalibWinSize", "moz Calibration window size", m_mozCalibSmoothWinSizeTF, DEFAULT_SMOOTH_WINSIZE_VALUE, Integer.valueOf(1), null);
        m_parameterList.add(mozCalibSmoothingWinSizeParameter);
        m_parameterList.add(mozCalibSmoothingWinSizeParameter);

        m_mozCalibSmoothWinOverlapTF = new JTextField();
        IntegerParameter mozCalibSmoothingWinOverlapParameter = new IntegerParameter("mozSmoothingSlidingWindowOverlap", "moz Calibration Smoothing sliding window overlap", m_mozCalibSmoothWinOverlapTF, DEFAULT_SMOOTH_WINOVERLAP_VALUE, Integer.valueOf(1), DEFAULT_SMOOTH_WINOVERLAP_MAX_VALUE);
        m_parameterList.add(mozCalibSmoothingWinOverlapParameter);

        m_mozCalibSmoothMinWinLMTF = new JTextField();
        IntegerParameter mozCalibSmoothingMinWinlandmarksParameter = new IntegerParameter("mozSmoothingMinimumNumberOfLandmarks", "moz Calibration Smoothing minimum number of landmarks", m_mozCalibSmoothMinWinLMTF, DEFAULT_SMOOTH_NBRLM_VALUE, Integer.valueOf(1), null);
        m_parameterList.add(mozCalibSmoothingMinWinlandmarksParameter);


    }

    /**
     * set the quanti params. This method assume parameters are formated as last Quant Params version
     *
     * @param quantParams
     */
    public void setQuantParams(Map<String, Object> quantParams) {
//        String version = quantParams.getOrDefault("config_version", "1.0").toString();
        StringBuffer errorMsg = new StringBuffer("Error setting following params, default values used:\n");
        boolean parseError = false;
        try {
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
                m_alignmentMethodParameter.setValue((String) alignmentConfig.getOrDefault(AbstractLabelFreeMSParamsPanel.QUANT_CONFIG_METHOD_NAME, ALIGNMENT_METHOD_VALUES[0]));
                Map<String, Object> alnParams = (Map<String, Object>) alignmentConfig.getOrDefault("aln_params", new HashMap<>());
                if (alnParams.containsKey("max_iterations")) {
                    try {
                        m_alignmentMaxIterationTF.setText("" + Integer.parseInt(alnParams.getOrDefault("max_iterations", DEFAULT_ALIGN_MAXITE_VALUE).toString()));
                    } catch (NumberFormatException ex) {
                        parseError = true;
                        errorMsg.append("alignment.max_iterations\n");
                        m_logger.error("error while settings max_iterations quantification params " + ex);
                        m_alignmentMaxIterationTF.setText(DEFAULT_ALIGN_MAXITE_VALUE.toString());
                    }
                }

                m_alnIgnoreErrorsParameter.setValue(alignmentConfig.getOrDefault("ignore_errors", false).toString());
                m_alignmentSmoothingMethodParameter.setValue((String) alignmentConfig.getOrDefault("smoothing_method_name", ALIGNMENT_SMOOTHING_METHOD_VALUES[0]));
                if (alignmentConfig.containsKey("smoothing_method_params")) {
                    Map<String, Object> smootingParams = (Map<String, Object>) alignmentConfig.get("smoothing_method_params");
                    try {
                        m_alignmentSmoothWinSizeTF.setText("" + Integer.parseInt(smootingParams.getOrDefault("window_size", DEFAULT_SMOOTH_WINSIZE_VALUE).toString()));
                        m_alignmentSmoothWinOverlapTF.setText("" + Integer.parseInt(smootingParams.getOrDefault("window_overlap", DEFAULT_SMOOTH_WINOVERLAP_VALUE).toString()));
                        m_alignmentSmoothMinWinLMTF.setText("" + Integer.parseInt(smootingParams.getOrDefault("min_window_landmarks", DEFAULT_SMOOTH_NBRLM_VALUE).toString()));
                    } catch (NumberFormatException ex) {
                        parseError = true;
                        errorMsg.append("alignment.smoothWinSize; alignment.smoothWinOverlap; alignment.smoothnbrLandmark\n");
                        m_logger.error("error while settings smoothing_params quanti params " + ex);
                        m_alignmentSmoothWinSizeTF.setText(DEFAULT_SMOOTH_WINSIZE_VALUE.toString());
                        m_alignmentSmoothWinOverlapTF.setText(DEFAULT_SMOOTH_WINOVERLAP_VALUE.toString());
                        m_alignmentSmoothMinWinLMTF.setText(DEFAULT_SMOOTH_NBRLM_VALUE.toString());
                    }
                }
                m_alignmentFeatureMappMethodParameter.setValue((String) alignmentConfig.getOrDefault("ft_mapping_method_name", FEATURE_MAPPING_METHOD_KEYS[0]));
                // ! V2 ft_mapping_method_params vs V3 ft_mapping_params VDS TODO keep v2 ?
                Map<String, Object> alnFtParams = (m_labelFreeParamVersion.equals("3.0")) ? (Map<String, Object>) alignmentConfig.get("ft_mapping_params") : (Map<String, Object>) alignmentConfig.get("ft_mapping_method_params");
                try {
                    if (alnFtParams.containsKey("moz_tol")) {
                        m_alignmentFeatureMapMoZTolTF.setText("" + Double.parseDouble(alnFtParams.getOrDefault("moz_tol", DEFAULT_ALIGN_FEATMAP_MOZTOL_VALUE).toString()));
                    }
                    m_alignmentFeatureMapTimeToleranceTF.setText("" + Double.parseDouble(alnFtParams.getOrDefault("time_tol", DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE).toString()));
                } catch (NumberFormatException ex) {
                    parseError = true;
                    errorMsg.append("alignment.feature mapping.mozTolenrace; \"alignment.feature mapping.RTTolenrace\n");
                    m_logger.error("error while settings ft_mapping_params quanti params " + ex);
                    m_alignmentFeatureMapMoZTolTF.setText(DEFAULT_ALIGN_FEATMAP_MOZTOL_VALUE.toString());
                    m_alignmentFeatureMapTimeToleranceTF.setText(DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE.toString());
                }
                m_alignmentSettingsCBoxTitle.setSelected(true);
            } else {
                m_alignmentSettingsCBoxTitle.setSelected(false);
            }

            //
            //*** Cross Assignment
            //
            if (quantParams.containsKey("cross_assignment_config")) {
                Map<String, Object> crossAssignmentConfig = (Map<String, Object>) quantParams.get("cross_assignment_config");
                m_crossAssignStrategyParameter.setValue((String) crossAssignmentConfig.getOrDefault("method_name", CROSSASSIGN_STRATEGY_VALUES[0]));

                Map<String, Object> ftMappingParams = (Map<String, Object>) crossAssignmentConfig.getOrDefault("ft_mapping_params", new HashMap<>());
                m_crossAssignUseAutoTimeTolParameter.setValue(ftMappingParams.getOrDefault("use_automatic_time_tol", DEFAULT_CA_FEATMAP_USE_AUTO_TIME_TOL).toString());

                Boolean useMozCalib = (Boolean) ftMappingParams.getOrDefault("use_moz_calibration",m_labelFreeParamVersion.equals("3.0") ?  DEFAULT_CA_FEATMAP_USE_MOZ_CALIBRATION : false);
                m_crossAssignUseMozCalibrationParameter.setValue(useMozCalib.toString());
                m_crossAssignFeatureMappCalibMozRB.setSelected(useMozCalib);
                m_crossAssignFeatureMappBestFeatMozRB.setSelected(!useMozCalib);

                try {
                    m_crossAssignFeatureMappMoZTolTF.setText("" + Double.parseDouble(ftMappingParams.getOrDefault("moz_tol", DEFAULT_CA_FEATMAP_MOZTOL_VALUE).toString()));
                    m_crossAssignFeatureMappRTTolTF.setText("" + Double.parseDouble(ftMappingParams.getOrDefault("time_tol", DEFAULT_CA_FEATMAP_RTTOL_VALUE).toString()));
                    m_crossAssignFeatureMappMaxAutoRTTolTF.setText("" + Double.parseDouble(ftMappingParams.getOrDefault("max_auto_time_tol", DEFAULT_CA_FEATMAP_AUTO_RT_MAX_TOL_VALUE).toString()));
                    m_crossAssignFeatureMappMinAutoRTTolTF.setText("" + Double.parseDouble(ftMappingParams.getOrDefault("min_auto_time_tol", DEFAULT_CA_FEATMAP_AUTO_RT_MIN_TOL_VALUE).toString()));
                } catch (NumberFormatException ex) {
                    parseError = true;
                    errorMsg.append("crossAssignment.feature mapping.mozTolerance; crossAssignment.feature mapping.Min/Max/RTTolerance\n");
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
                        parseError = true;
                        errorMsg.append("crossAssignment.feature mapping.filterValue\n");
                        m_logger.error("error while settings ft_filter quanti params " + ex);
                        m_featureFilterValueTF.setText(DEFAULT_CA_FILTER_VALUE.toString());
                    }
                } else {
                    m_featureFilterCBTitle.setSelected(false);
                }
                m_retainOnlyReliableFeaturesCB.setSelected(Boolean.parseBoolean(crossAssignmentConfig.getOrDefault("restrain_to_reliable_features", true).toString()));
                m_crossAssignCBoxTitle.setSelected(true);
            } else {
                m_crossAssignCBoxTitle.setSelected(false);
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
                parseError = true;
                errorMsg.append("clustering.mozTolerance; clustering.RTTolerance\n");
                m_logger.error("error while settings clustering_params quanti params " + ex);
                m_clusteringMoZTolTF.setText(DEFAULT_CLUSTER_MOZTOL_VALUE.toString());
                m_clusteringTimeTolTF.setText(DEFAULT_CLUSTER_TIMETOL_VALUE.toString());
            }
            m_clusteringIntensityComputationParameter.setValue((String) clusterParams.getOrDefault("intensity_computation", CLUSTERING_INTENSITY_COMPUTATION_VALUES[0]));
            m_clusteringTimeComputationParameter.setValue((String) clusterParams.getOrDefault("time_computation", CLUSTERING_TIME_COMPUTATION_VALUES[0]));

            //
            //*** Normalization Params
            //
            if (quantParams.containsKey("normalization_method") && quantParams.get("normalization_method") != null) {
                m_normalizationSettingsCBTitle.setSelected(true);
                m_normalizationParameter.setValue((String) quantParams.getOrDefault("normalization_method", FEATURE_NORMALIZATION_VALUES[0]));
            } else {
                m_normalizationSettingsCBTitle.setSelected(false);
            }

            // Moz Calibration Params
            m_mozCalibSmoothingMethodParameter.setValue((String) quantParams.getOrDefault("moz_calibration_smoothing_method", MOZ_CALIBRATION_SMOOTHING_METHOD_VALUES[0]));
            if (quantParams.containsKey("moz_calibration_smoothing_params")) {
                Map<String, Object> mozSmootingParams = (Map<String, Object>) quantParams.get("moz_calibration_smoothing_params");
                try {
                    m_mozCalibSmoothWinSizeTF.setText("" + Integer.parseInt(mozSmootingParams.getOrDefault("window_size", DEFAULT_SMOOTH_WINSIZE_VALUE).toString()));
                    m_mozCalibSmoothWinOverlapTF.setText("" + Integer.parseInt(mozSmootingParams.getOrDefault("window_overlap", DEFAULT_SMOOTH_WINOVERLAP_VALUE).toString()));
                    m_mozCalibSmoothMinWinLMTF.setText("" + Integer.parseInt(mozSmootingParams.getOrDefault("min_window_landmarks", DEFAULT_SMOOTH_NBRLM_VALUE).toString()));
                } catch (NumberFormatException ex) {
                    parseError = true;
                    errorMsg.append("mozCalibration.smoothingWinSize; mozCalibration.smoothingWinOverlap; mozCalibration.smoothingNbrLandmarks; \n");

                    m_logger.error("error while settings moz calibration smoothing_params quanti params " + ex); //VDS : Show error to user !
                    m_mozCalibSmoothWinSizeTF.setText(DEFAULT_SMOOTH_WINSIZE_VALUE.toString());
                    m_mozCalibSmoothWinOverlapTF.setText(DEFAULT_SMOOTH_WINOVERLAP_VALUE.toString());
                    m_mozCalibSmoothMinWinLMTF.setText(DEFAULT_SMOOTH_NBRLM_VALUE.toString());
                }
            }

            updateNormalizationSettings();
            updateAlignmentSettings();
            updateMozCalibSettings();
            updateCrossAssignmentSettings();
        } catch (Exception e) {
            parseError = true;
            errorMsg.append("Unknown error occurred. Displayed values may be wrong. See Dataset Properties view. (").append(e.getMessage()).append(") \n");

        }
        if(parseError)
            JOptionPane.showMessageDialog(this, errorMsg, "Quantitation parameters error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public Map<String, Object> getQuantParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("config_version", AbstractLabelFreeMSParamsPanel.CURRENT_QUANT_PARAM_VERSION);
        //VD TEST 
        //params.put("pep_ion_summarizing_method", "SUM");
        Map<String, Object> extractionParams = new HashMap<>();
        extractionParams.put("moz_tol", m_extractionMoZTolTF.getText());
        extractionParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
        params.put("extraction_params", extractionParams);

        params.put("use_last_peakel_detection", m_useLastPeakelDetectionParam.getStringValue());

        params.put("detection_method_name", DETECTION_METHOD_KEYS[0]);
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
            alignmentConfig.put(AbstractLabelFreeMSParamsPanel.QUANT_CONFIG_METHOD_NAME, m_alignmentMethodParameter.getStringValue());
            Map<String, Object> alignmentMethodParams = new HashMap<>();
            if (m_alignmentMethodCB.getSelectedItem().equals(ALIGNMENT_METHOD_VALUES[1])) { //param needed
                alignmentMethodParams.put("mass_interval", DEFAULT_ALIGN_MASSINTERVAL_VALUE);
                alignmentMethodParams.put("max_iterations", m_alignmentMaxIterationTF.getText());
            }
            alignmentConfig.put("method_params", alignmentMethodParams);
            alignmentConfig.put(AbstractLabelFreeMSParamsPanel.ALIGNMENT_SMOOTHING_METHOD_NAME, m_alignmentSmoothingMethodParameter.getStringValue());
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
            alnFtParams.put("time_tol", m_alignmentFeatureMapTimeToleranceTF.getText());
            if (m_alignmentFeatureMapMethodCB.getSelectedItem().equals(FEATURE_MAPPING_METHOD_VALUES[1])) { //param needed
                alnFtParams.put("moz_tol", m_alignmentFeatureMapMoZTolTF.getText());
                alnFtParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
            }
            alignmentConfig.put("ft_mapping_params", alnFtParams);
            alignmentConfig.put("ignore_errors", m_alnIgnoreErrorsParameter.getStringValue());
            params.put(ALIGNMENT_CONFIG, alignmentConfig);
        } //End if alignment selected 

        boolean crossAssignmentEnabled = m_crossAssignCBoxTitle.isSelected();
        if (crossAssignmentEnabled) {
            Map<String, Object> crossAssignmentConfig = new HashMap<>();
            crossAssignmentConfig.put(QUANT_CONFIG_METHOD_NAME, m_crossAssignStrategyParameter.getStringValue());
            crossAssignmentConfig.put("restrain_to_reliable_features", m_retainOnlyReliableFeaturesCB.isSelected());
            Map<String, Object> ftMappingParams = new HashMap<>();
            ftMappingParams.put("moz_tol", m_crossAssignFeatureMappMoZTolTF.getText());
            ftMappingParams.put("moz_tol_unit", DEFAULT_MOZTOL_UNIT);
            ftMappingParams.put("use_moz_calibration", m_crossAssignFeatureMappCalibMozRB.isSelected());
            ftMappingParams.put("use_automatic_time_tol", m_crossAssignFeatureMappUseAutoRTTolCB.isSelected());
            if(m_crossAssignFeatureMappUseAutoRTTolCB.isSelected()){
                ftMappingParams.put("max_auto_time_tol", m_crossAssignFeatureMappMaxAutoRTTolTF.getText());
                ftMappingParams.put("min_auto_time_tol", m_crossAssignFeatureMappMinAutoRTTolTF.getText());
            } else
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

        // Moz Calibration
        params.put("moz_calibration_smoothing_method", m_mozCalibSmoothingMethodParameter.getStringValue());
        if (!m_mozCalibSmoothingMethodCB.getSelectedItem().equals(MOZ_CALIBRATION_SMOOTHING_METHOD_VALUES[0])) { //param needed
            Map<String, Object> smootingParams = new HashMap<>();
            smootingParams.put("window_size", m_mozCalibSmoothWinSizeTF.getText());
            smootingParams.put("window_overlap", m_mozCalibSmoothWinOverlapTF.getText());
            if (m_mozCalibSmoothingMethodCB.getSelectedItem().equals(MOZ_CALIBRATION_SMOOTHING_METHOD_VALUES[2])) {
                smootingParams.put("min_window_landmarks", m_mozCalibSmoothMinWinLMTF.getText());
            }
            params.put("moz_calibration_smoothing_params", smootingParams);
        }
        return params;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // tabbed pane
        JTabbedPane m_tabbedPane = new JTabbedPane();
        m_tabbedPane.addTab("Detection", null, createDetectionPanel(), "Detection");
        m_tabbedPane.addTab("Clustering", null, createClusteringPanel(), "Feature Clustering");
        m_tabbedPane.addTab("Map Alignment", null, createAlignmentPanel(), "Map Alignment");
        m_tabbedPane.addTab("Moz Calibration", null, createMozCalibrationPanel(), "Moz Calibration");
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
    private JPanel createMozCalibrationPanel(){
        JPanel panel = new JPanel(new BorderLayout());

        ActionListener assActionListener = new AssignementActionListener();
        // Create Moz Calibration Smoothing specific params
        JPanel smootingPanel = new JPanel(new GridBagLayout());

        JLabel m_mozCalibSmoothMethodLabel = new JLabel("method:");
        m_mozCalibSmoothNbrLMLabel = new JLabel(SMOOTH_NBR_LANDMARKS_LABEL);
        m_mozCalibSmoothTimeIntervalLabel = new JLabel(SMOOTH_WINSIZE_LABEL);
        m_mozCalibSmoothWinOverlapLabel = new JLabel(SMOOTH_WINOVERLAP_LABEL);

        int width =Math.max((int)m_mozCalibSmoothNbrLMLabel.getPreferredSize().getWidth(),(int)m_mozCalibSmoothWinOverlapLabel.getPreferredSize().getWidth()) +5;
        m_mozCalibSmoothMethodLabel.setPreferredSize(new Dimension(width, m_mozCalibSmoothMethodLabel.getHeight()));
        m_mozCalibSmoothNbrLMLabel.setPreferredSize(new Dimension(width, m_mozCalibSmoothNbrLMLabel.getHeight()));
        m_mozCalibSmoothTimeIntervalLabel.setPreferredSize(new Dimension(width, m_mozCalibSmoothTimeIntervalLabel.getHeight()));
        m_mozCalibSmoothWinOverlapLabel.setPreferredSize(new Dimension(width, m_mozCalibSmoothWinOverlapLabel.getHeight()));

        smootingPanel.setBorder(createTitledBorder(" Smoothing ", 1));
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;
        smootingPanel.add(m_mozCalibSmoothMethodLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_mozCalibSmoothingMethodCB.addActionListener(assActionListener);
        smootingPanel.add(m_mozCalibSmoothingMethodCB, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(m_mozCalibSmoothTimeIntervalLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_mozCalibSmoothWinSizeTF, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(m_mozCalibSmoothWinOverlapLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_mozCalibSmoothWinOverlapTF, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(m_mozCalibSmoothNbrLMLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_mozCalibSmoothMinWinLMTF, c1);
        c1.weightx = 0;

        updateMozCalibSettings();
        panel.add(smootingPanel, BorderLayout.NORTH);
        setEnabled(panel, !m_readOnly);
        return panel;
    }

    private JPanel createAlignmentPanel() {
        ActionListener assActionListener = new AssignementActionListener();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(m_alignmentSettingsCBoxTitle);

        m_alignmentSettingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        JLabel m_algnmentMethodLabel = new JLabel("method:");
        m_alignmentSettingsPanel.add(m_algnmentMethodLabel, c);
        c.gridx++;
        c.weightx = 1;
        m_alignmentMethodCB.addActionListener(assActionListener);
        m_alignmentSettingsPanel.add(m_alignmentMethodCB, c);
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        m_alignmentMaxIteLabel = new JLabel("max iteration:");
        m_alignmentSettingsPanel.add(m_alignmentMaxIteLabel, c);
        c.gridx++;
        c.weightx = 1;
        m_alignmentSettingsPanel.add(m_alignmentMaxIterationTF, c);
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
        JLabel m_algnmentSmoothMethodLabel = new JLabel("method:");
        smootingPanel.add(m_algnmentSmoothMethodLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_alignmentSmoothingMethodCB.addActionListener(assActionListener);
        smootingPanel.add(m_alignmentSmoothingMethodCB, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentSmoothTimeIntervalLabel = new JLabel(SMOOTH_WINSIZE_LABEL);
        smootingPanel.add(m_alignmentSmoothTimeIntervalLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothWinSizeTF, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentSmoothWinOverlapLabel = new JLabel(SMOOTH_WINOVERLAP_LABEL);
        smootingPanel.add(m_alignmentSmoothWinOverlapLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothWinOverlapTF, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentSmoothNbrLMLabel = new JLabel(SMOOTH_NBR_LANDMARKS_LABEL);
        smootingPanel.add(m_alignmentSmoothNbrLMLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothMinWinLMTF, c1);
        c1.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        m_alignmentSettingsPanel.add(smootingPanel, c);

        // Add Alignement  FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 1));
        c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.FIRST_LINE_START;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;
        JLabel m_algnmentFeatureMapMethodLabel = new JLabel("method:");
        ftParamsPanel.add(m_algnmentFeatureMapMethodLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        m_alignmentFeatureMapMethodCB.addActionListener(assActionListener);
        ftParamsPanel.add(m_alignmentFeatureMapMethodCB, c1);
        c1.weightx = 0;

        c1.gridy++;
        c1.gridx = 0;
        m_alignmentFeatureMoZTolLabel = new JLabel("moz tolerance (ppm):");
        ftParamsPanel.add(m_alignmentFeatureMoZTolLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        ftParamsPanel.add(m_alignmentFeatureMapMoZTolTF, c1);
        c1.weightx = 0;

        c1.gridx = 0;
        c1.gridy++;
        JLabel m_alignmentFeatureTimeTolLabel = new JLabel("time tolerance (s):");
        ftParamsPanel.add(m_alignmentFeatureTimeTolLabel, c1);
        c1.gridx++;
        c1.weightx = 1;
        ftParamsPanel.add(m_alignmentFeatureMapTimeToleranceTF, c1);
        c1.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        m_alignmentSettingsPanel.add(ftParamsPanel, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        m_alignmentSettingsPanel.add(m_alnIgnoreErrorsParameter.getComponent(null), c);

        panel.add(m_alignmentSettingsPanel, BorderLayout.NORTH);
        updateAlignmentSettings();

        setEnabled(panel, !m_readOnly);
        return panel;
    }

    private JPanel createCrossAssignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(m_crossAssignCBoxTitle);

        m_crossAssignSettingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cm = new GridBagConstraints();
        cm.anchor = GridBagConstraints.FIRST_LINE_START;
        cm.fill = GridBagConstraints.BOTH;
        cm.insets = new java.awt.Insets(5, 5, 5, 5);

        cm.gridx = 0;
        cm.gridy = 0;
        cm.weightx = 0;
        JLabel m_allowCrossAssignLabel = new JLabel("Allow cross assignment ");
        m_crossAssignSettingsPanel.add(m_allowCrossAssignLabel, cm);
        cm.gridx++;
        cm.weightx = 1;
        m_crossAssignSettingsPanel.add(m_crossAssignStrategyCB, cm);

        JPanel ftPanel = getCrossAssignFilteringPanel();
        cm.gridy++;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;
        m_crossAssignSettingsPanel.add(ftPanel, cm);

        // Add FT mapping specific params
        JPanel ftParamsPanel = getCrossAssignFeatMapPanel();
        cm.gridy++;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;
        m_crossAssignSettingsPanel.add(ftParamsPanel, cm);
        panel.add(m_crossAssignSettingsPanel, BorderLayout.NORTH);

        updateCrossAssignment();
        setEnabled(panel, !m_readOnly);
        return panel;
    }

    private JPanel getCrossAssignFeatMapPanel() {
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 1));

        JLabel m_crossAssignFeatureMappMoZTolLabel = new JLabel("moz tolerance (ppm):");
        m_crossAssignFeatureMappAutoRTMaxTolLabel = new JLabel("maximum RT tolerance (s):");
        m_crossAssignFeatureMappAutoRTMinTolLabel = new JLabel("minimum RT tolerance (s):");

        int width = Math.max(m_crossAssignFeatureMappAutoRTMaxTolLabel.getPreferredSize().width, m_crossAssignFeatureMappAutoRTMinTolLabel.getPreferredSize().width)+5;
        m_crossAssignFeatureMappMoZTolLabel.setPreferredSize(new Dimension(width, m_crossAssignFeatureMappMoZTolLabel.getHeight()));
        m_crossAssignFeatureMappAutoRTMaxTolLabel.setPreferredSize(new Dimension(width, m_crossAssignFeatureMappAutoRTMaxTolLabel.getHeight()));
        m_crossAssignFeatureMappAutoRTMinTolLabel.setPreferredSize(new Dimension(width, m_crossAssignFeatureMappAutoRTMinTolLabel.getHeight()));

        GridBagConstraints c;
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;

        m_crossAssignFeatureMappUsedMozLabel = new JLabel("Used moz:");
        ftParamsPanel.add(m_crossAssignFeatureMappUsedMozLabel, c);
        c.gridx++;
        c.gridy++;
        c.weightx = 1;
        c.insets = new Insets(0, 5, 0, 5);
        ftParamsPanel.add(m_crossAssignFeatureMappCalibMozRB, c);
        c.gridy++;
        c.weightx = 1;
        ftParamsPanel.add(m_crossAssignFeatureMappBestFeatMozRB, c);
        c.weightx = 0;

        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy++;

        ftParamsPanel.add(m_crossAssignFeatureMappMoZTolLabel, c);
        c.gridx++;
        c.weightx = 1;
        ftParamsPanel.add(m_crossAssignFeatureMappMoZTolTF, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        c.gridwidth=2;
        m_crossAssignFeatureMappUseAutoRTTolCB.addActionListener(new AssignementActionListener());
        m_crossAssignFeatureMappUseAutoRTTolCB.setEnabled(false);
        ftParamsPanel.add(m_crossAssignFeatureMappUseAutoRTTolCB, c);

        c.gridwidth=1;
        c.gridx = 0;
        c.gridy++;
        m_crossAssignFeatureMappRTTolLabel = new JLabel("RT tolerance (s):");
        ftParamsPanel.add(m_crossAssignFeatureMappRTTolLabel, c);
        c.gridx++;
        c.weightx = 1;
//        c.gridwidth=2;
        ftParamsPanel.add(m_crossAssignFeatureMappRTTolTF, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
//        c.gridwidth=1;

        ftParamsPanel.add(m_crossAssignFeatureMappAutoRTMinTolLabel, c);
        c.gridx++;
        c.weightx = 1;
//        c.gridwidth=2;
        ftParamsPanel.add(m_crossAssignFeatureMappMinAutoRTTolTF, c);
        c.weightx = 0;

//        c.gridwidth=1;
        c.gridx = 0;
        c.gridy++;

        ftParamsPanel.add(m_crossAssignFeatureMappAutoRTMaxTolLabel, c);
        c.gridx++;
        c.weightx = 1;
//        c.gridwidth=2;
        ftParamsPanel.add(m_crossAssignFeatureMappMaxAutoRTTolTF, c);
        c.weightx = 0;
        return ftParamsPanel;
    }

    private JPanel getCrossAssignFilteringPanel() {
        JPanel ftPanel = new JPanel(new GridBagLayout());
        ftPanel.setBorder(createTitledBorder(" Filtering ", 1));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        ftPanel.add(m_retainOnlyReliableFeaturesCB, c);

        JPanel m_featureFilterPanel = new JPanel(new GridBagLayout());
        m_featureFilterPanel.setBorder(m_featureFilterCBTitle);

        m_featureFilterNameLabel = new JLabel("intensity:");
        m_featureFilterOperatorLabel = new JLabel("operator:");
        m_featureFilterValueLabel = new JLabel("value:");
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new Insets(5, 5, 5, 5);

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
        return ftPanel;
    }

    private void updateCrossAssignment() {
        setEnabled(m_crossAssignSettingsPanel, !m_readOnly && m_crossAssignCBoxTitle.isSelected());
        updateCrossAssignmentSettings();
        if (m_readOnly) {
            m_crossAssignSettingsPanel.setVisible(m_crossAssignCBoxTitle.isSelected());
        }
    }

    private void updateCrossAssignmentSettings() {
        //Until available, force m_crossAssignFeatureMappUseAutoRTTolCB to disable
        m_crossAssignFeatureMappUseAutoRTTolCB.setEnabled(false);
        boolean isAutoRT = m_crossAssignFeatureMappUseAutoRTTolCB.isSelected();
        m_crossAssignFeatureMappMinAutoRTTolTF.setVisible(isAutoRT);
        m_crossAssignFeatureMappMaxAutoRTTolTF.setVisible(isAutoRT);
        m_crossAssignFeatureMappRTTolTF.setVisible(!isAutoRT);
        m_crossAssignFeatureMappRTTolLabel.setVisible(!isAutoRT);
        m_crossAssignFeatureMappAutoRTMaxTolLabel.setVisible(isAutoRT);
        m_crossAssignFeatureMappAutoRTMinTolLabel.setVisible(isAutoRT);

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
        setEnabled(m_alignmentSettingsPanel, !m_readOnly && m_alignmentSettingsCBoxTitle.isSelected());
        updateAlignmentSettings();
        if (m_readOnly) {
            m_alignmentSettingsPanel.setVisible(m_alignmentSettingsCBoxTitle.isSelected());
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
        boolean isSettingNeeded = m_alignmentFeatureMapMethodCB.getSelectedItem().equals(FEATURE_MAPPING_METHOD_VALUES[1]);
        m_alignmentFeatureMoZTolLabel.setVisible(isSettingNeeded);
        m_alignmentFeatureMapMoZTolTF.setVisible(isSettingNeeded);
    }


    private void updateMozCalibSettings() {
        boolean isSettingNeeded = !m_mozCalibSmoothingMethodCB.getSelectedItem().equals(MOZ_CALIBRATION_SMOOTHING_METHOD_VALUES[0]);
        boolean isNbrLMNeeded = m_mozCalibSmoothingMethodCB.getSelectedItem().equals(MOZ_CALIBRATION_SMOOTHING_METHOD_VALUES[2]);

        m_mozCalibSmoothWinSizeTF.setVisible(isSettingNeeded);
        m_mozCalibSmoothTimeIntervalLabel.setVisible(isSettingNeeded);
        m_mozCalibSmoothWinOverlapTF.setVisible(isSettingNeeded);
        m_mozCalibSmoothWinOverlapLabel.setVisible(isSettingNeeded);

        m_mozCalibSmoothNbrLMLabel.setVisible(isNbrLMNeeded);
        m_mozCalibSmoothMinWinLMTF.setVisible(isNbrLMNeeded);
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
            if(e.getSource().equals(m_alignmentMethodCB) || e.getSource().equals(m_alignmentSmoothingMethodCB)  || e.getSource().equals(m_alignmentFeatureMapMethodCB) )
                updateAlignmentSettings();
            else if(e.getSource().equals(m_mozCalibSmoothingMethodCB))
                updateMozCalibSettings();
            else if(e.getSource().equals(m_crossAssignFeatureMappUseAutoRTTolCB))
                updateCrossAssignmentSettings();
            else
                throw new RuntimeException("WHO Generate Action "+e.getSource());
        }

    }
}
