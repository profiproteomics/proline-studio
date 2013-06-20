package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.*;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetPeptideSpectrumPanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetPeptideSpectrum extends AbstractDataBox {

    public DataBoxRsetPeptideSpectrum() {

        // Name of this databox
        name = "Spectrum";
        description = "Spectrum of a Peptide";

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
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged(Class dataType) {
        final PeptideMatch peptideMatch = (PeptideMatch) previousDataBox.getData(false, PeptideMatch.class);

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
                AccessDatabaseThread.getAccessDatabaseThread().removeTask(m_previousTaskId);
            }
            m_previousTaskId = taskId;
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);




        } else {
            ((RsetPeptideSpectrumPanel) m_panel).setData(peptideMatch);
        }
    }
    private Long m_previousTaskId = null;

}
