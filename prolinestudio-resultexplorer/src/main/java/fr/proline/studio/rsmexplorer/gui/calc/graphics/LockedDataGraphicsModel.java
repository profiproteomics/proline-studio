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
package fr.proline.studio.rsmexplorer.gui.calc.graphics;

import fr.proline.studio.extendedtablemodel.LockedDataModel;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Locked Model for graphics (if src data is modified, the modification is not propagated),
 * which is used for data analyzer
 * @author JM235353
 */
public class LockedDataGraphicsModel extends LockedDataModel {

    private PlotType m_bestPlotType;
    private int[] m_bestColsIndex;
    
    public LockedDataGraphicsModel(ExtendedTableModelInterface srcData, PlotType bestPlotType, int[] bestColsIndex) {
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
