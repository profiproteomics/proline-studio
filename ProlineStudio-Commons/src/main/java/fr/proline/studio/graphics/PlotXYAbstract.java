package fr.proline.studio.graphics;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Base class for plots with 
 * 
 * @author JM235353
 */
public abstract class PlotXYAbstract extends PlotBaseAbstract {


    public PlotXYAbstract(BasePlotPanel plotPanel, PlotType plotType, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface) {
        super(plotPanel, plotType, compareDataInterface, crossSelectionInterface);
    }

    
    @Override
    public boolean inside(int x, int y) {
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        int x1 = xAxis.valueToPixel(xAxis.getMinValue());
        int x2 = xAxis.valueToPixel(xAxis.getMaxValue());
        int y1 = yAxis.valueToPixel(yAxis.getMaxValue());
        int y2 = yAxis.valueToPixel(yAxis.getMinValue());
        return (x>=x1) && (x<=x2) && (y>=y1) && (y<=y2);
    }

}
