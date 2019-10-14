/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.swing.JTable;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.graphics.PlotInformation;

import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData.FragmentMatch;
import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData.TheoreticalFragmentSeries;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.GlobalValues;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import javax.swing.table.TableColumn;
import org.apache.poi.hssf.util.HSSFColor;
import org.jdesktop.swingx.table.TableColumnExt;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

public class RsetPeptideFragmentationTable extends DecoratedTable {

    /**
     * Created by AW
     */
    private FragTableCustomRenderer m_fragTableRenderer;

    private boolean m_fragmentsIntensityVisible = false;

    public RsetPeptideFragmentationTable() {

        RsetPeptideFragmentationTable.FragmentationTableModel fragmentationTableModel = new RsetPeptideFragmentationTable.FragmentationTableModel();

        m_fragTableRenderer = new RsetPeptideFragmentationTable.FragTableCustomRenderer();
        m_fragTableRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
        setModel(fragmentationTableModel);
        setDefaultRenderer(Double.class, m_fragTableRenderer);
        setSortable(false);

        updateFragmentsIntensityVisibility(false);
    }

    public void setData(DPeptideMatch pepMatch, PeptideFragmentationData petpideFragmentationData) {

        setAllColumnsVisibles(); //JPM.HACK : bug between columns visibility and update of the model of a JXTable

        FragmentationTableModel fragmentationTableModel = ((FragmentationTableModel) getModel());

        if (petpideFragmentationData == null) {
            fragmentationTableModel.reinitData();
            m_fragTableRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());

        } else {
            fragmentationTableModel.setData(petpideFragmentationData, pepMatch.getPeptide().getSequence());
            m_fragTableRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
            updateFragmentsIntensityVisibility();
        }

    }

    public final void updateFragmentsIntensityVisibility() {
        updateFragmentsIntensityVisibility(m_fragmentsIntensityVisible);
    }

    public final void updateFragmentsIntensityVisibility(boolean visible) {

        m_fragmentsIntensityVisible = visible;
        final String intensityStringIdentifier = "(I)";

        // get all columns including hidden ones
        List<TableColumn> columns = getColumns(true);

        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) { // use all columns but check for an identifier in the title

                TableColumnExt tce = (TableColumnExt) columns.get(i);
                if (tce.getTitle().indexOf(intensityStringIdentifier) != -1) {
                    tce.setVisible(visible);
                }

            }
        }

    }

    public void setAllColumnsVisibles() {
        List<TableColumn> columns = getColumns(true);
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) { // use all columns but check for an identifier in the title

                TableColumnExt tce = (TableColumnExt) columns.get(i);

                tce.setVisible(true);

            }
        }
    }

    @Override
    public TablePopupMenu initPopupMenu() {
        return null;
    }

    // set as abstract
    @Override
    public void prepostPopupMenu() {
        // nothing to do
    }

    public static class FragmentationTableModel extends DecoratedTableModel implements ExtendedTableModelInterface, ExportModelInterface {

        private List<TheoreticalFragmentSeries> m_fragSer;
        private String m_peptideSequence;
        private int m_sizeMaxSeries;
        private String[][] m_matrix;
        private double[][] m_matrixIntensity;
        private String[] m_columnNames;

        public FragmentationTableModel() { // constructor
            initData();
        }

        public void reinitData() {
            initData();
            fireTableStructureChanged();

        }

        private void initData() {
            m_fragSer = null;
            m_peptideSequence = null;
            m_sizeMaxSeries = 0;
            m_matrix = null;
            m_matrixIntensity = null;
            m_columnNames = null;
        }

        public void setData(PeptideFragmentationData peptideFragmentationData, String peptideSequence) {

            initData();

            m_fragSer = peptideFragmentationData.getTheoreticalFragmentSeries();
            FragmentMatch[] fragmentMatches = peptideFragmentationData.getFragmentMatches();
            m_peptideSequence = peptideSequence;

            int sizeMaxSeries = 0;
            for (int i = 0; i < m_fragSer.size(); i++) {
                if (m_fragSer.get(i).masses.length > sizeMaxSeries) {
                    sizeMaxSeries = m_fragSer.get(i).masses.length;
                }

            }

            m_sizeMaxSeries = sizeMaxSeries;

            // get series names
            String xyzSerieName = peptideFragmentationData.getXYZReferenceSeriesName();
            String abcSerieName = peptideFragmentationData.getABCReferenceSeriesName();

            m_columnNames = new String[m_fragSer.size() + 3 + m_fragSer.size()];
            int i = 0;
            m_columnNames[i++] = "amino acid";
            m_columnNames[i++] = abcSerieName + " ion";

            for (TheoreticalFragmentSeries currentFrag : m_fragSer) {
                m_columnNames[i++] = currentFrag.frag_series + " (M)";
            }

            m_columnNames[i] = xyzSerieName + " ion";
            i++;

            for (TheoreticalFragmentSeries currentFragSer : m_fragSer) {
                m_columnNames[i] = currentFragSer.frag_series + " (I)";
                i++;
            }

            m_matrix = new String[sizeMaxSeries][m_columnNames.length];
            m_matrixIntensity = new double[sizeMaxSeries][m_columnNames.length];

            for (int j = 0; j < m_fragSer.size(); j++) { // loop through theoFragment series here
                for (int k = 0; k < m_fragSer.get(j).masses.length; k++) { // loop through masses for each fragment series
                    for (i = 0; i < fragmentMatches.length; i++) { // find matching fragMatches with theoFragSeries
                        if ((fragmentMatches[i].getCharge() == m_fragSer.get(j).getCharge())
                                && fragmentMatches[i].getSeriesName().equals(m_fragSer.get(j).frag_series)
                                && Math.abs(fragmentMatches[i].calculated_moz - m_fragSer.get(j).masses[k]) < 0.01) {

                            if ((m_fragSer.get(j).frag_series.toUpperCase().contains("A")
                                    || m_fragSer.get(j).frag_series.toUpperCase().contains("B")
                                    || m_fragSer.get(j).frag_series.toUpperCase().contains("C")) && (fragmentMatches[i].getPosition() == k + 1)) {
                                m_matrix[k][j + 2] = "ABC";
                                m_matrixIntensity[k][j + 2] = fragmentMatches[i].intensity; // assign matching peak intensity
                                m_matrix[k][j + 2] += "intensity";
                            } else if ((m_fragSer.get(j).frag_series.toUpperCase().contains("X")
                                    || m_fragSer.get(j).frag_series.toUpperCase().contains("Y")
                                    || m_fragSer.get(j).frag_series.toUpperCase().contains("Z")) && ((sizeMaxSeries - fragmentMatches[i].getPosition()) == k)) {
                                m_matrix[k][j + 2] = "XYZ";
                                m_matrixIntensity[k][j + 2] = fragmentMatches[i].intensity; // assign matching peak intensity
                                m_matrix[k][j + 2] += "intensity";
                            } else {
                                // immonium or anything else than abc,v,w,xyz
                            }
                        }

                    }
                }
            }

            fireTableStructureChanged();
        }

        public String[][] getMatrix() {
            return m_matrix;
        }

        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }

        @Override
        public String getToolTipForHeader(int col) {
            return getColumnName(col);
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public int getRowCount() {
            return m_sizeMaxSeries;
        }

        @Override
        public int getColumnCount() {
            if (m_columnNames == null) {
                return 0;
            }
            return m_columnNames.length;
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            if (columnIndex == 1) {
                return Integer.class;
            }
            if (columnIndex == m_fragSer.size() + 2) {
                return Integer.class;
            }

            return Double.class;

        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {

                if (rowIndex < m_peptideSequence.length()) {
                    return m_peptideSequence.charAt(rowIndex);
                } else {
                    return "?"; // problem: should be of the right size...need
                    // debugging!
                }
            }

            if (columnIndex == 1) {
                return rowIndex + 1;
            }

            if (columnIndex == m_fragSer.size() + 3 /*
                     * m_columnNames.length
                     */ - 1) {
                return m_sizeMaxSeries - rowIndex;
            }

            if (columnIndex < m_fragSer.size() + 3) { // return mass value
                TheoreticalFragmentSeries currentFragSer = m_fragSer.get(columnIndex - 2);

                if (currentFragSer.masses[rowIndex] != 0) {
                    return (double) Math.round(currentFragSer.masses[rowIndex] * 10000) / 10000;
                } else {
                    return null;
                }
            } else if (columnIndex > m_fragSer.size() + 2 && columnIndex < m_columnNames.length)// return intensity value
            {

                if (m_matrixIntensity[rowIndex][columnIndex - m_fragSer.size() - 1] > 0) {
                    return new BigDecimal(m_matrixIntensity[rowIndex][columnIndex - m_fragSer.size() - 1], new MathContext(3));
                } else {
                    return null;
                }
            } else {
                return null;
            }

        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return null;
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ExportModelUtilities.getExportRowCell(this, row, col);
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {

            String exportString = getExportRowCell(row, col);
            
            if (m_matrix[row][col] != null) {

                if (m_matrix[row][col].contains("ABC")) {
                     ArrayList<ExportFontData> ExportFontDatas = new ArrayList<>();
                    ExportFontData newSubStringFont = new ExportFontData(0, exportString.length(), HSSFColor.LIGHT_BLUE.index, Font.BOLD);
                    ExportFontDatas.add(newSubStringFont);   
                    return ExportFontDatas;

                } else if (m_matrix[row][col].contains("XYZ")) {
                    ArrayList<ExportFontData> ExportFontDatas = new ArrayList<>();

                    ExportFontData newSubStringFont = new ExportFontData(0, exportString.length(), HSSFColor.RED.index, Font.BOLD);
                    ExportFontDatas.add(newSubStringFont);    
                    return ExportFontDatas;
                    
                }
            }
            return null;
        }

        @Override
        public String getExportColumnName(int col) {
            return getColumnName(col);
        }

        @Override
        public String getDataColumnIdentifier(int columnIndex) {
            return getColumnName(columnIndex);
        }

        @Override
        public Class getDataColumnClass(int columnIndex) {
            return getColumnClass(columnIndex);
        }

        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            return getValueAt(rowIndex, columnIndex);
        }

        @Override
        public int[] getKeysColumn() {
            return null;
        }

        @Override
        public int getInfoColumn() {
            return -1;
        }

        @Override
        public void setName(String name) {
            
        }

        @Override
        public String getName() {
            return null;
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

    }

    public static class FragTableCustomRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        private String[][] m_selectMatrix = new String[100][100];

        
        private StringBuilder m_stringBuilder;

        void setSelectMatrix(String[][] matx) {
            m_selectMatrix = matx;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (m_stringBuilder==null) {
                m_stringBuilder = new StringBuilder();
            }else{
                m_stringBuilder.setLength(0);
            }

            String textToExport;
            if (value != null) {
                textToExport = value.toString();
            } else {
                textToExport = "";
            }

            if (m_selectMatrix[row][column] != null) {
                
                m_stringBuilder.append("<HTML>");

                if (m_selectMatrix[row][column].contains("ABC")) {                        
                    m_stringBuilder.append("<span style='color:").append((isSelected) ? GlobalValues.HTML_COLOR_EXTRA_LIGHT_BLUE : GlobalValues.HTML_COLOR_LIGHT_BLUE).append("'>").append("<b>").append(textToExport).append("</b>").append("</span>");

                } else if (m_selectMatrix[row][column].contains("XYZ")) {               
                    m_stringBuilder.append("<span style='color:").append((isSelected) ? GlobalValues.HTML_COLOR_EXTRA_LIGHT_RED : GlobalValues.HTML_COLOR_LIGHT_RED).append("'>").append("<b>").append(textToExport).append("</b>").append("</span>");                
                }
                
                m_stringBuilder.append("</HTML>");

            } else {
                m_stringBuilder.append(textToExport);
            }

            Component component = super.getTableCellRendererComponent(table, m_stringBuilder.toString(), isSelected, hasFocus, row, column);
            
            return component;

        }

    }
}
