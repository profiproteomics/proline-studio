package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.filter.FilterProviderInterface;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.progress.ProgressInterface;
import javax.swing.table.TableModel;
import fr.proline.studio.table.DecoratedTableModelInterface;
import fr.proline.studio.table.LazyTableModelInterface;

/**
 * Interface of Type Table Model which offers all the functionnalities of other defined interfaces
 * @author JM235353
 */
public interface GlobalTableModelInterface extends LazyTableModelInterface, DecoratedTableModelInterface, ExtendedTableModelInterface, ExtraDataForTableModelInterface, TableModel, FilterProviderInterface, ProgressInterface, BestGraphicsInterface, ExportModelInterface {
    public GlobalTableModelInterface getFrozzenModel();
}
