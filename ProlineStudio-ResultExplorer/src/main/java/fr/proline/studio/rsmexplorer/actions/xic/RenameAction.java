package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Rename a Node in a Xic XICDesignTree
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction { 
    
    private final XICDesignTree m_tree;

    public RenameAction(XICDesignTree tree) {
        super(NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"), AbstractTree.TreeType.TREE_XIC_DESIGN, tree);
        m_tree = tree;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final AbstractNode n = selectedNodes[0];

        
        AbstractNode.NodeTypes nodeType = n.getType();
        
        switch(nodeType){
            case BIOLOGICAL_GROUP:
            case BIOLOGICAL_SAMPLE:
            case DATA_SET:
                DataSetData data = ((DataSetData) n.getData());

                String name = data.getName();
                String newName = showRenameDialog(name, x, y);

                if (newName == null) {
                    return;
                }

                if (name.compareTo(newName) != 0) {
                    data.setTemporaryName(newName);
                    ((DefaultTreeModel) m_tree.getModel()).nodeChanged(n);
                }
                break;
            case BIOLOGICAL_SAMPLE_ANALYSIS :
                XICBiologicalSampleAnalysisNode bioSplAnaysisNode = ((XICBiologicalSampleAnalysisNode) n);
                String qChName = bioSplAnaysisNode.getQuantChannelName();
                String qChNewName = showRenameDialog(qChName, x, y);
                
                if (qChNewName == null) {
                    return;
                }
                
                if (qChName.compareTo(qChNewName) != 0) {
                    bioSplAnaysisNode.setQuantChannelName(qChNewName);
                    ((DefaultTreeModel) m_tree.getModel()).nodeChanged(n);
                }
            default:              
                 break;
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

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        int nbSelectedNodes = selectedNodes.length;

        // we rename multiple nodes
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];
        AbstractNode.NodeTypes nodeType = node.getType();
        if ((nodeType != AbstractNode.NodeTypes.BIOLOGICAL_GROUP)
                && (nodeType != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE)
                && (nodeType != AbstractNode.NodeTypes.DATA_SET)
                && (nodeType != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) ) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }
    
    


}