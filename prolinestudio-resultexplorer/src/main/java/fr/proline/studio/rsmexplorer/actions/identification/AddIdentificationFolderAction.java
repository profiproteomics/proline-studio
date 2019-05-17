package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.AddAggregateDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.AddFolderDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class AddIdentificationFolderAction extends AbstractRSMAction {

    
    public AddIdentificationFolderAction(AbstractTree tree) {
        super("Add Identification Folder", tree);
    }

    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        AddFolderDialog dialog = AddFolderDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == AddAggregateDialog.BUTTON_OK) {
            
            // retrieve parameters
            String name = dialog.getFolderName();
            final AbstractNode n = selectedNodes[0];
            
            boolean isParentAProject = (n.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION);

            final ArrayList<DataSetNode> nodesCreated = new ArrayList<>();

            final IdentificationTree tree = IdentificationTree.getCurrentTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            DataSetNode datasetNode = new DataSetNode(DataSetData.createTemporaryFolder(name));//new DataSetData(name, Dataset.DatasetType.IDENTIFICATION_FOLDER, Aggregation.ChildNature.OTHER));
            nodesCreated.add(datasetNode);
            datasetNode.setIsChanging(true);

            if (isParentAProject) {
                n.insert(datasetNode, n.getChildCount() - 1);
            } else {
                n.add(datasetNode);
            }

            treeModel.nodeStructureChanged(n);

            // expand the parent node to display its children
            tree.expandNodeIfNeeded(n);

            // add to really create the aggregate dataset
            Project project = null;
            DDataset parentDataset = null;
            DataSetNode parentDatasetNode = null;
            if (n.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                project = ((IdProjectIdentificationNode) n).getProject();
                parentDataset = null;
            } else if (n.getType() == AbstractNode.NodeTypes.DATA_SET) {
                parentDatasetNode = ((DataSetNode) n);
                parentDataset = parentDatasetNode.getDataset();
                project = parentDataset.getProject();
            }

            final ArrayList<DDataset> createdDatasetList = new ArrayList<>();

            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    if (!success) {
                        return; // should not happen
                    }

                    int nbNodes = nodesCreated.size();
                    for (int i = 0; i < nbNodes; i++) {

                        DataSetNode datasetNode = nodesCreated.get(i);
                        DDataset dataset = createdDatasetList.get(i);
                        datasetNode.setIsChanging(false);
                        ((DataSetData) datasetNode.getData()).setDataset(dataset);
                        treeModel.nodeChanged(datasetNode);
                    }

                }
            };

            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initCreateDatasetFolder(project, parentDataset, name, true, createdDatasetList);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        }
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
        
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];

        // parent node is being created, we can not add an identification
        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        boolean enabled = false;

        // we can always add an aggregate directly to a project
        if (node.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
            enabled = true;
        } else if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {

            DDataset d = ((DataSetNode) node).getDataset();
            if (d.isIdentification() && d.isFolder()) {
                enabled = true;
            }

        }


        setEnabled(enabled);
 
    }
}