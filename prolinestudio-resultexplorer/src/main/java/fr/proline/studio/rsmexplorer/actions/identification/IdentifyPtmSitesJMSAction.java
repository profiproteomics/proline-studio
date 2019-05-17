/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.msi.Ptm;
import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.IdentifyPtmSitesTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.IdentifyPtmSitesDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;

import javax.swing.tree.DefaultTreeModel;

import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * action to generate spectrum matches via JMS
 *
 * @author MB243701
 */
public class IdentifyPtmSitesJMSAction extends AbstractRSMAction {

  protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

  public IdentifyPtmSitesJMSAction(AbstractTree tree) {
    super(NbBundle.getMessage(IdentifyPtmSitesJMSAction.class, "CTL_IdentifyPtmSitesAction"), tree);
  }

  @Override
  public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {

    final DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();

    int nbNodes = selectedNodes.length;
    for (int i = 0; i < nbNodes; i++) {
      final DataSetNode node = (DataSetNode) selectedNodes[i];

      AbstractJMSCallback callback = new AbstractJMSCallback() {

        @Override
        public boolean mustBeCalledInAWT() {
          return true;
        }

        @Override
        public void run(boolean success) {
          node.setIsChanging(false);
          treeModel.nodeChanged(node);
        }
      };

      final DDataset dataset = node.getDataset();

      //Retrieve potential PTMs from dataset
      final ArrayList<PtmSpecificity> ptmSpecificities = new ArrayList<>();
      DatabasePTMsTask ptmTask = new DatabasePTMsTask(null);
      ptmTask.initLoadUsedPTMs(dataset.getProject().getId(), dataset.getResultSummaryId(), ptmSpecificities);
      ptmTask.fetchData();
      List<Ptm> ptms = ptmSpecificities.stream().map(s -> s.getPtm()).distinct().collect(Collectors.toList());
      IdentifyPtmSitesDialog dialog = new IdentifyPtmSitesDialog(WindowManager.getDefault().getMainWindow(), dataset, ptms);
      dialog.setLocation(x, y);
      dialog.setVisible(true);
      if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

        node.setIsChanging(true);
        treeModel.nodeChanged(node);

        Long projectId = dataset.getProject().getId();
        Long resultSummaryId = dataset.getResultSummaryId();
        IdentifyPtmSitesTask task;

        if (dialog.getServiceVersion().equals("2.0")) {
          task = new IdentifyPtmSitesTask(callback, dataset.getName(), projectId, resultSummaryId, null, dialog.getServiceVersion(), dialog.getPtms(), dialog.getClusteringMethodName());
        } else {
          task = new IdentifyPtmSitesTask(callback, dataset.getName(), projectId, resultSummaryId, null);
        }

        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
      }
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
    for (int i = 0; i < nbSelectedNodes; i++) {
      AbstractNode node = selectedNodes[i];

      if (node.isChanging()) {
        setEnabled(false);
        return;
      }

      if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
        setEnabled(false);
        return;
      }

      DataSetNode dataSetNode = (DataSetNode) node;
      if (!dataSetNode.hasResultSet()) {
        setEnabled(false);
        return;
      }


    }
    setEnabled(true);
  }

}
