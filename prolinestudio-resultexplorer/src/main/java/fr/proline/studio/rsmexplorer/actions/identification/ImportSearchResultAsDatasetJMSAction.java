package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ImportIdentificationDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.CertifyIdentificationTask;
import fr.proline.studio.dpm.task.jms.ImportIdentificationTask;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * Action to import one or multiple identification, imported Rset are refered by
 * a Dataset
 * 
 * @author JM235353
 */
public class ImportSearchResultAsDatasetJMSAction extends AbstractRSMAction {

    //VDS TODO : To merge with ImportSearchResultAsRsetJMSAction
    private static final String GENERAL_APPLICATION_SETTINGS = "General Application Settings";

    public ImportSearchResultAsDatasetJMSAction(AbstractTree tree) {
        super(NbBundle.getMessage(ImportSearchResultAsDatasetJMSAction.class, "CTL_AddSearchResult"), tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        final AbstractNode n = selectedNodes[0];

        // retrieve project id
        long projectId = 0;
        if (n.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
            IdProjectIdentificationNode projectNode = (IdProjectIdentificationNode) n;
            projectId = projectNode.getProject().getId();
        } else if (n.getType() == AbstractNode.NodeTypes.DATA_SET) {
            DataSetNode dataSetNode = (DataSetNode) n;
            projectId = dataSetNode.getDataset().getProject().getId();

        }

        ImportIdentificationDialog dialog = ImportIdentificationDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

            // retrieve parameters
            File[] filePaths = dialog.getFilePaths();
            final HashMap<String, String> parserArguments = dialog.getParserArguments();

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
            final Project _project = project;
            final DDataset _parentDataset = parentDataset;

            final String parserId = dialog.getParserId();
            final String decoyRegex = dialog.getDecoyRegex();
            final long instrumentId = dialog.getInstrumentId();
            final long peaklistSoftwareId = dialog.getPeaklistSoftwareId();           
            final long fragRuleSetId = dialog.getFragmentationRuleSetId();
            IdentificationTree tree = IdentificationTree.getCurrentTree();

            final ArrayList<DataSetNode> allIdentificationNodes = new ArrayList<>();
            final ArrayList<String> allDatasetNames = new ArrayList<>();

            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            // Create dataset nodes for each files
            final int nbFiles = filePaths.length;
            for (int i = 0; i < nbFiles; i++) {
                File f = filePaths[i];

                // Create temporary nodes for the identifications
                String datasetName = f.getName();
                int indexOfDot = datasetName.lastIndexOf('.');
                if (indexOfDot != -1) {
                    datasetName = datasetName.substring(0, indexOfDot);
                }
//                final String _datasetName = datasetName;
                allDatasetNames.add(datasetName);

                DataSetData identificationData = DataSetData.createTemporaryIdentification(datasetName); //new DataSetData(datasetName, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS);  //JPM.TODO

                final DataSetNode identificationNode = new DataSetNode(identificationData);
                identificationNode.setIsChanging(true);
                allIdentificationNodes.add(identificationNode);

                if (isParentAProject) {
                    treeModel.insertNodeInto(identificationNode, n, n.getChildCount() - 1);
                } else {
                    treeModel.insertNodeInto(identificationNode, n, n.getChildCount());
                }

            }

            if (nbFiles > 0) {
                // expand the parent node to display its children
                tree.expandNodeIfNeeded(n);
            }

            // Pre-Import files
            final String[] pathArray = new String[nbFiles];
            for (int i = 0; i < nbFiles; i++) {
                File f = filePaths[i];

                pathArray[i] = f.getPath();
            }

            final String[] result = new String[1];

            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {

                    if (success) {
                        // start all imports
                        for (int i = 0; i < nbFiles; i++) {
                            startImport(_project, allIdentificationNodes.get(i), _parentDataset, allDatasetNames.get(i), pathArray[i], treeModel, parserId, parserArguments, decoyRegex, instrumentId, peaklistSoftwareId, fragRuleSetId);
                        }
                    } else {
                        // delete all nodes
                        for (DataSetNode allIdentificationNode : allIdentificationNodes) {
                            treeModel.removeNodeFromParent(allIdentificationNode);
                        }
                    }
                }
            };

            CertifyIdentificationTask task = new CertifyIdentificationTask(callback, parserId, parserArguments, pathArray, projectId, result);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

        }
    }

    private void startImport(final Project project, final DataSetNode identificationNode, final DDataset parentDataset, final String datasetName, String canonicalPath, final DefaultTreeModel treeModel, String parserId, HashMap<String, String> parserArguments, String decoyRegex, long instrumentId, long peaklistSoftwareId, long fragmentationRuleSetId) {
        // used as out parameter for the service
        final Long[] _resultSetId = new Long[1];

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {

                    ImportSearchResultAsRsetJMSAction.fireListener(project.getId());

                    createDataset(identificationNode, project, parentDataset, datasetName, _resultSetId[0], getTaskInfo());

                } else {
                    //JPM.TODO : manage error with errorMessage

                    treeModel.removeNodeFromParent(identificationNode);
                }
            }
        };

        // use canonicalPath when it is possible to be sure to have an unique path
        ImportIdentificationTask task = new ImportIdentificationTask(callback, parserId, parserArguments, canonicalPath, decoyRegex, instrumentId, peaklistSoftwareId, false /*don't save spectrum match any more*/, fragmentationRuleSetId, project.getId(), _resultSetId);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
    }

    private void createDataset(final DataSetNode identificationNode, Project project, DDataset parentDataset, String name, Long resultSetId, TaskInfo taskInfo) {

        identificationNode.setIsChanging(false);

        IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
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
                    treeModel.nodeChanged(identificationNode);

                    ImportManager.importRenaming(dataset, identificationNode, tree);
                    
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

}
