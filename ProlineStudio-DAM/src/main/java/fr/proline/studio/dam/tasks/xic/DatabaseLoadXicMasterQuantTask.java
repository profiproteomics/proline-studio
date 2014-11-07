package fr.proline.studio.dam.tasks.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseSlicerTask;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadXicMasterQuantTask extends AbstractDatabaseSlicerTask {

    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 1000;
    
    public static final int SUB_TASK_COUNT = 0; // <<----- get in sync  //JPM.TODO
    
    private Long m_projectId;
    private DDataset m_dataset;
    
     public DatabaseLoadXicMasterQuantTask(AbstractDatabaseCallback callback) {
        super(callback);
       
    }
     
    public void initLoadProteinSets(long projectId, DDataset dataset) {
        init(SUB_TASK_COUNT, new TaskInfo("Load Protein Sets of XIC " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_dataset = dataset;
        //action = LOAD_PROTEIN_SET_FOR_RSM; //JPM.TODO
    }
    
    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        /*switch (action) {
            case LOAD_PROTEIN_SET_FOR_RSM:
                m_rsm.getTransientData().setProteinSetArray(null);
                break;
            case LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE:
                m_peptideInstance.getTransientData().setProteinSetArray(null);
                break;
            case LOAD_PROTEIN_SET_NUMBER:
                m_rsm.getTransientData().setNumberOfProteinSet(null);
                break;
        }*/ //JPM.TODO
    }
     
    @Override
    public boolean fetchData() {
         return true; // JPM.TODO
    }

    @Override
    public boolean needToFetch() {
        /*switch (action) {
            case LOAD_PROTEIN_SET_FOR_RSM:
                return (m_rsm.getTransientData().getProteinSetArray() == null);
            case LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE:
                return (m_peptideInstance.getTransientData().getProteinSetArray() == null);
            case LOAD_PROTEIN_SET_NUMBER:
                m_rsm = m_dataset.getResultSummary();
                return (m_rsm.getTransientData().getNumberOfProteinSet() == null);
        }
        return false; // should not happen */ //JPM.TODO
        return true;
    }
    
}
