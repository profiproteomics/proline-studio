/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.settings.FilePreferences;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public abstract class AbstractLabelFreeMSParamsPanel extends AbstractParamsPanel {

    //-- quanti Params : All available params
    /*  
        
        "quantitation_config": {
            "config_version": "2.0",
            "extraction_params": {
                "moz_tol": 5.0,
                "moz_tol_unit": "PPM" 
            },
            "clustering_params": {
                "moz_tol": 5.0,
                "moz_tol_unit": "PPM",
                "time_tol": 15.0,
                "intensity_computation": "MOST_INTENSE",
                "time_computation": "MOST_INTENSE" 
            },
            "alignment_config": {
                "method_name": "ITERATIVE",
                "method_params": {
                    "mass_interval": 20000,
                    "max_iterations": "3" 
                    },
                "smoothing_method_name": "TIME_WINDOW",
                "smoothing_method_params": {
                        "window_size": 200,
                        "window_overlap": 20,
                        "min_window_landmarks": "50" 
                },
                "ft_mapping_method_name": "FEATURE_COORDINATES",
                "ft_mapping_method_params": {
                        "moz_tol": "5.0",
                        "moz_tol_unit": "PPM",
                        "time_tol": 600.0
                }
            },
            "cross_assignment_config": {
                "method_name": "BETWEEN_ALL_RUNS",
                "ft_mapping_params": {
                    "moz_tol": 5.0,
                    "moz_tol_unit": "PPM",
                    "time_tol": 60.0
                },
                "restrain_to_reliable_features": true,
                "ft_filter": {
                    "name": "RELATIVE_INTENSITY",
                    "operator": "LT",
                    "value": 12.0
                }
            },
            "normalization_method": "MEDIAN_RATIO",
            "detection_method_name": "DETECT_PEAKELS" // or "DETECT_FEATURES" or "EXTRACT_IONS" 
            "detection_method_params": {              // optionnel car utile uniquement pour "EXTRACT_IONS" 
                "start_from_validated_peptides": true   
            }
            "use_last_peakel_detection": false
	}
     */
    //DEFAULT VALUE should be at index 0 !!     
    protected final static String[] CROSSASSIGN_STRATEGY_VALUES = {"Between all runs", "Within groups only"};
    protected final static String[] CROSSASSIGN_STRATEGY_KEYS = {"BETWEEN_ALL_RUNS", "WITHIN_GROUPS_ONLY"};
    protected final static String[] ALIGNMENT_METHOD_VALUES = {"Exhaustive", "Iterative"};
    protected final static String[] ALIGNMENT_METHOD_KEYS = {"EXHAUSTIVE", "ITERATIVE"};
    protected final static String[] FEATURE_MAPPING_METHOD_VALUES = {"Peptide Identity", "Feature Coordinates"};
    protected final static String[] FEATURE_MAPPING_METHOD_KEYS = {"PEPTIDE_IDENTITY", "FEATURE_COORDINATES"};
    protected final static String[] ALIGNMENT_SMOOTHING_METHOD_VALUES = {"Loess", "Landmark Range", "Time Window"};
    protected final static String[] ALIGNMENT_SMOOTHING_METHOD_KEYS = {"LOESS", "LANDMARK_RANGE", "TIME_WINDOW"};
    protected final static String[] CLUSTERING_TIME_COMPUTATION_VALUES = {"Most Intense", "Median"};
    protected final static String[] CLUSTERING_TIME_COMPUTATION_KEYS = {"MOST_INTENSE", "MEDIAN"};
    protected final static String[] CLUSTERING_INTENSITY_COMPUTATION_VALUES = {"Most Intense", "Sum"};
    protected final static String[] CLUSTERING_INTENSITY_COMPUTATION_KEYS = {"MOST_INTENSE", "SUM"};
    protected final static String[] FEATURE_FILTER_NAME_VALUES = {"Intensity", "Relative Intensity"};
    protected final static String[] FEATURE_FILTER_NAME_KEYS = {"INTENSITY", "RELATIVE_INTENSITY"};
    protected final static String[] FEATURE_FILTER_OPERATOR_VALUES = {">", "<"};
    protected final static String[] FEATURE_FILTER_OPERATOR_KEYS = {"GT", "LT"};
    protected final static String[] FEATURE_NORMALIZATION_VALUES = {"Median Ratio", "Intensity Sum", "Median Intensity"};
    protected final static String[] FEATURE_NORMALIZATION_KEYS = {"MEDIAN_RATIO", "INTENSITY_SUM", "MEDIAN_INTENSITY"};
    protected final static String[] DETECTION_METHOD_VALUES = {"Detect Peakels", "Detect Features", "Extract ions"};
    protected final static String[] DETECTION_METHOD_KEYS = {"DETECT_PEAKELS", "DETECT_FEATURES", "EXTRACT_IONS"};

    //Some other default value   
    protected final Double DEFAULT_EXTRACTION_MOZTOL_VALUE = 5.0;
    protected final String DEFAULT_MOZTOL_UNIT = "PPM";

    protected final Double DEFAULT_CLUSTER_MOZTOL_VALUE = 5.0;
    protected final Double DEFAULT_CLUSTER_TIMETOL_VALUE = 15.0;
    protected final String DEFAULT_CLUSTER_TIMECOMPUT_VALUE = "MOST_INTENSE";
    protected final String DEFAULT_CLUSTER_INTENSITYCOMPUT_VALUE = "MOST_INTENSE";

    protected final Double DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE = 600.0;
    protected final Double DEFAULT_ALIGN_FEATMAP_MOZTOL_VALUE = 5.0;
    protected final Integer DEFAULT_ALIGN_MAXITE_VALUE = 3;
    protected final Integer DEFAULT_ALIGN_MASSINTERVAL_VALUE = 20000;
    protected final Integer DEFAULT_SMOOTH_WINSIZE_VALUE = 200;
    protected final Integer DEFAULT_SMOOTH_WINOVERLAP_VALUE = 20;
    protected final Integer DEFAULT_SMOOTH_WINOVERLAP_MAX_VALUE = 100;
    protected final Integer DEFAULT_SMOOTH_NBRLM_VALUE = 50;

    protected final Double DEFAULT_CA_FEATMAP_MOZTOL_VALUE = 5.0;
    public static final Double DEFAULT_CA_FEATMAP_RTTOL_VALUE = 60.0;
    protected final String DEFAULT_CA_FILTER_NAME_VALUE = "INTENSITY";
    protected final String DEFAULT_CA_FILTER_OPERATOR_VALUE = "GT";
    protected final Double DEFAULT_CA_FILTER_VALUE = 0.0;

    public static final String XIC_PARAMS_PREFIX = "XicParameters";
    public static final String XIC_SIMPLIFIED_PARAMS = XIC_PARAMS_PREFIX + ".isSimplifiedParameters";
    public static final String XIC_PARAMS_VERSION_KEY = XIC_PARAMS_PREFIX + ".parametersVersion";
    public final static String ALIGNMENT_CONFIG = "alignment_config";
    public final static String ALIGNMENT_METHOD_NAME = "method_name";

    protected final boolean m_readOnly;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    protected JScrollPane m_scrollPane;

    public AbstractLabelFreeMSParamsPanel(boolean readOnly) {
        m_readOnly = readOnly;
        m_parameterList = new ParameterList(XIC_PARAMS_PREFIX);
        setBorder(AbstractLabelFreeMSParamsPanel.createTitledBorder(" Quantitation Parameters ", 0));
    }


    public abstract boolean isSimplifiedPanel();

    public void resetScrollbar() {
        m_scrollPane.getVerticalScrollBar().setValue(0);
    }

    public void loadParameters(FilePreferences filePreferences) throws BackingStoreException {

        Preferences preferences = NbPreferences.root();
        String[] keys = filePreferences.keys();
        for (String key : keys) {

            String value = filePreferences.get(key, null);
            preferences.put(key, value);

        }

        getParameterList().loadParameters(filePreferences); //Load params 
    }


    public static TitledBorder createTitledBorder(String title, int level) {
        switch (level) {
            case 0:
                return BorderFactory.createTitledBorder("<html> <b>" + title + "</b></html>");
            case 1:
                return BorderFactory.createTitledBorder("<html><font color='#0000FF'>&nbsp;&#x25A0;&nbsp;</font> <b>" + title + "</b></html>");
            case 2:
            default:
                return BorderFactory.createTitledBorder("<html><font color='#009999'>&nbsp;&#x25A0;&nbsp;</font> <b>" + title + "</b></html>");
        }

    }
}
