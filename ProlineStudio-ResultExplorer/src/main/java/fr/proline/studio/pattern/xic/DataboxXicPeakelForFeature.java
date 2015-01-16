package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Peakel;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeakelPanel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxXicPeakelForFeature extends AbstractDataBox {

    private Feature m_feature;
    private List<Peakel> m_peakelList ;
    private Color m_color;
    private String m_title;
    
    public DataboxXicPeakelForFeature() { 
        super(DataboxType.DataboxXicPeakelForFeature);
        
        // Name of this databox
        m_name = "Peakels";
        m_description = "All Peakels for a Feature";

        // Register Possible in parameters
        // One Map 
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(Feature.class, false); 
        registerInParameter(inParameter);


        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(Peakel.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

    }
    
     @Override
    public void createPanel() {
        XicPeakelPanel p = new XicPeakelPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        Feature oldFeature = m_feature;
        m_feature = (Feature) m_previousDataBox.getData(false, Feature.class);
        m_color = (Color) m_previousDataBox.getData(false, Color.class);
        m_title = (String) m_previousDataBox.getData(false, String.class);
        final int loadingId = setLoading();
        
        if (m_feature != null && m_feature.equals(oldFeature)) {
            return;
        }

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    ((XicPeakelPanel) m_panel).setData(taskId, m_feature, m_peakelList, m_color, m_title, finished);
                } else {
                    ((XicPeakelPanel) m_panel).dataUpdated(subTask, finished);
                }
                
                
                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };

        // ask asynchronous loading of data
        m_peakelList = new ArrayList();
        DatabaseLoadLcMSTask task = new DatabaseLoadLcMSTask(callback);
        task.initLoadPeakelForFeature(getProjectId(), m_feature, m_peakelList);
        
        registerTask(task);

    }
    
    
    
    
    @Override
    public void setEntryData(Object data) {
        m_feature = (Feature) data;
        dataChanged();
    }
   
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
           if (parameterType.equals(Peakel.class)) {
                return ((XicPeakelPanel) m_panel).getSelectedPeakel();
            }
           if (parameterType.equals(CompareDataInterface.class)) {
                return ((CompareDataProviderInterface) m_panel).getCompareDataInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
   
    @Override
    public String getFullName() {
        return m_feature.getElutionTime()+" "+getName();
    }
}
