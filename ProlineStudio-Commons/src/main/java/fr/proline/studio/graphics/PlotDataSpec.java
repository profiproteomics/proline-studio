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
    protected static final Stroke DASHED = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f, 2.0f}, 0.0f);
    
    public enum SHARP{FILL, EMPTY};
    private Color _color;
    private SHARP _sharp;
    private Stroke _stroke;

    public PlotDataSpec() {
        this._color = Color.BLACK;
        this._sharp = SHARP.FILL;
        this._stroke = DASHED;
    }

    public Color getColor() {
        return _color;
    }

    public SHARP getSharp() {
        return _sharp;
    }

    public Stroke getStroke() {
        return _stroke;
    }

    public void setColor(Color color) {
        this._color = color;
    }

    public void setSharp(SHARP sharp) {
        this._sharp = sharp;
    }

    public void setStroke(Stroke stroke) {
        this._stroke = stroke;
    }

}
