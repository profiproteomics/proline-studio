package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;

/**
 * Action to empty the Trash
 * @author JM235353
 */
public class EmptyTrashAction extends AbstractRSMAction {

    private AbstractTree.TreeType m_treeType;
    
    public EmptyTrashAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(EmptyTrashAction.class, "CTL_EmptyTrashAction"), treeType);
        this.m_treeType = treeType;
    }
    

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // selected node is the Trash
        final AbstractNode n = selectedNodes[0];
        DataSetNode datasetNode = (DataSetNode) n;
        DDataset trashDataset = datasetNode.getDataset();
        
        AbstractTree tree = null;
        boolean identificationDataset = false;
        if (this.m_treeType  == AbstractTree.TreeType.TREE_IDENTIFICATION) {
            tree = IdentificationTree.getCurrentTree();
            identificationDataset = true;
        }else if (this.m_treeType  == AbstractTree.TreeType.TREE_QUANTITATION) {
            tree = QuantitationTree.getCurrentTree();
            identificationDataset = false;
        }
        if (tree == null){
            return;
        }
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        n.setIsChanging(true);


        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }
            
            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                n.setIsChanging(false);
                if (success) {
                    
                    n.removeAllChildren();
                    treeModel.nodeStructureChanged(n);
                    
                    
                } else {
                    treeModel.nodeChanged(n);
                }
            }
        };

        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initEmptyTrash(trashDataset, identificationDataset);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        

    }

    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }
        
        AbstractNode n = selectedNodes[0];  // only one node can be selected

        setEnabled(!n.isChanging() && (!n.isLeaf()));

    }
    
}
