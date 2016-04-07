package fr.proline.studio.rsmexplorer.gui.calc.graphics;


import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;


/**
 *
 * @author JM235353
 */
public class DensityPlotGraphic extends AbstractMatrixPlotGraphic {
    
    
    public DensityPlotGraphic(GraphPanel panel) {
        super(panel, "densityPlot", "densityPlot");
    }
   
    @Override
    public String getName() {
        return "Density Plot";
    }

    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        AbstractGraphic clone = new DensityPlotGraphic(p);
        clone.cloneInfo(this);
        return clone;
    }

    @Override
    public int getMinGroups() {
        return 2;
    }

    @Override
    public int getMaxGroups() {
        return 3;
    }


}