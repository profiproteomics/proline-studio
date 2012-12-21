/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;



import fr.proline.core.orm.msi.ResultSummary;
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
import fr.proline.studio.rsmexplorer.node.RSMResultSummaryNode;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class ProteinGroupsAction extends AbstractRSMAction {

   //private static ProteinGroupsAction instance = null;

   public ProteinGroupsAction() {
       super(NbBundle.getMessage(ProteinGroupsAction.class, "CTL_ProteinGroupsAction"));
   }

    @Override
    public void actionPerformed(RSMNode n) {
        
        RSMDataSetNode dataSetNode = (RSMDataSetNode) n;

        final DataSetTMP dataSet = ((DataSetData) dataSetNode.getData()).getDataSet();
                
        if (! dataSetNode.hasResultSummary()) {
            return; // should not happen
        }
        
        ResultSummary rsm = dataSetNode.getResultSummary();
        if (rsm != null) {

            // prepare window box
            WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox();
            wbox.setEntryData(rsm);


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
                    wbox.setEntryData(dataSet.getTransientData().getResultSummary() );


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
   
   /*public static ProteinGroupsAction getInstance() {
      if (instance == null) {
         instance = new ProteinGroupsAction();
      }
      return instance;
   }


    @Override
    protected void performAction(Node[] nodes) {
        /*if ((nodes == null) || (nodes.length != 1)) {
            // should never happen
            return;
        }
        
        RSMResultSummaryNode node = (RSMResultSummaryNode) nodes[0];
        final ResultSummary rsm = node.getResultSummary();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                DataViewerTopComponent viewer = (DataViewerTopComponent) WindowManager.getDefault().findTopComponent("DataViewerTopComponent");
                viewer.open();
                viewer.requestActive();

                ProteinSet[] proteinSetArray = rsm.getTransientProteinSets();
                viewer.setSelectedResultSummary(proteinSetArray);
            }
        };
        

        // ask asynchronous loading of data
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseProteinSetsTask(callback, rsm));
*/ //JPM.TODO
        
  /*  }

    @Override
    protected boolean enable(Node[] nodes) {
        
/*
        // a node must be selected
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));
        
        if (actionEnabled) {
            RSMNode node = (RSMNode) nodes[0];
            actionEnabled =  node.searchChildNodeOfAType(RSMNode.NodeTypes.RESULT_SUMMARY);
        }
        
        return actionEnabled;*/ //JPM.TODO
   /*     return true;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
            

    
}