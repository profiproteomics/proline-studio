/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 19 avr. 2019
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import static fr.proline.studio.rsmexplorer.tree.AbstractNode.NodeTypes.BIOLOGICAL_GROUP;
import static fr.proline.studio.rsmexplorer.tree.AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS;
import fr.proline.studio.rsmexplorer.tree.xic.AbstractTreeTransferHandler;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICSelectionTransferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTreeTable;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class QCMappingTransferHandler extends AbstractTreeTransferHandler {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private JXTreeTable m_tree;
    private QCMappingTreeTableModel m_treeTableModel;

    public QCMappingTransferHandler(JXTreeTable tree) {
        super(false);
        m_tree = tree;
        m_treeTableModel = (QCMappingTreeTableModel) m_tree.getTreeTableModel();
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
            return (designNodeType == BIOLOGICAL_GROUP) || (designNodeType == BIOLOGICAL_SAMPLE_ANALYSIS);

        }
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        try {
            JTable.DropLocation dropLocation = ((JTable.DropLocation) support.getDropLocation());
            TreePath dropPath = m_tree.getPathForRow(dropLocation.getRow());
            AbstractNode dropNode = (dropPath != null) ? (AbstractNode) dropPath.getLastPathComponent() : null;
            DDataset dropLocationDs = m_treeTableModel.getDatasetAt(dropLocation.getColumn());

            XICSelectionTransferable transfer = (XICSelectionTransferable) support.getTransferable().getTransferData(XICSelectionTransferable.RSMNodeList_FLAVOR);
            XICSelectionTransferable.TransferData transferData = XICSelectionTransferable.getData(transfer.getTransferKey());
            ArrayList<AbstractNode> transferDsNodesList = (ArrayList<AbstractNode>) transferData.getDesignList();

            if (transferDsNodesList == null) {
                return false;
            }
            AbstractNode dropChannelNode = dropNode;
            for (AbstractNode dsNode : transferDsNodesList) {
                switch (dsNode.getType()) {
                    case BIOLOGICAL_GROUP:
                        m_logger.info("moving group");
                    case BIOLOGICAL_SAMPLE_ANALYSIS: {
                        DQuantitationChannelMapping dropLocationMapping = m_treeTableModel.getMapping().get(dropChannelNode);
                        if (dropLocationMapping != null) {
                            DataSetData userObject = (DataSetData) dsNode.getData();
                            dropLocationMapping.put(dropLocationDs, userObject.getChannelNumber());
                        }
                        dropChannelNode = this.m_treeTableModel.getNextChannelNode((XICBiologicalSampleAnalysisNode)dropChannelNode);
                        if (dropChannelNode == null)
                            continue;
                    }
                }
                
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