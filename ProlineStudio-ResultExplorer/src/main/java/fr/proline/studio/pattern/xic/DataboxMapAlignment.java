/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern.xic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.MapAlignmentPanel;
import fr.proline.studio.rsmexplorer.gui.xic.MapTimePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractGenericQuantParamsPanel;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.RTCompareTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * databox for mapAlignment data
 *
 * @author MB243701
 */
public class DataboxMapAlignment extends AbstractDataBox {

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private DDataset m_dataset;

    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList;

    private Map<Long, RTCompareTableModel> m_compareRT2Maps;
    private double m_RT_Tolerance;
    private Long m_paramTaskId;

    public DataboxMapAlignment() {
        super(DataboxType.DataBoxMapAlignment, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "XIC Map Alignment";
        m_description = "Map Alignment information for a XIC";

        m_compareRT2Maps = new HashMap<Long, RTCompareTableModel>();
        // Register Possible in parameters
        // One Dataset 
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ExtendedTableModelInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);
        m_RT_Tolerance = AbstractGenericQuantParamsPanel.DEFAULT_CA_FEATMAP_RTTOL_VALUE;
    }

    public double getRT_Tolerance() {
        return m_RT_Tolerance;
    }

    /**
     * this.dataChanged() callback in run() propagateDataChanged will propagate
     * to DataboxMultiGraphics.dataChanged(), the DataboxMultiGraphics will call
     * m_previousDataBox.getData, his m_previousDataBox is this
     * DataboxMapAlignment used by DataboxMultiGraphics.dataChanged
     *
     * @param getArray
     * @param parameterType
     * @param isList
     * @return
     */
    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        //logger.debug("get Data");
        if (parameterType != null && isList) {
            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return getCompareDataInterfaceList();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return getCrossSelectionInterfaceList();
            }
        }
        return super.getData(getArray, parameterType, isList);
    }

    private List<ExtendedTableModelInterface> getCompareDataInterfaceList() {
        List<ExtendedTableModelInterface> listCDI = new ArrayList();
        List<MapTimePanel> listMapTimePanel = getMapTimeTableModelList();
        for (MapTimePanel mapTimePanel : listMapTimePanel) {
            listCDI.add(mapTimePanel.getGlobalTableModelInterface());
        }
        return listCDI;
    }

    private List<CrossSelectionInterface> getCrossSelectionInterfaceList() {
        List<CrossSelectionInterface> listCSI = new ArrayList();
        List<MapTimePanel> listMapTimePanel = getMapTimeTableModelList();
        for (MapTimePanel mapTimePanel : listMapTimePanel) {
            listCSI.add(mapTimePanel.getCrossSelectionInterface());
        }
        return listCSI;
    }

    /**
     * get new MapAligments and create MapTimePanel for each MapAlignement
     *
     * @return
     */
    private List<MapTimePanel> getMapTimeTableModelList() {
        //logger.debug(" getMapTimeTableModelList");
        List<MapTimePanel> list = new ArrayList();
        for (MapAlignment mapAlignment : m_dataset.getMapAlignmentsFromMap(m_dataset.getAlnReferenceMapId())) {//only these from reference map
            //for (MapAlignment mapAlignment : m_dataset.getMapAlignments()) {//all maps
            List<MapTime> listMapTime = mapAlignment.getMapTimeList();
            MapTimePanel mapTimePanel = new MapTimePanel();
            String fromMap = m_quantChannelInfo.getMapTitle(mapAlignment.getSourceMap().getId());
            String toMap = m_quantChannelInfo.getMapTitle(mapAlignment.getDestinationMap().getId());
            String title = "Map Alignment from " + fromMap + " (to " + toMap + ")";
            Color color = m_quantChannelInfo.getMapColor(mapAlignment.getDestinationMap().getId());
            mapTimePanel.setData((long) -1, mapAlignment, listMapTime, color, title, true, fromMap);  //set mapAlignemnt curve data with its AxisX
            list.add(mapTimePanel);
        }
        return list;
    }

    @Override
    public void createPanel() {
        MapAlignmentPanel p = new MapAlignmentPanel(this);
        p.setName(m_typeName);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;

        dataChanged();
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();

        if (m_dataset == null) {
            m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);

        }

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                setLoaded(loadingId);
                if (finished){
                    unregisterTask(taskId);
                }
                // do nothing, if juste de paramTask finished
                if (taskId != m_paramTaskId) {
                    m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                    ((MapAlignmentPanel) getDataBoxPanelInterface()).setData(m_quantChannelInfo, getCompareDataInterfaceList(), getCrossSelectionInterfaceList());

                    if (finished) {
                        propagateDataChanged(ExtendedTableModelInterface.class);
                        //if all task loaded, then execute the first Alignement Cloud
                        if (DataboxMapAlignment.this.isLoaded()) {
                            m_RT_Tolerance = getTimeTol();
                            ((MapAlignmentPanel) DataboxMapAlignment.this.getPanel()).setAlignmentCloud();
                        }
                    }
                }
            }

        };

        // ask asynchronous loading of data
        DatabaseLoadLcMSTask taskMapAlignment = new DatabaseLoadLcMSTask(callback);
        taskMapAlignment.initLoadAlignmentForXic(getProjectId(), m_dataset);

        DatabaseLoadXicMasterQuantTask taskParameter = new DatabaseLoadXicMasterQuantTask(callback);
        taskParameter.initLoadQuantChannels(getProjectId(), m_dataset);
        m_paramTaskId = taskParameter.getId();//this task is short, and it will be done at first
        
        m_masterQuantPeptideIonList = new ArrayList();
        DatabaseLoadXicMasterQuantTask taskPeptideCloud = new DatabaseLoadXicMasterQuantTask(callback);
        taskPeptideCloud.initLoadPeptideIons(this.getProjectId(), m_dataset, m_masterQuantPeptideIonList);

        registerTask(taskMapAlignment);
        registerTask(taskParameter);
        registerTask(taskPeptideCloud);
    }

    private double getTimeTol() {
        Double time = AbstractGenericQuantParamsPanel.DEFAULT_CA_FEATMAP_RTTOL_VALUE;
        try {
            Map<String, Object> quantParams = this.m_dataset.getQuantProcessingConfigAsMap();
            if (quantParams.containsKey("cross_assignment_config")) {
                Map<String, Object> crossAssignmentConfig = (Map<String, Object>) quantParams.get("cross_assignment_config");
                Map<String, Object> ftMappingParams = (Map<String, Object>) crossAssignmentConfig.getOrDefault("ft_mapping_params", new HashMap<>());

                time = (Double) ftMappingParams.get("time_tol");
                if (time == null) {
                    time = AbstractGenericQuantParamsPanel.DEFAULT_CA_FEATMAP_RTTOL_VALUE;
                }
            }
        } catch (Exception ex) {
            logger.error("error while get Tolerence RT Time " + ex);

        }
        return time;
    }

    /**
     * create a TableModel for PlotScatt
     *
     * @param mapFrom
     * @param mapTo
     */
    public RTCompareTableModel getPeptideCloud(long mapIdFrom) {
        RTCompareTableModel listETI;
        listETI = m_compareRT2Maps.get(mapIdFrom);
        if (listETI == null) {
            listETI = createMapRTCompareTableModel(mapIdFrom);
            m_compareRT2Maps.put(mapIdFrom, listETI);

        }
        return listETI;
    }

    private RTCompareTableModel createMapRTCompareTableModel(long mapIdFrom) {
        Map<Long, String> idNameMap = new HashMap<Long, String>(); //Map<rsmId,MapTitleName>
        Map<Long, Long> idMap = new HashMap<Long, Long>();//Map<MapId,resultSummaryId>
        List<ProcessedMap> processMapList = m_quantChannelInfo.getDataset().getMaps();
        long[] rsmIdArray = new long[processMapList.size()];
        int index = 1;
        for (ProcessedMap map : processMapList) {
            Long mapId = map.getId();
            String mapTitle = m_quantChannelInfo.getMapTitle(mapId); //by example F083069

            Long rsmId = m_quantChannelInfo.getQuantChannelForMap(mapId).getId();//resultSummayId

            idMap.put(mapId, rsmId);
            idNameMap.put(rsmId, mapTitle);
            if (mapId == mapIdFrom) {
                rsmIdArray[0] = rsmId;
            } else {
                rsmIdArray[index] = rsmId;
                index++;
            }
        }

        RTCompareTableModel cloud = new RTCompareTableModel(m_masterQuantPeptideIonList, idMap, idNameMap, rsmIdArray);
        return cloud;
    }

}
