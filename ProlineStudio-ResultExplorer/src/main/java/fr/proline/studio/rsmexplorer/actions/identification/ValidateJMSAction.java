package fr.proline.studio.rsmexplorer.actions.identification;

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
import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.ChangeTypicalProteinTask;
import fr.proline.studio.dpm.task.jms.ValidationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ValidationDialog;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to validate or re-validate a Search Result
 *
 * 
 * @author jm235353
 */
public class ValidateJMSAction extends AbstractRSMAction {

    Long rsm=null;
    
    public ValidateJMSAction() {
        super(NbBundle.getMessage(ValidateJMSAction.class, "CTL_ValidateAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        int nbAlreadyValidated = 0;

        int nbNodes = selectedNodes.length;
        ArrayList<DDataset> datasetList = new ArrayList<>(nbNodes);
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];
            DDataset d = dataSetNode.getDataset();
            datasetList.add(d);
            rsm=d.getResultSummaryId(); 
            if (dataSetNode.hasResultSummary()) {
                nbAlreadyValidated++;
            }
        }


        if (nbAlreadyValidated > 0) {
            // check if the user wants to revalidate the Search Result
            String message;
            if (nbAlreadyValidated == 1) {
                if (nbNodes == 1) {
                    message = "Search Result has been already validated.\nDo you want to re-validate it ?";
                } else {
                    message = "One of the Search Results has been already validated.\nDo you want to re-validate it ?";
                }
            } else {
                if (nbNodes == nbAlreadyValidated) {
                    message = "Search Results have been already validated.\nDo you want to re-validate them ?";
                } else {
                    message = "Some of the Search Results have been already validated.\nDo you want to re-validate them ?";
                }
            }
            OptionDialog yesNoDialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Re-Validate ?", message);
            yesNoDialog.setLocation(x, y);
            yesNoDialog.setVisible(true);
            if (yesNoDialog.getButtonClicked() != DefaultDialog.BUTTON_OK) {
                return;
            }
        }


        ValidationDialog dialog = ValidationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setDatasetList(datasetList);
        dialog.setVisible(true);


        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            // retrieve parameters
            final HashMap<String, String> parserArguments = dialog.getArguments();
            final List<ChangeTypicalRule> changeTypicalRules  = dialog.getChangeTypicalRules();
            final String scoringType = dialog.getScoringType();
            //VDTEST
            boolean getV2Service = false;
            if(parserArguments.containsKey("propagate_prot_set_filters") || parserArguments.containsKey("propagate_pep_match_filters"))
                getV2Service = true;
            final boolean v2Service = getV2Service;
            
            IdentificationTree tree = IdentificationTree.getCurrentTree();
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            // start validation for each selected Dataset
            for (int i = 0; i < nbNodes; i++) {
                final DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

                dataSetNode.setIsChanging(true);
                treeModel.nodeChanged(dataSetNode);

                final DDataset d = dataSetNode.getDataset();

                if (dataSetNode.hasResultSummary()) {
                    rsm=dataSetNode.getResultSummaryId();
                    // we remove the result Summary and we start validation
                    
                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                            askValidation(dataSetNode, parserArguments, changeTypicalRules, scoringType, v2Service);
                        }
                    };

                    DatabaseDataSetTask taskRemoveValidation = new DatabaseDataSetTask(callback);
                    taskRemoveValidation.initModifyDatasetToRemoveValidation(d);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(taskRemoveValidation);
                } else {
                    // there is no result summary, we start validation at once
                    askValidation(dataSetNode, parserArguments, changeTypicalRules, scoringType, v2Service);
                }
            }
        }
    }

    private void askValidation(final DataSetNode dataSetNode, HashMap<String, String> parserArguments, final List<ChangeTypicalRule> changeTypicalRules, final String scoringType,final boolean v2Service) {

        final DDataset d = dataSetNode.getDataset();

        // used as out parameter for the service
        final Integer[] _resultSummaryId = new Integer[1];
        final HashMap<Long,Long> _rsmIdsByRsIds = new HashMap();

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                    if (success) {

                    updateDatasets(dataSetNode, d, _resultSummaryId[0],_rsmIdsByRsIds,  getTaskInfo(), changeTypicalRules);

                } else {
                    //JPM.TODO : manage error with errorMessage
                    dataSetNode.setIsChanging(false);
                    IdentificationTree tree = IdentificationTree.getCurrentTree();
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    treeModel.nodeChanged(dataSetNode);
                }
            }
        };
        
        ValidationTask task = new ValidationTask(callback, d, "", parserArguments, _resultSummaryId, scoringType);
        if(v2Service)
            task = new ValidationTask(callback, d, "", parserArguments, _resultSummaryId,_rsmIdsByRsIds, scoringType);    
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

    }

    private void changeTypicalProtein(final DataSetNode datasetNode, List<ChangeTypicalRule> changeTypicalRules) {
        
                final DDataset d = datasetNode.getDataset();

                 AbstractJMSCallback callback = new AbstractJMSCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {

                        datasetNode.setIsChanging(false);


                        IdentificationTree tree = IdentificationTree.getCurrentTree();
                        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                        treeModel.nodeChanged(datasetNode);

                    }
                };

                ChangeTypicalProteinTask task = new ChangeTypicalProteinTask(callback, d, changeTypicalRules);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
    }
    
    private void updateDatasets(final DataSetNode rootDatasetNode, final DDataset rootDS, long rootRsmId, Map<Long,Long> rsmIdsByRsIds,  TaskInfo taskInfo, final List<ChangeTypicalRule> changeTypicalRules) {
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    //Update root Dataset childs
                    updateDatasetHierarchy(rootDatasetNode,rsmIdsByRsIds,taskInfo, changeTypicalRules);
                }
            };
            
        //load node children
        IdentificationTree.getCurrentTree().loadInBackground(rootDatasetNode, callback);                       

        //Update root node        
        updateDataset(rootDatasetNode, rootDS, rootRsmId, taskInfo, changeTypicalRules);

    }

    private void updateDatasetHierarchy(final DataSetNode rootDatasetNode, Map<Long,Long> rsmIdsByRsIds,  TaskInfo taskInfo, final List<ChangeTypicalRule> changeTypicalRules) {    
        
        Enumeration e = rootDatasetNode.children();
        while (e.hasMoreElements()) {
            //Go through childs dataset
            final DataSetNode dsChild = (DataSetNode) e.nextElement();
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    //Update this Dataset childs
                    updateDatasetHierarchy(dsChild, rsmIdsByRsIds, taskInfo, changeTypicalRules);
                }
            };
            updateDataset(dsChild, dsChild.getDataset(), rsmIdsByRsIds.get(dsChild.getResultSetId()), taskInfo, changeTypicalRules);
            
            //load node
            IdentificationTree.getCurrentTree().loadInBackground(dsChild, callback);
         }        
    }
        
    private void updateDataset(final DataSetNode datasetNode, final DDataset d, long resultSummaryId, TaskInfo taskInfo, final List<ChangeTypicalRule> changeTypicalRules) {
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success && changeTypicalRules!=null && !changeTypicalRules.isEmpty()) {
                    changeTypicalProtein(datasetNode, changeTypicalRules);
                } else {

                    datasetNode.setIsChanging(false);

                    IdentificationTree tree = IdentificationTree.getCurrentTree();
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    treeModel.nodeChanged(datasetNode);
                }
            }
        };

        // ask asynchronous loading of data

        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initModifyDatasetForValidation(d, resultSummaryId, taskInfo);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

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

            // if node is being created, we can not validate it (for the moment)
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            // node must be a dataset
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            // node must have a ResultSet
            DataSetNode dataSetNode = (DataSetNode) node;
            if (!dataSetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }
            
            // if merge DS : forbidden (re)validation on RSM merge
            DataSetData datasetData = (DataSetData) dataSetNode.getData();
            Dataset.DatasetType datasetType = datasetData.getDatasetType();
            if ( Dataset.DatasetType.AGGREGATE.equals(datasetType) && dataSetNode.hasResultSummary()) {
                DDataset.MergeInformation mergeInfo = datasetData.getDataset().getMergeInformation();
                if (mergeInfo.compareTo(DDataset.MergeInformation.MERGE_IDENTIFICATION_SUMMARY) == 0) {
                    setEnabled(false);
                    return;
                }
            } 
            
            // parent node
            AbstractNode parentNode = (AbstractNode) dataSetNode.getParent();
            if (parentNode.getType() == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode parentDatasetNode = (DataSetNode) parentNode;
                if (parentDatasetNode.hasResultSet()) {
                    // parent is already merged (RSM or Rset), we forbid to validata a son
                    setEnabled(false);
                    return;
                }
            }
            
        }

        setEnabled(true);

    }
}