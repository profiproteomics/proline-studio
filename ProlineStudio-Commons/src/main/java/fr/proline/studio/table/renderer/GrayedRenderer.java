package fr.proline.studio.table.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.Serializable;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class GrayedRenderer implements TableCellRenderer, Serializable {
    
    private final TableCellRenderer m_renderer;
    private boolean innerRenderIsGrayable = false;
    
    public GrayedRenderer(TableCellRenderer renderer) {
        m_renderer = renderer;
        innerRenderIsGrayable =  (m_renderer instanceof GrayableTableCellRenderer);
        if (innerRenderIsGrayable) {
            ((GrayableTableCellRenderer) m_renderer).setGrayed(true);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = m_renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (! innerRenderIsGrayable) {
            c.setFont(c.getFont().deriveFont(Font.ITALIC));
            // does not work c.setForeground(Color.lightGray);
        }
        return c;
    }

    
}
