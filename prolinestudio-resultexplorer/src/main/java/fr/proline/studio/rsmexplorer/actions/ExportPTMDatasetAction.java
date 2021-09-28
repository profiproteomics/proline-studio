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
package fr.proline.studio.rsmexplorer.actions;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.data.ptm.*;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportPTMDatasetAction extends AbstractRSMAction {

  protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ExportFastaAction");

  public ExportPTMDatasetAction(AbstractTree tree) {
    super("PTM Dataset", tree);
  }

  @Override
  public void updateEnabled(AbstractNode[] selectedNodes) {
    int nbSelectedNodes = selectedNodes.length;

    if (nbSelectedNodes != 1) {//don't allow multiple
      setEnabled(false);
      return;
    }

    DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
    if (dataSetNode.isChanging() || dataSetNode.isFolder()) {
      setEnabled(false);
      return;
    }

    if (!dataSetNode.hasResultSet() || !dataSetNode.hasResultSummary()) {
      setEnabled(false);
    }
  }

  @Override
  public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

    final DDataset dataSet = ((DataSetData) selectedNodes[0].getData()).getDataset();

    //Test if PTMDataset already loaded
    PTMDataset ptmDataset = DatabaseDataManager.getDatabaseDataManager().getClustersPTMDatasetForDS(dataSet.getId());
    if(ptmDataset == null)
      ptmDataset = DatabaseDataManager.getDatabaseDataManager().getSitesPTMDatasetForDS(dataSet.getId());

    if(ptmDataset != null){
      exportPTMDataset(ptmDataset);

    } else {
      JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "PTM Dataset not loaded yet ! Display it to load data...", DatabasePTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
    }
    //
//    --- VDS Get loaded PTMDataset
//    DDataset dataSet = dataSetNode.getDataset();
//    ResultSummary rsm = dataSetNode.getResultSummary();
//    if (rsm != null) {
//
//      //read PTMDataset
//      loadPTMDataset(dataSet);
//    } else {
//
//
//      // we have to load the result set
//      AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
//
//        @Override
//        public boolean mustBeCalledInAWT() {
//          return true;
//        }
//
//        @Override
//        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
//          loadPTMDataset(dataSet);
//        }
//      };
//
//
//      // ask asynchronous loading of data
//      DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
//      task.initLoadRsetAndRsm(dataSet);
//      AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
//    }
  }

//  boolean m_loadPepMatchOnGoing;
//  private void loadPTMDataset(DDataset dataSet) {
//
//    //Test if rsm is loaded
////    ResultSummary m_rsm = m_ptmDataset.getDataset().getResultSummary(); //facilitation === PTM Ident
//    List<PTMDataset> ptmDS = new ArrayList<>();
//
//    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
//
//      @Override
//      public boolean mustBeCalledInAWT() {
//        return true;
//      }
//
//      @Override
//      public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
//        m_logger.debug("ExportPTMDatasetAction : **** Callback task "+taskId+", success "+success+", finished "+finished+"; with subtask : "+(subTask != null));
//        if(success) {
//          if(subTask == null){
//            //Main task callback!
//            m_ptmDataset = ptmDS.get(0);
//            m_logger.debug("  -- created "+m_ptmDataset.getPTMClusters().size()+" PTMCluster.");
//            m_loadPepMatchOnGoing=true;
//            exportPTMDataset();
//          }
//        } else{
//          displayLoadError();
//        }
//
//        if (finished) {
//          m_logger.debug(" Task "+taskId+" DONE. Should propagate changes ");
//        }
//      }
//    };
//
//    ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId();
//    // ask asynchronous loading of data
//    m_task = new DatabasePTMsTask(callback);
//    m_task.initLoadPTMDataset(ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId(), dataSet, ptmDS, false);
//    m_logger.debug("DataBoxPTMClusters : **** Register task DatabasePTMsTask.initLoadPTMDataset. ID= "+m_task.getId());
//    AccessDatabaseThread.getAccessDatabaseThread().addTask(m_task);
//
//  }
//
//  private void displayLoadError(){
//
//    TaskError taskError = m_task.getTaskError();
//    if (taskError != null) {
//      if (DatabasePTMsTask.ERROR_PTM_CLUSTER_LOADING.equals(taskError.getErrorTitle()) ) {
//        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "To export PTM Dataset with Modification Clusters, you must run \"Identify Modification Sites\" beforehand.", taskError.getErrorTitle(), JOptionPane.WARNING_MESSAGE);
//      } else {
//        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), taskError.getErrorText(), DatabasePTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
//      }
//    } else {
//      JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Null Error Task", DatabasePTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
//    }
//
//  }

  private void exportPTMDataset(PTMDataset ptmDataset){

    JSONPTMDataset jsonPTMDataset = createJSONPTMDataset(ptmDataset);
    if(jsonPTMDataset == null)
       return;
    try {

      //File to export to
      String ptmFileName =  ptmDataset.getDataset().getName().trim();
      File ptmFile = new File("./"+ptmFileName+".json");

      FileWriter writer = new FileWriter(ptmFile);
      Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
      String jsonString = gson.toJson(jsonPTMDataset);

      writer.write(jsonString);
      writer.flush();
      writer.close();

    } catch (IOException e) {
      e.printStackTrace();
    }


    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Export PTM DS with "+ptmDataset.getPTMClusters().size()+" clusters ", DatabasePTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
  }

  private JSONPTMDataset createJSONPTMDataset(PTMDataset ptmDataset) {
    JSONPTMDataset ptmDS = new JSONPTMDataset();

    List<DInfoPTM> ptmInfos =ptmDataset.getInfoPTMs();
    Long[] ptmInfoIds = new Long[ptmInfos.size()];
    for(int i=0 ; i<ptmInfos.size();i++){
        ptmInfoIds[i] = ptmInfos.get(i).getIdPtmSpecificity();
    }
    ptmDS.ptmIds = ptmInfoIds;


    List<Long> leafRsmIds =ptmDataset.getLeafResultSummaryIds();
    Long[] rsmIds = new Long[leafRsmIds.size()];
    for(int i=0 ; i<leafRsmIds.size();i++){
      rsmIds[i] = leafRsmIds.get(i);
    }
    ptmDS.leafResultSummaryIds =rsmIds;

    //--- Read Sites
    List<PTMSite> allSites = ptmDataset.getPTMSites();
    JSONPTMSite2[] allJSONSites = new JSONPTMSite2[allSites.size()];
    for(int i=0 ; i<allSites.size();i++){
      AbstractJSONPTMSite newtPTMSite =  allSites.get(i).getJSONPtmSite();
      if(newtPTMSite instanceof JSONPTMSite2)
        allJSONSites[i] =(JSONPTMSite2) newtPTMSite;
      else {
          JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Can't Export old PTM Site informations ", DatabasePTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
          return null;
      }
    }
    ptmDS.ptmSites =allJSONSites;

    //--- Read Clustes
    List<PTMCluster> allClusters = ptmDataset.getPTMClusters();
    JSONPTMCluster[] allJSONClusters = new JSONPTMCluster[allClusters.size()];
    for(int i=0 ; i<allClusters.size();i++){

      PTMCluster nextCluster = allClusters.get(i);
      JSONPTMCluster newtPTMCluster  = new JSONPTMCluster();
      newtPTMCluster.id = nextCluster.getId();
      newtPTMCluster.bestPeptideMatchId = nextCluster.getRepresentativePepMatch().getId();
      newtPTMCluster.localizationConfidence = nextCluster.getLocalizationConfidence();
      newtPTMCluster.isomericPeptideIds = new Long[0];

      allSites = nextCluster.getPTMSites();
      Long[] clusterJSONSites = new Long[allSites.size()];
      for(int index =0 ; index<allSites.size();index++){
        AbstractJSONPTMSite newtPTMSite =  allSites.get(index).getJSONPtmSite();
        if(newtPTMSite instanceof JSONPTMSite2)
          clusterJSONSites[index] =((JSONPTMSite2) newtPTMSite).id;
        else {
          JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Can't Export old PTM Site informations ", DatabasePTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
          return null;
        }
      }
      newtPTMCluster.ptmSiteLocations = clusterJSONSites;
      allJSONClusters[i] = newtPTMCluster;
    }
    ptmDS.ptmClusters =allJSONClusters;
    return ptmDS;
  }



}
