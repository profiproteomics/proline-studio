package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.utils.DataFormat;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer to correctly format doubles
 * @author JM235353
 */
public class DoubleRenderer implements TableCellRenderer  {
    
    private final TableCellRenderer m_defaultRenderer;
    private int m_nbDigits = 2;
    private boolean m_scientific = false;
    private boolean m_showNaN = false;
    
    public DoubleRenderer(TableCellRenderer defaultRenderer, int digits, boolean scientific, boolean showNaN) {
        m_defaultRenderer = defaultRenderer;
        m_nbDigits = digits;
        m_scientific = scientific;
        m_showNaN = showNaN;
    }
    
    public DoubleRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
        m_nbDigits = 2;
    }
    
    public DoubleRenderer(TableCellRenderer defaultRenderer, int nbDigits) {
        m_defaultRenderer = defaultRenderer;
        m_nbDigits = nbDigits;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {


        Double d = (Double) value;
        String formatedValue;
        if ((d == null) || (d.isNaN())) {
            if (m_showNaN) {
                formatedValue = "NaN";
            } else {
                formatedValue = "";
            }
        } else if (m_scientific) {
            double dAbs = Math.abs(d.doubleValue());
 
            if ((dAbs!=0) && (dAbs*Math.pow(10, m_nbDigits-1)>=1)) {
                formatedValue = DataFormat.format(d.doubleValue(), m_nbDigits);
            } else {
                int digits = m_nbDigits-2;
                if (digits<2) {
                    digits = 2;
                }
                formatedValue = DataFormat.formatScientific(d.doubleValue(), digits);
            }
        } else {
            formatedValue = DataFormat.format(d.doubleValue(), m_nbDigits);
        }

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}
