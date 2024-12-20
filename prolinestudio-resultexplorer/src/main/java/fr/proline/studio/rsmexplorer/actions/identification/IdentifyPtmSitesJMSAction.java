/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.msi.Ptm;
import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.IdentifyPtmSitesTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.IdentifyPtmSitesDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * action to generate spectrum matches via JMS
 *
 * @author MB243701
 */
public class IdentifyPtmSitesJMSAction extends AbstractRSMAction {

  protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

  public IdentifyPtmSitesJMSAction(AbstractTree tree) {
    super("Identify Modification Sites", tree);
  }

  @Override
  public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {

    final DefaultTreeModel treeModel = (DefaultTreeModel) getTree().getModel();
    int nbNodes = selectedNodes.length;

    if (nbNodes > 0) {
      //Test if some nodes has already loaded PTMDataset
      boolean isPTMDatasetLoaded = false;
      //Retrieve potential PTMs from dataset
      List<Ptm> ptms = new ArrayList<>();
      for (int i = 0; i < nbNodes; i++) {
        ArrayList<PtmSpecificity> ptmSpecificities = new ArrayList<>();
        DataSetNode node = (DataSetNode) selectedNodes[i];
        if(DatabaseDataManager.getDatabaseDataManager().getPTMDatasetSetForDS(node.getDataset().getId()) != null ||
                DatabaseDataManager.getDatabaseDataManager().getPTMDatasetSetForDS(node.getDataset().getId()) != null)
          isPTMDatasetLoaded = true;

        DatabasePTMsTask ptmTask = new DatabasePTMsTask(null);
        ptmTask.initLoadUsedPTMs(node.getDataset().getProject().getId(), node.getDataset().getResultSummaryId(), ptmSpecificities);
        ptmTask.fetchData();
        ptms.addAll(ptmSpecificities.stream().map(s -> s.getPtm()).distinct().collect(Collectors.toList()));
      }

      TreeSet<Ptm> ptmsTreeSet = ptms.stream().collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(p -> Long.valueOf(p.getId())))));
      ptms = new ArrayList<>(ptmsTreeSet);

      IdentifyPtmSitesDialog dialog = new IdentifyPtmSitesDialog(WindowManager.getDefault().getMainWindow(), ptms);
      dialog.setLocation(x, y);
      dialog.setVisible(true);
      if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

        //Test if PTMDataset loaded

        //Alert user : previous modification view (Site/Cluster) should be closed in order to load new data
        StringBuilder msg = isPTMDatasetLoaded ? new StringBuilder("Be sure to close all previous Modification Site/Cluster view !\n\n") : new StringBuilder();
        msg.append("Warning: Previously saved Annotated Modification Dataset will be deleted ! \nAre you sure you want to continue ?");
        InfoDialog id = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING,"Run Identify Modification Sites",msg.toString(),false);
        id.setButtonName(DefaultDialog.BUTTON_OK, "Yes");
        id.setButtonName(DefaultDialog.BUTTON_CANCEL, "No");
        id.centerToWindow(WindowManager.getDefault().getMainWindow());
        id.setVisible(true);
        if (id.getButtonClicked() == DefaultDialog.BUTTON_CANCEL) {
          // No clicked
          return;
        }

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
              if(success){
                //remove previous Modificatio data to force reload
                DatabaseDataManager.getDatabaseDataManager().removeAllPTMDatasetsForDS( node.getDataset().getId());
              }
            }
          };

          final DDataset dataset = node.getDataset();

          node.setIsChanging(true);
          treeModel.nodeChanged(node);

          Long projectId = dataset.getProject().getId();
          Long resultSummaryId = dataset.getResultSummaryId();
          IdentifyPtmSitesTask task;
          task = new IdentifyPtmSitesTask(callback, dataset.getName(), projectId, resultSummaryId, null, "3.0", dialog.getPtms(), dialog.getClusteringMethodName());
          AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
        }
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

      if (dataSetNode.isFolder()) {
        setEnabled(false);
        return;
      }

      if (!dataSetNode.hasResultSet() || !dataSetNode.hasResultSummary()) {
        setEnabled(false);
        return;
      }


    }
    setEnabled(true);
  }

}
