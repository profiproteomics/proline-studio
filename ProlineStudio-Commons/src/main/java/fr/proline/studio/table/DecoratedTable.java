package fr.proline.studio.table;

import com.thierry.filtering.TableSelection;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.utils.RelativePainterHighlighter;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.AbstractLayoutPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.util.PaintUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table which has a bi-color striping, an ability to select columns viewed and
 * the possibility to display an "histogram" on a column
 *
 * @author JM235353
 */
public abstract class DecoratedTable extends JXTable implements CrossSelectionInterface {

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio");
   
    private RelativePainterHighlighter.NumberRelativizer m_relativizer = null;
    
    private TablePopupMenu m_popupMenu;
    
    public DecoratedTable() {

        // allow user to hide/show columns
        setColumnControlVisible(true);

        // highlight one line of two
        addHighlighter(HighlighterFactory.createSimpleStriping());
        setGridColor(Color.lightGray);
        setRowHeight(16);
        setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED);
        
        TableSelection.installCopyAction(this);
  
        TablePopupMenu popup = initPopupMenu();
        if (popup != null) {
            setTablePopup(popup);
        }
    }

    

    /*public void displayColumnAsPercentage(int column) {
        displayColumnAsPercentage(column, AbstractLayoutPainter.HorizontalAlignment.RIGHT);
    }
    public void displayColumnAsPercentage(int column, AbstractLayoutPainter.HorizontalAlignment alignment) {
        // Display of the Score Column as a percentage
        Color base = PaintUtils.setSaturation(Color.GREEN, .7f);
        MattePainter matte = new MattePainter(PaintUtils.setAlpha(base, 125));
        RelativePainterHighlighter highlighter = new RelativePainterHighlighter(matte);
        highlighter.setHorizontalAlignment(alignment);

        m_relativizer = new RelativePainterHighlighter.NumberRelativizer(column, 0, 100);
        highlighter.setRelativizer(m_relativizer);
        highlighter.setHighlightPredicate(new HighlightPredicate.ColumnHighlightPredicate(column));
        addHighlighter(highlighter);

    }*/
    
    /*public RelativePainterHighlighter.NumberRelativizer getRelativizer() {
        return m_relativizer;
    }*/
    
    public String getToolTipForHeader(int modelColumn) {
        return ((DecoratedTableModelInterface) getModel()).getToolTipForHeader(modelColumn);
    }

    
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new CustomTooltipTableHeader(this);
    }

    @Override
    public void select(ArrayList<Long> uniqueIds) {
        ListSelectionModel selModel = getSelectionModel();
        selModel.clearSelection();
        
        TableModel model = getModel();
        
        if (model instanceof CompareDataInterface) {
            CompareDataInterface compareDataInterface = (CompareDataInterface) model;
            for (Long id : uniqueIds) {
                int row = compareDataInterface.uniqueId2Row(id);
                if (row == -1) {
                    // id not found (possible after a filtering for instance)
                    continue;
                }
                row = convertRowIndexToView(row);
                selModel.addSelectionInterval(row, row);
            }
        } else {
            for (Long id : uniqueIds) {
                int row = convertRowIndexToView(id.intValue());  // should not happen
                selModel.addSelectionInterval(row, row);
            }
        }
        
        
       
        int minRow = selModel.getMinSelectionIndex();
        if (minRow != -1) {
            scrollRowToVisible(minRow);
        }
    }
    
    @Override
    public ArrayList<Long> getSelection() {

        ArrayList<Long> selectionList = new ArrayList<>();

        ListSelectionModel selModel = getSelectionModel();
        AbstractTableModel model = (AbstractTableModel) getModel();

        if (model instanceof CompareDataInterface) {
            CompareDataInterface compareDataInterface = (CompareDataInterface) model;
            for (int i = 0; i < model.getRowCount(); i++) {
                int row = convertRowIndexToView(i);
                if (selModel.isSelectedIndex(row)) {
                    selectionList.add(compareDataInterface.row2UniqueId(i));
                }
            }
        } else {
            for (int i = 0; i < model.getRowCount(); i++) {
                int row = convertRowIndexToView(i);
                if (selModel.isSelectedIndex(row)) {
                    selectionList.add((long) i);
                }
            }
        }

        return selectionList;
    }
    
    private class CustomTooltipTableHeader extends JXTableHeader {
        
        private DecoratedTable m_table;
        
        public CustomTooltipTableHeader(DecoratedTable table) {
            super(table.getColumnModel());
            m_table = table;
        }
        
        @Override
        public String getToolTipText(MouseEvent e) {
            
            Point p = e.getPoint();
            int column = columnAtPoint(p);
            if (column != -1) {
                column = m_table.convertColumnIndexToModel(column);
                String tooltip = getToolTipForHeader(column);
                if (tooltip != null) {
                    return tooltip;
                }
            }

            return super.getToolTipText(e);
        }
    }
    
    private void setTablePopup(TablePopupMenu popupMenu) {
        if (m_popupMenu == null) {
            addMouseListener(new TablePopupMouseAdapter(this));
        }
                    
        popupMenu.preparePopup();
        m_popupMenu = popupMenu;
    }
    
    public TablePopupMenu getTablePopup() {
        return m_popupMenu;
    }

    public abstract TablePopupMenu initPopupMenu();

    public abstract void prepostPopupMenu();
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        TableModel model = getModel();
        if (model instanceof GlobalTableModelInterface) {
            int columnInModel = convertColumnIndexToModel(column);
            TableCellRenderer renderer = ((GlobalTableModelInterface) model).getRenderer(columnInModel);
            if (renderer != null) {
                return renderer;
            }
        }

        return super.getCellRenderer(row, column);
    }
    
}
