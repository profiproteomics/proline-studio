package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to Change the description of a Project
 * @author JM235353
 */
public class ChangeDescriptionAction extends AbstractRSMAction {
   
    public ChangeDescriptionAction() {
        super(NbBundle.getMessage(ChangeDescriptionAction.class, "CTL_ChangeDescriptionAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final AbstractNode n = selectedNodes[0];


        IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) n;
        Project project = projectNode.getProject();

        String description = project.getDescription();
        String newDescription = showChangeDescriptionDialog(description, x, y);

        projectNode.changeNameAndDescription(project.getName(), newDescription);

    }
        
    private String showChangeDescriptionDialog(String description, int x, int y) {
        
        OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Change Description", null, "New Description", OptionDialog.OptionDialogType.TEXTAREA);
        dialog.setText(description);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        String newDescription = null;
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            newDescription = dialog.getText();
        }
        
        if ((newDescription != null) && (newDescription.length() > 0)) {
            return newDescription;
        }
        
        return null;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // we rename multiple nodes
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];
        AbstractNode.NodeTypes nodeType = node.getType();
        if (nodeType != AbstractNode.NodeTypes.PROJECT_IDENTIFICATION ) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }
    
    


}