package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.export.ExportSubStringFont;
import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinTableModel;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for sameset/subset (shown as full or half filled square)
 * @author JM235353
 */
public class SamesetRenderer extends DefaultTableCellRenderer implements ExportTextInterface {

    private String m_basicTextForExport = "";
    private ArrayList<ExportSubStringFont> m_ExportSubStringFonts;
    
    public SamesetRenderer() {
        m_ExportSubStringFonts = new ArrayList<ExportSubStringFont>();
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
        
        m_basicTextForExport = sameset.toString();
        if (sameset.isTypical()) {
            label.setIcon(IconManager.getIcon(IconManager.IconType.TYPICAL));
        } else if (sameset.isSameset()) {
            label.setIcon(IconManager.getIcon(IconManager.IconType.SAME_SET));
        } else {
            label.setIcon(IconManager.getIcon(IconManager.IconType.SUB_SET));
        }
        
        return label;
        
    }

    @Override
    public String getExportText() {
        return m_basicTextForExport;
    }

    @Override
    public ArrayList<ExportSubStringFont> getSubStringFonts() {
        return this.m_ExportSubStringFonts;
    }
    
    
}

