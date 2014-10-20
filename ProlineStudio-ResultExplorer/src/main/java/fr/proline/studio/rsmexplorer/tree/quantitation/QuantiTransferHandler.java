package fr.proline.studio.rsmexplorer.tree.quantitation;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.HourGlassNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used for Drag and Drop of nodes in the Quantitation Tree
 */
public class QuantiTransferHandler extends TransferHandler {
    
    private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }
    
    
    @Override
    protected Transferable createTransferable(JComponent c) {

        if (c instanceof QuantitationTree) {
            QuantitationTree tree = (QuantitationTree) c;
            
            // only Dataset which are not being changed can be transferred
            // furthermore Dataset must be of the same project
            long commonProjectId = -1;
            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
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
            for (int i=1;i<nbSelectedNode;i++) {
                AbstractNode curNode = selectedNodes[i];
                
                // look for an ancestor
                int nbKeptNodes = keptNodes.size();
                for (int j=0;j<nbKeptNodes;j++) {
                    
                    AbstractNode curKeptNode = keptNodes.get(j);
                    if (curNode.isNodeAncestor(curKeptNode)) {
                        // ancestor is already in kept node
                        continue mainloop;
                    }
                }
                // look for children and remove them
                for (int j=nbKeptNodes-1;j>=0;j--) {
                    
                    AbstractNode curKeptNode = keptNodes.get(j);
                    if (curKeptNode.isNodeAncestor(curNode)) {
                        // we have found a children
                        keptNodes.remove(j);
                    }
                }
                keptNodes.add(curNode);
 
            }

            
            QuantiTransferable.TransferData data = new QuantiTransferable.TransferData();
            data.setNodeList(keptNodes);
            Integer transferKey =  QuantiTransferable.register(data);

            
            
            return new QuantiTransferable(transferKey, commonProjectId);

        }
        return null;
    }

    
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

        // clean all transferred data
        QuantiTransferable.clearRegisteredData();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(QuantiTransferable.QuantiNodeList_FLAVOR)) {

            // drop path
            TreePath dropTreePath = ((JTree.DropLocation) support.getDropLocation()).getPath();
            if (dropTreePath == null) {
                // should not happen
                return false;
            }
            
            // Determine whether we accept the location
            Object dropComponent = dropTreePath.getLastPathComponent();
            
            if (! (dropComponent instanceof AbstractNode)) {
                return false;
            }
            
            // The node must not being changed
            AbstractNode dropRSMNode = (AbstractNode) dropComponent;
            if (dropRSMNode.isChanging()) {
                return false;
            }
            
            long dropProjectId;
            AbstractNode.NodeTypes nodeType = dropRSMNode.getType();
            if ( nodeType == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dropDatasetNode = (DataSetNode) dropRSMNode;
                if (dropDatasetNode.isQuantitation()) {
                    return false;
                }
                dropProjectId = dropDatasetNode.getDataset().getProject().getId();
            } else if ( nodeType == AbstractNode.NodeTypes.PROJECT_QUANTITATION) {
                QuantitationProjectNode dropProjectNode = (QuantitationProjectNode) dropRSMNode;
                dropProjectId = dropProjectNode.getProject().getId();
            } else {
                return false;
            }

            try {
                // drop node must be in the same project that nodes being transferred
                QuantiTransferable nodeListTransferable = (QuantiTransferable) support.getTransferable().getTransferData(QuantiTransferable.QuantiNodeList_FLAVOR);
                if (nodeListTransferable.getProjectId() != dropProjectId) {
                    return false;
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
    public boolean importData(TransferSupport support) {

        if (canImport(support)) {

            try {
                QuantiTransferable transfer = (QuantiTransferable) support.getTransferable().getTransferData(QuantiTransferable.QuantiNodeList_FLAVOR);
                QuantiTransferable.TransferData data = QuantiTransferable.getData(transfer.getTransferKey());
                if (data.isNodeList()) {
                    return importNodes(support, data);
                } 

            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            
        }
        
        return false;
    }
    
    private boolean importNodes(TransferSupport support, QuantiTransferable.TransferData data) {

        JTree.DropLocation location = ((JTree.DropLocation) support.getDropLocation());
        TreePath dropTreePath = location.getPath();
        int childIndex = location.getChildIndex();
        AbstractNode dropRSMNode = (AbstractNode) dropTreePath.getLastPathComponent();

        QuantitationTree tree = QuantitationTree.getCurrentTree();
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
            } else if (type == AbstractNode.NodeTypes.PROJECT_QUANTITATION) {
                QuantitationProjectNode projectNode = ((QuantitationProjectNode) parentNode);
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
        DatabaseDataSetTask.updateDatasetAndProjectsTree(databaseObjectsToModify, false);

        return true;


    }
}
