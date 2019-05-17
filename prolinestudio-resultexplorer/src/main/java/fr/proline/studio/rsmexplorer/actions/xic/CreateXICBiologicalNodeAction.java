/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 7 mai 2019
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode.NodeTypes;
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalGroupNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleNode;
import javax.swing.Action;
import javax.swing.tree.DefaultTreeModel;
import org.openide.windows.WindowManager;

/**
 *
 * @author Karine XUE
 */
public class CreateXICBiologicalNodeAction extends AbstractRSMAction {

    private NodeTypes m_createNodeType;

    private final QuantExperimentalDesignTree m_tree;

    public CreateXICBiologicalNodeAction(String actionName, NodeTypes type, QuantExperimentalDesignTree tree) {
        super(actionName, tree);
        m_createNodeType = type;
        m_tree = tree;
    }

    @Override //never used
    public void updateEnabled(AbstractNode[] selectedNodes) {
        NodeTypes nodeType = selectedNodes[0].getType();
        if (selectedNodes.length == 1) {
            if (nodeType == NodeTypes.DATA_SET) {
                setEnabled(true);
            } else if ((nodeType == NodeTypes.BIOLOGICAL_GROUP && m_createNodeType == NodeTypes.BIOLOGICAL_SAMPLE)
                    || (nodeType == NodeTypes.BIOLOGICAL_SAMPLE && m_createNodeType == NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
                setEnabled(true);
            } else {
                setEnabled(false);
            }
        }
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        final AbstractNode parentNode = selectedNodes[0];
        AbstractNode interNode;
        switch (m_createNodeType) {
            case BIOLOGICAL_GROUP:
                this.createXICNode(parentNode, m_createNodeType, "Group 1", x, y, true);
                break;
            case BIOLOGICAL_SAMPLE:
                if (parentNode.getType() == AbstractNode.NodeTypes.BIOLOGICAL_GROUP) {
                    this.createXICNode(parentNode, m_createNodeType, "Sample 1", x, y, true);
                } else if (parentNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
                    AbstractNode gNode = this.createXICNode(parentNode, NodeTypes.BIOLOGICAL_GROUP, "New Group", x, y, false);
                    AbstractNode sNode = this.createXICNode(gNode, m_createNodeType, "New Sample", x, y, true);
                }
                break;
            case BIOLOGICAL_SAMPLE_ANALYSIS:
                if (parentNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
                    AbstractNode gNode = this.createXICNode(parentNode, NodeTypes.BIOLOGICAL_GROUP, "New Group", x, y, false);
                    AbstractNode sNode = this.createXICNode(gNode, NodeTypes.BIOLOGICAL_SAMPLE, "New Sample", x, y, false);
                    this.createXICNode(sNode, m_createNodeType, "Channel new 1", x, y, true);
                } else if (parentNode.getType() == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE) {
                    this.createXICNode(parentNode, m_createNodeType, "Channel new 1", x, y, true);
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * create a Node at insert it in the tree
     * @param parentNode
     * @param creatNodeType
     * @param defaultName
     * @param x,dialog screen position
     * @param y,dialog screen position
     * @param withDialog
     * @return 
     */
    private AbstractNode createXICNode(AbstractNode parentNode, NodeTypes creatNodeType, String defaultName, int x, int y, boolean withDialog) {
        AbstractNode createdNode = null;
        String itemName = defaultName;
        if (withDialog) {
            itemName = showItemNameDialog(defaultName, x, y);
            if (itemName == null) {
                return null;
            }
        }
        DataSetData temporaryData = DataSetData.createTemporaryAggregate(itemName);
        switch (creatNodeType) {
            case BIOLOGICAL_GROUP:
                createdNode = new XICBiologicalGroupNode(temporaryData);
                break;
            case BIOLOGICAL_SAMPLE:
                createdNode = new XICBiologicalSampleNode(temporaryData);
                break;
            case BIOLOGICAL_SAMPLE_ANALYSIS:
                createdNode = new XICBiologicalSampleAnalysisNode(temporaryData);
                break;
            default:
                break;
        }
        if (createdNode == null) {
            return null;
        }
        DefaultTreeModel treeModel = (DefaultTreeModel) m_tree.getModel();
        treeModel.insertNodeInto(createdNode, parentNode, 0);
        m_tree.expandNodeIfNeeded(parentNode);
        return createdNode;
    }

    private String showItemNameDialog(String defaultName, int x, int y) {
        String createItemName = "New " + (String) super.getValue(Action.NAME) + "'s Name";
        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Name", null, createItemName, OptionDialog.OptionDialogType.TEXTFIELD);
        dialog.setText(defaultName);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        String newName = null;
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            newName = dialog.getText();
        }

        if ((newName != null) && (newName.length() > 0)) {
            return newName;
        }

        return null;
    }
}
