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
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import javax.swing.event.TableModelListener;

/**
 *
 * @author MB243701
 */
public class DataClearProjectTable extends DecoratedMarkerTable{
    
    
    public DataClearProjectTable() {
        super();
    }


    @Override
    public TablePopupMenu initPopupMenu() {
        return null;
    }

    @Override
    public void prepostPopupMenu() {
        // nothing to do
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        getModel().addTableModelListener(l);
    }
    
}
