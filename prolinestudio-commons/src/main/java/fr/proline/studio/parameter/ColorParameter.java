package fr.proline.studio.parameter;

import fr.proline.studio.graphics.ColorButton;
import fr.proline.studio.graphics.colorpicker.ColorPickerPanel;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.graphics.ColorButtonAndPalettePanel;
import fr.proline.studio.graphics.colorpicker.ColorDataInterface;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;

/**
 * Parameter to select a Color
 *
 * @author JM235353
 */
public class ColorParameter extends AbstractParameter {

    private Color m_defaultColor = null;

    private boolean m_edited = false;

    private Color m_startValue;

    public ColorParameter(String key, String name, Color defaultColor) {
        super(key, name, Color.class, ColorButton.class);
        m_defaultColor = defaultColor;
    }

    public ColorParameter(String key, String name, Color defaultColor, Class graphicalType) {
        super(key, name, Color.class, graphicalType);
        m_defaultColor = defaultColor;
    }

    @Override
    public JComponent getComponent(Object value) {

        m_startValue = (Color) value;

        if (m_startValue == null) {
            m_startValue = m_defaultColor;
        }

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(ColorButton.class)) {
                ((ColorButton) m_parameterComponent).setColor(m_startValue);
                return m_parameterComponent;
            } else if (m_graphicalType.equals(ColorPickerPanel.class)) {
                ((ColorPickerPanel) m_parameterComponent).setColor(m_startValue);
                return m_parameterComponent;
            } else if (m_graphicalType.equals(ColorButtonAndPalettePanel.class)) {
                ((ColorButtonAndPalettePanel) m_parameterComponent).setColor(m_startValue);
                return m_parameterComponent;
            }
        }

        if (m_graphicalType.equals(ColorButton.class)) {

            ColorButton colorButton = new ColorButton();
            colorButton.initActionListener();
            colorButton.setColor(m_startValue);

            colorButton.addListener(new ColorDataInterface() {

                @Override
                public void propagateColorChanged(int r, int g, int b) {
                    if (m_externalActionListener != null) {
                        ActionEvent e = new ActionEvent(colorButton, -1, null);
                        m_externalActionListener.actionPerformed(e);
                    }
                }

                @Override
                public int getRed() {
                    return -1;
                }

                @Override
                public int getGreen() {
                    return -1;
                }

                @Override
                public int getBlue() {
                    return -1;
                }

                @Override
                public void addListener(ColorDataInterface colorDataInterface) {
                }
            });

            m_parameterComponent = colorButton;
            return colorButton;
        } else if (m_graphicalType.equals(ColorPickerPanel.class)) {

            ColorPickerPanel colorPanel = new ColorPickerPanel(CyclicColorPalette.getPalette());
            colorPanel.setColor(m_startValue);

            m_parameterComponent = colorPanel;
            return colorPanel;
        } else if (m_graphicalType.equals(ColorButtonAndPalettePanel.class)) {
            ColorButtonAndPalettePanel colorButtonPalettePanel = new ColorButtonAndPalettePanel();
            colorButtonPalettePanel.setColor(m_startValue);
            m_parameterComponent = colorButtonPalettePanel;
            return colorButtonPalettePanel;
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
        } else if (m_graphicalType.equals(ColorPickerPanel.class)) {
            ColorPickerPanel colorPanel = (ColorPickerPanel) m_parameterComponent;
            colorPanel.setColor(m_defaultColor);
        } else if (m_graphicalType.equals(ColorButtonAndPalettePanel.class)) {
            ColorButtonAndPalettePanel panel = (ColorButtonAndPalettePanel) m_parameterComponent;
            panel.setColor(m_defaultColor);
        }
    }

    @Override
    public ParameterError checkParameter() {
        return null;
    }

    @Override
    public String getStringValue() {
        Object v = getObjectValue();
        return (v != null) ? v.toString() : null;
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(ColorButton.class) && (m_parameterComponent != null)) {
            return ((ColorButton) m_parameterComponent).getColor();
        } else if (m_graphicalType.equals(ColorPickerPanel.class) && (m_parameterComponent != null)) {
            return ((ColorPickerPanel) m_parameterComponent).getColor();
        } else if (m_graphicalType.equals(ColorButtonAndPalettePanel.class) && (m_parameterComponent != null)) {
            return ((ColorButtonAndPalettePanel) m_parameterComponent).getColor();
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

    public void setColor(Color c) {
        if (c == null) {
            c = m_defaultColor;
        }
        if (m_graphicalType.equals(ColorButton.class) && (m_parameterComponent != null)) {
            ((ColorButton) m_parameterComponent).setColor(c);
        } else if (m_graphicalType.equals(ColorPickerPanel.class) && (m_parameterComponent != null)) {
            ((ColorPickerPanel) m_parameterComponent).setColor(c);
        } else if (m_graphicalType.equals(ColorButtonAndPalettePanel.class) && (m_parameterComponent != null)) {
            ((ColorButtonAndPalettePanel) m_parameterComponent).setColor(c);
        }

        if (c!=null && m_startValue != null) {
            m_edited = c != m_startValue;
        }
    }

    @Override
    public void setValue(String v) {

        // method never used for the moment
        if (m_parameterComponent == null) {
            return; // should not happen
        }
        if (m_graphicalType.equals(ColorButton.class)) {
            //((ColorButton) m_parameterComponent).setColor(ColorButton.read(v));  //JPM.TODO
        } else if (m_graphicalType.equals(ColorPickerPanel.class)) {
            //JPM.TODO
        } else if (m_graphicalType.equals(ColorButtonAndPalettePanel.class)) {
            //JPM.TODO
        }

    }

    @Override
    public boolean isEdited() {
        return m_edited;
    }

}
