/* 
 * Copyright (C) 2019
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

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.lcms.ProcessedMapMozCalibration;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractLabelFreeMSParamsPanel;
import fr.proline.studio.rsmexplorer.gui.xic.*;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<Long, IonsRTTableModel> m_ionsRTBySourceMapId;
    private double m_crossAssignmentTimeTolerance = AbstractLabelFreeMSParamsPanel.DEFAULT_CA_FEATMAP_RTTOL_VALUE;
    private double m_featureAlignmentTimeTolerance = AbstractLabelFreeMSParamsPanel.DEFAULT_ALIGN_FEATMAP_TIMETOL_VALUE;
    private Long m_paramTaskId;
    private boolean m_isCloudLoaded;
    private boolean m_isCloudTaskAsked;
    private boolean m_isMoZAlignment;

    public DataboxMapAlignment() {
        this(false);
    }

    public DataboxMapAlignment(boolean isMoZAlignment) {
        super(DataboxType.DataBoxMapAlignment, DataboxStyle.STYLE_XIC);
        m_isMoZAlignment = isMoZAlignment;

        // Name of this databox
        m_typeName = m_isMoZAlignment ?  "Map moz Alignment Plot" : "Map Alignment Plot";
        m_description = m_isMoZAlignment ? "Graphical display of XIC Map moz Alignment." : "Graphical display of XIC Map Alignment.";

        m_ionsRTBySourceMapId = new HashMap<>();
        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DDataset.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();

        outParameter.addParameter(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);

        registerOutParameter(outParameter);
        
        m_isCloudLoaded = false;
        m_isCloudTaskAsked = false;
    }

    public double getCrossAssignmentTimeTolerance() {
        return m_crossAssignmentTimeTolerance;
    }

    public double getFeatureAlignmentTimeTolerance() {
        return m_featureAlignmentTimeTolerance;
    }

    /**
     * this.dataChanged() callback in run() propagateDataChanged will propagate
     * to DataboxMultiGraphics.dataChanged(), the DataboxMultiGraphics will call
     * m_previousDataBox.getData, his m_previousDataBox is this
     * DataboxMapAlignment used by DataboxMultiGraphics.dataChanged
     *
     * @param parameterType
     * @param parameterSubtype
     * @return
     */
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {

        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return getCompareDataInterfaceList();
                }
            }
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }

    private List<ExtendedTableModelInterface> getCompareDataInterfaceList() {
        List<ExtendedTableModelInterface> listCDI = new ArrayList<>();

        if(m_isMoZAlignment){
            for(ProcessedMap map : m_dataset.getMaps()) {
                List<ProcessedMapMozCalibration> allMozCalib = new ArrayList<>(map.getProcessedMapMozCalibration());

                String mapTitle = m_quantChannelInfo.getMapTitle(map.getId());
                String title = "moz Alignment for "+ mapTitle;

                for (ProcessedMapMozCalibration mapMozAlignment : allMozCalib) {
                    Color color = m_quantChannelInfo.getMapColor(map.getId());
                    MapTimeTableModel model = new MapTimeTableModel(mapMozAlignment.getProcessedMapMozList(), color, title, mapTitle);
                    listCDI.add(model);
                }
            }
        } else {
            for (MapAlignment mapAlignment : m_dataset.getMapAlignmentsFromMap(m_dataset.getAlnReferenceMapId())) {
                String fromMap = m_quantChannelInfo.getMapTitle(mapAlignment.getSourceMap().getId());
                String toMap = m_quantChannelInfo.getMapTitle(mapAlignment.getDestinationMap().getId());
                String title = "Map Alignment from " + fromMap + " (to " + toMap + ")";
                Color color = m_quantChannelInfo.getMapColor(mapAlignment.getDestinationMap().getId());
                MapTimeTableModel model = new MapTimeTableModel(mapAlignment.getMapTimeList(), color, title, fromMap, toMap);  //set mapAlignemnt curve data with its AxisX
                listCDI.add(model);
            }
        }
        return listCDI;
    }


    @Override
    public void createPanel() {
        if(m_isMoZAlignment){
            MapMozAlignmentPanel p = new MapMozAlignmentPanel(this);
            p.setName(m_typeName);
            setDataBoxPanelInterface(p);

        } else {
            MapAlignmentPanel p = new MapAlignmentPanel(this);
            p.setName(m_typeName);
            setDataBoxPanelInterface(p);
        }
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
                    if(m_isMoZAlignment)
                        ((MapMozAlignmentPanel) getDataBoxPanelInterface()).setData(m_quantChannelInfo, getCompareDataInterfaceList());
                    else
                        ((MapAlignmentPanel) getDataBoxPanelInterface()).setData(m_quantChannelInfo, getCompareDataInterfaceList());

                    if (finished) {
                        addDataChanged(ExtendedTableModelInterface.class, ParameterSubtypeEnum.LIST_DATA);
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
        
        if (m_isCloudLoaded) {
            if(m_isMoZAlignment)
                ((MapMozAlignmentPanel) getDataBoxPanelInterface()).setAlignmentCloud();
            else
                ((MapAlignmentPanel) getDataBoxPanelInterface()).setAlignmentCloud();
        } else {
            //avoid multiple call when the cloud is not loaded
            if (this.m_isCloudTaskAsked) {
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
                                extractTimeToleranceParameters();
                                if(m_isMoZAlignment)
                                    ((MapMozAlignmentPanel) getDataBoxPanelInterface()).setAlignmentCloud();
                                else
                                    ((MapAlignmentPanel) getDataBoxPanelInterface()).setAlignmentCloud();
                            }
                        }
                    }
                }

            };

            m_masterQuantPeptideIonList = new ArrayList<>();
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
     *  the cross assignment time tolerance
     */
    private void extractTimeToleranceParameters() {
        Double time;
        try {
            Map<String, Object> quantParams = this.m_dataset.getQuantProcessingConfigAsMap();
            if (quantParams.containsKey("cross_assignment_config")) {
                Map<String, Object> crossAssignmentConfig = (Map<String, Object>) quantParams.get("cross_assignment_config");
                Map<String, Object> ftMappingParams = (Map<String, Object>) crossAssignmentConfig.getOrDefault("ft_mapping_params", new HashMap<>());
                time = Double.valueOf((String) ftMappingParams.get("time_tol"));
                if (time != null) {
                    m_crossAssignmentTimeTolerance = time;
                }
            }

            if (quantParams.containsKey("alignment_config")) {
                Map<String, Object> alignmentConfig = (Map<String, Object>) quantParams.get("alignment_config");
                if (alignmentConfig.containsKey("ft_mapping_method_params")) {
                    Map<String, Object> featureMappingConfig = (Map<String, Object>) alignmentConfig.get("ft_mapping_method_params");
                    time = Double.valueOf((String) featureMappingConfig.get("time_tol"));
                    if (time != null) {
                        m_featureAlignmentTimeTolerance = time;
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("error while get cross assignment or alignment RT tolerance " + ex);

        }
    }

    /**
     * create a IonsRTTableModel representing ion's retention time and deltaTime from the
     * source map to the other maps.
     *
     * @param mapIdFrom the source (from) Map id.
     */
    public IonsRTTableModel getPeptideCloud(long mapIdFrom) {
        IonsRTTableModel listETI;
        listETI = m_ionsRTBySourceMapId.get(mapIdFrom);
        if (listETI == null) {
            listETI = createIonsRTTableModel(mapIdFrom);
            m_ionsRTBySourceMapId.put(mapIdFrom, listETI);

        }
        return listETI;
    }

    private IonsRTTableModel createIonsRTTableModel(long mapIdFrom) {
        Map<Long, String> mapTitleByRsmId = new HashMap<>(); // Map<rsmId,MapTitleName>
        Map<Long, Long> rsmIdByMapId = new HashMap<>();       // Map<MapId,resultSummaryId>
        List<ProcessedMap> processMapList = m_quantChannelInfo.getDataset().getMaps();
        long[] rsmIdArray = new long[processMapList.size()];
        int index = 1;
        for (ProcessedMap map : processMapList) {
            Long mapId = map.getId();
            String mapTitle = m_quantChannelInfo.getMapTitle(mapId);
            Long rsmId = m_quantChannelInfo.getQuantChannelForMap(mapId).getId();

            rsmIdByMapId.put(mapId, rsmId);
            mapTitleByRsmId.put(rsmId, mapTitle);
            if (mapId == mapIdFrom) {
                rsmIdArray[0] = rsmId;
            } else {
                rsmIdArray[index] = rsmId;
                index++;
            }
        }

        return new IonsRTTableModel(m_masterQuantPeptideIonList, rsmIdByMapId, mapTitleByRsmId, rsmIdArray);
    }

}
