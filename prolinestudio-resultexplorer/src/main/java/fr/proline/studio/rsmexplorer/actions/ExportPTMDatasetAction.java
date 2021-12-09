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
import fr.proline.studio.dam.tasks.DatabaseDatasetPTMsTask;
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
      JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "PTM Dataset not loaded yet ! Display it to load data...", DatabaseDatasetPTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
    }
  }


  private void exportPTMDataset(PTMDataset ptmDataset){

    try {
      JSONPTMDataset jsonPTMDataset = ptmDataset.createJSONPTMDataset();
      if(jsonPTMDataset == null)
        return;


      //File to export to
      String ptmFileName =  ptmDataset.getDataset().getName().trim();
      File ptmFile = new File("./"+ptmFileName+".json");

      FileWriter writer = new FileWriter(ptmFile);
      Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
      String jsonString = gson.toJson(jsonPTMDataset);

      writer.write(jsonString);
      writer.flush();
      writer.close();

    } catch (IOException | IllegalAccessException e) {
      JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), e.getMessage(), DatabaseDatasetPTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }


    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "Export PTM DS with "+ptmDataset.getPTMClusters().size()+" clusters ", DatabaseDatasetPTMsTask.ERROR_PTM_CLUSTER_LOADING, JOptionPane.ERROR_MESSAGE);
  }




}
