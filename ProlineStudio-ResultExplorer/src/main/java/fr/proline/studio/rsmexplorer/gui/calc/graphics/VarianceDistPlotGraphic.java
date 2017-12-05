package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;

/**
 *
 * @author JM235353
 */
public class VarianceDistPlotGraphic extends AbstractMatrixPlotGraphic {

    public VarianceDistPlotGraphic(GraphPanel panel) {
        super(panel, "varianceDistPlot", "varianceDistPlot", GRAPHIC_TYPE.VarianceDistPlotGraphic);
    }

    @Override
    public String getName() {
        return "Variance Dist Plot";
    }

    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        AbstractGraphic clone = new VarianceDistPlotGraphic(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public int getMinGroups() {
        return 2;
    }

    @Override
    public int getMaxGroups() {
        return 8;
    }

}
