/*
 * $Id$
 *
 * Copyright 2009 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * jm235353 : done some simplification of the code (could do others) 
 */
package fr.proline.studio.utils;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.AbstractLayoutPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;

@SuppressWarnings("unchecked")
public class RelativePainterHighlighter extends PainterHighlighter {
    // the fixed value to compare against (should be Comparable)
    private Relativizer relativizer;
    
    public RelativePainterHighlighter() {
        this(null);
    }
    
    public RelativePainterHighlighter(Painter delegate) {
        super(delegate);
    }

    public void setHorizontalAlignment(HorizontalAlignment align) {
        getPainter().setHorizontalAlignment(align);
        fireStateChanged();
    }
    
    public HorizontalAlignment getHorizontalAlignment() {
        return getPainter().getHorizontalAlignment();
    }
    

    /**
     * @param maxValue the maxValue to set
     */
    public void setRelativizer(Relativizer relativizer) {
        this.relativizer = relativizer;
        fireStateChanged();
    }

    public Relativizer getRelativizer() {
        return relativizer;
    }
    
    @Override
    protected Component doHighlight(Component component,
            ComponentAdapter adapter) {
        float xPercent = relativizer.getRelativeValue(adapter);
        getPainter().setXFactor(xPercent);
        getPainter().setVisible(xPercent != Relativizer.ZERO);
        return super.doHighlight(component, adapter);
    }
    /**
     * Overridden to wrap a RelativePainter around the given, if not
     * already is of type RelativePainter.
     */
    @Override
    public void setPainter(Painter painter) {
        if (!(painter instanceof RelativePainter)) {
            painter = new RelativePainter(painter); 
        }
        super.setPainter(painter);
    }

    @Override
    public RelativePainter getPainter() {
        return (RelativePainter) super.getPainter();
    }

    @Override
    protected boolean canHighlight(Component component,
            ComponentAdapter adapter) {
        return relativizer != null && super.canHighlight(component, adapter);
    }
    
//------------------- Relativizer
    
    public static interface Relativizer {
        public static final float ZERO = 0.0f;
        public static final float ONE = 1.0f;
        /**
         * Returns a float in the range of 0.0f to 1.0f inclusive which
         * indicates the relative value of the given adapter's value.
         * 
         * @param adapter
         * @return
         */
        public float getRelativeValue(ComponentAdapter adapter);

    }

    

    public static class NumberRelativizer implements Relativizer {

        private Number min;
        private Number max;
        private int valueColumn;
        

        public NumberRelativizer(int column, Number min, Number max) {
            this.min = min;
            this.max = max;
            this.valueColumn = column;
        }

        public void setMin(Number min) {
            this.min = min;
        }
        
        public void setMax(Number max) {
            this.max = max;
        }
        
        @Override
        public float getRelativeValue(ComponentAdapter adapter) {
            
            float floatValue;
            Object value = adapter.getValue(valueColumn);
            if (value instanceof String) {
                floatValue = Float.valueOf((String) value);
            } else {
                floatValue = ((Number) value).floatValue();
            }

            float percent = floatValue / max.floatValue()-min.floatValue();
            
            return percent;
   
        }

 


    }

    
    //--------- hack around missing size proportional painters
    
    public static class RelativePainter<T> extends AbstractLayoutPainter<T> {

        private Painter<? super T> painter;
        private double xFactor;
        private double yFactor;
        private boolean visible;

        public RelativePainter() {
            this(null);
            setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }
        
        
        public RelativePainter(Painter<? super T> delegate) {
            this.painter = delegate;
            setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }
        
        public RelativePainter(Painter<? super T> delegate, double xPercent) {
            this(delegate);
            xFactor = xPercent;
            setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }
        public void setPainter(Painter<? super T> painter) {
            Object old = getPainter();
            this.painter = painter;
            firePropertyChange("painter", old, getPainter());
        }
        
        public Painter<? super T> getPainter() {
            return painter;
        }
        
        public void setXFactor(double xPercent) {
            double old = getXFactor();
            this.xFactor = xPercent;
            firePropertyChange("xFactor", old, getXFactor());
        }
        
        /**
         * @return
         */
        public double getXFactor() {
            return xFactor;
        }

        public void setYFactor(double yPercent) {
            this.yFactor = yPercent;
        }
        @Override
        protected void doPaint(Graphics2D g, T object, int width, int height) {
            if (painter == null) return;
            // use epsilon
            if (xFactor != 0.0) {
                int oldWidth = width;
                width = (int) (xFactor * width);
                if (getHorizontalAlignment() == HorizontalAlignment.RIGHT) {
                    g.translate(oldWidth - width, 0);
                }
            }
            if (yFactor != 0.0) {
                int oldHeight = height;
                height = (int) (yFactor * height);
                if (getVerticalAlignment() == VerticalAlignment.BOTTOM) {
                    g.translate(0, oldHeight - height);
                }
            }
            
            painter.paint(g, object, width, height);
        }

        /**
         * Overridden to take over completely: super does strange things with
         * dirty which result in property changes fired during painting.
         */
        @Override
        public boolean isVisible() {
            return visible;
        }


        /**
         * Overridden to take over completely: super does strange things with
         * dirty which result in property changes fired during painting.
         */
        @Override
        public void setVisible(boolean visible) {
            if (isVisible() == visible) return;
            this.visible = visible;
            firePropertyChange("visible", !visible, isVisible());
        }
        
    }

    


}