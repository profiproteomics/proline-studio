package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.HideFragmentsTableIntensityButton;
import fr.proline.studio.utils.LazyTableModel;

import javax.swing.*;

public class RsetPeptideFragmentationTable extends JPanel {

    /**
     * Created by AW
     */
    private static final long serialVersionUID = 1L;
    
    private JScrollPane jScrollFragPane = new JScrollPane();
    private DPeptideMatch m_peptideMatch;
    private HideFragmentsTableIntensityButton m_hideFragIntensityButton = null;
    private DecoratedTable m_jTable1;
    private FragTableCustomRenderer m_matrixRenderer;

    public RsetPeptideFragmentationTable() {
        initComponent(new FragmentMatch_AW[0], new TheoreticalFragmentSeries_AW[0]);
    }

    private void initComponent(FragmentMatch_AW[] fragMa, TheoreticalFragmentSeries_AW[] fragSer) {
        setLayout(new BorderLayout());
        m_jTable1 = new DecoratedTable();
        JPanel internalPanel = new JPanel();

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton m_exportButton = new ExportButton(null, "Fragmentation Table", m_jTable1);
        toolbar.add(m_exportButton);

        m_hideFragIntensityButton = new HideFragmentsTableIntensityButton(m_jTable1, false);
        toolbar.add(m_hideFragIntensityButton);


        FragmentationTableModel fragmentationTableModel = new FragmentationTableModel(m_jTable1);
        fragmentationTableModel.setData(fragMa, fragSer, "");

        m_matrixRenderer = new FragTableCustomRenderer();
        m_jTable1.setDefaultRenderer(Double.class, m_matrixRenderer);
        m_jTable1.setSortable(false);
        m_jTable1.setModel(fragmentationTableModel);
        m_jTable1.setVisible(true);
        m_hideFragIntensityButton.updateFragmentsIntensityVisibility();

        m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());

        jScrollFragPane = new JScrollPane(m_jTable1);
        jScrollFragPane.setViewportView(m_jTable1);

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JScrollPane m_scrollPane = new JScrollPane();


        m_scrollPane.setViewportView(m_jTable1);
        m_jTable1.setFillsViewportHeight(true);


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_scrollPane, c);

        add(internalPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);

    }


    private class JsonProperties {

        public int ms_query_initial_id;
        public int peptide_match_rank;
        public TheoreticalFragmentSeries_AW[] frag_table;
        public FragmentMatch_AW[] frag_matches;
    }

    protected class TheoreticalFragmentSeries_AW {

        public String frag_series;
        public double[] masses;
        public int charge = 1; // default to 1 because it is used to multiply
        // the m/z to obtain real mass values for aa
        // calculation

        public void computeCharge() {
            this.charge = 0;
            if (frag_series != null) {
                for (int i = 0; i < frag_series.length(); i++) {
                    if (frag_series.charAt(i) == '+') {
                        this.charge++;
                    }
                }
            }
            if (this.charge == 0) {
                this.charge = 1;
            }

        }
    }

    protected class FragmentMatch_AW {

        public String label;
        public double moz;
        public double calculated_moz;
        public double intensity;
        public int charge = 0; // the charge taken from the serie (++ means
        // double charged)

        public void computeChargeFromLabel() {
            this.charge = 0;
            if (label != null) {
                for (int i = 0; i < label.length(); i++) {
                    if (label.charAt(i) == '+') {
                        this.charge++;
                    }
                }
            }

        }
    }

    public void updateFragmentationTable(DPeptideMatch pepMatch, AbstractDataBox dataBox) {

        m_peptideMatch = pepMatch;
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(dataBox.getProjectId()).getEntityManagerFactory().createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();
            PeptideMatch pmORM = entityManagerMSI.find(PeptideMatch.class,m_peptideMatch.getId());

            Map<String, Long> aw_Map = pmORM.getObjectTreeIdByName();

            Long objectTreeId = null;
            for (Map.Entry<String, Long> entry : aw_Map.entrySet()) {
                objectTreeId = entry.getValue();
            }


            if (objectTreeId == null) {
                updateTableData(new FragmentMatch_AW[0], new TheoreticalFragmentSeries_AW[0], "");
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug("objectr tree id is null, no framgentation table to display for pm_id=" + m_peptideMatch.getId());
            } else {

                ObjectTree ot = entityManagerMSI.find(ObjectTree.class, objectTreeId); // get the objectTree from id.
                String clobData = ot.getClobData();
                String jsonProperties = clobData;

                JsonParser parser = new JsonParser();
                Gson gson = new Gson();

                JsonObject array = parser.parse(jsonProperties).getAsJsonObject();
                JsonProperties jsonProp = gson.fromJson(array, JsonProperties.class);

                // compute the charge for each fragment match from the label
                for (FragmentMatch_AW fragMa : jsonProp.frag_matches) {
                    fragMa.computeChargeFromLabel();
                }

                TheoreticalFragmentSeries_AW[] fragSer = jsonProp.frag_table;
                FragmentMatch_AW[] fragMa = jsonProp.frag_matches;
                
                updateTableData(fragMa, fragSer, m_peptideMatch.getPeptide().getSequence());
            }

            entityManagerMSI.getTransaction().commit();

        } catch (Exception e) {
            entityManagerMSI.getTransaction().rollback();
        } finally {

            entityManagerMSI.close();
        }

    }

    private void updateTableData(FragmentMatch_AW[] fragMa, TheoreticalFragmentSeries_AW[] fragSer, String peptideSequence) {
        FragmentationTableModel fragmentationTableModel = new FragmentationTableModel(m_jTable1);
        fragmentationTableModel.setData(fragMa, fragSer, peptideSequence);
        m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
        m_jTable1.setModel(fragmentationTableModel);
//        ((FragmentationTableModel)m_jTable1.getModel()).setData(fragMa, fragSer, peptideSequence);                
//        m_matrixRenderer.setSelectMatrix(((FragmentationTableModel)m_jTable1.getModel()).getMatrix());
//        ((FragmentationTableModel)m_jTable1.getModel()).fireTableStructureChanged();
        m_hideFragIntensityButton.updateFragmentsIntensityVisibility();
                
    } 
    
    public static double getMassFromAminoAcid(char aa) {
        HashMap<Character, Double> aaHashMap = new HashMap<>();

        aaHashMap.put('A', (double) 71.03711);
        aaHashMap.put('C', (double) 103.00919);
        aaHashMap.put('D', (double) 115.02694);
        aaHashMap.put('E', (double) 129.04259);
        aaHashMap.put('F', (double) 147.06841);
        aaHashMap.put('G', (double) 57.02146);
        aaHashMap.put('H', (double) 137.05891);
        aaHashMap.put('I', (double) 113.08406);
        aaHashMap.put('K', (double) 128.09496);
        aaHashMap.put('L', (double) 113.08406);
        aaHashMap.put('M', (double) 131.04049);
        aaHashMap.put('N', (double) 114.04293);
        aaHashMap.put('P', (double) 97.05276);
        aaHashMap.put('Q', (double) 128.05858);
        aaHashMap.put('R', (double) 156.10111);
        aaHashMap.put('S', (double) 87.03203);
        aaHashMap.put('T', (double) 101.04768);
        aaHashMap.put('V', (double) 99.06841);
        aaHashMap.put('W', (double) 186.07931);
        aaHashMap.put('Y', (double) 163.06333);

        return aaHashMap.get(aa);

    }

    // the getAminoAcidName is not used but could be in the future...
    public String getAminoAcidName(double deltaMass, double tolerance) {

        // scan the spectrum to find potential aminoacids
        HashMap<Double, Character> aaHashMap = new HashMap<>();

        aaHashMap.put((double) 71.03711, 'A');
        aaHashMap.put((double) 103.00919, 'C');
        aaHashMap.put((double) 115.02694, 'D');
        aaHashMap.put((double) 129.04259, 'E');
        aaHashMap.put((double) 147.06841, 'F');
        aaHashMap.put((double) 57.02146, 'G');
        aaHashMap.put((double) 137.05891, 'H');
        aaHashMap.put((double) 113.08406, 'I');
        aaHashMap.put((double) 128.09496, 'K');
        aaHashMap.put((double) 113.08406, 'L');
        aaHashMap.put((double) 131.04049, 'M');
        aaHashMap.put((double) 114.04293, 'N');
        aaHashMap.put((double) 97.05276, 'P');
        aaHashMap.put((double) 128.05858, 'Q');
        aaHashMap.put((double) 156.10111, 'R');
        aaHashMap.put((double) 87.03203, 'S');
        aaHashMap.put((double) 101.04768, 'T');
        aaHashMap.put((double) 99.06841, 'V');
        aaHashMap.put((double) 186.07931, 'W');
        aaHashMap.put((double) 163.06333, 'Y');

        double toleranceCalc = tolerance;
        for (double aaMass : aaHashMap.keySet()) {
            if ((aaMass - toleranceCalc < deltaMass)
                    && (aaMass + toleranceCalc > deltaMass)) {
                return (aaHashMap.get(aaMass).toString());
            }
        }

        NumberFormat formatter = new DecimalFormat("#0.000");

        return ("" + formatter.format(deltaMass)); // return ("*");

    }

    public static class FragmentationTableModel extends LazyTableModel implements ProgressInterface {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private TheoreticalFragmentSeries_AW[] m_fragSer;
        private FragmentMatch_AW[] m_fragMa;
        private String m_peptideSequence;
        private int m_sizeMaxSeries;
        private String[][] m_matrix;
        private double[][] m_matrixIntensity;
        private String[] m_columnNames;

        public FragmentationTableModel(DecoratedTable table) { // constructor
            super(null);
        }

        public void setData(FragmentMatch_AW[] fragMa, TheoreticalFragmentSeries_AW[] fragSer, String peptideSequence) {
            m_fragMa = fragMa;
            m_fragSer = fragSer;
            m_peptideSequence = peptideSequence;

            int sizeMaxSeries = 0;
            for (int i = 0; i < fragSer.length; i++) {
                if (fragSer[i].masses.length > sizeMaxSeries) {
                    sizeMaxSeries = fragSer[i].masses.length;
                }

            }

            m_sizeMaxSeries = sizeMaxSeries;

            // get series names
            String xyzSerieName = "";
            String abcSerieName = "";
            for (int i = 0; i < fragSer.length; i++) {
                switch (fragSer[i].frag_series.charAt(0)) {
                    case 'a': // either a,b or c do:
                    case 'b':
                    case 'c':
                        if (fragSer[i].frag_series.length() > 1) {
                            // then it is either a ++ or a b-H2O and so on...
                        } else { // it's a 'a/b/c' ion
                            abcSerieName = "" + fragSer[i].frag_series.charAt(0);
                        }
                        break;
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':

                        if (fragSer[i].frag_series.length() > 1) {
                            // then it is either a ++ or a b-H2O and so on...
                        } else { // it's a 'x/y/z' ion
                            xyzSerieName = "" + fragSer[i].frag_series.charAt(0);
                        }
                        break;
                    case 'z':
                        xyzSerieName = "" + fragSer[i].frag_series.charAt(0);
                        break;
                    default:
                        break;
                }
            }

            m_columnNames = new String[fragSer.length + 3 + fragSer.length];
            int i = 0;
            m_columnNames[i++] = "amino acid";
            m_columnNames[i++] = abcSerieName + " ion";

            for (TheoreticalFragmentSeries_AW currentFrag : fragSer) {
                m_columnNames[i++] = currentFrag.frag_series + " (M)";
            }

            m_columnNames[i] = xyzSerieName + " ion";
            i++;

            for (TheoreticalFragmentSeries_AW currentFragSer : fragSer) {
                m_columnNames[i] = currentFragSer.frag_series + " (I)";
                i++;
            }

            m_matrix = new String[sizeMaxSeries][m_columnNames.length];
            m_matrixIntensity = new double[sizeMaxSeries][m_columnNames.length];

            double roundTol = 0.0001; // could be put to zero but in case some rounding happens at other's code.
            int nbFound = 0;

            for (int j = 0; j < fragSer.length; j++) { // loop through
                // theoFragment series
                // here
                for (int k = 0; k < fragSer[j].masses.length; k++) { // loop
                    // through
                    // masses
                    // for
                    // each
                    // fragment
                    // series
                    for (i = 0; i < fragMa.length; i++) { // find matching
                        // fragMatches with
                        // theoFragSeries

                        if ((fragMa[i].calculated_moz - roundTol <= (fragSer[j].masses[k]))
                                && (fragMa[i].calculated_moz + roundTol >= fragSer[j].masses[k])) {
                            nbFound++;
                            if (fragSer[j].frag_series.toUpperCase().contains(
                                    "A")
                                    || fragSer[j].frag_series.toUpperCase().contains("B")
                                    || fragSer[j].frag_series.toUpperCase().contains("C")) {
                                m_matrix[k][j + 2] = "ABC";

                            } else if (fragSer[j].frag_series.toUpperCase().contains("X")
                                    || fragSer[j].frag_series.toUpperCase().contains("Y")
                                    || fragSer[j].frag_series.toUpperCase().contains("Z")) {
                                m_matrix[k][j + 2] = "XYZ";
                            } else {
                                LoggerFactory.getLogger(
                                        "ProlineStudio.ResultExplorer").error(
                                        "AW: strange, there is no ABC nor XYZ ions..."
                                        + fragSer[j].frag_series);
                            }
                            m_matrixIntensity[k][j + 2] = fragMa[i].intensity; // assign matching peak intensity
                            m_matrix[k][j + 2] += "intensity";

                        } else {
                        }

                    }
                }
            }

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
                //return(m_fragMa[columnIndex - 3 - m_fragSer.length ].intensity);
                //return(m_fragMa[columnIndex - 3 - m_fragSer.length].intensity);
                if (m_matrixIntensity[rowIndex][columnIndex - m_fragSer.length - 1] > 0) {
                    return (double) Math.round(m_matrixIntensity[rowIndex][columnIndex - m_fragSer.length - 1] * 10000) / 10000;
                } else {
                    return null;
                }


                //return(m_fragSer[columnIndex - 2 - m_fragSer.length - 3].intensity[rowIndex]);
            } else {
                return null;
            }

        }

        @Override
        public boolean isLoaded() {
            return m_table.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return m_table.getLoadingPercentage();
        }

        @Override
        public void initFilters() {
            // TODO Auto-generated method stub
        }

        @Override
        public void filter() {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean filter(int row, int col) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getSubTaskId(int col) {
            // TODO Auto-generated method stub
            return 0;
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
            Component component = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);

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
                    foregroundColor = (isSelected) ? EXTRA_LIGHT_BLUE_COLOR
                            : LIGHT_BLUE_COLOR;
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
