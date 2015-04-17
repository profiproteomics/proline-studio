package fr.proline.studio.rsmexplorer.actions.identification;


import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve previously computed spectral count and display it
 * @author VD225637
 * 
 */
public class RetrieveSCDataAction extends AbstractRSMAction {
 
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
  
    public RetrieveSCDataAction() {
        super(NbBundle.getMessage(RetrieveSCDataAction.class, "CTL_RetrieveSCAction"), AbstractTree.TreeType.TREE_QUANTITATION);
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) { 
        
        // Only Ref should be selected to compute SC         
        final DataSetNode refDatasetNode = (DataSetNode) selectedNodes[0];
        Map<String,Object> params = new HashMap();
        
        ArrayList<DDataset> datasetList = new ArrayList<>();        
        datasetList.add(refDatasetNode.getDataset()); //first and single entry is Reference Dataset in data box !
        params.put(SpectralCountAction.DS_LIST_PROPERTIES, datasetList);
        WindowBox wbox = WindowBoxFactory.getRsmWSCWindowBox(refDatasetNode.getDataset().getName()+" WSC", true) ;
        wbox.setEntryData(refDatasetNode.getDataset().getProject().getId(), params);

        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive(); 
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
         if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;
        
        if (! datasetNode.isQuantSC()) {
            setEnabled(false);
            return;
        }
        
        setEnabled(true);        
    }

}
