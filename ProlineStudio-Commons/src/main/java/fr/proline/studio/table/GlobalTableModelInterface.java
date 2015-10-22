package fr.proline.studio.table;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.ExtraDataInterface;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.filter.FilterProviderInterface;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.progress.ProgressInterface;
import javax.swing.table.TableModel;

/**
 *
 * @author JM235353
 */
public interface GlobalTableModelInterface extends LazyTableModelInterface, DecoratedTableModelInterface, CompareDataInterface, ExtraDataInterface, TableModel, FilterProviderInterface, ProgressInterface, BestGraphicsInterface, ExportModelInterface {
    public GlobalTableModelInterface getFrozzenModel();
}
