package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.swing.JTable;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportSubStringFont;
import fr.proline.studio.export.ExportTextInterface;
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

    public static class FragmentationTableModel extends DecoratedTableModel implements CompareDataInterface, ExportModelInterface {

        private TheoreticalFragmentSeries[] m_fragSer;
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

        public void setData(PeptideFragmentationData petpideFragmentationData, String peptideSequence) {

            initData();

            m_fragSer = petpideFragmentationData.getFragmentSeries();
            FragmentMatch[] fragMa = petpideFragmentationData.getFragmentMatch();
            m_peptideSequence = peptideSequence;

            int sizeMaxSeries = 0;
            for (int i = 0; i < m_fragSer.length; i++) {
                if (m_fragSer[i].masses.length > sizeMaxSeries) {
                    sizeMaxSeries = m_fragSer[i].masses.length;
                }

            }

            m_sizeMaxSeries = sizeMaxSeries;

            // get series names
            String xyzSerieName = "";
            String abcSerieName = "";
            for (int i = 0; i < m_fragSer.length; i++) {
                switch (m_fragSer[i].frag_series.charAt(0)) {
                    case 'a': // either a,b or c do:
                    case 'b':
                    case 'c':
                        if (m_fragSer[i].frag_series.length() > 1) {
                            // then it is either a ++ or a b-H2O and so on...
                        } else // it's a 'a/b/c' ion
                        {
                            if (!abcSerieName.equals("b")) {// only if b not already defined, else we keep b
                                abcSerieName = "" + m_fragSer[i].frag_series.charAt(0);
                            }
                        }
                        break;
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':
                    case 'z':
                        if (m_fragSer[i].frag_series.length() > 1) {
                            // then it is either a ++ or a b-H2O and so on...
                        } else // it's a 'v/w/x/y/z' ion
                        {
                            if (!xyzSerieName.equals("y")) {// only if b not already defined, else we keep b
                                xyzSerieName = "" + m_fragSer[i].frag_series.charAt(0);

                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            m_columnNames = new String[m_fragSer.length + 3 + m_fragSer.length];
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

            for (int j = 0; j < m_fragSer.length; j++) { // loop through theoFragment series here
                for (int k = 0; k < m_fragSer[j].masses.length; k++) { // loop through masses for each fragment series
                    for (i = 0; i < fragMa.length; i++) { // find matching fragMatches with theoFragSeries
                        if ((fragMa[i].getCharge() == m_fragSer[j].getCharge()) && fragMa[i].getSeriesName().equals(m_fragSer[j].frag_series)) {

                            if ((m_fragSer[j].frag_series.toUpperCase().contains("A")
                                    || m_fragSer[j].frag_series.toUpperCase().contains("B")
                                    || m_fragSer[j].frag_series.toUpperCase().contains("C")) && (fragMa[i].getPosition() == k + 1)) {
                                m_matrix[k][j + 2] = "ABC";
                                m_matrixIntensity[k][j + 2] = fragMa[i].intensity; // assign matching peak intensity
                                m_matrix[k][j + 2] += "intensity";
                            } else if ((m_fragSer[j].frag_series.toUpperCase().contains("X")
                                    || m_fragSer[j].frag_series.toUpperCase().contains("Y")
                                    || m_fragSer[j].frag_series.toUpperCase().contains("Z")) && ((sizeMaxSeries - fragMa[i].getPosition()) == k)) {
                                m_matrix[k][j + 2] = "XYZ";
                                m_matrixIntensity[k][j + 2] = fragMa[i].intensity; // assign matching peak intensity
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
            if (columnIndex == m_fragSer.length + 2) {
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

            if (columnIndex == m_fragSer.length + 3 /*
                     * m_columnNames.length
                     */ - 1) {
                return m_sizeMaxSeries - rowIndex;
            }

            if (columnIndex < m_fragSer.length + 3) { // return mass value
                TheoreticalFragmentSeries currentFragSer = m_fragSer[columnIndex - 2];

                if (currentFragSer.masses[rowIndex] != 0) {
                    return (double) Math.round(currentFragSer.masses[rowIndex] * 10000) / 10000;
                } else {
                    return null;
                }
            } else if (columnIndex > m_fragSer.length + 2 && columnIndex < m_columnNames.length)// return intensity value
            {

                if (m_matrixIntensity[rowIndex][columnIndex - m_fragSer.length - 1] > 0) {
                    return new BigDecimal(m_matrixIntensity[rowIndex][columnIndex - m_fragSer.length - 1], new MathContext(3));
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
        public ArrayList<ExportSubStringFont> getSubStringFonts(int row, int col) {

            String exportString = getExportRowCell(row, col);
            
            if (m_matrix[row][col] != null) {

                if (m_matrix[row][col].contains("ABC")) {
                     ArrayList<ExportSubStringFont> exportSubStringFonts = new ArrayList<>();
                    ExportSubStringFont newSubStringFont = new ExportSubStringFont(0, exportString.length(), HSSFColor.LIGHT_BLUE.index, Font.BOLD);
                    exportSubStringFonts.add(newSubStringFont);   
                    return exportSubStringFonts;

                } else if (m_matrix[row][col].contains("XYZ")) {
                    ArrayList<ExportSubStringFont> exportSubStringFonts = new ArrayList<>();

                    ExportSubStringFont newSubStringFont = new ExportSubStringFont(0, exportString.length(), HSSFColor.RED.index, Font.BOLD);
                    exportSubStringFonts.add(newSubStringFont);    
                    return exportSubStringFonts;
                    
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

    public static class FragTableCustomRenderer extends DefaultTableCellRenderer /*implements ExportTextInterface*/ {

        private static final long serialVersionUID = 1L;

        private String[][] m_selectMatrix = new String[100][100];

        private String m_basicTextForExport = "";

        private ArrayList<ExportSubStringFont> m_ExportSubStringFonts;
        
        private StringBuilder stringBuilder;

        void setSelectMatrix(String[][] matx) {
            m_selectMatrix = matx;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if(stringBuilder==null){
                stringBuilder = new StringBuilder();
            }else{
                stringBuilder.setLength(0);
            }
            
            m_ExportSubStringFonts = new ArrayList<ExportSubStringFont>();

            if (value != null) {
                m_basicTextForExport = value.toString();
            } else {
                m_basicTextForExport = "";
            }

            if (m_selectMatrix[row][column] != null) {
                
                stringBuilder.append("<HTML>");

                if (m_selectMatrix[row][column].contains("ABC")) {
                    ExportSubStringFont newSubStringFont = new ExportSubStringFont(0, m_basicTextForExport.length(), HSSFColor.LIGHT_BLUE.index, Font.BOLD);
                    m_ExportSubStringFonts.add(newSubStringFont);                           
                    stringBuilder.append("<span style='color:").append((isSelected) ? GlobalValues.HTML_COLOR_EXTRA_LIGHT_BLUE : GlobalValues.HTML_COLOR_LIGHT_BLUE).append("'>").append("<b>").append(m_basicTextForExport).append("</b>").append("</span>");

                } else if (m_selectMatrix[row][column].contains("XYZ")) {
                    ExportSubStringFont newSubStringFont = new ExportSubStringFont(0, m_basicTextForExport.length(), HSSFColor.RED.index, Font.BOLD);
                    m_ExportSubStringFonts.add(newSubStringFont);                   
                    stringBuilder.append("<span style='color:").append((isSelected) ? GlobalValues.HTML_COLOR_EXTRA_LIGHT_RED : GlobalValues.HTML_COLOR_LIGHT_RED).append("'>").append("<b>").append(m_basicTextForExport).append("</b>").append("</span>");                
                }
                
                stringBuilder.append("</HTML>");

            } else {
                stringBuilder.append(m_basicTextForExport);
            }

            Component component = super.getTableCellRendererComponent(table, stringBuilder.toString(), isSelected, hasFocus, row, column);
            
            return component;

        }

        /*@Override
        public String getExportText() {
            return m_basicTextForExport;
        }

        @Override
        public ArrayList<ExportSubStringFont> getSubStringFonts() {
            return m_ExportSubStringFonts;
        }*/
    }
}
