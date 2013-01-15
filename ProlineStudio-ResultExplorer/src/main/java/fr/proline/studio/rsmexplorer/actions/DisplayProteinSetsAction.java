package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class DisplayProteinSetsAction extends AbstractRSMAction {

    //private static DisplayProteinSetsAction instance = null;
    public DisplayProteinSetsAction() {
        super(NbBundle.getMessage(DisplayProteinSetsAction.class, "CTL_DisplayProteinSetsAction"));
    }

    @Override
    public void actionPerformed(RSMNode n, int x, int y) {

        RSMDataSetNode dataSetNode = (RSMDataSetNode) n;

        final DataSetTMP dataSet = ((DataSetData) dataSetNode.getData()).getDataSet();

        if (!dataSetNode.hasResultSummary()) {
            return; // should not happen
        }

        ResultSummary rsm = dataSetNode.getResultSummary();
        if (rsm != null) {

            // prepare window box
            WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox();
            wbox.setEntryData(dataSet.getProjectId(), rsm);


            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
        } else {
            // we have to load the result summary

            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask) {
                    // prepare window box
                    WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox();
                    wbox.setEntryData(dataSet.getProjectId(), dataSet.getTransientData().getResultSummary());


                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initLoadRsetAndRsm(dataSet);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


        }



    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // we disallow to display multiple peptides window
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }

        RSMNode node = selectedNodes[0];
        if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        RSMDataSetNode dataSetNode = (RSMDataSetNode) node;

        setEnabled(dataSetNode.hasResultSummary());

    }
}