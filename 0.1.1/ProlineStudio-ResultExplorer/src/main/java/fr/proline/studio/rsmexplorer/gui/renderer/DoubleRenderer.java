/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.utils.DataFormat;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class DoubleRenderer implements TableCellRenderer  {
    
    TableCellRenderer defaultRenderer;
    
    public DoubleRenderer(TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {


        Double d = (Double) value;
        String formatedValue = (d != null) ? DataFormat.format(d.doubleValue(), 2) : "";

        

        return defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}
