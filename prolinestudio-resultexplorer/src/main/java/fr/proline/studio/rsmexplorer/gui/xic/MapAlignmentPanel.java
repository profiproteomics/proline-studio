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
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.studio.Exceptions;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.tasks.xic.MapAlignmentConverter;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.*;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.xic.DataboxMapAlignment;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractLabelFreeMSParamsPanel;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTScatterPlot;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTTableModel;
import fr.proline.studio.table.BeanTableModel;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.IconManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;

/**
 * map alignment panel for 1 dataset
 *
 * @author MB243701
 */
public class MapAlignmentPanel extends AbstractMapAlignmentPanel {

    //Data to display
    private List<MapAlignment> m_allMapAlignments;
    private long m_referenceMapId;

    //Second map
    private JComboBox m_destMapsCB;

    //Loess Specific
    private JButton m_addLoessCurveBtn;
    private JButton m_removeLoessCurveBtn;


    public MapAlignmentPanel(DataboxMapAlignment dataBox) {
        super(dataBox, "time");
        m_referenceMapId = 0;
    }

    @Override
    protected String getTitleLabel() {
        return  "<html>Reference Map: <font color='RED'>&#x25A0;&nbsp;</font>"
                + "   map   &nbsp;, Alignment Mode : I/H) </html>";
    }

    @Override
    protected List<JButton> getMoreCloudButtons() {
        List<JButton> bts = new ArrayList<>();
        m_addLoessCurveBtn = new JButton();
        m_addLoessCurveBtn.setIcon(IconManager.getIcon(IconManager.IconType.ADD_LOESS_CURVE));
        m_addLoessCurveBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_addLoessCurveBtn.setToolTipText("Fit a Loess smooth curve to ions scatter plot");
        m_addLoessCurveBtn.addActionListener(e -> computeLoess());
        bts.add(m_addLoessCurveBtn);

        m_removeLoessCurveBtn = new JButton();
        m_removeLoessCurveBtn.setIcon(IconManager.getIcon(IconManager.IconType.REMOVE_LOESS_CURVE));
        m_removeLoessCurveBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_removeLoessCurveBtn.setToolTipText("Clear Loess fit curve ");
        m_removeLoessCurveBtn.addActionListener(e ->  {
            //force cloud reload. TODO : find a better way to remove loess curves
            ((DataboxMapAlignment) m_dataBox).loadCloud();
            m_removeLoessCurveBtn.setEnabled(false);
        });
        bts.add(m_removeLoessCurveBtn);
        return bts;
    }

    @Override
    protected JPanel createConvertPanel() {

        m_destMapsCB = new JComboBox();
        m_destMapsCB.addActionListener(e -> {
            convertTime();
            setDataGraphic();
        });
        m_destMapsCB.setName("cbDestMaps");

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        timePanel.add(m_srcTimeValueTF);
        JLabel label0 = new JLabel("(min) in ");
        timePanel.add(label0);
        m_sourceMapsCB.setName("cbSourceMaps");
        timePanel.add(m_sourceMapsCB);
        JLabel label = new JLabel("predicted to");
        timePanel.add(label);
        m_destValueTF = new JTextField(10);
        m_destValueTF.setName("tfDestTime");
        m_destValueTF.setEditable(false);
        timePanel.add(m_destValueTF);
        JLabel label2 = new JLabel("(min) in");
        timePanel.add(label2);
        timePanel.add(m_destMapsCB);
        return timePanel;
    }

    @Override
    protected void setEnabledCloudButtons(boolean enable) {
        super.setEnabledCloudButtons(enable);
        m_addLoessCurveBtn.setEnabled(enable);
        m_removeLoessCurveBtn.setEnabled(false);
    }
    
    private void computeLoess() {
        if (this.m_isIonsCloudLoaded) {

            long mapIdSrc = getSelectedMapId(m_sourceMapsCB);
            long mapIdDst = getSelectedMapId(m_destMapsCB);
            MapAlignment map = MapAlignmentConverter.getMapAlgn(mapIdSrc, mapIdDst, m_allMapAlignments);

            IonsRTTableModel cloudData = getCloudData(mapIdSrc);
            if (cloudData != null) {

                double bandwidth = 0.1;

                LoessParametersDialog dialog = new LoessParametersDialog();
                dialog.getValueTF().setText(Double.toString(bandwidth));
                Point panelLocation = this.getLocationOnScreen();
                dialog.setLocation(panelLocation.x + this.getWidth()/2, panelLocation.y+this.getHeight()/2);
                dialog.setVisible(true);

                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK){

                    bandwidth = Double.parseDouble(dialog.getValueTF().getText());

                    int sourceMapRTColumn = cloudData.getColumnIndex(mapIdSrc);
                    int destMapDeltaRTColumn = cloudData.getColumnIndex(mapIdDst);
                    int size = cloudData.getRowCount();
                    List<Pair<Double, Double>> data = new ArrayList<>(size);

                    double ftAlignmentTimeTolerance = ((DataboxMapAlignment) this.m_dataBox).getFeatureAlignmentTimeTolerance();

                    for (int i = 0; i < size; i++) {
                        if (!cloudData.isCrossAssigned(i, destMapDeltaRTColumn)) {
                            Object value = cloudData.getDataValueAt(i, sourceMapRTColumn);
                            Double rt = ((value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue());
                            value = cloudData.getDataValueAt(i, destMapDeltaRTColumn);
                            Double deltaRT = ((value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue());
                            if (Math.abs(deltaRT) <= ftAlignmentTimeTolerance)
                                data.add(Pair.of(rt, deltaRT));
                        }
                    }

                    Map<Double, Double> landmarks = data.stream().distinct().collect(groupingBy(p -> p.getKey(), averagingDouble(p -> p.getValue())));
                    data = landmarks.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).sorted().collect(Collectors.toList());

                    double[] x = new double[data.size()];
                    double[] y = new double[data.size()];
                    for (int i = 0; i < data.size(); i++) {
                        x[i] = data.get(i).getKey();
                        y[i] = data.get(i).getValue();
                    }
                    try {


                        int robustness = 4; // LoessInterpolator.DEFAULT_ROBUSTNESS_ITERS
                        double accuracy = 1e-24; //LoessInterpolator.DEFAULT_ACCURACY
                        LoessInterpolator interpolator = new LoessInterpolator(bandwidth, robustness, accuracy);
                        double[] yfit = interpolator.smooth(x, y);
                        createRegressionPlot(x, yfit);

                        double[] residuals = new double[yfit.length];
                        for (int k = 0; k < yfit.length; k++) {
                            residuals[k] = (y[k] - yfit[k]) * (y[k] - yfit[k]);
                        }
                        
                        interpolator = new LoessInterpolator(bandwidth, robustness, accuracy);
                        double[] sd = interpolator.smooth(x, residuals);
                        double[] upper = new double[yfit.length];
                        double[] lower = new double[yfit.length];
                        // 4.417 for 99.999% probability
                        // 3.890 for 99.99% probability
                        double nsigma = 3.890;

                        for (int k = 0; k < yfit.length; k++) {
                            double s = Math.sqrt(Math.max(0, sd[k]));
                            upper[k] = yfit[k] + nsigma * s;
                            lower[k] = yfit[k] - nsigma * s;
                        }

                        PrintWriter writer = new PrintWriter("squared_residuals.csv");
                        writer.println("rt, deltaRt, predDeltaRt, residuals, sd, upperRt, lowerRT");
                        for (int k = 0; k < yfit.length; k++) {
                            writer.println(x[k]+","+y[k]+","+yfit[k]+","+residuals[k]+","+Math.sqrt(Math.max(0, sd[k]))+","+upper[k]+","+lower[k]);
                        }

                        writer.close();

                        createRegressionPlot(x, upper);
                        createRegressionPlot(x, lower);

                        m_removeLoessCurveBtn.setEnabled(true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void createRegressionPlot(double[] x, double[] newDeltas) {
        List<Pair<Double, Double>> data;
        data = new ArrayList<>(x.length);
        for (int i = 0 ; i < x.length; i++) {
          data.add(Pair.of(x[i], newDeltas[i]));
        }

        BeanTableModel model = new BeanTableModel(Pair.class);
        model.setData(data);
        String xAxisTitle = m_alignmentGraphicPanel.getXAxis().getTitle();
        String yAxisTitle = m_alignmentGraphicPanel.getYAxis().getTitle();

        PlotLinear regressionCurve = new PlotLinear(m_alignmentGraphicPanel, model, null ,0, 3);
        PlotInformation plotInfo = new PlotInformation();
        plotInfo.setPlotColor(CyclicColorPalette.GRAY_DARK);
        regressionCurve.setPlotInformation(plotInfo);
        regressionCurve.setStroke(1f);
        m_alignmentGraphicPanel.addPlot(regressionCurve, true);
        // restore axis titles
        m_alignmentGraphicPanel.getXAxis().setTitle(xAxisTitle);
        m_alignmentGraphicPanel.getYAxis().setTitle(yAxisTitle);
        m_alignmentGraphicPanel.repaint();
    }


    public void setData(QuantChannelInfo quantChannelInfo, List<ExtendedTableModelInterface> compareDataInterfaceList) {
        //@Karine XUE,when a databaseLoadTask or it's subTask is finished, the callback will be called in DataBox, 
        //so this setData method can be called several time. m_isInitialized is used to limite the repetition
        if (!this.m_isInitialized) {
            this.m_quantChannelInfo = quantChannelInfo;

            m_allMapAlignments = new ArrayList();
            m_allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapAlignments());
            m_allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapReversedAlignments());
            // reference alignment map
            m_referenceMapId = m_quantChannelInfo.getDataset().getAlnReferenceMapId();
            String referenceMapTitle = m_quantChannelInfo.getMapTitle(m_referenceMapId);
            String referenceMapColor = m_quantChannelInfo.getMapHtmlColor(m_quantChannelInfo.getDataset().getAlnReferenceMapId());


            StringBuilder sb = new StringBuilder();
            sb.append("<html>");

            sb.append("Reference Map: ");
            sb.append("<font color='").append(referenceMapColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(referenceMapTitle);
            sb.append(", Alignment Mode : ");
            sb.append(this.getAlignmentMethod());
            sb.append("</html>");
            m_labelTitle.setText(sb.toString());

            //model cb
            m_pMapByIndex = new HashMap<>();
            String[] mapItems = new String[m_quantChannelInfo.getDataset().getMaps().size()];
            int i = 0;
            for (ProcessedMap map : m_quantChannelInfo.getDataset().getMaps()) {
                String mapTitle = m_quantChannelInfo.getMapTitle(map.getId());
                sb = new StringBuilder();
                String mapColor = m_quantChannelInfo.getMapHtmlColor(map.getId());
                sb.append("<html><font color='").append(mapColor).append("'>&#x25A0;&nbsp;</font>");
                sb.append(mapTitle);
                sb.append("</html>");
                mapItems[i] = sb.toString();
                m_pMapByIndex.put(i, map);
                i++;
            }
            DefaultComboBoxModel sourceMapsModel = new DefaultComboBoxModel(mapItems);
            DefaultComboBoxModel destMapsModel = new DefaultComboBoxModel(mapItems);
            m_sourceMapsCB.setModel(sourceMapsModel);
            m_destMapsCB.setModel(destMapsModel);
            if (mapItems.length > 0) {
                sourceMapsModel.setSelectedItem(mapItems[0]);
            }
            if (mapItems.length > 1) {
                destMapsModel.setSelectedItem(mapItems[1]);
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
    private String getAlignmentMethod() {
        String method = "unknown";
        Map<String, Object> quantParams;
        try {
            quantParams = m_quantChannelInfo.getDataset().getQuantProcessingConfigAsMap();
            if (quantParams.containsKey(AbstractLabelFreeMSParamsPanel.ALIGNMENT_CONFIG)) {
                Map<String, Object> alignmentConfig = (Map<String, Object>) quantParams.get(AbstractLabelFreeMSParamsPanel.ALIGNMENT_CONFIG);
                method = (String) alignmentConfig.getOrDefault(AbstractLabelFreeMSParamsPanel.ALIGNMENT_METHOD_NAME, "unknown");
            }
            return method;
        } catch (Exception e) {
            return "unknown alignment method exception ";
        }
    }

    @Override
    protected Double getCorrespondingData(Double time, Long mapId) {
        Long targetMapId = getSelectedMapId(m_destMapsCB);
        logger.debug("calculate time for " + time + " from source mapId=" + mapId + " to target MapId=" + targetMapId);
        Double calcTime = Double.NaN;
        try {
            calcTime = MapAlignmentConverter.convertElutionTime(time * 60, mapId, targetMapId, m_allMapAlignments, m_quantChannelInfo.getDataset().getAlnReferenceMapId());
            logger.debug("...result= " + calcTime);
        } catch (Exception e) {
            logger.error("Error while retrieving time in map alignment: " + e);
        }
        return calcTime;
    }


    protected void setDataGraphic() {
        long mapIdSrc = getSelectedMapId(m_sourceMapsCB);
        long mapIdDst = getSelectedMapId(m_destMapsCB);
        if (mapIdSrc == mapIdDst) {
            this.m_alignmentGraphicPanel.clearPlotsWithRepaint();
            if (m_plotPanel2.isVisible()) {//iterative mode, and nobody of source, destination map is reference map
                m_plotPanel2.setVisible(false);
                m_alignmentGraphicPanel_2.clearPlotsWithRepaint();
            }
            repaint();
        } else {
            MapAlignment map = MapAlignmentConverter.getMapAlgn(mapIdSrc, mapIdDst, m_allMapAlignments);
            if (map != null) // exhaustive mode, or in Iterative mode, one of mapIdA,mapIdZ is the reference map
            {
                m_plotPanel2.setVisible(false);
                setDataGraphicTableModel(map, m_alignmentGraphicPanel);

            } else {
                //from source to reference
                map = MapAlignmentConverter.getMapAlgn(mapIdSrc, m_referenceMapId, m_allMapAlignments);
                if (map != null) { // in simple parameters extraction abundance, there is not any map Alignment
                    setDataGraphicTableModel(map, m_alignmentGraphicPanel);
                    //from reference to source
                    map = MapAlignmentConverter.getMapAlgn(m_referenceMapId, mapIdDst, m_allMapAlignments);
                    setDataGraphicTableModel(map, m_alignmentGraphicPanel_2);
                    m_plotPanel2.setVisible(true);
                    this.m_splitPane.resetToPreferredSizes();
                }
            }
        }
    }

    private void setDataGraphicTableModel(MapAlignment mapAlignment, BasePlotPanel graphicPanel) {

        Long mapIdSrc = mapAlignment.getSourceMap().getId();
        Long mapIdDst = mapAlignment.getDestinationMap().getId();
        String mapTitleFrom = m_quantChannelInfo.getMapTitle(mapIdSrc);
        String mapTitleTo = m_quantChannelInfo.getMapTitle(mapIdDst);
        String title = "Map Alignment from " + mapTitleFrom + " (to. " + mapTitleTo + ")";
        Color color = m_quantChannelInfo.getMapColor(mapIdDst);

        double crossAssignmentTimeTolerance = ((DataboxMapAlignment) this.m_dataBox).getCrossAssignmentTimeTolerance();
        double featureAlignmentTimeTolerance = ((DataboxMapAlignment) this.m_dataBox).getFeatureAlignmentTimeTolerance();

        ExtendedTableModelInterface extendedTableModel = new MapTimeTableModel(mapAlignment.getMapTimeList(), color, title, mapTitleFrom, mapTitleTo);
        PlotLinear alignmentCurve = new PlotLinear(graphicPanel, extendedTableModel,null, PlotBaseAbstract.COL_X_ID, PlotBaseAbstract.COL_Y_ID);

        alignmentCurve.setPlotInformation(extendedTableModel.getPlotInformation());
        alignmentCurve.setStroke(3f);
        graphicPanel.setPlot(alignmentCurve);

        if (this.m_isIonsCloudLoaded) {

            if (crossAssignmentTimeTolerance > 0) {

                List <MapTime> timeList = mapAlignment.getMapTimeList();
                PlotInformation plotInformation = extendedTableModel.getPlotInformation();
                plotInformation.setDashed(true);
                Map<String, Object> serializedPropsAsMap = null;

                try {
                    serializedPropsAsMap = mapAlignment.getSerializedPropertiesAsMap();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }

                List<Double> toleranceMapTime = serializedPropsAsMap == null ? null : (List<Double>)serializedPropsAsMap.get("tolerance_time_list");
                List<MapTime> newToleranceMapTime = new ArrayList<>(timeList.size());
                List <MapTime> maxToleranceTimeList = new ArrayList<>(timeList.size());
                List <MapTime> minToleranceTimeList = new ArrayList<>(timeList.size());
                for (int i = 0 ; i < timeList.size(); i++) {
                    MapTime mt = timeList.get(i);
                    double delta = toleranceMapTime != null ? toleranceMapTime.get(i) : crossAssignmentTimeTolerance;
                    maxToleranceTimeList.add(new MapTime(mt.getTime(), mt.getDeltaValue()+delta));
                    minToleranceTimeList.add(new MapTime(mt.getTime(), mt.getDeltaValue()-delta));
                }

                PlotLinear maxDeltaRtCurve = getPlotLinear(graphicPanel, mapTitleFrom, mapTitleTo, title, color, plotInformation, maxToleranceTimeList);
                graphicPanel.addPlot(maxDeltaRtCurve);

                PlotLinear minDeltaRtCurve = getPlotLinear(graphicPanel, mapTitleFrom, mapTitleTo, title, color, plotInformation, minToleranceTimeList);
                graphicPanel.addPlot(minDeltaRtCurve);

                // temporarily add two additional curves at the tolerance value (because we cannot guess if the autoRT was ON or OFF
                // and then if the stored tolerance_time_list was effectively used)

                // creates a new PlotInformation, with solid line

                plotInformation = extendedTableModel.getPlotInformation();
                plotInformation.setDashed(false);

                maxToleranceTimeList = new ArrayList<>(timeList.size());
                minToleranceTimeList = new ArrayList<>(timeList.size());
                for (int i = 0 ; i < timeList.size(); i++) {
                    MapTime mt = timeList.get(i);
                    maxToleranceTimeList.add(new MapTime(mt.getTime(), mt.getDeltaValue()+crossAssignmentTimeTolerance));
                    minToleranceTimeList.add(new MapTime(mt.getTime(), mt.getDeltaValue()-crossAssignmentTimeTolerance));
                }

                maxDeltaRtCurve = getPlotLinear(graphicPanel, mapTitleFrom, mapTitleTo, title, color, plotInformation, maxToleranceTimeList);
                graphicPanel.addPlot(maxDeltaRtCurve);

                minDeltaRtCurve = getPlotLinear(graphicPanel, mapTitleFrom, mapTitleTo, title, color, plotInformation, minToleranceTimeList);
                graphicPanel.addPlot(minDeltaRtCurve);

            }


            IonsRTTableModel cloudData = getCloudData(mapIdSrc);
            if (cloudData != null) {
                int axisX = cloudData.getColumnIndex(mapIdSrc);
                int axisY = cloudData.getColumnIndex(mapIdDst);
                m_removeLoessCurveBtn.setEnabled(false);
                m_ionsScatterPlot = new IonsRTScatterPlot(graphicPanel, cloudData, null, axisX, axisY);
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
                graphicPanel.addPlot(m_ionsScatterPlot);
            }
        }
        graphicPanel.repaint();
    }

    private PlotLinear getPlotLinear(BasePlotPanel graphicPanel, String mapTitleFrom, String mapTitleTo, String title, Color color, PlotInformation plotInformation, List<MapTime> minDeltaRt) {
        ExtendedTableModelInterface minExtendedTableModel = new MapTimeTableModel(minDeltaRt, color, title, mapTitleFrom, mapTitleTo);
        PlotLinear minDeltaRtCurve = new PlotLinear(graphicPanel, minExtendedTableModel, null, PlotBaseAbstract.COL_X_ID, PlotBaseAbstract.COL_Y_ID);
        minDeltaRtCurve.setPlotInformation(plotInformation);
        return minDeltaRtCurve;
    }

}

class LoessParametersDialog extends DefaultDialog {

    JTextField m_valueTF;

    LoessParametersDialog() {
        super(WindowManager.getDefault().getMainWindow(), Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Loess regression parameters");
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new java.awt.Insets(15, 15, 15, 5);
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Bandwidth value (0-1.0):"), c);
        c.gridx++;
        c.insets = new java.awt.Insets(15, 5, 15, 15);
        m_valueTF = new JTextField();
        m_valueTF.setColumns(10);
        panel.add(m_valueTF, c);
        setInternalComponent(panel);
    }

    public JTextField getValueTF() {
        return m_valueTF;
    }
}
