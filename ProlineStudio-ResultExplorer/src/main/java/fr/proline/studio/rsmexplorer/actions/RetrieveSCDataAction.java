package fr.proline.studio.rsmexplorer.actions;


import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
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
        super(NbBundle.getMessage(RetrieveSCDataAction.class, "CTL_RetrieveSCAction"), false);
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
        
        // Only Ref should be selected to compute SC         
        final RSMDataSetNode refDatasetNode = (RSMDataSetNode) selectedNodes[0];
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
        if (datasetType != Dataset.DatasetType.QUANTITATION) {
            setEnabled(false);
            return;
        }
        
        DDataset d =  ((DataSetData) datasetNode.getData()).getDataset();
        QuantitationMethod quantitationMethod = d.getQuantitationMethod();
        if (quantitationMethod == null) {
            setEnabled(false);
            return;
        }
        
        if (quantitationMethod.getAbundanceUnit().compareTo("spectral_counts") != 0) { // Spectral count
            setEnabled(false);
            return;
        } 
        
        setEnabled(true);        
    }

}
