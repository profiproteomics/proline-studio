package fr.proline.studio.pattern;


import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.DataAnalyzerPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessEngineInfo;

/**
 *
 * @author JM235353
 */
public class DataboxDataAnalyzer extends AbstractDataBox {

    public DataboxDataAnalyzer() {
        super(DataboxType.DataboxDataAnalyzer, DataboxStyle.STYLE_UNKNOWN);

        // Name of this databox
        m_typeName = "Data Analyzer";


    }

    @Override
    public void createPanel() {
        DataAnalyzerPanel p = new DataAnalyzerPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);

    }

    @Override
    public void dataChanged() {
    }

    @Override
    public void setEntryData(Object data) {
        if (data instanceof TableInfo) {
            ((DataAnalyzerPanel) getDataBoxPanelInterface()).addTableInfoToGraph((TableInfo) data);
        }
    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {
            if (parameterType.equals(ProcessEngineInfo.class)) {
                return ((DataAnalyzerPanel) getDataBoxPanelInterface()).getProcessEngineInfoToDisplay();
            }

        }
        return super.getData(getArray, parameterType);
    }

}
