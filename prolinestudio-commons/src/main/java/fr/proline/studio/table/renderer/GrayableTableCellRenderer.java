package fr.proline.studio.table.renderer;

import javax.swing.table.TableCellRenderer;

/**
 * Interface to be extended when the renderer offers the possibility to be grayed
 * @author JM235353
 */
public interface GrayableTableCellRenderer extends TableCellRenderer {

    public void setGrayed(boolean v);
}
