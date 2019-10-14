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

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.table.AbstractTableAction;
import fr.proline.studio.table.ExportTableSelectionInterface;
import java.util.HashSet;
import javax.swing.JTable;

/**
 *
 * @author JM235353
 */
public class DisplayIdentificationProteinSetsAction extends AbstractTableAction {

    private AbstractDataBox m_box;

    public DisplayIdentificationProteinSetsAction() {
        super("Display Identification Protein Sets");

    }

    public void setBox(AbstractDataBox box) {
        m_box = box;
    }

    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
        
        if (m_box == null) {
            return; // should not happen
        }
        
        ResultSummary rsm = (ResultSummary) m_box.getData(false, ResultSummary.class);

        // prepare window box
        WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox(m_box.getDataName(), false);
        HashSet selectedData = ((ExportTableSelectionInterface) table).exportSelection(selectedRows);
        wbox.selectDataWhenLoaded(selectedData);
        wbox.setEntryData(m_box.getProjectId(), rsm);

        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();
    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
        setEnabled(true); // always enabled
    }

}
