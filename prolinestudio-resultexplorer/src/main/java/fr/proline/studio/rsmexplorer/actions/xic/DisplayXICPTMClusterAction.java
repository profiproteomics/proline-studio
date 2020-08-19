/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;


/**
 *
 * @author VD225637
 */
public class DisplayXICPTMClusterAction extends AbstractRSMAction {

    public DisplayXICPTMClusterAction(AbstractTree tree) {
       super("Modification Clusters", tree);
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
      //  int answer= JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), "Do you want to \n - View Protein Sites in V2 ? Click on YES \n - View PTM Clusters ? Click on No \n - View PTM Sites in V1 ? Click on Cancel.");
        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];
            actionImpl(dataSetNode);
        }

    }
    
    private void actionImpl(DataSetNode dataSetNode) {
        
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        
        if (!dataSetNode.hasResultSummary()) {
            return; // should not happen
        }
        
        ResultSummary rsm = dataSetNode.getResultSummary();
        if (rsm != null) {

            // prepare window box
            WindowBox wbox = WindowBoxFactory.getXicPTMDataWindowBox(dataSet.getName(), false); ;
            wbox.setEntryData(dataSet.getProject().getId(), new PTMDataset(dataSet));    
               
            // open a window to display the window box
            DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
            WindowManager.getDefault().getMainWindow().displayWindow(win);
            
        } else {

            
            // we have to load the result set
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    WindowBox wbox = WindowBoxFactory.getXicPTMDataWindowBox(dataSet.getName(), false);

                    // open a window to display the window box
                    DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                    WindowManager.getDefault().getMainWindow().displayWindow(win);
                    
                    // prepare window box
                    wbox.setEntryData(dataSet.getProject().getId(), new PTMDataset(dataSet));
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initLoadRsetAndRsm(dataSet);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        int nbSelectedNodes = selectedNodes.length;

        if (nbSelectedNodes <0) {
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
            
             // must be a quantitation 
            if (! (dataSetNode.isQuantitation() && !dataSetNode.isQuantSC()) ) {
                setEnabled(false);
                return;
            }
        }
        
        setEnabled(true);
    }
    
    
}
