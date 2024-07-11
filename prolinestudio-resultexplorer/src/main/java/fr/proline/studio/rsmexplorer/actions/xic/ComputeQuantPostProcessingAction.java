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
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.QuantPostProcessingDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.utils.ResultCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;

import fr.proline.studio.WindowManager;

/**
 * key word: Refine = Compute Quan Post Processing
 *
 * @author Karine XUE at CEA
 */
public class ComputeQuantPostProcessingAction extends AbstractRSMAction {

    private int m_nbLoadedQuanti;

    public ComputeQuantPostProcessingAction(AbstractTree tree) {
        super("Compute Post Processing on Abundances...", tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        if (ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject() == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "A project should be selected !", "Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //project id
        final Long pID = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();

        final int posX = x;
        final int posY = y;

        ArrayList<DataSetNode> computedList = new ArrayList<>();
        ArrayList<DataSetNode> selectedDatasetNodeList = new ArrayList<>();
        m_nbLoadedQuanti = 0;
        for (AbstractNode n : selectedNodes) {

            DataSetNode node = (DataSetNode) n;
            //selectedDatasetNodeList.add(node);
            DDataset dataSet = ((DataSetData) node.getData()).getDataset();
            // call back for loading masterQuantChannel
            AbstractDatabaseCallback masterQuantChannelCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        // check if profilizer has already been launched
                        if (dataSet.getPostQuantProcessingConfig() != null) {
                            computedList.add(node);
                        }else{
                            selectedDatasetNodeList.add(node);
                        }
                        m_nbLoadedQuanti++;
                        
                        if (m_nbLoadedQuanti == selectedNodes.length) {//Last one is loaded, all loaded
                            if (!computedList.isEmpty()) {
                                String[] options = {"Compute All", "Skip already computed"};
                                String computedNodeName = computedList.stream().map(node->node.getDataset().getName()).collect(Collectors.joining(","));
                                String message = "Dataset "+ computedNodeName + ": Proteins Sets Abundances have already been post-processed \n(Compute Quant Post-Processing done).";
                                OptionDialog yesNoDialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Compute Post-Processing on Proteins Sets Abundances", message);
                                yesNoDialog.setButtonName(DefaultDialog.BUTTON_OK, options[0]);
                                yesNoDialog.setButtonName(DefaultDialog.BUTTON_CANCEL, options[1]);
                                yesNoDialog.setLocation(posX, posY);
                                yesNoDialog.setVisible(true);

                                if (yesNoDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                                    selectedDatasetNodeList.addAll(computedList);
                                }
                            }//else all no computed, 
                            if (!selectedDatasetNodeList.isEmpty()) {
                                quantificationProfile(null, posX, posY, pID, selectedDatasetNodeList, null);
                            }
                        }
                    }
                }

            };
            //load Quantitation, in order to test if the dataset is already post-processed
            DatabaseDataSetTask loadTask = new DatabaseDataSetTask(masterQuantChannelCallback);
            loadTask.initLoadQuantitation(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject(), dataSet);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(loadTask);
        }
    }

    public static boolean quantificationProfile(final ResultCallback resultCallback, int posX, int posY, Long pID, ArrayList<DataSetNode> nodeList, DDataset paramsFromdataset) {
        ArrayList<PtmSpecificity> ptms = fetchPtmsFromDAM(nodeList);
        boolean isAggregation = isAllAggregation(nodeList, posX, posY);

        boolean isValidLabelQuant = isQuantLabelValid(nodeList);

        DDatasetType.QuantitationMethodInfo qMethodInfo = nodeList.get(0).getDataset().getQuantMethodInfo();
        QuantitationMethod quantMethod = nodeList.get(0).getDataset().getQuantitationMethod();
        QuantPostProcessingDialog dialog = new QuantPostProcessingDialog(WindowManager.getDefault().getMainWindow(), ptms, isAggregation, quantMethod, qMethodInfo, paramsFromdataset,isValidLabelQuant);

        dialog.setLocation(posX, posY);
        dialog.setVisible(true);

        // retreive parameters
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            //User specified parameters
            String errorMsg = null;

            final Map<String, Object> quantParams = dialog.getQuantParams();
            if (quantParams == null) {
                errorMsg = "Null Quantitation parameters !";
            }

            if (errorMsg != null) {
                JOptionPane.showMessageDialog(dialog, errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            // set all node in color grey = in processing
          for (DataSetNode node : nodeList) {
            node.setIsChanging(true);
            QuantitationTree tree = QuantitationTree.getCurrentTree();
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            treeModel.nodeChanged(node);
          }

          for (DataSetNode node : nodeList) {
                // dataset name
                DDataset dataset = node.getDataset();
                final String xicName = dataset.getName();

                List<DMasterQuantitationChannel> listMasterQuantChannels = dataset.getMasterQuantitationChannels();
                if (listMasterQuantChannels != null && !listMasterQuantChannels.isEmpty()) {
                    Long masterQuantChannelId = listMasterQuantChannels.get(0).getId();
                    // CallBack for Xic Quantitation Service
                    //one callback by datasetNode
                    AbstractJMSCallback xicCallback = new AbstractJMSCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success) {

                          QuantitationTree tree = QuantitationTree.getCurrentTree();
                          DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                          node.setIsChanging(false);
                          treeModel.nodeChanged(node);
                          if (resultCallback != null) {
                                resultCallback.run(success);
                            }

                        }
                    };
                    fr.proline.studio.dpm.task.jms.ComputeQuantPostProcessingTask task = new fr.proline.studio.dpm.task.jms.ComputeQuantPostProcessingTask(xicCallback, pID, masterQuantChannelId, quantParams, xicName);
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

                }
            } //End OK entered   
            return true;
        }
        return false;
    }

    private static boolean isQuantLabelValid(ArrayList<DataSetNode> nodeList) {
        boolean isValidLabelQuant = true;
        List<QuantitationMethod> nodeMethods = nodeList.stream().map(node -> node.getDataset().getQuantitationMethod()).distinct().toList();
        if(nodeMethods.size() != 1) //method are not homogenous
            isValidLabelQuant = false;
        else { //verify quant Channels defines quantLabel
            QuantitationMethod commonMethod = nodeMethods.get(0);
            for (DataSetNode node : nodeList) {
                DDataset dataSet = ((DataSetData) node.getData()).getDataset();
                for (QuantitationChannel nextQCh : dataSet.getMasterQuantitationChannels().get(0).getQuantitationChannels()) {
                    if (!commonMethod.getLabels().contains(nextQCh.getQuantitationLabel())) {
                        isValidLabelQuant = false;
                        break;
                    }
                }
                if (!isValidLabelQuant)
                    break;
            }
        }
        return isValidLabelQuant;
    }

    public static boolean quantificationProfile(final ResultCallback resultCallback, int posx, int posy, Long pID, DDataset paramsFromdataSet) {
        ArrayList<DataSetNode> nodeList = new ArrayList<>();
        nodeList.add(new DataSetNode(new DataSetData(paramsFromdataSet)));
        return quantificationProfile(resultCallback, posx, posy, pID, nodeList, paramsFromdataSet);
    }

    private static ArrayList<PtmSpecificity> fetchPtmsFromDAM(ArrayList<DataSetNode> nodeList) {
        ArrayList<PtmSpecificity> ptms = new ArrayList<>();
        DatabasePTMsTask task = new DatabasePTMsTask(null);
        ArrayList<Long> rsmIdList = new ArrayList<>();
        DDataset dataset;
        long projectId = nodeList.get(0).getDataset().getProject().getId();
        for (int i = 0; i < nodeList.size(); i++) {
            dataset = nodeList.get(i).getDataset();
            rsmIdList.add(dataset.getResultSummaryId());
        }
        task.initLoadUsedPTMs(projectId, rsmIdList, ptms);
        task.fetchData();
        return ptms;
    }

//    private ArrayList<PtmSpecificity> fetchPtmsFromDAM(DDataset dataset) {
//        ArrayList<PtmSpecificity> ptms = new ArrayList();
//        DatabasePTMSitesTask task = new DatabasePTMSitesTask(null);
//        long projectId = dataset.getProject().getId();
//        task.initLoadUsedPTMs(projectId, dataset.getResultSummaryId(), ptms);
//        task.fetchData();
//        return ptms;
//    }
//    private boolean isAllAggregation(DDataset dataset) {
//        return dataset.isQuantitation() && dataset.isAggregation();
//    }
    private static boolean isAllAggregation(ArrayList<DataSetNode> nodeList, int posX, int posY) {
        DDataset dataset;
        String quantitationList = "";
        String aggregationList = "";
        for (DataSetNode node : nodeList) {
            dataset = node.getDataset();
            boolean test = (dataset.isQuantitation() && dataset.isAggregation());
            if (test) {
                aggregationList += dataset.getName() + " ";
            } else {
                quantitationList += dataset.getName() + " ";
            }
        }
        if (quantitationList.isEmpty()) {
            return true;
        } else if (aggregationList.isEmpty()) {
            return false;
        } else {
            String errorMsg = "The selection has Aggregated Quatitation dataSets and Quatitation datasets.";
            errorMsg += "\n Quantitation: " + quantitationList;
            errorMsg += "\n Aggregation: " + aggregationList;
            errorMsg += "\n The option Discard_Peptides_Sharing_Peakels will not be available";
            final JOptionPane pane = new JOptionPane(errorMsg, JOptionPane.INFORMATION_MESSAGE);
            final JDialog d = pane.createDialog((JFrame) null, "Warning");
            d.setLocation(posX, posY);
            d.setVisible(true);
            return true;
        }

    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }

        boolean first = true;
        DDatasetType.QuantitationMethodInfo methodInfo = DDatasetType.QuantitationMethodInfo.NONE;
        for (AbstractNode node : selectedNodes) {

            DataSetNode datasetNode = (DataSetNode) node;

            // the node must not be in changing state
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            // must be a dataset 
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            // must be a quantitation XIC
            if (!datasetNode.isQuantXIC()) {
                setEnabled(false);
                return;
            }

            //must be same quanti type
            if(first){
                methodInfo = datasetNode.getDataset().getQuantMethodInfo();
                first = false;
            } else if(! datasetNode.getDataset().getQuantMethodInfo().equals(methodInfo)){
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);
    }

}
