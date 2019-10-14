/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.filter;

import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import java.util.HashSet;
import org.jdesktop.swingx.JXTable;


/**
 * Interface which must be respected to do a Filter Model
 * @author JM235353
 */
public interface FilterTableModelInterface extends GlobalTableModelInterface, FilterMapInterface {

    public void setTableModelSource(GlobalTableModelInterface tableModelSource);
    public GlobalTableModelInterface getTableModelSource();
    
    public void initFilters();
    
    public Filter getColumnFilter(int col);

    public boolean filter(); 
    
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
