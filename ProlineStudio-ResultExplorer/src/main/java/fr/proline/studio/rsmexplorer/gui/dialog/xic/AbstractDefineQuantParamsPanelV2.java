package fr.proline.studio.rsmexplorer.gui.dialog.xic;

/**
 *
 * @author JM235353
 */
public abstract class AbstractDefineQuantParamsPanelV2 extends AbstractGenericQuantParamsPanel {

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

    public final static String ALIGNMENT_CONFIG = "alignment_config";
    public final static String ALIGNMENT_METHOD_NAME = "method_name";

    public AbstractDefineQuantParamsPanelV2(boolean readOnly) {
        super(readOnly);
        setBorder(AbstractDefineQuantParamsPanelV2.createTitledBorder(" XIC Parameters ", 0));
    }

}
