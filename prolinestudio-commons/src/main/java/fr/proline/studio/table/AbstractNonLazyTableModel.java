package fr.proline.studio.table;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterProviderInterface;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractNonLazyTableModel extends DecoratedTableModel implements DecoratedTableModelInterface, ExtendedTableModelInterface, TableModel, FilterProviderInterface, BestGraphicsInterface, ExportModelInterface {

  private String name;
  private Column[] m_columns;

  protected Column[] getColumns() {
    if (m_columns == null) {
      m_columns = Column.pack(this.getClass());
    }
    return m_columns;
  }

  @Override
  public int getColumnCount() {
    return getColumns().length;
  }

  @Override
  public String getColumnName(int column) {
    return getColumns()[column].getName();
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return getColumns()[columnIndex].getColumnClass();
  }

  @Override
  public Class getDataColumnClass(int columnIndex) {
    return getColumns()[columnIndex].getDataColumnClass();
  }

  @Override
  public String getDataColumnIdentifier(int columnIndex) {
    return getColumnName(columnIndex);
  }

  @Override
  public Object getDataValueAt(int rowIndex, int columnIndex) {
    return getValueAt(rowIndex, columnIndex);
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, Object> getExternalData() {
    return null;
  }

  @Override
  public PlotInformation getPlotInformation() {
    return null;
  }

  @Override
  public String getToolTipForHeader(int col) {
    return getColumns()[col].getTooltip();
  }


  @Override
  public TableCellRenderer getRenderer(int row, int col) {
      return getColumns()[col].getRenderer();
  }

  @Override
  public String getExportRowCell(int row, int col) {
    return ExportModelUtilities.getExportRowCell(this, row, col);
  }

  @Override
  public ArrayList<ExportFontData> getExportFonts(int row, int col) {
    return null;
  }

  @Override
  public String getExportColumnName(int col) {
    return getColumnName(col);
  }

  @Override
  public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
    for (Column c : getColumns()) {
      Filter filter = c.getFilter();
      if (filter != null) {
      filtersMap.put(c.getIndex(), filter);
      }
    }
  }

  @Override
  public PlotType getBestPlotType() {
    return null;
  }

  @Override
  public int[] getBestColIndex(PlotType plotType) {
    return null;
  }
}
