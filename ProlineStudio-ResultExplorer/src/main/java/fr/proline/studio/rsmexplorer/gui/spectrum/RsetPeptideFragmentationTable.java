package fr.proline.studio.rsmexplorer.gui.spectrum;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.swing.JTable;

import org.slf4j.LoggerFactory;


import fr.proline.core.orm.msi.dto.DPeptideMatch;

import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData.FragmentMatch_AW;
import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData.TheoreticalFragmentSeries_AW;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.DecoratedTableModel;
import java.util.List;

import javax.swing.table.TableColumn;
import org.jdesktop.swingx.table.TableColumnExt;


public class RsetPeptideFragmentationTable extends DecoratedTable {

    /**
     * Created by AW
     */

    private FragTableCustomRenderer m_matrixRenderer;

    private boolean m_fragmentsIntensityVisible = false;
    
    public RsetPeptideFragmentationTable() {
        
        RsetPeptideFragmentationTable.FragmentationTableModel fragmentationTableModel = new RsetPeptideFragmentationTable.FragmentationTableModel();

        m_matrixRenderer = new RsetPeptideFragmentationTable.FragTableCustomRenderer();
        m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
        setModel(fragmentationTableModel);
        setDefaultRenderer(Double.class, m_matrixRenderer);
        setSortable(false);
        
        updateFragmentsIntensityVisibility(false);
    }




    public void setData(DPeptideMatch pepMatch, PeptideFragmentationData petpideFragmentationData) {

        setAllColumnsVisibles(); //JPM.HACK : bug between columns visibility and update of the model of a JXTable
        
        FragmentationTableModel fragmentationTableModel = ((FragmentationTableModel) getModel());
        
        if (petpideFragmentationData == null) {
            fragmentationTableModel.reinitData();
            m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());

        } else {
            fragmentationTableModel.setData(petpideFragmentationData, pepMatch.getPeptide().getSequence());
            m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
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


    public static class FragmentationTableModel extends DecoratedTableModel {

        private  TheoreticalFragmentSeries_AW[] m_fragSer;
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
            FragmentMatch_AW[] fragMa = petpideFragmentationData.getFragmentMatch();
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
                        } else { // it's a 'a/b/c' ion
                        	if(!abcSerieName.equals("b")) {// only if b not already defined, else we keep b
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
                        } else { // it's a 'v/w/x/y/z' ion
                        	if(!xyzSerieName.equals("y")) {// only if b not already defined, else we keep b
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

            for (TheoreticalFragmentSeries_AW currentFrag : m_fragSer) {
                m_columnNames[i++] = currentFrag.frag_series + " (M)";
            }

            m_columnNames[i] = xyzSerieName + " ion";
            i++;

            for (TheoreticalFragmentSeries_AW currentFragSer : m_fragSer) {
                m_columnNames[i] = currentFragSer.frag_series + " (I)";
                i++;
            }

            m_matrix = new String[sizeMaxSeries][m_columnNames.length];
            m_matrixIntensity = new double[sizeMaxSeries][m_columnNames.length];

            double roundTol = 0.0001; // could be put to zero but in case some rounding happens at other's code.
            int nbFound = 0;

            for (int j = 0; j < m_fragSer.length; j++) { // loop through
                // theoFragment series
                // here
                for (int k = 0; k < m_fragSer[j].masses.length; k++) { // loop
                    // through
                    // masses
                    // for
                    // each
                    // fragment
                    // series
                    for (i = 0; i < fragMa.length; i++) { // find matching
                        // fragMatches with
                        // theoFragSeries
                    	fragMa[i].computeChargeFromLabel();
						m_fragSer[j].computeChargeFromLabel();
						if(    fragMa[i].charge == m_fragSer[j].charge
								&& fragMa[i].countSeq('*') == m_fragSer[j].countSeq('*')
								&& fragMa[i].countSeq('0') == m_fragSer[j].countSeq('0'))
	                    {
                			if ((fragMa[i].calculated_moz - roundTol <= (m_fragSer[j].masses[k]))
	                                && (fragMa[i].calculated_moz + roundTol >= m_fragSer[j].masses[k])) {
	                            nbFound++;
	                            if (m_fragSer[j].frag_series.toUpperCase().contains(
	                                    "A")
	                                    || m_fragSer[j].frag_series.toUpperCase().contains("B")
	                                    || m_fragSer[j].frag_series.toUpperCase().contains("C")) {
	                                m_matrix[k][j + 2] = "ABC";
	
	                            } else if (m_fragSer[j].frag_series.toUpperCase().contains("X")
	                                    || m_fragSer[j].frag_series.toUpperCase().contains("Y")
	                                    || m_fragSer[j].frag_series.toUpperCase().contains("Z")) {
	                                m_matrix[k][j + 2] = "XYZ";
	                            } else {
	                                // immonium or anything else than abc,v,w,xyz
	                            }
	                            m_matrixIntensity[k][j + 2] = fragMa[i].intensity; // assign matching peak intensity
	                            m_matrix[k][j + 2] += "intensity";
	
		                    } else {
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
                TheoreticalFragmentSeries_AW currentFragSer = m_fragSer[columnIndex - 2];

                if (currentFragSer.masses[rowIndex] != 0) {
                    return (double) Math.round(currentFragSer.masses[rowIndex] * 10000) / 10000;
                } else {
                    return null;
                }
            } else if (columnIndex > m_fragSer.length + 2 && columnIndex < m_columnNames.length)// return intensity value
            {
                
                if (m_matrixIntensity[rowIndex][columnIndex - m_fragSer.length - 1] > 0) {
                   return  new BigDecimal(m_matrixIntensity[rowIndex][columnIndex- m_fragSer.length - 1], new MathContext(3));
                } else {
                    return null;
                }
            } else {
                return null;
            }

        }

    }

    public static class FragTableCustomRenderer extends org.jdesktop.swingx.renderer.DefaultTableRenderer {
        
        private static final long serialVersionUID = 1L;
        private String[][] m_selectMatrix = new String[100][100];
        private Font m_fontPlain = null;
        private Font m_fontBold = null;
        private final static Color LIGHT_BLUE_COLOR = new Color(51, 153, 255);
        private final static Color LIGHT_RED_COLOR = new Color(255, 85, 85);
        private final static Color EXTRA_LIGHT_BLUE_COLOR = new Color(175, 255,255);
        private final static Color EXTRA_LIGHT_RED_COLOR = new Color(255, 230,230);

        void setSelectMatrix(String[][] matx) {
            m_selectMatrix = matx;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // prepare needed fonts
            if (m_fontPlain == null) {
                m_fontPlain = component.getFont().deriveFont(Font.PLAIN);
                m_fontBold = m_fontPlain.deriveFont(Font.BOLD);
            }

            // select font
            if (m_selectMatrix[row][column] != null) {
                component.setFont(m_fontBold);
            } else {
                component.setFont(m_fontPlain);
            }

            // select color
            Color foregroundColor;

            if (m_selectMatrix[row][column] != null) {

                if (m_selectMatrix[row][column].contains("ABC")) { // highlight
                    // the cell
                    // if true
                    // in
                    // selectMatrix
                    foregroundColor = (isSelected) ? EXTRA_LIGHT_BLUE_COLOR : LIGHT_BLUE_COLOR;
                } else if (m_selectMatrix[row][column].contains("XYZ")) {
                    foregroundColor = (isSelected) ? EXTRA_LIGHT_RED_COLOR
                            : LIGHT_RED_COLOR;
                } else {
                    foregroundColor = (isSelected) ? Color.white : Color.black;
                }
            } else {
                // standard color:
                foregroundColor = (isSelected) ? Color.white : Color.black;
            }

            component.setForeground(foregroundColor);

            return component;

        }
    }
}
