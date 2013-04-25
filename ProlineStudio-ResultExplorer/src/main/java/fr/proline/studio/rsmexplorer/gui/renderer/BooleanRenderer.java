package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for booleans : show a green tick for TRUE, show nothing for FALSE
 * @author JM235353
 */
public class BooleanRenderer extends JLabel implements TableCellRenderer {

    public BooleanRenderer() {
        setHorizontalAlignment(JLabel.CENTER);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if ((value == null) || (! (value instanceof Boolean))) {
            setIcon(null);
            return this;
        }
        
        Boolean b = (Boolean) value;
        
        if (b.booleanValue()) {
            setIcon(IconManager.getIcon(IconManager.IconType.OK));
        } else {
            setIcon(null);
        }
        
        return this;
        
    }
    
    
}
