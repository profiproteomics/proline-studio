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
package fr.proline.studio.table;


import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JTable;

/**
 * Base class for all actions on JTable
 * @author JM235353
 */
public abstract class AbstractTableAction extends AbstractAction {
    
    private TablePopupMenu m_popup = null;
    
    public AbstractTableAction(String name) {
        super(name);
    }
    
    public Component getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public void setTablePopupMenu(TablePopupMenu popup) {
        m_popup = popup;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (m_popup == null) {
            return;
        }
        
        actionPerformed(m_popup.getCol(), m_popup.getRow(), m_popup.getSelectedRows(), m_popup.getTable());
    }
    
    public abstract void actionPerformed(int col, int row, int[] selectedRows, JTable table);
    

    
    public abstract void updateEnabled(int row, int col, int[] selectedRows, JTable table);
    

    
}
