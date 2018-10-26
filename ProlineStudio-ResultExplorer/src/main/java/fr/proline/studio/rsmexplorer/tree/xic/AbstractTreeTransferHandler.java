/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class AbstractTreeTransferHandler extends TransferHandler {
    
    private Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    protected boolean m_isIdentificationSelectionTree;
    
    protected AbstractTreeTransferHandler(boolean isSelectionTree) {
        m_isIdentificationSelectionTree = isSelectionTree;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (m_isIdentificationSelectionTree) {
            IdentificationSelectionTree tree = (IdentificationSelectionTree) c;
            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            ArrayList<DataSetNode> keptNodes = new ArrayList<>();
            retrieveLeaves(keptNodes, selectedNodes);
            if (keptNodes.isEmpty()) {
                return null;
            }
            XICSelectionTransferable.TransferData data = new XICSelectionTransferable.TransferData();
            data.setDatasetList(keptNodes);
            Integer transferKey = XICSelectionTransferable.register(data);
            return new XICSelectionTransferable(transferKey);
        } else {
            XICDesignTree tree = (XICDesignTree) c;
            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            ArrayList<AbstractNode> keptNodes = new ArrayList<>();
            // only the nodes of the same type can be transferred
            AbstractNode.NodeTypes commonType = null;
            int nbSelectedNode = selectedNodes.length;
            for (int i = 0; i < nbSelectedNode; i++) {
                AbstractNode node = selectedNodes[i];
                AbstractNode.NodeTypes type = node.getType();
                if ((type != AbstractNode.NodeTypes.BIOLOGICAL_GROUP) && (type != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) && (type != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)) {
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
            String keptNodesAsString = keptNodes.stream().map(i -> i.toString()).collect(Collectors.joining(",","[","]"));
            XICSelectionTransferable.TransferData data = new XICSelectionTransferable.TransferData();
            data.setDesignList(keptNodes);
            Integer transferKey = XICSelectionTransferable.register(data);
            return new XICSelectionTransferable(transferKey);
        }
    }

    protected void retrieveLeaves(ArrayList<DataSetNode> keptNodes, AbstractNode[] selectedNodes) {
        int nbSelectedNode = selectedNodes.length;
        for (int i = 0; i < nbSelectedNode; i++) {
            AbstractNode node = selectedNodes[i];
            retrieveLeaves(keptNodes, node);
        }
    }

    protected void retrieveLeaves(ArrayList<DataSetNode> keptNodes, AbstractNode node) {
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
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        // clean all transferred data
        if (m_isIdentificationSelectionTree) {
            XICSelectionTransferable.clearRegisteredData();
        }
    }
    
}
