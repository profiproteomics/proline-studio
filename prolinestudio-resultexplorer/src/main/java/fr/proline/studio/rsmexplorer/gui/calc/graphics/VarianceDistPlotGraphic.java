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
