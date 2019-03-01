package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.utils.DataFormat;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer to display a float as a percentage
 * @author JM235353
 */
public class PercentageRenderer implements TableCellRenderer {
    
    private TableCellRenderer m_defaultRenderer;
    
    public PercentageRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        ;
        String formatedValue;
        if (value == null) {
            formatedValue = "";
        } else if (value instanceof Float) {
            Float f = (Float) value;
            formatedValue = (f.isNaN()) ? "" : DataFormat.format(f.floatValue(), 2)+" %";
            
        } else {
            Double d = (Double) value;
            Float f = d.floatValue();
            formatedValue =  (f.isNaN()) ? "" : DataFormat.format(f.floatValue(), 2)+" %";
        }
        
        

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}

