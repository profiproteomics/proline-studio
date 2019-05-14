/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 16 avr. 2019
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import static fr.proline.studio.rsmexplorer.tree.AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
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
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class QCMappingTreeTableModel extends AbstractTreeTableModel {

    //protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.QCMappingTreeTableModel");
    /**
     * Aggregation Quanti result tree data
     */
    List<DDataset> m_datasets;
    /**
     * Quantitation(orignial) Tree data Map <AggregationChannelNode,
     * <AggregationChannelid, <QuantiDataset, QuantiChannel>>
     */
    Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> m_parentQCMappings, m_startingQCMappings;
    //m_startingQCMappings for alt+up/down
    /**
     * a ordered List according to tree row index, useful for getNodeAt()
     */
    List<AbstractNode> m_indexedNodes;
    /**
     * List, for ordred channel, memorize its tree row index
     */
    List<Integer> m_indexChannelNodes;
    /**
     * memorize which channel is selected, used for move Up/Down
     */
    List<Long> m_selectedChannelIds;

    public QCMappingTreeTableModel(AbstractNode rootNode, List<DDataset> datasets) {
        super(rootNode);
        m_datasets = datasets;
        m_parentQCMappings = inferDefaultMapping(rootNode);
        m_indexedNodes = new ArrayList<>();
        m_indexChannelNodes = new ArrayList<>();

        Enumeration en = rootNode.preorderEnumeration();
        while (en.hasMoreElements()) {
            AbstractNode node = (AbstractNode) en.nextElement();
            m_indexedNodes.add(node);//create a ordered List according to tree row index
            if (XICBiologicalSampleAnalysisNode.class.isInstance(node)) {
                m_indexChannelNodes.add(m_indexedNodes.size() - 1);
            }
        }
    }

    /**
     * Clone Mapping in m_startingQCMappings, used for Alt+Up/Down
     */
    public void cloneMapping() {
        m_startingQCMappings = new HashMap();
        for (XICBiologicalSampleAnalysisNode cn : m_parentQCMappings.keySet()) {
            DQuantitationChannelMapping copy = m_parentQCMappings.get(cn).clone();
            m_startingQCMappings.put(cn, copy);
            //m_logger.debug("channel {}, mapping: {}", cn, copy);
        }

    }

    /**
     * clean m_startingQCMappings, used for Alt+Up/Down
     */
    public void cleanStartingMapping() {
        m_startingQCMappings = new HashMap();
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
        AbstractNode node = (AbstractNode) o;
        if (node != null && node.getType() == BIOLOGICAL_SAMPLE_ANALYSIS) {
            DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
            Map<DDataset, QuantitationChannel> quantChannelMap = mapping.getMappedQuantChannels();
            if (quantChannelMap != null) {
                QuantitationChannel childQc = quantChannelMap.get(ds);
                if (childQc != null) {
                    return new StringBuilder(childQc.getName()).append(" (id=").append(childQc.getId()).append(", number=").append(childQc.getNumber()).append(")").toString();               
                }
            }
        }
        return null;
    }

    /**
     * from root node(Tree data ), parse each group node, for each groupnode,
     * create a map (aggregated channel node - map(dataset -Quant channel)
     *
     * @param node
     * @return
     */
    private Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> inferDefaultMapping(AbstractNode node) {

        Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> mappings = new HashMap<>();
        Stream<Object> groupStream = Collections.list(node.children()).stream().filter(n -> (((AbstractNode) n).getType() == AbstractNode.NodeTypes.BIOLOGICAL_GROUP));
        AtomicInteger index = new AtomicInteger(1);
        List<XICBiologicalGroupNode> groupNodes = groupStream.map(o -> ((XICBiologicalGroupNode) o)).collect(Collectors.toList());
        for (XICBiologicalGroupNode groupNode : groupNodes) {
            Stream<Object> sampleStream = Collections.list(groupNode.children()).stream().filter(n -> (((AbstractNode) n).getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE));
            List<XICBiologicalSampleNode> sampleNodeList = sampleStream.map(s -> ((XICBiologicalSampleNode) s)).collect(Collectors.toList());
            for (XICBiologicalSampleNode sampleNode : sampleNodeList) {
                //@KX for each group, create a  DQuantitationChannelMapping, the parentQCNumber of DQuantitationChannelMapping increse+1 at each time
                //@KX so that each group has a map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping>, but the DQuantitationChannelMapping is empty 
                //@KX (x = XICBiologicalSampleAnalysisNode), empty mapping which has only channel number
                //Map<ChannelNode, Mapping =(Aggregation channel, Map<Quanti-QuantiChannel>)
                Stream<XICBiologicalSampleAnalysisNode> sampleNodeStream = parseSample(sampleNode);
                Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> qcsNodes = sampleNodeStream.collect(Collectors.toMap(x -> x, x -> new DQuantitationChannelMapping(index.getAndIncrement())));

                //@KX obtain all DQuantitationChannelMapping(empty util now) sorted by parentQCNumber Quant Channel idNumber
                //now fill the DQuantitationChannelMapping in qcNodes
                List<DQuantitationChannelMapping> sortedMappings = qcsNodes.values().stream().sorted(Comparator.comparing(DQuantitationChannelMapping::getParentQCNumber)).collect(Collectors.toList());
                //now, parse each (starting) Quantitation DataSet
                for (DDataset ds : m_datasets) {
                    //for each  quntitation(DDataSet), find if it has a group who has a same name (bg = BiologicalGroup)
                    BiologicalGroup group = ds.getGroupSetup().getBiologicalGroups().stream().filter(bg -> bg.getName().equals(((DataSetData) groupNode.getData()).getTemporaryName())).findAny().orElse(null);
                    if (group != null) {
                        BiologicalSample sample = group.getBiologicalSamples().stream().filter(bs -> bs.getName().equals(getCompletSampleName(group, sampleNode))).findAny().orElse(null);
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
        Stream<Object> stream = Collections.list(groupNode.children()).stream().flatMap(node -> Collections.list(((AbstractNode) node).children()).stream());
        return stream.map(node -> ((XICBiologicalSampleAnalysisNode) node));
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

    @Override
    public boolean isCellEditable(Object o, int column) {
        return false;
    }

    /**
     *
     * @param value
     * @param node, aggregation quanti tree node,
     * @param column, starting quanti selected order number
     */
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
        AbstractNode node = (AbstractNode) o;
        if (columnIndex == 0) {
            return node.toString();
        } else if (XICBiologicalSampleAnalysisNode.class.isAssignableFrom(node.getClass())) {
            DDataset ds = m_datasets.get(columnIndex - 1);
            DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
            if (mapping == null) {
                return "<nothing>";
            }
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
        TreeNode node = (TreeNode) parent;
        return node.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        TreeNode node = (TreeNode) parent;
        return node.getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        AbstractNode node = (AbstractNode) parent;
        return node.getIndex((TreeNode) child);
    }

    public Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> getMapping() {
        return m_parentQCMappings;
    }

    public DDataset getDatasetAt(int column) {
        return m_datasets.get(column - 1);
    }

    /**
     * from rowIndex, get Node
     *
     * @param rowIndex
     * @return
     */
    public AbstractNode getNodeAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < m_indexedNodes.size()) {
            return m_indexedNodes.get(rowIndex);
        }
        return null;
    }

    /**
     * from one XICBiologicalSampleAnalysisNode get the next
     * XICBiologicalSampleAnalysisNode
     *
     * @param node
     * @return
     */
    public XICBiologicalSampleAnalysisNode getNextChannelNode(XICBiologicalSampleAnalysisNode node) {
        int i = m_indexedNodes.indexOf(node);
        int j = m_indexChannelNodes.indexOf(i) + 1;
        if (j < m_indexChannelNodes.size()) {
            int nextChannelIndex = m_indexChannelNodes.get(j);
            return (XICBiologicalSampleAnalysisNode) getNodeAt(nextChannelIndex);
        }
        return null;
    }

    public void remove(int row, int column) {
        AbstractNode node = this.m_indexedNodes.get(row);
        if (XICBiologicalSampleAnalysisNode.class.isInstance(node)) {
            DQuantitationChannelMapping mapping = this.m_parentQCMappings.get((XICBiologicalSampleAnalysisNode) node);
            DDataset Quanti = this.m_datasets.get(column - 1);
            mapping.remove(Quanti);
        }
    }

    /**
     *
     * @param row
     * @param column
     * @param weight, 1= down, -1 = up
     * @return the index of target node, or -1 if failed
     */
    public int moveUpDown(int row, int column, int weight, boolean isAltDown) {
        AbstractNode srcNode = this.m_indexedNodes.get(row);
        if (XICBiologicalSampleAnalysisNode.class.isInstance(srcNode)) {
            DDataset Quanti = this.m_datasets.get(column - 1);
            int targetIndex = m_indexChannelNodes.indexOf(row) + weight;
            int targetIndexChannelNode = m_indexChannelNodes.get(targetIndex);
            //m_logger.debug("targetIndex: {},{}", targetIndex, targetIndexChannelNode);
            DQuantitationChannelMapping srcMapping = this.m_parentQCMappings.get((XICBiologicalSampleAnalysisNode) srcNode);
            QuantitationChannel srcChannel = srcMapping.getQuantChannel(Quanti);

            AbstractNode targetNode = this.m_indexedNodes.get(targetIndexChannelNode);
            DQuantitationChannelMapping targetMapping = this.m_parentQCMappings.get((XICBiologicalSampleAnalysisNode) targetNode);

            if (srcChannel != null) {
                //m_logger.debug("selectedChannelId {}", m_selectedChannelIds.toString());
                if (!isAltDown) {
                    srcMapping.remove(Quanti);
                } else {
                    QuantitationChannel oldChannel = m_startingQCMappings.get(srcNode).getQuantChannel(Quanti);
                    if (oldChannel != null) {
                        //m_logger.debug("oldChannel id {}, name {}", oldChannel.getId(), oldChannel.getName());
                    }
                    if (oldChannel != null && !m_selectedChannelIds.contains(oldChannel.getId())) {
                        srcMapping.put(Quanti, oldChannel);
                        //m_logger.debug(" srcMapping: {}", srcMapping.toString());
                    } else {
                        srcMapping.remove(Quanti);
                    }
                }
                targetMapping.put(Quanti, srcChannel);
            } else {
                targetMapping.put(Quanti, null);

            }
            return targetIndexChannelNode;//row index of the table
        }
        return -1;
    }

    /**
     * call by TreeTable, set selected channel
     *
     * @param rowList
     * @param columnList
     */
    public void setSelected(int[] rowList, int[] columnList) {
        m_selectedChannelIds = new ArrayList();
        for (int row : rowList) {
            for (int col : columnList) {
                AbstractNode srcNode = this.m_indexedNodes.get(row);
                if (XICBiologicalSampleAnalysisNode.class.isInstance(srcNode)) {
                    DDataset Quanti = this.m_datasets.get(col - 1);
                    DQuantitationChannelMapping srcMapping = this.m_parentQCMappings.get((XICBiologicalSampleAnalysisNode) srcNode);
                    QuantitationChannel srcChannel = srcMapping.getQuantChannel(Quanti);
                    if (srcChannel != null) {
                        m_selectedChannelIds.add(srcChannel.getId());
                    }
                }
            }
        }
    }

    /**
     * determinate if the move up or move down reach the top/bottm
     *
     * @param selectedRowList
     * @param weight
     * @return
     */
    public boolean isEndChannel(List<Integer> selectedRowList, int weight) {
        for (int row : selectedRowList) {
            AbstractNode srcNode = this.m_indexedNodes.get(row);
            if (!XICBiologicalSampleAnalysisNode.class.isInstance(srcNode)) {
                continue;
            } else {
                int nextIndex = m_indexChannelNodes.indexOf(row) + weight;
                if (nextIndex < 0 || nextIndex >= m_indexChannelNodes.size()) {
                    return true;
                } else {
                    return false;
                }
            }

        }
        return false;
    }

    /**
     * test if at list 1 Channel is selected, used by TreeTable to determine if
     * trigger Popup Menu
     *
     * @param rowList
     * @param columnList
     * @return
     */
    public boolean isChannelSelected(int[] rowList, int[] columnList) {
        for (int row : rowList) {
            AbstractNode srcNode = this.m_indexedNodes.get(row);
            for (int column : columnList) {
                DDataset Quanti = this.m_datasets.get(column - 1);
                if (XICBiologicalSampleAnalysisNode.class.isInstance(srcNode)) {
                    DQuantitationChannelMapping srcMapping = this.m_parentQCMappings.get((XICBiologicalSampleAnalysisNode) srcNode);
                    QuantitationChannel srcChannel = srcMapping.getQuantChannel(Quanti);
                    if (srcChannel != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    DQuantitationChannelMapping m_holdChannel = new DQuantitationChannelMapping(0);

    /**
     * 1 row, several columns
     *
     * @param srcRow
     * @param targetRow
     * @param columnList
     * @param weight
     */
    public void preInsertMove(Integer srcRow, Integer targetRow, int[] columnList, int weight) {
        m_holdChannel = new DQuantitationChannelMapping(targetRow);
        int nextIndex = m_indexChannelNodes.indexOf(srcRow) + weight;
        int nextChannelIndex = m_indexChannelNodes.get(nextIndex);
        for (int column : columnList) {
            DQuantitationChannelMapping srcMapping = this.m_parentQCMappings.get((XICBiologicalSampleAnalysisNode) getNodeAt(nextChannelIndex));
            DDataset quanti = this.m_datasets.get(column - 1);
            QuantitationChannel srcChannel = srcMapping.getQuantChannel(quanti);
            m_holdChannel.put(quanti, srcChannel);
        }
    }

    void postInsertMove(Integer targetRow, int[] columnList) {
        int targetIndex = m_indexChannelNodes.indexOf(targetRow);
        int targetChannelIndex = m_indexChannelNodes.get(targetIndex);
        XICBiologicalSampleAnalysisNode targetNode = (XICBiologicalSampleAnalysisNode) this.getNodeAt(targetChannelIndex);
        for (int column : columnList) {
            DQuantitationChannelMapping mapping = m_parentQCMappings.get(targetNode);
            DDataset quanti = this.m_datasets.get(column - 1);
            mapping.remove(quanti);
            mapping.put(quanti, m_holdChannel.getQuantChannel(quanti));
        }
    }

}
