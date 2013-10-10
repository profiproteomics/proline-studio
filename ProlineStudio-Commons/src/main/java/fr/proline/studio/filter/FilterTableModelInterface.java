package fr.proline.studio.filter;

import fr.proline.studio.progress.ProgressInterface;

/**
 * A Table model must implements this interfact to be able to be filtered
 * @author JM235353
 */
public interface FilterTableModelInterface extends ProgressInterface {
    
    public void initFilters();
    
    public Filter[] getFilters();
    
    public Filter getColumnFilter(int col);

    public void filter(); 
    
    public boolean filter(int row, int col);
    
    public boolean filter(int row);

}
