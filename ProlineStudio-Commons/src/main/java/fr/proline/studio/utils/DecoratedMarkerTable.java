/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.utils;

import fr.proline.studio.markerbar.MarkerComponentInterface;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;

/**
 *
 * @author JM235353
 */
public class DecoratedMarkerTable extends DecoratedTable implements MarkerComponentInterface, ChangeListener {

    private int firstVisibleRow = -1;
    private int lastVisibleRow = -1;
    private JViewport viewport = null;

    public void setViewport(JViewport viewport) {
        this.viewport = viewport;
        viewport.addChangeListener(this);
    }

    @Override
    public int getFirstVisibleRow() {
        return firstVisibleRow;
    }

    @Override
    public int getLastVisibleRow() {
        return lastVisibleRow;
    }

    @Override
    public void scrollToVisible(int row) {
        scrollRectToVisible(new Rectangle(getCellRect(row, 0, true)));
    }

    @Override
    public int getRowYStart(int row) {

        Rectangle viewRect = viewport.getViewRect();
        
        Rectangle r = getCellRect(row, 0, false);

        int headerHeight = 0;
        JTableHeader header = getTableHeader();
        if (header != null) {
            headerHeight = header.getHeight();
        }

        return headerHeight + r.y-viewRect.y;
    }

    @Override
    public int getRowYStop(int row) {
        
        Rectangle viewRect = viewport.getViewRect();
        
        Rectangle r = getCellRect(row, 0, false);

        int headerHeight = 0;
        JTableHeader header = getTableHeader();
        if (header != null) {
            headerHeight = header.getHeight();
        }

        return headerHeight + r.y + r.height-viewRect.y;
    }

    
    
    @Override
    public void stateChanged(ChangeEvent e) {

        Rectangle viewRect = viewport.getViewRect();




        firstVisibleRow = rowAtPoint(new Point(0, viewRect.y));
        if (firstVisibleRow == -1) {
            lastVisibleRow = -1;
            return;
        }



        lastVisibleRow = rowAtPoint(new Point(0, viewRect.y + viewRect.height - 1));
        if (lastVisibleRow == -1) {
            // Handle empty space below last row
            lastVisibleRow = getModel().getRowCount() - 1;
        }
        //  System.out.println(firstVisibleRow + " " + lastVisibleRow);
    }

    @Override
    public int getRow(int y) {
        
        Rectangle viewRect = viewport.getViewRect();
        
        int headerHeight = 0;
        JTableHeader header = getTableHeader();
        if (header != null) {
            headerHeight = header.getHeight();
        }
        
        int row = rowAtPoint(new Point(0, y+viewRect.y-headerHeight));
        System.out.println(row);
        return row;
    }
}
