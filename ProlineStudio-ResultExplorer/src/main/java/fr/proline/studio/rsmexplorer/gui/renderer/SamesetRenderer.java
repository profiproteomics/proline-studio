package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinTableModel;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for sameset/subset
 * @author JM235353
 */
public class SamesetRenderer extends DefaultTableCellRenderer implements ExportTextInterface {

    private String m_basicTextForExport = "";
    
    public SamesetRenderer() {
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setText("");
        label.setHorizontalAlignment(JLabel.CENTER);
        
        if ((value == null) || (! (value instanceof ProteinTableModel.Sameset))) {
            label.setIcon(null);
            m_basicTextForExport = "";
            return label;
        }
        
        
        ProteinTableModel.Sameset sameset = (ProteinTableModel.Sameset) value;
        
        if (sameset.isTypical()) {
            m_basicTextForExport = "typical";
            label.setIcon(IconManager.getIcon(IconManager.IconType.TYPICAL));
        } else if (sameset.isSameset()) {
            m_basicTextForExport = "sameset";
            label.setIcon(IconManager.getIcon(IconManager.IconType.SAME_SET));
        } else {
            m_basicTextForExport = "subset";
            label.setIcon(IconManager.getIcon(IconManager.IconType.SUB_SET));
        }
        
        return label;
        
    }

    @Override
    public String getExportText() {
        return m_basicTextForExport;
    }
    
    
}

