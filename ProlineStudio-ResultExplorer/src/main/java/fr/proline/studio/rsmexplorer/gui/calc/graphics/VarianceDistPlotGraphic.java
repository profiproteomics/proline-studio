package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;

/**
 *
 * @author JM235353
 */
public class VarianceDistPlotGraphic extends AbstractMatrixPlotGraphic {

    public VarianceDistPlotGraphic(GraphPanel panel) {
        super(panel, "varianceDistPlot");
    }
   
    @Override
    public String getName() {
        return "Variance Dist Plot";
    }

    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        return new VarianceDistPlotGraphic(p);
    }


}