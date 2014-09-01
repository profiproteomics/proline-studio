package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.DatabaseObjectTreeTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideSpectrumErrorPanel;
import fr.proline.studio.rsmexplorer.spectrum.PeptideFragmentationData;

/**
 * Databox for a Spectrum
 *
 * @author JM235353
 */
public class DataBoxRsetPeptideSpectrumError extends AbstractDataBox {

    private DPeptideMatch m_previousPeptideMatch = null;
    
    public DataBoxRsetPeptideSpectrumError() {

        // Name of this databox
        m_name = "Spectrum Error";
        m_description = "Spectrum Error of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
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

        if (m_previousPeptideMatch == peptideMatch) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;

        
        if (peptideMatch == null) {
            ((RsetPeptideSpectrumErrorPanel) m_panel).setData(null, null);
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

                    loadAnnotations(peptideMatch);

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
            loadAnnotations(peptideMatch);
        }
    }
    private Long m_previousTaskId = null;

    public void loadAnnotations(final DPeptideMatch peptideMatch) {


        final ObjectTree[] objectTreeResult = new ObjectTree[1];
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                ObjectTree objectTree = objectTreeResult[0];
                PeptideFragmentationData peptideFragmentationData = (objectTree != null) ? new PeptideFragmentationData(objectTree) : null;

                ((RsetPeptideSpectrumErrorPanel) m_panel).setData(peptideMatch, peptideFragmentationData);


            }
        };

        // Load data if needed asynchronously
        DatabaseObjectTreeTask task = new DatabaseObjectTreeTask(callback, getProjectId(), peptideMatch, objectTreeResult);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
}
