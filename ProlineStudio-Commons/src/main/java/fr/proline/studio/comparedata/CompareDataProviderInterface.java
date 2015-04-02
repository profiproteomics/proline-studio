package fr.proline.studio.comparedata;

import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.table.GlobalTableModelInterface;

/**
 *
 * @author JM235353
 */
public interface CompareDataProviderInterface {
    public GlobalTableModelInterface getCompareDataInterface();
    public CrossSelectionInterface getCrossSelectionInterface();
}
