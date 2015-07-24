package fr.proline.studio.table;

import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public interface DecoratedTableModelInterface {
    public String getToolTipForHeader(int col);
    public String getTootlTipValue(int row, int col);
    public TableCellRenderer getRenderer(int col);
}
