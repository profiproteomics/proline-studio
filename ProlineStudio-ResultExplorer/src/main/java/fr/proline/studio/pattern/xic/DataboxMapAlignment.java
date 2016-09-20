/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.rsmexplorer.gui.xic.MapAlignmentPanel;
import fr.proline.studio.rsmexplorer.gui.xic.MapTimePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * databox for mapAlignment data
 * @author MB243701
 */
public class DataboxMapAlignment extends AbstractDataBox {
    
    private DDataset m_dataset;
    private  DQuantitationChannel[] m_quantitationChannelArray = null;
    private QuantChannelInfo  m_quantChannelInfo;
    
    private List<MapAlignment> m_mapAlignments;
    private List<MapAlignment> m_allMapAlignments;
    private List<ProcessedMap> m_allMaps;
    
    public DataboxMapAlignment() {
        super(DataboxType.DataBoxMapAlignment, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "XIC Map Alignment";
        m_description = "Map Alignment information for a XIC";

        // Register Possible in parameters
        // One Dataset 
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);
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
    
    
    private List<CompareDataInterface> getCompareDataInterfaceList() {
        List<CompareDataInterface> listCDI = new ArrayList();
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
    
    private List<MapTimePanel> getMapTimeTableModelList() {
        List<MapTimePanel> list = new ArrayList();
        for (MapAlignment mapAlignment : m_mapAlignments) {
            List<MapTime> listMapTime = mapAlignment.getMapTimeList();
            MapTimePanel mapTimePanel = new MapTimePanel();
            String fromMap = m_quantChannelInfo.getMapTitle(mapAlignment.getSourceMap().getId());
            String toMap = m_quantChannelInfo.getMapTitle(mapAlignment.getDestinationMap().getId());
            String title = "Map Alignment from "+ toMap +" (ref. "+fromMap+")";
            Color color = m_quantChannelInfo.getMapColor(mapAlignment.getDestinationMap().getId());
            mapTimePanel.setData((long) -1, mapAlignment, listMapTime, color, title, true);
            list.add(mapTimePanel);
        }
        return list;
    }
    
    
    @Override
    public void createPanel() {
        MapAlignmentPanel p = new MapAlignmentPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void setEntryData(Object data) {
        m_panel.addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();
        if (m_dataset == null){
            m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
        }
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
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
                ((MapAlignmentPanel) m_panel).setData(m_quantChannelInfo, m_mapAlignments, m_allMaps, m_allMapAlignments, getCompareDataInterfaceList(), getCrossSelectionInterfaceList());
                
                setLoaded(loadingId);
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class); 
                }
            }
        };


        // ask asynchronous loading of data
        m_mapAlignments = new ArrayList();
        m_allMapAlignments = new ArrayList();
        m_allMaps = new ArrayList();
        DatabaseLoadLcMSTask task = new DatabaseLoadLcMSTask(callback);
        task.initLoadAlignmentForXic(getProjectId(), m_dataset, m_mapAlignments, m_allMapAlignments, m_allMaps);
        registerTask(task);
    }
    
}
