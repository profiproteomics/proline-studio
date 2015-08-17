package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;

import javax.swing.tree.DefaultTreeModel;

import org.openide.util.NbBundle;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.SendProjectidAndRsmTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.utils.StringUtils;



public class GetProjectidAndRsmJMSAction extends AbstractRSMAction {

    public GetProjectidAndRsmJMSAction() {
        super(StringUtils.getActionName(NbBundle.getMessage(GetProjectidAndRsmJMSAction.class, "CTL_GenerateSequenceCoverage"), true), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
    	  
    	    IdentificationTree tree = IdentificationTree.getCurrentTree();
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            // start validation for each selected Dataset
            int nbNodes = selectedNodes.length;
            for (int i = 0; i < nbNodes; i++) {
                final DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];
                dataSetNode.setIsChanging(true);
                treeModel.nodeChanged(dataSetNode);
                final DDataset d = dataSetNode.getDataset();
                AbstractJMSCallback callback = new AbstractJMSCallback() {

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
                SendProjectidAndRsmTask task = new SendProjectidAndRsmTask(callback,d);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
            }
           
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {


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
          
            DataSetNode dataSetNode = (DataSetNode) node;
            if (!dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
        }
        setEnabled(true);
    }
    
}
