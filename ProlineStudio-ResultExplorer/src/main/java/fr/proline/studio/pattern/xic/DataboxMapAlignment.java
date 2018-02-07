/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.core.orm.uds.dto.DDataset;
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
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * databox for mapAlignment data
 * @author MB243701
 */
public class DataboxMapAlignment extends AbstractDataBox {
    
    private DDataset m_dataset;
    private QuantChannelInfo  m_quantChannelInfo;
        
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
        outParameter.addParameter(ExtendedTableModelInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);
    }

    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
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
    
    private List<MapTimePanel> getMapTimeTableModelList() {
        List<MapTimePanel> list = new ArrayList();
        for (MapAlignment mapAlignment : m_dataset.getMapAlignmentsFromMap(m_dataset.getAlnReferenceMapId())) {
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
                m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                ((MapAlignmentPanel) getDataBoxPanelInterface()).setData(m_quantChannelInfo, getCompareDataInterfaceList(), getCrossSelectionInterfaceList());
                
                setLoaded(loadingId);
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class); 
                }
            }
        };


        // ask asynchronous loading of data
        DatabaseLoadLcMSTask task = new DatabaseLoadLcMSTask(callback);
        task.initLoadAlignmentForXic(getProjectId(), m_dataset);
        registerTask(task);
    }
    
}
