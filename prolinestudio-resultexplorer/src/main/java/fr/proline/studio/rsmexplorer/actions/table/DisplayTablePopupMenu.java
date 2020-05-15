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
package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class DisplayTablePopupMenu extends TablePopupMenu {
    
    private DataBoxPanelInterface m_databoxProvider;
    private final DisplayMenuAction m_displayMenuAction;
    
    public DisplayTablePopupMenu(DataBoxPanelInterface databoxProvider) {
        super(true);

        m_databoxProvider = databoxProvider;

        m_displayMenuAction = new DisplayMenuAction();
        addAction(m_displayMenuAction);

        addAction(null);

        addAction(new RestrainAction() {
            @Override
            public void filteringDone() {
                m_databoxProvider.getDataBox().addDataChanged(ExtendedTableModelInterface.class);
                m_databoxProvider.getDataBox().propagateDataChanged();
            }
        });
        addAction(new ClearRestrainAction() {
            @Override
            public void filteringDone() {
                m_databoxProvider.getDataBox().addDataChanged(ExtendedTableModelInterface.class);
                m_databoxProvider.getDataBox().propagateDataChanged();
            }
        });
    }
    

    
    public void prepostPopupMenu() {
                
        AbstractDataBox databox = m_databoxProvider.getDataBox();
        m_displayMenuAction.populate( databox);
    }
}
