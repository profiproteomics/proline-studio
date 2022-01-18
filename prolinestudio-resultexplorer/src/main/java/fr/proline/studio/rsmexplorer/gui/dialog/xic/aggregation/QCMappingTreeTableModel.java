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
package fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.tree.TreeNode;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 *
 * @author CB205360
 */
public class QCMappingTreeTableModel extends AbstractTreeTableModel {

    //protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.QCMappingTreeTableModel");
    /**
     * Column List: Aggregation Quanti result tree data
     */
    List<DDataset> m_datasets;

    /**
     * map(row, map(column,value))<br>
     * DQuantitationChannelMapping = map(column, value)<br>
     * row = XICBiologicalSampleAnalysisNode,<br>
     * column = DDataset, <br>
     * value = QuantitationChannel.<br>
     */
    Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> m_parentQCMappings;

    public QCMappingTreeTableModel(AbstractNode rootNode, List<DDataset> datasets) {
        super(rootNode);//define the first Tree column 
        m_datasets = datasets;//other columns
        m_parentQCMappings = inferDefaultMapping(rootNode);// map(row, (column, value))
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

    /**
     *
     * @param o row node
     * @param column
     * @return
     */
    public String getToolTipText(Object o, int column) {
        DDataset ds = getDatasetAt(column);
        AbstractNode node = (AbstractNode) o;
        if (node != null && node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
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

    @Override
    public boolean isCellEditable(Object o, int column) {
        return false;
    }

    /**
     *
     * @param value
     * @param node, as row, aggregation quanti tree node,
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

    /**
     *
     * @param o: row object, here is the tree node object
     * @param columnIndex
     * @return
     */
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

    /**
     *
     * @param o row object, here is the tree node object
     * @param columnIndex
     * @return
     */
    public Object getChannelAt(Object o, int columnIndex) {
        AbstractNode node = (AbstractNode) o;
        if (columnIndex == 0) {
            return node.toString();
        } else if (XICBiologicalSampleAnalysisNode.class.isAssignableFrom(node.getClass())) {
            DDataset ds = m_datasets.get(columnIndex - 1);
            DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);
            if (mapping != null) {
                QuantitationChannel childQc = mapping.getMappedQuantChannels().get(ds);
                return childQc;
            }
        }
        return null;
    }

    /**
     *
     * @param o row object, here is the tree node object
     * @return
     */
    public DQuantitationChannelMapping getRowMapping(Object o) {
        if (o instanceof XICBiologicalSampleAnalysisNode) {
            return m_parentQCMappings.get(o);
        } else {
            return null;
        }
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

    public boolean isEmptyChannel(Object srcNode, int col) {
        if (col == 0) {
            return true;
        }
        DDataset Quanti = getDatasetAt(col);
        if (XICBiologicalSampleAnalysisNode.class.isInstance(srcNode)) {
            DQuantitationChannelMapping srcMapping = this.m_parentQCMappings.get((XICBiologicalSampleAnalysisNode) srcNode);
            QuantitationChannel srcChannel = srcMapping.getQuantChannel(Quanti);
            if (srcChannel == null) {
                return true;
            }
        }
        return false;
    }

    public String verifyRedundantChannel() {
        String r = "";
        for (DDataset ds : this.m_datasets) {
            String info = redundantChannel(ds);
            if (!info.isEmpty()) {
                r += info + "\n";
            }
        }
        return r;
    }

    protected String redundantChannel(DDataset quanti) {
        HashMap<String, List<XICBiologicalSampleAnalysisNode>> repeatedChannel = new HashMap(); //HashMap Channel Name- QC Parent Channel
        ArrayList<String> result = new ArrayList();
        Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> map = this.getMapping();
        for (XICBiologicalSampleAnalysisNode node : map.keySet()) {
            QuantitationChannel channel = map.get(node).getQuantChannel(quanti);
            if (channel != null) {
                List<XICBiologicalSampleAnalysisNode> nodes = repeatedChannel.get(channel.getName());
                if (nodes == null) {
                    nodes = new ArrayList();
                }
                nodes.add(node);
                repeatedChannel.put(channel.getName(), nodes);
            }
        }

        for (String channel : repeatedChannel.keySet()) {
            List<XICBiologicalSampleAnalysisNode> nodes = repeatedChannel.get(channel);
            if (nodes.size() > 1) {
                String s = channel + " repeated in ";
                s += nodes.stream().map(n -> n.toString()).collect(Collectors.joining(",", "{", "}"));
                result.add(s);
            }
        }
        if (result.isEmpty()) {
            return "";
        }
        return "In Column: " + quanti.getName() + "\n" + result.stream().collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return "QCMappingTreeTableModel{" + "m_datasets=" + m_datasets + ", m_parentQCMappings=" + m_parentQCMappings + '}';
    }

}
