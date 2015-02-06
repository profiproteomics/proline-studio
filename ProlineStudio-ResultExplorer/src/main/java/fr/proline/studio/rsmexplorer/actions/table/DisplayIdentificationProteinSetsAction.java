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
        WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox(m_box.getFullName(), false);
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
