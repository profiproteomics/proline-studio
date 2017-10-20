package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.LongFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 * Model to perform a difference between two models (joined thanks to key columns) with the same columns
 * @author JM235353
 */
public class DiffDataModel extends AbstractJoinDataModel {

    private ArrayList<Integer> m_keysColumns1 = new ArrayList<>();
    private ArrayList<Integer> m_keysColumns2 = new ArrayList<>();
    private final ArrayList<Integer> m_allColumns1 = new ArrayList<>();
    private final ArrayList<Integer> m_allColumns2 = new ArrayList<>();

    
    @Override
    protected void setColumns() {
        
        // construct a map of the columns in data2
        HashMap<String, Integer> mapColumn2 = new HashMap<>();
        int nbColumn = m_data2.getColumnCount();
        for (int i = 0; i < nbColumn; i++) {
            if ((i == m_selectedTable2Key1) || (i == m_selectedTable2Key2)) {
                continue;
            }
            mapColumn2.put(m_data2.getDataColumnIdentifier(i), i);
        }
        
        // find corresponding columns in data1 and data2
        nbColumn = m_data1.getColumnCount();
        for (int i = 0; i < nbColumn; i++) {
            if ((i == m_selectedTable1Key1) || (i == m_selectedTable1Key2)) {
                continue;
            }
            String columnIdentifier = m_data1.getDataColumnIdentifier(i);
            if (!mapColumn2.containsKey(columnIdentifier)) {
                continue;
            }
            
            int col2 = mapColumn2.get(columnIdentifier);
            
            // check classes
            Class c1 = m_data1.getDataColumnClass(i);
            Class c2 = m_data2.getDataColumnClass(col2);
            if (!c1.equals(c2)) {
                if (! ((c1.equals(Long.class) && c2.equals(Integer.class)) || (c1.equals(Integer.class) && c2.equals(Long.class)) || 
                        (c1.equals(Float.class) && c2.equals(Double.class)) || (c1.equals(Double.class) && c2.equals(Float.class))) ) {
                    continue;
                }
            }
            
            m_allColumns1.add(i);
            m_allColumns2.add(col2);
        }

        // First Key column(s)
        Class c1 = m_data1.getDataColumnClass(m_selectedTable1Key1);
        if (c1.equals(Double.class) || c1.equals(Float.class)) {
            m_keysColumns1.add(m_selectedTable1Key1);   // For Double and Float keys, we show both values from Table 1 and Table 2
            m_keysColumns2.add(-1);
            m_keysColumns1.add(-1);
            m_keysColumns2.add(m_selectedTable2Key1);
        } else {
            m_keysColumns1.add(m_selectedTable1Key1);  // For Other keys (String, Integer) the key is the same, or absent in one table
            m_keysColumns2.add(m_selectedTable2Key1);
        }
        
        // Second Key column(s)
        if (m_selectedTable1Key2 != -1) {
            Class c2 = m_data2.getDataColumnClass(m_selectedTable1Key2);
            if (c2.equals(Double.class) || c2.equals(Float.class)) {
                m_keysColumns1.add(m_selectedTable1Key2);   // For Double and Float keys, we show both values from Table 1 and Table 2
                m_keysColumns2.add(-1);
                m_keysColumns1.add(-1);
                m_keysColumns2.add(m_selectedTable2Key2);
            } else {
                m_keysColumns1.add(m_selectedTable1Key2);  // For Other keys (String, Integer) the key is the same, or absent in one table
                m_keysColumns2.add(m_selectedTable2Key2);
            }
        }
        
    }



    @Override
    public int getColumnCount() {
        return m_keysColumns1.size()+m_allColumns1.size()+ (m_showSourceColumn ? 1 : 0);
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        if (!joinPossible()) {
            return null;
        }
        
        // Columns with keys
        if (columnIndex<m_keysColumns1.size()) {
            int colIndexCur = m_keysColumns1.get(columnIndex);
            String col1 = null;
            String col2 = null;
            if (colIndexCur != -1) {
                col1 = m_data1.getDataColumnIdentifier(colIndexCur);
            }
            colIndexCur = m_keysColumns2.get(columnIndex);
             if (colIndexCur != -1) {
                col2 = m_data2.getDataColumnIdentifier(colIndexCur);
            }
            
            if (col1 == null) {
                return col2+" "+m_data2.getName();
            } else if (col2 == null) {
                return col1+" "+m_data1.getName();
            } else {
                if (col1.compareTo(col2) == 0) {
                    return col1;
                } else {
                    return col1 + "/" + col2;
                }
            }
        }
        
        columnIndex -= m_keysColumns1.size();

        if (m_showSourceColumn) {
            if (columnIndex == 0) {
                return "Source";
            }
            
            columnIndex--;
        }
        
 
        return m_data1.getDataColumnIdentifier(m_allColumns1.get(columnIndex));
  
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (!joinPossible()) {
            return null;
        }
        // Columns with keys
        if (columnIndex<m_keysColumns1.size()) {
            int index1 = m_keysColumns1.get(columnIndex);
            if (index1 != -1) {
                return m_data1.getDataColumnClass(index1);
            }
            return m_data1.getDataColumnClass(m_keysColumns2.get(columnIndex));
        }
        
        columnIndex -= m_keysColumns1.size();

        if (m_showSourceColumn) {
            if (columnIndex == 0) {
                return String.class;
            }
            columnIndex--;
        }
        
        Class c = m_data1.getDataColumnClass(m_allColumns1.get(columnIndex));

        
        if ((c.equals(Double.class)) || (c.equals(Float.class)) || (c.equals(Long.class)) || (c.equals(Integer.class))) {
            return c;
        }
        return String.class;
        
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return getDataColumnClass(columnIndex);
    }
    
    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        
        if (!joinPossible()) {
            return null;
        }
        
        Integer row1 = m_rowsInTable1.get(rowIndex);
        Integer row2 = m_rowsInTable2.get(rowIndex);
        
        
        // Columns with keys
        if (columnIndex<m_keysColumns1.size()) {
            int colIndexCur = m_keysColumns1.get(columnIndex);
            if ((colIndexCur != -1) && (row1 != null)) {
                return m_data1.getDataValueAt(row1, colIndexCur);
            } else {
                colIndexCur = m_keysColumns2.get(columnIndex);
                if ((colIndexCur !=-1) && (row2 != null)) {
                    return m_data2.getDataValueAt(row2, colIndexCur);
                } else {
                    return null;
                }
            }
        }
        
        columnIndex -= m_keysColumns1.size();
        
        // Source Column
        if (m_showSourceColumn) {
            if (columnIndex == 0) {
                if ((row1 == null) && (row2 != null)) {
                    return m_data2.getName();
                } else if ((row1 != null) && (row2 == null)) {
                    return m_data1.getName();
                } else {
                    return m_data1.getName()+","+m_data2.getName();
                }
                
            }
            columnIndex--;
        }
        
        Object value1 = null;
        if (row1 != null) {
            value1 = m_data1.getDataValueAt(row1, m_allColumns1.get(columnIndex));
        }

        Object value2 = null;
        if (row2 != null) {
            value2 = m_data2.getDataValueAt(row2, m_allColumns2.get(columnIndex));
        }
        
        if ((value1 == null) || (value2 == null)) {
            return null;
        }
        
        if (value1 instanceof Double) {
            if (value2 instanceof Double) {
               return new Double(((Double) value1)-((Double) value2)); 
            } else if  (value2 instanceof Float) {
               return new Double(((Double) value1)-((Float) value2)); 
            }
        }
        
        if (value1 instanceof Float) {
            if (value2 instanceof Double) {
               return new Float(((Float) value1)-((Double) value2)); 
            } else if  (value2 instanceof Float) {
               return new Float(((Float) value1)-((Float) value2)); 
            }
        }
        
        if (value1 instanceof Long) {
            if (value2 instanceof Long) {
                return new Long(((Long) value1) - ((Long) value2));
            } else if (value2 instanceof Integer) {
                return new Long(((Long) value1) - ((Integer) value2));
            }
        }

        if (value1 instanceof Integer) {
            if (value2 instanceof Long) {
                return new Integer( (int) (((Integer) value1) - ((Long) value2)) );
            } else if (value2 instanceof Integer) {
                return new Integer(((Integer) value1) - ((Integer) value2));
            }
        }


        String value1String = value1.toString();
        String value2String = value2.toString();
        if (value1String.compareTo(value2String) == 0) {
            return "";
        } else {
            return "<>";
        }

        
    }
    
    @Override
    public String getColumnName(int column) {
        return getDataColumnIdentifier(column);
    }

    @Override
    public int getInfoColumn() {
        return 0;
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public Long getTaskId() {
        return -1l; // not used
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null; // not used
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        // not used
    }

    @Override
    public void sortingChanged(int col) {
        return; // not used
    }

    @Override
    public int getSubTaskId(int col) {
        return -1; // not used
    }

    @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null; // not used
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        
        int nbColumns = getColumnCount();
        for (int i=0;i<nbColumns;i++) {
            Class c = getDataColumnClass(i); 
            if (c.equals(Double.class)) {
                filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
            } else if (c.equals(Integer.class)) {
                filtersMap.put(i, new IntegerFilter(getColumnName(i), null, i));
            } else if (c.equals(String.class)) {
                filtersMap.put(i, new StringFilter(getColumnName(i), null, i));
            } else if (c.equals(Long.class)) {
                filtersMap.put(i, new LongFilter(getColumnName(i), null, i));
            }
        }
        
    }



    @Override
    public PlotType getBestPlotType() {
         return null; //JPM.TODO
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
         return null;
    }


    @Override
    public String getExportRowCell(int row, int col) {
        Object o = getValueAt(row, col);
        if (o == null) {
            return "";
        }
        return o.toString();
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }
    
    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        //JPM.TODO
        return null;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }
    
    
    @Override
    public Object getColValue(Class c, int col) {
        return null; // could be enhanced
    }
    
}
