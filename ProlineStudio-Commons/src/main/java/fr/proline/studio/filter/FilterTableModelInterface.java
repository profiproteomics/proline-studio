package fr.proline.studio.filter;

/**
 * A Table model must implements this interfact to be able to be filtered
 * @author JM235353
 */
public interface FilterTableModelInterface {
    
    public void initFilters();
    
    public Filter[] getFilters();
    
    public Filter getColumnFilter(int col);

    public void filter(); 
    
    public boolean filter(int row, int col);
    
    public boolean filter(int row);

    
        
}
