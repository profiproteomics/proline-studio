/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ComputeSCTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.TreeSelectionDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class CompareWithSCAction extends AbstractRSMAction {
 
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public CompareWithSCAction() {
        super(NbBundle.getMessage(CompareWithSCAction.class, "CTL_CompareWithSCAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
     
        // Onlt Ref should be selected to compute SC         
        final RSMDataSetNode datasetNode = (RSMDataSetNode) selectedNodes[0];
        
        //Create Child Tree to select RSM to compute SC for
        final RSMTree childTree = RSMTree.getTree().copyDataSetRootSubTree(datasetNode.getDataset(),  datasetNode.getDataset().getProject().getId());
        final TreeSelectionDialog treeSelectionDialog = new TreeSelectionDialog(WindowManager.getDefault().getMainWindow(), childTree, "Select Result Summaries for Spectral Count");
        treeSelectionDialog.setLocation(x, y);
        treeSelectionDialog.setBusy(true);
        treeSelectionDialog.setVisible(true);
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                // check if we can compute SC
                String error = null;
                ArrayList<Long> resultSummaryIdList = new ArrayList<>();                
                treeSelectionDialog.setBusy(false);
                
                if (treeSelectionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                   ArrayList<RSMDataSetNode> selectedDSNodes = treeSelectionDialog.getSelectedRSMDSNodeList();
                   for(RSMDataSetNode dsNode : selectedDSNodes){
                        if (!dsNode.hasResultSummary()) {
                            error = " Spectral Count is not possible on Search result ("+ dsNode.getDataset().getName() +").  Identification Summary should be created first";
                            break;                       
                        }
                        if(dsNode.isChanging()){
                            error = " Spectral Count is not possible while import or validation is on going. (Search result "+ dsNode.getDataset().getName() +")";
                            break;  
                        }
                        resultSummaryIdList.add(dsNode.getResultSummaryId());
                   }
                } else {
                    return;                                                
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(RSMTree.getTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                m_logger.debug(" Will Compute SC on "+resultSummaryIdList.size()+" RSMs. Ids: "+resultSummaryIdList);
                askComputeSCService(datasetNode,resultSummaryIdList);
            }
        };
               
        

        RSMTree.getTree().loadInBackground(datasetNode, callback);
    
    }
    
     private void askComputeSCService(final RSMDataSetNode refNode, final List<Long> rsmIdList) {

        final Dataset dataset = refNode.getDataset();
        m_logger.debug(" -------- Run Compute SC service for  "+refNode.getDataset().getName());
        refNode.setIsChanging(true);
        RSMTree tree = RSMTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(refNode);

        // used as out parameter for the service
        final String[] _spCountJSON = new String[1];
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {
            protected final Logger m_log2 = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    m_log2.debug(" Service Success. Result = ------------  ");
                    m_log2.debug(_spCountJSON[0]);
                    m_log2.debug(" Service Success. END Result = ------------  ");

                } else {
                    //JPM.TODO : manage error with errorMessage
                    refNode.setIsChanging(false);
                    m_log2.debug(" Service ERROR ");
                    treeModel.nodeChanged(refNode);
                }
            }
        };

        ComputeSCTask task = new ComputeSCTask(callback,  dataset, rsmIdList, _spCountJSON);

        AccessServiceThread.getAccessServiceThread().addTask(task);
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        
         if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        RSMNode node = (RSMNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        RSMDataSetNode datasetNode = (RSMDataSetNode) node;
        
        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
        if (datasetType != Dataset.DatasetType.AGGREGATE) {
            setEnabled(false);
            return;
        }

        if (!datasetNode.hasResultSummary()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }

}
