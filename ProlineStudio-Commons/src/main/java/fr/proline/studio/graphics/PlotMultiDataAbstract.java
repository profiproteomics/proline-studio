package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;


/**
 *
 * @author JM235353
 */
public abstract class PlotMultiDataAbstract extends PlotBaseAbstract {

    public PlotMultiDataAbstract(BasePlotPanel plotPanel, PlotType plotType, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface) {
        super(plotPanel, plotType, compareDataInterface, crossSelectionInterface);
    }

        
    @Override
    public boolean inside(int x, int y) {
        return true;
    }
    
}
