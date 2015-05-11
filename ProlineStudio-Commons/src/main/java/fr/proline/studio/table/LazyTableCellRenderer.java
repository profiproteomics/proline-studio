package fr.proline.studio.table;

import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for LazyData : 
 * - paint a glasshour when the wrapped data is not ready
 * - call another renderer when the wrapped data is ready according to its
 * type
 *
 * @author JM235353
 */
public class LazyTableCellRenderer extends DefaultTableCellRenderer {

    private TableCellRenderer m_childRenderer = null;
    
    public LazyTableCellRenderer() {
        setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS));
    }
    
    public LazyTableCellRenderer(TableCellRenderer childRenderer) {
        setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS));
        m_childRenderer = childRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (value == null) {
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            return this;
        }
        
        Object data = ((LazyData) value).getData();
        
        if ((m_childRenderer != null) && (data != null)) {
            return m_childRenderer.getTableCellRendererComponent(table, data, isSelected, hasFocus, row, column);
        }
        
        if (data == null) {
            super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

            return this;
        } else {
            return table.getDefaultRenderer(data.getClass()).getTableCellRendererComponent(table, data, isSelected, hasFocus, row, column);
        }


    }
}
