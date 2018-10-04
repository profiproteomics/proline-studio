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
public abstract class AbstractGenericQuantParamsPanel extends JPanel {

     //Some default value
    protected final Double DEFAULT_EXTRACTION_MOZTOL_VALUE=5.0;
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
    protected final Double DEFAULT_CA_FEATMAP_RTTOL_VALUE = 60.0;
    protected final String DEFAULT_CA_FILTER_NAME_VALUE = "INTENSITY";
    protected final String DEFAULT_CA_FILTER_OPERATOR_VALUE = "GT";
    protected final Double DEFAULT_CA_FILTER_VALUE = 0.0;
    
    public static final String XIC_PARAMS_PREFIX = "XicParameters";
    public static final String XIC_SIMPLIFIED_PARAMS = XIC_PARAMS_PREFIX+".isSimplifiedParameters";
    public static final String XIC_PARAMS_VERSION_KEY = XIC_PARAMS_PREFIX+".parametersVersion";

    protected ParameterList m_parameterList;
        
    protected final boolean m_readOnly;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    protected JScrollPane m_scrollPane;
    
    public AbstractGenericQuantParamsPanel(boolean readOnly) {
        m_readOnly = readOnly;
        m_parameterList = new ParameterList(XIC_PARAMS_PREFIX);
    }
        
    public abstract Map<String,Object> getQuantParams();    
    public abstract void setQuantParams(Map<String,Object>  quantParams);    
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
    
    public ParameterList getParameterList() {
        return m_parameterList;
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
