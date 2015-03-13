package fr.proline.studio.python.data;

import fr.proline.studio.table.CompoundTableModel;
import java.awt.EventQueue;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.python.core.PyObject;
import org.python.core.PyTuple;

/**
 *
 * @author JM235353
 */
public class Table {
    
    private static JXTable m_currentTable = null;
    
    public static void setCurrentTable(JXTable t) {
        m_currentTable = t;
    }
    
    public static JXTable getCurrentTable() {
        return m_currentTable;
    }
    
    public static ColRef col(String colName) {
        if (m_currentTable == null) {
            return null;
        }
        
        int nbCol = m_currentTable.getColumnCount();
        for (int i=0;i<nbCol;i++) {
            String name = m_currentTable.getColumnName(i);
            if (colName.compareToIgnoreCase(name) == 0) {
                int modelCol = m_currentTable.convertColumnIndexToModel(i);
                return new ColRef(modelCol+1, (CompoundTableModel) m_currentTable.getModel());
            }
        }
        throw new RuntimeException("Column not found "+colName);
    }
    
    public static ColRef col(int colIndex) {
 
        int nbCol = m_currentTable.getColumnCount();
        if ((colIndex<1) || (colIndex>nbCol)) {
            throw new IndexOutOfBoundsException("No Column at index "+colIndex);
        }
        return new ColRef(m_currentTable.convertColumnIndexToModel(colIndex-1), (CompoundTableModel) m_currentTable.getModel());
    }
    
    public static PyTuple col(int colIndex1, int colIndex2) {
        PyObject[] objects = new PyObject[colIndex2-colIndex1+1];
        for (int i=colIndex1;i<=colIndex2;i++) {
            ColRef c = col(i);
            objects[i-colIndex1] = c;
        }
        return new PyTuple(objects);
    }
    
    public static void addColumn(Col col) {

        
        final JXTable table = Table.getCurrentTable();
        
        List<TableColumn> columns = table.getColumns(true);
        final int nbColumns = columns.size();
        final boolean[] visibilityArray = new boolean[nbColumns];
        for (int i=0;i<nbColumns;i++) {
            visibilityArray[i] = ((TableColumnExt) columns.get(i)).isVisible();
        }

        
        
        TableModel model = Table.getCurrentTable().getModel();
        if (model instanceof CompoundTableModel) {
            ((CompoundTableModel) model).addModel(new ExprTableModel(col, ((CompoundTableModel) model).getLastNonFilterModel()));
        }
        
        /*EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {*/
                columns = table.getColumns(true);
                for (int i = 0; i < nbColumns; i++) {
                    if (!visibilityArray[i]) {
                        ((TableColumnExt) columns.get(i)).setVisible(false);
                    }
                }
            /*}
            
        });*/
        

        
    }
}
