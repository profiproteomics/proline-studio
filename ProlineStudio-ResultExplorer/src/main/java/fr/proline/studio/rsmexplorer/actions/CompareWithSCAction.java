package fr.proline.studio.rsmexplorer.actions;


import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.dialog.TreeSelectionDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to start a spectral count calculation and display
 * @author VD225637
 */
public class CompareWithSCAction extends AbstractRSMAction {
 
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    protected TreeSelectionDialog treeSelectionDialog;
  
    public CompareWithSCAction() {
        super(NbBundle.getMessage(CompareWithSCAction.class, "CTL_CompareWithSCAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
     
        // Onlt Ref should be selected to compute SC         
        final RSMDataSetNode refDatasetNode = (RSMDataSetNode) selectedNodes[0];

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.debug(" Choose RSM for Weighted SC");
//                treeSelectionDialog.setBusy(false);

                // check if we can compute SC
                String error = null;
                ArrayList<Dataset> datasetList = new ArrayList<>();        
                datasetList.add(refDatasetNode.getDataset());
                
                if (treeSelectionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                   ArrayList<RSMDataSetNode> selectedDSNodes = treeSelectionDialog.getSelectedRSMDSNodeList();
                   //A Voir :) 
//                   if(selectedDSNodes.contains(refDatasetNode) && selectedDSNodes.size()>1) 
//                        error = " Spectral Count is not possible on reference Node and some of its child";
                       
                   for (RSMDataSetNode dsNode : selectedDSNodes) {
                       //TODO : Verif pas père + fils sélectionnés : Que père ou que fils ?!? 
                        if (!dsNode.hasResultSummary()) {
                            error = " Spectral Count is not possible on Search result ("+ dsNode.getDataset().getName() +").  Identification Summary should be created first";
                            break;                       
                        }
                        if(dsNode.isChanging()){
                            error = " Spectral Count is not possible while import or validation is on going. (Search result "+ dsNode.getDataset().getName() +")";
                            break;  
                        }
                        datasetList.add(dsNode.getDataset());
                   }
                } else { //Cancel / Close was clicked
                    return;                                                
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(RSMTree.getTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                m_logger.debug(" Will Compute SC on "+(datasetList.size()-1)+" RSMs : "+datasetList);
                
                WindowBox wbox = WindowBoxFactory.getRsmWSCWindowBox(refDatasetNode.getDataset().getName()+" WSC") ;
                wbox.setEntryData(refDatasetNode.getDataset().getProject().getId(), datasetList);

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive(); 
                
            }
        };
        
        //Create Child Tree to select RSM to compute SC for
        final RSMTree childTree = RSMTree.getTree().copyDataSetRootSubTree(refDatasetNode.getDataset(),  refDatasetNode.getDataset().getProject().getId());
        treeSelectionDialog = new TreeSelectionDialog(WindowManager.getDefault().getMainWindow(), childTree, "Select Identification Summaries for Spectral Count", 380, 500);
        treeSelectionDialog.setLocation(x, y);   
        treeSelectionDialog.setVisible(true);  
        
        RSMTree.getTree().loadInBackground(refDatasetNode, callback);

    
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        
         if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        RSMNode node = (RSMNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        RSMDataSetNode datasetNode = (RSMDataSetNode) node;
        
        Dataset.DatasetType datasetType = ((DataSetData) datasetNode.getData()).getDatasetType();
        if (datasetType != Dataset.DatasetType.AGGREGATE) {
            setEnabled(false);
            return;
        }

        if (!datasetNode.hasResultSummary()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }

}
