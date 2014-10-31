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
import fr.proline.studio.rserver.dialog.ImportMsnSetDialog;
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
public class RImportMsnSetAction extends AbstractRAction {

    public RImportMsnSetAction() {
        super("Create Msn Set...");
    }

    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        ImportMsnSetDialog dialog = ImportMsnSetDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            

            // retrieve files to import
            String designFilePath = dialog.getDesignFilePath();
            String dataFilePath = dialog.getDataFilePath();
            String nodeName = new File(designFilePath).getName();
            
            
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
            
            ImportMsnSetTask task = new ImportMsnSetTask(callback, designFilePath, dataFilePath, data);
            AccessServiceThread.getAccessServiceThread().addTask(task);  //JPM.TODO

        }
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        setEnabled(RServerManager.getRServerManager().isConnected());
    }
    
    
       
    public class ImportMsnSetTask extends AbstractServiceTask {

        private String m_designFilePath;
        private String m_dataFilePath;
        private MsnSetData m_msnSetData;

        public ImportMsnSetTask(AbstractServiceCallback callback, String designFilePath, String dataFilePath, MsnSetData msnSetData) {
            super(callback, true /** synchronous */, new TaskInfo("Import MsnSet " + designFilePath, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

            m_designFilePath = designFilePath;
            m_dataFilePath = dataFilePath;
            m_msnSetData = msnSetData;
        }

        @Override
        public boolean askService() {

            String timestamp = String.valueOf(System.currentTimeMillis());
            String designOnServer = "msnSetDesign"+timestamp+".txt";
            String dataOnServer = "msnSetData"+timestamp+".txt";
            
            RServerManager serverR = RServerManager.getRServerManager();

            try {
                serverR.uploadFile(m_designFilePath, designOnServer);
                serverR.uploadFile(m_dataFilePath, dataOnServer);

                String var = serverR.getNewVariableName("msnSet");
                m_msnSetData.setVar(new RVar(var, RVar.MSN_SET));
                

                String codeToCreateMsnSet = var+" <- Quant2MSnset('" + dataOnServer + "','" + designOnServer + "',NULL,c(3:8),c(9:20),1)";
                serverR.parseAndEval(codeToCreateMsnSet);
                
            } catch (RServerManager.RServerException ex) {
                m_taskError = new TaskError(ex);
                return false;
            } finally {
                // remove files
                try {
                    serverR.deleteFile(designOnServer);
                } catch (Exception ex) {
                }

                try {
                    serverR.deleteFile(dataOnServer);
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
