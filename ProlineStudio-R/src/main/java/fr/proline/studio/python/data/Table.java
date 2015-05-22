package fr.proline.studio.python.data;


import fr.proline.studio.comparedata.DiffDataModel;
import fr.proline.studio.comparedata.JoinDataModel;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;

import java.util.HashMap;
import java.util.List;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

/**
 *
 * @author JM235353
 */
public class Table extends PyObject {
    
    private static HashMap<Integer,JXTable> m_tableMap = null;
    
    private HashMap<Integer, ColRef> m_colums = null;
    
    
    private int m_index = -1;
    private GlobalTableModelInterface m_model = null;
    
    public Table(int index) {
        m_index = index;
    }
    
    public Table(GlobalTableModelInterface model) {
        m_model = model;
    }
    
    public static Table get(int index) {
        return new Table(index);
    }
    
    public JXTable getGraphicalTable() {
        return m_tableMap.get(m_index);
    }
    
    public GlobalTableModelInterface getModel() {
        JXTable table = getGraphicalTable();
        if (table != null) {
            return (GlobalTableModelInterface) table.getModel();
        }
        
        return m_model;
    }
    
    public ColRef getCol(int colIndex) {
        if (m_colums == null) {
            m_colums = new HashMap<>();
        } 
        ColRef col = m_colums.get(colIndex);
        if (col == null) {
            JXTable t = m_tableMap.get(m_index);
            if (t != null) {
                CompoundTableModel model = (CompoundTableModel) t.getModel();
                if (model.getColumnCount() <= colIndex) {
                    String error = "ColRef.getCol(" + colIndex + ") : Out of bound error";
                    throw Py.IndexError(error);
                }
                col = new ColRef(this, colIndex-1, model);
            } else {
                col = new ColRef(this, colIndex-1, m_model);
            }
            
            m_colums.put(colIndex, col);
        }

        return col;

    }
    
    /**
     * __finditem__ is useful for [] call
     *
     * @param key
     * @return
     */
    @Override
    public PyObject __finditem__(PyObject key) {
        if (key instanceof PyInteger) {
            ColRef col = getCol(((PyInteger) key).getValue());
            return col;
        }
        throw Py.TypeError("Unexpected Type Found " + key.getClass().getName());
    }
    
    @Override
    public void __setitem__(PyObject key, PyObject value) {
        throw Py.TypeError("Unexpected Type Found " + key.getClass().getName());
    }
    
    @Override
    public int __len__() {
        JXTable t = m_tableMap.get(m_index);
        return t.getColumnCount(true);
    }
    
    
    public static void setTables(HashMap<Integer,JXTable> tableMap) {
        m_tableMap = tableMap;
    }
    
    public static JXTable getTable(int index) {
        return m_tableMap.get(index);
    }
    

    
    public void addColumn(Col col) {

        
        JXTable table = m_tableMap.get(m_index);
        
        if (table != null) {

            List<TableColumn> columns = table.getColumns(true);
            final int nbColumns = columns.size();
            final boolean[] visibilityArray = new boolean[nbColumns];
            for (int i = 0; i < nbColumns; i++) {
                visibilityArray[i] = ((TableColumnExt) columns.get(i)).isVisible();
            }

            TableModel model = table.getModel();
            if (model instanceof CompoundTableModel) {
                ((CompoundTableModel) model).addModel(new ExprTableModel(col, ((CompoundTableModel) model).getLastNonFilterModel()));
            }

            columns = table.getColumns(true);
            for (int i = 0; i < nbColumns; i++) {
                if (!visibilityArray[i]) {
                    ((TableColumnExt) columns.get(i)).setVisible(false);
                }
            }
        } else {
            // we have only the model
            m_model = new ExprTableModel(col, m_model);
        }

    }
    
    public static Table join(Table t1, Table t2) {
        JoinDataModel joinDataModel = new JoinDataModel();
        joinDataModel.setData(t1.getModel(), t2.getModel());
        return new Table(joinDataModel);
    }
    
    public static Table join(Table t1, Table t2, Integer key1, Integer key2) {
        JoinDataModel joinDataModel = new JoinDataModel();
        joinDataModel.setData(t1.getModel(), t2.getModel(), key1, key2);
        return new Table(joinDataModel);
    }
    
    public static Table diff(Table t1, Table t2) {
        DiffDataModel diffDataModel = new DiffDataModel();
        diffDataModel.setData(t1.getModel(), t2.getModel());
        return new Table(diffDataModel);
    }
    
    public static Table diff(Table t1, Table t2, Integer key1, Integer key2) {
        DiffDataModel diffDataModel = new DiffDataModel();
        diffDataModel.setData(t1.getModel(), t2.getModel(), key1, key2);
        return new Table(diffDataModel);
    }
}
