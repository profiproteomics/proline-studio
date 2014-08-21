package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICRunNode;
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
 * Management of the drag and drop from the XICSelectionTree to the XIC XICDesignTree
 * @author JM235353
 */
public class XICTransferHandler extends TransferHandler {
    
    private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    private boolean m_isSelectionTree;
    
    public XICTransferHandler(boolean isSelectionTree) {
        m_isSelectionTree = isSelectionTree;
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }
    
    

    
    @Override
    protected Transferable createTransferable(JComponent c) {

        if (m_isSelectionTree) {
            XICSelectionTree tree = (XICSelectionTree) c;

            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            ArrayList<DataSetNode> keptNodes = new ArrayList<>();
            
            retrieveLeaves(keptNodes, selectedNodes);
            
            if (keptNodes.isEmpty()) {
                return null;
            }

            XICSelectionTransferable.TransferData data = new XICSelectionTransferable.TransferData();
            data.setDatasetList(keptNodes);
            Integer transferKey =  XICSelectionTransferable.register(data);

            return new XICSelectionTransferable(transferKey);




        } else {
            XICDesignTree tree = (XICDesignTree) c;

            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            
            ArrayList<AbstractNode> keptNodes = new ArrayList<>();
            
            // only the nodes of the same type can be transferred
            AbstractNode.NodeTypes commonType = null;
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
                AbstractNode node = selectedNodes[i];
               
                AbstractNode.NodeTypes type = node.getType();
                
                if ((type != AbstractNode.NodeTypes.BIOLOGICAL_GROUP) && (type != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) &&  (type !=AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)) {
                    return null;
                }
                if (commonType != null) {
                    if (commonType != type) {
                        return null;
                    }
                } else {
                    commonType = type;
                }
                
                keptNodes.add(node);
            } 
            
            
            XICSelectionTransferable.TransferData data = new XICSelectionTransferable.TransferData();
            data.setDesignList(keptNodes);
            Integer transferKey =  XICSelectionTransferable.register(data);

            
            
            return new XICSelectionTransferable(transferKey);

            
            /*
            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            ArrayList<RSMBiologicalSampleAnalysisNode> keptNodes = new ArrayList<>();
            
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
                AbstractNode node = selectedNodes[i];


                AbstractNode.NodeTypes type = node.getType();
                if (type!= AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                    return null;
                }
                XICBiologicalSampleAnalysisNode sampleAnalysisNode = (XICBiologicalSampleAnalysisNode) node;
                if (!sampleAnalysisNode.hasResultSummary()) {
                    return null;
                }

                keptNodes.add(sampleAnalysisNode);


                
                
            } 
            
            XICSelectionTransferable.TransferData data = new XICSelectionTransferable.TransferData();
            data.setSampleAnalysisList(keptNodes);
            Integer transferKey =  XICSelectionTransferable.register(data);

            
            
            return new XICSelectionTransferable(transferKey);*/


        }

    }

    private void retrieveLeaves(ArrayList<DataSetNode> keptNodes, AbstractNode[] selectedNodes) {
        int nbSelectedNode = selectedNodes.length;
        for (int i = 0; i < nbSelectedNode; i++) {
            AbstractNode node = selectedNodes[i];
            retrieveLeaves(keptNodes, node);
        }
    }

    private void retrieveLeaves(ArrayList<DataSetNode> keptNodes, AbstractNode node) {
        if (node.isChanging()) {
            return;
        }
        AbstractNode.NodeTypes type = node.getType();
        if (type != AbstractNode.NodeTypes.DATA_SET) {
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;

        if (node.isLeaf()) {
            if (!datasetNode.hasResultSummary()) {
                return;
            }
            keptNodes.add(datasetNode);
        } else {
            int nbChildren = node.getChildCount();
            for (int i = 0; i < nbChildren; i++) {
                retrieveLeaves(keptNodes, (AbstractNode) node.getChildAt(i));
            }
        }
    }
    
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

        // clean all transferred data
        if (m_isSelectionTree) {
            XICSelectionTransferable.clearRegisteredData();
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        if (!m_isSelectionTree) {

            support.setShowDropLocation(true);
            
            if (support.isDataFlavorSupported(XICSelectionTransferable.RSMNodeList_FLAVOR)) {

                // drop path
                TreePath dropTreePath = ((JTree.DropLocation) support.getDropLocation()).getPath();
                if (dropTreePath == null) {
                    // should not happen
                    return false;
                }
                
                
                boolean designData;
                AbstractNode.NodeTypes designNodeType = null;
                try {
                    XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                    XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());
                    designData = data.isDesignData();
                    if (designData) {
                        designNodeType = data.getDesignNodeType();
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    // should never happen
                    m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                    return false;
                }


                // Determine whether we accept the location
                Object dropComponent = dropTreePath.getLastPathComponent();
                if (designData) {
                    if (!(dropComponent instanceof AbstractNode)) {
                        return false;
                    }
                    
                    AbstractNode.NodeTypes dropType = ((AbstractNode) dropComponent).getType();
                    switch (dropType) {
                        case DATA_SET:
                            return (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP);
                        case BIOLOGICAL_GROUP:
                            return (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE);
                        case BIOLOGICAL_SAMPLE:
                            return (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS);
                        default:
                            return false;
                            
                    }

                } else {

                    if ((dropComponent instanceof XICBiologicalSampleNode) || // Sample Node
                            (dropComponent instanceof DataSetNode) || // XIC Node
                            (dropComponent instanceof XICBiologicalGroupNode)) {    // Group Node
                        return true;
                    }

                }
            }
        }

        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {

        if (canImport(support)) {

            try {
                XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());
                return importNodes(support, data);


            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            
        }
        
        return false;
    }

    
    private boolean importNodes(TransferSupport support, XICSelectionTransferable.TransferData data) {

        JTree.DropLocation location = ((JTree.DropLocation) support.getDropLocation());
        TreePath dropTreePath = location.getPath();
        int childIndex = location.getChildIndex();
        AbstractNode dropRSMNode = (AbstractNode) dropTreePath.getLastPathComponent();

        
        XICDesignTree tree = XICDesignTree.getDesignTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        // no insert index specified -> we insert at the end
        if (childIndex == -1) {

            childIndex = dropRSMNode.getChildCount();
        }


        ArrayList<DataSetNode> datasetList = (ArrayList<DataSetNode>) data.getDatasetList();
        if (datasetList != null) {
            
            tree.expandNodeIfNeeded(dropRSMNode);
            
            if (dropRSMNode instanceof DataSetNode) {
                // top node, we create a group now
                String groupName = "Group "+Integer.toString(dropRSMNode.getChildCount()+1);
                XICBiologicalGroupNode biologicalGroupNode = new XICBiologicalGroupNode(new DataSetData(groupName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                treeModel.insertNodeInto(biologicalGroupNode, dropRSMNode, childIndex);
                dropRSMNode = biologicalGroupNode;
                childIndex = 0;
                tree.expandNodeIfNeeded(dropRSMNode);
            }
            
            if (dropRSMNode instanceof XICBiologicalGroupNode) {
                // Group Node, we create a sample node
                String sampleName = "Sample " + Integer.toString(dropRSMNode.getChildCount() + 1);
                XICBiologicalSampleNode biologicalSampleNode = new XICBiologicalSampleNode(new DataSetData(sampleName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.OTHER));
                treeModel.insertNodeInto(biologicalSampleNode, dropRSMNode, childIndex);
                dropRSMNode = biologicalSampleNode;
                childIndex = 0;
                tree.expandNodeIfNeeded(dropRSMNode);
            }
            
            
            if (dropRSMNode instanceof XICBiologicalSampleNode) {
                int nbNodes = datasetList.size();
                for (int i = 0; i < nbNodes; i++) {
                    DataSetNode node = datasetList.get(i);

                    // create the new node
                    XICBiologicalSampleAnalysisNode sampleAnalysisNode = new XICBiologicalSampleAnalysisNode(node.getData());

                    // put a Run node in it

                    XICRunNode runNode = new XICRunNode(new RunInfoData(), node.getDataset().getProject().getId(), node.getResultSetId());
                    sampleAnalysisNode.add(runNode);

                    // add to new parent
                    treeModel.insertNodeInto(sampleAnalysisNode, dropRSMNode, childIndex);

                    childIndex++;

                }
            }
            

        } else {
            ArrayList<AbstractNode> rsmList = (ArrayList<AbstractNode>) data.getDesignList();
            int nbNodes = rsmList.size();
            for (int i = 0; i < nbNodes; i++) {
                AbstractNode node = rsmList.get(i);


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

