package fr.proline.studio.rsmexplorer.gui.calc.graphics;


import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;


/**
 *
 * @author JM235353
 */
public class BoxPlotGraphic extends AbstractMatrixPlotGraphic {

    public BoxPlotGraphic(GraphPanel panel) {
        super(panel, "boxPlot", "boxPlot", GRAPHIC_TYPE.BoxPlotGraphic);
    }
    
        @Override
    public int getMinGroups() {
        return 2;
    }

    @Override
    public int getMaxGroups() {
        return 8;
    }
   
    @Override
    public String getName() {
        return "Box Plot";
    }

    @Override
    public AbstractGraphic cloneGraphic(GraphPanel p) {
        AbstractGraphic clone = new BoxPlotGraphic(p);
        clone.cloneInfo(this);
        return clone;
    }




}