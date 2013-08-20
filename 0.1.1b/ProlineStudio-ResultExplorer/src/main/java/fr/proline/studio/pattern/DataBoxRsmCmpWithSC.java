/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.rsmexplorer.gui.RsmProteinSetPanel;

/**
 *
 * @author VD225637
 */
public class DataBoxRsmCmpWithSC extends AbstractDataBox {
        
    private ResultSummary m_rsm = null;

    public DataBoxRsmCmpWithSC() {
        // Name of this databox
        m_name = "Weighted SC";
        m_description = "Identification Summaries comparison with Spectral Count";
  
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
     
        // Register possible out parameters
        // One or Multiple ProteinMatches
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ProteinMatch.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);

    }
    
     
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ProteinMatch.class)) {
//                return ((RsmProteinSetPanel)m_panel).getSelectedProteinSet();
            }
            
            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }
        }
        return super.getData(getArray, parameterType);
    }
    
    public void createPanel(){
        RsmProteinSetPanel p = new RsmProteinSetPanel(true);
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }
    
    public void dataChanged(){
        
        final ResultSummary _rsm = (m_rsm != null) ? m_rsm : (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);

        final int loadingId = setLoading();

        
    }
    
    @Override
    public void setEntryData(Object data) {
        m_rsm = (ResultSummary) data;
        dataChanged();
    }
}
