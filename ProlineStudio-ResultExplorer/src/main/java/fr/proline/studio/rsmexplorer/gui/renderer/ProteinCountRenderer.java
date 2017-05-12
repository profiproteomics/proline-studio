package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.export.ExportSubStringFont;
import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinSetTableModel;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author JM235353
 */
public class ProteinCountRenderer extends DefaultTableCellRenderer /*implements ExportTextInterface*/ {

    private String m_basicTextForExport = "";
    private ArrayList<ExportSubStringFont> m_ExportSubStringFonts;
    
    public ProteinCountRenderer() {
        m_ExportSubStringFonts = new ArrayList<ExportSubStringFont>();
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(JLabel.RIGHT);
        
        if ((value == null) || (! (value instanceof ProteinSetTableModel.ProteinCount))) {
            label.setText("");
            m_basicTextForExport = "";
            return label;
        }
        
        
        ProteinSetTableModel.ProteinCount proteinCount = (ProteinSetTableModel.ProteinCount) value;
        label.setText(proteinCount.toHtml());
        m_basicTextForExport = proteinCount.toString();

        
        return label;
        
    }

    /*@Override
    public String getExportText() {
        return m_basicTextForExport;
    }

    @Override
    public ArrayList<ExportSubStringFont> getSubStringFonts() {
        return this.m_ExportSubStringFonts;
    }*/
    
    
}

