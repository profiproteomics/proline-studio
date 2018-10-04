package fr.proline.studio.rsmexplorer.tree.identification;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.actions.identification.ImportManager;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.HourGlassNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used for Drag and Drop of nodes
 *
 * @author JM235353
 */
public class IdTransferHandler extends TransferHandler {

    private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {

        if (c instanceof IdentificationTree) {
            IdentificationTree tree = (IdentificationTree) c;

            // only Dataset which are not being changed can be transferred
            // furthermore Dataset must be of the same project
            long commonProjectId = -1;
            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            int nbSelectedNode = selectedNodes.length;
            for (int i = 0; i < nbSelectedNode; i++) {
                AbstractNode node = selectedNodes[i];
                if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                    return null;
                }
                if (node.isChanging()) {
                    return null;
                }

                DataSetNode datasetNode = (DataSetNode) node;

                if (datasetNode.isTrash()) {
                    return null;
                }

                long projectId = datasetNode.getDataset().getProject().getId();
                if (commonProjectId == -1) {
                    commonProjectId = projectId;
                } else if (commonProjectId != projectId) {
                    return null;
                }
            }

            // we must keep only parent nodes
            // if a child and its parent are selected, we keep only the parent
            ArrayList<AbstractNode> keptNodes = new ArrayList<>(nbSelectedNode);
            keptNodes.add(selectedNodes[0]);
            mainloop:
            for (int i = 1; i < nbSelectedNode; i++) {
                AbstractNode curNode = selectedNodes[i];

                // look for an ancestor
                int nbKeptNodes = keptNodes.size();
                for (int j = 0; j < nbKeptNodes; j++) {

                    AbstractNode curKeptNode = keptNodes.get(j);
                    if (curNode.isNodeAncestor(curKeptNode)) {
                        // ancestor is already in kept node
                        continue mainloop;
                    }
                }
                // look for children and remove them
                for (int j = nbKeptNodes - 1; j >= 0; j--) {

                    AbstractNode curKeptNode = keptNodes.get(j);
                    if (curKeptNode.isNodeAncestor(curNode)) {
                        // we have found a children
                        keptNodes.remove(j);
                    }
                }
                keptNodes.add(curNode);

            }

            // a selected node in a merged parent can not be transferred
            int nbKeptNodes = keptNodes.size();
            for (int i = 0; i < nbKeptNodes; i++) {
                AbstractNode curNode = keptNodes.get(i);
                AbstractNode parentNode = (AbstractNode) curNode.getParent();
                if (parentNode instanceof DataSetNode) {
                    DataSetNode parentDatasetNode = (DataSetNode) parentNode;
                    if ((parentDatasetNode.hasResultSet()) || (parentDatasetNode.hasResultSummary()) || parentDatasetNode.isChanging()) {
                        // parent is a merged dataset or in a current merge: no transfer possible
                        return null;
                    }
                }
            }

            IdTransferable.TransferData data = new IdTransferable.TransferData();
            data.setNodeList(keptNodes);
            Integer transferKey = IdTransferable.register(data);

            return new IdTransferable(transferKey, commonProjectId);

        }
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

        // clean all transferred data
        IdTransferable.clearRegisteredData();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(IdTransferable.RSMNodeList_FLAVOR)) {

            // drop path
            TreePath dropTreePath = ((JTree.DropLocation) support.getDropLocation()).getPath();
            if (dropTreePath == null) {
                // should not happen
                return false;
            }

            // Determine whether we accept the location
            Object dropComponent = dropTreePath.getLastPathComponent();

            if (!(dropComponent instanceof AbstractNode)) {
                return false;
            }

            // We can drop only in project or aggregate node with no Rsm or Rset
            // The node must not being changed
            AbstractNode dropRSMNode = (AbstractNode) dropComponent;
            if (dropRSMNode.isChanging()) {
                return false;
            }

            long dropProjectId;
            AbstractNode.NodeTypes nodeType = dropRSMNode.getType();
            if (nodeType == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dropDatasetNode = (DataSetNode) dropRSMNode;
                if (dropDatasetNode.hasResultSet() || dropDatasetNode.hasResultSummary()) {
                    return false;
                }
                dropProjectId = dropDatasetNode.getDataset().getProject().getId();
            } else if (nodeType == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                IdProjectIdentificationNode dropProjectNode = (IdProjectIdentificationNode) dropRSMNode;
                dropProjectId = dropProjectNode.getProject().getId();
                if (((JTree.DropLocation) support.getDropLocation()).getChildIndex() == 0) {
                    // drop can not been done in Project before the All imported
                    return false;
                }
            } else {
                return false;
            }

            try {

                // drop node must be in the same project that nodes being transferred
                IdTransferable nodeListTransferable = (IdTransferable) support.getTransferable().getTransferData(IdTransferable.RSMNodeList_FLAVOR);
                if (nodeListTransferable.getProjectId() != dropProjectId) {
                    return false;
                }

                // drop node must not be equal or a child of a transferred node
                IdTransferable.TransferData data = IdTransferable.getData(nodeListTransferable.getTransferKey());

                if (data.isNodeList()) {
                    ArrayList<AbstractNode> nodeList = (ArrayList<AbstractNode>) data.getDataList();
                    int nbNodes = nodeList.size();
                    for (int i = 0; i < nbNodes; i++) {
                        AbstractNode nodeTransfered = nodeList.get(i);

                        if (dropRSMNode.isNodeAncestor(nodeTransfered)) {
                            return false;
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            return true;

        }

        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {

        if (canImport(support)) {

            try {
                IdTransferable transfer = (IdTransferable) support.getTransferable().getTransferData(IdTransferable.RSMNodeList_FLAVOR);
                IdTransferable.TransferData data = IdTransferable.getData(transfer.getTransferKey());
                if (data.isNodeList()) {
                    return importNodes(support, data);
                } else {
                    return importResultSets(support, data);
                }

            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

        }

        return false;
    }

    private boolean importResultSets(TransferHandler.TransferSupport support, IdTransferable.TransferData data) {

        JTree.DropLocation location = ((JTree.DropLocation) support.getDropLocation());
        TreePath dropTreePath = location.getPath();
        int childIndex = location.getChildIndex();
        final AbstractNode dropRSMNode = (AbstractNode) dropTreePath.getLastPathComponent();

        IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        // no insert index specified -> we insert at the end
        if (childIndex == -1) {

            childIndex = dropRSMNode.getChildCount();

            // special case to drop before the Trash
            if (childIndex > 0) {
                AbstractNode lastChild = (AbstractNode) dropRSMNode.getChildAt(childIndex - 1);
                if ((lastChild instanceof DataSetNode) && ((DataSetNode) lastChild).isTrash()) {
                    childIndex--; // we drop before the Trash
                }
            }
        }

        Project project = null;
        DDataset parentDataset = null;
        DataSetNode parentDatasetNode = null;

        if (dropRSMNode.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
            IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) dropRSMNode;
            project = projectNode.getProject();
        } else if (dropRSMNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
            parentDatasetNode = (DataSetNode) dropRSMNode;
            project = parentDatasetNode.getDataset().getProject();
            parentDataset = parentDatasetNode.getDataset();
        }

        // start imports
        ArrayList<ResultSet> rsetList = (ArrayList<ResultSet>) data.getDataList();
        int nbRset = rsetList.size();
        final AtomicInteger counter = new AtomicInteger(0); 
        
        for (int i = 0; i < nbRset; i++) {
            ResultSet rset = rsetList.get(i);
            String datasetName = rset.getMsiSearch().getResultFileName();
            int indexOfDot = datasetName.lastIndexOf('.');
            if (indexOfDot != -1) {
                datasetName = datasetName.substring(0, indexOfDot);
            }

            DataSetData identificationData = new DataSetData(datasetName, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS);  //JPM.TODO

            final DataSetNode identificationNode = new DataSetNode(identificationData);
            identificationNode.setIsChanging(true);

            treeModel.insertNodeInto(identificationNode, dropRSMNode, childIndex);
            childIndex++;

            final ArrayList<DDataset> createdDatasetList = new ArrayList<>();

            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    identificationNode.setIsChanging(false);
                    counter.getAndIncrement();
                    
                    if (success) {

                        DDataset dataset = createdDatasetList.get(0);
                        identificationNode.setIsChanging(false);
                        ((DataSetData) identificationNode.getData()).setDataset(dataset);
                        treeModel.nodeChanged(identificationNode);

                        ImportManager.importRenaming(dataset, identificationNode, tree);

                    } else {
                        // should not happen
                        treeModel.removeNodeFromParent(identificationNode);
                    }
                    
                    if (counter.get() == nbRset) {
                        LinkedHashMap<Object, ArrayList<DDataset>> databaseObjectsToModify = new LinkedHashMap<>();
                        int nbChildren = dropRSMNode.getChildCount();
                        ArrayList<DDataset> datasetList = new ArrayList<>(nbChildren);
                        for (int i = 0; i < nbChildren; i++) {
                            AbstractNode childNode = ((AbstractNode) dropRSMNode.getChildAt(i));
                            DataSetNode childDatasetNode = (DataSetNode) childNode;
                            DDataset dataset = childDatasetNode.getDataset();
                            datasetList.add(dataset);
                        }
                        Object databaseParentObject = (dropRSMNode.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) ? ((IdProjectIdentificationNode)dropRSMNode).getProject() : ((DataSetNode)dropRSMNode).getDataset();
                        databaseObjectsToModify.put(databaseParentObject, datasetList);
                        DatabaseDataSetTask.updateDatasetAndProjectsTree(databaseObjectsToModify, true);
                    }
                }
            };

            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initCreateDatasetForIdentification(project, parentDataset, Aggregation.ChildNature.SAMPLE_ANALYSIS, datasetName, rset.getId(), null, createdDatasetList, new TaskInfo("Create Dataset " + datasetName, true, AbstractDatabaseTask.TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        }

        return true;
    }

    private boolean importNodes(TransferHandler.TransferSupport support, IdTransferable.TransferData data) {

        JTree.DropLocation location = ((JTree.DropLocation) support.getDropLocation());
        TreePath dropTreePath = location.getPath();
        int childIndex = location.getChildIndex();
        AbstractNode dropRSMNode = (AbstractNode) dropTreePath.getLastPathComponent();

        IdentificationTree tree = IdentificationTree.getCurrentTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        // no insert index specified -> we insert at the end
        if (childIndex == -1) {

            childIndex = dropRSMNode.getChildCount();

            // special case to drop before the Trash
            if (childIndex > 0) {
                AbstractNode lastChild = (AbstractNode) dropRSMNode.getChildAt(childIndex - 1);
                if ((lastChild instanceof DataSetNode) && ((DataSetNode) lastChild).isTrash()) {
                    childIndex--; // we drop before the Trash
                }
            }
        }

        // used to keep parent node modified
        // to be able later to modify the database
        LinkedHashSet<AbstractNode> allParentNodeModified = new LinkedHashSet<>();

        ArrayList<AbstractNode> nodeList = (ArrayList<AbstractNode>) data.getDataList();
        int nbNodes = nodeList.size();
        for (int i = 0; i < nbNodes; i++) {
            AbstractNode node = nodeList.get(i);

            // specific case when the node is moved in its parent
            int indexChild;
            if (dropRSMNode.isNodeChild(node)) {
                // we are moving the node in its parent
                indexChild = dropRSMNode.getIndex(node);
                if (indexChild < childIndex) {
                    childIndex--;
                }
            } else {
                allParentNodeModified.add((AbstractNode) node.getParent());
            }

            // remove from parent (required when drag and dropped in the same parent)
            treeModel.removeNodeFromParent(node);

            // add to new parent
            treeModel.insertNodeInto(node, dropRSMNode, childIndex);

            childIndex++;

        }

        // Drop node must be added at the end : because the add of dataset must be done
        // after children have been removed from their parents
        allParentNodeModified.add(dropRSMNode);

        // create HashMap of Database Object which need to be updated
        LinkedHashMap<Object, ArrayList<DDataset>> databaseObjectsToModify = new LinkedHashMap<>();
        //HashSet<RSMDataSetNode> nodeToBeChanged = new HashSet<>();

        Iterator<AbstractNode> it = allParentNodeModified.iterator();
        while (it.hasNext()) {

            AbstractNode parentNode = it.next();

            // get Parent Database Object
            Object databaseParentObject = null;
            AbstractNode.NodeTypes type = parentNode.getType();
            if (type == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode datasetNode = ((DataSetNode) parentNode);
                databaseParentObject = datasetNode.getDataset();
                //nodeToBeChanged.add(datasetNode);
            } else if (type == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                IdProjectIdentificationNode projectNode = ((IdProjectIdentificationNode) parentNode);
                databaseParentObject = projectNode.getProject();
            }

            // get new Dataset children
            int nbChildren = parentNode.getChildCount();
            ArrayList<DDataset> datasetList = new ArrayList<>(nbChildren);
            for (int i = 0; i < nbChildren; i++) {
                // we are sure that it is a Dataset

                AbstractNode childNode = ((AbstractNode) parentNode.getChildAt(i));
                if (!(childNode instanceof DataSetNode)) {

                    if (childNode instanceof HourGlassNode) {
                        // drop on a node whose children have not been loaded
                        datasetList.add(null); // JPM.WART : children not already loaded
                    }

                    continue; // possible for "All imported" node or Hour Glass Node
                }
                DataSetNode childDatasetNode = (DataSetNode) childNode;
                DDataset dataset = childDatasetNode.getDataset();
                datasetList.add(dataset);
                //nodeToBeChanged.add(childDatasetNode);
            }

            // register this modification
            databaseObjectsToModify.put(databaseParentObject, datasetList);

        }

        // ask the modification to the database at once (intricate to put in a thread in Dnd context)
        DatabaseDataSetTask.updateDatasetAndProjectsTree(databaseObjectsToModify, true);

        return true;

    }

}