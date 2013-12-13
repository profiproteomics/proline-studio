package fr.proline.studio.rsmexplorer.actions;



import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.Dataset;
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
 * Display Protein Matches of a Search Result (rset)
 * @author JM235353
 */
public class DisplayRsetProteinMatchesAction extends AbstractRSMAction {

   public DisplayRsetProteinMatchesAction() {
       super(NbBundle.getMessage(DisplayRsetPeptidesAction.class, "CTL_DisplayProteinMatchesAction"));
   }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        // only one node selected for this action
        RSMDataSetNode dataSetNode = (RSMDataSetNode) selectedNodes[0];
        
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        
        if (! dataSetNode.hasResultSet()) {
            return; // should not happen
        }
        
        ResultSet rset = dataSetNode.getResultSet();

        
        if (rset != null) {
        
            // prepare window box
            WindowBox wbox =  WindowBoxFactory.getProteinMatchesForRsetWindowBox(dataSet.getName()+" Proteins", false);
            wbox.setEntryData(dataSet.getProject().getId(), rset);
            


            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
        } else {
            
            final WindowBox wbox = WindowBoxFactory.getProteinMatchesForRsetWindowBox(dataSet.getName()+" Proteins", false);
            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
            
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
        
        setEnabled(dataSetNode.hasResultSet());

    }
    
}