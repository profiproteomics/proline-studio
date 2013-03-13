package fr.proline.studio.rsmexplorer.actions;


import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ImportIdentificationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ImportIdentificationDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class IdentificationAction extends AbstractRSMAction {

    public IdentificationAction() {
        super(NbBundle.getMessage(IdentificationAction.class, "CTL_IdentificationAction"));
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final RSMNode n = selectedNodes[0];
        
        ImportIdentificationDialog dialog = ImportIdentificationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
            // retrieve parameters
            File[] filePaths = dialog.getFilePaths();
            HashMap<String, String> parserArguments = dialog.getParserArguments();
            
            Project project = null;
            Dataset parentDataset = null;
            if (n.getType() == RSMNode.NodeTypes.PROJECT) {
                RSMProjectNode projectNode = (RSMProjectNode) n;
                project = projectNode.getProject();
            } else if (n.getType() == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode dataSetNode = (RSMDataSetNode) n;
                project = dataSetNode.getDataset().getProject();
                parentDataset = dataSetNode.getDataset();
            } 
            final Project _project = project;
            final Dataset _parentDataset = parentDataset;
                    
            String parserId = dialog.getParserId();
            String decoyRegex = dialog.getDecoyRegex();
            Integer instrumentId = dialog.getInstrumentId();
            Integer peaklistSoftwareId = dialog.getPeaklistSoftwareId();
            
            RSMTree tree = RSMTree.getTree();
            
            // Start identification for each file
            int nbFiles = filePaths.length;
            for (int i=0;i<nbFiles;i++) {
                File f = filePaths[i];

                // Create temporary nodes for the identifications
                String datasetName = f.getName();
                int indexOfDot = datasetName.lastIndexOf('.');
                if (indexOfDot != -1) {
                    datasetName = datasetName.substring(0, indexOfDot);
                }
                final String _datasetName = datasetName;
                DataSetData identificationData = new DataSetData(datasetName, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS );  //JPM.TODO
                
                final RSMDataSetNode identificationNode = new RSMDataSetNode(identificationData);
                identificationNode.setIsChanging(true);
                
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.insertNodeInto(identificationNode, n, n.getChildCount());
                
                // used as out parameter for the service
                final Integer[] _resultSetId = new Integer[1]; 
                
                AbstractServiceCallback callback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {
                        if (success) {
                            
                            createDataset(identificationNode, _project, _parentDataset, _datasetName, _resultSetId[0], getTaskInfo());
                            
                            
                        } else {
                            //JPM.TODO : manage error with errorMessage
                            treeModel.removeNodeFromParent(identificationNode);
                        }
                    }
                };


                ImportIdentificationTask task = new ImportIdentificationTask(callback, parserId, parserArguments, f.getAbsolutePath(), decoyRegex, instrumentId, peaklistSoftwareId, project.getId(), _resultSetId);
                AccessServiceThread.getAccessServiceThread().addTask(task);
                
            }
            
            if (nbFiles>0) {
                // expand the parent node to display its children
                tree.expandNodeIfNeeded(n);
            }
            

        }
    }
    
    private void createDataset(final RSMDataSetNode identificationNode, Project project, Dataset parentDataset, String name, Integer resultSetId, TaskInfo taskInfo) {
                                    

        identificationNode.setIsChanging(false);

        RSMTree tree = RSMTree.getTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(identificationNode);

        final ArrayList<Dataset> createdDatasetList = new ArrayList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {

                if (success) {

                    Dataset dataset = createdDatasetList.get(0);
                    identificationNode.setIsChanging(false);
                    ((DataSetData) identificationNode.getData()).setDataset(dataset);
                    treeModel.nodeChanged(identificationNode);
                } else {
                    // should not happen
                    treeModel.removeNodeFromParent(identificationNode);
                }
            }
        };

        // ask asynchronous loading of data



        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initCreateDatasetForIdentification(project, parentDataset, Aggregation.ChildNature.SAMPLE_ANALYSIS, name, resultSetId, null, createdDatasetList, taskInfo);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        
        // identification must be added in one parent node (for the moment)
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
        RSMNode node = selectedNodes[0];
        
        // parent node is being created, we can not add an identification
        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        // we can always add an identification directly to a project
        if (node.getType() == RSMNode.NodeTypes.PROJECT) {
            setEnabled(true);
            return;
        }
        
        // we can add an identification only to a data set without a ResultSet or a ResultSummary
        if (node.getType() == RSMNode.NodeTypes.DATA_SET) {
            RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
        
            setEnabled(!dataSetNode.hasResultSet() && !dataSetNode.hasResultSummary());
            return;  
        }

        setEnabled(false);

    }
    
    
}