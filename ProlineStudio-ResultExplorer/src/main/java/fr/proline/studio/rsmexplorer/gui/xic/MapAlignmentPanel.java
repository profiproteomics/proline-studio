/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapAlignment;
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

/**
 * map alignment panel for 1 dataset
 * @author MB243701
 */
public class MapAlignmentPanel extends HourglassPanel implements DataBoxPanelInterface {
    private  static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
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
    
    
    public MapAlignmentPanel() {
        super();
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        JPanel mapAlignmentPanel = createMapAlignmentPanel();
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createRaisedBevelBorder());
        m_labelTitle = new JLabel(panelTitle);
        panelHeader.add(m_labelTitle, BorderLayout.NORTH);
        panelHeader.add(mapAlignmentPanel, BorderLayout.CENTER);
        this.add(panelHeader, BorderLayout.CENTER);
    }
    
    private JPanel createMapAlignmentPanel(){
        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        
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
        internalPanel.add(m_tfSouceTime);
        
        m_cbSourceMaps = new JComboBox();
        m_cbSourceMaps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertTime();
            }
        });
        
        JLabel label0 = new JLabel("(min) in ");
        internalPanel.add(label0);
        
        m_cbSourceMaps.setName("cbSourceMaps");
        internalPanel.add(m_cbSourceMaps);
        
        JLabel label = new JLabel("predicted to");
        internalPanel.add(label);
        
        m_tfDestTime = new JTextField(10);
        m_tfDestTime.setName("tfDestTime");
        m_tfDestTime.setEditable(false);
        internalPanel.add(m_tfDestTime);
        
        JLabel label2 = new JLabel("(min) in");
        internalPanel.add(label2);
        
        m_cbDestMaps = new JComboBox();
        m_cbDestMaps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertTime();
            }
        });
        m_cbDestMaps.setName("cbDestMaps");
        internalPanel.add(m_cbDestMaps);
        
        return internalPanel;
    }
    

    public void setData(QuantChannelInfo quantChannelInfo, List<ExtendedTableModelInterface> compareDataInterfaceList, List<CrossSelectionInterface> crossSelectionInterfaceList){
        
        this.m_quantChannelInfo = quantChannelInfo;
        
        m_allMapAlignments = new ArrayList();
        m_allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapAlignments());
        m_allMapAlignments.addAll(m_quantChannelInfo.getDataset().getMapReversedAlignments());
        
        
        // reference alignment map
        String referenceMapTitle = m_quantChannelInfo.getMapTitle(m_quantChannelInfo.getDataset().getAlnReferenceMapId());
        StringBuilder sb = new StringBuilder();
        String htmlColor = m_quantChannelInfo.getMapHtmlColor(m_quantChannelInfo.getDataset().getAlnReferenceMapId());
        sb.append("<html><font color='").append(htmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(panelTitle);
        sb.append(" to ");
        sb.append(referenceMapTitle);
        sb.append("<br/>");
        sb.append("</html>");
        m_labelTitle.setText(sb.toString());
        
        //model cb
        m_mapName = new HashMap<>();
        String[] mapItems = new String[m_quantChannelInfo.getDataset().getMaps().size()];
        int i=0;
        for(ProcessedMap map: m_quantChannelInfo.getDataset().getMaps()){
            String mapTitle =  m_quantChannelInfo.getMapTitle(map.getId());
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
        if (mapItems.length > 0){
            m_cbSourceModel.setSelectedItem(mapItems[0]);
        }
        if(mapItems.length > 1){
            m_cbDestModel.setSelectedItem(mapItems[1]);
        }
        
        repaint();
    }
    
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }
    
    @Override
    public void addSingleValue(Object v) {
        // not used for the moment JPM.TODO ?
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }
    
    private void convertTime(){
        try {
            Double time = Double.parseDouble(m_tfSouceTime.getText());
            Double calcTime = calcTimeInMapAlign(time, getSelectedMapId(m_cbSourceMaps), getSelectedMapId(m_cbDestMaps));
            if (calcTime.isNaN()){
                m_tfDestTime.setText("");
            }else{
                m_tfDestTime.setText(format2.format(calcTime / 60));
            }
        } catch (NumberFormatException ex) {
        }
    }
    
    
    private Long getSelectedMapId(JComboBox cb){
        if (cb == null){
            return (long)-1;
        }else{
            int selId = cb.getSelectedIndex();
            if (m_mapName.containsKey(selId)){
                return m_mapName.get(selId).getId();
            }else{
                return (long)-1;//should not happen
            }
        }
    }
    
    private Double calcTimeInMapAlign(Double time, Long sourceMapId, Long targetMapId){
        m_logger.debug("calculate time for "+time+" from source mapId="+sourceMapId +" to target MapId="+targetMapId );
        Double calcTime = Double.NaN;
        try{
            calcTime = MapAlignmentConverter.convertElutionTime(time*60, sourceMapId, targetMapId, m_allMapAlignments, m_quantChannelInfo.getDataset().getAlnReferenceMapId());
            m_logger.debug("...result= "+calcTime);
        }catch(Exception e){
            m_logger.error("Error while retrieving time in map alignment: "+e);
        }
        return calcTime;
    }

}