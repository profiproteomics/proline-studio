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



import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;


/**
 * Display Protein Matches of a Search Result (rset)
 * @author JM235353
 */
public class DisplayRsetProteinMatchesAction extends AbstractRSMAction {

   public DisplayRsetProteinMatchesAction(AbstractTree tree) {
       super("Proteins", tree);
   }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

            actionImpl(dataSetNode);
        }

    }
   
    private void actionImpl(DataSetNode dataSetNode) {

        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();

        if (!dataSetNode.hasResultSet()) {
            return; // should not happen
        }

        ResultSet rset = dataSetNode.getResultSet();


        if (rset != null) {

            ResultSet.Type rsType = rset.getType();
            boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
            
            // prepare window box
            WindowBox wbox = WindowBoxFactory.getProteinMatchesForRsetWindowBox(dataSet.getName(), false, mergedData);
            wbox.setEntryData(dataSet.getProject().getId(), rset);



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
                    
                    ResultSet rset = dataSet.getResultSet();
                    ResultSet.Type rsType = rset.getType();
                    boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
                    
                    WindowBox wbox = WindowBoxFactory.getProteinMatchesForRsetWindowBox(dataSet.getName(), false, mergedData);
                    // open a window to display the window box
                    DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                    WindowManager.getDefault().getMainWindow().displayWindow(win);
                    
                    // prepare window box
                    wbox.setEntryData(dataSet.getProject().getId(), rset);
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
            if (! dataSetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }
        }

        
        setEnabled(true);

    }
    
}