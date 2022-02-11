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
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.*;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.DataBoxChooserDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;

import java.util.ArrayList;
import fr.proline.studio.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to display the dialog to choose a view (databox) for a user window
 * @author JM235353
 */
public class DisplayUserWindowAction extends AbstractRSMAction {
    
    private final char m_windowType;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public DisplayUserWindowAction(char windowType, AbstractTree tree) {
        super("New User Window...", tree);
        m_windowType = windowType;
    }
    
    private boolean forRsm(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM;
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        // only one node selected for this action
        DataSetNode dataSetNode = (DataSetNode) selectedNodes[0];
        
        ArrayList<ParameterList> outParameters = new ArrayList<>();
        if ((!forRsm()) && (dataSetNode.hasResultSet())) {
            ParameterList outParameter = new ParameterList();
            outParameter.addParameter(ResultSet.class);
            outParameters.add(outParameter);
        }
        if ((forRsm()) && (dataSetNode.hasResultSummary())) {
            ParameterList outParameter = new ParameterList();
            outParameter.addParameter(ResultSummary.class);
            outParameters.add(outParameter);
        }
        if (m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI && dataSetNode.isQuantitation()){
            ParameterList outParameter = new ParameterList();
            outParameter.addParameter(DDataset.class);
            outParameters.add(outParameter);
        }

        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();

        DataBoxChooserDialog dialog = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), outParameters, true);
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            AbstractDataBox genericDatabox = dialog.getSelectedDataBox();
            AbstractDataBox databox = null;
            try {

                databox = DataboxManager.getDataboxNewInstance(genericDatabox);


                final WindowBox wbox = WindowBoxFactory.getUserDefinedWindowBox(dataSet.getName(), dataSet.getName()+" "+dialog.getWndTitle(), databox, false, dataSetNode.isQuantitation() && !dataSetNode.isQuantSC(), m_windowType);

                if (forRsm()) {
                    
                    // --- RSM
                    
                    ResultSummary rsm = dataSetNode.getResultSummary();



                    if (rsm != null) {


                        wbox.setEntryData(dataSet.getProject().getId(), rsm);

                        // open a window to display the window box
                        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                        WindowManager.getDefault().getMainWindow().displayWindow(win);

                    } else {

                        // open a window to display the window box
                        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                        WindowManager.getDefault().getMainWindow().displayWindow(win);

                        // we have to load the result set
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                // prepare window box
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getResultSummary());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadRsetAndRsm(dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                } else if (m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET){
                    // --- RSET


                    ResultSet rset = dataSetNode.getResultSet();


                    if (rset != null) {

                        wbox.setEntryData(dataSet.getProject().getId(), rset);



                        // open a window to display the window box
                        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                        WindowManager.getDefault().getMainWindow().displayWindow(win);
                    } else {

                        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                        WindowManager.getDefault().getMainWindow().displayWindow(win);

                        // we have to load the result set
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                // prepare window box
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getResultSet());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadRsetAndRsm(dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                }else if (m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI){
                    // --- QUANTI
                    DDataset ds = dataSetNode.getDataset();


                    if (ds != null) {

                        wbox.setEntryData(dataSet.getProject().getId(), ds);



                        // open a window to display the window box
                        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                        WindowManager.getDefault().getMainWindow().displayWindow(win);
                    } else {

                        DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                        WindowManager.getDefault().getMainWindow().displayWindow(win);

                        // we have to load the result set
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                // prepare window box
                                wbox.setEntryData(dataSet.getProject().getId(), dataSet.getResultSet());
                            }
                        };


                        // ask asynchronous loading of data
                        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
                        task.initLoadQuantitation(dataSet.getProject(), dataSet);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                    }
                }


            } catch (InstantiationException | IllegalAccessException e) {
                // should never happen
                m_logger.error("Error creating new Databox ",e);
            }
         }

        
        
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
         int nbSelectedNodes = selectedNodes.length;

        // only one node can be selected
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
         AbstractNode node = selectedNodes[0];
        if (node.getType() != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
            setEnabled(false);
            return;
        }
        
        DataSetNode dataSetNode = (DataSetNode) node;

        if (dataSetNode.isFolder()) {
            setEnabled(false);
            return;
        }

        if ((m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET) && (!dataSetNode.hasResultSet())) {
            setEnabled(false);
            return;
        }

        if ((forRsm()) && (!dataSetNode.hasResultSummary())) {
            setEnabled(false);
            return;
        }
        
        if ((m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI && (!dataSetNode.isQuantitation())) ) {
            setEnabled(false);
            return;
        }
        
        setEnabled(true);
    }
    
}
