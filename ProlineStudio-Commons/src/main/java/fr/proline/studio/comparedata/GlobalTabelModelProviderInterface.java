package fr.proline.studio.comparedata;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.table.GlobalTableModelInterface;
import org.jdesktop.swingx.JXTable;

/**
 * Class must implements this interface
 * if it can provide a GlobalTableModelInterface, potentially its associated table,
 * and a crossSelectionInterface.
 * @author JM235353
 */
public interface GlobalTabelModelProviderInterface {
    public GlobalTableModelInterface getGlobalTableModelInterface();
    public JXTable getGlobalAssociatedTable();
    public CrossSelectionInterface getCrossSelectionInterface();
}
