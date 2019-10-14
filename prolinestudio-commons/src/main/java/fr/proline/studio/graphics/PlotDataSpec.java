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

import java.awt.Color;

/**
 * a Data Class for each data in a plot
 * @author Karine XUE
 */
public class PlotDataSpec {

    public enum FILL {
        FILL, EMPTY
    };

    public enum SHARP {
        OVAL, CROIX
    };
    private Color _color;
    private SHARP _sharp;
    private FILL _fill;

    public PlotDataSpec() {
        this._color = Color.BLACK;
        this._sharp = null;
        this._fill = FILL.FILL;
    }

    public Color getColor() {
        return _color;
    }

    public SHARP getSharp() {
        return _sharp;
    }

    public FILL getFill() {
        return _fill;
    }

    public void setColor(Color color) {
        this._color = color;
    }

    public void setSharp(SHARP sharp) {
        this._sharp = sharp;
    }

    public void setFill(FILL fill) {
        this._fill = fill;
    }

}
