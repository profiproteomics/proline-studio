package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.LockedDataModel;
import fr.proline.studio.graphics.PlotType;

/**
 * Locked Model for graphics (if src data is modified, the modification is not propagated),
 * which is used for data analyzer
 * @author JM235353
 */
public class LockedDataGraphicsModel extends LockedDataModel {

    private PlotType m_bestPlotType;
    private int[] m_bestColsIndex;
    
    public LockedDataGraphicsModel(CompareDataInterface srcData, PlotType bestPlotType, int[] bestColsIndex) {
        super(srcData);
        
        m_bestPlotType = bestPlotType;
        m_bestColsIndex = bestColsIndex;
    }
    
    @Override
    public PlotType getBestPlotType() {
        return m_bestPlotType;
    }
    @Override
    public int[] getBestColIndex(PlotType plotType) {
        if (plotType == m_bestPlotType) {
            if (m_bestColsIndex != null) {
                int nb = m_bestColsIndex.length;
                int[] copy = new int[nb];
                System.arraycopy( m_bestColsIndex, 0, copy, 0, nb);
                return copy;
            }
            return null;
        }
        return super.getBestColIndex(plotType);
    }

}
