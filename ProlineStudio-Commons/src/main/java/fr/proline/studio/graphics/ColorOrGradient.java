package fr.proline.studio.graphics;

import java.awt.Color;
import java.awt.LinearGradientPaint;

/**
 *
 * @author JM235353
 */
public class ColorOrGradient {

    private boolean m_colorSelected = true;
    private Color m_color = null;
    private LinearGradientPaint m_gradient = null;
    
    

    public ColorOrGradient() {

    }

    public void setColorSelected() {
        m_colorSelected = true;
    }

    public boolean isColorSelected() {
        return m_colorSelected;
    }

    public void setGradientSelected() {
        m_colorSelected = false;
    }

    public boolean isGradientSelected() {
        return m_colorSelected;
    }

    public void setColor(Color color) {
        m_color = color;
    }

    public Color getColor() {
        return m_color;
    }

    public void setGradient(LinearGradientPaint gradient) {
        m_gradient = gradient;
    }

    public LinearGradientPaint getGradient() {
        return m_gradient;
    }

    @Override
    public String toString() {
        /*StringBuilder sb = new StringBuilder();
         sb.append(m_colorSelected ? "0" : "1");
         if ()*/
        //JPM.TODO
        return "";
    }

    public static ColorOrGradient read(String s) {
        //JPM.TODO
        return new ColorOrGradient();
    }
}
