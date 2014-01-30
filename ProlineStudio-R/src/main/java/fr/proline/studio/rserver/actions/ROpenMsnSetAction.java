package fr.proline.studio.rserver.actions;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rserver.command.RVar;
import fr.proline.studio.rserver.data.MsnSetData;
import fr.proline.studio.rserver.dialog.OpenMsnSetDialog;
import fr.proline.studio.rserver.node.RMsnSetNode;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import java.io.File;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class ROpenMsnSetAction extends AbstractRAction {

    public ROpenMsnSetAction() {
        super("Open Msn Set...");
    }

    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        OpenMsnSetDialog dialog = OpenMsnSetDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            

            // retrieve files to import
            String filePath = dialog.getFilePath();
            String nodeName = new File(filePath).getName();
            
            
            // create the Msn Set Node
            final RNode parentNode = selectedNodes[0];
            RTree tree = RTree.getTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            MsnSetData data = new MsnSetData(nodeName);
            final RMsnSetNode msnSetNode = new RMsnSetNode(data, true);
            
            msnSetNode.setIsChanging(true);
            treeModel.insertNodeInto(msnSetNode, parentNode, parentNode.getChildCount());
            // expand parent node if needed
            final TreePath pathToExpand = new TreePath(parentNode.getPath());
            if (!tree.isExpanded(pathToExpand)) {
                tree.expandPath(pathToExpand);
            }

            

            AbstractServiceCallback callback = new AbstractServiceCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {
                        msnSetNode.setIsChanging(false);
                        treeModel.nodeChanged(msnSetNode);
                    } else {
                        treeModel.removeNodeFromParent(msnSetNode);
                    }
                }
                
            };
            
            OpenMsnSetTask task = new OpenMsnSetTask(callback, filePath, data);
            AccessServiceThread.getAccessServiceThread().addTask(task); 

        }
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        setEnabled(RServerManager.getRServerManager().isConnected());
    }
    
    
       
    public class OpenMsnSetTask extends AbstractServiceTask {

        private String m_filePath;
        private MsnSetData m_msnSetData;

        public OpenMsnSetTask(AbstractServiceCallback callback, String filePath, MsnSetData msnSetData) {
            super(callback, true /** synchronous */, new TaskInfo("Open MsnSet " + filePath, false, TASK_LIST_INFO));

            m_filePath = filePath;
            m_msnSetData = msnSetData;
        }

        @Override
        public boolean askService() {

            String timestamp = String.valueOf(System.currentTimeMillis());
            String msnSetOnServer = "msnSet"+timestamp+".txt";
            
            RServerManager serverR = RServerManager.getRServerManager();

            try {
                serverR.uploadFile(m_filePath, msnSetOnServer);

                String var = serverR.getNewVariableName("msnSet");
                m_msnSetData.setVar(new RVar(var, RVar.MSN_SET));


                String codeToCreateMsnSet = var+" <- readRDS('" + msnSetOnServer + "')";
                serverR.parseAndEval(codeToCreateMsnSet);

                
                
            } catch (RServerManager.RServerException ex) {
                m_taskError = new TaskError(ex);
                return false;
            } finally {
                // remove files
                try {
                    serverR.deleteFile(msnSetOnServer);
                } catch (Exception ex) {
                }


            }


            return true;
        }

        @Override
        public AbstractServiceTask.ServiceState getServiceState() {
            // always returns STATE_DONE because it is a synchronous service
            return AbstractServiceTask.ServiceState.STATE_DONE;
        }
    }
    
}
