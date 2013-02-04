package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.CreateProjectTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.AddProjectDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * To Add a Project in the UDS DB
 * @author jm235353
 */
public class AddProjectAction  extends AbstractRSMAction {

    public AddProjectAction() {
        super(NbBundle.getMessage(AggregateAction.class, "CTL_AddProjectAction"));
    }
    
    @Override
    public void actionPerformed(final RSMNode[] selectedNodes, int x, int y) {
        
        // only one node selected for this action
        final RSMNode n = selectedNodes[0];

        AddProjectDialog dialog = AddProjectDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
            // data needed to create the project
            String projectName = dialog.getProjectName();
            String projectDescription = dialog.getProjectDescription();
            UserAccount owner = UDSDataManager.getUDSDataManager().getProjectUser();
            
            // look where to put the node (alphabetical order)
            int insertionIndex = 0;
            int nbChildren = n.getChildCount();
            for (int i=0;i<nbChildren;i++) {
                RSMNode child = (RSMNode) n.getChildAt(i);
                if (child.getType() == RSMNode.NodeTypes.PROJECT) {
                    String childProjectName = ((RSMProjectNode) child).getData().getName();
                    if (projectName.compareToIgnoreCase(childProjectName) >= 0) {
                        insertionIndex = i+1;
                    } else {
                        break;
                    }
                }
            }
            
            // Create a temporary node in the Project List
            ProjectData projectData = new ProjectData(projectName);
            final RSMProjectNode projectNode = new RSMProjectNode(projectData);
            projectNode.setIsChanging(true);
            RSMTree tree = RSMTree.getTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            treeModel.insertNodeInto(projectNode, n, insertionIndex);
            
            AbstractServiceCallback callback = new AbstractServiceCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {
                        projectNode.setIsChanging(false);
                        treeModel.nodeChanged(projectNode);
                    } else {
                        //JPM.TODO : manage error with errorMessage
                        treeModel.removeNodeFromParent(projectNode);
                    }
                }
                
            };
            
            
            CreateProjectTask task = new CreateProjectTask(callback, projectName, projectDescription, owner.getId(), projectData);
            AccessServiceThread.getAccessServiceThread().addTask(task);
            
        }
        

    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        boolean b = ServerConnectionManager.getServerConnectionManager().isConnectionDone();
        setEnabled(b);
    }
    
}
