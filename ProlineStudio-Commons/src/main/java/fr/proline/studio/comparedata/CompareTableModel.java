package fr.proline.studio.comparedata;


import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterTableModel;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import java.util.ArrayList;


/**
 *
 * @author JM235353
 */
public class CompareTableModel extends FilterTableModel {

    private CompareDataInterface m_dataInterface = null;
    

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    
    public CompareTableModel(CompareDataInterface dataInterface) {
        m_dataInterface = dataInterface;
        
    }
    
    public void setDataInterface(CompareDataInterface dataInterface) {
        m_dataInterface = dataInterface;
        m_filters = null; // reinit filters
        fireTableStructureChanged();
    }
    
    @Override
    public int getRowCount() {
        if (m_dataInterface == null) {
            return 0;
        }
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        
        
        return m_dataInterface.getRowCount();
    }

    @Override
    public int getColumnCount() {
        if (m_dataInterface == null) {
            return 0;
        }
        return m_dataInterface.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (m_dataInterface == null) {
            return null;
        }
        
        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        
        return m_dataInterface.getDataValueAt(rowIndex, columnIndex);
    }
    
    @Override
    public String getColumnName(int column) {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getDataColumnIdentifier(column);
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getDataColumnClass(columnIndex);
    }

    @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(col);
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            
            for (int i=0;i<nbCol;i++) {
                Class columnClass = getColumnClass(i);
                if (columnClass.equals(String.class)) {
                    m_filters[i] = new StringFilter(getColumnName(i));
                } else if (columnClass.equals(Integer.class)) {
                    m_filters[i] = new IntegerFilter(getColumnName(i));
                } else if (columnClass.equals(Double.class)) {
                    m_filters[i] = new DoubleFilter(getColumnName(i));
                } else if (columnClass.equals(Float.class)) {
                    m_filters[i] = new DoubleFilter(getColumnName(i));
                } else {
                    m_filters[i] = null;
                }
            }

        }
    }

    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }

        Object data = getValueAt(row, col);
        
         Class columnClass = getColumnClass(col);
        
         if (columnClass.equals(String.class)) {
            return ((StringFilter) filter).filter((String) data);
        } else if (columnClass.equals(Integer.class)) {
            return ((IntegerFilter) filter).filter((Integer) data);
        } else if (columnClass.equals(Double.class)) {
            return ((DoubleFilter) filter).filter((Double) data);
        } else if (columnClass.equals(Float.class)) {
            return ((DoubleFilter) filter).filter((Float) data);
        }

        
        return true; // should never happen
    }

    @Override
    public void filter() {

        m_isFiltering = true;
        try {

            int nbData = getRowCount();
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(Integer.valueOf(i));
            }
        } finally {
            m_isFiltering = false;
        }
        fireTableDataChanged();
    }


    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getLoadingPercentage() {
        return 100;
    }



    
}
