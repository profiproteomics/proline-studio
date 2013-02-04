/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ValidationDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
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

        
        ValidationDialog dialog = ValidationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
            // retrieve parameters
            /*String description = dialog.getDescription();
            int peptideFDR = dialog.getPeptideFDR();
            int peptideMinPepSequence = dialog.getPeptideMinPepSequence();
            int proteinFDR = dialog.getProteinFDR();
            int proteinMinPepSequence = dialog.getProteinMinPepSequence();*/

            // start validation for each selected Dataset
            int nbNodes = selectedNodes.length;
            for (int i=0;i<nbNodes;i++) {
                RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[i];

                /*
                // Create temporary nodes for the validation
                String datasetName = f.getName();
                int indexOfDot = datasetName.lastIndexOf('.');
                if (indexOfDot != -1) {
                    datasetName = datasetName.substring(0, indexOfDot);
                }
                final String _datasetName = datasetName;
                DataSetData identificationData = new DataSetData(datasetName, DataSetTMP.SAMPLE_ANALYSIS );  //JPM.TODO
                
                final RSMDataSetNode identificationNode = new RSMDataSetNode(identificationData);
                identificationNode.setIsChanging(true);
                RSMTree tree = RSMTree.getTree();
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.insertNodeInto(identificationNode, n, n.getChildCount());
                
                AbstractServiceCallback callback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {
                            
                            Integer resultSetId = null; //JPM.TODO !!!! : it must be a result of the service
                            
                            createDataset(identificationNode, _projectId, _parentDatasetId, _datasetName, resultSetId);
                            
                            
                        } else {
                            //JPM.TODO : manage error with errorMessage
                            treeModel.removeNodeFromParent(identificationNode);
                        }
                    }
                };


                ImportIdentificationTask task = new ImportIdentificationTask(callback, parserArguments, f.getAbsolutePath(), instrumentId, peaklistSoftwareId, projectId);
                AccessServiceThread.getAccessServiceThread().addTask(task);*/
                
            }
            

        }
    }
    
    /*private void createDataset(final RSMDataSetNode identificationNode, Integer projectId, Integer parentDatasetId, String name, Integer resultSetId) {
                                    

        identificationNode.setIsChanging(false);

        RSMTree tree = RSMTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(identificationNode);

        final ArrayList<DataSetTMP> createdDatasetList = new ArrayList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {

                if (success) {

                    DataSetTMP dataset = createdDatasetList.get(0);
                    identificationNode.setIsChanging(false);
                    ((DataSetData) identificationNode.getData()).setDataSet(dataset);
                    treeModel.nodeChanged(identificationNode);
                } else {
                    // should not happen
                    treeModel.removeNodeFromParent(identificationNode);
                }
            }
        };

        // ask asynchronous loading of data



        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initCreateDatasetForIdentification(projectId, parentDatasetId, DataSetTMP.SAMPLE_ANALYSIS, name, resultSetId, null, createdDatasetList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }*/
    

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