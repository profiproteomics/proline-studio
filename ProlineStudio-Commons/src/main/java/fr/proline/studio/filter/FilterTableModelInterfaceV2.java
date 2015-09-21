package fr.proline.studio.filter;

import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.HashSet;
import org.jdesktop.swingx.JXTable;


/**
 * Interface which must be respected to do a Filter Model
 * @author JM235353
 */
public interface FilterTableModelInterfaceV2 extends GlobalTableModelInterface, FilterMapInterface {

    public void setTableModelSource(GlobalTableModelInterface tableModelSource);
    public GlobalTableModelInterface getTableModelSource();
    
    public void initFilters();
    
    public Filter getColumnFilter(int col);

    public void filter(); 
    
    public boolean filter(int row);
    
    public boolean filter(int row, int col);

    public int convertRowToOriginalModel(int row);
    
    public int convertOriginalModelToRow(int row);
    
    /**
     * Restrain rows of the table model to some specified rows before filtering
     * @param restrainRowSet  Set corresponding to the rows kept
     */
    public void restrain(HashSet<Integer> restrainRowSet);
    
    public HashSet<Integer> getRestrainRowSet();
    
    public boolean hasRestrain();
    
    
    public int search(JXTable table, Filter f, boolean newSearch);

    
}
