package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.rsmexplorer.gui.model.ProteinSetTableModel;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author JM235353
 */
public class ProteinCountRenderer extends DefaultTableCellRenderer {

    public ProteinCountRenderer() {
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(JLabel.RIGHT);
        
        if ((value == null) || (! (value instanceof ProteinSetTableModel.ProteinCount))) {
            label.setText("");
            return label;
        }
        
        
        ProteinSetTableModel.ProteinCount proteinCount = (ProteinSetTableModel.ProteinCount) value;
        label.setText(proteinCount.toHtml());

        return label;
        
    }

}

