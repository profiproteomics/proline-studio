/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.UpdateSpectraParamsTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.UpdatePeaklistSoftDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class UpdatePeaklistSoftwareAction extends AbstractRSMAction {
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
 
    public UpdatePeaklistSoftwareAction(){        
        super(NbBundle.getMessage(UpdatePeaklistSoftwareAction.class, "CTL_UpdatePeaklistSoft"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }
    
    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {
        UpdatePeaklistSoftDialog dialog = UpdatePeaklistSoftDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            Long peaklistSoftID = dialog.getSelectedPeaklistSoftwareId();
            if(peaklistSoftID != null){
                //get Selected Node RS Ids
                int nbrNodes = selectedNodes.length;
                List<Long> rsIds = new ArrayList<>();
                Long projectId = ((DataSetNode) selectedNodes[0]).getDataset().getProject().getId();
                IdentificationTree tree = IdentificationTree.getCurrentTree();
                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                List<DataSetNode> selectedDSNodes = new ArrayList<>();
                
                for(int i =0; i< nbrNodes; i++){
                    DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];                                      
                    selectedDSNodes.add(dataSetNode);
                    rsIds.add(dataSetNode.getResultSetId());
                    dataSetNode.setIsChanging(true);
                    treeModel.nodeChanged(dataSetNode);
                }
                                
                
                //CALL TASK WITH IDS  
                AbstractJMSCallback callback = new AbstractJMSCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        IdentificationTree tree = IdentificationTree.getCurrentTree();
                        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                        
                        for(DataSetNode node : selectedDSNodes){
                            node.setIsChanging(false);                                
                            treeModel.nodeChanged(node);
                        }                  
                    }
                };
                
                UpdateSpectraParamsTask task = new UpdateSpectraParamsTask(callback, projectId, rsIds, peaklistSoftID);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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
            
            DataSetNode dataSetNode = (DataSetNode) node;
            if (! dataSetNode.isLeaf() ||  ! dataSetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }


        }
        setEnabled(true);
    }
    
}
