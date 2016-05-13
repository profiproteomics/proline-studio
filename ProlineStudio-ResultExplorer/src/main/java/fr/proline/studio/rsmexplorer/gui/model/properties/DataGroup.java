package fr.proline.studio.rsmexplorer.gui.model.properties;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public abstract class DataGroup {

    
    public static final String[] COLUMN_NAMES = {"Group", "Type"};
    
    public static final int COLTYPE_GROUP_NAME = 0;
    public static final int COLTYPE_PROPERTY_NAME = 1;
    
    

    private final String m_name;
    protected int m_rowStart;

    private PropertiesRenderer m_groupRenderer = null;
    private PropertiesRenderer m_groupSubRenderer = null;


    public DataGroup(String name, int rowStart) {
        m_name = name;
        m_rowStart = rowStart;
        
        m_groupRenderer = new PropertiesRenderer(false);
        m_groupSubRenderer = new PropertiesRenderer(false);
    }

    private String getName(int row) {
        if (row == m_rowStart) {
            return m_name;
        }
        
        JTable table = m_groupRenderer.getTable(); //JPM.WART
        if ((table == null) || table.getRowSorter().getSortKeys().isEmpty()) {
            return ""; // no sorting, for group name, we show no text
        }
        return m_name;
    }

    public TableCellRenderer getRenderer(int row) {
        if (row == m_rowStart) {
            return m_groupRenderer;
        } else {
            return m_groupSubRenderer;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COLTYPE_GROUP_NAME) {
            return getName(rowIndex);
        }
        if (rowIndex == m_rowStart) {
            return "";
        }

        if (columnIndex == COLTYPE_PROPERTY_NAME) {
            return getGroupNameAt(rowIndex - m_rowStart - 1);
        }

        return getGroupValueAt(rowIndex - m_rowStart - 1, columnIndex - 2);
    }

    public boolean isFirstRow(int row) {
        return row == m_rowStart;
    }

    public abstract String getGroupValueAt(int rowIndex, int columnIndex);

    public abstract String getGroupNameAt(int rowIndex);

    public abstract Color getGroupColor(int row);

    public int getRowCount() {
        return getRowCountImpl() + 1;
    }

    public abstract int getRowCountImpl();
}
