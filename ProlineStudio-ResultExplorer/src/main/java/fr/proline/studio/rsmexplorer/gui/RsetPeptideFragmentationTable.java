package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.rsmexplorer.spectrum.FragmentMatch_AW;
import fr.proline.studio.rsmexplorer.spectrum.FragmentationJsonProperties;
import fr.proline.studio.rsmexplorer.spectrum.TheoreticalFragmentSeries_AW;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.DecoratedTableModel;
import fr.proline.studio.utils.HideFragmentsTableIntensityButton;

import javax.swing.*;

public class RsetPeptideFragmentationTable extends JPanel {

    /**
     * Created by AW
     */

    private DPeptideMatch m_peptideMatch;
    private HideFragmentsTableIntensityButton m_hideFragIntensityButton = null;
    private DecoratedTable m_table;
    private FragTableCustomRenderer m_matrixRenderer;

    public RsetPeptideFragmentationTable() {
        initComponent();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        
        m_table = new DecoratedTable();
        
        JToolBar toolbar = createToolbar();
        JPanel internalPanel = createInternalPanel();
        
        add(internalPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);

    }
    
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton m_exportButton = new ExportButton(null, "Fragmentation Table", m_table);
        toolbar.add(m_exportButton);

        m_hideFragIntensityButton = new HideFragmentsTableIntensityButton(m_table, false);
        toolbar.add(m_hideFragIntensityButton);
        return toolbar;
    }
    
    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        
        FragmentationTableModel fragmentationTableModel = new FragmentationTableModel(m_table);
        //fragmentationTableModel.setData(new FragmentMatch_AW[0], new TheoreticalFragmentSeries_AW[0], "");

        m_matrixRenderer = new FragTableCustomRenderer();
        m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
        
        m_table.setDefaultRenderer(Double.class, m_matrixRenderer);
        m_table.setSortable(false);
        m_table.setModel(fragmentationTableModel);
        m_hideFragIntensityButton.updateFragmentsIntensityVisibility();


        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(m_table);
        m_table.setFillsViewportHeight(true);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(scrollPane, c);
        
        return internalPanel;
    }





    public void updateFragmentationTable(DPeptideMatch pepMatch, AbstractDataBox dataBox) {

        m_peptideMatch = pepMatch;

        if (m_peptideMatch == null) {
            FragmentationTableModel fragmentationTableModel = ((FragmentationTableModel)m_table.getModel());
            fragmentationTableModel.reinitData();
            m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
            return;
        }
        
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
                FragmentationTableModel fragmentationTableModel = ((FragmentationTableModel)m_table.getModel());
                fragmentationTableModel.reinitData();
                m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug("objectr tree id is null, no framgentation table to display for pm_id=" + m_peptideMatch.getId());
            } else {

                ObjectTree ot = entityManagerMSI.find(ObjectTree.class, objectTreeId); // get the objectTree from id.
                String clobData = ot.getClobData();
                String jsonProperties = clobData;

                JsonParser parser = new JsonParser();
                Gson gson = new Gson();

                JsonObject array = parser.parse(jsonProperties).getAsJsonObject();
                FragmentationJsonProperties jsonProp = gson.fromJson(array, FragmentationJsonProperties.class);

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
        
        FragmentationTableModel fragmentationTableModel = ((FragmentationTableModel)m_table.getModel());
        fragmentationTableModel.setData(fragMa, fragSer, peptideSequence);
        m_matrixRenderer.setSelectMatrix(fragmentationTableModel.getMatrix());
        m_hideFragIntensityButton.updateFragmentsIntensityVisibility();
                
    } 


    public static class FragmentationTableModel extends DecoratedTableModel {


        private TheoreticalFragmentSeries_AW[] m_fragSer = null;
        private String m_peptideSequence = null;
        private int m_sizeMaxSeries = 0;
        private String[][] m_matrix = null;
        private double[][] m_matrixIntensity = null;
        private String[] m_columnNames = null;

        public FragmentationTableModel(DecoratedTable table) { // constructor
        }

        public void reinitData() {
            reinitDataImpl();
            fireTableStructureChanged();
                    
        }
        
        private void reinitDataImpl() {
            m_fragSer = null;
            m_peptideSequence = null;
            m_sizeMaxSeries = 0;
            m_matrix = null;
            m_matrixIntensity = null;
            m_columnNames = null;
        }
        
        
        public void setData(FragmentMatch_AW[] fragMa, TheoreticalFragmentSeries_AW[] fragSer, String peptideSequence) {
            
            reinitDataImpl();
            
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
