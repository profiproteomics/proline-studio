/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
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
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.xic.MapAlignmentPanel;
import fr.proline.studio.rsmexplorer.gui.xic.MapTimePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractLabelFreeMSParamsPanel;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * databox for mapAlignment data
 *
 * @author MB243701
 */
public class DataboxMapAlignment extends AbstractDataBox {

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.XIC.alignment");
    private double m_logStartTime, m_logCloudStartTime;
    private DDataset m_dataset;

    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList;

    private Map<Long, IonsRTTableModel> m_compareRT2Maps;
    private double m_RT_Tolerance;
    private Long m_paramTaskId;
    private boolean m_isCloudLoaded;
    private boolean m_isCloudTaskAsked;
    
    public DataboxMapAlignment() {
        super(DataboxType.DataBoxMapAlignment, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Map Alignment Plot";
        m_description = "Graphical display of XIC Map Alignment.";

        m_compareRT2Maps = new HashMap<Long, IonsRTTableModel>();
        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DDataset.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        
        outParameter.addParameter(ExtendedTableModelInterface.class);
        outParameter.addParameter(CrossSelectionInterface.class);
        
        registerOutParameter(outParameter);
        
        m_RT_Tolerance = 0.0;

        m_isCloudLoaded = false;
        m_isCloudTaskAsked = false;
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
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {

        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return getCompareDataInterfaceList();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return getCrossSelectionInterfaceList();
                }
            }
        }
        return super.getDataImpl(parameterType, parameterSubtype);
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
            m_dataset = (DDataset) getData(DDataset.class);
        }

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                setLoaded(loadingId);
                if (finished) {
                    logger.debug ( "DataboxMapAlignment task Id =" + taskId + " finished during " + (System.currentTimeMillis() - m_logStartTime) + " TimeMillis");

                    unregisterTask(taskId);
                }
                // do nothing, if only de paramTask finished
                if (taskId != m_paramTaskId) {
                    m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                    ((MapAlignmentPanel) getDataBoxPanelInterface()).setData(m_quantChannelInfo, getCompareDataInterfaceList(), getCrossSelectionInterfaceList());

                    if (finished) {
                        addDataChanged(ExtendedTableModelInterface.class);
                        propagateDataChanged();
                    }
                }
            }

        };
        DatabaseLoadXicMasterQuantTask taskParameter = new DatabaseLoadXicMasterQuantTask(callback);
        taskParameter.initLoadQuantChannels(getProjectId(), m_dataset);
        // ask asynchronous loading of data
        DatabaseLoadLcMSTask taskMapAlignment = new DatabaseLoadLcMSTask(callback);
        taskMapAlignment.initLoadAlignmentForXic(getProjectId(), m_dataset);

        m_paramTaskId = taskParameter.getId();//this task is short, and it will be done at first
        m_logStartTime = System.currentTimeMillis();
        logger.debug(this.getClass().getName() + " DatabaseLoadXicMasterQuantTask taskParameter Id =" + m_paramTaskId + " registered");
        logger.debug(this.getClass().getName() + " DatabaseLoadLcMSTask taskMapAlignment Id =" + taskMapAlignment.getId() + " registered");
        registerTask(taskParameter);
        registerTask(taskMapAlignment);
    }

    /**
     * load peptideIon task is separated from the above task.
     */
    public void loadCloud() {
        
        if (m_isCloudLoaded == true) {
            ((MapAlignmentPanel) super.getDataBoxPanelInterface()).setAlignmentCloud();
        } else {
            //avoid multiple call when the cloud is not loaded
            if (this.m_isCloudTaskAsked == true) {
                return;
            }

            m_isCloudTaskAsked = true;
            final int loadingId = setLoading();

            if (m_dataset == null) {
                m_dataset = (DDataset) getData(DDataset.class);
            }
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    // wait for the whole task to complete
                    if (taskId != m_paramTaskId) {
                        if (finished) {
                            setLoaded(loadingId);
                            unregisterTask(taskId);
                            logger.debug("DataboxMapAlignment task Id =" + taskId + " finished during " + (System.currentTimeMillis() - m_logCloudStartTime) + " TimeMillis");

                            //if all task loaded, then execute the first Alignement Cloud
                            if (DataboxMapAlignment.this.isLoaded()) {
                                m_isCloudLoaded = true;
                                m_RT_Tolerance = getTimeTol();
                                ((MapAlignmentPanel) DataboxMapAlignment.this.getPanel()).setAlignmentCloud();
                            }
                        }
                    }
                }

            };

            m_masterQuantPeptideIonList = new ArrayList();
            DatabaseLoadXicMasterQuantTask taskPeptideCloud = new DatabaseLoadXicMasterQuantTask(callback);
            taskPeptideCloud.initLoadPeptideIons(this.getProjectId(), m_dataset, m_masterQuantPeptideIonList);
            m_logCloudStartTime = System.currentTimeMillis();
            logger.debug(this.getClass().getName() + " DatabaseLoadLcMSTask taskPeptideCloud Id =" + taskPeptideCloud.getId() + " registered");
            registerTask(taskPeptideCloud);
        }

    }

    /**
     * from the m_dataset, extact the RT tolerance
     *
     * @return
     */
    private double getTimeTol() {
        Double time = AbstractLabelFreeMSParamsPanel.DEFAULT_CA_FEATMAP_RTTOL_VALUE;
        try {
            Map<String, Object> quantParams = this.m_dataset.getQuantProcessingConfigAsMap();
            if (quantParams.containsKey("cross_assignment_config")) {
                Map<String, Object> crossAssignmentConfig = (Map<String, Object>) quantParams.get("cross_assignment_config");
                Map<String, Object> ftMappingParams = (Map<String, Object>) crossAssignmentConfig.getOrDefault("ft_mapping_params", new HashMap<>());
                time = Double.valueOf((String) ftMappingParams.get("time_tol"));
                if (time == null) {
                    time = AbstractLabelFreeMSParamsPanel.DEFAULT_CA_FEATMAP_RTTOL_VALUE;
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
    public IonsRTTableModel getPeptideCloud(long mapIdFrom) {
        IonsRTTableModel listETI;
        listETI = m_compareRT2Maps.get(mapIdFrom);
        if (listETI == null) {
            listETI = createMapRTCompareTableModel(mapIdFrom);
            m_compareRT2Maps.put(mapIdFrom, listETI);

        }
        return listETI;
    }

    private IonsRTTableModel createMapRTCompareTableModel(long mapIdFrom) {
        Map<Long, String> idNameMap = new HashMap<>(); // Map<rsmId,MapTitleName>
        Map<Long, Long> idMap = new HashMap<>();       // Map<MapId,resultSummaryId>
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

        IonsRTTableModel cloud = new IonsRTTableModel(m_masterQuantPeptideIonList, idMap, idNameMap, rsmIdArray);
        return cloud;
    }

}
