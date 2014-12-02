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
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.dialog.MSDiagDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;

import javax.swing.tree.DefaultTreeModel;

import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to launch the msdiag generation of the report. action
 * @author AW
 */
public class GenerateMSDiagReportAction extends AbstractRSMAction {

    public GenerateMSDiagReportAction() {
        super(NbBundle.getMessage(GenerateMSDiagReportAction.class, "CTL_GenerateMSDiagReportAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

    	IdentificationTree tree = IdentificationTree.getCurrentTree();
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        
    	

        
        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            final DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

           // actionImpl(dataSetNode);
         //   AbstractServiceCallback callback = new AbstractServiceCallback() {

//                @Override
//                public boolean mustBeCalledInAWT() {
//                    return true;
//                }
//
//                @Override
//                public void run(boolean success) {
//                	dataSetNode.setIsChanging(false);
//                    treeModel.nodeChanged(dataSetNode);
//                }
//            };

            MSDiagDialog dialog = MSDiagDialog.getDialog(WindowManager.getDefault().getMainWindow());
            dialog.setLocation(x, y);
            dialog.setVisible(true);
            
            HashMap<String,String> msdiagParams = null;
            if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                
                 // retrieve parameters
            	
               msdiagParams = dialog.getMSDiagSettings();
              
            } else
            {
            	return;
            }
            
            ArrayList resultMessages = new ArrayList(0);
            resultMessages.add(msdiagParams);
            
           
            actionImpl(dataSetNode,resultMessages);
            
            // ---------------------------
        }

    }
    
    private void actionImpl(DataSetNode dataSetNode, ArrayList resultMessages) {
        
        final DDataset dataSet = ((DataSetData) dataSetNode.getData()).getDataset();
        
        if (! dataSetNode.hasResultSet()) {
            return; // should not happen
        }
        
        ResultSet rset = dataSetNode.getResultSet();
       // final boolean hasResultSummary = dataSetNode.hasResultSummary();
        
        
       
        
        if (rset != null) {
        
            // prepare window box
            //WindowBox wbox = WindowBoxFactory.getMSDiagWindowBox(dataSet.getName()+" MSDiag" , resultMessages.get(0).toString());
        	WindowBox wbox = WindowBoxFactory.getMSDiagWindowBox(dataSet.getName()+" MSDiag" , (HashMap<String,String>) resultMessages.get(0));
            wbox.setEntryData(dataSet.getProject().getId(), rset); 
            
            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
        } else {
            
            final WindowBox wbox = WindowBoxFactory.getMSDiagWindowBox(dataSet.getName()+" MSDiag" , (HashMap<String,String>) resultMessages.get(0)); 
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
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        

        if (nbSelectedNodes <0) {
            setEnabled(false);
            return;
        }
        
        for (int i=0;i<nbSelectedNodes;i++) {
            AbstractNode node = selectedNodes[i];
            if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
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

