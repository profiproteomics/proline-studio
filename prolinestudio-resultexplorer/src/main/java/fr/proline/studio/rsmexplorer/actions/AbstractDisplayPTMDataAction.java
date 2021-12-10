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

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;

public abstract class AbstractDisplayPTMDataAction extends AbstractRSMAction  {

  private boolean m_dataIsPTMSite;
  private boolean m_isAnnotatedPTMs;

  public AbstractDisplayPTMDataAction(boolean isSite, AbstractTree tree) {
    this(isSite, false, tree);
  }

  public AbstractDisplayPTMDataAction(boolean isSite,boolean isAnnotatedPTMDs, AbstractTree tree) {
    super(isSite ? (isAnnotatedPTMDs ? "Annotated Sites" : "Sites" ): (isAnnotatedPTMDs ? "Annotated Clusters" : "Clusters" ), tree);
    m_dataIsPTMSite = isSite;
    m_isAnnotatedPTMs = isAnnotatedPTMDs;
  }

  public AbstractDisplayPTMDataAction(boolean isSite,boolean isAnnotatedPTMDs, AbstractTree tree, String prefix) {
    super(isSite ? (isAnnotatedPTMDs ? prefix+" Annotated Sites" : prefix+" Sites" ): (isAnnotatedPTMDs ? prefix+" Annotated Clusters" : prefix+" Clusters" ), tree);
    m_dataIsPTMSite = isSite;
    m_isAnnotatedPTMs = isAnnotatedPTMDs;
  }


  @Override
  public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
    int nbNodes = selectedNodes.length;
    for (int i = 0; i < nbNodes; i++) {
      DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];
      actionImpl(dataSetNode);
    }
  }

  protected  boolean isAnnotatedPTMsAction(){
    return m_isAnnotatedPTMs;
  }

  protected  abstract void loadWindowBox(DDataset dataSet, Object data);

  protected void actionImpl(DataSetNode dataSetNode) {

    final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();

    if (!dataSetNode.hasResultSummary()) {
      return; // should not happen
    }

    //Test if PTMDataset already loaded
    PTMDataset ptmDataset = null;
    if(m_isAnnotatedPTMs)
      ptmDataset = m_dataIsPTMSite ?  DatabaseDataManager.getDatabaseDataManager().getAnnotatedSitesPTMDatasetForDS(dataSet.getId()) :  DatabaseDataManager.getDatabaseDataManager().getAnnotatedClustersPTMDatasetForDS(dataSet.getId());
    else
      ptmDataset = m_dataIsPTMSite ?  DatabaseDataManager.getDatabaseDataManager().getSitesPTMDatasetForDS(dataSet.getId()) :  DatabaseDataManager.getDatabaseDataManager().getClustersPTMDatasetForDS(dataSet.getId());

    if(ptmDataset != null){
      loadWindowBox(dataSet, ptmDataset);

    } else {
      //Not loaded yet

      ResultSummary rsm = dataSetNode.getResultSummary();
      if (rsm != null) {
        loadWindowBox(dataSet, dataSet);

      } else {


        // we have to load the result set
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

          @Override
          public boolean mustBeCalledInAWT() {
            return true;
          }

          @Override
          public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
            loadWindowBox(dataSet, dataSet);
          }
        };


        // ask asynchronous loading of data
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadRsetAndRsm(dataSet);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
      }
    }
  }


  @Override
  public void updateEnabled(AbstractNode[] selectedNodes) {
    int nbSelectedNodes = selectedNodes.length;


    if (nbSelectedNodes == 0) {
      setEnabled(false);
      return;
    }

    for (int i=0;i<nbSelectedNodes;i++) {
      AbstractNode node = selectedNodes[i];
      if (node.getType() != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
        setEnabled(false);
        return;
      }

      DataSetNode dataSetNode = (DataSetNode) node;
      if (! dataSetNode.hasResultSummary()) {
        setEnabled(false);
        return;
      }
    }


    setEnabled(true);
  }
}
