package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.utils.DataFormat;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for a Float Value in a Table Cell which is displayed as 0.00
 * @author JM235353
 */
public class FloatRenderer implements TableCellRenderer {
    
    private final TableCellRenderer m_defaultRenderer;
    
    private int m_digits = 2;
    private boolean m_scientific = false;
    private boolean m_showNaN = false;
    
    
    public FloatRenderer(TableCellRenderer defaultRenderer, int digits, boolean scientific, boolean showNaN) {
        m_defaultRenderer = defaultRenderer;
        m_digits = digits;
        m_scientific = scientific;
        m_showNaN = showNaN;
    }
    
    public FloatRenderer(TableCellRenderer defaultRenderer, int digits) {
        m_defaultRenderer = defaultRenderer;
        m_digits = digits;
    }
    
    public FloatRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Float f = (Float) value;
        String formatedValue;
        
        if ((f == null) || (f.isNaN())) {
            if (m_showNaN) {
                formatedValue = "NaN";
            } else {
                formatedValue = "";
            }
        } else if (m_scientific) {
            double dAbs = Math.abs(f.floatValue());
            if ((dAbs!=0) && (dAbs*Math.pow(10, m_digits-1)>=1)) {
                formatedValue = DataFormat.format(f.floatValue(), m_digits);
            } else {
                int digits = m_digits - 2;
                if (digits < 2) {
                    digits = 2;
                }
                formatedValue = DataFormat.formatScientific(f.floatValue(), digits);
            }
        } else {
            formatedValue = DataFormat.format(f.floatValue(), m_digits);
        }

        

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}
