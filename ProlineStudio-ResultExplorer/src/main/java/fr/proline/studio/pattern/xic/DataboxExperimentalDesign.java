package fr.proline.studio.pattern.xic;


import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.ExperimentalDesignPanel;


/**
 * experimental design databox
 * @author 
 */
public class DataboxExperimentalDesign extends AbstractDataBox {

    private DDataset m_dataset;
    
    
    public DataboxExperimentalDesign() { 
        super(DataboxType.DataboxExperimentalDesign, DataboxStyle.STYLE_XIC);
        
        // Name of this databox
        m_typeName = "Experimental Design";
        m_description = "Experimental Design of the quantitation";

        // Register Possible in parameters
        // One Dataset 
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false); 
        registerInParameter(inParameter);


        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DDataset.class, false);
        registerOutParameter(outParameter);
    }
    
    @Override
    public void createPanel() {
        ExperimentalDesignPanel p = new ExperimentalDesignPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    ((ExperimentalDesignPanel) m_panel).setData(taskId, m_dataset, finished);
                   
                } else {
                    ((ExperimentalDesignPanel) m_panel).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId); 
                    propagateDataChanged(DDataset.class); 
                }
            }
        };

        // ask asynchronous loading of data
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadQuantChannels(getProjectId(), m_dataset);
        registerTask(task);

    }
    
    
    @Override
    public void setEntryData(Object data) {
        m_panel.addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }
   
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(DDataset.class)) {
                return m_dataset;
            }
        }
        return super.getData(getArray, parameterType);
    }
   
    @Override
    public String getFullName() {
        return m_dataset.getName()+" "+getTypeName();
    }
}
