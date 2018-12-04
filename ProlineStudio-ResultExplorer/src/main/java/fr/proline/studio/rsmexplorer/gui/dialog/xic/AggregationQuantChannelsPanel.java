/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.WizardPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import static fr.proline.studio.rsmexplorer.tree.AbstractNode.NodeTypes.*;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.xic.AbstractTreeTransferHandler;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICSelectionTransferable;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.openide.util.Exceptions;

/**
 *
 * @author CB205360
 */
public class AggregationQuantChannelsPanel extends JPanel {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private static AggregationQuantChannelsPanel m_singleton;
    private QCMappingTreeTableModel m_model; 
    private JScrollPane m_tableScrollPane;
    private JTabbedPane m_tabbedPane;
    
    public static AggregationQuantChannelsPanel getPanel(AbstractNode rootNode, List<DDataset> datasets) {
        if (m_singleton == null) {
            m_singleton = new AggregationQuantChannelsPanel();
        } 
        m_singleton.setMapping(rootNode, datasets);
        
        return m_singleton;
    }
    
    public AggregationQuantChannelsPanel() {
        setLayout(new BorderLayout());
        add(new WizardPanel("<html><b>Step 2:</b> Define mapping between quantitation channels.</html>"), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    
    public final JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JPanel framePanel = new JPanel(new GridBagLayout());

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(createQCMappingPanel());
        sp.setRightComponent(createDatasetsPanel());
        sp.setDividerLocation(0.70);
        sp.setResizeWeight(0.5);

        final GridBagConstraints cFrame = new GridBagConstraints();
        cFrame.insets = new java.awt.Insets(5, 5, 5, 5);

        cFrame.gridx = 0;
        cFrame.gridy = 0;
        cFrame.gridwidth = 2;
        cFrame.weightx = 1;
        cFrame.weighty = 0;
        cFrame.anchor = GridBagConstraints.NORTH;
        cFrame.fill = GridBagConstraints.NONE;
        framePanel.add(new JLabel("Drag & Drop", IconManager.getIcon(IconManager.IconType.DRAG_AND_DROP), JLabel.LEADING), cFrame);

        cFrame.anchor = GridBagConstraints.NORTHWEST;
        cFrame.fill = GridBagConstraints.BOTH;
        cFrame.gridwidth = 1;
        cFrame.gridy++;
        cFrame.weighty = 1;
        framePanel.add(sp, cFrame);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(framePanel, c);

        return mainPanel;
    }
        
    private JComponent createQCMappingPanel() {
        m_tableScrollPane = new JScrollPane();
        m_tableScrollPane.getViewport().setBackground(Color.white);
        return m_tableScrollPane;
    }

    private JComponent createDatasetsPanel() {
        m_tabbedPane = new JTabbedPane();
        
        return m_tabbedPane;
    }
  
    public void setMapping(AbstractNode rootNode, List<DDataset> datasets)  {
        m_model = new QCMappingTreeTableModel(rootNode, datasets);
        
        m_tableScrollPane.setViewportView(createTreeTable(m_model));
        m_tabbedPane.removeAll();
        
        for (DDataset ds : datasets) {
            DataSetData datasetData = DataSetData.createTemporaryQuantitation(ds.getName()); //new DataSetData(ds.getName(), Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);
            datasetData.setDataset(ds);
            DataSetNode dsNode = new DataSetNode(datasetData);
            QuantExperimentalDesignTree dsDesignTree = new QuantExperimentalDesignTree(dsNode, true);
            QuantExperimentalDesignTree.displayExperimentalDesign(ds, dsNode, dsDesignTree, true, true);
            m_tabbedPane.add(ds.getName(), dsDesignTree);
        }
        validate();
        repaint();
    }
    
    private JXTreeTable createTreeTable(QCMappingTreeTableModel model) {
        JXTreeTable treeTable = new JXTreeTable(model) {
            @Override
            public String getToolTipText(MouseEvent event) {   
                int column = columnAtPoint(event.getPoint());
                if (column > 0 && column < getColumnCount()) {
                    int row = rowAtPoint(event.getPoint());
                    if (row < getRowCount())
                        return model.getToolTipText(nodeForRow(row), column);
                }
                return super.getToolTipText(event);
            }
            
            protected Object nodeForRow(int row) {
                TreePath path = getPathForRow(row);
                return path != null ? path.getLastPathComponent() : null;
            }
        };
        
        // rendering of the tree
        treeTable.putClientProperty("JTree.lineStyle", "Horizontal");
        treeTable.setRowHeight(18);

        IdentificationTree.RSMTreeRenderer renderer = new IdentificationTree.RSMTreeRenderer();
        treeTable.setTreeCellRenderer(renderer);
        treeTable.expandAll();
        TransferHandler handler = new QCMappingTransferHandler(treeTable);
        
        treeTable.setTransferHandler(handler);
        treeTable.setDropMode(DropMode.ON);
        treeTable.setRootVisible(true);

        return treeTable;

    }
    
    public List<Map<String, Object>> getQuantChannelsMatching() {
        List<Map<String, Object>> mappingList = new ArrayList<>();
        for (DQuantitationChannelMapping entry : m_model.getMapping().values()) {
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("quant_channel_number", entry.getParentQCNumber());
            Map<Long, Long> map = entry.getMappedQuantChannels().entrySet().stream().filter(e -> (e.getValue() != null) ).collect(Collectors.toMap(
                e -> e.getKey().getMasterQuantitationChannels().get(0).getId(),
                e -> e.getValue().getId()
            ));
            mapping.put("quant_channels_matching", map);
            mappingList.add(mapping);
        }
        
        return mappingList;
    }

}

class QCMappingTreeTableModel extends AbstractTreeTableModel {

    List<DDataset> m_datasets;
    Map<AbstractNode, DQuantitationChannelMapping> m_parentQCMappings;
//    List<AbstractNode> m_indexedNodes; 
    
    public QCMappingTreeTableModel(AbstractNode rootNode, List<DDataset> datasets) {
        super(rootNode);
        m_datasets = datasets;
        m_parentQCMappings = inferDefaultMapping(rootNode);
//        m_indexedNodes = new ArrayList<>();
//        Enumeration en = rootNode.preorderEnumeration();
//        while(en.hasMoreElements()) {
//            m_indexedNodes.add((AbstractNode)en.nextElement());
//        }
    }


    @Override
    public int getColumnCount() {
        return m_datasets.size() + 1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class; 
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Sample Analysis";
        } else {
           DDataset ds = m_datasets.get(columnIndex - 1);
           return ds.getName();
        }
    }
        
    public String getToolTipText(Object o, int column) {
        DDataset ds = m_datasets.get(column - 1);
        AbstractNode node = (AbstractNode)o;
        if (node != null && node.getType() == BIOLOGICAL_SAMPLE_ANALYSIS) {
            DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
            QuantitationChannel childQc = mapping.getMappedQuantChannels().get(ds);
            if (childQc != null) {
                return new StringBuilder(childQc.getName()).append(" (id=").append(childQc.getId()).append(", number=").append(childQc.getNumber()).append(")").toString();
            } else {
                return null;
            }
        }
        return null;
    }
    
    private Map<AbstractNode, DQuantitationChannelMapping> inferDefaultMapping(AbstractNode node) {

        Map<AbstractNode, DQuantitationChannelMapping> mappings = new HashMap<>();
        Stream<Object> groupStream = Collections.list(node.children()).stream().filter(n -> (((AbstractNode) n).getType() == AbstractNode.NodeTypes.BIOLOGICAL_GROUP));
        AtomicInteger index = new AtomicInteger(1);
        List<XICBiologicalGroupNode> groupNodes = groupStream.map(o -> ((XICBiologicalGroupNode) o)).collect(Collectors.toList());
        for (XICBiologicalGroupNode groupNode : groupNodes) {
            
            Map<AbstractNode, DQuantitationChannelMapping> qcNodes = parseGroup(groupNode).collect(Collectors.toMap(x -> x, x -> new DQuantitationChannelMapping(index.getAndIncrement())));
            List<DQuantitationChannelMapping> sortedMappings = qcNodes.values().stream().sorted(Comparator.comparing(DQuantitationChannelMapping::getParentQCNumber)).collect(Collectors.toList());
            
            for (DDataset ds : m_datasets) {
                BiologicalGroup group = ds.getGroupSetup().getBiologicalGroups().stream().filter(bg -> bg.getName().equals(((DataSetData)groupNode.getData()).getTemporaryName())).findAny().orElse(null);
                if (group != null) {
                    List<QuantitationChannel> groupQcs = group.getBiologicalSamples().stream().flatMap(bs -> bs.getQuantitationChannels().stream()).collect(Collectors.toList());
                    for (int k = 0; k < Math.min(groupQcs.size(), sortedMappings.size()); k++) {
                        sortedMappings.get(k).put(ds, groupQcs.get(k));
                    }
                }
            }            
            mappings.putAll(qcNodes);
        }
        return mappings;
    }

    private Stream<XICBiologicalSampleAnalysisNode> parseGroup(XICBiologicalGroupNode groupNode) {
        Stream<Object> stream = Collections.list(groupNode.children()).stream().flatMap(node -> Collections.list(((AbstractNode)node).children()).stream());
        return stream.map(node -> ((XICBiologicalSampleAnalysisNode)node));
    }

    @Override
    public boolean isCellEditable(Object o, int column) {
        AbstractNode node = (AbstractNode)o;
        boolean editable = (column>0) && (node.getType() == BIOLOGICAL_SAMPLE_ANALYSIS);
        return editable;
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        String text = (String) value;
        DDataset ds = m_datasets.get(column - 1);
        DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
        if ((text == null) || (text.trim().isEmpty())) {
            mapping.put(ds, null);
        } else {
            try {
                int number = Integer.parseInt(text);
                mapping.put(ds, number);
            } catch (Exception e) { 
                //not a number, do not change the value 
            }
        }
    }
    
    @Override
    public Object getValueAt(Object o, int columnIndex) {
        AbstractNode node = (AbstractNode)o;
        if (columnIndex == 0) {
            return node.toString();
        } else if (XICBiologicalSampleAnalysisNode.class.isAssignableFrom(node.getClass())) {
            DDataset ds = m_datasets.get(columnIndex - 1);
            DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
            QuantitationChannel childQc = mapping.getMappedQuantChannels().get(ds);
            if (childQc != null) {
                return childQc.getName();
            } else {
                return "<ignored>";
            }
        }
        return null;
    }
    
    @Override
    public Object getChild(Object parent, int index) {
        TreeNode node = (TreeNode)parent;
        return node.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        TreeNode node = (TreeNode)parent;
        return node.getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        AbstractNode node = (AbstractNode)parent;
        return node.getIndex((TreeNode)child);
    }
  
    public Map<AbstractNode, DQuantitationChannelMapping> getMapping() {
        return m_parentQCMappings;
    }

    DDataset getDatasetAt(int column) {
        return m_datasets.get(column-1);
    }
    
//    public AbstractNode getNodeAt(int rowIndex) {
//        if (rowIndex >= 0 && rowIndex < m_indexedNodes.size()) {
//            return m_indexedNodes.get(rowIndex);
//        }
//        return null;
//    }

}

class DQuantitationChannelMapping {

    private Integer parentQCNumber;
    private Map<DDataset, QuantitationChannel> mappedQuantChannels;

    public DQuantitationChannelMapping(Integer parentQCNumber) {
        this.parentQCNumber = parentQCNumber;
        this.mappedQuantChannels = new HashMap<>();
    }

    public Integer getParentQCNumber() {
        return parentQCNumber;
    }

    public Map<DDataset, QuantitationChannel> getMappedQuantChannels() {
        return mappedQuantChannels;
    }

    void put(DDataset ds, QuantitationChannel childQC) {
        mappedQuantChannels.put(ds, childQC);
    }

    void put(DDataset ds, int qcNumber) {
        QuantitationChannel childQC = ds.getMasterQuantitationChannels().get(0).getQuantitationChannels().stream().filter(qc -> qc.getNumber() == qcNumber).findFirst().orElse(null);
        if (childQC != null) {
            mappedQuantChannels.put(ds, childQC);
        }
    }

}

class QCMappingTransferHandler extends AbstractTreeTransferHandler {

    private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private final QCMappingTreeTableModel m_model;
    private final JXTreeTable m_tree;
    
    public QCMappingTransferHandler(JXTreeTable tree) {
        super(false);
        m_tree = tree;
        m_model = (QCMappingTreeTableModel)m_tree.getTreeTableModel();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);

        if (support.isDataFlavorSupported(XICSelectionTransferable.RSMNodeList_FLAVOR)) {

            // drop path
            JTable.DropLocation dropLocation = ((JTable.DropLocation) support.getDropLocation());
            if ((dropLocation == null) || (dropLocation.getRow() == -1) || (dropLocation.getColumn() == -1) || (dropLocation.getColumn() == 0)){
                return false;
            }
            
            AbstractNode.NodeTypes designNodeType = null;
            try {
                XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());
                designNodeType = data.getDesignNodeType();
                ArrayList<AbstractNode> nodesList = (ArrayList<AbstractNode>) data.getDesignList();
                if (nodesList != null && !nodesList.isEmpty()) {
                    AbstractData rootData = ((AbstractNode)nodesList.get(0).getRoot()).getData();
                    DDataset sourceDS = ((DataSetData)rootData).getDataset();
                    DDataset destDS = m_model.getDatasetAt(dropLocation.getColumn());
                    if (sourceDS.getId() != destDS.getId()) return false;
                }
            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            // TODO : Determine whether we accept the location depending on the node type
            
            return (designNodeType == BIOLOGICAL_GROUP) || (designNodeType == BIOLOGICAL_SAMPLE_ANALYSIS);
            
        }
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        try {
            JTable.DropLocation dropLocation = ((JTable.DropLocation) support.getDropLocation());
            TreePath path = m_tree.getPathForRow(dropLocation.getRow());
            AbstractNode node = (path != null) ? (AbstractNode)path.getLastPathComponent() : null;
            DDataset ds = m_model.getDatasetAt(dropLocation.getColumn());
            
            XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
            XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());
            ArrayList<AbstractNode> dsNodesList = (ArrayList<AbstractNode>) data.getDesignList();
            
            if (dsNodesList == null) return false;
            
            for (AbstractNode dsNode : dsNodesList) {
                switch (dsNode.getType()) {
                    case BIOLOGICAL_GROUP: m_logger.info("moving group");
                    case BIOLOGICAL_SAMPLE_ANALYSIS: {
                        DQuantitationChannelMapping mapping = m_model.getMapping().get(node);
                        if (mapping != null) {
                            mapping.put(ds,((DataSetData)dsNode.getData()).getDataset().getNumber());
                        }
                    }
                }
            }
            
            return false;
        } catch (UnsupportedFlavorException ex) {
            m_logger.error(getClass().getSimpleName() + " DnD error ", ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
    
    

}
