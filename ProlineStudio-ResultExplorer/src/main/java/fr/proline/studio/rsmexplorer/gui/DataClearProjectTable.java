/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
