package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.dto.DDataset;
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
import fr.proline.studio.dpm.task.ChangeTypicalProteinTask;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to validate or re-validate a Search Result
 *
 * @author jm235353
 */
public class ValidateAction extends AbstractRSMAction {

    public ValidateAction() {
        super(NbBundle.getMessage(ValidateAction.class, "CTL_ValidateAction"));
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        int nbAlreadyValidated = 0;

        int nbNodes = selectedNodes.length;
        ArrayList<DDataset> datasetList = new ArrayList<>(nbNodes);
        for (int i = 0; i < nbNodes; i++) {
            RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[i];
            DDataset d = dataSetNode.getDataset();
            datasetList.add(d);

            if (dataSetNode.hasResultSummary()) {
                nbAlreadyValidated++;
            }
        }


        if (nbAlreadyValidated > 0) {
            // check if the user wants to revalidate the Search Result
            String message;
            if (nbAlreadyValidated == 1) {
                if (nbNodes == 1) {
                    message = "Search Result has been validated already.\nDo you want to re-validate it ?";
                } else {
                    message = "One of the Search Results has been validated already.\nDo you want to re-validate it ?";
                }
            } else {
                if (nbNodes == nbAlreadyValidated) {
                    message = "Search Results have been validated already.\nDo you want to re-validate them ?";
                } else {
                    message = "Some of the Search Results have been validated already.\nDo you want to re-validate them ?";
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
            final String regex = dialog.getTypicalProteinRegex();
            final boolean regexOnAccession = dialog.isTYpicalProteinOnAccession();
            final String scoringType = dialog.getScoringType();

            RSMTree tree = RSMTree.getCurrentTree();
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            // start validation for each selected Dataset
            for (int i = 0; i < nbNodes; i++) {
                final RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[i];

                dataSetNode.setIsChanging(true);
                treeModel.nodeChanged(dataSetNode);

                final DDataset d = dataSetNode.getDataset();

                if (dataSetNode.hasResultSummary()) {

                    // we remove the result Summary and we start validation
                    
                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                            askValidation(dataSetNode, parserArguments, regex, regexOnAccession, scoringType);
                        }
                    };

                    DatabaseDataSetTask taskRemoveValidation = new DatabaseDataSetTask(callback);
                    taskRemoveValidation.initModifyDatasetToRemoveValidation(d);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(taskRemoveValidation);
                } else {
                    // there is no result summary, we start validation at once
                    askValidation(dataSetNode, parserArguments, regex, regexOnAccession, scoringType);
                }



            }


        }
    }

    private void askValidation(final RSMDataSetNode dataSetNode, HashMap<String, String> parserArguments, final String regex, final boolean regexOnAccession, final String scoringType) {

        final DDataset d = dataSetNode.getDataset();

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

                    updateDataset(dataSetNode, d, _resultSummaryId[0], getTaskInfo(), regex, regexOnAccession);


                } else {
                    //JPM.TODO : manage error with errorMessage
                    dataSetNode.setIsChanging(false);


                    RSMTree tree = RSMTree.getCurrentTree();
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    treeModel.nodeChanged(dataSetNode);
                }
            }
        };


        ValidationTask task = new ValidationTask(callback, dataSetNode.getDataset(), "", parserArguments, _resultSummaryId, scoringType);
        AccessServiceThread.getAccessServiceThread().addTask(task);
    }

    private void changeTypicalProtein(final RSMDataSetNode datasetNode, String regex, boolean regexOnAccession) {
        
                final DDataset d = datasetNode.getDataset();

                AbstractServiceCallback callback = new AbstractServiceCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success) {

                        datasetNode.setIsChanging(false);


                        RSMTree tree = RSMTree.getCurrentTree();
                        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                        treeModel.nodeChanged(datasetNode);

                    }
                };


                ChangeTypicalProteinTask task = new ChangeTypicalProteinTask(callback, d, regex, regexOnAccession);
                AccessServiceThread.getAccessServiceThread().addTask(task);
    }
    
    private void updateDataset(final RSMDataSetNode datasetNode, final DDataset d, long resultSummaryId, TaskInfo taskInfo, final String regex, final boolean regexOnAccession) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success && (regex!=null)) {
                    changeTypicalProtein(datasetNode, regex, regexOnAccession);
                } else {

                    datasetNode.setIsChanging(false);

                    RSMTree tree = RSMTree.getCurrentTree();
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
    public void updateEnabled(RSMNode[] selectedNodes) {

        // note : we can ask for the validation of multiple ResultSet in one time

        int nbSelectedNodes = selectedNodes.length;
        for (int i = 0; i < nbSelectedNodes; i++) {
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

            // parent node must have a ResultSet
            RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
            if (!dataSetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }
        }

        setEnabled(true);

    }
}