package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.table.ExportTableSelectionInterface;
import java.util.HashSet;
import javax.swing.JTable;

/**
 *
 * @author JM235353
 */
public class DisplayIdentificationProteinSetsAction extends AbstractTableAction {

    private final AbstractDataBox m_box;

    public DisplayIdentificationProteinSetsAction(AbstractDataBox box, JTable table) {
        super(table, "Display Identification Protein Sets");

        m_box = box;
    }

    @Override
    public void actionPerformed(JTable table, int[] selectedRows) {
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

}
