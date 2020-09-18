package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.Exceptions;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.DQuantitationChannelMapping;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.QCMappingTreeTable;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.QCMappingTreeTableModel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuantAggregateExperimentalTreePanel extends JPanel {

    private QuantExperimentalDesignTree m_tree;
    private ChannelPanel m_channelPanel;
    private JTabbedPane m_tabbedPane;


    private List<DDataset> m_datasets;
    private Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> m_parentQCMappings;

    private static final int COLUMN_WIDTH = 100;
    private static final int PAD = 5;

    public QuantAggregateExperimentalTreePanel(AbstractNode rootNode, List<DDataset> datasets) {

        // prepare data
        m_datasets = datasets;

        m_parentQCMappings = inferDefaultMapping(rootNode);
        JPanel treePanel = createTreePanel(rootNode);

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

    private JPanel createTreePanel(AbstractNode rootNode) {

        m_tree = new QuantExperimentalDesignTree(rootNode, true, true);
        m_channelPanel = new ChannelPanel();

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
        c.insets = new java.awt.Insets(5, 0, 0, 0);
        treePanel.add(m_channelPanel, c);

        return treePanel;
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
            QuantExperimentalDesignTree dsDesignTree = new QuantExperimentalDesignTree(dsNode, false, false);
            QuantExperimentalDesignTree.displayExperimentalDesign(ds, dsNode, dsDesignTree, false, true);
            JScrollPane sPane = new JScrollPane(dsDesignTree);
            m_tabbedPane.add(ds.getName(), sPane);
        }
        validate();
        repaint();
    }


    public  QuantExperimentalDesignTree getTree() {
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


    private class ChannelPanel extends JPanel {

        private Dimension m_minimumDimension = null;
        private ArrayList<DropZone> m_dropZones;
        private DropZone m_selectedDropZone = null;

        public ChannelPanel() {

            m_dropZones = new ArrayList<>();

            setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferHandler.TransferSupport support) {

                    boolean repaint = false;
                    try {


                        if (!support.isDrop()) {
                            repaint = (m_selectedDropZone != null);
                            m_selectedDropZone = null;
                            return false;
                        }


                        DropLocation dropLocation = support.getDropLocation();
                        Point p = dropLocation.getDropPoint();

                        for (DropZone dropZone : m_dropZones) {
                            if (dropZone.contains(p)) {

                                boolean typeCompatible = false;
                                try {
                                    XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                                    XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());


                                    boolean designData = data.isDesignData();
                                    if (designData) {
                                        AbstractNode.NodeTypes designNodeType = data.getDesignNodeType();
                                        if (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                                            ArrayList<AbstractNode> nodesList =  data.getDesignList();
                                            if (nodesList != null && !nodesList.isEmpty()) {
                                                AbstractData rootData = ((AbstractNode) nodesList.get(0).getRoot()).getData();
                                                DDataset sourceDS = ((DataSetData) rootData).getDataset();
                                                DDataset destDS = dropZone.getDataset();
                                                if (sourceDS.getId() == destDS.getId()) {
                                                    typeCompatible = true;
                                                }
                                            }

                                        }
                                    }


                                }  catch (UnsupportedFlavorException | IOException e) {

                                }

                                if (typeCompatible) {
                                    repaint = (dropZone != m_selectedDropZone);
                                    m_selectedDropZone = dropZone;

                                    return true;
                                } else {
                                    if (m_selectedDropZone != null) {
                                        m_selectedDropZone = null;
                                        repaint = true;
                                    }

                                    return false;
                                }

                            }
                        }


                        if (m_selectedDropZone != null) {
                            m_selectedDropZone = null;
                            repaint = true;
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
                    try {
                        //get transfer data
                        XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                        XICSelectionTransferable.TransferData transferData = XICSelectionTransferable.getData(transfer.getTransferKey());
                        ArrayList<AbstractNode> transferDsNodesList = (ArrayList<AbstractNode>) transferData.getDesignList();
                        if (transferDsNodesList == null) {
                            return false;
                        }

                        //get drop node
                        DDataset dropLocationDs = m_selectedDropZone.getDataset();
                        AbstractNode dropLocationNode = m_selectedDropZone.getNode();

                        //TOTOCHE
                        DQuantitationChannelMapping mapping = m_parentQCMappings.get(dropLocationNode);

                        for (AbstractNode dsNode : transferDsNodesList) {
                            DataSetData userObject = (DataSetData) dsNode.getData();
                            mapping.put(dropLocationDs, userObject.getChannelNumber());

                                                            /*
                                    int nextRowIndex = this.m_treeTable.getNextChannelRowIndex(currentRow, QCMappingTreeTable.DOWN);//DOWN ONLY
                                    if (nextRowIndex != -1) {
                                        dropChannelTreeNode = this.m_treeTable.getNodeForRow(nextRowIndex);
                                        currentRow = nextRowIndex;
                                    } else {
                                        dropChannelTreeNode = null;
                                    }
                                    if (dropChannelTreeNode == null) {
                                        continue;
                                    }*/
                        }

                        /*
                        String doubleChannel = m_treeTableModel.redundantChannel(dropLocationDs);
                        if (doubleChannel != null && !doubleChannel.isEmpty()) {

                            JOptionPane.showMessageDialog(m_treeTable, doubleChannel);
                        }*/

                        m_selectedDropZone = null;
                        repaint();
                        return false;
                    } catch (UnsupportedFlavorException ex) {
                        //m_logger.error(getClass().getSimpleName() + " DnD error ", ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    m_selectedDropZone = null;
                    repaint();
                    return false;
                }

            });

        }

        @Override
        public Dimension getPreferredSize() {
            if (m_minimumDimension == null) {
                m_minimumDimension = new Dimension();
            }

            Dimension d = m_tree.getPreferredSize();

            m_minimumDimension.width = COLUMN_WIDTH * m_datasets.size() + PAD * 2;
            m_minimumDimension.height = d.height;

            return m_minimumDimension;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.black);

            int nbRow = m_tree.getRowCount();


            int nodeNumber = 0;
            for (int i = 0; i < nbRow; i++) {
                Rectangle r = m_tree.getRowBounds(i);

                TreePath path = m_tree.getPathForRow(i);
                AbstractNode node = (AbstractNode) path.getLastPathComponent();

                if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                    DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);

                    Map<DDataset, QuantitationChannel> map = mapping.getMappedQuantChannels();

                    int colIndex = 0;
                    int decalX = 0;
                    for (DDataset d : m_datasets) {
                        QuantitationChannel channel = map.get(d);
                        if (channel != null) {
                            g.drawString(channel.getName(), PAD + decalX, r.y + r.height / 2);
                        }


                        int dropZoneIndex = colIndex + nodeNumber*m_datasets.size();

                        DropZone dropZone = null;
                        if (m_dropZones.size()<=dropZoneIndex) {
                            dropZone = new DropZone();
                            m_dropZones.add(dropZone);
                        } else {
                            dropZone = m_dropZones.get(dropZoneIndex);
                        }
                        dropZone.set(d, node);
                        dropZone.set(decalX, r.y, PAD+decalX+COLUMN_WIDTH, r.height);

                        decalX += COLUMN_WIDTH;
                        colIndex++;
                    }

                    nodeNumber++;

                }

            }

            if (m_selectedDropZone != null) {
                m_selectedDropZone.paint(g);
            }
        }
    }


    public class DropZone {

        private DDataset m_dataset;
        private AbstractNode m_node;
        private Rectangle m_rectangle = new Rectangle();

        public DropZone() {
        }

        public void paint(Graphics g) {
            g.setColor(Color.blue);
            g.drawRect(m_rectangle.x, m_rectangle.y, m_rectangle.width, m_rectangle.height);
        }

        public void set(DDataset dataset, AbstractNode node) {
            m_dataset = dataset;
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
}
