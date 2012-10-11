/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.utils;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.openide.util.ImageUtilities;

/**
 * Renderer for LazyData : 
 * - paint a glasshour when the wrapped data is not ready
 * - call another renderer when the wrapped data is ready according to its
 * type
 *
 * @author JM235353
 */
public class LazyTableCellRenderer extends DefaultTableCellRenderer {

    private static ImageIcon hourGlassIcon = ImageUtilities.loadImageIcon("fr/proline/studio/images/hourGlass.png", false);

    public LazyTableCellRenderer() {
        setIcon(hourGlassIcon);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Object data = ((LazyData) value).getData();
        if (data == null) {
            super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

            return this;
        } else {
            return table.getDefaultRenderer(data.getClass()).getTableCellRendererComponent(table, data, isSelected, hasFocus, row, column);
        }


    }
}
