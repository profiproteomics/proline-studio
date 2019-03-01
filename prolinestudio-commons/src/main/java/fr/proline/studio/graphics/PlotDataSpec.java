/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 6 févr. 2019
 */
package fr.proline.studio.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

/**
 *
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
