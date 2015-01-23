package fr.proline.studio.rsmexplorer.gui.renderer;

import java.awt.Component;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class TimestampRenderer implements TableCellRenderer {
    
    private TableCellRenderer m_defaultRenderer;
    
    private static final DateFormat m_df = DateFormat.getDateInstance(DateFormat.LONG, new Locale.Builder().setLanguage("en").setRegion("US").build());
    
    
    public TimestampRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        String formatedValue;
        
        Timestamp timestamp = (Timestamp) value;
        if (timestamp == null) {
            formatedValue = "";
        } else {
            formatedValue = m_df.format(timestamp);
        }
        

        

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}

