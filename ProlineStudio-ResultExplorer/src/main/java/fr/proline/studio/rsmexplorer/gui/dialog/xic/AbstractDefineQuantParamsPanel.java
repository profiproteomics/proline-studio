package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.settings.FilePreferences;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public abstract class AbstractDefineQuantParamsPanel extends JPanel {
    
    public static final String XIC_SIMPLIFIED_PARAMS = "XIC_SIMPLIFIED_PARAMS";
    
    protected ParameterList m_parameterList;
        
    protected JScrollPane m_scrollPane;
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    protected final boolean m_readOnly;


    
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


    protected JTextField m_featureMappingTimeTolTF;
    

    public AbstractDefineQuantParamsPanel(boolean readOnly) {
        m_readOnly = readOnly;
        setBorder(AbstractDefineQuantParamsPanel.createTitledBorder(" XIC Parameters ", 0));
    }
    
    public abstract Map<String,Object> getQuantParams();
    
    public abstract boolean isSimplifiedPanel();
        
    public void resetScrollbar() {
        m_scrollPane.getVerticalScrollBar().setValue(0);
    }
    
    
    public ParameterList getParameterList() {
        return m_parameterList;
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
    
    public abstract void setQuantParams(Map<String,Object>  quantParams);
    
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
