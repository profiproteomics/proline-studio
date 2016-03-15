package fr.proline.studio.table.renderer;

import fr.proline.studio.utils.DataFormat;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for a Float or Double Value in a Table Cell which is displayed as 0.00
 * @author JM235353
 */
public class BigFloatOrDoubleRenderer implements TableCellRenderer {
    
    private TableCellRenderer m_defaultRenderer;
    
    private int m_digits = 2;
    
    public BigFloatOrDoubleRenderer(TableCellRenderer defaultRenderer, int digits) {
        m_defaultRenderer = defaultRenderer;
        m_digits = digits;
    }
    
    public BigFloatOrDoubleRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        String formatedValue;
        if (value instanceof Float) {
            Float f = (Float) value;
            formatedValue = ((f == null) || (f.isNaN())) ? "" : DataFormat.formatWithGroupingSep(f, m_digits);
        } else { // Double
            Double d = (Double) value;
            formatedValue = ((d == null) || (d.isNaN())) ? "" : DataFormat.formatWithGroupingSep(d, m_digits);
        }
        

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}
