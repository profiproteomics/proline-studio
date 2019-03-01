package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSiteTablePanel;


/**
 *
 * @author VD225637
 */
public class DataBoxPTMSitePeptides extends AbstractDataBox {

    private ResultSummary m_rsm;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");
    private long m_logTimeStart;

    public DataBoxPTMSitePeptides() {
        super(DataboxType.DataBoxPTMSitePeptides, DataboxStyle.STYLE_RSM);

        // Name of this databox
        m_typeName = "PTM Site's Peptides";
        m_description = "Peptides of a PTM Protein Sites ";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMSite.class, false);
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);

        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(DPeptideInstance.class, false);
        registerOutParameter(outParameter);
    }

    @Override
    public void createPanel() {
        //PeptidesPTMSiteTablePanel p = new PeptidesPTMSiteTablePanel();
        PeptidesPTMSiteTablePanel p = new PeptidesPTMSiteTablePanel(false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {

        final PTMSite ptmSite = (PTMSite) m_previousDataBox.getData(false, PTMSite.class);
        final ResultSummary rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
        m_rsm = rsm;

        if (ptmSite == null) {
            ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).setData(null, null);
            return;
        }

        //m_logger.debug("DATA Changed : Update PTMSite Peptide WINDOWS. " + ptmSite.toString() + " data loaded " + ptmSite.isLoaded());
        if (ptmSite.isLoaded()) {
            m_previousTaskId = null;
            ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).setData(ptmSite, null);
            propagateDataChanged(ExtendedTableModelInterface.class);
            return;
        }

        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).setData(ptmSite, null);
                } else {
                    ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).setData(null, null);
                }

                setLoaded(loadingId);

                if (finished) {
                    m_logger.info(" DataBoxPTMSitePeptides task#" + taskId + " in " + (System.currentTimeMillis() - m_logTimeStart) + " ms");
                    m_previousTaskId = null;
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };
        
        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initFillPTMSite(getProjectId(), rsm, ptmSite);
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);

    }

    private Long m_previousTaskId = null;

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {

            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }

            if (parameterType.equals(PTMSite.class)) {
                PTMSite ptmSite = ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).getSelectedPTMSite();
                if (ptmSite != null) {
                    return ptmSite;
                }
            }

            if (parameterType.equals(DPeptideInstance.class)) {
                DPeptideInstance selectedParentPepInstance = ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).getSelectedPeptideInstance();
                if (selectedParentPepInstance != null) {
                    return selectedParentPepInstance;
                }
            }

            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
        }

        return super.getData(getArray, parameterType);
    }

}
