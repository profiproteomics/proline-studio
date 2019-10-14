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