package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to Rename a dataset
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction {

    // tree type: could be Identification or Quantitation
    AbstractTree.TreeType m_treeType = null;
    
    /**
     * Builds the RenameAction depending of the treeType
     * @param treeType 
     */
    public RenameAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"), treeType);
        this.m_treeType = treeType;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final AbstractNode n = selectedNodes[0];

        
        AbstractNode.NodeTypes nodeType = n.getType();
        if (nodeType == AbstractNode.NodeTypes.DATA_SET) {
            DataSetNode datasetNode = (DataSetNode) n;
            DDataset dataset = datasetNode.getDataset();
            
            String name = dataset.getName();
            String newName = showRenameDialog(name, x, y);

            // depends on the treeType
            AbstractTree tree = null;
            if (m_treeType == AbstractTree.TreeType.TREE_IDENTIFICATION ){
                tree = IdentificationTree.getCurrentTree();
            }else if (m_treeType == AbstractTree.TreeType.TREE_QUANTITATION ){
                 tree = QuantitationTree.getCurrentTree();
            }
            datasetNode.rename(newName, tree);
            
        } else if (nodeType == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
            IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) n;
            Project project = projectNode.getProject();
            
            String name = project.getName();
            String newName = showRenameDialog(name,  x, y);

            projectNode.changeNameAndDescription(newName, project.getDescription());

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
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // we rename multiple nodes
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];
        AbstractNode.NodeTypes nodeType = node.getType();
        if ((nodeType != AbstractNode.NodeTypes.DATA_SET) && (nodeType != AbstractNode.NodeTypes.PROJECT_IDENTIFICATION )) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }
    
    


}