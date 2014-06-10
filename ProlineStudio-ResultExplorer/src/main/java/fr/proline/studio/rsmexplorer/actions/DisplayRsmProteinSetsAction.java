package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
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
 * Display Protein Sets of an Identification Summary (rsm)
 * @author JM235353
 */
public class DisplayRsmProteinSetsAction extends AbstractRSMAction {

    public DisplayRsmProteinSetsAction() {
        super(NbBundle.getMessage(DisplayRsmProteinSetsAction.class, "CTL_DisplayProteinSetsAction"));
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[i];

            actionImpl(dataSetNode);
        }

    }

    private void actionImpl(RSMDataSetNode dataSetNode) {
        
        final DDataset dataset = ((DataSetData) dataSetNode.getData()).getDataset();

        if (!dataSetNode.hasResultSummary()) {
            return; // should not happen
        }

        ResultSummary rsm = dataSetNode.getResultSummary();
        if (rsm != null) {

            // prepare window box
            WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox(dataset.getName()+" Protein Sets", false);
            wbox.setEntryData(dataset.getProject().getId(), rsm);


            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
        } else {
            // we have to load the result summary

            final WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox(dataset.getName()+" Protein Sets", false);
            
            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    // prepare window box
                    
                    wbox.setEntryData(dataset.getProject().getId(), dataset.getResultSummary());


                    
                }
            };


            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initLoadRsetAndRsm(dataset);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


        }



    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        

        if (nbSelectedNodes <0) {
            setEnabled(false);
            return;
        }
        
        for (int i=0;i<nbSelectedNodes;i++) {
            RSMNode node = selectedNodes[i];
            if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
                setEnabled(false);
                return;
            }

            RSMDataSetNode dataSetNode = (RSMDataSetNode) node;
            if (! dataSetNode.hasResultSummary()) {
                setEnabled(false);
                return;
            }
        }

        
        setEnabled(true);

    }
}