package fr.proline.studio.pattern;

import fr.proline.studio.rsmexplorer.gui.StatsHistogramPanel;
import fr.proline.studio.stats.ValuesForStatsAbstract;

/**
 *
 * @author JM235353
 */
public class DataBoxStatisticsFrequencyResponse /*extends AbstractDataBox*/  {
    /*
    private ValuesForStatsAbstract m_values = null;

    public DataBoxStatisticsFrequencyResponse() {
        super(DataboxType.DataBoxStatisticsFrequencyResponse);

        // Name of this databox
        m_name = "Histogram";
        m_description = "Histogram and Standard Deviation of a Value";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ValuesForStatsAbstract.class, false);
        registerInParameter(inParameter);

    }
    
    @Override
    public void createPanel() {
        StatsHistogramPanel p = new StatsHistogramPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final ValuesForStatsAbstract values = (m_values!=null) ? m_values : (ValuesForStatsAbstract) m_previousDataBox.getData(false, ValuesForStatsAbstract.class);
        ((StatsHistogramPanel)m_panel).setData(values);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_values = (ValuesForStatsAbstract) data;
        dataChanged();
    }
 */   
}
