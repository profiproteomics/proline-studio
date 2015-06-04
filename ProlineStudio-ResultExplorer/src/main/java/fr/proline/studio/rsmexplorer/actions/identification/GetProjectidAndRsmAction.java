package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ChangeTypicalProteinTask;
import fr.proline.studio.dpm.task.jms.SendProjectidAndRsmTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ChangeTypicalProteinDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.dpm.task.jms.SendProjectidAndRsmTask;
import fr.proline.studio.dpm.task.SendProjectidAndRsmTasks;

import javax.swing.tree.DefaultTreeModel;

import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import java.util.List;


public class GetProjectidAndRsmAction extends AbstractRSMAction {

    
    public GetProjectidAndRsmAction() {
        super(NbBundle.getMessage(GetProjectidAndRsmAction.class, "CTL_GenerateSequenceCoverage"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
 public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

       
            IdentificationTree tree = IdentificationTree.getCurrentTree();
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            
            int nbNodes = selectedNodes.length;
            for (int i = 0; i < nbNodes; i++) {
                final DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

                dataSetNode.setIsChanging(true);
                treeModel.nodeChanged(dataSetNode);

                final DDataset d = dataSetNode.getDataset();
                AbstractServiceCallback callback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {

                       
                        ResultSummary rsm = d.getResultSummary();
                        if (rsm != null) {
                            rsm.getTransientData().setProteinSetArray(null);
                        }
                        
                        dataSetNode.setIsChanging(false);

                        IdentificationTree tree = IdentificationTree.getCurrentTree();
                        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                        treeModel.nodeChanged(dataSetNode);
                    }
                };
                SendProjectidAndRsmTasks task = new SendProjectidAndRsmTasks(callback,d);
                AccessServiceThread.getAccessServiceThread().addTask(task);
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
        
        // note : we can ask for the validation of multiple ResultSet in one time

        int nbSelectedNodes = selectedNodes.length;
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            // parent node is being created, we can not validate it (for the moment)
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            // parent node must be a dataset
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

         
            DataSetNode dataSetNode = (DataSetNode) node;
            if (!dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
        }
        setEnabled(true);

    }
    
}
