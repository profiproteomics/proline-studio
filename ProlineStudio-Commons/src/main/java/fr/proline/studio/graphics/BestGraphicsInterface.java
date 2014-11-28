package fr.proline.studio.graphics;

/**
 *
 * @author JM235353
 */
public interface BestGraphicsInterface {
    
    public PlotType getBestPlotType();
    public int getBestXAxisColIndex(PlotType plotType);
    public int getBestYAxisColIndex(PlotType plotType);
}
