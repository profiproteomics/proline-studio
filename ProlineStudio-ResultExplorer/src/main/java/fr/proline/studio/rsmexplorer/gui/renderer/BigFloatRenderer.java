package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.utils.DataFormat;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for a Float Value in a Table Cell which is displayed as 0.00
 * @author JM235353
 */
public class BigFloatRenderer implements TableCellRenderer {
    
    private TableCellRenderer m_defaultRenderer;
    
    private int m_digits = 2;
    
    public BigFloatRenderer(TableCellRenderer defaultRenderer, int digits) {
        m_defaultRenderer = defaultRenderer;
        m_digits = digits;
    }
    
    public BigFloatRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Float f = (Float) value;
        String formatedValue = ((f == null) || (f.isNaN())) ? "" : DataFormat.formatWithGroupingSep(f, m_digits);

        

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}
