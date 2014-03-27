package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.dialog.SetQuantitationDSNameDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.TreeSelectionDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class SpectralCountAction extends AbstractRSMAction {
 
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    protected TreeSelectionDialog m_treeSelectionDialog;
  
    final static public String DS_NAME_PROPERTIES="dsName";
    final static public String DS_DESCRIPTION_PROPERTIES="dsDescription";
    final static public String DS_LIST_PROPERTIES="dsList";
    
    public SpectralCountAction() {
        super(NbBundle.getMessage(SpectralCountAction.class, "CTL_CompareWithSCAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, final int x, final int y) { 
     
        // Only Ref should be selected to compute SC         
        final RSMDataSetNode refDatasetNode = (RSMDataSetNode) selectedNodes[0];
        final SetQuantitationDSNameDialog qttDSDialog = SetQuantitationDSNameDialog.getDialog(WindowManager.getDefault().getMainWindow());
        qttDSDialog.setLocation(x, y);

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.debug(" Choose RSM for Weighted SC");

                // check if we can compute SC
                String error = null;
                Map<String,Object> params = new HashMap();
                ArrayList<DDataset> datasetList = new ArrayList<>();        
                datasetList.add(refDatasetNode.getDataset()); //first entry is Reference Dataset in data box !
                
                if (m_treeSelectionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                   ArrayList<RSMDataSetNode> selectedDSNodes = m_treeSelectionDialog.getSelectedRSMDSNodeList();
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
                        params.put(DS_LIST_PROPERTIES,datasetList);
                   }
                   
                      
                   qttDSDialog.setVisible(true);
                    if (qttDSDialog.getButtonClicked() == SetQuantitationDSNameDialog.BUTTON_OK) {
                        params.put(DS_NAME_PROPERTIES, qttDSDialog.getQuantiDSName());
                        params.put(DS_DESCRIPTION_PROPERTIES, qttDSDialog.getQuantiDSDescriptionName());
                    }  else { //Cancel / Close was clicked
                        return;                                                
                    }
                   
                } else { //Cancel / Close was clicked
                    return;                                                
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(RSMTree.getCurrentTree(), error, "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                m_logger.debug(" Will Compute SC on "+(datasetList.size())+" RSMs : "+datasetList);
                
                WindowBox wbox = WindowBoxFactory.getRsmWSCWindowBox((String)params.get(DS_NAME_PROPERTIES), false) ;
                wbox.setEntryData(refDatasetNode.getDataset().getProject().getId(), params);

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive(); 
                
            }
        };
        
        //Create Child Tree to select RSM to compute SC for
        final RSMTree childTree = RSMTree.getCurrentTree().copyDataSetRootSubTree(refDatasetNode.getDataset(),  refDatasetNode.getDataset().getProject().getId());
        m_treeSelectionDialog = new TreeSelectionDialog(WindowManager.getDefault().getMainWindow(), childTree, "Select Identification Summaries for Spectral Count", 380, 500);
        m_treeSelectionDialog.setButtonName(TreeSelectionDialog.BUTTON_OK, "Next");
        m_treeSelectionDialog.setLocation(x, y);   
        m_treeSelectionDialog.setVisible(true);  

        RSMTree.getCurrentTree().loadInBackground(refDatasetNode, callback);

    
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
