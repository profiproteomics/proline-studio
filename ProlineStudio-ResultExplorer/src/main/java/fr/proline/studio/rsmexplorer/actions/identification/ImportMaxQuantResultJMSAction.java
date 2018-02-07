/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.FileUploadTask;
import fr.proline.studio.dpm.task.jms.ImportMaxQuantTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ImportMaxQuantResultDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author VD225637
 */
public class ImportMaxQuantResultJMSAction extends AbstractRSMAction {

    public ImportMaxQuantResultJMSAction() {
        super(NbBundle.getMessage(ImportMaxQuantResultJMSAction.class, "CTL_ImportMaxQuantResult"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final AbstractNode n = selectedNodes[0];
        
        ImportMaxQuantResultDialog dialog = ImportMaxQuantResultDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            // retrieve parameters
            File[] filePaths = dialog.getFilePaths();
            final long instrumentId = dialog.getInstrumentId();
            final String accessionRegexp = dialog.getAccessionRegexp();
            final Boolean importQuantitation = dialog.getImportQuantitation();

            Project project = null;
            DDataset parentDataset = null;
            boolean isParentAProject = false;
            if (n.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
                IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) n;
                project = projectNode.getProject();
                isParentAProject = true;
            } else if (n.getType() == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dataSetNode = (DataSetNode) n;
                project = dataSetNode.getDataset().getProject();
                parentDataset = dataSetNode.getDataset();
            }

            final ArrayList<DataSetNode> allIdentificationNodes = new ArrayList<>();
            final ArrayList<String> allDatasetNames = new ArrayList<>();
            final DefaultTreeModel treeModel = (DefaultTreeModel) IdentificationTree.getCurrentTree().getModel();

            // Create dataset nodes for parent folder
            final int nbFiles = filePaths.length;
            for (int i=0;i<nbFiles;i++) {
                File f = filePaths[i];
                // Create temporary nodes for the identifications
                String datasetName = f.getName();
                int indexOfDot = datasetName.lastIndexOf('.');
                if (indexOfDot != -1) {
                    datasetName = datasetName.substring(0, indexOfDot);
                }
                allDatasetNames.add(datasetName);

                DataSetData identificationData = new DataSetData(datasetName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.BIOLOGICAL_GROUP);  //JPM.TODO                
                final DataSetNode identificationNode = new DataSetNode(identificationData);
                identificationNode.setIsChanging(true);
                allIdentificationNodes.add(identificationNode);

                if (isParentAProject) {
                    treeModel.insertNodeInto(identificationNode, n, n.getChildCount() - 1);
                } else {
                    treeModel.insertNodeInto(identificationNode, n, n.getChildCount());
                }
            }

            // Import MaxQuant files             
            for (int i = 0; i < nbFiles; i++) {
                File f = filePaths[i]; 
                uploadAndImport(f.getPath(), project, allIdentificationNodes.get(i),parentDataset,allDatasetNames.get(i),treeModel,instrumentId,accessionRegexp, importQuantitation);
            }
        }
    }

    private void uploadAndImport(final String filePath, final Project project, final DataSetNode identificationNode, final DDataset parentDataset, final String datasetName, final DefaultTreeModel treeModel,final long instrumentId,final String accessionRegexp, final Boolean importQuantitation){
         
        final String[] result = new String[1];
        
        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {

                if (success) {
                    // start  imports
                    startImport(project, identificationNode, parentDataset, datasetName, result[0], treeModel, instrumentId, accessionRegexp, importQuantitation);
                } else {
                    // delete all nodes
                    treeModel.removeNodeFromParent(identificationNode);
                }
            }
        };

        FileUploadTask task = new FileUploadTask(callback, filePath, result);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

    }
    
    private void startImport(final Project project, final DataSetNode identificationNode, final DDataset parentDataset, final String datasetName, String canonicalPath, final DefaultTreeModel treeModel,final long instrumentId,final String accessionRegexp, final Boolean importQuantitation) {

        // used as out parameter for the service
        final Object[] _results = new Object[ImportMaxQuantTask.RESULTS_SIZE];

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                   Map<String, Long> rsmIdByRsId = _results[ImportMaxQuantTask.RESULT_SUMMARY_IDS_MAP] == null ? null : (Map<String, Long>)_results[ImportMaxQuantTask.RESULT_SUMMARY_IDS_MAP];
                   createDataset(identificationNode, project, parentDataset, datasetName,treeModel, (List) _results[ImportMaxQuantTask.RESULT_SET_IDS_LIST], rsmIdByRsId, getTaskInfo());
                   Long quantDatasetId = (Long)_results[ImportMaxQuantTask.QUANTITATION_DATASET_ID];
                   if (quantDatasetId != null) {
                       createQuantDataset(quantDatasetId);
                   }
                   if(_results[ImportMaxQuantTask.WARNING_MESSAGES] != null && !_results[ImportMaxQuantTask.WARNING_MESSAGES].toString().isEmpty()){
                         JOptionPane.showMessageDialog(IdentificationTree.getCurrentTree(), _results[ImportMaxQuantTask.WARNING_MESSAGES].toString(), "Warning", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    //JPM.TODO : manage error with errorMessage
                    treeModel.removeNodeFromParent(identificationNode);
                }
            }
        };

        // use canonicalPath when it is possible to be sure to have an unique path
        ImportMaxQuantTask  task = new ImportMaxQuantTask(callback, canonicalPath, instrumentId, accessionRegexp, importQuantitation, project.getId(), _results);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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
        
        // identification must be added in one parent node (for the moment)
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

        // we can always add an identification directly to a project
        if (node.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
            setEnabled(true);
            return;
        }
        
        // we can add an identification only to a data set without a ResultSet or a ResultSummary
        if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {
            DataSetNode dataSetNode = (DataSetNode) node;
        
            setEnabled(!dataSetNode.hasResultSet() && !dataSetNode.hasResultSummary());
            return;  
        }

        setEnabled(false);
    }

    private void createQuantDataset(Long quantDatasetId) {

        QuantitationTree tree = QuantitationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                
        DataSetData quantitationData = new DataSetData("running import ...", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);
        final DataSetNode quantitationNode = new DataSetNode(quantitationData);
        quantitationNode.setIsChanging(true);

        AbstractNode rootNode = (AbstractNode) treeModel.getRoot();
        // insert before Trash
        treeModel.insertNodeInto(quantitationNode, rootNode, rootNode.getChildCount() - 1);
        // expand the parent node to display its children
        tree.expandNodeIfNeeded(rootNode);
        QuantitationTree.getCurrentTree().loadDataSet(quantDatasetId, quantitationNode);
    }

    private void createDataset(final DataSetNode identificationNode, Project project, DDataset parentDataset, String name,final DefaultTreeModel treeModel, List resultSetIds, Map<String, Long> rsmIdByRsId, TaskInfo taskInfo) {
                                    

        identificationNode.setIsChanging(false);        
        treeModel.nodeChanged(identificationNode);

        final ArrayList<DDataset> createdDatasetList = new ArrayList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {
                    DDataset dataset = createdDatasetList.get(0);
                    identificationNode.setIsChanging(false);
                    ((DataSetData) identificationNode.getData()).setDataset(dataset);
                    createSubDataset(identificationNode, project, dataset, treeModel, resultSetIds, rsmIdByRsId, taskInfo);
                    treeModel.nodeChanged(identificationNode);
                } else {
                    // should not happen
                    treeModel.removeNodeFromParent(identificationNode);
                }
            }
        };

        // ask asynchronous loading of data
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);

        task.initCreateDatasetAggregate(project, parentDataset, Aggregation.ChildNature.BIOLOGICAL_GROUP, name, createdDatasetList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
    
        
    private void createSubDataset(final DataSetNode parentNode, Project project, DDataset parentDataset, final DefaultTreeModel treeModel, List resultSetIds, Map<String, Long> rsmIdByRsId, TaskInfo taskInfo) {
           
        for(int i=0; i<resultSetIds.size();i++){
            String dsName = new StringBuilder(parentDataset.getName()).append(".").append(resultSetIds.get(i).toString()).toString();
            DataSetData identificationData = new DataSetData(dsName, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS);  //JPM.TODO                
            final DataSetNode identificationNode = new DataSetNode(identificationData);
            identificationNode.setIsChanging(true);
            treeModel.insertNodeInto(identificationNode, parentNode, parentNode.getChildCount());
        
        
            final ArrayList<DDataset> createdDatasetList = new ArrayList<>();

            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    if (success) {

                        DDataset dataset = createdDatasetList.get(0);
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
            Long rsId = (Long)resultSetIds.get(i);
            Long rmsId = (rsmIdByRsId != null) && (rsmIdByRsId.containsKey(rsId.toString())) ?  rsmIdByRsId.get(rsId.toString()) : null;
            task.initCreateDatasetForIdentification(project, parentDataset, Aggregation.ChildNature.SAMPLE_ANALYSIS, dsName, (Long)resultSetIds.get(i), rmsId, createdDatasetList, taskInfo);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    
}
