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

import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.AbstractDataBox.DataboxStyle;
import fr.proline.studio.pattern.DataBoxAdjacencyMatrixChoice;
import fr.proline.studio.pattern.DataBoxAdjacencyMatrix;
import fr.proline.studio.pattern.DataboxManager;
import fr.proline.studio.pattern.ParameterDistance;
import fr.proline.studio.table.AbstractTableAction;
import java.awt.Component;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;

/**
 *
 * @author JM235353
 */
public class DisplayMenuAction extends AbstractTableAction {

    private JMenu m_menu = null;

    public DisplayMenuAction() {
        super("Display");
        m_menu = new JMenu("Display");
    }

    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
        // nothing to do
    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
        setEnabled(true);
    }

    @Override
    public Component getPopupPresenter() {
        return m_menu;
    }


    
    public void populate(AbstractDataBox dataBox) {

        m_menu.removeAll();

        boolean hasSubActions = false;
            
        DataboxStyle previousStyle = null;
        for (DataboxStyle style : DataboxStyle.values()) {

            TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap = DataboxManager.getDataboxManager().findCompatibleDataboxList(dataBox, dataBox.getImportantInParameterClass());
            Iterator<ParameterDistance> it = dataBoxMap.descendingKeySet().iterator();
            while (it.hasNext()) {
                AbstractDataBox destDatabox = dataBoxMap.get(it.next());
                if (destDatabox.getStyle() != style) {
                    continue;
                }
                if ((previousStyle != null) && (previousStyle != style)) {
                    m_menu.addSeparator();
                }
                previousStyle = style;
                // JPM.WART for description
                String description = (destDatabox instanceof DataBoxAdjacencyMatrixChoice) ?  DataBoxAdjacencyMatrix.DESCRIPTION : destDatabox.getDescription();
                DisplayViewInNewWindowAction displayAction = new DisplayViewInNewWindowAction(dataBox, destDatabox, description);
                JMenuItem displayOptionMenuItem = new JMenuItem(displayAction);
                m_menu.add(displayOptionMenuItem);
                hasSubActions = true;
            }

        }
        
        m_menu.setEnabled(hasSubActions);

    }
    
    
}
