package fr.proline.studio.parameter;


import fr.proline.studio.graphics.ColorButton;
import fr.proline.studio.graphics.ColorOrGradient;
import java.awt.Color;
import javax.swing.JComponent;

/**
 *
 * @author JM235353
 */
public class ColorParameter extends AbstractParameter {

    private Color m_defaultColor = null;
    
    public ColorParameter(String key, String name, Color defaultColor) {
        super(key, name, Color.class, ColorButton.class);
        m_defaultColor = defaultColor;
    }

    
    @Override
    public JComponent getComponent(Object value) {

        Color startValue = (Color) value;

        if (startValue == null) {
            startValue = m_defaultColor;
        }

        if (m_parameterComponent !=null) {
            if (m_graphicalType.equals(ColorButton.class)) {
                ((ColorButton) m_parameterComponent).setColor(startValue);
                return m_parameterComponent;
            }
        }

        if (m_graphicalType.equals(ColorButton.class)) {

            ColorButton colorButton = new ColorButton();
            colorButton.initActionListener();
            colorButton.setColor(startValue);

            m_parameterComponent = colorButton;
            return colorButton;
        }


        return null;
    }

    @Override
    public void initDefault() {
        if (m_defaultColor == null) {
            return; // should not happen
        }

        if (m_graphicalType.equals(ColorButton.class)) {
            ColorButton colorButton = (ColorButton) m_parameterComponent;
            colorButton.setColor(m_defaultColor);
        }
    }

    @Override
    public ParameterError checkParameter() {
        return null;
    }

    @Override
    public String getStringValue() {
        return ((ColorOrGradient) getObjectValue()).toString();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(ColorButton.class) && (m_parameterComponent != null)) {
           return ((ColorButton) m_parameterComponent).getColor();
        }
        return null; // should not happen
    }

    public Color getColor() {
        Color c = (Color) getObjectValue();
        if (c == null) {
            return m_defaultColor;
        }
        return c;
    }

    
    @Override
    public void setValue(String v) {
        
        if (m_parameterComponent == null) {
            return; // should not happen
        }
        if (m_graphicalType.equals(ColorButton.class)) {
            //((ColorButton) m_parameterComponent).setColor(ColorButton.read(v));  //JPM.TODO
        }
        
        
    }

    
    
}
