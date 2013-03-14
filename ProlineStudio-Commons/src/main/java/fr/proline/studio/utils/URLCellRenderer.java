/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.utils;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */

public class URLCellRenderer extends DefaultTableCellRenderer implements MouseListener {
    
    public static final String HTMLAI_CLOSE = "</a></i></html>";
    public static final String HTMLIA_HREF = "<html><i><a href=";

    private String URLTemplate;
    
    public URLCellRenderer(String template) {
        URLTemplate = template;
    }
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            StringBuilder builder = new StringBuilder(90);
            builder.append(HTMLIA_HREF).append(URLTemplate).append(value.toString()).append('>');
            builder.append(value.toString()).append(HTMLAI_CLOSE);
        setText(builder.toString());
        return this;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isAltDown()) {
        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int ccol = table.columnAtPoint(pt);

        int crow = table.rowAtPoint(pt);
        Object value = table.getValueAt(crow, ccol);
        LoggerFactory.getLogger(this.getClass()).info(value.toString());
        try {
            if (Desktop.isDesktopSupported()) { // JDK 1.6.0
                Desktop.getDesktop().browse(new URL(URLTemplate + value.toString()).toURI());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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