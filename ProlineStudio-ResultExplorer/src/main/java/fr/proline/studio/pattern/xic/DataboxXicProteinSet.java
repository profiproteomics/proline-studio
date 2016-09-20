package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.ProteinQuantPanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicProteinSetPanel;
import fr.proline.studio.types.XicMode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class DataboxXicProteinSet extends AbstractDataBox {

    private DDataset m_dataset;
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantProteinSet> m_masterQuantProteinSetList;
    private DQuantitationChannel[] m_quantitationChannelArray = null;
    private boolean m_isXICMode = true;

    private List<MapAlignment> m_mapAlignments;
    private List<MapAlignment> m_allMapAlignments;
    private List<ProcessedMap> m_allMaps;

    public DataboxXicProteinSet() {
        super(DataboxType.DataboxXicProteinSet, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quanti Protein Sets";
        m_description = "All Protein Sets of a Quantitation";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        // One or Multiple ProteinSet
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinSet.class, true);
        outParameter.addParameter(DMasterQuantProteinSet.class, true);
        outParameter.addParameter(DDataset.class, false);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(QuantChannelInfo.class, false);
        outParameter.addParameter(XicMode.class, false);
        
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, false);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);

        
    }

    public boolean isXICMode() {
        return m_isXICMode;
    }

    public void setXICMode(boolean isXICMode) {
        m_isXICMode = isXICMode;
        m_style = (m_isXICMode) ? DataboxStyle.STYLE_XIC : DataboxStyle.STYLE_SC;
        if (m_panel != null) {
            m_panel.addSingleValue(new XicMode((isXICMode)));
        }
    }

    @Override
    public Long getRsetId() {
        if (m_dataset != null) {
            return null;
        }
        return m_dataset.getResultSetId();
    }

    @Override
    public Long getRsmId() {
        if (m_dataset != null) {
            return null;
        }
        return m_dataset.getResultSummaryId();
    }
    
    @Override
    public void createPanel() {
        XicProteinSetPanel p = new XicProteinSetPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
        
        m_panel.addSingleValue(new XicMode((m_isXICMode)));
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                if (subTask == null) {

                    AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                            // list quant Channels
                            List<DQuantitationChannel> listQuantChannel = new ArrayList();
                            if (m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()) {
                                DMasterQuantitationChannel masterChannel = m_dataset.getMasterQuantitationChannels().get(0);
                                listQuantChannel = masterChannel.getQuantitationChannels();
                            }
                            m_quantitationChannelArray = new DQuantitationChannel[listQuantChannel.size()];
                            listQuantChannel.toArray(m_quantitationChannelArray);
                            m_quantChannelInfo = new QuantChannelInfo(m_quantitationChannelArray);
                            m_quantChannelInfo.setAllMapAlignments(m_allMapAlignments);
                            m_quantChannelInfo.setMapAlignments(m_mapAlignments);
                            m_quantChannelInfo.setAllMaps(m_allMaps);
                            m_panel.addSingleValue(m_quantChannelInfo);

                            // proteins set 
                            //DMasterQuantProteinSet[] masterQuantProteinSetArray = new DMasterQuantProteinSet[m_masterQuantProteinSetList.size()];
                            //m_masterQuantProteinSetList.toArray(masterQuantProteinSetArray);
                            ((XicProteinSetPanel) m_panel).setData(taskId, m_quantitationChannelArray, m_masterQuantProteinSetList, m_isXICMode, finished);
                            if (finished) {
                                unregisterTask(task2Id);
                            }
                        }
                    };
                    // ask asynchronous loading of data
                    m_mapAlignments = new ArrayList();
                    m_allMapAlignments = new ArrayList();
                    m_allMaps = new ArrayList();
                    DatabaseLoadLcMSTask taskMap = new DatabaseLoadLcMSTask(mapCallback);
                    taskMap.initLoadAlignmentForXic(getProjectId(), m_dataset, m_mapAlignments, m_allMapAlignments, m_allMaps);
                    registerTask(taskMap);

                } else {
                    ((XicProteinSetPanel) m_panel).dataUpdated(subTask, finished);
                }
                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantProteinSetList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadProteinSets(getProjectId(), m_dataset, m_masterQuantProteinSetList);
        //Long taskId = task.getId();
        /*if (m_previousTaskId != null) {
         // old task is suppressed if it has not been already done
         AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
         }*/
        //m_previousTaskId = taskId;
        registerTask(task);

    }
    //private Long m_previousTaskId = null;

    /**
     * To be overriden if the modification in a following databox can lead to a
     * modificiation of the data of the current databox. (for instance,
     * disabling peptides -> modifications of protein set in the XIC View
     *
     * @param dataType
     */
    @Override
    public void dataMustBeRecalculated(Long rsetId, Long rsmId, Class dataType, ArrayList modificationsList, int reason) {
        if (m_dataset.getResultSetId() != rsetId) {
            return;
        }
        if (m_dataset.getResultSummaryId()!= rsmId) {
            return;
        }
        if (dataType.equals(DMasterQuantProteinSet.class)) {
            ((XicProteinSetPanel) m_panel).dataModified(modificationsList, reason);
        }
    }
    
    
    @Override
    public void setEntryData(Object data) {
        m_panel.addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(ResultSummary.class)) {
                return m_dataset.getResultSummary();
            }
            if (parameterType.equals(DDataset.class)) {
                return m_dataset;
            }
            if (parameterType.equals(QuantChannelInfo.class)) {
                return m_quantChannelInfo;
            }
            if (parameterType.equals(DProteinSet.class)) {
                return ((XicProteinSetPanel) m_panel).getSelectedProteinSet();
            }
            if (parameterType.equals(DMasterQuantProteinSet.class)) {
                return ((XicProteinSetPanel) m_panel).getSelectedMasterQuantProteinSet();
            }
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getCrossSelectionInterface();
            }
            if (parameterType.equals(XicMode.class)) {
                return new XicMode(isXICMode());
            }

        }
        return super.getData(getArray, parameterType);
    }

    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        if (parameterType != null && isList) {
            if (parameterType.equals(CompareDataInterface.class)) {
                return getCompareDataInterfaceList();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return getCrossSelectionInterfaceList();
            }
        }
        return super.getData(getArray, parameterType, isList);
    }

    @Override
    public String getFullName() {
        return m_dataset.getName() + " " + getTypeName();
    }

    private List<CompareDataInterface> getCompareDataInterfaceList() {
        List<CompareDataInterface> listCDI = new ArrayList();
        ProteinQuantPanel aProtPanel = getProteinQuantTableModelList();
        listCDI.add(aProtPanel.getGlobalTableModelInterface());
        return listCDI;
    }

    private List<CrossSelectionInterface> getCrossSelectionInterfaceList() {
        List<CrossSelectionInterface> listCSI = new ArrayList();
        ProteinQuantPanel aProtPanel = getProteinQuantTableModelList();
        listCSI.add(aProtPanel.getCrossSelectionInterface());
        return listCSI;
    }

    private ProteinQuantPanel getProteinQuantTableModelList() {
        ProteinQuantPanel aProtPanel = new ProteinQuantPanel();
        aProtPanel.setData(m_quantitationChannelArray, ((XicProteinSetPanel) m_panel).getSelectedMasterQuantProteinSet(), m_isXICMode);
        return aProtPanel;
    }

    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DMasterQuantProteinSet.class, DProteinSet.class};
        return classList;
    }

    @Override
    public String getImportantOutParameterValue() {
        DProteinSet p = (DProteinSet) getData(false, DProteinSet.class);
        if (p != null) {
            DProteinMatch pm = p.getTypicalProteinMatch();
            if (pm != null) {
                return pm.getAccession();
            }
        }
        return null;
    }
}
