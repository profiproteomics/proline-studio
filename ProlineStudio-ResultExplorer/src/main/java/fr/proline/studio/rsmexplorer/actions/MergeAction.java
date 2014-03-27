package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.MergeTask;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;

/**
 * Action to Merge data from a set of Search Results (rset) or Identification Summaries (rsm)
 * @author JM235353
 */
public class MergeAction extends AbstractRSMAction {

    public MergeAction() {
        super(NbBundle.getMessage(AddAction.class, "CTL_MergeAction"));
    }

    @Override
    public void actionPerformed(final RSMNode[] selectedNodes, int x, int y) {

        // merge done only on one node for the moment
        final RSMNode node = selectedNodes[0];

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
                    RSMDataSetNode childNode = (RSMDataSetNode) node.getChildAt(i);
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
                    JOptionPane.showMessageDialog(RSMTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                if (resultSummaryIdList.size()==nbChildren) {
                    // we do a merge on resultSummary
                    askMergeService((RSMDataSetNode) node, resultSummaryIdList, true);
                } else if (resultSummaryIdList.size()>0) {
                    // not all children have a result summary
                    error = "Merge is not possible : some Search Results are not validated ";
                    JOptionPane.showMessageDialog(RSMTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    // merge on result set
                    askMergeService((RSMDataSetNode) node, resultSetIdList, false);
                }

                


            }
        };

        RSMTree.getCurrentTree().loadInBackground(node, callback);

    }

    private void askMergeService(final RSMDataSetNode node, final List<Long> idList, final boolean mergeOnRsm) {

        final DDataset dataset = node.getDataset();
        long projectId = dataset.getProject().getId();
        String datasetName = dataset.getName();

        node.setIsChanging(true);
        RSMTree tree = RSMTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(node);

        // used as out parameter for the service
        final Long[] _resultSetId = new Long[1];
        final Long[] _resultSummaryId = new Long[1];

        AbstractServiceCallback callback = new AbstractServiceCallback() {

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
        AccessServiceThread.getAccessServiceThread().addTask(task);
    }

    private void updateDataset(final RSMDataSetNode datasetNode, DDataset d, Long resultSetId, Long resultSummaryId, TaskInfo taskInfo) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                datasetNode.setIsChanging(false);

                RSMTree tree = RSMTree.getCurrentTree();
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
    public void updateEnabled(RSMNode[] selectedNodes) {

        if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        RSMNode node = (RSMNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        RSMDataSetNode datasetNode = (RSMDataSetNode) node;

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
