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
package fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.AbstractTreeTransferHandler;
import fr.proline.studio.rsmexplorer.tree.xic.XICSelectionTransferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class QCMappingTransferHandler extends AbstractTreeTransferHandler {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private QCMappingTreeTable m_treeTable;
    private QCMappingTreeTableModel m_treeTableModel;

    public QCMappingTransferHandler(QCMappingTreeTable tree) {
        super(false, -1);
        m_treeTable = tree;
        m_treeTableModel = (QCMappingTreeTableModel) m_treeTable.getTreeTableModel();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);

        if (support.isDataFlavorSupported(XICSelectionTransferable.RSMNodeList_FLAVOR)) {

            // drop path
            JTable.DropLocation dropLocation = ((JTable.DropLocation) support.getDropLocation());
            if ((dropLocation == null) || (dropLocation.getRow() == -1) || (dropLocation.getColumn() == -1) || (dropLocation.getColumn() == 0)) {
                return false;
            }


            int currentRow = dropLocation.getRow();
            AbstractNode dropChannelTreeNode = m_treeTable.getNodeForRow(currentRow);
            DQuantitationChannelMapping dropLocationMapping = m_treeTableModel.getMapping().get(dropChannelTreeNode);
            if (dropLocationMapping == null) {
                // can not drop at this position
                return false;
            }


            AbstractNode.NodeTypes designNodeType = null;
            try {
                XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
                XICSelectionTransferable.TransferData data = XICSelectionTransferable.getData(transfer.getTransferKey());
                designNodeType = data.getDesignNodeType();
                ArrayList<AbstractNode> nodesList = (ArrayList<AbstractNode>) data.getDesignList();
                if (nodesList != null && !nodesList.isEmpty()) {
                    AbstractData rootData = ((AbstractNode) nodesList.get(0).getRoot()).getData();
                    DDataset sourceDS = ((DataSetData) rootData).getDataset();
                    DDataset destDS = m_treeTableModel.getDatasetAt(dropLocation.getColumn());
                    if (sourceDS.getId() != destDS.getId()) {
                        return false;
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }
            // TODO : Determine whether we accept the location depending on the node type
            return (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) || (designNodeType == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS);

        }
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        try {
            //get transfer data
            XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
            XICSelectionTransferable.TransferData transferData = XICSelectionTransferable.getData(transfer.getTransferKey());
            ArrayList<AbstractNode> transferDsNodesList = (ArrayList<AbstractNode>) transferData.getDesignList();
            if (transferDsNodesList == null) {
                return false;
            }
            //get drop node
            JTable.DropLocation dropLocation = ((JTable.DropLocation) support.getDropLocation());
            int currentRow = dropLocation.getRow();
            AbstractNode dropChannelTreeNode = m_treeTable.getNodeForRow(currentRow);
            DDataset dropLocationDs = m_treeTableModel.getDatasetAt(dropLocation.getColumn());
            //drop behavior
            for (AbstractNode dsNode : transferDsNodesList) {
                switch (dsNode.getType()) {
                    case BIOLOGICAL_GROUP:
                        m_logger.info("moving group");//no implement now
                    case BIOLOGICAL_SAMPLE_ANALYSIS: {
                        DQuantitationChannelMapping dropLocationMapping = m_treeTableModel.getMapping().get(dropChannelTreeNode);
                        if (dropLocationMapping != null) {
                            DataSetData userObject = (DataSetData) dsNode.getData();
                            dropLocationMapping.put(dropLocationDs, userObject.getChannelNumber());
                        }
                        int nextRowIndex = this.m_treeTable.getNextChannelRowIndex(currentRow, QCMappingTreeTable.DOWN);//DOWN ONLY
                        if (nextRowIndex != -1) {
                            dropChannelTreeNode = this.m_treeTable.getNodeForRow(nextRowIndex);
                            currentRow = nextRowIndex;
                        } else {
                            dropChannelTreeNode = null;
                        }
                        if (dropChannelTreeNode == null) {
                            continue;
                        }
                    }
                }
            }
            String doubleChannel = m_treeTableModel.redundantChannel(dropLocationDs);
            if (doubleChannel != null && !doubleChannel.isEmpty()) {
               
                JOptionPane.showMessageDialog(m_treeTable, doubleChannel);
            }
            return false;
        } catch (UnsupportedFlavorException ex) {
            m_logger.error(getClass().getSimpleName() + " DnD error ", ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

}
