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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.xic.*;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class AggregateQuantitationDialog extends CheckDesignTreeDialog {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static AggregateQuantitationDialog m_singletonDialog = null;


    private QuantAggregateExperimentalTreePanel m_experimentalDesignPanel;

    public static AggregateQuantitationDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AggregateQuantitationDialog(parent);
        }
        return m_singletonDialog;
    }
    /**
     * step 1 Panel
     */
    private JPanel m_designPanel = null;
    /**
     * step 2 Panel
     */
    //private AggregationQuantChannelsPanel m_quantChannelsPanel;
    private AbstractNode m_experimentalDesignNode = null;
    /**
     * initals Quantitations
     */
    private List<DDataset> m_quantitations;
    /**
     * first Quantitation as reference
     */
    private DDataset m_refDataset = null;

    private AggregateQuantitationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Aggregate Quantitation Wizard");
        setDocumentationSuffix("id.2eclud0");//id(location) in help file
        setSize(750, 750);
        setResizable(true);
    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }

    public Map<String, Object> getExperimentalDesignParameters() throws IllegalAccessException {
        if (m_experimentalDesignNode == null) {
            throw new IllegalAccessException("Design parameters have not been set.");
        }

        Map<String, Object> experimentalDesignParams = QuantExperimentalDesignTree.toExperimentalDesignParameters(m_experimentalDesignNode, m_refDataset, null);
        return experimentalDesignParams;
    }

    public Map<String, Object> getQuantiParameters() {
        Map<String, Object> aggregationConfig = new HashMap<>();
        aggregationConfig.put("quantitation_ids", m_quantitations.stream().map(d -> d.getId()).toArray());
        //TOTOCHE
        aggregationConfig.put("quant_channels_mapping", m_experimentalDesignPanel.getQuantChannelsMatching());
        aggregationConfig.put("intensity_computation_method_name", "INTENSITY_SUM"); // change manually to MOST_INTENSE to test another summarization method
        return aggregationConfig;
    }

    public AbstractNode getExperimentalDesignNode() {
        return m_experimentalDesignNode;
    }

    /**
     * as soon as the constructor called, set data, m_quantitations and
     * m_refDataSet = first DataSet (Quantitation)
     *
     * @param loadedQuantitations selected Quantitations to Aggregate
     */
    public void setQuantitationDatasets(List<DDataset> loadedQuantitations) {
        m_quantitations = loadedQuantitations;
        //for each dataset in m_quantitations, get it's getIdentDataset of getMasterQuantitationChannels id, ds=DDataset, ids = IdentDataset
        List<Long> refDatasetIds = m_quantitations.stream().map(ds -> ds.getMasterQuantitationChannels().get(0).getIdentDataset()).map(ids -> (ids == null) ? -1 : ids.getId()).distinct().collect(Collectors.toList());
        // If all quantification datasets are using the same reference, set that reference to m_refDataset
        if (refDatasetIds.size() == 1 && refDatasetIds.get(0) != -1) {
            DDataset dataset = m_quantitations.get(0);
            m_refDataset = dataset.getMasterQuantitationChannels().get(0).getIdentDataset();
        } else {
            m_refDataset = null;
        }
        displayExperimentalDesignTree();
    }

    public void displayExperimentalDesignTree() {
        AbstractNode rootNode = inferExperimentalDesign();
        displayStep1ExperimentalDesignTree(rootNode);
    }

    /**
     * step 1 panel
     *
     * @param node
     */
    public void displayStep1ExperimentalDesignTree(AbstractNode node) {
        String step1Title = "";
        String step1Help = "<b>Left Panel: Define the aggregation experimental design</b><br><br>" +
                " The following experimental design was inferred from the quantitation that will be aggregated. Group, samples and channels (replicates) entities can be modified<br>"
                + " &nbsp - &nbsp Change entities <b>order</b> by drag and drop<br> "
                + " &nbsp - &nbsp <b>Rename</b> entities by contextual menu (right click)<br> "
                + " &nbsp - &nbsp <b>Create</b> or <b>delete</b> entities from the contextual menu<br><br>"
                + "<b>Center Panel: Define quantitation channels mapping</b><br><br>"
                + "Each quantitation channel of the aggregation will correspond to sample analyses of aggregated quantitations. The following modifications can be made: <br>"
                + " &nbsp - &nbsp <b>Change association</b> by dragging and dropping sample analysis from the right panel to a cell or from another cell<br>"
                + " &nbsp - &nbsp <b>Remove association</b> by using contextual menu or toolbar<br>"
                + " &nbsp - &nbsp <b>Move</b> analyses up or down by using contextual menu or toolbar";
        this.setHelpHeader(step1Title, step1Help);
        if (m_quantitations != null && m_quantitations.size() > 0) {

            setButtonVisible(BUTTON_LOAD, false);
            setButtonVisible(BUTTON_SAVE, false);
            setButtonVisible(BUTTON_BACK, false);

            if (node != m_experimentalDesignNode) {
                m_experimentalDesignNode = node;
                
                m_designPanel = new JPanel();
                m_designPanel.setBorder(BorderFactory.createTitledBorder(" Experimental Design "));
                m_designPanel.setLayout(new BorderLayout());

                m_experimentalDesignPanel = new QuantAggregateExperimentalTreePanel(m_experimentalDesignNode, m_quantitations);
                
                m_designPanel.add(m_experimentalDesignPanel, BorderLayout.CENTER);

                TreeUtils.expandTree(m_experimentalDesignPanel.getTree(), true);
            }
            replaceInternalComponent(m_designPanel);
            revalidate();
            repaint();
        }
    }


    @Override
    protected boolean okCalled() {
        if (!checkDesignStructure(m_experimentalDesignPanel.getTree(), m_experimentalDesignNode, new HashSet<>())) {
            return false;
        }
        if (!checkBiologicalGroupName(m_experimentalDesignPanel.getTree(), m_experimentalDesignNode)) {
            return false;
        }

        QuantAggregateExperimentalTreePanel.DropZone errorZone = m_experimentalDesignPanel.getChannelPanel().checkDuplicateDropZone();
        if (errorZone != null) {
            setStatus(true, "Channel is duplicated : "+errorZone.getChannel().getName());
            highlight(m_experimentalDesignPanel.getChannelPanel(), errorZone.getBounds());
            return false;
        }

        return true;

    }

    @Override
    protected boolean backCalled() {

        return false;
    }

    private String shortenSampleName(String groupName, String sampleName) {
        if (sampleName.startsWith(groupName)) {
            return sampleName.substring(groupName.length());
        }
        return sampleName;
    }

    /**
     * display, from m_quantitations, create the tree data, rootNode
     *
     * @return
     */
    private DataSetNode inferExperimentalDesign() {
        int childIndex = 0;
        DataSetNode rootNode = new DataSetNode(DataSetData.createTemporaryQuantitation("XIC Aggregation")); //new DataSetData("XIC Aggregation", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION));
        DatasetReferenceNode refDatasetNode = new DatasetReferenceNode(DataSetData.createTemporaryAggregate(m_refDataset == null ? "auto" : m_refDataset.getName()));//new DataSetData(m_refDataset == null ? "auto" : m_refDataset.getName(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
        if (m_refDataset != null) {
            Long refResultSummaryId = m_quantitations.get(0).getMasterQuantitationChannels().get(0).getIdentResultSummaryId();
            if (refResultSummaryId == null || !refResultSummaryId.equals(m_refDataset.getResultSummaryId())) {
                refDatasetNode.setInvalidReference(true);
            }
        }
        rootNode.insert(refDatasetNode, childIndex++);
        int qcIndex = 1;
        List<String> sortedGroupList = new ArrayList();
        List<BiologicalGroup> groupList;
        XICBiologicalGroupNode biologicalGroupNode;
        Map<String, List<String>> groupSamplesMap = new HashMap();
        List<String> sampleNameList;
        // create Map<GroupName, List<BiologicalGroup>> by "collect" (ds = dataSet) (bg = BiologicalGroup)
        Map<String, List<BiologicalGroup>> groups = m_quantitations.stream()
                .map(ds -> ds.getGroupSetup().getBiologicalGroups())
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(bg -> bg.getName(), Collectors.toList()));
        for (DDataset Quant : m_quantitations) {
            //create group List according first quanti group compositon
            groupList = Quant.getGroupSetup().getBiologicalGroups();
            List<String> sortedSampleList = new ArrayList();
            for (BiologicalGroup group : groupList) {
                String groupName = group.getName();
                int gIndex = sortedGroupList.indexOf(groupName);
                if (gIndex == -1) {
                    sortedGroupList.add(groupName);
                    groupSamplesMap.put(groupName, new ArrayList());
                    biologicalGroupNode = new XICBiologicalGroupNode(DataSetData.createTemporaryAggregate(groupName));
                    rootNode.insert(biologicalGroupNode, childIndex++);
                } else {
                    biologicalGroupNode = (XICBiologicalGroupNode) rootNode.getChildAt(gIndex + 1);//first node is rootNode
                }
                List<BiologicalSample> sampleList = group.getBiologicalSamples();
                for (BiologicalSample sample : sampleList) {
                    String sampleName = sample.getName();
                    sampleNameList = groupSamplesMap.get(groupName);
                    XICBiologicalSampleNode biologicalSampleNode;
                    int sIndex = sampleNameList.indexOf(sampleName);
                    if (sIndex == -1) {
                        sampleNameList.add(sampleName);
                        String shortSampleName = shortenSampleName(groupName, sampleName);
                        biologicalSampleNode = new XICBiologicalSampleNode(DataSetData.createTemporaryAggregate(shortSampleName));
                        //create XICBiologicalSampleNode under biologicalGroupNode
                        biologicalGroupNode.insert(biologicalSampleNode, sampleNameList.size() - 1);

                        Map<String, List<BiologicalSample>> samples = groups.get(groupName).stream().map(bg -> bg.getBiologicalSamples()).flatMap(Collection::stream).collect(Collectors.groupingBy(s -> s.getName(), Collectors.toList()));
                        int maxReplicates = 0;
                        for (BiologicalSample bs : samples.get(sampleName)) {
                            int replicates = bs.getQuantitationChannels().size();
                            maxReplicates = Math.max(replicates, maxReplicates);
                        }

                        for (int i = 0; i < maxReplicates; i++) {
                            //for each channel, give it a name with a number order
                            String name = "Channel " + Integer.toString(qcIndex++);
                            //empty DataSetData, who has only name
                            DataSetData dsData = DataSetData.createTemporaryIdentification(name);
                            //create a ChannelNode, with empty DataSetData; 
                            XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(dsData);
                            sampleAnalysisNode.setQuantChannelName(name);
                            // created XICBiologicalSampleAnalysisNode under biologicalSampleNode
                            biologicalSampleNode.insert(sampleAnalysisNode, biologicalSampleNode.getChildCount());
                        }
                    }
                }
            }
        }
        //rename all channel
        int cIndex = 1;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AbstractNode groupNode = (AbstractNode) rootNode.getChildAt(i);//groupe node
            if (XICBiologicalGroupNode.class.isInstance(groupNode)) {
                for (int j = 0; j < groupNode.getChildCount(); j++) {
                    AbstractNode sampleNode = (AbstractNode) groupNode.getChildAt(j);
                    for (int k = 0; k < sampleNode.getChildCount(); k++) {
                        XICBiologicalSampleAnalysisNode cNode = (XICBiologicalSampleAnalysisNode) sampleNode.getChildAt(k);
                        cNode.setQuantChannelName("Channel " + cIndex++);
                    }
                }
            }
        }
        return rootNode;
    }

}
