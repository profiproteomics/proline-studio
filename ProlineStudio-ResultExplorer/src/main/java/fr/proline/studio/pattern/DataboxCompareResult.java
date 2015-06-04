package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.ResultComparePanel;
import fr.proline.studio.table.GlobalTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DataboxCompareResult extends AbstractDataBox {

    private GlobalTableModelInterface m_entryModel = null;
    
    public DataboxCompareResult() {
        super(DataboxType.DataboxCompareResult);
        
        // Name of this databox
        m_typeName = "Compare Data";
        m_description = "Compare Data Result";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(CompareDataInterface.class, false);
        registerInParameter(inParameter);
        
        
        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

        outParameter = new GroupParameter();
        outParameter.addParameter(CrossSelectionInterface.class, true);
        registerOutParameter(outParameter);
        

        
        
    }
    
    @Override
    public void createPanel() {
        ResultComparePanel p = new ResultComparePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void setEntryData(Object data) {
        if (data instanceof GlobalTableModelInterface) {
            m_entryModel = (GlobalTableModelInterface) data;
        }
         dataChanged();
    }

    @Override
    public void dataChanged() {
        GlobalTableModelInterface dataInterface = m_entryModel;
        if (dataInterface == null) {
            dataInterface = (GlobalTableModelInterface) m_previousDataBox.getData(false, GlobalTableModelInterface.class);
        }

        ((ResultComparePanel) m_panel).setData(dataInterface);

    }
    
        @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface)m_panel).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
    
}
