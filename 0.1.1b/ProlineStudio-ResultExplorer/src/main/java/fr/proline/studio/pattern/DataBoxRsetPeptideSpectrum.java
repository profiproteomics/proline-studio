package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideSpectrumPanel;

/**
 * Databox for a Spectrum
 * @author JM235353
 */
public class DataBoxRsetPeptideSpectrum extends AbstractDataBox {

    public DataBoxRsetPeptideSpectrum() {

        // Name of this databox
        m_name = "Spectrum";
        m_description = "Spectrum of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PeptideMatch.class, false);
        registerInParameter(inParameter);

        inParameter = new GroupParameter();
        inParameter.addParameter(PeptideInstance.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        // none
    }

    @Override
    public void createPanel() {
        RsetPeptideSpectrumPanel p = new RsetPeptideSpectrumPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final PeptideMatch peptideMatch = (PeptideMatch) m_previousDataBox.getData(false, PeptideMatch.class);

        if (peptideMatch == null) {
            ((RsetPeptideSpectrumPanel) m_panel).setData(null);
            return;
        }

        boolean needToLoadData = ((!peptideMatch.getTransientData().getIsMsQuerySet())
                || (!peptideMatch.getMsQuery().getTransientIsSpectrumSet()));

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


                    ((RsetPeptideSpectrumPanel) m_panel).setData(peptideMatch);
                    
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
            ((RsetPeptideSpectrumPanel) m_panel).setData(peptideMatch);
        }
    }
    private Long m_previousTaskId = null;

}
