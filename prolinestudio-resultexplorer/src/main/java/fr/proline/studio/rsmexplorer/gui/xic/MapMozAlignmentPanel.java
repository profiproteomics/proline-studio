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
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapTime;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.PlotBaseAbstract;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.pattern.xic.DataboxMapAlignment;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractLabelFreeMSParamsPanel;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTScatterPlot;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * map alignment panel for 1 dataset
 *
 * @author MB243701
 */
public class MapMozAlignmentPanel extends AbstractMapAlignmentPanel {

    //data to display
    private Map<Long, ProcessedMap> m_pMapById;


    public MapMozAlignmentPanel(DataboxMapAlignment dataBox) {
        super(dataBox," moz ");
    }

    @Override
    protected String getTitleLabel() {
        return "<html>Reference Map: <font color='RED'>&#x25A0;&nbsp;</font>"
                + "   map   &nbsp;, Alignment Mode : I/H) </html>";
    }

    @Override
    protected List<JButton> getMoreCloudButtons() {
        return new ArrayList<>();
    }

    @Override
    protected JPanel createConvertPanel() {
        JPanel moz2DeltaPanel = new JPanel();
        moz2DeltaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        moz2DeltaPanel.add(m_srcTimeValueTF);
        moz2DeltaPanel.add(new JLabel(" moz in "));
        m_sourceMapsCB.setName("cbSourceMaps");
        moz2DeltaPanel.add(m_sourceMapsCB);
        moz2DeltaPanel.add(new JLabel(" correspond to"));
        m_destValueTF = new JTextField(10);
        m_destValueTF.setName("tfDestTime");
        m_destValueTF.setEditable(false);
        moz2DeltaPanel.add(m_destValueTF);
        moz2DeltaPanel.add(new JLabel(" delta moz "));

        return moz2DeltaPanel;
    }


    @Override
    public void setData(QuantChannelInfo quantChannelInfo, List<ExtendedTableModelInterface> compareDataInterfaceList) {
        if (!this.m_isInitialized) {

            this.m_quantChannelInfo = quantChannelInfo;
            m_pMapByIndex = new HashMap<>();
            m_pMapById = new HashMap<>();

            String[] mapItems = new String[m_quantChannelInfo.getDataset().getMaps().size()];
            int i = 0;
            for (ProcessedMap map : m_quantChannelInfo.getDataset().getMaps()) {
                String mapTitle = m_quantChannelInfo.getMapTitle(map.getId());
                StringBuilder sb  = new StringBuilder();
                String mapColor = m_quantChannelInfo.getMapHtmlColor(map.getId());
                sb.append("<html><font color='").append(mapColor).append("'>&#x25A0;&nbsp;</font>");
                sb.append(mapTitle);
                sb.append("</html>");
                mapItems[i] = sb.toString();
                m_pMapByIndex.put(i, map);
                m_pMapById.put(map.getId(), map);
                i++;
            }


            StringBuilder sb = new StringBuilder();
            sb.append("<html> Alignment Smooting Mode : ");
            sb.append(this.getAlignmentSmoothingMethod());
            sb.append("</html>");
            m_labelTitle.setText(sb.toString());

            DefaultComboBoxModel sourceMapsModel = new DefaultComboBoxModel(mapItems);
            m_sourceMapsCB.setModel(sourceMapsModel);
            if (mapItems.length > 0) {
                sourceMapsModel.setSelectedItem(mapItems[0]);
            }
            setDataGraphic();
            this.m_isInitialized = true;
            repaint();
        }
    }

    /**
     * info to show above the map alignment curve
     *
     * @return
     */
    private String getAlignmentSmoothingMethod() {
        String method = "unknown";
        Map<String, Object> quantParams;
        try {
            quantParams = m_quantChannelInfo.getDataset().getQuantProcessingConfigAsMap();
            if (quantParams.containsKey(AbstractLabelFreeMSParamsPanel.ALIGNMENT_CONFIG)) {
                Map<String, Object> alignmentConfig = (Map<String, Object>) quantParams.get(AbstractLabelFreeMSParamsPanel.ALIGNMENT_CONFIG);
                method = (String) alignmentConfig.getOrDefault(AbstractLabelFreeMSParamsPanel.ALIGNMENT_SMOOTHING_METHOD_NAME, "unknown");
            }
            return method;
        } catch (Exception e) {
            return "unknown alignment method exception ";
        }
    }

    @Override
    protected Double getCorrespondingData(Double time, Long mapId) {
        logger.debug("calculate delta moz at RT " + time + " for mapId=" + mapId);
        Double calcDeltaMoz = Double.NaN;
        try {
            ProcessedMap pMap =  m_pMapById.get(mapId);
            if(pMap.getProcessedMapMozCalibration() != null && pMap.getProcessedMapMozCalibration().size()>0) {
                List<MapTime> mapMozs = pMap.getProcessedMapMozCalibration().get(0).getProcessedMapMozList();
                for(MapTime mapMoz : mapMozs){
                    if (Math.abs(mapMoz.getTime() - time) < THRESHOLD){
                        calcDeltaMoz = mapMoz.getDeltaValue();
                        break;
                    }
                }
            }
            logger.debug("...result= " + calcDeltaMoz);
        } catch (Exception e) {
            logger.error("Error while retrieving time in map alignment: " + e);
        }
        return calcDeltaMoz;
    }


    protected void setDataGraphic() {
        long mapIdSrc = getSelectedMapId(m_sourceMapsCB);
        ProcessedMap pMap = m_pMapById.get(mapIdSrc);

        String mapTitle = m_quantChannelInfo.getMapTitle(mapIdSrc);
        String title = "Map moz Alignment for " + mapTitle;
        Color color = m_quantChannelInfo.getMapColor(mapIdSrc);

        double crossAssignmentTimeTolerance = ((DataboxMapAlignment) this.m_dataBox).getCrossAssignmentTimeTolerance();
        double featureAlignmentTimeTolerance = ((DataboxMapAlignment) this.m_dataBox).getFeatureAlignmentTimeTolerance();

        if(pMap.getProcessedMapMozCalibration() != null && pMap.getProcessedMapMozCalibration().size()>0) {
            ExtendedTableModelInterface extendedTableModel = new MapTimeTableModel(pMap.getProcessedMapMozCalibration().get(0).getProcessedMapMozList(), color, title, mapTitle);

            PlotLinear alignmentCurve = new PlotLinear(m_alignmentGraphicPanel, extendedTableModel, null, PlotBaseAbstract.COL_X_ID, PlotBaseAbstract.COL_Y_ID);
            alignmentCurve.setPlotInformation(extendedTableModel.getPlotInformation());
            alignmentCurve.setStroke(3f);
            m_alignmentGraphicPanel.setPlot(alignmentCurve);
            if (this.m_isIonsCloudLoaded) {
                IonsRTTableModel cloudData = getCloudData(mapIdSrc);
                if (cloudData != null) {
                    int axisX = IonsRTTableModel.ELUTION_TIME_FROM_COL_INDEX;
                    int axisY =  IonsRTTableModel.DELTA_MOZ_COL_INDEX;
                    m_ionsScatterPlot = new IonsRTScatterPlot(m_alignmentGraphicPanel, cloudData, null, axisX, axisY);
                    m_ionsScatterPlot.showCrossAssignedIons(m_showHideCrossAssigned.getActionCommand().equals("HIDE"));
                    m_ionsScatterPlot.setColor(color);
                    m_ionsScatterPlot.setFeatureAlignmentTimeTolerance(featureAlignmentTimeTolerance);
                    if (m_zoomMode == CLOUD_VIEW_BEST_FIT) {
                        //set visible Min Max, the real Min Max are too large to show the alignment PlotLinear
                        double yMax = alignmentCurve.getYMax();
                        double yMin = alignmentCurve.getYMin();
                        m_ionsScatterPlot.setYMax(yMax + 2 * crossAssignmentTimeTolerance);
                        m_ionsScatterPlot.setYMin(yMin - 2 * crossAssignmentTimeTolerance);
                    }
                    m_alignmentGraphicPanel.addPlot(m_ionsScatterPlot);
                }
            }

        } else {
            m_alignmentGraphicPanel.clearPlotsWithRepaint();

        }
        m_alignmentGraphicPanel.repaint();

    }


}

