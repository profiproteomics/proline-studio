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
    
    private TableCellRenderer m_defaultRenderer;
    
    public DoubleRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {


        Double d = (Double) value;
        String formatedValue = (d != null) ? DataFormat.format(d.doubleValue(), 2) : "";

        

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}
