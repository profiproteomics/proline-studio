package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSitePanelInterface;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSiteTablePanel;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class DataBoxPTMSitePeptides extends AbstractDataBox {

    private ResultSummary m_rsm;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");
    private long m_logTimeStart;
    private PTMSite m_currentPtmSite;
    private PTMDataset m_ptmDataset;

    public DataBoxPTMSitePeptides() {
        super(DataboxType.DataBoxPTMSitePeptides, DataboxStyle.STYLE_RSM);

        // Name of this databox
            m_typeName = "PTM Site's Peptides";
            m_description = "Peptides of a PTM Protein Sites ";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMSite.class, false);
        inParameter.addParameter(PTMDataset.class, false);
        registerInParameter(inParameter);

        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(DPeptideInstance.class, false);
        registerOutParameter(outParameter);
    }

    @Override
    public void createPanel() {
            PeptidesPTMSiteTablePanel p = new PeptidesPTMSiteTablePanel(false);
            p.setName(m_typeName);
            p.setDataBox(this);
            setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        PeptidesPTMSitePanelInterface panel = (PeptidesPTMSitePanelInterface) getDataBoxPanelInterface();

        m_currentPtmSite = (PTMSite) m_previousDataBox.getData(false, PTMSite.class);

        DPeptideInstance selectedInsts = (DPeptideInstance) m_previousDataBox.getData(false, DPeptideInstance.class);
        //m_logger.debug("selected peptide Match, ptm {}", m_selecedDPeptideMatch.getPeptide().getTransientData().getPeptideReadablePtmString().getReadablePtmString());
        m_ptmDataset = (PTMDataset) m_previousDataBox.getData(false, PTMDataset.class);
        m_rsm = m_ptmDataset.getDataset().getResultSummary();

        if (m_currentPtmSite == null) {
            panel.setData(null, null);
            return;
        }

        //m_logger.debug("DATA Changed : Update PTMSite Peptide WINDOWS. " + ptmSite.toString() + " data loaded " + ptmSite.isLoaded());
        if (m_currentPtmSite.isLoaded()) {
            m_previousTaskId = null;
            panel.setData(m_currentPtmSite, null);
            if (selectedInsts != null) {
                panel.setSelectedPeptide(selectedInsts);
            }
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
                    panel.setData(m_currentPtmSite, null);
                } else {
                    panel.setData(null, null);
                }

                setLoaded(loadingId);

                if (finished) {
                    m_logger.debug(" DataBoxPTMSitePeptides task#" + taskId + " in " + (System.currentTimeMillis() - m_logTimeStart) + " ms");
                    m_previousTaskId = null;
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };

        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initFillPTMSite(getProjectId(), m_rsm, m_currentPtmSite);
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
                PTMSite ptmSite = ((PeptidesPTMSitePanelInterface) getDataBoxPanelInterface()).getSelectedPTMSite();
                if (ptmSite != null) {
                    return ptmSite;
                }
            }

            if (parameterType.equals(DPeptideInstance.class)) {
                DPeptideInstance selectedParentPepInstance = ((PeptidesPTMSitePanelInterface) getDataBoxPanelInterface()).getSelectedPeptideInstance();
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

    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        if (parameterType != null && isList) {
            if (parameterType.equals(DPeptideInstance.class)) {
                List<DPeptideInstance> parentPepInstances = m_currentPtmSite.getParentPeptideInstances();
                return parentPepInstances;
            }
        }
        return super.getData(getArray, parameterType, isList);
    }

}
