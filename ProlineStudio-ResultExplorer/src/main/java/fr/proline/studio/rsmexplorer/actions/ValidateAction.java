/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ValidationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ValidationDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author jm235353
 */
public class ValidateAction extends AbstractRSMAction {

    public ValidateAction() {
        super(NbBundle.getMessage(ValidateAction.class, "CTL_ValidateAction"));
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 

        int nbNodes = selectedNodes.length;
        ArrayList<Dataset> datasetList = new ArrayList<>(nbNodes);
        for (int i=0; i<nbNodes; i++) {
            RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[i];
            Dataset d = dataSetNode.getDataset();
            datasetList.add(d);
        }
        
        
        
        
        ValidationDialog dialog = ValidationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setDatasetList(datasetList);
        dialog.setVisible(true);

        
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
            // retrieve parameters
            HashMap<String, String> parserArguments = dialog.getArguments();


            RSMTree tree = RSMTree.getTree();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            
            // start validation for each selected Dataset
            for (int i=0;i<nbNodes;i++) {
                final RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[i];

                dataSetNode.setIsChanging(true);
                treeModel.nodeChanged(dataSetNode);
                
                
                final Dataset d = dataSetNode.getDataset();
                
                // used as out parameter for the service
                final Integer[] _resultSummaryId = new Integer[1]; 
                
                AbstractServiceCallback callback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {
                             
                            updateDataset(dataSetNode, d, _resultSummaryId[0], getTaskInfo());
                            
                            
                        } else {
                            //JPM.TODO : manage error with errorMessage
                            dataSetNode.setIsChanging(false);

                            treeModel.nodeChanged(dataSetNode);
                        }
                    }
                };


                ValidationTask task = new ValidationTask(callback, dataSetNode.getDataset(), "", parserArguments, _resultSummaryId);
                AccessServiceThread.getAccessServiceThread().addTask(task);
                

                
            }
            

        }
    }
    
    private void updateDataset(final RSMDataSetNode datasetNode, final Dataset d, Integer resultSummaryId, TaskInfo taskInfo) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                datasetNode.setIsChanging(false);

                RSMTree tree = RSMTree.getTree();
                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.nodeChanged(datasetNode);
            }
        };

        // ask asynchronous loading of data



        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initModifyDatasetForValidation(d, resultSummaryId, taskInfo);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        // note : we can ask for the validation of multiple ResultSet in one time
        
        int nbSelectedNodes = selectedNodes.length;
        for (int i=0;i<nbSelectedNodes;i++) {
            RSMNode node = selectedNodes[i];
            
            // parent node is being created, we can not validate it (for the moment)
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }
            
            // parent node must be a dataset
            if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }
            
            // parent node must have a ResultSet and no ResultSummary (for the moment ?)
            RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
            if (!dataSetNode.hasResultSet() || dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);

    }
    
    
}