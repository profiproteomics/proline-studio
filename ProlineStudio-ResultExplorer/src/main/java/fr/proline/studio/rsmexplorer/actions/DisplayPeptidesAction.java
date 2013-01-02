package fr.proline.studio.rsmexplorer.actions;



import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadDataSetTask;
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
public class DisplayPeptidesAction extends AbstractRSMAction {

   //private static ProteinGroupsAction instance = null;

   public DisplayPeptidesAction() {
       super(NbBundle.getMessage(DisplayPeptidesAction.class, "CTL_DisplayPeptidesAction"));
   }

    @Override
    public void actionPerformed(RSMNode n) {
        
        RSMDataSetNode dataSetNode = (RSMDataSetNode) n;
        
        final DataSetTMP dataSet = ((DataSetData) dataSetNode.getData()).getDataSet();
        
        if (! dataSetNode.hasResultSet()) {
            return; // should not happen
        }
        
        ResultSet rset = dataSetNode.getResultSet();
        
        
        
        if (rset != null) {
        
            // prepare window box
            WindowBox wbox = WindowBoxFactory.getPeptidesWindowBox();
            wbox.setEntryData(rset);


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
                    WindowBox wbox = WindowBoxFactory.getPeptidesWindowBox();
                    wbox.setEntryData(dataSet.getTransientData().getResultSet() );


                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
                }
            };


            // ask asynchronous loading of data
            AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadDataSetTask(callback, dataSet));

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