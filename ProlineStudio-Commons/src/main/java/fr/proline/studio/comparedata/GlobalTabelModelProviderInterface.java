package fr.proline.studio.comparedata;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.table.GlobalTableModelInterface;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author JM235353
 */
public interface GlobalTabelModelProviderInterface {
    public GlobalTableModelInterface getGlobalTableModelInterface();
    public JXTable getGlobalAssociatedTable();
    public CrossSelectionInterface getCrossSelectionInterface();
}
