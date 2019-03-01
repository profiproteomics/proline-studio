package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for booleans : show a green tick for TRUE, show nothing for FALSE
 * @author JM235353
 */
public class BooleanRenderer extends DefaultTableCellRenderer /*implements ExportTextInterface*/ {

    public BooleanRenderer() {
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setText("");
        label.setHorizontalAlignment(JLabel.CENTER);
        
        if ((value == null) || (! (value instanceof Boolean))) {
            label.setIcon(null);
            return label;
        }
        
        
        Boolean b = (Boolean) value;
        
        if (b.booleanValue()) {
            label.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
        } else {
            label.setIcon(null);
        }
        
        return label;
        
    }

    
}
