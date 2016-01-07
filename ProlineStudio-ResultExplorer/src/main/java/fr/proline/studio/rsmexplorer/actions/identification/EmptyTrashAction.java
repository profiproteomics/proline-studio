package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.ClearProjectData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseClearProjectTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.ClearProjectTask;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ClearProjectDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

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
  
    
    //TODO: replace current actionPerformed with this method to connect the clear rs/rsm data on the empty trash action
    //@Override
    public void actionPerformedWithClearRsRsm(AbstractNode[] selectedNodes, int x, int y) {

        // selected node is the Trash
        final AbstractNode n = selectedNodes[0];
        DataSetNode datasetNode = (DataSetNode) n;
        final DDataset trashDataset = datasetNode.getDataset();

        AbstractTree tree = null;
        boolean identDs = false;
        if (this.m_treeType == AbstractTree.TreeType.TREE_IDENTIFICATION) {
            tree = IdentificationTree.getCurrentTree();
            identDs = true;
        } else if (this.m_treeType == AbstractTree.TreeType.TREE_QUANTITATION) {
            tree = QuantitationTree.getCurrentTree();
            identDs = false;
        }
        final boolean identificationDataset = identDs;
        if (tree == null) {
            return;
        }
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

        n.setIsChanging(true);

        Long projectId = trashDataset.getProject().getId();
        List<ClearProjectData> listDataToClear = new ArrayList();
        List<Long> datasetIds = new ArrayList();
        List<ClearProjectData> openedData = ProjectExplorerPanel.getOpenedData(projectId);

        AbstractDatabaseCallback callbackLoadTrash = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                AbstractDatabaseCallback loadClearCallback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
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
                                    
                                    ClearProjectDialog clearProjectDialog = new ClearProjectDialog(WindowManager.getDefault().getMainWindow(), trashDataset.getProject(), listDataToClear);
                                    clearProjectDialog.setLocation(x, y);
                                    DefaultDialog.ProgressTask task = new DefaultDialog.ProgressTask() {
                                        @Override
                                        public int getMinValue() {
                                            return 0;
                                        }

                                        @Override
                                        public int getMaxValue() {
                                            return 100;
                                        }

                                        @Override
                                        protected Object doInBackground() throws Exception {
                                            if ((clearProjectDialog.canModifyValues())) {
                                                List<ClearProjectData> dataToClear = clearProjectDialog.getSelectedData();

                                                List<Long> rsmIds = new ArrayList();
                                                List<Long> rsIds = new ArrayList();
                                                dataToClear.stream().forEach((d) -> {
                                                    if (d.isResultSet()) {
                                                        rsIds.add(d.getResultSet().getId());
                                                    } else if (d.isResultSummary()) {
                                                        rsmIds.add(d.getResultSummary().getId());
                                                    }
                                                });

                                                boolean isJMSDefined = JMSConnectionManager.getJMSConnectionManager().isJMSDefined();
                                                if (isJMSDefined) {
                                                    AbstractJMSCallback clearCallBack = new AbstractJMSCallback() {

                                                        @Override
                                                        public boolean mustBeCalledInAWT() {
                                                            return true;
                                                        }

                                                        @Override
                                                        public void run(boolean success) {
                                                            setProgress(100);
                                                        }

                                                    };
                                                    // clear db on server's side
                                                    ClearProjectTask clearTaskDb = new ClearProjectTask(clearCallBack, projectId, rsmIds, rsIds);
                                                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(clearTaskDb);
                                                } else {
                                                    setProgress(100);
                                                }
                                            }
                                            return null;
                                        }
                                    };
                                    clearProjectDialog.setTask(task);
                                    clearProjectDialog.setVisible(true);
                        
                                } else {
                                    treeModel.nodeChanged(n);
                                }
                                
                                
                            }
                        };
                        // remove dataset
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initEmptyTrash(trashDataset, identificationDataset);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                                

                        
                    }
                };
                // load clear data
                DatabaseClearProjectTask clearDsTask = new DatabaseClearProjectTask(loadClearCallback);
                clearDsTask.initLoadDataToClearTrash(projectId, datasetIds, listDataToClear, openedData);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(clearDsTask);

            }
        };

        // load ds and all rsm/rs which are in the trash
        DatabaseClearProjectTask taskLoadTrash = new DatabaseClearProjectTask(callbackLoadTrash);
        taskLoadTrash.initLoadDataInTrash(projectId, trashDataset.getId(), datasetIds);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(taskLoadTrash);

    }
    
}
