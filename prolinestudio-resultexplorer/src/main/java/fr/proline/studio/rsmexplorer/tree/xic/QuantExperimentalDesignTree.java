package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.BiologicalGroup;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.GroupSetup;
import fr.proline.core.orm.uds.SampleAnalysis;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.core.orm.uds.Run;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.actions.xic.CreateAction;
import fr.proline.studio.rsmexplorer.actions.xic.DeleteAction;
import fr.proline.studio.rsmexplorer.actions.xic.RenameAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.utils.StudioExceptions;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * QuantExperimentalDesignTree represents a quantitation experimental design
 * tree
 *
 * @author JM235353
 */
public class QuantExperimentalDesignTree extends AbstractTree {

    private JPopupMenu m_mainPopup;
    private ArrayList<AbstractRSMAction> m_mainActions;

    public QuantExperimentalDesignTree(AbstractNode top, boolean editable) {

        setEditable(editable);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        XICTransferHandler handler = new XICTransferHandler(false, this);
        super.setTransferHandler(handler);

        setDropMode(DropMode.ON_OR_INSERT);
        setDragEnabled(true);

        initTree(top);
    }

    @Override
    public void rename(AbstractNode rsmNode, String newName) {

        AbstractNode.NodeTypes nodeType = rsmNode.getType();
        if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP)
                || (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)
                || (nodeType == AbstractNode.NodeTypes.DATA_SET)) {

            ((DataSetData) rsmNode.getData()).setTemporaryName(newName);
        }
    }

    public void renameXicTitle(String newName) {
        AbstractNode rootNode = (AbstractNode) m_model.getRoot();
        rename(rootNode, newName);
        ((DefaultTreeModel) getModel()).nodeChanged(rootNode);
    }
    
    /**
     * a node type isPathEditable, can use F2 trigger rename
     * @param path
     * @return 
     */
    @Override
    public boolean isPathEditable(TreePath path) {

        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            return false;
        }

        if (isEditable()) {
            AbstractNode node = (AbstractNode) path.getLastPathComponent();
            AbstractNode.NodeTypes nodeType = node.getType();
            if ((nodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP)
                    || (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)
                    || (nodeType == AbstractNode.NodeTypes.DATA_SET)
                    || (nodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isEditable() && SwingUtilities.isRightMouseButton(e)) {
            triggerPopup(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void triggerPopup(MouseEvent e) {

        // retrieve selected nodes
        AbstractNode[] selectedNodes = getSelectedNodes();

        JPopupMenu popup;
        ArrayList<AbstractRSMAction> actions;

        // creation of the popup if needed
        if (m_mainPopup == null) {

            // create the actions
            m_mainActions = new ArrayList<AbstractRSMAction>(5);  // <--- get in sync

            CreateAction createAction = new CreateAction(this);
            m_mainActions.add(createAction);

            m_mainActions.add(null);

            RenameAction renameAction = new RenameAction(this);
            m_mainActions.add(renameAction);

            m_mainActions.add(null);  // separator

            DeleteAction deleteAction = new DeleteAction(this);
            m_mainActions.add(deleteAction);

            // add actions to popup
            m_mainPopup = new JPopupMenu();
            for (int i = 0; i < m_mainActions.size(); i++) {
                AbstractRSMAction action = m_mainActions.get(i);
                if (action == null) {
                    m_mainPopup.addSeparator();
                } else {
                    m_mainPopup.add(action.getPopupPresenter());
                }
            }

        }

        popup = m_mainPopup;
        actions = m_mainActions;

        // update of the enable/disable state
        for (int i = 0; i < actions.size(); i++) {
            AbstractRSMAction action = actions.get(i);

            if (action == null) {
                continue;
            }

            action.updateEnabled(selectedNodes);
        }

        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
    }

    /**
     * Replace current Experimental Design Node with an existing one. This is
     * called when Design Tree is used with an existing experimental Design : to
     * create XIC by clone, display XIC result (in quanti tree for instanceà) or
     * display experimental design already run ...
     *
     * @param dataset quantitiation DS to extract experimental design from
     * @param rootNode : root node for Experimental Design nodes
     * @param tree : tree to insert Experimental Design nodes
     * @param expandPath :specify if tree should be expanded or not
     * @param includeRunNodes : Add runNode to Experimental Design Tree. If not,
     * root Node are created but only added to XICBiologicalSampleAnalysisNode (
     * addXicRunNode method)
     */
    public static void displayExperimentalDesign(DDataset dataset, AbstractNode rootNode, AbstractTree tree, boolean expandPath, boolean includeRunNodes) {
        if (dataset == null) {
            return;
        }
        RSMTreeModel treeModel = (RSMTreeModel) tree.getModel();
        if (rootNode.getChildCount() > 0) {
            rootNode.removeAllChildren(); // remove the first child which correspond to the hour glass
            treeModel.nodeStructureChanged(rootNode);
        }

        rootNode.setIsChanging(false);
        treeModel.nodeChanged(rootNode);
        GroupSetup groupSetup = dataset.getGroupSetup();
        if (groupSetup == null) {
            return;
        }
        List<DQuantitationChannel> listQuantChannels = dataset.getMasterQuantitationChannels().isEmpty() ? new ArrayList() : dataset.getMasterQuantitationChannels().get(0).getQuantitationChannels();
        List<BiologicalGroup> listBiologicalGroups = groupSetup.getBiologicalGroups();
        int childIndex = 0;

        // insert a node to represent reference identification dataset
        DDataset refDataset = dataset.getMasterQuantitationChannels().get(0).getIdentDataset();
        Long refResultSummaryId = dataset.getMasterQuantitationChannels().get(0).getIdentResultSummaryId();
        DatasetReferenceNode refDatasetNode = new DatasetReferenceNode(DataSetData.createTemporaryAggregate(refDataset == null ? "auto" : refDataset.getName())); //new DataSetData(refDataset == null ? "auto" : refDataset.getName(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
        if (refDataset != null) {
            if (refResultSummaryId == null || !refResultSummaryId.equals(refDataset.getResultSummaryId())) {
                refDatasetNode.setInvalidReference(true);
            }
        }
        treeModel.insertNodeInto(refDatasetNode, rootNode, childIndex++);

        //insert a node to represents aggregated datasets
        if (dataset.isAggregation()) {
            DatasetReferenceNode aggregationNode = new DatasetReferenceNode(DataSetData.createTemporaryAggregate("Aggregation"), true);
            treeModel.insertNodeInto(aggregationNode, rootNode, childIndex++);
            try {
                ArrayList<Integer> refDatasetIs = (ArrayList<Integer>) dataset.getQuantProcessingConfigAsMap().get("quantitation_ids");
                int refDatasetIndex = 0;
                for (Integer datasetId : refDatasetIs) {
                    DataSetNode node = new DataSetNode(DataSetData.createTemporaryQuantitation(datasetId.toString()));
                    loadDataSet(datasetId.longValue(), node);
                    node.setIsReference();
                    treeModel.insertNodeInto(node, aggregationNode, refDatasetIndex++);                    
                }
                
            } catch (Exception e) {
                StudioExceptions.notify("Unable to build aggregated datasets nodes", e);
            }
        }

        for (BiologicalGroup bioGroup : listBiologicalGroups) {
            XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(DataSetData.createTemporaryAggregate(bioGroup.getName())); //new DataSetData(bioGroup.getName(), Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
            treeModel.insertNodeInto(biologicalGroupNode, rootNode, childIndex);
            List<BiologicalSample> listSample = bioGroup.getBiologicalSamples();
            int childSampleIndex = 0;
            for (BiologicalSample biologicalSample : listSample) {
                String sampleName = biologicalSample.getName();
                // split the name because in Studio the sampleName is saved as groupName+sampleName see Issue#12628
                if (sampleName.startsWith(bioGroup.getName())) {
                    sampleName = sampleName.substring(bioGroup.getName().length());
                }
                XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(DataSetData.createTemporaryAggregate(sampleName)); //new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                treeModel.insertNodeInto(biologicalSampleNode, biologicalGroupNode, childSampleIndex);
                int childSampleAnalysisIndex = 0;
                for (SampleAnalysis sampleAnalysis : biologicalSample.getSampleAnalyses()) {
                    DQuantitationChannel qCh = getQuantChannelSampleAnalysis(sampleAnalysis, listQuantChannels);
                    if (qCh != null) {
                        String name = qCh.getName();
                        DataSetData dsData = DataSetData.createTemporaryIdentification(name); //new DataSetData(name, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS);
                        if (qCh.getIdentRs() != null) {
                            // fake dataset
                            DDataset dds = new DDataset(qCh.getIdentDatasetId(), dataset.getProject(), name, Dataset.DatasetType.IDENTIFICATION, 0, qCh.getIdentRs().getId(), qCh.getIdentResultSummaryId(), qCh.getNumber());
                            dds.setResultSet(qCh.getIdentRs());
                            dsData.setDataset(dds);
                        }
                        XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(dsData);
                        if (qCh.getName() != null) {
                            sampleAnalysisNode.setQuantChannelName(qCh.getName());
                        }

                        if (qCh.getRun() != null) {
                            Run run = qCh.getRun();
                            RawFile rawFile = run.getRawFile();
                            RunInfoData runInfoData = new RunInfoData();
                            runInfoData.setLinkedRawFile(rawFile);
                            runInfoData.setRun(run);
                            runInfoData.setStatus(RunInfoData.Status.LINKED_IN_DATABASE);

                            XICRunNode runNode = new XICRunNode(runInfoData, treeModel);
                            if (includeRunNodes) {
                                sampleAnalysisNode.add(runNode);
                            } else {
                                sampleAnalysisNode.addXicRunNode(runNode, false);
                            }
                        }
                        treeModel.insertNodeInto(sampleAnalysisNode, biologicalSampleNode, childSampleAnalysisIndex);
                        if (expandPath) {
                            tree.expandPath(new TreePath(sampleAnalysisNode.getPath()));
                        } else {
                            tree.expandPath(new TreePath(biologicalSampleNode.getPath()));
                        }
                        childSampleAnalysisIndex++;
                    }
                }
                childSampleIndex++;
            }
            if (dataset.isAggregation()) {//aggragation dataset can't expand by above code
                tree.expandPath(new TreePath(biologicalGroupNode.getPath()));
               for (Enumeration e = biologicalGroupNode.children(); e.hasMoreElements();) {
                   XICBiologicalSampleNode samplNode = (XICBiologicalSampleNode) e.nextElement();
                   tree.expandPath(new TreePath(samplNode.getPath()));
               }
            }
            childIndex++;
        }
    }

    /**
     * use quantiDatasetId as id, load data from database, to fill the object
     * datasetNode
     *
     * @param quantiDatasetId
     * @param datasetNode, empty datasetNode
     */
    private static void loadDataSet(Long quantiDatasetId, final DataSetNode datasetNode) {

        final ArrayList<DDataset> readDatasetList = new ArrayList<>(1);
        final QuantitationTree tree = QuantitationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        AbstractDatabaseCallback readDatasetCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    DDataset ds = readDatasetList.get(0);
                    ((DataSetData) datasetNode.getData()).setDataset(ds);
                    datasetNode.setIsChanging(false);
                    treeModel.nodeChanged(datasetNode);
                } else {
                    treeModel.removeNodeFromParent(datasetNode);
                }
            }
        };

        DatabaseDataSetTask task = new DatabaseDataSetTask(readDatasetCallback);
        task.initLoadDataset(quantiDatasetId, readDatasetList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    private static DQuantitationChannel getQuantChannelSampleAnalysis(SampleAnalysis sampleAnalysis, List<DQuantitationChannel> listQuantChannels) {
        for (DQuantitationChannel qCh : listQuantChannels) {
            SampleAnalysis sampleReplicate = qCh.getSampleReplicate();
            if (sampleReplicate != null && sampleReplicate.getId() == sampleAnalysis.getId()) {
                return qCh;
            }
        }
        return null;
    }

    /**
     * Convert Tree hierarchy from specified node, which should contain Xic
     * specific nodes : groups, samples ....) to a Map containing corresponding
     * experimental design structure
     *
     * @param node : root node containing XIC Experimental Design nodes
     * @param refDataset : Dataset to used as Reference dataset for
     * MasterQuantChannel, may be null if no reference Dataset specified
     * @param refRsmId : Result Summary Id to use as Reference identification
     * result summary for MasterQuantChannel. If null and reference dataset
     * specified, use result summatu Id of dataset
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> toExperimentalDesignParameters(AbstractNode node, DDataset refDataset, Long refRsmId) throws IllegalAccessException {
        Map<String, Object> experimentalDesignParams = new HashMap<>();
        String errorMsg = null;
        //Number used for Experimental Design data
        int ratioNumeratorGrp = 1;
        int ratioDenominatorGrp = 1;
        int grpNumber = 1;
        int splNumber = 1;
        int splAnalysisNumber = 1;
        List _biologicalGroupList = new ArrayList();
        HashMap<String, Long> _rsmIdBySampleAnalysis = new HashMap<>();
        HashMap<Long, Long> _runIdByRSMId = new HashMap<>();
        HashMap<String, ArrayList<String>> _samplesAnalysisBySample = new HashMap<>();
        Map<Integer, String> splNameByNbr = new HashMap<>();
        List<Long> rsmIdsToGetRunIdsFor = new ArrayList();
        Enumeration xicGrps = node.children();
        while (xicGrps.hasMoreElements() && errorMsg == null) {
            AbstractNode grpNode = (AbstractNode) xicGrps.nextElement();

            if (XICBiologicalGroupNode.class.isAssignableFrom(grpNode.getClass())) {
                String grpName = grpNode.getData().getName();

                //Iterate over Samples
                Enumeration grpSpls = grpNode.children();
                List<Integer> splNumbers = new ArrayList();

                while (grpSpls.hasMoreElements() && errorMsg == null) {
                    AbstractNode splNode = (AbstractNode) grpSpls.nextElement();
                    String sampleName = grpName + splNode.getData().getName();
                    splNameByNbr.put(splNumber, sampleName);
                    splNumbers.add(splNumber++);

                    //Iterate over SampleAnalysis
                    Enumeration identRSMs = splNode.children();
                    ArrayList<String> splAnalysisNames = new ArrayList<>();
                    while (identRSMs.hasMoreElements()) {
                        //VD TODO TEST child type
                        XICBiologicalSampleAnalysisNode qChannelNode = (XICBiologicalSampleAnalysisNode) identRSMs.nextElement();
                        XICRunNode associatedRunNode = qChannelNode.getXicRunNode();
                        String quantChName = qChannelNode.getQuantChannelName();
                        if ((quantChName == null) || (quantChName.isEmpty())) {
                            quantChName = qChannelNode.getData().getName();
                        }
                        splAnalysisNames.add(quantChName);
                        if (!DataSetData.class.isInstance(qChannelNode.getData())) {
                            errorMsg = "Invalide Sample Analysis specified ";
                            break;
                        }

                        if (qChannelNode.getData() != null && ((DataSetData) qChannelNode.getData()).getDataset() != null) {
                            Long rsmId = ((DataSetData) qChannelNode.getData()).getDataset().getResultSummaryId();
                            _rsmIdBySampleAnalysis.put(quantChName, rsmId);
                            if (associatedRunNode != null && ((RunInfoData) associatedRunNode.getData()).getRun() != null) {
                                _runIdByRSMId.put(rsmId, ((RunInfoData) associatedRunNode.getData()).getRun().getId());
                            } else {
                                rsmIdsToGetRunIdsFor.add(rsmId);
                            }
                        }
                    }
                    _samplesAnalysisBySample.put(sampleName, splAnalysisNames);
                } //End go through group's sample

                Map<String, Object> biologicalGroupParams = new HashMap<>();
                biologicalGroupParams.put("number", grpNumber++);
                biologicalGroupParams.put("name", grpName);
                biologicalGroupParams.put("sample_numbers", splNumbers);
                _biologicalGroupList.add(biologicalGroupParams);
            }
        }// End go through groups
        //Read Run IDs for not found run Ids
        if (!rsmIdsToGetRunIdsFor.isEmpty()) {
            _runIdByRSMId.putAll(getRunIdForRSMs(rsmIdsToGetRunIdsFor));
        }
        if (errorMsg != null) {
            throw new IllegalAccessException(errorMsg);
        }
        if (grpNumber > 2) {
            ratioDenominatorGrp = 2; //VD TODO :  Comment gerer les ratois ?
        }
        Map<String, Object> ratioParams = new HashMap<>();
        ratioParams.put("number", 1);
        ratioParams.put("numerator_group_number", ratioNumeratorGrp);
        ratioParams.put("denominator_group_number", ratioDenominatorGrp);
        List ratioParamsList = new ArrayList();
        ratioParamsList.add(ratioParams);
        Map<String, Object> groupSetupParams = new HashMap<>();
        groupSetupParams.put("number", 1);
        groupSetupParams.put("name", node.getData().getName());
        groupSetupParams.put("biological_groups", _biologicalGroupList);
        groupSetupParams.put("ratio_definitions", ratioParamsList);
        ArrayList groupSetupParamsList = new ArrayList();
        groupSetupParamsList.add(groupSetupParams);
        experimentalDesignParams.put("group_setups", groupSetupParamsList);
        List biologicalSampleList = new ArrayList();
        List quantChanneList = new ArrayList();
        List<Integer> samplesNbrList = new ArrayList(splNameByNbr.keySet());
        Collections.sort(samplesNbrList);
        Iterator<Integer> samplesNbrIt = samplesNbrList.iterator();
        while (samplesNbrIt.hasNext()) {
            Integer nextSplNbr = samplesNbrIt.next();
            String nextSpl = splNameByNbr.get(nextSplNbr);

            Map<String, Object> biologicalSampleParams = new HashMap<>();
            biologicalSampleParams.put("number", nextSplNbr);
            biologicalSampleParams.put("name", nextSpl);
            biologicalSampleList.add(biologicalSampleParams);

            List<String> splAnalysis = _samplesAnalysisBySample.get(nextSpl);
            for (String nextSplAnalysis : splAnalysis) {
                Map<String, Object> quantChannelParams = new HashMap<>();
                quantChannelParams.put("number", splAnalysisNumber++);
                quantChannelParams.put("sample_number", nextSplNbr);
                quantChannelParams.put("name", nextSplAnalysis);
                if (_rsmIdBySampleAnalysis.containsKey(nextSplAnalysis)) {
                    quantChannelParams.put("ident_result_summary_id", _rsmIdBySampleAnalysis.get(nextSplAnalysis));
                    quantChannelParams.put("run_id", _runIdByRSMId.get(_rsmIdBySampleAnalysis.get(nextSplAnalysis)));
                }
                quantChanneList.add(quantChannelParams);
            }

        } // End go through samples
        experimentalDesignParams.put("biological_samples", biologicalSampleList);
        List masterQuantChannelsList = new ArrayList();
        Map<String, Object> masterQuantChannelParams = new HashMap<>();
        masterQuantChannelParams.put("number", 1);
        masterQuantChannelParams.put("name", node.getData().getName());
        masterQuantChannelParams.put("quant_channels", quantChanneList);
        if (refDataset != null) {
            masterQuantChannelParams.put("ident_dataset_id", (refRsmId != null) ? refRsmId : refDataset.getId());
            masterQuantChannelParams.put("ident_result_summary_id", refDataset.getResultSummaryId());
        }
        masterQuantChannelsList.add(masterQuantChannelParams);
        experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);
        return experimentalDesignParams;
    }

    /**
     * get run id from database for each rsm id
     *
     * @param rsmIDs
     * @return HashMap<rsmId, runId>
     */
    private static HashMap<Long, Long> getRunIdForRSMs(Collection<Long> rsmIDs) {
        //Get Run Ids for specified RSMs
        Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
        HashMap<Long, Long> returnedRunIdsByRsmIds = new HashMap<>();
        DatabaseRunsTask loadRunIdsTask = new DatabaseRunsTask(null);
        loadRunIdsTask.initLoadRunIdsForRsms(pID, new ArrayList(rsmIDs), returnedRunIdsByRsmIds);
        loadRunIdsTask.fetchData();

        return returnedRunIdsByRsmIds;
    }
}
