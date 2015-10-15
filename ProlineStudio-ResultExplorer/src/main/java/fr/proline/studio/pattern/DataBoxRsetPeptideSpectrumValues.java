
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.spectrum.RsetPeptideSpectrumValuesPanel;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetPeptideSpectrumValues extends AbstractDataBox {

    private DPeptideMatch m_previousPeptideMatch = null;
    
    public DataBoxRsetPeptideSpectrumValues() {
        super(DataboxType.DataBoxRsetPeptideSpectrumValues);

        // Name of this databox
        m_typeName = "Spectrum Values";
        m_description = "Spectrum Values of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
        registerInParameter(inParameter);


    }

    @Override
    public void createPanel() {
        RsetPeptideSpectrumValuesPanel p = new RsetPeptideSpectrumValuesPanel();
        p.setName(m_typeName);
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
            ((RsetPeptideSpectrumValuesPanel) m_panel).setData(null);
            return;
        }

        
        boolean needToLoadData = ((!peptideMatch.isMsQuerySet()) ||
                                  (!peptideMatch.getMsQuery().isSpectrumFullySet()));

        // JPM.WART : look fo Spectrum table which will load same data
        if (needToLoadData) {
            AbstractDataBox previousBox = m_previousDataBox;
            while (previousBox != null) {
                if (previousBox instanceof DataBoxRsetPeptideSpectrum) {
                    needToLoadData = false;
                    break;
                }
                previousBox = previousBox.m_previousDataBox;
            }
        }
        
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

                    ((RsetPeptideSpectrumValuesPanel) m_panel).setData(peptideMatch);
                    
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
            ((RsetPeptideSpectrumValuesPanel) m_panel).setData(peptideMatch);
        }
    }
    private Long m_previousTaskId = null;
    
    

    
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(DMsQuery.class)) {
                DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);
                if (peptideMatch != null) {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    if (msQuery != null) {
                        return msQuery;
                    }
                }
            }
            if (parameterType.equals(DSpectrum.class)) {
                DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);
                if (peptideMatch != null) {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    if (msQuery != null) {
                        DSpectrum spectrum = msQuery.getDSpectrum();
                        if (spectrum != null) {
                            return spectrum;
                        }
                    }
                }
            }
        }
        return super.getData(getArray, parameterType);
    }

}
