/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabasePTMSitesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.QuantPostProcessingMultipleDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.utils.ResultCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Karine XUE at CEA
 */
public class ComputeQuantPostProcessingMultipleAction extends AbstractRSMAction {

    private int m_nbLoadedQuanti;

    public ComputeQuantPostProcessingMultipleAction(AbstractTree tree) {
        super(NbBundle.getMessage(ComputeQuantPostProcessingMultipleAction.class, "CTL_ComputeQuantPostProcessingAction"), tree);
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

        ArrayList<String> computedList = new ArrayList();
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
                            node.setIsRefined(true);
                            computedList.add(node.getDataset().getName());
                        }
                        m_nbLoadedQuanti++;
                        ArrayList<DataSetNode> selectedDatasetNodeList = new ArrayList<>();
                        if (m_nbLoadedQuanti == selectedNodes.length) {//all loaded
                            if (!computedList.isEmpty()) {
                                String message = computedList + " Proteins Sets Abundances have already been refined.\nDo you want to continue?";
                                OptionDialog yesNoDialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Refine Proteins Sets Abundances", message);
                                yesNoDialog.setLocation(posX, posY);
                                yesNoDialog.setVisible(true);

                                if (yesNoDialog.getButtonClicked() != DefaultDialog.BUTTON_OK) {
                                    for (AbstractNode an : selectedNodes) {
                                        DataSetNode dn = ((DataSetNode) an);
                                        if (!dn.isRefined()) {
                                            selectedDatasetNodeList.add(dn);
                                        }
                                    }
                                } else {
                                    for (AbstractNode an : selectedNodes) {
                                        DataSetNode dn = ((DataSetNode) an);
                                        selectedDatasetNodeList.add(dn);
                                    }

                                }
                            }
                            if (!selectedDatasetNodeList.isEmpty()) {
                                quantificationProfile(null, posX, posY, pID, selectedDatasetNodeList);
                            }
                        }
                    }
                }

            };

            DatabaseDataSetTask loadTask = new DatabaseDataSetTask(masterQuantChannelCallback);
            loadTask.initLoadQuantitation(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject(), dataSet);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(loadTask);
        }
    }

    public static boolean quantificationProfile(final ResultCallback resultCallback, int posX, int posY, Long pID, ArrayList<DataSetNode> nodeList) {
        ArrayList<PtmSpecificity> ptms = fetchPtmsFromDAM(nodeList);
        boolean isAggregation = isAllAggregation(nodeList, posX, posY);
        QuantPostProcessingMultipleDialog dialog = new QuantPostProcessingMultipleDialog(WindowManager.getDefault().getMainWindow(), ptms, isAggregation);
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
            if (nodeList != null) {
                for (DataSetNode node : nodeList) {
                    node.setIsChanging(true);
                    QuantitationTree tree = QuantitationTree.getCurrentTree();
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    treeModel.nodeChanged(node);
                }
            }

            for (DataSetNode node : nodeList) {
                // dataset name
                DDataset dataset = node.getDataset();
                final String xicName = dataset.getName();

                List<DMasterQuantitationChannel> listMasterQuantChannels = dataset.getMasterQuantitationChannels();
                if (listMasterQuantChannels != null && !listMasterQuantChannels.isEmpty()) {
                    Long masterQuantChannelId = new Long(listMasterQuantChannels.get(0).getId());
                    // CallBack for Xic Quantitation Service
                    //one callback by datasetNode
                    AbstractJMSCallback xicCallback = new AbstractJMSCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success) {

                            if (node != null) {
                                QuantitationTree tree = QuantitationTree.getCurrentTree();
                                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                                node.setIsChanging(false);
                                treeModel.nodeChanged(node);
                            }
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

    public static boolean quantificationProfile(final ResultCallback resultCallback, int posx, int posy, Long pID, DDataset dataSet) {
        ArrayList<DataSetNode> nodeList = new ArrayList();
        nodeList.add(new DataSetNode(new DataSetData(dataSet)));
        return quantificationProfile(resultCallback, posx, posy, pID, nodeList);
    }

    private static ArrayList<PtmSpecificity> fetchPtmsFromDAM(ArrayList<DataSetNode> nodeList) {
        ArrayList<PtmSpecificity> ptms = new ArrayList();
        DatabasePTMSitesTask task = new DatabasePTMSitesTask(null);
        ArrayList<Long> rsmIdList = new ArrayList();
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
            String errorMsg = "The selection has aggregation dataSet and non aggregation dataset.";
            errorMsg += "\n Quantitation: " + quantitationList;
            errorMsg += "\n Aggregation: " + aggregationList;
            errorMsg += "\n The option Discard_Peptides_Sharing_Peakels will not available";
            //JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), errorMsg, "Warning", JOptionPane.ERROR_MESSAGE);
            final JOptionPane pane = new JOptionPane(errorMsg, JOptionPane.WARNING_MESSAGE);
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

        for (AbstractNode node : selectedNodes) {
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

            DataSetNode datasetNode = (DataSetNode) node;

            // must be a quantitation XIC
            if (!datasetNode.isQuantXIC()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);
    }

}
