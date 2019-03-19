package fr.proline.studio.graphics;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;


/**
 *
 * @author JM235353
 */
public abstract class PlotMultiDataAbstract extends PlotBaseAbstract {

    public PlotMultiDataAbstract(BasePlotPanel plotPanel, PlotType plotType, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface) {
        super(plotPanel, plotType, compareDataInterface, crossSelectionInterface);
    }

        
//    @Override
//    public boolean inside(int x, int y) {
//        return true;
//    }
    
    @Override
    public boolean isMouseWheelSupported() {
        return false;
    }
    
}
