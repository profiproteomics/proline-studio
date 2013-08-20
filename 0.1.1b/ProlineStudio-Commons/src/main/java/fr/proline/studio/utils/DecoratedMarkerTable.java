package fr.proline.studio.utils;

import fr.proline.studio.markerbar.MarkerComponentInterface;
import fr.proline.studio.markerbar.ViewChangeListener;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;

/**
 * Table which can be used with marker bars (it inherits display an "histogram" on a column from DecoratedTable)
 * @author JM235353
 */
public class DecoratedMarkerTable extends DecoratedTable implements MarkerComponentInterface, ChangeListener {

    private int m_firstVisibleRow = -1;
    private int m_lastVisibleRow = -1;
    private JViewport m_viewport = null;

    public DecoratedMarkerTable() {
        getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                dispatchViewChange();
            }
        });
    }
    
    public void setViewport(JViewport viewport) {
        m_viewport = viewport;
        viewport.addChangeListener(this);
    }

    @Override
    public int getFirstVisibleRow() {
        return m_firstVisibleRow;
    }

    @Override
    public int getLastVisibleRow() {
        return m_lastVisibleRow;
    }

    @Override
    public void scrollToVisible(int row) {
        scrollRectToVisible(new Rectangle(getCellRect(row, 0, true)));
    }

    @Override
    public int getRowYStart(int row) {

        Rectangle viewRect = m_viewport.getViewRect();
        
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
        
        Rectangle viewRect = m_viewport.getViewRect();
        
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

        Rectangle viewRect = m_viewport.getViewRect();

        m_firstVisibleRow = rowAtPoint(new Point(0, viewRect.y));
        if (m_firstVisibleRow == -1) {
            m_lastVisibleRow = -1;
            return;
        }

        m_lastVisibleRow = rowAtPoint(new Point(0, viewRect.y + viewRect.height - 1));
        if (m_lastVisibleRow == -1) {
            // Handle empty space below last row
            m_lastVisibleRow = getModel().getRowCount() - 1;
        }
        //  System.out.println(firstVisibleRow + " " + lastVisibleRow);
        
        
    }

    @Override
    public int getRowInModel(int y) {
        
        Rectangle viewRect = m_viewport.getViewRect();
        
        int headerHeight = 0;
        JTableHeader header = getTableHeader();
        if (header != null) {
            headerHeight = header.getHeight();
        }
        
        int row = rowAtPoint(new Point(0, y+viewRect.y-headerHeight));
        //System.out.println(row);

        return convertRowIndexToModel(row);
    }
    
    @Override
    public int convertRowIndexToModel(int rowIndex) {
        if (rowIndex == -1) {
            return -1;
        }
        return super.convertRowIndexToModel(rowIndex);
    }
    
    @Override
    public int convertRowIndexToView(int rowIndex) {
        if (rowIndex == -1) {
            return -1;
        }
        return super.convertRowIndexToView(rowIndex);
    }

    @Override
    public void addViewChangeListerner(ViewChangeListener listener) {
        if (viewChangeListeners == null) {
            viewChangeListeners = new ArrayList<>();
        }
        viewChangeListeners.add(listener);
    }

    @Override
    public void removeViewChangeListener(ViewChangeListener listener) {
        if (viewChangeListeners == null) {
            return;
        }
        viewChangeListeners.remove(listener);
    }
     ArrayList<ViewChangeListener> viewChangeListeners = null;

    @Override
    public void dispatchViewChange() {
        if (viewChangeListeners == null) {
            return;
        }
        int nbListener = viewChangeListeners.size();
        for (int i=0;i<nbListener;i++) {
            viewChangeListeners.get(i).viewChanged();
        }
    }
    
}
