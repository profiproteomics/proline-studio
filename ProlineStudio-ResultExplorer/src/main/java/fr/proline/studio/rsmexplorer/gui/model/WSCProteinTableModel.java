package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.dpm.data.SpectralCountResultData;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.export.ExportRowTextInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.rsmexplorer.gui.renderer.CompareValueRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.CompareValueRenderer.CompareValue;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.awt.Color;
import java.util.*;

/**
 * Table Model for Spectral Count
 *
 * @author JM235353
 */
public class WSCProteinTableModel extends LazyTableModel implements ExportColumnTextInterface, ExportRowTextInterface {

    public static final int COLTYPE_PROTEIN_NAME = 0;
    public static final int COLTYPE_OVERVIEW = 1;
    public static final int COLTYPE_STATUS = 2;
    public static final int COLTYPE_PEPTIDE_NUMBER = 3;
    public static final int COLTYPE_BSC = 4;
    public static final int COLTYPE_SSC = 5;
    public static final int COLTYPE_WSC = 6;
    
    public int m_overviewType = COLTYPE_BSC;
    
    private static final String[] m_columnNames = {"Protein", "Overview", "Status", "Peptide Number", "Basic SC", "Specific SC", "Weighted SC"};
    private static final String[] m_columnTooltips = {"Protein",  "Overview", "Status", "Peptide Number", "Basic Spectral Count", "Specific Spectral Count", "Weighted Spectral Count"};
    private static final Class[] m_columnTypes = {String.class, Integer.class, Float.class, Float.class, Float.class};
    private static final int COLNBR_PER_RSM = m_columnNames.length - 2;
    
    private SpectralCountResultData m_wscResult = null;
    private List<String> m_protMatchNames = new ArrayList();
    
    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    public WSCProteinTableModel(LazyTable table) {
        super(table);
    }

    public String getProteinMatchAccession(int row) {

        if (m_filteredIds != null) {
            row = m_filteredIds.get(row).intValue();
        }

        return m_protMatchNames.get(row);
    }
    
    public void setOverviewType(int overviewType) {
        m_overviewType = overviewType;
        fireTableDataChanged();
    }
    
    public int getOverviewType() {
        return m_overviewType;
    }

    @Override
    public int getColumnCount() {
        if (m_wscResult == null) {
            return 1;
        }
        return 2 + (m_wscResult.getComputedSCRSMIds().size() * COLNBR_PER_RSM);
    }

    public int getColumStart(int rsmIndex) {
        return 2+rsmIndex*COLNBR_PER_RSM;
    }
    public int getColumStop(int rsmIndex) {
        return 1+(1+rsmIndex)*COLNBR_PER_RSM;
    }
    
    public int getRsmCount() {
        return m_wscResult.getComputedSCRSMIds().size();
    }
    
    public String getRsmName(int i) {

        StringBuilder sb = new StringBuilder();

        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(i);
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(m_wscResult.getComputedSCDatasetNames().get(i));
        sb.append("</html>");

        return sb.toString();
    }
    
    public int getByRsmCount() {
        return m_columnNames.length-2;
    }
    
    public String getByRsmColumnName(int index) {
        return m_columnNames[index+2];
    }
    
    public int getRsmNumber(int col) {
        return (col-2) / COLNBR_PER_RSM;
    }
    
    public int getTypeNumber(int col) {
        return (col-2) % COLNBR_PER_RSM;
    }
    
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_OVERVIEW:
                return m_columnNames[col];
            default: {
                int currentRSMNbr = getRsmNumber(col)+1;
                int colSuffixIndex = getTypeNumber(col)+2;

                StringBuilder sb = new StringBuilder();

                String rsmHtmlColor = CyclicColorPalette.getHTMLColor(currentRSMNbr - 1);
                sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                if (colSuffixIndex == COLTYPE_STATUS) {
                    sb.append(m_wscResult.getComputedSCDatasetNames().get(currentRSMNbr - 1));
                    sb.append(" ");
                }
                sb.append(m_columnNames[colSuffixIndex]);

                sb.append("</html>");
                return sb.toString();
            }
        }
    }

    @Override
    public String getExportColumnName(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_OVERVIEW:
                return m_columnNames[col];
            default: {
                int currentRSMNbr = getRsmNumber(col)+1;
                int colSuffixIndex = getTypeNumber(col)+2;
                StringBuilder sb = new StringBuilder();


                sb.append(m_wscResult.getComputedSCDatasetNames().get(currentRSMNbr - 1));
                sb.append(" ");
                sb.append(m_columnNames[colSuffixIndex]);

                return sb.toString();
            }
        }
    }
    
    @Override
    public String getToolTipForHeader(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
            case COLTYPE_OVERVIEW:
                return m_columnTooltips[col];
            default: {
                int currentRSMNbr = getRsmNumber(col)+1;
                int colSuffixIndex = getTypeNumber(col)+2;
                StringBuilder sb = new StringBuilder();

                String rsmHtmlColor = CyclicColorPalette.getHTMLColor(currentRSMNbr - 1);
                sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                sb.append(m_wscResult.getComputedSCDatasetNames().get(currentRSMNbr - 1));
                sb.append(" ");

                sb.append(m_columnTooltips[colSuffixIndex]);

                sb.append("</html>");
                return sb.toString();
            }
        }
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return String.class;
            case COLTYPE_OVERVIEW:
                return CompareValueRenderer.CompareValue.class; 
            default:
                int colSuffixIndex = getTypeNumber(col);
                return m_columnTypes[colSuffixIndex];
        }
    }

    @Override
    public int getRowCount() {

        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }

        return m_protMatchNames.size();
    }

    @Override
    public Object getValueAt(final int row, int col) {

        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }

        // Retrieve Protein Match
        String proteinMatchName = m_protMatchNames.get(rowFiltered);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatchName;
            case COLTYPE_OVERVIEW:
                return new CompareValueRenderer.CompareValue() {

                    @Override
                    public int getNumberColumns() {
                        return getRsmCount();
                    }

                    @Override
                    public Color getColor(int col) {
                        return CyclicColorPalette.getColor(col);
                    }

                    @Override
                    public double getValue(int col) {
                        if (m_overviewType == -1) {
                            return 0; // should not happen
                        }
                       int realCol = m_overviewType+col*getByRsmCount();
                       return ((Float)getValueAt(row, realCol)).doubleValue();
                    }
                    
                    public double getValueNoNaN(int col) {
                        double val = getValue(col);
                        if (val != val) { // NaN value
                            return 0;
                        }
                        return val;
                    }

                    @Override
                    public double getMaximumValue() {
                        int nbCols = getNumberColumns();
                        double maxValue = 0;
                        for (int i=0;i<nbCols;i++) {
                            double v = getValue(i);
                            if (v>maxValue) {
                                maxValue = v;
                            }
                        }
                        return maxValue;
                        
                    }

                    @Override
                    public double calculateComparableValue() {
                        int nbColumns = getNumberColumns();
                        double mean = 0;
                        for (int i=0;i<nbColumns;i++) {
                            mean += getValueNoNaN(i);
                        }
                        mean /= nbColumns;
                        
                        double maxDiff = 0;
                        for (int i=0;i<nbColumns;i++) {
                            double diff = getValueNoNaN(i)-mean;
                            if (diff<0) {
                                diff = -diff;
                            }
                            if (diff>maxDiff) {
                                maxDiff = diff;
                            }
                        }
                        return maxDiff / mean;
                    }
                    
                    @Override
                    public int compareTo(CompareValue o) {
                        return Double.compare(calculateComparableValue(), o.calculateComparableValue());
                    }
                };
            default: {
                int currentRSMNbr = getRsmNumber(col)+1;
                Long rsmID = m_wscResult.getComputedSCRSMIds().get(currentRSMNbr - 1);
                Map<String, SpectralCountResultData.SpectralCountsStruct> rsmResult = m_wscResult.getRsmSCResult(rsmID);
                SpectralCountResultData.SpectralCountsStruct searchedProtSC = rsmResult.get(proteinMatchName);
                int colSuffixIndex = getTypeNumber(col)+2;
                switch (colSuffixIndex) {
                    case COLTYPE_STATUS:
                        if (searchedProtSC != null) {
                            return searchedProtSC.getProtMatchStatus();
                        } else {
                            return "";
                        }
                    case COLTYPE_PEPTIDE_NUMBER:
                        if (searchedProtSC != null) {
                            return searchedProtSC.getPeptideNumber();
                        } else {
                            return null;
                        }
                    case COLTYPE_BSC:
                        if (searchedProtSC != null) {
                            return searchedProtSC.getBsc();
                        } else {
                            return Float.NaN;
                        }
                    case COLTYPE_SSC:
                        if (searchedProtSC != null) {
                            return searchedProtSC.getSsc();
                        } else {
                            return Float.NaN;
                        }
                    case COLTYPE_WSC:
                        if (searchedProtSC != null) {
                            return searchedProtSC.getWsc();
                        } else {
                            return Float.NaN;
                        }
                }
            }
        }
        return null; // should never happen
    }

    public void setData(SpectralCountResultData wscResult) {
        m_wscResult = wscResult;

        Set<String> names = new HashSet();
        List<Long> rsmIds = m_wscResult.getComputedSCRSMIds();
        for (Long rsmId : rsmIds) {
            names.addAll(m_wscResult.getRsmSCResult(rsmId).keySet());
        }
        m_protMatchNames = new ArrayList(names);

        // calculate maximum value
        /*m_maximumValue = 0;
        for (int row=0; row<getRowCount(); row++) {
            for (int rsm=0;rsm<getRsmCount();rsm++) {
                double v = ((Float) getValueAt(row, COLTYPE_BSC + rsm*COLNBR_PER_RSM)).doubleValue();
                if (v>m_maximumValue) {
                    m_maximumValue = v;
                }
            }
        }*/

        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        }

        fireTableStructureChanged();
    }

    public void sortAccordingToModel(ArrayList<Integer> proteinNames) {

        if (m_protMatchNames.isEmpty()) {
            // data not loaded 
            return;
        }

        HashSet<Integer> proteinNamesMap = new HashSet<>(proteinNames.size());
        proteinNamesMap.addAll(proteinNames);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Protein Set
            String proteinName = (String) getValueAt(iModel, COLTYPE_PROTEIN_NAME);
            if (proteinNamesMap.contains(iModel)) {
                proteinNames.set(iCur++, iModel);
            }
        }

        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PROTEIN_NAME] = new StringFilter(getColumnName(COLTYPE_PROTEIN_NAME));
            m_filters[COLTYPE_OVERVIEW] = null;
            for (int i = COLTYPE_STATUS; i < nbCol; i++) {
                int colSuffixIndex = getTypeNumber(i)+2;
                switch (colSuffixIndex) {
                    case COLTYPE_STATUS:
                        m_filters[i] = new StringFilter(getColumnName(i));
                        break;
                    case COLTYPE_PEPTIDE_NUMBER:
                        m_filters[i] = new IntegerFilter(getColumnName(i));
                        break;
                    default:
                        m_filters[i] = new DoubleFilter(getColumnName(i));
                        break;
                }

            }
        }
    }

    @Override
    public void filter() {

        if (m_wscResult == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_protMatchNames.size();
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                
                Integer iInteger = i;
                
                if ((m_restrainIds!=null) && (!m_restrainIds.isEmpty()) && (!m_restrainIds.contains(iInteger))) {
                    continue;
                }
                
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(iInteger);
            }

        } finally {
            m_isFiltering = false;
        }
        fireTableDataChanged();
    }

    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }

        Object data = getValueAt(row, col);
        if (data == null) {
            return true; // should not happen
        }

        if (col == COLTYPE_PROTEIN_NAME) {
            return ((StringFilter) filter).filter((String) data);
        }

        int colSuffixIndex = getTypeNumber(col)+2;

        if (colSuffixIndex == COLTYPE_STATUS) { // COLTYPE_STATUS
            return ((StringFilter) filter).filter((String) data);
        }
        if (colSuffixIndex == COLTYPE_PEPTIDE_NUMBER) {
            return ((IntegerFilter) filter).filter((Integer) data);
        }

        // other cases
        return ((DoubleFilter) filter).filter((Float) data);


    }

    @Override
    public boolean isLoaded() {
        return (m_protMatchNames.size() > 0);
    }

    @Override
    public int getLoadingPercentage() {
        if (m_protMatchNames.size() > 0) {
            return 100;
        }
        return 0;
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }

        // Retrieve Protein Match
        String proteinMatchName = m_protMatchNames.get(rowFiltered);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatchName;
            case COLTYPE_OVERVIEW:
                return "";

            default: {
                int currentRSMNbr = getRsmNumber(col)+1;
                Long rsmID = m_wscResult.getComputedSCRSMIds().get(currentRSMNbr - 1);
                Map<String, SpectralCountResultData.SpectralCountsStruct> rsmResult = m_wscResult.getRsmSCResult(rsmID);
                SpectralCountResultData.SpectralCountsStruct searchedProtSC = rsmResult.get(proteinMatchName);
                int colSuffixIndex = getTypeNumber(col)+2;
                switch (colSuffixIndex) {
                    case COLTYPE_STATUS:
                        if (searchedProtSC != null) {
                            return searchedProtSC.getProtMatchStatus();
                        } else {
                            return "";
                        }
                    case COLTYPE_PEPTIDE_NUMBER:
                        if (searchedProtSC != null) {
                            return Integer.toString(searchedProtSC.getPeptideNumber());
                        } else {
                            return "0";
                        }
                    case COLTYPE_BSC:
                        if (searchedProtSC != null) {
                            return Float.toString(searchedProtSC.getBsc());
                        } else {
                            return "0";
                        }
                    case COLTYPE_SSC:
                        if (searchedProtSC != null) {
                            return Float.toString(searchedProtSC.getSsc());
                        } else {
                            return "0";
                        }
                    case COLTYPE_WSC:
                        if (searchedProtSC != null) {
                            return Float.toString(searchedProtSC.getWsc());
                        } else {
                            return "0";
                        }
                }
            }
        }
        return null; // should never happen
    }
}
