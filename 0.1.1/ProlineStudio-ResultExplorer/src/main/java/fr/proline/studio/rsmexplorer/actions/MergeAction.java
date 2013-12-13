package fr.proline.studio.rsmexplorer.actions;

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
 *
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
                if (nbChildren == 1) {
                    error = "Merge on a Aggregate with only one Child is not possible";
                }

                ArrayList<Long> resultSetIdList = new ArrayList<>();
                for (int i = 0; i < nbChildren; i++) {
                    RSMDataSetNode childNode = (RSMDataSetNode) node.getChildAt(i);
                    if (!childNode.hasResultSet()) {
                        error = "Merge impossible : " + childNode.getDataset().getName() + " has no Result Set";
                        break;
                    }
                    resultSetIdList.add(childNode.getDataset().getResultSetId());

                }

                if (error != null) {
                    JOptionPane.showMessageDialog(RSMTree.getTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                askMergeService((RSMDataSetNode) node, resultSetIdList);


            }
        };

        RSMTree.getTree().loadInBackground(node, callback);

    }

    private void askMergeService(final RSMDataSetNode node, final List<Long> resultSetIdList) {

        final Dataset dataset = node.getDataset();
        long projectId = dataset.getProject().getId();
        String datasetName = dataset.getName();

        node.setIsChanging(true);
        RSMTree tree = RSMTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(node);

        // used as out parameter for the service
        final Long[] _resultSetId = new Long[1];


        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    updateDataset(node, dataset, _resultSetId[0], getTaskInfo());

                } else {
                    //JPM.TODO : manage error with errorMessage
                    node.setIsChanging(false);

                    treeModel.nodeChanged(node);
                }
            }
        };

        MergeTask task = new MergeTask(callback, projectId, resultSetIdList, datasetName, _resultSetId);
        AccessServiceThread.getAccessServiceThread().addTask(task);
    }

    private void updateDataset(final RSMDataSetNode datasetNode, Dataset d, Long resultSetId, TaskInfo taskInfo) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                datasetNode.setIsChanging(false);

                RSMTree tree = RSMTree.getTree();
                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.nodeChanged(datasetNode);
            }
        };

        // ask asynchronous loading of data



        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initModifyDatasetForMerge(d, resultSetId, taskInfo);
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
