package fr.proline.studio.parameter;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Renderer for Paramters
 *
 * @author JM235353
 */
public class ParameterComboboxRenderer<E> extends DefaultListCellRenderer {

    private AbstractParameterToString<E> m_paramToString = null;

    public ParameterComboboxRenderer(AbstractParameterToString<E> paramToString) {
        m_paramToString = paramToString;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value == null) {
            l.setText("< Select >");
            return l;
        }

        String display;
        if (m_paramToString != null) {
            display = m_paramToString.toString((E) value);
        } else {
            display = value.toString();
        }
        l.setText(display);

        return l;
    }
}
