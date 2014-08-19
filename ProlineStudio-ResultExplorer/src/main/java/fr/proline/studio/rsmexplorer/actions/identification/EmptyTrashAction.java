package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;

/**
 * Action to empty the Trash
 * @author JM235353
 */
public class EmptyTrashAction extends AbstractRSMAction {

    public EmptyTrashAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_EmptyTrashAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }
    

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // selected node is the Trash
        final AbstractNode n = selectedNodes[0];
        DataSetNode datasetNode = (DataSetNode) n;
        DDataset trashDataset = datasetNode.getDataset();
        
        IdentificationTree tree = IdentificationTree.getCurrentTree();
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
        task.initEmptyTrash(trashDataset);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        

    }

    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        AbstractNode n = selectedNodes[0];  // only one node can be selected

        setEnabled(!n.isChanging() && (!n.isLeaf()));

    }
    
}
