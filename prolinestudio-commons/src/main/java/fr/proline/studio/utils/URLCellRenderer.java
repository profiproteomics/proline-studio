package fr.proline.studio.utils;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URISyntaxException;
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

public class URLCellRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {

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
        int row = table.rowAtPoint(pt);
        
        int modelCol = table.convertColumnIndexToModel(col);
        
        if ((modelCol != m_column) || (row == -1)) {
            return;
        }
 
        JTableHeader th = table.getTableHeader();  
        TableColumnModel tcm = th.getColumnModel();
        
        int columnStart = 0;
        for (int i=0;i<col;i++) {
            TableColumn column = tcm.getColumn(i);
            columnStart += column.getWidth();
        }
 
        // check that the user has clicked on the icon
        if (columnStart+20<e.getX()) {
            return;
        }

        
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
                
                String info = value.toString();
                if (info.startsWith("sp|")) {
                    // wart for proteins starting with sp|
                    info = info.substring(3, info.length());
                } // AW (2015/07/08): to allow uniprot sequence to be openend in browser
                if (info.subSequence(2, 3).equals("|")) { //  like "sp|P07259|PYR1_YEAST"
                	 info = info.substring(3, info.length());
                }
                if (info.contains("|")) { // like P07259|PYR1_YEAST
                	info = info.substring(0,info.indexOf("|"));
                }
                info = encode(info);
                
                Desktop.getDesktop().browse(new URL(template + info).toURI());
            }
        } catch (URISyntaxException | IOException ex) {
            // should not happen
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", ex);
        }


    }
    
    private static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0 ) {
            return true;
        }
        return " %$&+,/:;=?@<>#%|".indexOf(ch) >= 0;
    }


    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        checkCursor(e);
    }
    
    
    private void checkCursor(MouseEvent e) {

        
        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);
        int modelCol = table.convertColumnIndexToModel(col);
        
        if ((modelCol != m_column) || (row == -1)) {
            table.setCursor(Cursor.getDefaultCursor());
            return;
        }

        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();

        int columnStart = 0;
        for (int i = 0; i < col; i++) {
            TableColumn column = tcm.getColumn(i);
            columnStart += column.getWidth();
        }

        // check that the user is over the icon
        if (columnStart + 20 < e.getX()) {
            table.setCursor(Cursor.getDefaultCursor());
            return;
        }

        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


    }
}