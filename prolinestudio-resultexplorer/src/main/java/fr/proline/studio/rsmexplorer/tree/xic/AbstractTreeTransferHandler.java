/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
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
            //create Transferable for IdentificationSelectionTree
            //data = DataSetNode List
            IdentificationSelectionTree tree = (IdentificationSelectionTree) c;
            AbstractNode[] selectedNodes = tree.getSelectedNodes();
            ArrayList<DataSetNode> keptNodes = new ArrayList<>();
            retrieveLeaves(keptNodes, selectedNodes);
            if (keptNodes.isEmpty()) {
                return null;
            }
            XICSelectionTransferable.TransferData data = new XICSelectionTransferable.TransferData();
            data.setDatasetList(keptNodes);//set Transferable data
            Integer transferKey = XICSelectionTransferable.register(data);
            return new XICSelectionTransferable(transferKey);
        } else {
            //create Transferable for QuantExperimentalDesignTree
            //date = AbstractNode List
            QuantExperimentalDesignTree tree = (QuantExperimentalDesignTree) c;
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
            data.setDesignList(keptNodes);//set Transferable data
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
