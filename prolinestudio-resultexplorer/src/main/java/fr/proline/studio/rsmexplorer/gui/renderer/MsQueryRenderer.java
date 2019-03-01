package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.dto.DMsQuery;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for a MsQuery in a Table Cell which is renderered as its initial id
 * @author JM235353
 */
public class MsQueryRenderer extends DefaultTableCellRenderer {
    
    public MsQueryRenderer() {
        setHorizontalAlignment(JLabel.RIGHT);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        DMsQuery msQuery = (DMsQuery) value;

        return super.getTableCellRendererComponent(table, msQuery.getInitialId(), isSelected, hasFocus, row, column);
    }
}
