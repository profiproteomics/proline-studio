package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.dpm.data.SpectralCountResultData;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.CompareValueRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.CompareValueRenderer.CompareValue;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.Color;
import java.util.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Table Model for Spectral Count
 *
 * @author JM235353
 */
public class WSCProteinTableModel extends LazyTableModel implements GlobalTableModelInterface {

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

    private String m_modelName;

    public WSCProteinTableModel(LazyTable table) {
        super(table);
    }

    public String getProteinMatchAccession(int row) {

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
    public String getTootlTipValue(int row, int col) {
        return null;
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

        return m_protMatchNames.size();
    }

    @Override
    public Object getValueAt(final int row, int col) {

        // Retrieve Protein Match
        String proteinMatchName = m_protMatchNames.get(row);

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


        fireTableStructureChanged();
    }

    public void sortAccordingToModel(ArrayList<Integer> proteinNames, CompoundTableModel compoundTableModel) {

        if (m_protMatchNames.isEmpty()) {
            // data not loaded 
            return;
        }

        HashSet<Integer> proteinNamesMap = new HashSet<>(proteinNames.size());
        proteinNamesMap.addAll(proteinNames);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Protein Set

            if (proteinNamesMap.contains(iModel)) {
                proteinNames.set(iCur++, iModel);
            }
        }

    }


    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        filtersMap.put(COLTYPE_PROTEIN_NAME, new StringFilter(getColumnName(COLTYPE_PROTEIN_NAME), null));
        int nbCol = getColumnCount();
        for (int i = COLTYPE_STATUS; i < nbCol; i++) {
            int colSuffixIndex = getTypeNumber(i) + 2;
            switch (colSuffixIndex) {
                case COLTYPE_STATUS:
                    filtersMap.put(i, new StringFilter(getColumnName(i), null));
                    break;
                case COLTYPE_PEPTIDE_NUMBER:
                    filtersMap.put(i, new IntegerFilter(getColumnName(i), null));
                    break;
                default:
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null));
                    break;
            }

        }

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

        // Retrieve Protein Match
        String proteinMatchName = m_protMatchNames.get(row);

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
        int[] keys = { COLTYPE_PROTEIN_NAME };
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PROTEIN_NAME;
    }

    @Override
    public void setName(String name) {
        m_modelName = name;
    }

    @Override
    public String getName() {
        return m_modelName;
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
    public PlotType getBestPlotType() {
        return null; //JPM.TODO
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return -1; //JPM.TODO
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1; //JPM.TODO
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }
        
        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_PROTEIN_NAME: {
                renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", COLTYPE_PROTEIN_NAME);
                break;
            }
            case COLTYPE_OVERVIEW: {
                renderer = new CompareValueRenderer();
                break;
            }
            default: {
                int colSuffixIndex = getTypeNumber(col) + 2;
                switch (colSuffixIndex) {
                    //private static final Class[] m_columnTypes = {String.class, Integer.class, Float.class, Float.class, Float.class};
                    case COLTYPE_STATUS: {
                        renderer = TableDefaultRendererManager.getDefaultRenderer(String.class);
                        break;
                    }
                    case COLTYPE_PEPTIDE_NUMBER: {
                        renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                        break;
                    }
                    case COLTYPE_BSC:
                    case COLTYPE_SSC:
                    case COLTYPE_WSC: {
                        renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                    }
                }

            }

        }
        m_rendererMap.put(col, renderer);
        return renderer;
    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
}
