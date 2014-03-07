package fr.proline.studio.rsmexplorer.actions;


import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.RetrieveSpectralCountTask;
import fr.proline.studio.dpm.task.SpectralCountTask;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.jfree.data.general.Dataset;
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
        super(NbBundle.getMessage(RetrieveSCDataAction.class, "CTL_RetrieveSCAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
     
        // Onlt Ref should be selected to compute SC         
        final RSMDataSetNode refDatasetNode = (RSMDataSetNode) selectedNodes[0];
        final String[] _spCountJSON = new String[1];
        final Long[] _refIdfRSMId = new Long[1];
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                m_logger.debug(" Read Weighted SC");
                if (success) {
                
                    m_logger.debug(" Will GET SC from "+refDatasetNode.getDataset().getType()+" with ID "+ refDatasetNode.getDataset().getId());
                    String scResultAsJson = _spCountJSON[0];
                    Long refIdfRSM = _refIdfRSMId[0];
                    SpectralCountTask.WSCResultData scResult = new SpectralCountTask.WSCResultData(null, null, scResultAsJson);
                    System.out.println(scResultAsJson);
                } else {
                     System.out.println("ERROR ");
                }
                    
        
            }
        };
        RetrieveSpectralCountTask task = new RetrieveSpectralCountTask(callback, refDatasetNode.getDataset(), _refIdfRSMId, _spCountJSON);
        AccessServiceThread.getAccessServiceThread().addTask(task);
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
        setEnabled(true);        
    }

}
