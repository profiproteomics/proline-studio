package fr.proline.studio.pattern;

import fr.proline.studio.rsmexplorer.gui.StatsFrequencyResponsePanel;
import fr.proline.studio.stats.ValuesForStatsAbstract;

/**
 *
 * @author JM235353
 */
public class DataBoxStatisticsFrequencyResponse extends AbstractDataBox  {

        public DataBoxStatisticsFrequencyResponse() {

        // Name of this databox
        m_name = "Standard Deviation";
        m_description = "Standard Deviation";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ValuesForStatsAbstract.class, false);
        registerInParameter(inParameter);
        

       
    }
    
    @Override
    public void createPanel() {
        StatsFrequencyResponsePanel p = new StatsFrequencyResponsePanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        final ValuesForStatsAbstract values = (ValuesForStatsAbstract) m_previousDataBox.getData(false, ValuesForStatsAbstract.class);
        ((StatsFrequencyResponsePanel)m_panel).setData(values);
    }
    
}
