package fr.proline.studio.pattern;


import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.SpectralCountTask;
import fr.proline.studio.rsmexplorer.gui.WSCResultPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * RSM Weighted Spectral Count result
 * @author JM235353
 */
public class DataBoxRsmWSC extends AbstractDataBox {

    private DDataset m_refDataset = null;
    private ArrayList<DDataset> m_datasetRsms = null;
    
    public DataBoxRsmWSC() {
        
        // Name of this databox
        m_name = "Weighted SC result";
        m_description = "Weighted Spectral Count result";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(SpectralCountTask.WSCResultData.class, false);
        registerInParameter(inParameter);

    }
    
    @Override
    public void createPanel() {
        WSCResultPanel p = new WSCResultPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {

        final int loadingId = setLoading(true); 
        
        // used as out parameter for the service
        final String[] _spCountJSON = new String[1];
        final Long[] _quantiDatasetId = new Long[1];
        AbstractServiceCallback callback = new AbstractServiceCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    
                    String scResultAsJson = _spCountJSON[0];
                    SpectralCountTask.WSCResultData scResult = new SpectralCountTask.WSCResultData(m_refDataset, m_datasetRsms, scResultAsJson);
                    
                    ((WSCResultPanel)m_panel).setData(scResult);                  
                } else {
                    ((WSCResultPanel)m_panel).setData(null);
                }
                
                setLoaded(loadingId);
            }
        };
                                
        SpectralCountTask task = new SpectralCountTask(callback,  m_refDataset, m_datasetRsms,_quantiDatasetId, _spCountJSON);

        AccessServiceThread.getAccessServiceThread().addTask(task);
 

    }
    
    
    @Override
    public void setEntryData(Object data) {
        
        ArrayList<DDataset> datasetArray = (ArrayList) data;
        m_refDataset = datasetArray.get(0);
        
        int nb = datasetArray.size()-1;
        m_datasetRsms = new ArrayList<>(nb);
        for (int i=1;i<=nb;i++) {
            m_datasetRsms.add(datasetArray.get(i));
        }

        dataChanged();
    }
    
}
