package fr.proline.studio.pattern;



import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideFragmentationTablePanel;

/**
 *
 * @author AW
 */
public class DataBoxRsetPeptideFragmentationTable extends AbstractDataBox {


	 public DataBoxRsetPeptideFragmentationTable() {
			// Name of this databox
			m_name = "Fragmentation Table";
			m_description = "Fragmentation table of a Peptide";

	        // Register in parameters
	        GroupParameter inParameter = new GroupParameter();
	        inParameter.addParameter(DPeptideMatch.class, false);
	        registerInParameter(inParameter);

	        inParameter = new GroupParameter();
	        inParameter.addParameter(PeptideInstance.class, false);
	        registerInParameter(inParameter);

	        // Register possible out parameters
	        // none

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

        if (peptideMatch == null) {
            ((RsetPeptideFragmentationTablePanel ) m_panel).setData(null);
            return;
        }

        boolean needToLoadData = ((!peptideMatch.isMsQuerySet())
                || (!peptideMatch.getMsQuery().isSpectrumSet()));

        if (needToLoadData) {

            final int loadingId = setLoading();

            //final String searchedText = searchTextBeingDone; //JPM.TODO
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {


                    ((RsetPeptideFragmentationTablePanel) m_panel).setData(peptideMatch);
                    
                    setLoaded(loadingId);
                }
            };

            // Load data if needed asynchronously
            DatabaseLoadSpectrumsTask task = new DatabaseLoadSpectrumsTask(callback, getProjectId(), peptideMatch);
            Long taskId = task.getId();
            if (m_previousTaskId != null) {
                // old task is suppressed if it has not been already done
                AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
            }
            m_previousTaskId = taskId;
            registerTask(task);




        } else {
            ((RsetPeptideFragmentationTablePanel) m_panel).setData(peptideMatch);
        }
    }
    private Long m_previousTaskId = null;


}
