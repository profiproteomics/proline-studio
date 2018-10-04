package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.ObjectParameter;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 *
 * @author JM235353
 */

public abstract class AbstractDefineQuantParamsPanel extends AbstractGenericQuantParamsPanel {
    
            //-- quanti Params
            /*"quantitation_config": {
		"extraction_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM"
		},
		"clustering_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM",
			"time_tol": "15",
			"time_computation": "MOST_INTENSE",
			"intensity_computation": "MOST_INTENSE"
		},
		"aln_method_name": "ITERATIVE",
		"aln_params": {
			"mass_interval": "20000",
			"max_iterations": "3",
			"smoothing_method_name": "TIME_WINDOW",
			"smoothing_params": {
				"window_size": "200",
				"window_overlap": "20",
				"min_window_landmarks": "50"
			},
			"ft_mapping_params": {
				"moz_tol": "5",
				"moz_tol_unit": "PPM",
				"time_tol": "600"
			}
		},
		"ft_filter": {
			"name": "INTENSITY",
			"operator": "GT",
			"value": "0"
		},
		"ft_mapping_params": {
			"moz_tol": "10",
			"moz_tol_unit": "PPM",
			"time_tol": "120"
		},
		"normalization_method": "MEDIAN_RATIO"
	}*/
    
    
    protected final static String[] ALIGNMENT_METHOD_VALUES = {"Exhaustive", "Iterative"};
    protected final static String[] ALIGNMENT_METHOD_KEYS = {"EXHAUSTIVE", "ITERATIVE"};

    protected final static String[] ALIGNMENT_SMOOTHING_METHOD_VALUES = {"Landmark Range", "Loess", "Time Window"};
    protected final static String[] ALIGNMENT_SMOOTHING_METHOD_KEYS = {"LANDMARK_RANGE", "LOESS", "TIME_WINDOW"};


    protected ObjectParameter<String> m_alignmentMethodParameter;
    protected ObjectParameter<String> m_alignmentSmoothingMethodParameter;    

    protected JTextField m_extractionMoZTolTF;

     
    protected JComboBox  m_alignmentMethodCB;
    protected JTextField m_alignmentMaxIterationTF;
    protected JComboBox  m_alignmentSmoothingMethodCB;
    protected JTextField m_alignmentSmoothingWinSizeTF;
    protected JTextField m_alignmentSmoothingMinWinlandmarksTF;
    protected JTextField m_alignmentFeatureMappingTimeToleranceTF;


    protected final static String[] FEATURE_MAPPING_METHOD_VALUES = {"Feature Coordinates", "Peptide Identity"};
    protected final static String[] FEATURE_MAPPING_METHOD_KEYS = {"FEATURE_COORDINATES", "PEPTIDE_IDENTITY"};
    

    protected JTextField m_featureMappingTimeTolTF;
    

    public AbstractDefineQuantParamsPanel(boolean readOnly) {
        super(readOnly);
        setBorder(AbstractDefineQuantParamsPanel.createTitledBorder(" XIC Parameters ", 0));
    }
    
}
