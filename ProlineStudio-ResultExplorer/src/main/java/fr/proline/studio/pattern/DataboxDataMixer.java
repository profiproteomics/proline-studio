package fr.proline.studio.pattern;


import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.DataAnalyzerPanel;

/**
 *
 * @author JM235353
 */
public class DataboxDataMixer extends AbstractDataBox {

    public DataboxDataMixer() {
        super(DataboxType.DataboxDataMixer);

        // Name of this databox
        m_typeName = "Data Analyzer";


    }

    @Override
    public void createPanel() {
        DataAnalyzerPanel p = new DataAnalyzerPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;

    }

    @Override
    public void dataChanged() {
    }

    @Override
    public void setEntryData(Object data) {
        if (data instanceof TableInfo) {
            ((DataAnalyzerPanel) m_panel).addTableInfoToGraph((TableInfo) data);
        }
    }

}
