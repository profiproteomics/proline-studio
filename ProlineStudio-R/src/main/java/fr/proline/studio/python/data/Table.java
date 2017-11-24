package fr.proline.studio.python.data;


import fr.proline.studio.python.model.ExprTableModel;
import fr.proline.studio.extendedtablemodel.DiffDataModel;
import fr.proline.studio.extendedtablemodel.JoinDataModel;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.extendedtablemodel.MultiJoinDataModel;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

/**
 * Python object corresponding to a Table
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
        m_model = model.getFrozzenModel();
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
    
    public int getNbCols() {
        if (m_model != null) {
            return m_model.getColumnCount();
        }

        if (m_colums != null) {
            return m_colums.size();
        }
        
        return 0;
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
                if (model.getColumnCount() < colIndex) {
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
        addColumn(col, null, null);
    }
    public void addColumn(Col col, Object colExtraInfo, TableCellRenderer colRenderer) {

        
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
                ExprTableModel exprModel = new ExprTableModel(col, colRenderer, ((CompoundTableModel) model).getLastNonFilterModel());
                exprModel.addExtraColumnInfo(m_model.getColumnCount()-1, colExtraInfo);
                ((CompoundTableModel) model).addModel(exprModel);
            }

            columns = table.getColumns(true);
            for (int i = 0; i < nbColumns; i++) {
                if (!visibilityArray[i]) {
                    ((TableColumnExt) columns.get(i)).setVisible(false);
                }
            }
        } else {
            // we have only the model
            m_model = new ExprTableModel(col, colRenderer, m_model);
            ((ExprTableModel)m_model).addExtraColumnInfo(m_model.getColumnCount()-1, colExtraInfo);
        }

    }
    
    public static Table join(Table t1, Table t2) {
        JoinDataModel joinDataModel = new JoinDataModel();
        joinDataModel.setData(t1.getModel(), t2.getModel());
        return new Table(joinDataModel);
    }
    
    public static Table join(Table t1, Table t2, Integer table1Key1, Integer table2Key1, Double tolerance1, Integer table1Key2, Integer table2Key2, Double tolerance2, Boolean showSourceColumn) {
        JoinDataModel joinDataModel = new JoinDataModel();
        joinDataModel.setData(t1.getModel(), t2.getModel(), table1Key1, table2Key1, tolerance1, table1Key2, table2Key2, tolerance2, showSourceColumn);
        return new Table(joinDataModel);
    }
    
    public static Table join(ArrayList<Table> tables) {
        
        int nbTables = tables.size();
        ArrayList<GlobalTableModelInterface> models = new ArrayList<>(nbTables);
        for (int i=0;i<nbTables;i++) {
            models.add(tables.get(i).getModel());
        }
        
        MultiJoinDataModel joinDataModel = new MultiJoinDataModel();
        joinDataModel.setData(models, null, null, null, null, false);
        return new Table(joinDataModel);
    }

    public static Table join(ArrayList<Table> tables, ArrayList<Integer> tableKey1,  Double tolerance1,  ArrayList<Integer> tableKey2, Double tolerance2, Boolean showSourceColumn) {
        int nbTables = tables.size();
        ArrayList<GlobalTableModelInterface> models = new ArrayList<>(nbTables);
        for (int i = 0; i < nbTables; i++) {
            models.add(tables.get(i).getModel());
        }

        MultiJoinDataModel joinDataModel = new MultiJoinDataModel();
        joinDataModel.setData(models, tableKey1, tolerance1, tableKey2, tolerance2, showSourceColumn);
        return new Table(joinDataModel);
    }
    
    
    
    public static Table diff(Table t1, Table t2) {
        DiffDataModel diffDataModel = new DiffDataModel();
        diffDataModel.setData(t1.getModel(), t2.getModel());
        return new Table(diffDataModel);
    }
    
    public static Table diff(Table t1, Table t2, Integer table1Key1, Integer table2Key1, Double tolerance1, Integer table1Key2, Integer table2Key2, Double tolerance2, Boolean showSourceColumn) {
        DiffDataModel diffDataModel = new DiffDataModel();
        diffDataModel.setData(t1.getModel(), t2.getModel(), table1Key1, table2Key1, tolerance1, table1Key2, table2Key2, tolerance2, showSourceColumn);
        return new Table(diffDataModel);
    }
}
