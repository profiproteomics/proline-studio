package fr.proline.mzscope.ui.peakels;

import fr.proline.mzscope.model.IPeakel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.AbstractNonLazyTableModel;
import fr.proline.studio.table.Column;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeakelsTableModel extends AbstractNonLazyTableModel implements GlobalTableModelInterface {

  private static TableCellRenderer BIG_FLOAT = new BigFloatOrDoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0 );

  public static final Column COLTYPE_FEATURE_MZCOL = new Column("m/z", "m/z", Double.class,0, Column.DOUBLE_5DIGITS_RIGHT);
  public static final Column COLTYPE_FEATURE_ET_COL = new Column("Elution", "Elution Time in minutes", Float.class, 1, Column.DOUBLE_2DIGITS_RIGHT);
  public static final Column COLTYPE_FEATURE_DURATION_COL = new Column("Duration", "Duration in minutes", Float.class, 2, Column.DOUBLE_2DIGITS_RIGHT);
  public static final Column COLTYPE_FEATURE_APEX_INT_COL = new Column("Apex Int.", "Apex intensity", Float.class, 3, BIG_FLOAT);
  public static final Column COLTYPE_FEATURE_AREA_COL = new Column("Area", "Area", Double.class,4, BIG_FLOAT);
  public static final Column COLTYPE_FEATURE_SCAN_COUNT_COL = new Column("MS Count", "MS Count", Integer.class, 5);
  public static final Column COLTYPE_FEATURE_RAWFILE = new Column("Raw file", "Raw File Name", String.class,6, Column.STRING_LEFT);
  public static final Column COLTYPE_FEATURE_MZ_STDEV = new Column("mz Stdev", "m/z standard deviation", Double.class,7, Column.DOUBLE_5DIGITS_RIGHT);

  protected List<IPeakel> m_peakels = new ArrayList<>();
  protected Map<Integer, Double> m_statistics = new HashMap<>();

  @Override
  public int[] getKeysColumn() {
    int[] keys = { COLTYPE_FEATURE_MZCOL.getIndex(), COLTYPE_FEATURE_ET_COL.getIndex(), COLTYPE_FEATURE_RAWFILE.getIndex() };
    return keys;
  }

  @Override
  public int getInfoColumn() {
    return COLTYPE_FEATURE_MZCOL.getIndex();
  }

  @Override
  public ArrayList<ExtraDataType> getExtraDataTypes() {
    return null;
  }

  @Override
  public String getTootlTipValue(int row, int col) {
    return null;
  }

  @Override
  public int getRowCount() {
    return m_peakels.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {

      if (columnIndex == COLTYPE_FEATURE_MZCOL.getIndex()) {
        return m_peakels.get(rowIndex).getMz();
      } else if (columnIndex == COLTYPE_FEATURE_ET_COL.getIndex()) {
        return m_peakels.get(rowIndex).getElutionTime() / 60.0;
      } else if (columnIndex == COLTYPE_FEATURE_DURATION_COL.getIndex()) {
        return m_peakels.get(rowIndex).getDuration() / 60.0;
      } else if (columnIndex == COLTYPE_FEATURE_APEX_INT_COL.getIndex()) {
        return m_peakels.get(rowIndex).getApexIntensity();
      } else if (columnIndex == COLTYPE_FEATURE_AREA_COL.getIndex()) {
        return m_peakels.get(rowIndex).getArea() / 60.0;
      } else if (columnIndex == COLTYPE_FEATURE_SCAN_COUNT_COL.getIndex()) {
        return m_peakels.get(rowIndex).getScanCount();
      } else if (columnIndex == COLTYPE_FEATURE_RAWFILE.getIndex()) {
        return m_peakels.get(rowIndex).getRawFile().getName();
      } else if (columnIndex == COLTYPE_FEATURE_MZ_STDEV.getIndex()) {
        return m_statistics.get(rowIndex);
      }

      return null; // should not happen
  }

  private Double getMzStats(int row, IPeakel iPeakel) {
    if (!m_statistics.containsKey(row)) {
      if (iPeakel.getPeakel() != null) {
        double[] mzValues = iPeakel.getPeakel().mzValues();
        double standardDeviation = new DescriptiveStatistics(mzValues).getStandardDeviation();
        m_statistics.put(row, 1e6 * standardDeviation / iPeakel.getMz());
      } else {
        return 0.0;
      }
    }
    return m_statistics.get(row);
  }

  @Override
  public Object getValue(Class c) {
    return getSingleValue(c);
  }

  @Override
  public Object getRowValue(Class c, int row) {
    if (IPeakel.class.isAssignableFrom(c)) {
      return m_peakels.get(row);
    }
    return null;
  }

  @Override
  public Object getColValue(Class c, int col) {
    return null;
  }

  public void setPeakels(List<IPeakel> peakels) {
    m_statistics.clear();
    m_peakels.clear();
    m_peakels.addAll(peakels);
    int row = 0;
    for(IPeakel p : m_peakels) {
      getMzStats(row++, p);
    }
    fireTableDataChanged();
  }

  public void addPeakels(List<IPeakel> features) {
    int row = m_peakels.size()-1;
    m_peakels.addAll(features);
    for(IPeakel p : features) {
      getMzStats(row++, p);
    }
    fireTableDataChanged();
  }


  @Override
  public GlobalTableModelInterface getFrozzenModel() {
    return this;
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public int getLoadingPercentage() {
    return 100;
  }

  @Override
  public Long getTaskId() {
    return -1L;
  }

  @Override
  public LazyData getLazyData(int row, int col) {
    return null;
  }

  @Override
  public void givePriorityTo(Long taskId, int row, int col) {

  }

  @Override
  public void sortingChanged(int col) {

  }

  @Override
  public int getSubTaskId(int col) {
    return 0;
  }
}
