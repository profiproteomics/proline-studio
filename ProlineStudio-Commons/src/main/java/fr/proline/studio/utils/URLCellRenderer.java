package fr.proline.studio.utils;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 * Renderer for a Cell Table with an URL
 * @author CB205360
 */

public class URLCellRenderer extends DefaultTableCellRenderer implements MouseListener {

    private String m_preferenceKey;
    private String m_defaultURLTemplate;
    private int m_column;
    
    public URLCellRenderer(String preferenceKey, String defaultURLTemplate, int column) {
        m_preferenceKey = preferenceKey;
        m_column = column;
        m_defaultURLTemplate = defaultURLTemplate;
    }
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

        setIcon(IconManager.getIcon(IconManager.IconType.WEB_LINK));

        return this;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        
        if (col != m_column) {
            return;
        }
 
        JTableHeader th = table.getTableHeader();  
        TableColumnModel tcm = th.getColumnModel();
        
        int columnStart = 0;
        for (int i=0;i<m_column;i++) {
            TableColumn column = tcm.getColumn(i);
            columnStart += column.getWidth();
        }
 
        // check that the user has clicked on the icon
        if (columnStart+20<e.getX()) {
            return;
        }

        int row = table.rowAtPoint(pt);
        
        Object value = table.getValueAt(row, col);

        //LoggerFactory.getLogger("ProlineStudio.Commons").info(value.toString());
        try {
            if (Desktop.isDesktopSupported()) { // JDK 1.6.0
                
                Preferences preferences = NbPreferences.root();
                String template = preferences.get(m_preferenceKey, null);
                if (template == null) {
                    template = m_defaultURLTemplate;
                    preferences.put(m_preferenceKey, m_defaultURLTemplate);
                }
                
                Desktop.getDesktop().browse(new URL(template + value.toString()).toURI());
            }
        } catch (Exception ex) {
            // should not happen
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", ex);
        }

    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}