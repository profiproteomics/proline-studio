package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to Rename a dataset
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction {

    public RenameAction() {
        super(NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"));
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final RSMNode n = selectedNodes[0];

        
        RSMNode.NodeTypes nodeType = n.getType();
        if (nodeType == RSMNode.NodeTypes.DATA_SET) {
            RSMDataSetNode datasetNode = (RSMDataSetNode) n;
            Dataset dataset = datasetNode.getDataset();
            
            String name = dataset.getName();
            String newName = showRenameDialog(name, x, y);

            datasetNode.rename(newName);
            
        } else if (nodeType == RSMNode.NodeTypes.PROJECT) {
            RSMProjectNode projectNode = (RSMProjectNode) n;
            Project project = projectNode.getProject();
            
            String name = project.getName();
            String newName = showRenameDialog(name,  x, y);

            projectNode.rename(newName);

        }


    }
        
    private String showRenameDialog(String name, int x, int y) {
        
        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Rename", null, "New Name");
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
        if ((nodeType != RSMNode.NodeTypes.DATA_SET) && (nodeType != RSMNode.NodeTypes.PROJECT )) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }
    
    


}