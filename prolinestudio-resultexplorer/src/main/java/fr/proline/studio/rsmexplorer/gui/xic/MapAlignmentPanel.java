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
import fr.proline.studio.graphics.*;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.dam.tasks.xic.MapAlignmentConverter;
import fr.proline.studio.export.ExportButton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;

import fr.proline.studio.utils.CyclicColorPalette;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.lang3.tuple.Pair;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.pattern.xic.DataboxMapAlignment;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractLabelFreeMSParamsPanel;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.alignment.IonsRTScatterPlot;
import fr.proline.studio.table.BeanTableModel;
import fr.proline.studio.utils.IconManager;
import java.io.PrintWriter;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.border.TitledBorder;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;

/**
 * map alignment panel for 1 dataset
 *
 * @author MB243701
 */
public class MapAlignmentPanel extends HourglassPanel implements DataBoxPanelInterface {


    private static final Logger logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final String CLOUD_VIEW_BEST_FIT_TEXT = "Zoom to fit RT tolerance range";
    private static final String CLOUD_VIEW_All_TEXT = "Zoom to fit all ions";
    private static final String CLOUD_VIEW_NOT_ENABLED_TEXT = "Zoom disabled";
    private static final int CLOUD_VIEW_ALL = 1;
    private static final int CLOUD_VIEW_BEST_FIT = 0;//default show mode
    private static final String CLOUD_LOAD_TEXT = "Load matched peptide ions";
    private static final String CLOUD_REMOVE_TEXT = "Remove peptide ions plot";
    private static final String CLOUD_IS_LOADING = "Loading ions ...";
    private final static NumberFormat format2 = new DecimalFormat("#0.0000");

    private AbstractDataBox m_dataBox;
    private QuantChannelInfo m_quantChannelInfo;
    private List<MapAlignment> m_allMapAlignments;
    private Map<Integer, ProcessedMap> m_mapName;
    private long m_referenceMapId;

    private JLabel m_labelTitle;
    private JTextField m_SourceTimeTF;
    private JTextField m_DestTimeTF;
    private JComboBox m_sourceMapsCB;
    private JComboBox m_destMapsCB;

    private JToggleButton m_loadIonsBtn;
    private boolean m_isIonsCloudLoaded;
    private JToggleButton m_zoomModeBtn;
    private int m_zoomMode;
    private JButton m_addLoessCurveBtn;
    private JButton m_removeLoessCurveBtn;
    private JButton m_showHideCrossAssigned;

    private JSplitPane m_splitPane;
    private BasePlotPanel m_alignmentGraphicPanel;
    /**
     * for alignment iterative mode, sometimes, we should show 2 graphic
     */
    private BasePlotPanel m_alignmentGraphicPanel_2;
    private boolean m_isInitialized = false;
    private IonsRTScatterPlot m_ionsScatterPlot;

    public MapAlignmentPanel(DataboxMapAlignment dataBox) {
        super();
        m_referenceMapId = 0;
        m_dataBox = dataBox;
        m_isIonsCloudLoaded = false;
        initComponents();
    }

    private void initComponents() {
        JPanel pane;
        pane = new JPanel();
        JPanel mapAlignmentPanel = initMapAlignmentPanel();
        //m_alignmentGraphicPanel = new MultiGraphicsPanel(false, false);
        m_alignmentGraphicPanel = new BasePlotPanel();
        //m_alignmentGraphicPanel_2 = new MultiGraphicsPanel(false, false);
        m_alignmentGraphicPanel_2 = new BasePlotPanel();
        // the second graphic panel has not data in exhaustive mode and in iterative mode, when one selected map is reference map
        m_alignmentGraphicPanel_2.setVisible(false);
        pane.setLayout(new BorderLayout());
        TitledBorder titleB = new TitledBorder(null, " LC-MS Map Alignments", TitledBorder.CENTER, TitledBorder.CENTER);
        pane.setBorder(titleB);
        pane.add(mapAlignmentPanel, BorderLayout.PAGE_START);
        m_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_alignmentGraphicPanel, m_alignmentGraphicPanel_2);
        m_splitPane.setResizeWeight(0.5); //half half for each grapic panel
        m_splitPane.setBorder(BorderFactory.createRaisedBevelBorder());
        pane.add(m_splitPane, BorderLayout.CENTER);
        this.setLayout(new BorderLayout());
        this.add(pane, BorderLayout.CENTER);
    }

    private JPanel initMapAlignmentPanel() {
        ExportButton exportImageButton = new ExportButton("Graphic", this);
        exportImageButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        createLoadIonsButton();
        createZoomButton();

        m_labelTitle = new JLabel("<html>Reference Map: <font color='RED'>&#x25A0;&nbsp;</font>"
                + "   map   &nbsp;, Alignment Mode : I/H) </html>");

        m_SourceTimeTF = new JTextField(10);
        m_SourceTimeTF.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                convertTime();
            }
        });
        m_SourceTimeTF.addActionListener(e -> convertTime());
        m_SourceTimeTF.setName("tfSourceTime");
        m_SourceTimeTF.setToolTipText("Enter retention time (in minutes)");

        m_sourceMapsCB = new JComboBox();
        m_sourceMapsCB.addActionListener(e -> {
            convertTime();
            setDataGraphic();
        });

        m_destMapsCB = new JComboBox();
        m_destMapsCB.addActionListener(e -> {
            convertTime();
            setDataGraphic();
        });
        m_destMapsCB.setName("cbDestMaps");

        JPanel cloudOptionPane = new JPanel();
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        cloudOptionPane.setLayout(new BoxLayout(cloudOptionPane, BoxLayout.LINE_AXIS));

        m_showHideCrossAssigned = new JButton();
        m_showHideCrossAssigned.setIcon(IconManager.getIcon(IconManager.IconType.HIDE_CROSS_ASSIGNED));
        m_showHideCrossAssigned.setActionCommand("HIDE");
        m_showHideCrossAssigned.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_showHideCrossAssigned.setToolTipText("Show/Hide cross assigned ions");
        m_showHideCrossAssigned.addActionListener(e -> {
            if (m_ionsScatterPlot != null) {
              if (e.getActionCommand().equals("HIDE")) { 
                m_ionsScatterPlot.showCrossAssignedIons(false);
                m_showHideCrossAssigned.setActionCommand("SHOW");
                m_showHideCrossAssigned.setIcon(IconManager.getIcon(IconManager.IconType.SHOW_CROSS_ASSIGNED));
              } else {
                m_ionsScatterPlot.showCrossAssignedIons(true);
                m_showHideCrossAssigned.setActionCommand("HIDE");
                m_showHideCrossAssigned.setIcon(IconManager.getIcon(IconManager.IconType.HIDE_CROSS_ASSIGNED));
              }
              m_alignmentGraphicPanel.repaintUpdateDoubleBuffer();
            }
        });

        m_addLoessCurveBtn = new JButton();
        m_addLoessCurveBtn.setIcon(IconManager.getIcon(IconManager.IconType.ADD_LOESS_CURVE));
        m_addLoessCurveBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_addLoessCurveBtn.setToolTipText("Fit a Loess smooth curve to ions scatter plot");
        m_addLoessCurveBtn.addActionListener(e -> computeLoess());

        m_removeLoessCurveBtn = new JButton();
        m_removeLoessCurveBtn.setIcon(IconManager.getIcon(IconManager.IconType.REMOVE_LOESS_CURVE));
        m_removeLoessCurveBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_removeLoessCurveBtn.setToolTipText("Clear Loess fit curve ");
        m_removeLoessCurveBtn.addActionListener(e ->  {
            //force cloud reload. TODO : find a better way to remove loess curves
            ((DataboxMapAlignment) m_dataBox).loadCloud();
            m_removeLoessCurveBtn.setEnabled(false);
        });

        cloudOptionPane.add(exportImageButton);
        cloudOptionPane.add(m_loadIonsBtn);
        cloudOptionPane.add(m_zoomModeBtn);
        cloudOptionPane.add(m_showHideCrossAssigned);
        cloudOptionPane.add(m_addLoessCurveBtn);
        cloudOptionPane.add(m_removeLoessCurveBtn);
        cloudOptionPane.add(Box.createRigidArea(new Dimension(10, 0)));
        setEnabledCloudButtons(false);
        cloudOptionPane.add(separator);

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        timePanel.add(m_SourceTimeTF);
        JLabel label0 = new JLabel("(min) in ");
        timePanel.add(label0);
        m_sourceMapsCB.setName("cbSourceMaps");
        timePanel.add(m_sourceMapsCB);
        JLabel label = new JLabel("predicted to");
        timePanel.add(label);
        m_DestTimeTF = new JTextField(10);
        m_DestTimeTF.setName("tfDestTime");
        m_DestTimeTF.setEditable(false);
        timePanel.add(m_DestTimeTF);
        JLabel label2 = new JLabel("(min) in");
        timePanel.add(label2);
        timePanel.add(m_destMapsCB);

        JPanel beginPane = new JPanel(new FlowLayout());
        beginPane.add(cloudOptionPane);
        beginPane.add(timePanel);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.add(beginPane, BorderLayout.LINE_START);
        pane.add(m_labelTitle, BorderLayout.LINE_END);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        return pane;
    }

    private void setEnabledCloudButtons(boolean enable) {
      m_addLoessCurveBtn.setEnabled(enable);
      //warning: removeFit enable status depends on addFit only
      m_removeLoessCurveBtn.setEnabled(false);
      m_showHideCrossAssigned.setEnabled(enable);
      m_zoomModeBtn.setEnabled(enable);
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

                    int axisX = cloudData.getColumnIndex(mapIdSrc);
                    int axisY = cloudData.getColumnIndex(mapIdDst);
                    int size = cloudData.getRowCount();
                    List<Pair<Double, Double>> data = new ArrayList<>(size);

                    for (int i = 0; i < size; i++) {
                        if (!cloudData.isCrossAssigned(i, axisY)) {
                            Object value = cloudData.getDataValueAt(i, axisX);
                            Double v1 = ((value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue());
                            value = cloudData.getDataValueAt(i, axisY);
                            Double v2 = ((value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue());
                            if (Math.abs(v2) < 600.0)
                                data.add(Pair.of(v1, v2));
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


                        LoessInterpolator interpolator = new LoessInterpolator(bandwidth, LoessInterpolator.DEFAULT_ROBUSTNESS_ITERS, LoessInterpolator.DEFAULT_ACCURACY);
                        double[] yfit = interpolator.smooth(x, y);
                        createRegressionPlot(x, yfit);

                        PrintWriter writer = new PrintWriter("squared_residuals.csv");
                        
                        double[] residuals = new double[yfit.length];
                        for (int k = 0; k < yfit.length; k++) {
                            residuals[k] = (y[k] - yfit[k]) * (y[k] - yfit[k]);
                            writer.println(residuals[k]+","+x[k]);
                        }

                        writer.close();
                        
                        interpolator = new LoessInterpolator(Math.max(0.15, bandwidth), LoessInterpolator.DEFAULT_ROBUSTNESS_ITERS, LoessInterpolator.DEFAULT_ACCURACY);
                        double[] sd = interpolator.smooth(x, residuals);
                        double[] upper = new double[yfit.length];
                        double[] lower = new double[yfit.length];
                        for (int k = 0; k < yfit.length; k++) {
                            double s = Math.sqrt(Math.max(0, sd[k]));
                            upper[k] = yfit[k] + 2 * s;
                            lower[k] = yfit[k] - 2 * s;
                        }

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

    private void createLoadIonsButton() {
        m_loadIonsBtn = new JToggleButton();
        m_loadIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.LOAD_ALIGNMENT_CLOUD));
        m_loadIonsBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));

        m_loadIonsBtn.setToolTipText(CLOUD_LOAD_TEXT);
        m_loadIonsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!m_isIonsCloudLoaded) {//action = set cloud
                    m_isIonsCloudLoaded = true;
                    m_loadIonsBtn.setIcon(IconManager.getIconWithHourGlass(IconManager.IconType.LOAD_ALIGNMENT_CLOUD));
                    m_loadIonsBtn.setToolTipText(CLOUD_IS_LOADING);
                    ((DataboxMapAlignment) m_dataBox).loadCloud(); //will return in this class and call this.setAlignmentCloud
                } else if (m_dataBox.isLoaded()) {//cloud is been shown, action = remove
                    m_isIonsCloudLoaded = false;//this boolean will affect cloud show
                    m_loadIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.LOAD_ALIGNMENT_CLOUD));
                    m_loadIonsBtn.setToolTipText(CLOUD_LOAD_TEXT);
                    setEnabledCloudButtons(false);
                    setDataGraphic();
                }
            }
        });
    }

    private void createZoomButton() {
        m_zoomModeBtn = new JToggleButton();
        m_zoomModeBtn.setIcon(IconManager.getIcon(IconManager.IconType.ZOOM_ALL));
        m_zoomModeBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_zoomModeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (m_zoomMode == CLOUD_VIEW_BEST_FIT) {
                    m_zoomMode = CLOUD_VIEW_ALL;
                    m_zoomModeBtn.setIcon(IconManager.getIcon(IconManager.IconType.ZOOM_FIT));
                    m_zoomModeBtn.setToolTipText(CLOUD_VIEW_BEST_FIT_TEXT);
                    setDataGraphic();
                } else {
                    m_zoomMode = CLOUD_VIEW_BEST_FIT;
                    m_zoomModeBtn.setIcon(IconManager.getIcon(IconManager.IconType.ZOOM_ALL));
                    m_zoomModeBtn.setToolTipText(CLOUD_VIEW_All_TEXT);
                    setDataGraphic();
                }

            }

        });
        m_zoomModeBtn.setToolTipText(CLOUD_VIEW_NOT_ENABLED_TEXT);
    }



    /**
     * show in plotScatter, the cloud, usually be called when user change combo
     * box selection <br>
     * can be called by DataBoxMapAlignment
     */
    public void setAlignmentCloud() {
        this.m_loadIonsBtn.setToolTipText(CLOUD_REMOVE_TEXT);
        this.m_loadIonsBtn.setIcon(IconManager.getIcon(IconManager.IconType.REMOVE_ALIGNMENT_CLOUD));
        this.m_zoomModeBtn.setToolTipText(CLOUD_VIEW_All_TEXT);
        setEnabledCloudButtons(true);
        this.setDataGraphic();
    }


    public void setData(QuantChannelInfo quantChannelInfo, List<ExtendedTableModelInterface> compareDataInterfaceList, List<CrossSelectionInterface> crossSelectionInterfaceList) {
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
            m_mapName = new HashMap<>();
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
                m_mapName.put(i, map);
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
    public void setDataBox(AbstractDataBox dataBox
    ) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public void addSingleValue(Object v
    ) {
        // not used for the moment JPM.TODO ?
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel
    ) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel
    ) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel
    ) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    private void convertTime() {
        try {
            Double time = Double.parseDouble(m_SourceTimeTF.getText());
            Double calcTime = calcTimeInMapAlign(time, getSelectedMapId(m_sourceMapsCB), getSelectedMapId(m_destMapsCB));
            if (calcTime.isNaN()) {
                m_DestTimeTF.setText("");
            } else {
                m_DestTimeTF.setText(format2.format(calcTime / 60));
            }
        } catch (NumberFormatException ex) {
        }
    }

    private void setDataGraphic() {
        long mapIdSrc = getSelectedMapId(m_sourceMapsCB);
        long mapIdDst = getSelectedMapId(m_destMapsCB);
        if (mapIdSrc == mapIdDst) {
            this.m_alignmentGraphicPanel.clearPlotsWithRepaint();
            if (this.m_alignmentGraphicPanel_2.isVisible()) {//iterative mode, and nobody of source, destination map is reference map
                this.m_alignmentGraphicPanel_2.setVisible(false);
                this.m_alignmentGraphicPanel_2.clearPlotsWithRepaint();
            }
            repaint();
        } else {
            MapAlignment map = MapAlignmentConverter.getMapAlgn(mapIdSrc, mapIdDst, m_allMapAlignments);
            if (map != null) // exhaustive mode, or in Iterative mode, one of mapIdA,mapIdZ is the reference map
            {
                this.m_alignmentGraphicPanel_2.setVisible(false);
                setDataGraphicTableModel(map, m_alignmentGraphicPanel);

            } else {
                //from source to reference
                map = MapAlignmentConverter.getMapAlgn(mapIdSrc, m_referenceMapId, m_allMapAlignments);
                if (map != null) { // in simple parameters extraction abundance, there is not any map Alignment
                    setDataGraphicTableModel(map, m_alignmentGraphicPanel);
                    //from reference to source
                    map = MapAlignmentConverter.getMapAlgn(m_referenceMapId, mapIdDst, m_allMapAlignments);
                    setDataGraphicTableModel(map, m_alignmentGraphicPanel_2);
                    this.m_alignmentGraphicPanel_2.setVisible(true);
                    this.m_splitPane.resetToPreferredSizes();
                }
            }
        }
    }

    private void setDataGraphicTableModel(MapAlignment map, BasePlotPanel graphicPanel) {
        CrossSelectionInterface crossSelectionTableModel;
        ExtendedTableModelInterface extendedTableModel;
        Long mapIdSrc = map.getSourceMap().getId();
        Long mapIdDst = map.getDestinationMap().getId();

        MapTimePanel mapTimePanel;
        List<MapTime> listMapTime = map.getMapTimeList();
        mapTimePanel = new MapTimePanel();
        String mapTitleFrom = m_quantChannelInfo.getMapTitle(mapIdSrc);
        String mapTitleTo = m_quantChannelInfo.getMapTitle(mapIdDst);
        String title = "Map Alignment from " + mapTitleFrom + " (to. " + mapTitleTo + ")";
        Color color = m_quantChannelInfo.getMapColor(mapIdDst);
        mapTimePanel.setData((long) -1, map, listMapTime, color, title, true, mapTitleFrom, mapTitleTo);//set graphic content

        crossSelectionTableModel = mapTimePanel.getCrossSelectionInterface();
        extendedTableModel = mapTimePanel.getGlobalTableModelInterface();
        double tolerance = ((DataboxMapAlignment) this.m_dataBox).getRT_Tolerance();
        PlotLinear alignmentCurve = new PlotLinear(graphicPanel, extendedTableModel, crossSelectionTableModel,
                PlotBaseAbstract.COL_X_ID, PlotBaseAbstract.COL_Y_ID);

        alignmentCurve.setPlotInformation(extendedTableModel.getPlotInformation());//set Color
        alignmentCurve.setStroke(3f);  //set Stroke
        alignmentCurve.setTolerance(tolerance);

        graphicPanel.setPlot(alignmentCurve);

        if (this.m_isIonsCloudLoaded) {
            IonsRTTableModel cloudData = getCloudData(mapIdSrc);
            if (cloudData != null) {
                int axisX = cloudData.getColumnIndex(mapIdSrc);
                int axisY = cloudData.getColumnIndex(mapIdDst);
                m_removeLoessCurveBtn.setEnabled(false);
                m_ionsScatterPlot = new IonsRTScatterPlot(graphicPanel, cloudData, null, axisX, axisY);
                m_ionsScatterPlot.showCrossAssignedIons(m_showHideCrossAssigned.getActionCommand().equals("HIDE"));
                m_ionsScatterPlot.setColor(color);
                if (m_zoomMode == CLOUD_VIEW_BEST_FIT) {
                    //set visible Min Max, the real Min Max are too large to show the alignment PlotLinear
                    double yMax = alignmentCurve.getYMax();
                    double yMin = alignmentCurve.getYMin();
                    m_ionsScatterPlot.setYMax(yMax + 2 * tolerance);
                    m_ionsScatterPlot.setYMin(yMin - 2 * tolerance);
                }
                graphicPanel.setPlot(m_ionsScatterPlot);
                graphicPanel.addPlot(alignmentCurve, true);
            }
        }
        graphicPanel.repaint();
    }

    private IonsRTTableModel getCloudData(long mapIdSrc) {
        if (this.m_dataBox.isLoaded()) {
            return ((DataboxMapAlignment) this.m_dataBox).getPeptideCloud(mapIdSrc);
        } else {
            return null;
        }
    }

    private Long getSelectedMapId(JComboBox cb) {
        if (cb == null) {
            return (long) -1;
        } else {
            int selId = cb.getSelectedIndex();
            if (m_mapName.containsKey(selId)) {
                return m_mapName.get(selId).getId();
            } else {
                return (long) -1;//should not happen
            }
        }
    }

    private Double calcTimeInMapAlign(Double time, Long sourceMapId, Long targetMapId) {
        logger.debug("calculate time for " + time + " from source mapId=" + sourceMapId + " to target MapId=" + targetMapId);
        Double calcTime = Double.NaN;
        try {
            calcTime = MapAlignmentConverter.convertElutionTime(time * 60, sourceMapId, targetMapId, m_allMapAlignments, m_quantChannelInfo.getDataset().getAlnReferenceMapId());
            logger.debug("...result= " + calcTime);
        } catch (Exception e) {
            logger.error("Error while retrieving time in map alignment: " + e);
        }
        return calcTime;
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
