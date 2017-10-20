package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.graphics.CrossSelectionInterface;
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
