package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.LockedDataModel;
import fr.proline.studio.graphics.PlotType;

/**
 *
 * @author JM235353
 */
public class LockedDataGraphicsModel extends LockedDataModel {

    private PlotType m_bestPlotType;
    private int m_bestXAxisColIndex;
    private int m_bestYAxisColIndex;
    
    public LockedDataGraphicsModel(CompareDataInterface srcData, PlotType bestPlotType, int bestXAxisColIndex, int bestYAxisColIndex) {
        super(srcData);
        
        m_bestPlotType = bestPlotType;
        m_bestXAxisColIndex = bestXAxisColIndex;
        m_bestYAxisColIndex = bestYAxisColIndex;
    }
    
    @Override
    public PlotType getBestPlotType() {
        return m_bestPlotType;
    }
    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        if (plotType == m_bestPlotType) {
            return m_bestXAxisColIndex;
        }
        return super.getBestXAxisColIndex(plotType);
    }
    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        if (plotType == m_bestPlotType) {
            return m_bestYAxisColIndex;
        }
        return super.getBestYAxisColIndex(plotType);
    }
}
