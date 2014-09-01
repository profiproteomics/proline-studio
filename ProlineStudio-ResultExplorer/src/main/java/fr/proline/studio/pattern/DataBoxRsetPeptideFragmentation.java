package fr.proline.studio.pattern;



import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseObjectTreeTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideFragmentationTablePanel;
import fr.proline.studio.rsmexplorer.spectrum.PeptideFragmentationData;

/**
 *
 * @author AW
 */
public class DataBoxRsetPeptideFragmentation extends AbstractDataBox {


    public DataBoxRsetPeptideFragmentation() {
        // Name of this databox
        m_name = "Fragmentation Table";
        m_description = "Fragmentation table of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
        registerInParameter(inParameter);


    }
    @Override
    public void createPanel() {
    	RsetPeptideFragmentationTablePanel p = new RsetPeptideFragmentationTablePanel();
    	p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }
	    

    
    @Override
    public void dataChanged() {
        final DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);

        if (peptideMatch == m_previousPeptideMatch) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;
        
        if (peptideMatch == null) {
            ((RsetPeptideFragmentationTablePanel ) m_panel).setData(null, null);
            return;
        }
        
        
            final int loadingId = setLoading();

            
            final ObjectTree[] objectTreeResult = new ObjectTree[1];
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    ObjectTree objectTree = objectTreeResult[0];
                    PeptideFragmentationData peptideFragmentationData = (objectTree!=null) ? new PeptideFragmentationData(objectTree) : null;
                    ((RsetPeptideFragmentationTablePanel) m_panel).setData(peptideMatch, peptideFragmentationData); 
                  
                    

                    setLoaded(loadingId);
                    
                    if (finished) {
                        unregisterTask(taskId);
                    }
                }
            };

            // Load data if needed asynchronously
            DatabaseObjectTreeTask task = new DatabaseObjectTreeTask(callback, getProjectId(), peptideMatch, objectTreeResult);
            Long taskId = task.getId();
            if (m_previousTaskId != null) {
                // old task is suppressed if it has not been already done
                AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
            }
            m_previousTaskId = taskId;
            registerTask(task);



            

    }
    private Long m_previousTaskId = null;
    private DPeptideMatch m_previousPeptideMatch = null;

}
