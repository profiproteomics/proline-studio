package fr.proline.studio.pattern;

import fr.proline.studio.rsmexplorer.gui.calc.DataAnalyzerResultsPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessEngineInfo;

/**
 *
 * @author JM235353
 */
public class DataBoxDataAnalyzerResults extends AbstractDataBox {

    public DataBoxDataAnalyzerResults() {
        super(DataboxType.DataBoxDataAnalyzerResults, DataboxStyle.STYLE_UNKNOWN);

        // Name of this databox
        m_typeName = "Data Analyzer Results";
        m_description = "Data Analyzer Results";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ProcessEngineInfo.class, false);
        registerInParameter(inParameter);

        
    }
    
    @Override
    public void createPanel() {
        DataAnalyzerResultsPanel p = new DataAnalyzerResultsPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        final ProcessEngineInfo processEngineInfo = (ProcessEngineInfo) m_previousDataBox.getData(false, ProcessEngineInfo.class);
        if (processEngineInfo != null) {
            ((DataAnalyzerResultsPanel) getDataBoxPanelInterface()).displayGraphNode(processEngineInfo);
        }
    }
    
}
