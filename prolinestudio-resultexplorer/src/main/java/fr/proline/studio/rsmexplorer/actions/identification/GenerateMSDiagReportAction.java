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
package fr.proline.studio.rsmexplorer.actions.identification;

import java.util.ArrayList;
import java.util.HashMap;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.MSDiagDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;


import fr.proline.studio.WindowManager;

/**
 * Action to launch the msdiag generation of the report. action
 * Display (Result Set) - Quality Control
 *
 * @author AW
 */
public class GenerateMSDiagReportAction extends AbstractRSMAction {

    public GenerateMSDiagReportAction(AbstractTree tree) {
        super("Quality Control...", tree);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        int nbNodes = selectedNodes.length;
        //dialog for  parameter section
        MSDiagDialog dialog = MSDiagDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        HashMap<String, String> msdiagParams = null;
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            // retrieve parameters
            msdiagParams = dialog.getMSDiagSettings();
        } else {
            return;
        }
        ArrayList resultMessages = new ArrayList(0);
        resultMessages.add(msdiagParams);
        //display action
        for (int i = 0; i < nbNodes; i++) {
            final DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];
            actionImpl(dataSetNode, resultMessages);
        }
    }

    private void actionImpl(DataSetNode dataSetNode, ArrayList resultMessages) {
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        if (!dataSetNode.hasResultSet()) {
            return; // should not happen
        }

        ResultSet rset = dataSetNode.getResultSet();
        // final boolean hasResultSummary = dataSetNode.hasResultSummary();

        if (rset != null) {
            // prepare window box
            //WindowBox wbox = WindowBoxFactory.getMSDiagWindowBox(dataSet.getName()+" MSDiag" , resultMessages.get(0).toString());
            WindowBox wbox = WindowBoxFactory.getMSDiagWindowBox(dataSet.getName() + " Quality Control", (HashMap<String, String>) resultMessages.get(0));
            wbox.setEntryData(dataSet.getProject().getId(), rset);

            // open a window to display the window box
            DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
            WindowManager.getDefault().getMainWindow().displayWindow(win);
        } else {

            final WindowBox wbox = WindowBoxFactory.getMSDiagWindowBox(dataSet.getName() + " Quality Control", (HashMap<String, String>) resultMessages.get(0));
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
                    wbox.setEntryData(dataSet.getProject().getId(), dataSet.getResultSet());
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

        if (nbSelectedNodes < 0) {
            setEnabled(false);
            return;
        }

        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET && node.getType() != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) {
                setEnabled(false);
                return;
            }

            DataSetNode dataSetNode = (DataSetNode) node;
            if (!dataSetNode.hasResultSet()) {
                setEnabled(false);
                return;
            }

            if (node.getType() == AbstractNode.NodeTypes.DATA_SET && !dataSetNode.isLeaf()) {
                setEnabled(false);
                return;
            }

        }

        setEnabled(true);

    }
}
