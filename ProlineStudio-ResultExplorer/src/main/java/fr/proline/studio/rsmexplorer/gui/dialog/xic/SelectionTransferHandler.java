package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.node.xic.RSMBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.node.xic.RSMBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
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
public class SelectionTransferHandler extends TransferHandler {
    
    private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    private boolean m_isSelectionTree;
    
    public SelectionTransferHandler(boolean isSelectionTree) {
        m_isSelectionTree = isSelectionTree;
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }
    
    
    @Override
    protected Transferable createTransferable(JComponent c) {

        if (m_isSelectionTree) {
            SelectionTree tree = (SelectionTree) c;

            RSMNode[] selectedNodes = tree.getSelectedNodes();
            ArrayList<RSMDataSetNode> keptNodes = new ArrayList<>();
            
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
                RSMNode node = selectedNodes[i];
                if (!node.isLeaf()) {
                    return null;
                }
                if (node.isChanging()) {
                    return null;
                }

                RSMNode.NodeTypes type = node.getType();
                if (type != RSMNode.NodeTypes.DATA_SET) {
                    return null;
                }

                RSMDataSetNode datasetNode = (RSMDataSetNode) node;

                if (!datasetNode.hasResultSummary()) {
                    return null;
                }

                keptNodes.add(datasetNode);



 
            } 
            
            SelectionTransferable.TransferData data = new SelectionTransferable.TransferData();
            data.setDatasetList(keptNodes);
            Integer transferKey =  SelectionTransferable.register(data);

            
            
            return new SelectionTransferable(transferKey);




        } else {
            DesignTree tree = (DesignTree) c;

            RSMNode[] selectedNodes = tree.getSelectedNodes();
            ArrayList<RSMBiologicalSampleAnalysisNode> keptNodes = new ArrayList<>();
            
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
                RSMNode node = selectedNodes[i];
                if (!node.isLeaf()) {
                    return null;
                }


                RSMNode.NodeTypes type = node.getType();
                if (type!= RSMNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                    return null;
                }
                RSMBiologicalSampleAnalysisNode sampleAnalysisNode = (RSMBiologicalSampleAnalysisNode) node;
                if (!sampleAnalysisNode.hasResultSummary()) {
                    return null;
                }

                keptNodes.add(sampleAnalysisNode);


                
                
            } 
            
            SelectionTransferable.TransferData data = new SelectionTransferable.TransferData();
            data.setSampleAnalysisList(keptNodes);
            Integer transferKey =  SelectionTransferable.register(data);

            
            
            return new SelectionTransferable(transferKey);


        }

    }

    
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

        // clean all transferred data
        if (m_isSelectionTree) {
            SelectionTransferable.clearRegisteredData();
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        if (!m_isSelectionTree) {

            support.setShowDropLocation(true);
            if (support.isDataFlavorSupported(SelectionTransferable.RSMNodeList_FLAVOR)) {

                // drop path
                TreePath dropTreePath = ((JTree.DropLocation) support.getDropLocation()).getPath();
                if (dropTreePath == null) {
                    // should not happen
                    return false;
                }

                // Determine whether we accept the location
                Object dropComponent = dropTreePath.getLastPathComponent();

                if (!(dropComponent instanceof RSMBiologicalSampleNode)) {
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {

        if (canImport(support)) {

            try {
                SelectionTransferable transfer = (SelectionTransferable) support.getTransferable().getTransferData(SelectionTransferable.RSMNodeList_FLAVOR);
                SelectionTransferable.TransferData data = SelectionTransferable.getData(transfer.getTransferKey());
                return importNodes(support, data);


            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            
        }
        
        return false;
    }

    
    private boolean importNodes(TransferSupport support, SelectionTransferable.TransferData data) {

        JTree.DropLocation location = ((JTree.DropLocation) support.getDropLocation());
        TreePath dropTreePath = location.getPath();
        int childIndex = location.getChildIndex();
        RSMBiologicalSampleNode dropRSMNode = (RSMBiologicalSampleNode) dropTreePath.getLastPathComponent();

        DesignTree tree = DesignTree.getDesignTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        // no insert index specified -> we insert at the end
        if (childIndex == -1) {

            childIndex = dropRSMNode.getChildCount();
        }


        ArrayList<RSMDataSetNode> datasetList = (ArrayList<RSMDataSetNode>) data.getDatasetList();
        if (datasetList != null) {
            int nbNodes = datasetList.size();
            for (int i = 0; i < nbNodes; i++) {
                RSMDataSetNode node = datasetList.get(i);

                // create the new node
                RSMBiologicalSampleAnalysisNode sampleAnalysisNode = new RSMBiologicalSampleAnalysisNode(node.getData());

                // add to new parent
                treeModel.insertNodeInto(sampleAnalysisNode, dropRSMNode, childIndex);

                childIndex++;

            }
        } else {
            ArrayList<RSMBiologicalSampleAnalysisNode> sampleAnalysisList = (ArrayList<RSMBiologicalSampleAnalysisNode>) data.getSampleAnalysisList();
            int nbNodes = sampleAnalysisList.size();
            for (int i = 0; i < nbNodes; i++) {
                RSMNode node = sampleAnalysisList.get(i);


                // specific case when the node is moved in its parent
                int indexChild;
                if (dropRSMNode.isNodeChild(node)) {
                    // we are moving the node in its parent
                    indexChild = dropRSMNode.getIndex(node);
                    if (indexChild < childIndex) {
                        childIndex--;
                    }
                }

                // remove from parent (required when drag and dropped in the same parent)
                treeModel.removeNodeFromParent(node);

                // add to new parent
                treeModel.insertNodeInto(node, dropRSMNode, childIndex);

                childIndex++;

            }
        }
        return true;


    }

    
}

