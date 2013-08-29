package fr.proline.studio.pattern;


import fr.proline.studio.dpm.task.ComputeSCTask;
import fr.proline.studio.rsmexplorer.gui.WSCResultPanel;

/**
 * RSM Weighted Spectral Count result
 * @author JM235353
 */
public class DataBoxRsmWSC extends AbstractDataBox {

    private ComputeSCTask.WSCResultData m_wscResult;
    
    public DataBoxRsmWSC() {
        
        // Name of this databox
        m_name = "Weighted SC result";
        m_description = "Weighted Spectral Count result";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ComputeSCTask.WSCResultData.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple ProteinMatch
//        GroupParameter outParameter = new GroupParameter();
//        outParameter.addParameter(DProteinMatch.class, true);
//        registerOutParameter(outParameter);
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

        final ComputeSCTask.WSCResultData _scResult = (m_wscResult != null) ? m_wscResult : (ComputeSCTask.WSCResultData) m_previousDataBox.getData(false, ComputeSCTask.WSCResultData.class);
        if (_scResult == null) {
            ((WSCResultPanel)m_panel).setData(null, null);
            m_wscResult = null;
            return;
        }
        
        
        m_wscResult = _scResult;
        
        final int loadingId = setLoading();        
        
        ((WSCResultPanel)m_panel).setData(m_wscResult, null); //JPM.TODO
        setLoaded(loadingId);
    }
    
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ComputeSCTask.WSCResultData.class)) {
                if (m_wscResult != null) {
                    return m_wscResult;
                }
            }
        }
        return super.getData(getArray, parameterType);
    }
    
        @Override
    public void setEntryData(Object data) {
        m_wscResult = (ComputeSCTask.WSCResultData) data;
        dataChanged();
    }
    
}
