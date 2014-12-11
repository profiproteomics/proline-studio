package fr.proline.studio.pattern;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.comparedata.DiffDataModel;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.SelectComparePanel;

/**
 *
 * @author JM235353
 */
public class DataboxSelectCompareData extends AbstractDataBox {

    private CompareDataInterface m_compareDataInterface1 = null;
    private CompareDataInterface m_compareDataInterface2 = null;
    
    public DataboxSelectCompareData() {
        super(DataboxType.DataboxSelectCompareData);

        // Name of this databox
        m_name = "Compare Data";

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        SelectComparePanel p = new SelectComparePanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;

    }

    @Override
    public void dataChanged() {

        ((SelectComparePanel) m_panel).setData(m_compareDataInterface1, m_compareDataInterface2);
        

    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((CompareDataProviderInterface) m_panel).getCompareDataInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((CompareDataProviderInterface)m_panel).getCrossSelectionInterface();
            }

        }
        return super.getData(getArray, parameterType); //JPM.TODO
    }

    @Override
    public void setEntryData(Object data) {
        if (m_compareDataInterface1 == null) {
            m_compareDataInterface1 = (CompareDataInterface) data;
            ((SelectComparePanel) m_panel).setData(m_compareDataInterface1, null);
        } else if (m_compareDataInterface2 == null) {
            m_compareDataInterface2 = (CompareDataInterface) data;
            dataChanged();
        }

    }

}
