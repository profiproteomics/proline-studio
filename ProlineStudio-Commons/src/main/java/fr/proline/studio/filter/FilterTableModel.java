package fr.proline.studio.filter;

import fr.proline.studio.utils.LazyTable;
import fr.proline.studio.utils.LazyTableModel;

/**
 *
 * @author JM235353
 */
public abstract class FilterTableModel extends LazyTableModel implements FilterTableModelInterface {

    protected Filter[] m_filters = null;

    
    public FilterTableModel(LazyTable table) {
        super(table);
    }
    
    @Override
    public Filter[] getFilters() {
       
        initFilters();
        
        int nbFilter = m_filters.length;
        int nbNull = 0;
        for (int i=0;i<nbFilter;i++) {
            if (m_filters[i] == null) {
                nbNull++;
            }
        }
        
        if (nbNull == 0) {
            return m_filters;
        }
        
        Filter[] filters = new Filter[nbFilter-nbNull];
        int j=0;
        for (int i=0;i<nbFilter;i++) {
            if (m_filters[i] != null) {
                filters[j++] = m_filters[i];
            }
        }
        
        return filters;
    }

    @Override
    public Filter getColumnFilter(int col) {
    
        initFilters();
        return m_filters[col];
        
    }

    @Override
    public boolean filter(int row) {
        int nbCol = getColumnCount();
        for (int i=0;i<nbCol;i++) {
            if (!filter(row, i)) {
                return false;
            }
        }
        return true;
    }
    
}
