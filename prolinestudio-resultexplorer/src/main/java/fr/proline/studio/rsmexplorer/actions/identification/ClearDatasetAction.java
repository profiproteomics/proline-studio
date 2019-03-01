package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Remove Identification Summary and potentially Search Result from a dataset
 * @author JM235353
 */
public class ClearDatasetAction extends AbstractRSMAction {
    
    
    public ClearDatasetAction() {
        super(NbBundle.getMessage(ClearDatasetAction.class, "CTL_ClearAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

     @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {
        String msg = NbBundle.getMessage(ClearDatasetAction.class,"ClearDatasetAction.help.text");
        String title = "Clear Dataset";
        int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (n == JOptionPane.YES_OPTION) {
        IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        
        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            final DataSetNode node = (DataSetNode) selectedNodes[i];
            node.setIsChanging(true);
            treeModel.nodeChanged(node);
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }


                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    
                    if (success) {
                        DDataset dataset = node.getDataset();
                        dataset.setResultSummaryId(null);
                        dataset.setResultSummary(null);
                        if (!node.isLeaf()) {
                            dataset.setResultSetId(null);
                            dataset.setResultSet(null);
                        }
                    }
                    node.setIsChanging(false);
                    treeModel.nodeChanged(node);
                }
            };
            
            DDataset dataset = node.getDataset();
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initClearDataset(dataset, !node.isLeaf());
            
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
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
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            DataSetNode datasetNode = (DataSetNode) node;

            if (datasetNode.isLeaf()) {
                if (!datasetNode.hasResultSummary()) {
                    setEnabled(false);
                    return;
                }
                AbstractNode parentNode = (AbstractNode) datasetNode.getParent();
                if (parentNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
                    DataSetNode parentDatasetNode = (DataSetNode) parentNode;
                    if (parentDatasetNode.isChanging() || parentDatasetNode.hasResultSet() || parentDatasetNode.hasResultSummary()) {
                        // parent merged
                        setEnabled(false);
                        return;
                    }
                }
                
            } else {
                if (!datasetNode.hasResultSummary() && ! datasetNode.hasResultSet()) {
                    setEnabled(false);
                    return;
                } else {
                    AbstractNode parentNode = (AbstractNode) datasetNode.getParent();
                    if (parentNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
                        DataSetNode parentDatasetNode = (DataSetNode) parentNode;
                        if (parentDatasetNode.isChanging() || parentDatasetNode.hasResultSet() || parentDatasetNode.hasResultSummary()) {
                            // parent merged
                            setEnabled(false);
                            return;
                        }
                    }
                }
            }
            
            
        }
        setEnabled(true);
    }
    
}
