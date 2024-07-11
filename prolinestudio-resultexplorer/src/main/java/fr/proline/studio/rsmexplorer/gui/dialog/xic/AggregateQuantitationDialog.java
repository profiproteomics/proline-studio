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

import fr.proline.core.orm.uds.*;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.xic.*;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
     *  Specify if this aggregation is done with only labelled quantitation, of same type
     */
    private boolean m_sameLabelledQuantMethodInfo;
    private boolean m_validLabelledQuant;
    List<QuantitationMethod> quantMethods;
    /**
     * Identification dataset reference, if commons to all quantitation
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

      return QuantExperimentalDesignTree.toExperimentalDesignParameters(m_experimentalDesignNode, m_refDataset, null);
    }

    public Map<String, Object> getQuantiParameters() {
        Map<String, Object> aggregationConfig = new HashMap<>();
        aggregationConfig.put("quantitation_ids", m_quantitations.stream().map(DDataset::getId).toArray());
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
        quantMethods = loadedQuantitations.stream().map(DDataset::getQuantitationMethod).toList();
        Set<DDatasetType.QuantitationMethodInfo> quantMethodInfos = loadedQuantitations.stream().map(DDataset::getQuantMethodInfo).collect(Collectors.toSet());
        m_sameLabelledQuantMethodInfo = quantMethodInfos.size() == 1;
        m_validLabelledQuant = m_sameLabelledQuantMethodInfo ? isAllQuantiLabelled() : false;

        //for each dataset in m_quantitations, get it's getIdentDataset of getMasterQuantitationChannels id, ds=DDataset, ids = IdentDataset
        List<Long> refDatasetIds = m_quantitations.stream().map(ds -> ds.getMasterQuantitationChannels().get(0).getIdentDataset()).map(ids -> (ids == null) ? -1 : ids.getId()).distinct().toList();
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
        if (m_quantitations != null && !m_quantitations.isEmpty()) {

            setButtonVisible(BUTTON_LOAD, false);
            setButtonVisible(BUTTON_SAVE, false);
            setButtonVisible(BUTTON_BACK, false);

            if (node != m_experimentalDesignNode) {
                m_experimentalDesignNode = node;
                
                m_designPanel = new JPanel();
                m_designPanel.setBorder(BorderFactory.createTitledBorder(" Experimental Design "));
                m_designPanel.setLayout(new BorderLayout());
                m_experimentalDesignPanel = new QuantAggregateExperimentalTreePanel(m_experimentalDesignNode, m_quantitations, m_validLabelledQuant);
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

        if(! m_experimentalDesignPanel.checkQCMapping()){
            setStatus(true, "Invalid Channel mapping");
            highlight(m_experimentalDesignPanel.getChannelPanel());
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
        boolean isInferred  = false;
//        if (m_sameLabelledQuantMethodInfo) {
//            isInferred =  inferExperimentalDesignFromQuantLabel(rootNode);
//        }

        if(!isInferred){
            int qcIndex = 1;
            List<String> sortedGroupList = new ArrayList<>();
            List<BiologicalGroup> groupList;
            XICBiologicalGroupNode biologicalGroupNode;
            Map<String, List<String>> groupSamplesMap = new HashMap<>();
            List<String> sampleNameList;
            // create Map<GroupName, List<BiologicalGroup>> by "collect" (ds = dataSet) (bg = BiologicalGroup)
            Map<String, List<BiologicalGroup>> allDSGroupsByName = m_quantitations.stream()
                    .map(ds -> ds.getGroupSetup().getBiologicalGroups())
                    .flatMap(Collection::stream)
                    .collect(Collectors.groupingBy(BiologicalGroup::getName, Collectors.toList()));

            for (DDataset dDataset : m_quantitations) {
                //create group List according first quanti group compositon
                groupList = dDataset.getGroupSetup().getBiologicalGroups();
                for (BiologicalGroup dsCurrentGroup : groupList) {
                    String groupName = dsCurrentGroup.getName();
                    //create or get tree group node with same name
                    int gIndex = sortedGroupList.indexOf(groupName);
                    if (gIndex == -1) {
                        sortedGroupList.add(groupName);
                        groupSamplesMap.put(groupName, new ArrayList<>());
                        biologicalGroupNode = new XICBiologicalGroupNode(DataSetData.createTemporaryAggregate(groupName));
                        rootNode.insert(biologicalGroupNode, childIndex++);
                    } else {
                        biologicalGroupNode = (XICBiologicalGroupNode) rootNode.getChildAt(gIndex + 1);//first node is rootNode
                    }

                    List<BiologicalSample> sampleList = dsCurrentGroup.getBiologicalSamples();
                    for (BiologicalSample dsCurrentSample : sampleList) {
                        String sampleName = dsCurrentSample.getName();
                        sampleNameList = groupSamplesMap.get(groupName); //reset sample list to current group's samples : outside for ..???  VDS
                        XICBiologicalSampleNode biologicalSampleNode;
                        int sIndex = sampleNameList.indexOf(sampleName);
                        if (sIndex == -1) { //first time this group->sample is seen. Create all needed sampleAnalysis node (for all datasets)
                            sampleNameList.add(sampleName);
                            String shortSampleName = shortenSampleName(groupName, sampleName);
                            biologicalSampleNode = new XICBiologicalSampleNode(DataSetData.createTemporaryAggregate(shortSampleName));
                            //create XICBiologicalSampleNode under biologicalGroupNode
                            biologicalGroupNode.insert(biologicalSampleNode, sampleNameList.size() - 1);

                            Map<String, List<BiologicalSample>> allDSSamplesBySplName = allDSGroupsByName.get(groupName).stream().map(BiologicalGroup::getBiologicalSamples).flatMap(Collection::stream).collect(Collectors.groupingBy(BiologicalSample::getName, Collectors.toList()));

                            //-- count SplAnalysisNode for all existing DS sample of current group
                            int maxReplicates = 0;
                            Map<QuantitationLabel,Integer> nbReplicateByLabel =new HashMap<>();
                            for (BiologicalSample bs : allDSSamplesBySplName.get(sampleName)) {
                                if(m_validLabelledQuant) {
                                    Map<QuantitationLabel, List<QuantitationChannel>> qChByLabel= bs.getQuantitationChannels().stream().collect(Collectors.groupingBy(QuantitationChannel::getQuantitationLabel, Collectors.toList()));
                                    for(QuantitationLabel currentLabel : qChByLabel.keySet()){
                                        if(!nbReplicateByLabel.containsKey(currentLabel))
                                            nbReplicateByLabel.put(currentLabel,0);
                                        int maxNblabel = nbReplicateByLabel.get(currentLabel);
                                        if(maxNblabel<qChByLabel.get(currentLabel).size())
                                            nbReplicateByLabel.put(currentLabel,qChByLabel.get(currentLabel).size());
                                    }
                                } else {
                                    int replicates = bs.getQuantitationChannels().size();
                                    maxReplicates = Math.max(replicates, maxReplicates);
                                }
                            }
                            if(m_validLabelledQuant) {
                                AtomicInteger labelMaxCount = new AtomicInteger(0);
                                nbReplicateByLabel.values().forEach(nb -> labelMaxCount.addAndGet(nb));
                                maxReplicates = labelMaxCount.get();
                            }

                            //-- create SplAnalysisNode
                            List<QuantitationLabel> allLabels = nbReplicateByLabel.keySet().stream().sorted(Comparator.comparingInt(QuantitationLabel::getNumber)).toList();
                            for (int i = 0; i < maxReplicates; i++) {
                                String suffix = "";
                                Long currentLabelId = null;
                                if(m_validLabelledQuant) {
                                    int nbCumulativeLabel = 0;
                                    for(QuantitationLabel nextLabel : allLabels){
                                        int nbCurrentLabel = nbReplicateByLabel.get(nextLabel);
                                        nbCumulativeLabel = nbCumulativeLabel+nbCurrentLabel;
                                        if(i<nbCumulativeLabel) {
                                            suffix ="-("+ nextLabel.getName()+")";
                                            currentLabelId = nextLabel.getId();
                                            break;
                                        }
                                    }
                                }
                                //for each channel, give it a name with a number order
                                String name = "Channel" +suffix +" "+ qcIndex++;
                                //empty DataSetData, who has only name
                                DataSetData dsData = DataSetData.createTemporaryIdentification(name);
                                //create a ChannelNode, with empty DataSetData;
                                XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(dsData);
                                sampleAnalysisNode.setQuantChannelName(name);
                                sampleAnalysisNode.setQuantLabelId(currentLabelId);
                                // created XICBiologicalSampleAnalysisNode under biologicalSampleNode
                                biologicalSampleNode.insert(sampleAnalysisNode, biologicalSampleNode.getChildCount());
                            }
                        }
                    } // end for DS samples
                } //end for DS groups
            } //end for all DSquant

            //For none labelled quanti, rename all channel if some channel are empty
            if(!m_validLabelledQuant) {
                int cIndex = 1;
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AbstractNode groupNode = (AbstractNode) rootNode.getChildAt(i);//groupe node
                    if (groupNode instanceof XICBiologicalGroupNode) {
                        for (int j = 0; j < groupNode.getChildCount(); j++) {
                            AbstractNode sampleNode = (AbstractNode) groupNode.getChildAt(j);
                            for (int k = 0; k < sampleNode.getChildCount(); k++) {
                                XICBiologicalSampleAnalysisNode cNode = (XICBiologicalSampleAnalysisNode) sampleNode.getChildAt(k);
                                cNode.setQuantChannelName("Channel " + cIndex++);
                            }
                        }
                    }
                }
            }
        }
        return rootNode;
    }

    private boolean isAllQuantiLabelled(){
        boolean isOK = true;
        for (DDataset dsQuanti : m_quantitations) {
            if (dsQuanti.getQuantitationMethod().getLabels() == null || dsQuanti.getQuantitationMethod().getLabels().isEmpty()) {
                isOK = false;
                break;
            }
            AtomicBoolean labelFound = new AtomicBoolean(true);
            dsQuanti.getMasterQuantitationChannels().get(0).getQuantitationChannels().forEach(qch ->{
                if(qch.getQuantitationLabel() == null)
                    labelFound.set(false); //Error in ds quant channels !
            });
            if(!labelFound.get()) {
                m_validLabelledQuant = false;
                isOK = false;
                break;
            }
        }
        return isOK;
    }

//    private Map<Long, Map<QuantitationLabel, Integer>> testAndGetLabelCountInfo(){
//        m_validLabelledQuant = true;
//        List<QuantitationLabel> labelForMethod = m_quantitations.get(0).getQuantitationMethod().getLabels();
//        Map<Long, Map<QuantitationLabel, Integer>> nQChannelByLabelByQuant = new HashMap<>();
//        for (DDataset dsQuanti : m_quantitations) {
//            //Count labels (qch with label)
//            Map<QuantitationLabel, Integer> nbQChannelByLabel = new HashMap<>();
//            AtomicBoolean labelFound = new AtomicBoolean(true);
//            dsQuanti.getMasterQuantitationChannels().get(0).getQuantitationChannels().forEach(qch ->{
//                if(qch.getQuantitationLabel() == null || !labelForMethod.contains(qch.getQuantitationLabel()))
//                    labelFound.set(false); //Error in ds quant channels !
//                else {
//                    int nbQtt = nbQChannelByLabel.getOrDefault(qch.getQuantitationLabel(), 0) + 1;
//                    nbQChannelByLabel.put(qch.getQuantitationLabel(), nbQtt);
//                }
//            });
//
//            if(!labelFound.get()) {
//                m_validLabelledQuant = false;
//                break;
//            }
//
//            nQChannelByLabelByQuant.put(dsQuanti.getId(), nbQChannelByLabel);
//
////            // set as max if it is the case ...
////            for(QuantitationLabel label : nbQChannelByLabel.keySet()){
////                int nbMax = maxNQChannelByLabel.getOrDefault(label,0);
////                if(nbQChannelByLabel.get(label)>nbMax) {
////                    maxNQChannelByLabel.put(label, nbQChannelByLabel.get(label));
////                }
////            }
//        }
//        return nQChannelByLabelByQuant;
//    }

//    private boolean inferExperimentalDesignFromQuantLabel(DataSetNode rootNode) {
//        // -- Use label information to create exp design
//
//        //Create Map of (max) number of labels per quantitation
//        Map<QuantitationLabel, Integer> maxNQChannelByLabel = new HashMap<>();
//        Map<Long, Map<QuantitationLabel, Integer>> nQChannelByLabelByQuant = testAndGetLabelCountInfo();
//
//        // search max
//        nQChannelByLabelByQuant.values().forEach( nbQChannelByLabel -> {
//            for(QuantitationLabel label : nbQChannelByLabel.keySet()){
//                int nbMax = maxNQChannelByLabel.getOrDefault(label,0);
//                if(nbQChannelByLabel.get(label)>nbMax) {
//                    maxNQChannelByLabel.put(label, nbQChannelByLabel.get(label));
//                }
//            }
//        });
//
//
//
////        m_validLabelledQuant = true;
////        for (DDataset dsQuanti : m_quantitations) {
////            //Count labels (qch with label)
////            Map<QuantitationLabel, Integer> nbQChannelByLabel = new HashMap<>();
////            AtomicBoolean labelFound = new AtomicBoolean(true);
////            dsQuanti.getMasterQuantitationChannels().get(0).getQuantitationChannels().forEach(qch ->{
////                if(qch.getQuantitationLabel() == null || !labelForMethod.contains(qch.getQuantitationLabel()))
////                    labelFound.set(false); //Error in ds quant channels !
////                else {
////                    int nbQtt = nbQChannelByLabel.getOrDefault(qch.getQuantitationLabel(), 0) + 1;
////                    nbQChannelByLabel.put(qch.getQuantitationLabel(), nbQtt);
////                }
////            });
////
////            if(!labelFound.get()) {
////                m_validLabelledQuant = false;
////                break;
////            }
////
////            nQChannelByLabelByQuant.put(dsQuanti.getId(), nbQChannelByLabel);
////
////            // set as max if it is the case ...
////            for(QuantitationLabel label : nbQChannelByLabel.keySet()){
////                int nbMax = maxNQChannelByLabel.getOrDefault(label,0);
////                if(nbQChannelByLabel.get(label)>nbMax) {
////                    maxNQChannelByLabel.put(label, nbQChannelByLabel.get(label));
////                }
////            }
////        }
//
//        //test if a dataset contains all labels
//        Long completeQChDSId = null;
//        boolean dsAreHomogenous = true;
//        String errMsg = "";
//        if(!m_validLabelledQuant){
//            errMsg = "- Dataset with no or invalid quant labels \n";
//            m_logger.warn(errMsg);
//        } else {
//            Map<QuantitationLabel, Integer> lastDSnbQChannelByLabel = null;
//            for (Long dsId : nQChannelByLabelByQuant.keySet()) {
//                Map<QuantitationLabel, Integer> currentDSnbQChannelByLabel = nQChannelByLabelByQuant.get(dsId);
//                if (currentDSnbQChannelByLabel.size() != maxNQChannelByLabel.size()) { //note same nb labels, can't contains all...
//                    dsAreHomogenous = false; //missing some qlabels, so DS are not homogenous
//                    continue;
//                }
//
//                boolean isMax = true; //suppose current DS contains all qch/labels
//                for (QuantitationLabel label : currentDSnbQChannelByLabel.keySet()) {
//                    Integer currentLabelCount = currentDSnbQChannelByLabel.getOrDefault(label, 0);
//                    if (!currentLabelCount.equals(maxNQChannelByLabel.get(label))) { //less qchannel for this label ...
//                        isMax = false;
//                        dsAreHomogenous = false;
//                    } else if (lastDSnbQChannelByLabel != null && dsAreHomogenous) { //no difference found, test this one
//                        dsAreHomogenous = lastDSnbQChannelByLabel.getOrDefault(label, 0).equals(currentLabelCount);
//                    }
//                }
//                lastDSnbQChannelByLabel = currentDSnbQChannelByLabel;
//                if (isMax) {
//                    completeQChDSId = dsId; //last one will be used
//                }
//            }
//
//            if(maxNQChannelByLabel.keySet().size()<m_quantitations.get(0).getQuantitationMethod().getLabels().size()){
//                errMsg = errMsg+"- There are missing labels\n ";
//            }
//        }
//        if (!dsAreHomogenous) {
//            errMsg = "- Not homogenous quant channel & labels \n";
//            m_logger.warn(errMsg);
//        }
//
//
//        if(completeQChDSId != null){
//            Long finalCompleteQChDSId = completeQChDSId;
//            DDataset matchingDs = m_quantitations.stream().filter(ds -> (ds.getId() == finalCompleteQChDSId)).findFirst().get();
//            if(!errMsg.isEmpty())
//                errMsg = "Warning: \n"+errMsg;
//            JOptionPane.showMessageDialog(this, " Use DS as template  "+matchingDs.getName()+" \n "+errMsg,"Found template DS", JOptionPane.INFORMATION_MESSAGE);
//            createDatasetTree(matchingDs,rootNode);
//
//        } else{
//            errMsg = "Warning: \n- No dataset found for template \n"+errMsg+" Default aggregation will be used";
//            JOptionPane.showMessageDialog(this, errMsg,"Error DS Template", JOptionPane.ERROR_MESSAGE);
//        }
//
//        return completeQChDSId != null;
//    }

//    private void createDatasetTree(DDataset dDataset,DataSetNode rootNode ){
//        int childIndex = rootNode.getChildCount();
//
//        //create group List according first quanti group compositon
//        List<BiologicalGroup> groupList = dDataset.getGroupSetup().getBiologicalGroups();
//        Map<Long, Integer> countByLabelId = new HashMap<>();
//        for (BiologicalGroup group : groupList) {
//            String groupName = group.getName();
//            XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(DataSetData.createTemporaryAggregate(groupName));
//            rootNode.insert(biologicalGroupNode, childIndex++);
//            int splIndex = 0;
//            for (BiologicalSample sample : group.getBiologicalSamples()) {
//                String sampleName = sample.getName();
//                String shortSampleName = shortenSampleName(groupName, sampleName);
//                XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(DataSetData.createTemporaryAggregate(shortSampleName));
//                //create XICBiologicalSampleNode under biologicalGroupNode
//                biologicalGroupNode.insert(biologicalSampleNode, splIndex);
//                List<QuantitationChannel> replicates = sample.getQuantitationChannels();
//
//                for (QuantitationChannel replicate : replicates) {
//                    QuantitationLabel label = replicate.getQuantitationLabel();
//                    String indexStr = "";
//                    int index =0;
//                    if(countByLabelId.containsKey(label.getId())) {
//                        index = countByLabelId.get(label.getId());
//                        indexStr = "-" + ( index + 1);
//                    }
//                    countByLabelId.put(label.getId(), ++index);
//                    //for each channel, give it a name with a number order
//                    String name = "Channel_" + label.getName()+indexStr;
//                    //empty DataSetData, who has only name
//                    DataSetData dsData = DataSetData.createTemporaryIdentification(name);
//                    //create a ChannelNode, with empty DataSetData;
//                    XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(dsData);
//                    sampleAnalysisNode.setQuantChannelName(name);
//                    sampleAnalysisNode.setQuantLabelId(label.getId());
//                    // created XICBiologicalSampleAnalysisNode under biologicalSampleNode
//                    biologicalSampleNode.insert(sampleAnalysisNode, biologicalSampleNode.getChildCount());
//                }
//            } // end for samples
//        } //end for groups
//}
}
