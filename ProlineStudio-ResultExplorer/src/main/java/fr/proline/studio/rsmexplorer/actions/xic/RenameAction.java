package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.actions.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.DesignTree;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Rename a Node in a Xic DesignTree
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction {

    public RenameAction() {
        super(NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"), RSMTree.TreeType.TREE_XIC_DESIGN);
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final RSMNode n = selectedNodes[0];

        
        RSMNode.NodeTypes nodeType = n.getType();
        if ((nodeType == RSMNode.NodeTypes.BIOLOGICAL_GROUP)
                || (nodeType == RSMNode.NodeTypes.BIOLOGICAL_SAMPLE)
                || (nodeType == RSMNode.NodeTypes.DATA_SET)) {

            DataSetData data = ((DataSetData) n.getData());

            String name = data.getName();
            String newName = showRenameDialog(name, x, y);
            
            if (newName == null) {
                return;
            }

            if (name.compareTo(newName) != 0) {
                data.setTemporaryName(newName);
                ((DefaultTreeModel) DesignTree.getDesignTree().getModel()).nodeChanged(n);
            }
        }


    }
        
    private String showRenameDialog(String name, int x, int y) {
        
        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Rename", null, "New Name", OptionDialog.OptionDialogType.TEXTFIELD);
        dialog.setText(name);
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
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // we rename multiple nodes
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        RSMNode node = selectedNodes[0];
        RSMNode.NodeTypes nodeType = node.getType();
        if ((nodeType != RSMNode.NodeTypes.BIOLOGICAL_GROUP)
                && (nodeType != RSMNode.NodeTypes.BIOLOGICAL_SAMPLE)
                && (nodeType != RSMNode.NodeTypes.DATA_SET)) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }
    
    


}