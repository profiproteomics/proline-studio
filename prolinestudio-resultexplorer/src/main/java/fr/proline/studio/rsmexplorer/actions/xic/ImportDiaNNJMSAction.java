package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.ImportDiaNNTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.ImportDiaNNDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ImportDiaNNJMSAction extends AbstractRSMAction  {

  static Logger logger = LoggerFactory.getLogger(ImportDiaNNJMSAction.class);

  public ImportDiaNNJMSAction(AbstractTree tree) {
    super("Import DiaNN Result...", tree);
  }

  @Override
  public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

    // only one node selected for this action
    final AbstractNode n = selectedNodes[0];

    ImportDiaNNDialog dialog = ImportDiaNNDialog.getDialog(WindowManager.getDefault().getMainWindow());
    dialog.setLocation(x,y);
    dialog.setVisible(true);

    if(dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
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

      final DefaultTreeModel treeModel = (DefaultTreeModel) IdentificationTree.getCurrentTree().getModel();
      final long instrumentId = dialog.getInstrumentId();
      final long peaklistSoftwareId = dialog.getPeaklistSoftwareId();
      File diaNNFile = dialog.getFile2Import();

      // Create temporary nodes for the identifications
      String datasetName = diaNNFile.getName();
      int indexOfDot = datasetName.lastIndexOf('.');
      if (indexOfDot != -1) {
        datasetName = datasetName.substring(0, indexOfDot);
      }

      DataSetData identificationData = DataSetData.createTemporaryAggregate("_"+datasetName+"_"); // new DatasetData(datasetName, Dataset.DatasetType.AGGREGATE, Aggregation.ChildNature.BIOLOGICAL_GROUP);  //JPM.TODO
      final DataSetNode identificationNode = new DataSetNode(identificationData);
      identificationNode.setIsChanging(true);

      if (isParentAProject) {
        treeModel.insertNodeInto(identificationNode, n, n.getChildCount() - 1);
      } else {
        treeModel.insertNodeInto(identificationNode, n, n.getChildCount());
      }

      IdentificationTree.getCurrentTree().expandNodeIfNeeded(n);

      startImport(diaNNFile.getPath(), project, identificationNode, parentDataset, datasetName, treeModel, instrumentId, peaklistSoftwareId);
    }
  }

  private void startImport(final String filePath, final Project project, final DataSetNode identificationNode, final DDataset parentDataset, final String datasetName, final DefaultTreeModel treeModel,final long instrumentId,final long peaklisSoftId) {

    final Object[] _taskResults = new Object[3];

    AbstractJMSCallback callback = new AbstractJMSCallback() {
      @Override
      public boolean mustBeCalledInAWT() {
        return true;
      }

      @Override
      public void run(boolean success) {
          if(success) {
            Map<String, Long> rsmIdByRsId =  (Map<String, Long>)_taskResults[0];
            Long identDsId = (Long)_taskResults[1];
            createDataset(identificationNode, project, parentDataset, datasetName, treeModel,  rsmIdByRsId,identDsId, getTaskInfo());
            Long quantDatasetId = (Long)_taskResults[2];
            if (quantDatasetId != null) {
              createQuantDataset(quantDatasetId);
            }
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), " Import DIANN Result OK !! ");
          } else {
            treeModel.removeNodeFromParent(identificationNode);
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), " Import DIANN Result ERROR !! "+getTaskError());
          }
      }
    };


    logger.info(" WILL CALL ImportDiaNNTask ");

    ImportDiaNNTask task =new ImportDiaNNTask(callback,filePath,instrumentId,peaklisSoftId,project.getId(), parentDataset, _taskResults);
    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

  }

  private void createQuantDataset(Long quantDatasetId) {

    QuantitationTree tree = QuantitationTree.getCurrentTree();
    final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

    DataSetData quantitationData = DataSetData.createTemporaryQuantitation("running import ...");//new DataSetData("running import ...", Dataset.DatasetType.QUANTITATION, Aggregation.ChildNature.QUANTITATION_FRACTION);
    final DataSetNode quantitationNode = new DataSetNode(quantitationData);
    quantitationNode.setIsChanging(true);

    AbstractNode rootNode = (AbstractNode) treeModel.getRoot();
    // insert before Trash
    treeModel.insertNodeInto(quantitationNode, rootNode, rootNode.getChildCount() - 1);
    // expand the parent node to display its children
    tree.expandNodeIfNeeded(rootNode);
    QuantitationTree.getCurrentTree().loadDataSet(quantDatasetId, quantitationNode);
  }

  private void createDataset(final DataSetNode identificationNode, Project project, DDataset parentDataset, String name, final DefaultTreeModel treeModel, Map<String, Long> rsmIdByRsId, Long identDsId, TaskInfo taskInfo) {

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
          createSubDataset(identificationNode, project, dataset, treeModel, rsmIdByRsId, taskInfo);
          treeModel.nodeChanged(identificationNode);
        } else {
          // should not happen
          treeModel.removeNodeFromParent(identificationNode);
        }
      }
    };

    // ask asynchronous loading of data
    DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
    if(identDsId != null) {
      task.initLoadDataset(identDsId, createdDatasetList);
    } else
      task.initCreateDatasetAggregate(project, parentDataset, Aggregation.ChildNature.BIOLOGICAL_GROUP, name, createdDatasetList);
    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

  }

  private void createSubDataset(final DataSetNode parentNode, Project project, DDataset parentDataset, final DefaultTreeModel treeModel, Map<String, Long> rsmIdByRsId, TaskInfo taskInfo) {

    for(String rsIdAsStr : rsmIdByRsId.keySet()){
      Long associatedRsmId = rsmIdByRsId.get(rsIdAsStr);
      Long rsId = Long.valueOf(rsIdAsStr);
      String dsName = parentDataset.getName() + "." + associatedRsmId.toString();
      DataSetData identificationData = DataSetData.createTemporaryIdentification(dsName); //new DataSetData(dsName, Dataset.DatasetType.IDENTIFICATION, Aggregation.ChildNature.SAMPLE_ANALYSIS);  //JPM.TODO
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
      /* Long rmsId = null; // (rsmIdByRsId != null) && (rsmIdByRsId.containsKey(rsId.toString())) ?  rsmIdByRsId.get(rsId.toString()) : null;*/
      task.initCreateDatasetForIdentification(project, parentDataset, Aggregation.ChildNature.SAMPLE_ANALYSIS, dsName, rsId, associatedRsmId, createdDatasetList, taskInfo);
      AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
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

    // identification must be added only in one parent node
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
    if ( node.getType() == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) {
      setEnabled(true);
      return;
    }

   // we can add an identification only to a data set without a ResultSet or a ResultSummary
    if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {
      DataSetNode dataSetNode = (DataSetNode) node;
      setEnabled(!dataSetNode.hasResultSet() && !dataSetNode.hasResultSummary());
      return;
    }
//    else { //Import Quant. Dataset should contains ident childs
//        Enumeration<TreeNode> childsDataset = dataSetNode.children();
//        while (childsDataset.hasMoreElements() ) {
//          TreeNode dsNode = childsDataset.nextElement();
//          if(dsNode instanceof DataSetNode) {
//            DDataset dDataSet =((DataSetNode) dsNode).getDataset();
//            if(dDataSet.getResultSetId() <=1 ||dDataSet.getResultSummaryId() <=1){
//              setEnabled(false);
//              return;
//            }
//
//          } else {
//            setEnabled(false);
//            return;
//          }
//
//        }
//      }

//      return;
//    }

    setEnabled(false);
  }
}
