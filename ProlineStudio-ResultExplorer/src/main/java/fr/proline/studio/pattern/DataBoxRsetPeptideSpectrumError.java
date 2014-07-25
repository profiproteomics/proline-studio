package fr.proline.studio.pattern;



	import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideSpectrumErrorPanel;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideSpectrumPanel;

	/**
	 * Databox for a Spectrum
	 * @author JM235353
	 */
	public class DataBoxRsetPeptideSpectrumError extends AbstractDataBox {

	    public DataBoxRsetPeptideSpectrumError() {

	        // Name of this databox
	        m_name = "Spectrum Error";
	        m_description = "Spectrum Error of a Peptide";

	        // Register in parameters
	        GroupParameter inParameter = new GroupParameter();
	        inParameter.addParameter(DPeptideMatch.class, false);
	        registerInParameter(inParameter);

	        inParameter = new GroupParameter();
	        inParameter.addParameter(DPeptideInstance.class, false);
	        registerInParameter(inParameter);

	        // Register possible out parameters
	        // none
	    }

	    @Override
	    public void createPanel() {
	        RsetPeptideSpectrumErrorPanel p = new RsetPeptideSpectrumErrorPanel();
	        p.setName(m_name);
	        p.setDataBox(this);
	        m_panel = p;
	    }

	    @Override
	    public void dataChanged() {
	        final DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);

	        if (peptideMatch == null) {
	            ((RsetPeptideSpectrumErrorPanel) m_panel).setData(null);
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


	                    ((RsetPeptideSpectrumErrorPanel) m_panel).setData(peptideMatch);
	                    
	                    setLoaded(loadingId);
	                    
	                    if (finished) {
	                        unregisterTask(taskId);
	                    }
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
	            ((RsetPeptideSpectrumErrorPanel) m_panel).setData(peptideMatch);
	        }
	    }
	    private Long m_previousTaskId = null;

	}
