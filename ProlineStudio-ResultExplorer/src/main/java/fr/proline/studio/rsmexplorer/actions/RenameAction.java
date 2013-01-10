package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.DatabaseProjectTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class RenameAction extends AbstractRSMAction {

    //private static RenameAction instance = null;
    public RenameAction() {
        super(NbBundle.getMessage(RenameAction.class, "CTL_RenameAction"));
    }

    @Override
    public void actionPerformed(RSMNode n, int x, int y) {

        
        String name = null;
        
        RSMNode.NodeTypes nodeType = n.getType();
        if (nodeType == RSMNode.NodeTypes.DATA_SET) {
            final RSMDataSetNode datasetNode = (RSMDataSetNode) n;
            final DataSetTMP dataset = datasetNode.getDataSet();
            name = dataset.getName();

            final String newName = showRenameDialog(name);

            if (newName != null) {
                datasetNode.setIsChanging(true);
                dataset.setName(newName+"...");
                ((DefaultTreeModel)RSMTree.getTree().getModel()).nodeChanged(datasetNode);


                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask) {
                        datasetNode.setIsChanging(false);
                        dataset.setName(newName);
                        ((DefaultTreeModel)RSMTree.getTree().getModel()).nodeChanged(datasetNode);
                    }
                };


                // ask asynchronous loading of data
                DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                task.initRenameDataset(dataset, newName);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }
            
        } else if (nodeType == RSMNode.NodeTypes.PROJECT) {
            final RSMProjectNode projectNode = (RSMProjectNode) n;
            final Project project = projectNode.getProject();
            name = project.getName();

            final String newName = showRenameDialog(name);

            if (newName != null) {
                projectNode.setIsChanging(true);
                project.setName(newName+"...");
                ((DefaultTreeModel)RSMTree.getTree().getModel()).nodeChanged(projectNode);

                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask) {
                        projectNode.setIsChanging(false);
                        project.setName(newName);
                        ((DefaultTreeModel)RSMTree.getTree().getModel()).nodeChanged(projectNode);
                    }
                };


                // ask asynchronous loading of data
                DatabaseProjectTask task = new DatabaseProjectTask(callback);
                task.initRenameProject(project.getId(), newName);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }
        }
        
        
        
        


        
        
        /**
        ValidationDialog dialog = new ValidationDialog(null);
        dialog.setVisible(true);  //JPM.TODO : remove it : not to be put here
        */
        /*
         * DatabaseConnection connection = null;
         * ConnectionManager.getDefault().showConnectionDialog(connection);
         */

    }
        
    private String showRenameDialog(String name) {
        
        
        Component parentComponent = WindowManager.getDefault().findTopComponent("RSMExplorerTopComponent");
        String newName = (String) JOptionPane.showInputDialog(
                            parentComponent,
                            "New Name:",
                            "Rename",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            name);
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