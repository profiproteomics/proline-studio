package fr.proline.studio.pattern;


import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.data.SpectralCountResultData;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.dpm.task.RetrieveSpectralCountTask;
import fr.proline.studio.dpm.task.SpectralCountTask;
import fr.proline.studio.rsmexplorer.actions.SpectralCountAction;
import fr.proline.studio.rsmexplorer.gui.WSCResultPanel;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RSMs Weighted Spectral Count result DataBox.
 * @author JM235353
 */
public class DataBoxRsmWSC extends AbstractDataBox {

    private DDataset m_refDataset = null;
    private ArrayList<DDataset> m_datasetRsms = null;
    private String m_qttDSName = null;
    private String m_qttDSDescr = null;
    private boolean m_readData;
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
     /**
     * 
     * @param name : title of the created Windows 
     * @param readData : specify if the spectral count to display has to be retrieve from computing (false)
     * or read back operation (true)
     * @return 
     */
    public DataBoxRsmWSC(boolean readData) {
        
        // Name of this databox
        m_name = "Weighted SC result";
        m_description = "Weighted Spectral Count result";
        
        m_readData = readData;
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(SpectralCountResultData.class, false);
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
        
        // Used in acse of computing SC
        final Long[] _quantiDatasetId = new Long[1];
        
        //Used in case of read back SC
        final Long[] _refIdfRSMId = new Long[1];
        final Long[] _refIdfDSId = new Long[1];
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                m_logger.debug(" Get Weighted SC");
                if (success) {
                    
                    if (m_readData) {
                        Long refIdfRSM = _refIdfRSMId[0];
                        Long refDSRSM = _refIdfDSId[0];
                        String scResultAsJson = _spCountJSON[0];
                        
                        final SpectralCountResultData readScResult = new SpectralCountResultData(scResultAsJson, m_refDataset.getProject());

                        m_logger.debug(" READ SC from DS with ID " + m_refDataset.getId() + " FROM IDF RSM " + refIdfRSM + " and DS " + refDSRSM);
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                                if (!success) {
                                   ((WSCResultPanel)m_panel).setData(null); 
                                }
                                    ((WSCResultPanel)m_panel).setData(readScResult);

                            }
                        };
                        
                        readScResult.loadData(refDSRSM, callback);
                        System.out.println(scResultAsJson);

                    } else {
                        String scResultAsJson = _spCountJSON[0];
                        SpectralCountResultData scResult = new SpectralCountResultData(m_refDataset, m_datasetRsms, scResultAsJson, m_refDataset.getProject());
                        ((WSCResultPanel)m_panel).setData(scResult); 
                    }                                     
                } else {
                    ((WSCResultPanel)m_panel).setData(null);
                }
                
                setLoaded(loadingId);
            }
        };
                      
        AbstractServiceTask task;
        if(m_readData){
            task = new RetrieveSpectralCountTask(callback, m_refDataset, _refIdfRSMId, _refIdfDSId, _spCountJSON);          
        } else {
            task = new SpectralCountTask(callback,  m_refDataset, m_datasetRsms, m_qttDSName, m_qttDSDescr, _quantiDatasetId, _spCountJSON);
        }            
        AccessServiceThread.getAccessServiceThread().addTask(task);

    }
    
    
    @Override
    public void setEntryData(Object data) {
        if (!Map.class.isAssignableFrom(data.getClass())) {
            throw new IllegalArgumentException("Specified parameter (" + data + ") should be a Map.");
        }

        ArrayList<DDataset> datasetArray = (ArrayList) ((Map) data).get(SpectralCountAction.DS_LIST_PROPERTIES);
        m_refDataset = datasetArray.get(0);
        if (!m_readData) {
            int nb = datasetArray.size() - 1;
            m_datasetRsms = new ArrayList<>(nb);
            for (int i = 1; i <= nb; i++) {
                m_datasetRsms.add(datasetArray.get(i));
            }
        
            m_qttDSName = (String) ((Map) data).get(SpectralCountAction.DS_NAME_PROPERTIES);
            m_qttDSDescr = (String) ((Map) data).get(SpectralCountAction.DS_DESCRIPTION_PROPERTIES);            
        }
        dataChanged();
    }
    
}
