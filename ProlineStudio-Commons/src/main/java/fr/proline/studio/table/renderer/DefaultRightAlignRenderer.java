package fr.proline.studio.table.renderer;

import java.awt.Component;
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * This renderer encapsulates another renderer and force to display the text to the right
 * @author JM235353
 */
public class DefaultRightAlignRenderer implements TableCellRenderer, Serializable {
    
    private TableCellRenderer m_renderer;
    
    public DefaultRightAlignRenderer(TableCellRenderer renderer) {
        m_renderer = renderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = m_renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel) {
            ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
        }
        return c;
    }
    
}
