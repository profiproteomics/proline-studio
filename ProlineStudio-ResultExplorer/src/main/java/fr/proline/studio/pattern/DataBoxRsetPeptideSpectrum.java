package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadSpectrumsTask;
import fr.proline.studio.dam.tasks.DatabaseObjectTreeTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.spectrum.RsetPeptideSpectrumPanel;
import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData;

/**
 * Databox for a Spectrum
 * @author JM235353
 */          
public class DataBoxRsetPeptideSpectrum extends AbstractDataBox {

    private DPeptideMatch m_previousPeptideMatch = null;
    private PeptideFragmentationData m_fragmentationData = null;
    
    public DataBoxRsetPeptideSpectrum() {
        super(DataboxType.DataBoxRsetPeptideSpectrum, DataboxStyle.STYLE_RSET);

        // Name of this databox
        m_typeName = "Spectrum";
        m_description = "Spectrum of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DMsQuery.class, false);
        outParameter.addParameter(DSpectrum.class, false);
        outParameter.addParameter(PeptideFragmentationData.class, false);
        registerOutParameter(outParameter);
    }

    @Override
    public void createPanel() {
        RsetPeptideSpectrumPanel p = new RsetPeptideSpectrumPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        final DPeptideMatch peptideMatch = (DPeptideMatch) m_previousDataBox.getData(false, DPeptideMatch.class);

        if (m_previousPeptideMatch == peptideMatch) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;
        m_fragmentationData = null;
        
        if (peptideMatch == null) {
            ((RsetPeptideSpectrumPanel) getDataBoxPanelInterface()).setData(null, null);
            return;
        }

        boolean needToLoadData = ((!peptideMatch.isMsQuerySet()) ||
                                  (!peptideMatch.getMsQuery().isSpectrumFullySet()));

        if (needToLoadData) {

            final int loadingId = setLoading();

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

        final DataBoxRsetPeptideSpectrum _databox = this;

        final ObjectTree[] objectTreeResult = new ObjectTree[1];
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                ObjectTree objectTree = objectTreeResult[0];
                m_fragmentationData = (objectTree != null) ? new PeptideFragmentationData(peptideMatch, objectTree) : null;

                ((RsetPeptideSpectrumPanel) getDataBoxPanelInterface()).setData(peptideMatch, m_fragmentationData);

                if (m_fragmentationData != null) {
                    _databox.propagateDataChanged(PeptideFragmentationData.class);
                }
                
                if (finished) {
                    unregisterTask(taskId);
                }

            }
        };

        // Load data if needed asynchronously
        DatabaseObjectTreeTask task = new DatabaseObjectTreeTask(callback, getProjectId(), peptideMatch, objectTreeResult);
        Long taskId = task.getId();
        if (m_previousFragmentationTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousFragmentationTaskId);
        }
        m_previousFragmentationTaskId = taskId;
        registerTask(task);

    }
    private Long m_previousFragmentationTaskId = null;
    
    
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
            if (parameterType.equals(PeptideFragmentationData.class)) {
                return m_fragmentationData;
            }
        }
        return super.getData(getArray, parameterType);
    }

}
