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
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
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
    protected TreeSelectionDialog treeSelectionDialog;
  
    public CompareWithSCAction() {
        super(NbBundle.getMessage(CompareWithSCAction.class, "CTL_CompareWithSCAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
     
        // Onlt Ref should be selected to compute SC         
        final RSMDataSetNode refDatasetNode = (RSMDataSetNode) selectedNodes[0];

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.debug(" Choose RSM for Weighted SC");
//                treeSelectionDialog.setBusy(false);

                // check if we can compute SC
                String error = null;
                ArrayList<Dataset> datasetsComputeRsmSCList = new ArrayList<>();                
                
                if (treeSelectionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                   ArrayList<RSMDataSetNode> selectedDSNodes = treeSelectionDialog.getSelectedRSMDSNodeList();
                   //A Voir :) 
//                   if(selectedDSNodes.contains(refDatasetNode) && selectedDSNodes.size()>1) 
//                        error = " Spectral Count is not possible on reference Node and some of its child";
                       
                   for(RSMDataSetNode dsNode : selectedDSNodes){
                       //TODO : Verif pas père + fils sélectionnés : Que père ou que fils ?!? 
                        if (!dsNode.hasResultSummary()) {
                            error = " Spectral Count is not possible on Search result ("+ dsNode.getDataset().getName() +").  Identification Summary should be created first";
                            break;                       
                        }
                        if(dsNode.isChanging()){
                            error = " Spectral Count is not possible while import or validation is on going. (Search result "+ dsNode.getDataset().getName() +")";
                            break;  
                        }
                        datasetsComputeRsmSCList.add(dsNode.getDataset());
                   }
                } else { //Cancel / Close was clicked
                    return;                                                
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(RSMTree.getTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                m_logger.debug(" Will Compute SC on "+datasetsComputeRsmSCList.size()+" RSMs : "+datasetsComputeRsmSCList);
                askComputeSCService(refDatasetNode,  datasetsComputeRsmSCList);
            }
        };
        
            //Create Child Tree to select RSM to compute SC for
        final RSMTree childTree = RSMTree.getTree().copyDataSetRootSubTree(refDatasetNode.getDataset(),  refDatasetNode.getDataset().getProject().getId());
        treeSelectionDialog = new TreeSelectionDialog(WindowManager.getDefault().getMainWindow(), childTree, "Select Result Summaries for Spectral Count");
        treeSelectionDialog.setLocation(x, y);   
//        treeSelectionDialog.setBusy(true);  
        treeSelectionDialog.setVisible(true);  
        
        RSMTree.getTree().loadInBackground(refDatasetNode, callback);

    
    }
    
     private void askComputeSCService(final RSMDataSetNode refNode, final List<Dataset> dsRsmList) {

        final Dataset dataset = refNode.getDataset();
        m_logger.debug(" -------- Run Compute SC service for  "+refNode.getDataset().getName());
        refNode.setIsChanging(true);
        RSMTree tree = RSMTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(refNode);

        // used as out parameter for the service
        final String[] _spCountJSON = new String[1];
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    openWSCPanel(refNode, dsRsmList, _spCountJSON[0], treeModel);                   
                } else {    
                    //JPM.TODO : manage error with errorMessage
                    refNode.setIsChanging(false);
                    treeModel.nodeChanged(refNode);
                }
            }
        };
                      
        List<Long> rsmIds = new ArrayList<>(dsRsmList.size());
        for(Dataset ds :dsRsmList ){
            rsmIds.add(ds.getResultSummaryId());
        }        
                
        ComputeSCTask task = new ComputeSCTask(callback,  dataset, rsmIds, _spCountJSON);

        AccessServiceThread.getAccessServiceThread().addTask(task);
    }

     private void openWSCPanel(final RSMDataSetNode datasetNode,  final List<Dataset> dsRsmList, String scResultAsJson,final DefaultTreeModel treeModel){
      
        m_logger.debug(" Service Success. Result = ------------  ");
        m_logger.debug(scResultAsJson);
        m_logger.debug(" Service Success. END Result = ------------  ");

        ComputeSCTask.WSCResultData scResult = new ComputeSCTask.WSCResultData(datasetNode.getDataset(), dsRsmList, scResultAsJson);
        WindowBox wbox = WindowBoxFactory.getRsmWSCWindowBox(datasetNode.getDataset().getName()+" WSC", false) ;
        wbox.setEntryData(datasetNode.getDataset().getProject().getId(), scResult);

         // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();            

        datasetNode.setIsChanging(false);
        treeModel.nodeChanged(datasetNode);
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
