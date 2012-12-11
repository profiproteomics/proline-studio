package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.MsQuery;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for a MsQuery in a Table Cell which is renderered as its initial id
 * @author JM235353
 */
public class MsQueryRenderer extends DefaultTableCellRenderer {
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        MsQuery msQuery = (MsQuery) value;

        return super.getTableCellRendererComponent(table, msQuery.getInitialId(), isSelected, hasFocus, row, column);
    }
}
