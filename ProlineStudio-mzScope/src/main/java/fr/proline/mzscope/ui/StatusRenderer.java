package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.ExtractionResult.Status;
import fr.proline.studio.export.ExportSubStringFont;
import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for extraction status: show a green tick for DONE, a hour glass for REQUESTED, nothing otherwise (NONE)
 * @author MB243701
 */
public class StatusRenderer  extends DefaultTableCellRenderer /*implements ExportTextInterface */ {
    
    private String m_basicTextForExport = "";
    private ArrayList<ExportSubStringFont> m_ExportSubStringFonts;
    
    public StatusRenderer() {
        m_ExportSubStringFonts = new ArrayList<ExportSubStringFont>();
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setText("");
        label.setHorizontalAlignment(JLabel.CENTER);
        
        if ((value == null) || (! (value instanceof Status))) {
            label.setIcon(null);
            m_basicTextForExport = "";
            return label;
        }
        
        
        Status st = (Status) value;
        
        if (st.equals(Status.DONE)) {
            m_basicTextForExport = "done";
            label.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
        } else if (st.equals(Status.REQUESTED)) {
            m_basicTextForExport = "requested";
            label.setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16));
        } else {
            m_basicTextForExport = "none";
            label.setIcon(null);
        }
        
        return label;
        
    }

    /*@Override
    public String getExportText() {
        return m_basicTextForExport;
    }*/

    /*@Override
    public ArrayList<ExportSubStringFont> getSubStringFonts() {
        return this.m_ExportSubStringFonts;
    }*/
    
}
