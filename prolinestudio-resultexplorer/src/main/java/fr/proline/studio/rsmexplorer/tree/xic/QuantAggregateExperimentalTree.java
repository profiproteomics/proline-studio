package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation.DQuantitationChannelMapping;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuantAggregateExperimentalTree extends QuantExperimentalDesignTree {

    private Dimension m_preferredSize;

    private List<DDataset> m_datasets;
    private Map<XICBiologicalSampleAnalysisNode, DQuantitationChannelMapping> m_parentQCMappings;

    private static final int COLUMN_WIDTH = 100;
    private static final int PAD = 5;

    public QuantAggregateExperimentalTree(AbstractNode rootNode, List<DDataset> datasets) {
        super(rootNode, true, true);

        m_datasets = datasets;
        m_parentQCMappings = inferDefaultMapping(rootNode);

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.red);
        int nbRow = getRowCount();

        int start = 0;
        for (int i = 0;i<nbRow;i++) {
            Rectangle r = getRowBounds(i);
            int xMax = r.x+  r.width;
            start = Math.max(start, xMax);
        }

        g.setColor(Color.black);

        for (int i = 0;i<nbRow;i++) {
            Rectangle r = getRowBounds(i);
            g.drawRect(r.x, r.y, r.width, r.height);

            TreePath path = getPathForRow(i);
            AbstractNode node = (AbstractNode) path.getLastPathComponent();

            if (node.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                DQuantitationChannelMapping mapping = m_parentQCMappings.get(node);

                Map<DDataset, QuantitationChannel> map = mapping.getMappedQuantChannels();

                int decalX = 0;
                for (DDataset d :  m_datasets) {
                    QuantitationChannel channel = map.get(d);

                    g.drawString(channel.getName(), start + decalX, r.y+r.height/2);

                    decalX += COLUMN_WIDTH;
                }

            }

        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (m_preferredSize == null) {
            m_preferredSize = new Dimension();
        }

        Dimension d = super.getPreferredSize();

        m_preferredSize.width = d.width + COLUMN_WIDTH*m_datasets.size() + PAD;
        m_preferredSize.height = d.height;

        return m_preferredSize;
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
}
