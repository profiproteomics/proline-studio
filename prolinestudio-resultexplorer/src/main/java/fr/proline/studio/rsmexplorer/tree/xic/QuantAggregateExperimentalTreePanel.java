package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.Exceptions;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultFloatingPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.DQuantitationChannelMapping;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuantAggregateExperimentalTreePanel extends JPanel {

    private AssociatedQuantExperimentalDesignTree m_tree;
    private ChannelPanel m_channelPanel;
    private DefaultFloatingPanel m_floattingButtonsPanel;
    private JTabbedPane m_tabbedPane;

    private ActionListener m_eraseAction;
    private ActionListener m_moveUpAction;
    private ActionListener m_moveDownAction;

    private List<DDataset> m_datasets;
    private Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> m_parentQCMappings;

    private static final int COLUMN_WIDTH_MIN = 100;
    private static final int COLUMN_WIDTH_MAX = 300;
    private static final int PAD = 5;

    public QuantAggregateExperimentalTreePanel(AbstractNode rootNode, List<DDataset> datasets) {

        // prepare data
        m_datasets = datasets;

        m_parentQCMappings = inferDefaultMapping(rootNode);
        JComponent treePanel = createTreePanel(rootNode);

        m_tabbedPane = new JTabbedPane();
        setMapping(datasets);

        setLayout(new BorderLayout());

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(treePanel);
        sp.setRightComponent(m_tabbedPane);
        sp.setDividerLocation(0.70);
        sp.setResizeWeight(0.5);

        add(sp, BorderLayout.CENTER);

    }

    private JComponent createTreePanel(AbstractNode rootNode) {

        m_tree = new AssociatedQuantExperimentalDesignTree(rootNode);
        m_channelPanel = new ChannelPanel();
        m_floattingButtonsPanel = createButtonsPanel();

        m_tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                m_channelPanel.treeChanged(false);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                m_channelPanel.treeChanged(false);
            }
        });

        m_tree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                m_channelPanel.treeChanged(false);
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                m_channelPanel.treeChanged(true);
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                m_channelPanel.treeChanged(true);
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                m_channelPanel.treeChanged(true);
            }
        });

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        treePanel.add(m_tree, c);

        c.gridx++;
        c.weightx = 1;
        treePanel.add(m_channelPanel, c);


        final JLayeredPane layeredPane = new JLayeredPane() {

            @Override
            public Dimension getPreferredSize() {
                return treePanel.getPreferredSize();
            }
        };

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                treePanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                layeredPane.revalidate();
                layeredPane.repaint();

                m_floattingButtonsPanel.setLocation(c.getWidth()-m_floattingButtonsPanel.getPreferredSize().width-5, 5);

            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        add(layeredPane, BorderLayout.CENTER);

        layeredPane.add(treePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_floattingButtonsPanel, JLayeredPane.PALETTE_LAYER);


        return layeredPane;
    }

    public List<Map<String, Object>> getQuantChannelsMatching() {

        // we need to do some cleaning : due to deleted or added channels

        // collect channels node
        LinkedHashSet<XICBiologicalSampleAnalysisNode> bioSampleAnalysisNodes = new LinkedHashSet<>();
        collectBioSampleAnalysisNodes((AbstractNode) m_tree.getModel().getRoot(), bioSampleAnalysisNodes);

        // remove unused DQuantitationChannelMapping

        HashSet<XICBiologicalSampleAnalysisNode> nodes = new HashSet<>(m_parentQCMappings.keySet());
        for (XICBiologicalSampleAnalysisNode node : nodes) {
            if (!bioSampleAnalysisNodes.contains(node)) {
                // node had been deleted
                m_parentQCMappings.remove(node);
            }
        }

        // re-index DQuantitationChannelMapping objects
        int index = 1;
        for (XICBiologicalSampleAnalysisNode node : bioSampleAnalysisNodes) {
            DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
            mapping.setParentQCNumber(index);
            index++;
        }


        List<Map<String, Object>> mappingList = new ArrayList<>();
        for (DQuantitationChannelMapping entry : m_parentQCMappings.values()) {
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("quant_channel_number", entry.getParentQCNumber());
            Map<Long, Long> map = entry.getMappedQuantChannels().entrySet().stream().filter(e -> (e.getValue() != null)).collect(Collectors.toMap(
                    e -> e.getKey().getMasterQuantitationChannels().get(0).getId(),
                    e -> e.getValue().getId()
            ));
            mapping.put("quant_channels_matching", map);
            mappingList.add(mapping);
        }

        return mappingList;
    }

    private void collectBioSampleAnalysisNodes(AbstractNode node, HashSet<XICBiologicalSampleAnalysisNode> bioSampleAnalysisNodes) {
        int nb = node.getChildCount();
        for (int i=0;i<nb;i++) {
            AbstractNode child = (AbstractNode) node.getChildAt(i);
            if (child.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                bioSampleAnalysisNodes.add((XICBiologicalSampleAnalysisNode) child);
            }
            collectBioSampleAnalysisNodes(child, bioSampleAnalysisNodes);
        }
    }


    public ChannelPanel getChannelPanel() {
        return m_channelPanel;
    }



    private DefaultFloatingPanel createButtonsPanel() {



        m_eraseAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_channelPanel.eraseDropZone();
            }
        };


        m_moveUpAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_channelPanel.moveDropZone(true);
            }
        };

        m_moveDownAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_channelPanel.moveDropZone(false);
            }
        };


        String[] actionText = {"", "", ""};
        ActionListener[] actionListeners = {m_eraseAction, m_moveUpAction, m_moveDownAction};
        Icon[] icons = {IconManager.getIcon(IconManager.IconType.ERASER), IconManager.getIcon(IconManager.IconType.ARROW_UP), IconManager.getIcon(IconManager.IconType.ARROW_DOWN)};

        DefaultFloatingPanel floattingPanel = new DefaultFloatingPanel(null, actionText, actionListeners, icons, false) {
            @Override
            public void actionFinished(boolean success, String errorMessage) {
                super.actionFinished(success, errorMessage);

            }

        };
        floattingPanel.setVisible(true);

        return floattingPanel;
    }

    /**
     * set data in left m_tableScrollPane, right m_tabbedPane, create for each
     * tab a QuantExperimentalDesignTree using DDataset(=Quantitation).
     *
     * @param datasets
     */
    public void setMapping(List<DDataset> datasets) {

        m_tabbedPane.removeAll();

        for (DDataset ds : datasets) {
            DataSetData datasetData = DataSetData.createTemporaryQuantitation(ds.getName()); //new DataSetData(ds.getName(), Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);
            datasetData.setDataset(ds);
            DataSetNode dsNode = new DataSetNode(datasetData);
            QuantExperimentalDesignTree dsDesignTree = new QuantExperimentalDesignTree(dsNode, false, false, true);
            QuantExperimentalDesignTree.displayExperimentalDesign(ds, dsNode, dsDesignTree, false, true);
            JScrollPane sPane = new JScrollPane(dsDesignTree);
            m_tabbedPane.add(ds.getName(), sPane);
        }
        validate();
        repaint();
    }


    public QuantExperimentalDesignTree getTree() {
        return m_tree;
    }


    /**
     * from root node(Tree data ), parse each group node, for each groupnode,
     * create a map (aggregated channel node - map(dataset -Quant channel) then
     * fill it with m_datasets
     *
     * @param node
     * @return
     */
    private Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> inferDefaultMapping(AbstractNode node) {

        Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> mappings = new HashMap<>();
        Stream<Object> groupStream = Collections.list(node.children()).stream()
                .filter(n -> (((AbstractNode) n).getType() == AbstractNode.NodeTypes.BIOLOGICAL_GROUP));
        AtomicInteger index = new AtomicInteger(1);
        List<XICBiologicalGroupNode> groupNodes = groupStream.map(o -> ((XICBiologicalGroupNode) o)).collect(Collectors.toList());
        for (XICBiologicalGroupNode groupNode : groupNodes) {
            Stream<Object> sampleStream = Collections.list(groupNode.children()).stream()
                    .filter(n -> (((AbstractNode) n).getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE));
            List<XICBiologicalSampleNode> sampleNodeList = sampleStream.map(s -> ((XICBiologicalSampleNode) s)).collect(Collectors.toList());
            for (XICBiologicalSampleNode sampleNode : sampleNodeList) {
                //@KX for each group, create a  DQuantitationChannelMapping, the parentQCNumber of DQuantitationChannelMapping increse+1 at each time
                //@KX so that each group has a map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping>, but the DQuantitationChannelMapping is empty
                //@KX (x = XICBiologicalSampleAnalysisNode), empty mapping which has only channel number
                //Map<ChannelNode, Mapping =(Aggregation channel, Map<Quanti-QuantiChannel>)
                Stream<XICBiologicalSampleAnalysisNode> sampleNodeStream = parseSample(sampleNode);
                Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> qcsNodes = sampleNodeStream.collect(
                        Collectors.toMap(x -> x, x -> new DQuantitationChannelMapping(index.getAndIncrement())));

                //@KX obtain all DQuantitationChannelMapping(empty util now) sorted by parentQCNumber Quant Channel idNumber
                //now fill the DQuantitationChannelMapping in qcNodes
                List<DQuantitationChannelMapping> sortedMappings = qcsNodes.values()
                        .stream()
                        .sorted(Comparator.comparing(DQuantitationChannelMapping::getParentQCNumber))
                        .collect(Collectors.toList());
                //now, parse each (starting) Quantitation DataSet
                for (DDataset ds : m_datasets) {
                    //for each  quntitation(DDataSet), find if it has a group who has a same name (bg = BiologicalGroup)
                    BiologicalGroup group = ds.getGroupSetup().getBiologicalGroups()
                            .stream()
                            .filter(bg -> bg.getName().equals(((DataSetData) groupNode.getData()).getTemporaryName()))
                            .findAny().orElse(null);
                    if (group != null) {
                        BiologicalSample sample = group.getBiologicalSamples()
                                .stream()
                                .filter(bs -> bs.getName().equals(getCompletSampleName(group, sampleNode)))
                                .findAny().orElse(null);
                        if (sample != null) {
                            //retreive all QuantitationChannel in all of the sample (bs=getBiologicalSamples)
                            List<QuantitationChannel> groupQcs = sample.getQuantitationChannels();
                            //for each QuantitationChannel
                            for (int k = 0; k < Math.min(groupQcs.size(), sortedMappings.size()); k++) {
                                //fill each mapping
                                sortedMappings.get(k).put(ds, groupQcs.get(k));
                            }
                        }
                    }
                }
                mappings.putAll(qcsNodes);
            }
        }
        return mappings;
    }

    private String getCompletSampleName(BiologicalGroup group, XICBiologicalSampleNode sampleNode) {
        String sampleCompletName;
        String groupName = group.getName();
        String sampleName = ((DataSetData) sampleNode.getData()).getTemporaryName();
        if (sampleName.startsWith(groupName)) {
            sampleCompletName = sampleName;
        } else {
            sampleCompletName = groupName + sampleName;
        }
        return sampleCompletName;
    }

    /**
     * from a Group, retrive all its Channel Nodes. Group->parse All
     * Samples->retrive each Channel
     *
     * @param groupNode
     * @return
     */
    private Stream<XICBiologicalSampleAnalysisNode> parseGroup(XICBiologicalGroupNode groupNode) {
        Stream<Object> stream = Collections.list(groupNode.children())//sampleNode in list
                .stream()
                .flatMap(sampleNode -> Collections.list(
                        ((AbstractNode) sampleNode).children()).stream()//XICBiologicalSampleAnalysisNode in stream
                );
        return stream.map(node -> ((XICBiologicalSampleAnalysisNode) node));//type cast
    }

    /**
     * from a sample, retrive all its Channel Nodes.
     *
     * @param sampleNode
     * @return
     */
    private Stream<XICBiologicalSampleAnalysisNode> parseSample(XICBiologicalSampleNode sampleNode) {
        Stream<Object> stream = Collections.list(sampleNode.children()).stream();
        return stream.map(node -> ((XICBiologicalSampleAnalysisNode) node));
    }

    private class AssociatedQuantExperimentalDesignTree extends QuantExperimentalDesignTree {

        public AssociatedQuantExperimentalDesignTree(AbstractNode rootNode) {
            super(rootNode, true, true, true);


        }

        public void paint(Graphics g) {
            super.paint(g);

            g.setColor(Color.lightGray);

            int nbRow = m_tree.getRowCount();

            for (int i = 0; i < nbRow; i++) {
                Rectangle r = m_tree.getRowBounds(i);

                TreePath path = m_tree.getPathForRow(i);
                AbstractNode node = (AbstractNode) path.getLastPathComponent();

                if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {


                    g.drawLine(r.x + r.width + PAD, r.y + r.height / 2, getPreferredSize().width, r.y + r.height / 2);

                }

            }

        }
    }


    public class ChannelPanel extends JPanel implements MouseListener, MouseMotionListener {

        private Dimension m_minimumDimension = null;
        private int m_columnWidth = COLUMN_WIDTH_MIN;

        private DropZone[][] m_dropZones; // in order col / row
        private int m_nbCols;
        private int m_nbRows;


        private ArrayList<DropZone> m_selectedDropZoneList = new ArrayList<>();
        private MouseEvent m_mouseDragBegin;

        private Font m_font = null;


        private TransferHandler m_transferHandler;

        public ChannelPanel() {

            setLayout(null);

            addMouseListener(this);
            addMouseMotionListener(this);


            m_transferHandler = new TransferHandler() {

                @Override
                public int getSourceActions(JComponent c) {

                    return MOVE;
                }

                @Override
                public Transferable createTransferable(JComponent c) {

                    ArrayList<DropZone> draggedList = new ArrayList<>(m_selectedDropZoneList);


                    DropZoneTransferable.TransferData data = new DropZoneTransferable.TransferData();
                    data.setDropZoneList(draggedList); //set Transferable data
                    Integer transferKey = DropZoneTransferable.register(data);
                    return new DropZoneTransferable(transferKey);
                }

                @Override
                public void exportDone(JComponent c, Transferable t, int action) {
                    // selection must be cleaned
                    for (DropZone dropZone : m_selectedDropZoneList) {
                        dropZone.setSelected(false);
                    }
                    m_selectedDropZoneList.clear();
                    updateFloattingButtons();
                }

                @Override
                public boolean canImport(TransferHandler.TransferSupport support) {
                    if (support.isDataFlavorSupported(XICSelectionTransferable.RSMNodeList_FLAVOR)) {
                        return canImportFromXICTree(support);
                    } else if (support.isDataFlavorSupported(DropZoneTransferable.DROPZONE_FLAVOR)) {
                        return canImportFromDropNode(support);
                    }

                    return false;

                }

                private boolean canImportFromXICTree(TransferHandler.TransferSupport support) {

                    boolean repaint = false;
                    try {


                        if (!support.isDrop()) {
                            if (!m_selectedDropZoneList.isEmpty()) {
                                for (DropZone dropZone : m_selectedDropZoneList) {
                                    dropZone.setSelected(false);
                                }
                                m_selectedDropZoneList.clear();
                                updateFloattingButtons();
                                repaint = true;
                            }

                            return false;
                        }


                        DropLocation dropLocation = support.getDropLocation();
                        Point p = dropLocation.getDropPoint();


                        for (DropZone[] dropZoneList : m_dropZones) {
                            for (DropZone dropZone : dropZoneList) {

                                if (dropZone.contains(p)) {

                                    boolean typeCompatible = false;
                                    try {
                                        XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                                        XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());


                                        boolean designData = data.isDesignData();
                                        if (designData) {
                                            AbstractNode.NodeTypes designNodeType = data.getDesignNodeType();
                                            if (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                                                ArrayList<AbstractNode> nodesList = data.getDesignList();
                                                if (nodesList != null && !nodesList.isEmpty()) {
                                                    AbstractData rootData = ((AbstractNode) nodesList.get(0).getRoot()).getData();
                                                    DDataset sourceDS = ((DataSetData) rootData).getDataset();
                                                    if (sourceDS == null) {
                                                        // happen for drag and drop from itself
                                                        //sourceDS = dropZone.getSourceDataset();
                                                    }
                                                    DDataset destDS = dropZone.getDataset();
                                                    if (sourceDS.getId() == destDS.getId()) {
                                                        typeCompatible = true;
                                                    }
                                                }

                                            }
                                        }


                                    } catch (UnsupportedFlavorException | IOException e) {

                                    }


                                    for (DropZone dropZoneCur : m_selectedDropZoneList) {
                                        dropZoneCur.setSelected(false);
                                    }
                                    m_selectedDropZoneList.clear();

                                    if (typeCompatible) {
                                        dropZone.setSelected(true);
                                        m_selectedDropZoneList.add(dropZone);
                                        repaint = true;
                                        updateFloattingButtons();
                                        return true;

                                    }
                                    updateFloattingButtons();
                                    repaint = true;

                                }
                            }
                        }


                        return false;
                    } finally {
                        if (repaint) {
                            repaint();
                        }
                    }


                }

                private boolean canImportFromDropNode(TransferHandler.TransferSupport support) {

                    boolean repaint = false;
                    try {


                        if (!support.isDrop()) {
                            if (!m_selectedDropZoneList.isEmpty()) {
                                for (DropZone dropZone : m_selectedDropZoneList) {
                                    dropZone.setSelected(false);
                                }
                                m_selectedDropZoneList.clear();
                                updateFloattingButtons();
                                repaint = true;
                            }

                            return false;
                        }


                        DropLocation dropLocation = support.getDropLocation();
                        Point p = dropLocation.getDropPoint();


                        for (DropZone[] dropZoneList : m_dropZones) {
                            for (DropZone dropZone : dropZoneList) {

                                if (dropZone.contains(p)) {

                                    boolean typeCompatible = false;
                                    try {
                                        DropZoneTransferable transfer = (DropZoneTransferable) support.getTransferable().getTransferData(DropZoneTransferable.DROPZONE_FLAVOR);
                                        DropZoneTransferable.TransferData data = DropZoneTransferable.getData(transfer.getTransferKey());


                                            ArrayList<DropZone> dropZonesSrc = data.getDropZoneList();

                                            if (dropZonesSrc != null && !dropZonesSrc.isEmpty()) {

                                                DDataset sourceDS = dropZonesSrc.get(0).getDataset();
                                                DDataset destDS = dropZone.getDataset();

                                                 if (sourceDS.getId() == destDS.getId()) {
                                                    typeCompatible = true;
                                                }
                                            }



                                    } catch (UnsupportedFlavorException | IOException e) {

                                    }


                                    for (DropZone dropZoneCur : m_selectedDropZoneList) {
                                        dropZoneCur.setSelected(false);
                                    }
                                    m_selectedDropZoneList.clear();

                                    if (typeCompatible) {
                                        dropZone.setSelected(true);
                                        m_selectedDropZoneList.add(dropZone);
                                        repaint = true;
                                        updateFloattingButtons();
                                        return true;

                                    }
                                    updateFloattingButtons();
                                    repaint = true;

                                }
                            }
                        }


                        return false;
                    } finally {
                        if (repaint) {
                            repaint();
                        }
                    }


                }


                @Override
                public boolean importData(TransferHandler.TransferSupport support) {
                    if (support.isDataFlavorSupported(XICSelectionTransferable.RSMNodeList_FLAVOR)) {
                        return importDataFromXic(support);
                    } else if (support.isDataFlavorSupported(DropZoneTransferable.DROPZONE_FLAVOR)) {
                        return importDataFromDropNode(support);
                    }
                    return false;
                }

                private boolean importDataFromXic(TransferHandler.TransferSupport support) {

                    try {
                        //get transfer data
                        XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                        XICSelectionTransferable.TransferData transferData = XICSelectionTransferable.getData(transfer.getTransferKey());
                        ArrayList<AbstractNode> transferDsNodesList = (ArrayList<AbstractNode>) transferData.getDesignList();
                        if (transferDsNodesList == null) {
                            return false;
                        }


                        DropZone selectedDropZone = m_selectedDropZoneList.get(0);
                        selectedDropZone.setSelected(false);
                        int dropRow = selectedDropZone.getRow();
                        int dropCol = selectedDropZone.getCol();

                        for (AbstractNode dsNode : transferDsNodesList) {

                            selectedDropZone = m_dropZones[dropCol][dropRow];

                            DDataset dropLocationDs = selectedDropZone.getDataset();
                            AbstractNode dropLocationNode = selectedDropZone.getNode();
                            DQuantitationChannelMapping mapping = m_parentQCMappings.get(dropLocationNode);

                            DataSetData userObject = (DataSetData) dsNode.getData();
                            mapping.put(dropLocationDs, userObject.getChannelNumber());

                            dropRow++;
                            if (dropRow>=m_nbRows) {
                                break;
                            }

                        }

                        m_selectedDropZoneList.clear();
                        updateFloattingButtons();
                        repaint();
                        return false;
                    } catch (UnsupportedFlavorException ex) {
                        //m_logger.error(getClass().getSimpleName() + " DnD error ", ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    for (DropZone dropZone : m_selectedDropZoneList) {
                        dropZone.setSelected(false);
                    }
                    m_selectedDropZoneList.clear();
                    updateFloattingButtons();

                    repaint();
                    return false;
                }

                private boolean importDataFromDropNode(TransferHandler.TransferSupport support) {

                    try {
                        //get transfer data
                        DropZoneTransferable transfer = (DropZoneTransferable) support.getTransferable().getTransferData(DropZoneTransferable.DROPZONE_FLAVOR);
                        DropZoneTransferable.TransferData transferData = DropZoneTransferable.getData(transfer.getTransferKey());

                        ArrayList<DropZone> transferDsNodesList = (ArrayList<DropZone>) transferData.getDropZoneList();
                        if (transferDsNodesList == null) {
                            return false; // should not happen
                        }

                        HashSet<DropZone> destinationDropZones = new HashSet<>();


                        DropZone selectedDropZone = m_selectedDropZoneList.get(0);
                        selectedDropZone.setSelected(false);
                        int dropRow = selectedDropZone.getRow();
                        int dropCol = selectedDropZone.getCol();

                        for (DropZone dropZoneSource : transferDsNodesList) {

                            selectedDropZone = m_dropZones[dropCol][dropRow];
                            destinationDropZones.add(selectedDropZone);

                            DDataset dropLocationDs = selectedDropZone.getDataset();
                            AbstractNode dropLocationNode = selectedDropZone.getNode();
                            DQuantitationChannelMapping mapping = m_parentQCMappings.get(dropLocationNode);

                            mapping.put(dropLocationDs, dropZoneSource.getChannel());

                            dropRow++;
                            if (dropRow>=m_nbRows) {
                                break;
                            }

                        }

                        // clear source DropZone when it is not a Destination in the same time
                        for (DropZone dropZoneSource : transferDsNodesList) {
                            if (!destinationDropZones.contains(dropZoneSource)) {
                                AbstractNode node = dropZoneSource.getNode();
                                DDataset ds = dropZoneSource.getDataset();
                                DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
                                mapping.put(ds, null);
                            }
                        }


                        m_selectedDropZoneList.clear();
                        updateFloattingButtons();
                        repaint();
                        return false;
                    } catch (UnsupportedFlavorException ex) {
                        //m_logger.error(getClass().getSimpleName() + " DnD error ", ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    for (DropZone dropZone : m_selectedDropZoneList) {
                        dropZone.setSelected(false);
                    }
                    m_selectedDropZoneList.clear();
                    updateFloattingButtons();

                    repaint();
                    return false;
                }

            };

            setTransferHandler(m_transferHandler);

        }

        public DropZone checkDuplicateDropZone() {
            for (int col=0;col< m_nbCols; col++) {
                for (int row=0;row< m_nbRows; row++) { //TOTOCHE
                    if (m_dropZones[col][row].getError()) {
                        return m_dropZones[col][row];
                    }
                }
            }
            return null;
        }




        public void treeChanged(boolean nbOfChannelsChanged) {

            if (nbOfChannelsChanged) {
                m_dropZones = null;

                // we must modify mapping
                AbstractNode rootNode = (AbstractNode) m_tree.getModel().getRoot();
                LinkedHashSet<XICBiologicalSampleAnalysisNode> bioSampleAnalysisNodes = new LinkedHashSet<>();
                collectBioSampleAnalysisNodes(rootNode, bioSampleAnalysisNodes);

                for (XICBiologicalSampleAnalysisNode node : bioSampleAnalysisNodes) {
                    if (! m_parentQCMappings.containsKey(node)) {
                        DQuantitationChannelMapping mapping = new DQuantitationChannelMapping(-1); //JPM.TODO : not sure for -1
                        for (DDataset d : m_datasets) {
                            mapping.put(d, null);
                        }

                        m_parentQCMappings.put(node, mapping);
                    }
                }

                /*for (XICBiologicalSampleAnalysisNode node : m_parentQCMappings.keySet()) {
                    if (!bioSampleAnalysisNodes.contains(node)) {
                        // node had been deleted
                        m_parentQCMappings.remove(node);
                    }
                }*/



            } else {


                // selection must be cleaned
                for (DropZone dropZone : m_selectedDropZoneList) {
                    dropZone.setSelected(false);
                }
            }
            m_selectedDropZoneList.clear();
            updateFloattingButtons();


            repaint();
        }




        @Override
        public Dimension getPreferredSize() {
            if (m_minimumDimension == null) {
                m_minimumDimension = new Dimension();
            }

            Dimension d = m_tree.getPreferredSize();

            m_minimumDimension.width = m_columnWidth * m_datasets.size() + PAD * 2;
            m_minimumDimension.height = d.height;

            return m_minimumDimension;
        }

        @Override
        public void paint(Graphics g) {

            // doing drag and drop, for unknown reason, font is change.
            // so get the font from the parent at the first call
            if (m_font == null) {
                m_font = g.getFont();
            } else {
                g.setFont(m_font);
            }

            // Prepare DropZones the first time
            if (m_dropZones != null) {
                int nbRow = 0;
                int nbRowInTree = m_tree.getRowCount();
                for (int i = 0; i < nbRowInTree; i++) {

                    TreePath path = m_tree.getPathForRow(i);
                    AbstractNode node = (AbstractNode) path.getLastPathComponent();


                    if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                        nbRow++;
                    }
                }

                if (nbRow != m_nbRows) {
                    m_dropZones = null; // we must recreate the structure
                }
            }
            if (m_dropZones == null) {
                m_nbCols = m_datasets.size();
                m_nbRows = 0;


                int nbRowInTree = m_tree.getRowCount();
                for (int i = 0; i < nbRowInTree; i++) {

                    TreePath path = m_tree.getPathForRow(i);
                    AbstractNode node = (AbstractNode) path.getLastPathComponent();


                    if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                        m_nbRows++;
                    }
                }

                m_dropZones = new DropZone[m_nbCols][m_nbRows];
                for (int i = 0; i < m_nbRows; i++) {
                    for (int j = 0; j < m_nbCols; j++) {
                        m_dropZones[j][i] = new DropZone();

                    }
                }
            }



            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.black);

            int nbRow = m_tree.getRowCount();

            m_duplicatesChannel.clear();

            boolean foundFirstSampleAnalysis = false;

            int textWidthNeededMin = 0;

            int rowIndex = 0;
            for (int i = 0; i < nbRow; i++) {
                Rectangle r = m_tree.getRowBounds(i);

                TreePath path = m_tree.getPathForRow(i);
                AbstractNode node = (AbstractNode) path.getLastPathComponent();


                if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {

                    g.setColor(Color.lightGray);
                    g.drawLine(0, r.y + r.height / 2, PAD + m_columnWidth * m_datasets.size(), r.y + r.height / 2);

                    DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);

                    Map<DDataset, QuantitationChannel> map = mapping.getMappedQuantChannels();


                    int colIndex = 0;
                    int decalX = 0;
                    for (DDataset d : m_datasets) {
                        QuantitationChannel channel = map.get(d);

                        DropZone dropZone = m_dropZones[colIndex][rowIndex];

                        dropZone.set(colIndex, rowIndex, d, channel, node);
                        dropZone.set(PAD + decalX, r.y, m_columnWidth - PAD, r.height);


                        dropZone.setError(false);
                        if (channel != null) {
                            DropZone dropZoneWithSameChannel = m_duplicatesChannel.get(channel.getId());
                            if (dropZoneWithSameChannel != null) {
                                dropZoneWithSameChannel.setError(true); // this one must be repainted
                                dropZoneWithSameChannel.paint(g);
                                dropZone.setError(true);

                            } else {
                                m_duplicatesChannel.put(channel.getId(), dropZone);
                            }
                        }

                        int textWidthNeeded = dropZone.paint(g);
                        if (textWidthNeeded > textWidthNeededMin) {
                            textWidthNeededMin = textWidthNeeded;
                        }

                        if (!foundFirstSampleAnalysis) {
                            // draw Title from dataset name
                            g.setColor(Color.black);
                            Rectangle titleRectangle = new Rectangle(PAD + decalX, r.y - r.height, m_columnWidth - PAD, r.height);
                            textWidthNeeded = paintCenterdString(g, titleRectangle, g.getFont(), d.getName());
                            if (textWidthNeeded > textWidthNeededMin) {
                                textWidthNeededMin = textWidthNeeded;
                            }
                        }

                        decalX += m_columnWidth;
                        colIndex++;
                    }

                    rowIndex++;
                    foundFirstSampleAnalysis = true;

                }

            }


            if (textWidthNeededMin>m_columnWidth) {
                m_columnWidth = Math.min(textWidthNeededMin+PAD*2, COLUMN_WIDTH_MAX);
                setPreferredSize(getPreferredSize());
            }

            //super.paintComponents(g);


        }
        private HashMap<Long, DropZone> m_duplicatesChannel = new HashMap<>(); // Long is id of QuantitationChannel


        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            // current col selected (can not select DropZones from different cols
            int currentSelectionCol = -1;
            if (!m_selectedDropZoneList.isEmpty()) {
                currentSelectionCol = m_selectedDropZoneList.get(0).getCol();
            }

            // look for the over DropZone
            DropZone overDropZone = null;
            searchOver:
            for (DropZone[] list : m_dropZones) {
                for (DropZone dropZone : list) {

                    if (dropZone.contains(e.getPoint())) {
                        m_mouseDragBegin = e;
                        overDropZone = dropZone;
                        break searchOver;
                    }
                }
            }
            if ((overDropZone != null) && (currentSelectionCol != -1) && (overDropZone.getCol() != currentSelectionCol)) {
                // deselect everything
                for (DropZone dropZone : m_selectedDropZoneList) {
                    dropZone.setSelected(false);
                }
                m_selectedDropZoneList.clear();
            }

            int modifier = e.getModifiers();

            if (overDropZone == null) {
                // remove all selections
                for (DropZone dropZone : m_selectedDropZoneList) {
                    dropZone.setSelected(false);
                }
                m_selectedDropZoneList.clear();

            } else {

                if (m_selectedDropZoneList.isEmpty()) {
                    // treated as no MASK
                    modifier = 0;
                }

                if ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) == 0) {
                    // no modifier

                    if (overDropZone.isSelected()) {
                        // for drag and drop support, we do not deselect other rows now
                        m_willSelectDropZone = overDropZone;
                    } else {
                        for (DropZone dropZone : m_selectedDropZoneList) {
                            dropZone.setSelected(false);
                        }
                        m_selectedDropZoneList.clear();
                        overDropZone.setSelected(true);
                        m_selectedDropZoneList.add(overDropZone);
                    }


                } else if ((modifier & (InputEvent.CTRL_MASK)) != 0) {
                    if (overDropZone.isSelected()) {
                        overDropZone.setSelected(false);
                        m_selectedDropZoneList.remove(overDropZone);
                    } else {
                        overDropZone.setSelected(true);
                        m_selectedDropZoneList.add(overDropZone);
                    }
                } else if ((modifier & (InputEvent.SHIFT_MASK)) != 0) {
                    // SHIFT KEY
                    // look for min and max row
                    int minRow = overDropZone.getRow();
                    int maxRow = minRow;
                    for (DropZone dropZone : m_selectedDropZoneList) {
                        int row = dropZone.getRow();
                        if (minRow > row) {
                            minRow = row;
                        }
                        if (maxRow < row) {
                            maxRow = row;
                        }
                    }
                    m_selectedDropZoneList.clear();

                    for (DropZone dropZone : m_dropZones[currentSelectionCol]) {
                        boolean select = dropZone.getRow() >= minRow && dropZone.getRow() <= maxRow;
                        dropZone.setSelected(select);
                        if (select) {
                            m_selectedDropZoneList.add(dropZone);
                        }
                    }


                }
            }

            updateFloattingButtons();

            revalidate();
            repaint();
        }
        private DropZone m_willSelectDropZone = null;

        @Override
        public void mouseReleased(MouseEvent e) {

            if (m_willSelectDropZone!= null) {

                // treat only now press on already selected dropZone (can not do it before due to potential drag & drop)

                for (DropZone dropZone : m_selectedDropZoneList) {
                    dropZone.setSelected(false);
                }
                m_selectedDropZoneList.clear();
                m_willSelectDropZone.setSelected(true);
                m_selectedDropZoneList.add(m_willSelectDropZone);
                updateFloattingButtons();

                m_willSelectDropZone = null;
            }

            // Right click popup
            if (e.isPopupTrigger() && !m_selectedDropZoneList.isEmpty()) {
                triggerPopup(e);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if ((m_mouseDragBegin != null) && (!m_selectedDropZoneList.isEmpty())) {
                int dx = m_mouseDragBegin.getX() - e.getX();
                int dy = m_mouseDragBegin.getY() - e.getY();
                if ((dx * dx + dy * dy) > 16) {
                    m_willSelectDropZone = null; // no deselection for previous pressed DropZone
                    m_transferHandler.exportAsDrag(this, m_mouseDragBegin, TransferHandler.MOVE);
                    m_mouseDragBegin = null;
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }


        //remove, up, down action
        private void triggerPopup(MouseEvent e) {

            if (m_popup == null) {

                m_popup = new JPopupMenu();

                JMenuItem item = new JMenuItem("Clear", IconManager.getIcon(IconManager.IconType.ERASER));
                item.addActionListener(m_eraseAction);
                m_popup.add(item);


                m_popup.addSeparator();

                item = new JMenuItem("Move Up", IconManager.getIcon(IconManager.IconType.ARROW_UP));
                item.addActionListener(m_moveUpAction);
                m_popup.add(item);

                item = new JMenuItem("Move Down", IconManager.getIcon(IconManager.IconType.ARROW_DOWN));
                item.addActionListener(m_moveDownAction);
                m_popup.add(item);
            }

            m_popup.show((JComponent) e.getSource(), e.getX(), e.getY());
        }
        private JPopupMenu m_popup = null;

        private void updateFloattingButtons() {
            boolean enabled = ! m_selectedDropZoneList.isEmpty();
            m_floattingButtonsPanel.enableAllButtons(enabled);
        }

        public void eraseDropZone() {
            for (DropZone dropZone : m_selectedDropZoneList) {
                dropZone.setSelected(false);
                dropZone.setChannel(null);
            }
            m_selectedDropZoneList.clear();

            updateFloattingButtons();

            updateTreeFromDropZone();
        }



        public void moveDropZone(boolean up) {

            int col = m_selectedDropZoneList.get(0).getCol();
            DropZone[] dropZoneList = m_dropZones[col];

            if (up) {
                for (int row = 1; row < dropZoneList.length; row++) {
                    DropZone dropZone = dropZoneList[row];
                    DropZone dropZonePrevious = dropZoneList[row - 1];
                    if (!dropZone.isSelected() || dropZonePrevious.isSelected()) {
                        // impossible to move the next one
                        continue;
                    }
                    m_dropZones[col][row - 1] = dropZone;
                    dropZone.setRow(row - 1);
                    m_dropZones[col][row] = dropZonePrevious;
                    dropZonePrevious.setRow(row);
                }

            } else { // down


                for (int row = dropZoneList.length - 2; row >= 0; row--) {
                    DropZone dropZone = dropZoneList[row];
                    DropZone dropZoneNext = dropZoneList[row + 1];
                    if (!dropZone.isSelected() || dropZoneNext.isSelected()) {
                        // impossible to move the previous one
                        continue;
                    }
                    m_dropZones[col][row + 1] = dropZone;
                    dropZone.setRow(row + 1);
                    m_dropZones[col][row] = dropZoneNext;
                    dropZoneNext.setRow(row);
                }
            }


            updateTreeFromDropZone();
        }

        private void updateTreeFromDropZone() {


            int nbRow = m_tree.getRowCount();


            int rowIndex = 0;
            for (int i = 0; i < nbRow; i++) {

                TreePath path = m_tree.getPathForRow(i);
                AbstractNode node = (AbstractNode) path.getLastPathComponent();

                if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {

                    DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);

                    Map<DDataset, QuantitationChannel> map = mapping.getMappedQuantChannels();

                    int colIndex = 0;
                    for (DDataset d : m_datasets) {
                        DropZone dropZone = m_dropZones[colIndex][rowIndex];

                        map.put(d, dropZone.getChannel());


                        colIndex++;
                    }

                    rowIndex++;

                }

            }


            repaint();
        }


    }



    private static final Color SELECTION_COLOR = javax.swing.UIManager.getColor("Table.selectionBackground");
    public class DropZone {

        private DDataset m_dataset;
        private AbstractNode m_node;
        private QuantitationChannel m_channel;

        private Rectangle m_rectangle = new Rectangle();

        private boolean m_selected = false;
        private boolean m_error = false;

        private int m_col;
        private int m_row;

        public DropZone() {
        }

        public int getCol() {
            return m_col;
        }

        public int getRow() {
            return m_row;
        }

        public void setRow(int row) {
            m_row = row;
        }

        public Rectangle getBounds() {
            return m_rectangle;
        }


        public QuantitationChannel getChannel() {
            return m_channel;
        }

        public void setChannel(QuantitationChannel channel) {
            m_channel = channel;
        }

        public void setSelected(boolean v) {
            m_selected = v;
        }

        public boolean isSelected() {
            return m_selected;
        }

        public void setError(boolean error) {
            m_error = error;
        }

        public boolean getError() {
            return m_error;
        }

        public int paint(Graphics g) {

            // background is :
            // white generally
            // blue when selected
            // white when on error
            g.setColor(m_selected ? SELECTION_COLOR : Color.white );
            g.fillRect(m_rectangle.x, m_rectangle.y, m_rectangle.width, m_rectangle.height);

            // frame is always black
            g.setColor(Color.black);
            g.drawRect(m_rectangle.x, m_rectangle.y, m_rectangle.width, m_rectangle.height);

            if (m_channel != null) {

                // writting is :
                // black generally
                // white when selected
                // red when on error
                g.setColor(m_error ? Color.red : m_selected ? Color.white : Color.black);

                return paintCenterdString(g, m_rectangle, g.getFont(), m_channel.getName());

            }

            return 0;
        }




        public void set(int col, int row, DDataset dataset, QuantitationChannel channel, AbstractNode node) {
            m_col = col;
            m_row = row;
            m_dataset = dataset;
            m_channel = channel;
            m_node = node;
        }

        public void set(int x, int y, int width, int height) {
            m_rectangle.setBounds(x, y, width, height);
        }

        public DDataset getDataset() {
            return m_dataset;
        }

        public AbstractNode getNode() {
            return m_node;
        }

        public boolean contains(Point p) {
            return m_rectangle.contains(p);
        }
    }


    public static class DropZoneTransferable implements Transferable, Serializable {

        public final static DataFlavor DROPZONE_FLAVOR = new DataFlavor(DropZone.class, "Drag and drop DropZone");


        private Integer m_transferKey = null;


        private static HashMap<Integer, fr.proline.studio.rsmexplorer.tree.xic.QuantAggregateExperimentalTreePanel.DropZoneTransferable.TransferData> transferMap = new HashMap<>();

        private static final DataFlavor[] DATA_FLAVORS = {DROPZONE_FLAVOR};

        private static final long serialVersionUID = 1L;

        public DropZoneTransferable(Integer transferKey) {
            m_transferKey = transferKey;
        }

        public static Integer register(fr.proline.studio.rsmexplorer.tree.xic.QuantAggregateExperimentalTreePanel.DropZoneTransferable.TransferData data) {

            Integer transferKey = Integer.valueOf(m_transferIndex);
            m_transferIndex++;
            transferMap.put(transferKey, data);
            return transferKey;
        }

        public static fr.proline.studio.rsmexplorer.tree.xic.QuantAggregateExperimentalTreePanel.DropZoneTransferable.TransferData getData(Integer transferKey) {
            return transferMap.get(transferKey);
        }

        public static void clearRegisteredData() {
            transferMap.clear();
        }

        private static int m_transferIndex = 0;

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return DATA_FLAVORS;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (DATA_FLAVORS[0].equals(flavor));
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return this;
            }

            return null;
        }

        public Integer getTransferKey() {
            return m_transferKey;
        }

        public static class TransferData {

            private ArrayList<DropZone> m_dropZoneList = null;

            public TransferData() {

            }


            public void setDropZoneList(ArrayList<DropZone> list) {
                m_dropZoneList = list;
            }

            public ArrayList<DropZone> getDropZoneList() {
                return m_dropZoneList;
            }


        }
    }


    public static int paintCenterdString(Graphics g, Rectangle zone, Font f, String s) {
        // Find the size of string s in font f in the current Graphics context g.
        FontMetrics fm = g.getFontMetrics(f);
        java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, g);

        int textHeight = (int) (rect.getHeight());
        int textWidth = (int) (rect.getWidth());

        String subText = null;
        int countSub = 1;
        while (textWidth>=COLUMN_WIDTH_MAX) {
            subText = ".."+s.substring(countSub++);
            rect = fm.getStringBounds(s, g);
            textWidth = (int) (rect.getWidth());
        }
        if (subText != null) {
            s = subText;
        }

        // Center text horizontally and vertically
        int x = zone.x + (zone.width - textWidth) / 2;
        int y = zone.y + (zone.height - textHeight) / 2 + fm.getAscent();

        g.drawString(s, x, y);  // Draw the string.

        return textWidth;
    }
}
