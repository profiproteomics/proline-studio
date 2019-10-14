/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
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
