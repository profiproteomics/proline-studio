/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.dam.tasks.xic.MapAlignmentConverter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.MultiGraphicsPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.AbstractDefineQuantParamsPanelV2;
import java.awt.Color;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

/**
 * map alignment panel for 1 dataset
 *
 * @author MB243701
 */
public class MapAlignmentPanel extends HourglassPanel implements DataBoxPanelInterface {

    private static final Logger logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private AbstractDataBox m_dataBox;

    private final static NumberFormat format2 = new DecimalFormat("#0.0000");

    private QuantChannelInfo m_quantChannelInfo;
    // all map alignments
    private List<MapAlignment> m_allMapAlignments;

    private final static String panelTitle = " LC-MS Map Alignments";

    private JLabel m_labelTitle;
    private JTextField m_tfSouceTime;
    private JTextField m_tfDestTime;
    private JComboBox m_cbSourceMaps;
    private JComboBox m_cbDestMaps;
    private DefaultComboBoxModel m_cbSourceModel;
    private DefaultComboBoxModel m_cbDestModel;
    private Map<Integer, ProcessedMap> m_mapName;
    private long m_referenceMapId;

    private String m_alnMode;

    private List<ExtendedTableModelInterface> m_valuesList = null;
    private List<CrossSelectionInterface> m_crossSelectionInterfaceList = null;
    private JSplitPane m_splitPane;
    private MultiGraphicsPanel m_alignmentGraphicPanel;  //who has plotPanel
    /**
     * for alignement iterative mode, sometimes, we should show 2 graphic
     */
    private MultiGraphicsPanel m_alignmentGraphicPanel_2;

    public MapAlignmentPanel() {
        super();
        m_referenceMapId = 0;
        m_alnMode = "unknown";
        initComponents();

    }

    private void initComponents() {
        JPanel pane;
        pane = new JPanel();
        JPanel mapAlignmentPanel = initMapAlignmentPanel();
        m_alignmentGraphicPanel = new MultiGraphicsPanel(false, false);
        m_alignmentGraphicPanel_2 = new MultiGraphicsPanel(false, false);
        // the second graphic panel has not data in exhaustive mode and in iterative mode, when one selected map is reference map
        m_alignmentGraphicPanel_2.setVisible(false);
        pane.setLayout(new BorderLayout());
        TitledBorder titleB = new TitledBorder(null, panelTitle, TitledBorder.CENTER, TitledBorder.CENTER);
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

        m_labelTitle = new JLabel("<html>Reference Map: <font color='RED'>&#x25A0;&nbsp;</font>"
                + "   map   &nbsp;, Alignment Mode : I/H) </html>");

        m_tfSouceTime = new JTextField(10);
        m_tfSouceTime.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                convertTime();
            }
        });
        m_tfSouceTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertTime();
            }
        });
        m_tfSouceTime.setName("tfSourceTime");
        m_tfSouceTime.setToolTipText("fill time in min");

        m_cbSourceMaps = new JComboBox();
        m_cbSourceMaps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertTime();
                setDataGraphic();
            }
        });

        m_cbDestMaps = new JComboBox();
        m_cbDestMaps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertTime();
                setDataGraphic();
            }
        });
        m_cbDestMaps.setName("cbDestMaps");

        JPanel pane = new JPanel();
        JPanel timePanel = new JPanel();
        pane.setLayout(new BorderLayout());
        {
            timePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            timePanel.add(m_tfSouceTime);
            JLabel label0 = new JLabel("(min) in ");
            timePanel.add(label0);
            m_cbSourceMaps.setName("cbSourceMaps");
            timePanel.add(m_cbSourceMaps);
            JLabel label = new JLabel("predicted to");
            timePanel.add(label);
            m_tfDestTime = new JTextField(10);
            m_tfDestTime.setName("tfDestTime");
            m_tfDestTime.setEditable(false);
            timePanel.add(m_tfDestTime);
            JLabel label2 = new JLabel("(min) in");
            timePanel.add(label2);
            timePanel.add(m_cbDestMaps);
        }

        pane.add(timePanel, BorderLayout.LINE_START);
        pane.add(m_labelTitle, BorderLayout.LINE_END);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return pane;
    }

    public void setData(QuantChannelInfo quantChannelInfo, List<ExtendedTableModelInterface> compareDataInterfaceList, List<CrossSelectionInterface> crossSelectionInterfaceList) {

        this.m_quantChannelInfo = quantChannelInfo;
        this.m_valuesList = compareDataInterfaceList;
        this.m_crossSelectionInterfaceList = crossSelectionInterfaceList;

        m_allMapAlignments = new ArrayList();
        m_allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapAlignments());
        m_allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapReversedAlignments());
        // reference alignment map
        m_referenceMapId = m_quantChannelInfo.getDataset().getAlnReferenceMapId();
        String referenceMapTitle = m_quantChannelInfo.getMapTitle(m_referenceMapId);
        String htmlColor = m_quantChannelInfo.getMapHtmlColor(m_quantChannelInfo.getDataset().getAlnReferenceMapId());
        m_alnMode = this.getAlignmentMethod();

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");

        sb.append("Reference Map: ");
        sb.append("<font color='").append(htmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(referenceMapTitle);
        sb.append(", Alignment Mode : ");
        sb.append(m_alnMode);
        sb.append("</html>");
        m_labelTitle.setText(sb.toString());

        //model cb
        m_mapName = new HashMap<>();
        String[] mapItems = new String[m_quantChannelInfo.getDataset().getMaps().size()];
        int i = 0;
        for (ProcessedMap map : m_quantChannelInfo.getDataset().getMaps()) {
            String mapTitle = m_quantChannelInfo.getMapTitle(map.getId());
            sb = new StringBuilder();
            htmlColor = m_quantChannelInfo.getMapHtmlColor(map.getId());
            sb.append("<html><font color='").append(htmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(mapTitle);
            sb.append("</html>");
            mapItems[i] = sb.toString();
            m_mapName.put(i, map);
            i++;
        }
        m_cbSourceModel = new DefaultComboBoxModel(mapItems);
        m_cbDestModel = new DefaultComboBoxModel(mapItems);
        m_cbSourceMaps.setModel(m_cbSourceModel);
        m_cbDestMaps.setModel(m_cbDestModel);
        if (mapItems.length > 0) {
            m_cbSourceModel.setSelectedItem(mapItems[0]);
        }
        if (mapItems.length > 1) {
            m_cbDestModel.setSelectedItem(mapItems[1]);
        }
        setDataGraphic();
        repaint();
    }

    private String getAlignmentMethod() {
        String method = "unknown";
        Map<String, Object> quantParams;
        try {
            quantParams = m_quantChannelInfo.getDataset().getQuantProcessingConfigAsMap();
            if (quantParams.containsKey(AbstractDefineQuantParamsPanelV2.ALIGNMENT_CONFIG)) {
                Map<String, Object> alignmentConfig = (Map<String, Object>) quantParams.get(AbstractDefineQuantParamsPanelV2.ALIGNMENT_CONFIG);
                method = (String) alignmentConfig.getOrDefault(AbstractDefineQuantParamsPanelV2.ALIGNMENT_METHOD_NAME, "unknown");
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
            Double time = Double.parseDouble(m_tfSouceTime.getText());
            Double calcTime = calcTimeInMapAlign(time, getSelectedMapId(m_cbSourceMaps), getSelectedMapId(m_cbDestMaps));
            if (calcTime.isNaN()) {
                m_tfDestTime.setText("");
            } else {
                m_tfDestTime.setText(format2.format(calcTime / 60));
            }
        } catch (NumberFormatException ex) {
        }
    }

    private void setDataGraphic() {
        long mapIdSrc = getSelectedMapId(m_cbSourceMaps);
        long mapIdDst = getSelectedMapId(m_cbDestMaps);
        if (mapIdSrc == mapIdDst) {
            this.m_alignmentGraphicPanel.clearPlots();
            if ( this.m_alignmentGraphicPanel_2.isVisible())
                this.m_alignmentGraphicPanel_2.clearPlots();
        } else {
            List<MapAlignment> mapAList;
            MapAlignment map = MapAlignmentConverter.getMapAlgn(mapIdSrc, mapIdDst, m_allMapAlignments);
            List<CrossSelectionInterface> listCSI = new ArrayList();
            List<ExtendedTableModelInterface> listETI = new ArrayList();
            if (map != null) // exhaustive mode, or in Iterative mode, one of mapIdA,mapIdZ is the reference map
            {
                this.m_alignmentGraphicPanel_2.setVisible(false);
                setDataGraphicTableModel(map, this.m_alignmentGraphicPanel);
            } else {
                this.m_alignmentGraphicPanel_2.setVisible(true);
                this.m_splitPane.resetToPreferredSizes();
                //from source to reference
                map = MapAlignmentConverter.getMapAlgn(mapIdSrc, m_referenceMapId, m_allMapAlignments);
                setDataGraphicTableModel(map, m_alignmentGraphicPanel);
                //from reference to source
                map = MapAlignmentConverter.getMapAlgn(m_referenceMapId, mapIdDst, m_allMapAlignments);
                setDataGraphicTableModel(map, m_alignmentGraphicPanel_2);
            }

        }
    }

    private void setDataGraphicTableModel(MapAlignment map, MultiGraphicsPanel graphicPanel) {
        List<MapAlignment> mapAList = new ArrayList();
        mapAList.add(map);
        List<CrossSelectionInterface> listCSI = new ArrayList();
        List<ExtendedTableModelInterface> listETI = new ArrayList();
        List<MapTimePanel> mapTimePanelist = new ArrayList();
        MapTimePanel mapTimePanel;
        for (MapAlignment m : mapAList) {
            List<MapTime> listMapTime = m.getMapTimeList();
            mapTimePanel = new MapTimePanel();
            String fromMap = m_quantChannelInfo.getMapTitle(m.getSourceMap().getId());
            String toMap = m_quantChannelInfo.getMapTitle(m.getDestinationMap().getId());
            String title = "Map Alignment from " + fromMap + " (to. " + toMap + ")";
            Color color = m_quantChannelInfo.getMapColor(m.getDestinationMap().getId());
            mapTimePanel.setData((long) -1, m, listMapTime, color, title, true, fromMap, toMap);//set graphic content
            mapTimePanelist.add(mapTimePanel);
        }
        for (MapTimePanel mPanel : mapTimePanelist) {
            listCSI.add(mPanel.getCrossSelectionInterface());
            listETI.add(mPanel.getGlobalTableModelInterface());
        }
        graphicPanel.setData(listETI, listCSI);
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
