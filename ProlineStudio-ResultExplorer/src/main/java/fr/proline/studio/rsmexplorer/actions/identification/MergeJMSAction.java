package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.MergeTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;

/**
 * Action to Merge data from a set of Search Results (rset) or Identification Summaries (rsm)
 * @author JM235353
 */
public class MergeJMSAction extends AbstractRSMAction {

    public MergeJMSAction() {
        super(NbBundle.getMessage(AddAction.class, "CTL_MergeAction")+"_JMS", AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {

        // merge done only on one node for the moment
        final AbstractNode node = selectedNodes[0];

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                // check if we can do a merge
                String error = null;
                int nbChildren = node.getChildCount();
                /*if (nbChildren == 1) {
                    error = "Merge on an Dataset with only one Child is not possible";
                }*/

                ArrayList<Long> resultSetIdList = new ArrayList<>();
                ArrayList<Long> resultSummaryIdList = new ArrayList<>();
                for (int i = 0; i < nbChildren; i++) {
                    DataSetNode childNode = (DataSetNode) node.getChildAt(i);
                    if (childNode.isChanging()) {
                        error = "Merge is not possible while a child Search Result is being imported or validated";
                        break;
                    }
                    if (!childNode.hasResultSet()) {
                        error = "Merge is not possible : " + childNode.getDataset().getName() + " has no Search Result";
                        break;
                    }

                    resultSetIdList.add(childNode.getDataset().getResultSetId());
                    if (childNode.hasResultSummary()) {
                        resultSummaryIdList.add(childNode.getDataset().getResultSummaryId());
                    }
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(IdentificationTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                if (resultSummaryIdList.size()==nbChildren) {
                    // we do a merge on resultSummary
                    askMergeService((DataSetNode) node, resultSummaryIdList, true);
                } else if (resultSummaryIdList.size()>0) {
                    // not all children have a result summary
                    error = "Merge is not possible : some Search Results are not validated ";
                    JOptionPane.showMessageDialog(IdentificationTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    // merge on result set
                    askMergeService((DataSetNode) node, resultSetIdList, false);
                }

                


            }
        };

        IdentificationTree.getCurrentTree().loadInBackground(node, callback);

    }

    private void askMergeService(final DataSetNode node, final List<Long> idList, final boolean mergeOnRsm) {

        final DDataset dataset = node.getDataset();
        long projectId = dataset.getProject().getId();
        String datasetName = dataset.getName();

        node.setIsChanging(true);
        IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(node);

        // used as out parameter for the service
        final Long[] _resultSetId = new Long[1];
        final Long[] _resultSummaryId = new Long[1];

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    updateDataset(node, dataset, _resultSetId[0], _resultSummaryId[0], getTaskInfo());

                } else {
                    //JPM.TODO : manage error with errorMessage
                    node.setIsChanging(false);

                    treeModel.nodeChanged(node);
                }
            }
        };

        MergeTask task = new MergeTask(callback, projectId);
        if (mergeOnRsm) {
             task.initMergeRsm(idList, datasetName, _resultSetId, _resultSummaryId);
        } else {
            _resultSummaryId[0] = null;
            task.initMergeRset(idList, datasetName, _resultSetId);
        }
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
    }

    private void updateDataset(final DataSetNode datasetNode, DDataset d, Long resultSetId, Long resultSummaryId, TaskInfo taskInfo) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                datasetNode.setIsChanging(false);

                if (datasetNode.hasResultSummary()) {
                    datasetNode.getDataset().setMergeInformation(DDataset.MergeInformation.MERGE_IDENTIFICATION_SUMMARY);
                }
                
                IdentificationTree tree = IdentificationTree.getCurrentTree();
                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.nodeChanged(datasetNode);
            }
        };

        // ask asynchronous loading of data



        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initModifyDatasetForMerge(d, resultSetId, resultSummaryId, taskInfo);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;

        if (datasetNode.isLeaf()) {
            setEnabled(false);
            return;
        }

        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
        if (datasetType != Dataset.DatasetType.AGGREGATE) {
            setEnabled(false);
            return;
        }

        if (datasetNode.hasResultSet()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
}
