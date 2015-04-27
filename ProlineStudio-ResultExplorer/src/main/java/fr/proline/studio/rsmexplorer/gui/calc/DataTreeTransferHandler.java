package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.DataTree.DataNode;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graph.DataGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;

/**
 *
 * @author JM235353
 */
public class DataTreeTransferHandler extends TransferHandler {

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {

        if (c instanceof DataTree) {
            DataTree tree = (DataTree) c;

            // only Dataset which are not being changed can be transferred
            // furthermore Dataset must be of the same project
            DataNode[] selectedNodes = tree.getSelectedNodes();
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
                DataNode node = selectedNodes[i];
                if ((node.getType() != DataNode.DataNodeType.VIEW_DATA) && (node.getType() != DataNode.DataNodeType.FUNCTION)) {
                    return null;
                }
            } 

            DataTreeTransferable.TransferData data = new DataTreeTransferable.TransferData();
            data.setDataNodes(selectedNodes);
            Integer transferKey =  DataTreeTransferable.register(data);

            
            
            return new DataTreeTransferable(transferKey);

        }
        return null;
    }
    
    
    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(DataTreeTransferable.DATANODE_FLAVOR)) {

            // drop path
            DropLocation dropLocation = support.getDropLocation();
            if (dropLocation == null) {
                // should not happen
                return false;
            }
            
            if (dropLocation instanceof JTree.DropLocation) {
                return false;
            }
            
            
            return true;
            
            /*
            // Determine whether we accept the location
            Object dropComponent = dropTreePath.getLastPathComponent();
            
            if (! (dropComponent instanceof AbstractNode)) {
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
            if ( nodeType == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dropDatasetNode = (DataSetNode) dropRSMNode;
                if (dropDatasetNode.hasResultSet() || dropDatasetNode.hasResultSummary()) {
                    return false;
                }
                dropProjectId = dropDatasetNode.getDataset().getProject().getId();
            } else if ( nodeType == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
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

            
            
            return true;*/

        }

        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {

        if (canImport(support)) {

            try {
                DataTreeTransferable transfer = (DataTreeTransferable) support.getTransferable().getTransferData(DataTreeTransferable.DATANODE_FLAVOR);
                DataTreeTransferable.TransferData data = DataTreeTransferable.getData(transfer.getTransferKey());
                
                
                DataNode[] dataNodes = data.getDataNodes();
                TransferHandler.DropLocation dropLocation = support.getDropLocation();
                Point p = dropLocation.getDropPoint();
                int x = p.x;
                int y = p.y;
                GraphPanel graphPanel = ((GraphPanel)support.getComponent());
                for (DataNode node : dataNodes) {
                    switch (node.getType()) {

                        case VIEW_DATA: {
                            TableInfo tableInfo = ((DataTree.ViewDataNode) node).getTableInfo();
                            DataGraphNode graphNode = new DataGraphNode(tableInfo);
                            graphPanel.addGraphNode(graphNode, x, y);
                            break;
                        }
                        case FUNCTION: {
                            AbstractFunction function = ((DataTree.FunctionNode) node).getFunction();
                            FunctionGraphNode graphNode = new FunctionGraphNode(function);
                            graphPanel.addGraphNode(graphNode, x, y);
                            break;
                        }

                    }
                    x += DataGraphNode.WIDTH/2;
                    y += DataGraphNode.HEIGHT/2; 
                    
                }


            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                //m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            
        }
        
        return false;
    }
    
}
