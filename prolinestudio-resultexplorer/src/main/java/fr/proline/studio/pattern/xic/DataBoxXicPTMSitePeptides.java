package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptidesPTMSitePanel;
import java.util.ArrayList;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.PeptideTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class DataBoxXicPTMSitePeptides extends AbstractDataBox {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private ResultSummary m_rsm;
    private DDataset m_dataset;

    private PTMSite m_currentPtmSite = null;

    public DataBoxXicPTMSitePeptides() {
        super(DataboxType.DataBoxXICPTMSitePeptides, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quantification : PTM Site's Peptides";
        m_description = "Quantification of all Peptides of a PTM Protein Sites";

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
        XicPeptidesPTMSitePanel p = new XicPeptidesPTMSitePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    private Long m_previousTaskId = null;

    @Override
    public void dataChanged() {

        m_currentPtmSite = (PTMSite) m_previousDataBox.getData(false, PTMSite.class);
        m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
        m_rsm = m_dataset.getResultSummary();

        if (m_currentPtmSite == null) {
            ((XicPeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(null);
            return;
        }
        ptmSiteChanged(m_currentPtmSite);
    }

    private void ptmSiteChanged(PTMSite ptmSite) {

        if (ptmSite.isLoaded()) {
            m_previousTaskId = null;

            loadXicData(ptmSite, -1);
//            setPanelData(ptmSite);
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
                loadXicData(ptmSite, loadingId);
            }
        };

        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initFillPTMSite(getProjectId(), m_rsm, ptmSite);
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);
    }

    //Load XIC Information    
    private DQuantitationChannel[] m_quantitationChannelArray = null;
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;

    private void loadXicData(PTMSite ptmSite, int startedLoadingId) {

        m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
        m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);

        final int loadingId = (startedLoadingId <= 0) ? setLoading() : startedLoadingId;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    m_quantitationChannelArray = m_quantChannelInfo.getQuantChannels();
                    ((XicPeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(taskId, m_quantitationChannelArray, m_masterQuantPeptideList, ptmSite, finished);
                } else {
                    ((XicPeptidesPTMSitePanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }
                setLoaded(loadingId);

                if (finished) {
                    m_previousTaskId = null;
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadPeptides(getProjectId(), m_dataset, ptmSite.getPeptideInstanceIds(), m_masterQuantPeptideList, true);

        registerTask(task);

    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {

            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }

            if (parameterType.equals(PTMSite.class)) {
                return m_currentPtmSite;
            }
            if (parameterType.equals(DPeptideInstance.class)) {

                DPeptideInstance selectedParentPepInstance = ((XicPeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPeptideInstance();
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

    /**
     * a list of PeptideTableModel will be retrived by MultigraphiquePanel to
     * show peptide-channel abundance
     *
     * @param getArray
     * @param parameterType
     * @param isList
     * @return
     */
    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        if (parameterType != null && isList) {
            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return getTableModelInterfaceList();
            }
        } else if (parameterType.equals(XicAbundanceProteinTableModel.class)) {
            return this.getProteinAbundanceTableModel();

        }
        return super.getData(getArray, parameterType, isList);
    }

    private List<ExtendedTableModelInterface> getTableModelInterfaceList() {
        List<ExtendedTableModelInterface> list = new ArrayList();
        if (m_masterQuantPeptideList != null) {
            for (DMasterQuantPeptide quantPeptide : m_masterQuantPeptideList) {
                PeptideTableModel peptideTableModel = new PeptideTableModel(null);
                peptideTableModel.setData(m_quantChannelInfo.getQuantChannels(), quantPeptide, true);
                list.add(peptideTableModel);
            }
        }
        return list;
    }

    private XicAbundanceProteinTableModel getProteinAbundanceTableModel() {
        XicAbundanceProteinTableModel protTableModel = new XicAbundanceProteinTableModel();
        if (m_quantChannelInfo != null) {
            protTableModel.setData(m_quantChannelInfo.getQuantChannels(), m_currentPtmSite.getMasterQuantProteinSet());
        }
        return protTableModel;

    }

}
