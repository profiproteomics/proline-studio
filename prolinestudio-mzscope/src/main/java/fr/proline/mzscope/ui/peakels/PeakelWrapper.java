package fr.proline.mzscope.ui.peakels;

import fr.profi.mzdb.model.Peakel;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.DataFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class PeakelWrapper implements ExtendedTableModelInterface {

  Peakel m_peakel;
  int m_isotopeIndex;
  PlotInformation m_plotInformation = null;

  public PeakelWrapper(Peakel p, int i) {
    m_peakel = p;
    m_isotopeIndex = i;
  }

  @Override
  public int getRowCount() {
    return m_peakel.getElutionTimes().length;
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getDataColumnIdentifier(int columnIndex) {
    return (columnIndex == 0) ? "rt" : "intensity";
  }

  @Override
  public Class getDataColumnClass(int columnIndex) {
    return Float.class;
  }

  @Override
  public Object getDataValueAt(int rowIndex, int columnIndex) {
    return (columnIndex == 0) ? m_peakel.getElutionTimes()[rowIndex] / 60.0f : m_peakel.getIntensityValues()[rowIndex];
  }

  @Override
  public int[] getKeysColumn() {
    return new int[]{0};
  }

  @Override
  public int getInfoColumn() {
    return 0;
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getName() {
    return "Elution peak";
  }

  @Override
  public Map<String, Object> getExternalData() {
    return null;
  }

  @Override
  public PlotInformation getPlotInformation() {
    if (m_plotInformation == null) {
      m_plotInformation = new PlotInformation();
      m_plotInformation.setPlotColor(CyclicColorPalette.getColor(1));
      m_plotInformation.setPlotTitle("Elution peak\n");
      m_plotInformation.setDrawPoints(false);
      m_plotInformation.setDrawGap(true);
      HashMap<String, String> plotInfo = new HashMap<>();
      plotInfo.put("m/z", Double.toString(m_peakel.getApexMz()));
      plotInfo.put("Apex Int.", DataFormat.formatWithGroupingSep(m_peakel.getApexIntensity(), 0));
      plotInfo.put("Isotope index", Integer.toString(m_isotopeIndex));
      m_plotInformation.setPlotInfo(plotInfo);
    }
    return m_plotInformation;
  }

  @Override
  public long row2UniqueId(int rowIndex) {
    return rowIndex;
  }

  @Override
  public int uniqueId2Row(long id) {
    return (int) id;
  }

  @Override
  public ArrayList<ExtraDataType> getExtraDataTypes() {
    return null;
  }

  @Override
  public Object getValue(Class c) {
    return null;
  }

  @Override
  public Object getRowValue(Class c, int row) {
    return null;
  }

  @Override
  public Object getColValue(Class c, int col) {
    return null;
  }

  @Override
  public void addSingleValue(Object v) {

  }

  @Override
  public Object getSingleValue(Class c) {
    return null;
  }

  @Override
  public PlotDataSpec getDataSpecAt(int i) {
    return null;
  }


}
