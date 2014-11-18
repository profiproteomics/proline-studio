package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.rsmexplorer.gui.ResultComparePanel;

/**
 *
 * @author JM235353
 */
public class DataboxCompareResult extends AbstractDataBox {

    public DataboxCompareResult() {
        super(DataboxType.DataboxCompareResult);
        
        // Name of this databox
        m_name = "Compare Data";
        m_description = "Compare Data Result";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(CompareDataInterface.class, false);
        registerInParameter(inParameter);
    }
    
    @Override
    public void createPanel() {
        ResultComparePanel p = new ResultComparePanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        CompareDataInterface dataInterface = (CompareDataInterface) m_previousDataBox.getData(false, CompareDataInterface.class);

        ((ResultComparePanel) m_panel).setData(dataInterface);

    }
    
}
