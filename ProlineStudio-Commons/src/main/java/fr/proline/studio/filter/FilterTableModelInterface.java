package fr.proline.studio.filter;

import fr.proline.studio.progress.ProgressInterface;
import java.util.HashSet;

/**
 * A Table model must implements this interface to be able to be filtered
 * @author JM235353
 */
public interface FilterTableModelInterface extends ProgressInterface {
    
    public void initFilters();
    
    public Filter[] getFilters();
    
    public Filter getColumnFilter(int col);

    public void filter(); 
    
    public boolean filter(int row, int col);
    
    public boolean filter(int row);
    
    public int convertRowToOriginalModel(int row);
    
    /**
     * Restrain rows of the table model to some specified rows before filtering
     * @param restrainRowSet  Set corresponding to the rows kept
     */
    public void restrain(HashSet<Integer> restrainRowSet);
    
    public HashSet<Integer> getRestrainRowSet();
    
    public boolean hasRestrain();

}
