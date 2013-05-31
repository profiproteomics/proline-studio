/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class EmptyTrashAction extends AbstractRSMAction {

    public EmptyTrashAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_EmptyTrashAction"));
    }
    

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        // selected node is the Trash
        final RSMNode n = selectedNodes[0];
        RSMDataSetNode datasetNode = (RSMDataSetNode) n;
        Dataset trashDataset = datasetNode.getDataset();
        
        RSMTree tree = RSMTree.getTree();
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
    public void updateEnabled(RSMNode[] selectedNodes) {

        RSMNode n = selectedNodes[0];  // only one node can be selected

        setEnabled(!n.isChanging() && (!n.isLeaf()));

    }
    
}
