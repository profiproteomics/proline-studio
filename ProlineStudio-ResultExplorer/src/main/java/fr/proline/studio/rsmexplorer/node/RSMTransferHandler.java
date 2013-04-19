package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class RSMTransferHandler extends TransferHandler {
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }
    
    
    @Override
    protected Transferable createTransferable(JComponent c) {

        if (c instanceof RSMTree) {
            RSMTree tree = (RSMTree) c;
            
            // only Dataset which are not being changed can be transferred
            // furthermore Dataset must be of the same project
            Integer commonProjectId = null;
            RSMNode[] selectedNodes = tree.getSelectedNodes();
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
                RSMNode node = selectedNodes[i];
                if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
                    return null;
                }
                if (node.isChanging()) {
                    return null;
                }
                
                RSMDataSetNode datasetNode = (RSMDataSetNode) node;
                
                if (datasetNode.isTrash()) {
                    return null;
                }
                
                Integer projectId = datasetNode.getDataset().getProject().getId();
                if (commonProjectId == null) {
                    commonProjectId = projectId;
                } else if (commonProjectId.intValue() != projectId.intValue()) {
                    return null;
                }
            } 
            
            // we must keep only parent nodes
            // if a child and its parent are selected, we keep only the parent
            ArrayList<RSMNode> keptNodes = new ArrayList<>(nbSelectedNode);
            keptNodes.add(selectedNodes[0]);
            mainloop:
            for (int i=1;i<nbSelectedNode;i++) {
                RSMNode curNode = selectedNodes[i];
                
                // look for an ancestor
                int nbKeptNodes = keptNodes.size();
                for (int j=0;j<nbKeptNodes;j++) {
                    
                    RSMNode curKeptNode = keptNodes.get(j);
                    if (curNode.isNodeAncestor(curKeptNode)) {
                        // ancestor is already in kept node
                        continue mainloop;
                    }
                }
                // look for children and remove them
                for (int j=nbKeptNodes-1;j>=0;j--) {
                    
                    RSMNode curKeptNode = keptNodes.get(j);
                    if (curKeptNode.isNodeAncestor(curNode)) {
                        // we have found a children
                        keptNodes.remove(j);
                    }
                }
                keptNodes.add(curNode);
 
            }


            RSMTransferable.TransferData data = new RSMTransferable.TransferData();
            data.setNodeList(keptNodes);
            Integer transferKey =  RSMTransferable.register(data);

            
            
            return new RSMTransferable(transferKey, commonProjectId);

        }
        return null;
    }

    
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        


        // clean all transferred data
        RSMTransferable.clearRegisteredData();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(RSMTransferable.RSMNodeList_FLAVOR)) {

            // drop path
            TreePath dropTreePath = ((JTree.DropLocation) support.getDropLocation()).getPath();
            if (dropTreePath == null) {
                // should not happen
                return false;
            }
            
            // Determine whether we accept the location
            Object dropComponent = dropTreePath.getLastPathComponent();
            
            if (! (dropComponent instanceof RSMNode)) {
                return false;
            }
            
            // We can drop only in project or aggregate node with no Rsm or Rset
            // The node must not being changed
            RSMNode dropRSMNode = (RSMNode) dropComponent;
            if (dropRSMNode.isChanging()) {
                return false;
            }
            
            Integer dropProjectId = null;
            RSMNode.NodeTypes nodeType = dropRSMNode.getType();
            if ( nodeType == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode dropDatasetNode = (RSMDataSetNode) dropRSMNode;
                if (dropDatasetNode.hasResultSet() || dropDatasetNode.hasResultSummary()) {
                    return false;
                }
                dropProjectId = dropDatasetNode.getDataset().getProject().getId();
            } else if ( nodeType == RSMNode.NodeTypes.PROJECT) {
                RSMProjectNode dropProjectNode = (RSMProjectNode) dropRSMNode;
                dropProjectId = dropProjectNode.getProject().getId();
            } else {
                return false;
            }


            
            
            try {
                
                // drop node must be in the same project that nodes being transferred
                RSMTransferable nodeListTransferable = (RSMTransferable) support.getTransferable().getTransferData(RSMTransferable.RSMNodeList_FLAVOR);
                if (nodeListTransferable.getProjectId().intValue() != dropProjectId.intValue()) {
                    return false;
                }
                
                // drop node must not be equal or a child of a transferred node
                RSMTransferable.TransferData data = RSMTransferable.getData(nodeListTransferable.getTransferKey());
                
                if (data.isNodeList()) {
                    ArrayList<RSMNode> nodeList = (ArrayList<RSMNode>) data.getDataList();
                    int nbNodes = nodeList.size();
                    for (int i = 0; i < nbNodes; i++) {
                        RSMNode nodeTransfered = nodeList.get(i);

                        if (dropRSMNode.isNodeAncestor(nodeTransfered)) {
                            return false;
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                Logger logger = LoggerFactory.getLogger(RSMTransferHandler.class);
                logger.error(getClass().getSimpleName() + " DnD error ", e);
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
                RSMTransferable transfer = (RSMTransferable) support.getTransferable().getTransferData(RSMTransferable.RSMNodeList_FLAVOR);
                RSMTransferable.TransferData data = RSMTransferable.getData(transfer.getTransferKey());
                if (data.isNodeList()) {
                    return importNodes(support, data);
                } else {
                    return importResultSets(support, data);
                }

            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                Logger logger = LoggerFactory.getLogger(RSMTransferHandler.class);
                logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            
        }
        
        return false;
    }
    
    private boolean importResultSets(TransferSupport support, RSMTransferable.TransferData data) {
        System.out.println("importResultSets");
        return false;
    }
    
    private boolean importNodes(TransferSupport support, RSMTransferable.TransferData data) {



        JTree.DropLocation location = ((JTree.DropLocation) support.getDropLocation());
        TreePath dropTreePath = location.getPath();
        int childIndex = location.getChildIndex();
        RSMNode dropRSMNode = (RSMNode) dropTreePath.getLastPathComponent();

        RSMTree tree = RSMTree.getTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        // no insert index specified -> we insert at the end
        if (childIndex == -1) {

            childIndex = dropRSMNode.getChildCount();

            // special case to drop before the Trash
            if (childIndex > 0) {
                RSMNode lastChild = (RSMNode) dropRSMNode.getChildAt(childIndex - 1);
                if ((lastChild instanceof RSMDataSetNode) && ((RSMDataSetNode) lastChild).isTrash()) {
                    childIndex--; // we drop before the Trash
                }
            }
        }

        // used to keep parent node modified
        // to be able later to modify the database
        HashSet<RSMNode> allParentNodeModified = new HashSet<>();
        allParentNodeModified.add(dropRSMNode);


        ArrayList<RSMNode> nodeList = (ArrayList<RSMNode>) data.getDataList();
        int nbNodes = nodeList.size();
        for (int i = 0; i < nbNodes; i++) {
            RSMNode node = nodeList.get(i);


            // specific case when the node is moved in its parent
            int indexChild = -1;
            if (dropRSMNode.isNodeChild(node)) {
                // we are moving the node in its parent
                indexChild = dropRSMNode.getIndex(node);
                if (indexChild < childIndex) {
                    childIndex--;
                }
            } else {
                allParentNodeModified.add((RSMNode) node.getParent());
            }

            // remove from parent (required when drag and dropped in the same parent)
            treeModel.removeNodeFromParent(node);

            // add to new parent
            treeModel.insertNodeInto(node, dropRSMNode, childIndex);

            childIndex++;

        }

        // create HashMap of Databse Object which need to be updated
        HashMap<Object, ArrayList<Dataset>> databaseObjectsToModify = new HashMap<>();

        Iterator<RSMNode> it = allParentNodeModified.iterator();
        while (it.hasNext()) {

            RSMNode parentNode = it.next();

            // get Parent Database Object
            Object databaseParentObject = null;
            RSMNode.NodeTypes type = parentNode.getType();
            if (type == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode datasetNode = ((RSMDataSetNode) parentNode);
                databaseParentObject = datasetNode.getDataset();
            } else if (type == RSMNode.NodeTypes.PROJECT) {
                RSMProjectNode projectNode = ((RSMProjectNode) parentNode);
                databaseParentObject = projectNode.getProject();
            }



            // get new Dataset children
            int nbChildren = parentNode.getChildCount();
            ArrayList<Dataset> datasetList = new ArrayList<>(nbChildren);
            for (int i = 0; i < nbChildren; i++) {
                // we are sure that it is a Dataset

                RSMNode childNode = ((RSMNode) parentNode.getChildAt(i));
                if (!(childNode instanceof RSMDataSetNode)) {
                    continue; // possible for "All imported" node
                }
                RSMDataSetNode childDatasetNode = (RSMDataSetNode) childNode;
                Dataset dataset = childDatasetNode.getDataset();
                datasetList.add(dataset);
            }

            // register this modification
            databaseObjectsToModify.put(databaseParentObject, datasetList);
        }

        // ask the modification to the database at once (intricate to put in a thread in Dnd context)
        DatabaseDataSetTask.updateDatasetAndProjectsTree(databaseObjectsToModify);


        return true;


    }

}
