package fr.proline.studio.filter;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Renderer for the filter combobox 
 *
 * @author JM235353
 */
public class FilterComboboxRenderer extends DefaultListCellRenderer {

    public FilterComboboxRenderer() {
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value == null) {
            l.setText("< Select >");
            return l;
        }

        l.setText(value.toString());

        return l;
    }
}
