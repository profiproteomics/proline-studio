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
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to empty the Trash
 *
 * @author JM235353
 */
public class EmptyTrashAction extends AbstractRSMAction {


    public EmptyTrashAction(AbstractTree tree) {
        super(NbBundle.getMessage(EmptyTrashAction.class, "CTL_EmptyTrashAction"), tree);
    }

//    @Override
//    public void actionPerformedWOClearRsRSM(AbstractTree selectedTree, AbstractNode[] selectedNodes, int x, int y) {
//
//        // selected node is the Trash
//        final AbstractNode n = selectedNodes[0];
//        DataSetNode datasetNode = (DataSetNode) n;
//        DDataset trashDataset = datasetNode.getDataset();
//        boolean identificationDataset = (selectedTree == IdentificationTree.getCurrentTree());
//        final DefaultTreeModel treeModel = (DefaultTreeModel) selectedTree.getModel();
//
//        n.setIsChanging(true);
//
//        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
//
//            @Override
//            public boolean mustBeCalledInAWT() {
//                return true;
//            }
//
//            @Override
//            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
//                n.setIsChanging(false);
//                if (success) {
//
//                    n.removeAllChildren();
//                    treeModel.nodeStructureChanged(n);
//                } else {
//                    treeModel.nodeChanged(n);
//                }
//            }
//        };
//
//        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
//        task.initEmptyTrash(trashDataset, identificationDataset);
//        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
//
//    }

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
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // check if user also wants to erase locally
        InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Confirm Erasing of Recycle Bin", "Are you sure you want to erase the contents of the Recycle Bin?");
        exitDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
        exitDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
        exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
        exitDialog.setVisible(true);

        if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
            // No clicked
            return;
        }

        // selected node is the Trash
        final AbstractNode n = selectedNodes[0];
        final DDataset trashDataset = ((DataSetNode) n).getDataset();

        final boolean identificationDataset = (getTree() == IdentificationTree.getCurrentTree());
        final DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();

        n.setIsChanging(true);

        Project project = trashDataset.getProject();
        Long projectId = project.getId();
        List<ClearProjectData> listDataToClear = new ArrayList();
        List<ClearProjectData> openedData = ProjectExplorerPanel.getOpenedData(project);

        //probably here nothing is deleted yet(could be wrong - Andreas)
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

                            //I think this is local removing
                            n.removeAllChildren();
                            treeModel.nodeStructureChanged(n);
                            
                            /*
                            // check if user also wants to delete from database
                            InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Do you agree with this action?", "We suggest you also clean the database");
                            exitDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
                            exitDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
                            exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                            exitDialog.setVisible(true);

                            if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
                                // No clicked
                                return;
                            }
                            
                           
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
                            */
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
        clearDsTask.initLoadDataToClearTrash(projectId, listDataToClear, openedData);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(clearDsTask);

    }

}
