package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.CompareValueRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.utils.StringUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class QuantProteinSetTableModel extends LazyTableModel implements ExportTableSelectionInterface, GlobalTableModelInterface {

    public static final int COLTYPE_PROTEIN_SET_ID = 0;
    public static final int COLTYPE_PROTEIN_SET_NAME = 1;
    public static final int COLTYPE_OVERVIEW = 2;
    public static final int COLTYPE_NB_PEPTIDE = 3;
    public static final int COLTYPE_NB_QUANT_PEPTIDE = 4;
    public static final int LAST_STATIC_COLUMN = COLTYPE_NB_QUANT_PEPTIDE;
    
    private static final String[] m_columnNames = {"Id", "Protein Set", "Overview", "#Peptide", "<html>#Quant.<br/>Peptide</html>"};
    private static final String[] m_columnNamesForFilter = {"Id", "Protein Set", "Overview", "#Peptide", "#Quant.Peptide"};
    private static final String[] m_toolTipColumns = {"MasterQuantProteinSet Id",  "Identified Protein label", "Overview", "Number of Identified Peptides", "Number of Quantified Peptides"};
    
    public static final int COLTYPE_SELECTION_LEVEL = 0;
    public static final int COLTYPE_ABUNDANCE = 1;
    public static final int COLTYPE_RAW_ABUNDANCE = 2;
    public static final int COLTYPE_PSM = 3;
    
    private static final String[] m_columnNamesQC = {"Sel. level", "Abundance", "Raw abundance", "Pep. match count"};
    private static final String[] m_toolTipQC = {"Selection level", "Abundance", "Raw abundance", "Peptides match count"};
    
    private List<DMasterQuantProteinSet> m_proteinSets = null;
    private DQuantitationChannel[] m_quantChannels = null;
    private int m_quantChannelNumber;
    
    private int m_overviewType = COLTYPE_ABUNDANCE;

    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
    private String m_modelName;
    
    
    public QuantProteinSetTableModel(LazyTable table) {
        super(table);
    }
    
 
    
    @Override
    public int getColumnCount() {
        if (m_quantChannels == null) {
            return m_columnNames.length ;
        }else {
            return m_columnNames.length+m_quantChannelNumber*m_columnNamesQC.length;
        }
    }
    
    public int getColumnQCCount(){
        if (m_quantChannels == null) {
            return 0;
        }else{
            return m_quantChannelNumber*m_columnNamesQC.length;
        }
    }

    @Override
    public String getColumnName(int col) {
        if (col<=LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        } else {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length ;
            int id = col - m_columnNames.length -  (nbQc *m_columnNamesQC.length );
            
            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_columnNamesQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            /*sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getRawFileName());*/
            
            sb.append("</html>");
            return sb.toString();
        }
    }
    
    public String getColumnNameForFilter(int col) {
        if (col<=LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            return m_columnNamesQC[id];
        } else {
            return ""; // should not happen
        }
    }
    
    @Override
    public String getExportColumnName(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            
            StringBuilder sb = new StringBuilder();
            sb.append(m_columnNamesQC[id]);
            sb.append(" ");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            return sb.toString();
        }else {
            return ""; // should not happen
        }
        
    }
    
    @Override
    public String getToolTipForHeader(int col) {
        if (col == COLTYPE_OVERVIEW) {
            return m_toolTipColumns[col]+" on "+m_toolTipQC[m_overviewType] ;
        }
        if (col<=LAST_STATIC_COLUMN) {
            return m_toolTipColumns[col];
        } else {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length ;
            int id = col - m_columnNames.length -  (nbQc *m_columnNamesQC.length );
            String rawFilePath = StringUtils.truncate(m_quantChannels[nbQc].getRawFilePath(), 50);
            
            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_toolTipQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            sb.append("<br/>");
            sb.append(rawFilePath);
            
            sb.append("</html>");
            return sb.toString();
        }
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_PROTEIN_SET_ID) {
            return Long.class;
        } else if (col == COLTYPE_OVERVIEW) {
            return CompareValueRenderer.CompareValue.class; 
        }
        return LazyData.class;
    }
    
    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadXicMasterQuantTask.SUB_TASK_PROTEIN_SET;
    }

    
    @Override
    public int getRowCount() {
        if (m_proteinSets == null) {
            return 0;
        }

        return m_proteinSets.size();
    }

    @Override
    public Object getValueAt(final int row, int col) {

        
        // Retrieve Protein Set
        DMasterQuantProteinSet proteinSet = m_proteinSets.get(row);
        //long rsmId = proteinSet.getResultSummaryId();

        switch (col) {
            case COLTYPE_PROTEIN_SET_ID: {
                return proteinSet.getId();
            }
            case COLTYPE_PROTEIN_SET_NAME: {

                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = null;
                if (proteinSet.getProteinSet()!= null) {
                    proteinMatch =proteinSet.getProteinSet().getTypicalProteinMatch();
                    if (proteinMatch == null) {
                        lazyData.setData("");
                    }else {
                        lazyData.setData(proteinMatch.getAccession());
                    }
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }
                
            case COLTYPE_OVERVIEW:
                return new CompareValueRenderer.CompareValue() {

                    @Override
                    public int getNumberColumns() {
                        return m_quantChannels.length ;
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
                       int realCol = LAST_STATIC_COLUMN+1 + m_overviewType+col*m_columnNamesQC.length;
                       LazyData lazyData = (LazyData)getValueAt(row, realCol);
                       if (lazyData != null && lazyData.getData() != null){
                            if (Number.class.isAssignableFrom(lazyData.getData().getClass())) {
                                return ((Number)lazyData.getData()).floatValue();
                            }
                       }
                       return 0;
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
                    public int compareTo(CompareValueRenderer.CompareValue o) {
                        return Double.compare(calculateComparableValue(), o.calculateComparableValue());
                    }
                };
                
            case COLTYPE_NB_PEPTIDE: {
                LazyData lazyData = getLazyData(row,col);
                
                if (proteinSet.getProteinSet()!= null) {
                    lazyData.setData(proteinSet.getNbPeptides());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }   
            case COLTYPE_NB_QUANT_PEPTIDE: {
                LazyData lazyData = getLazyData(row,col);
                
                if (proteinSet.getProteinSet()!= null) {
                    lazyData.setData(proteinSet.getNbQuantifiedPeptides());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }       
                
            default: {
                // Quant Channel columns 
                LazyData lazyData = getLazyData(row,col);
                if (proteinSet.getProteinSet()== null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else{
                    // retrieve quantProteinSet for the quantChannelId
                    int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                    int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                    Map<Long, DQuantProteinSet> quantProteinSetByQchIds = proteinSet.getQuantProteinSetByQchIds();
                    if (quantProteinSetByQchIds == null) {
                        
                        switch (id) {
                            case COLTYPE_SELECTION_LEVEL:
                                lazyData.setData(Integer.valueOf(0));
                                break;
                            case COLTYPE_ABUNDANCE:
                                lazyData.setData(Float.valueOf(0));
                                break;
                            case COLTYPE_RAW_ABUNDANCE:
                                lazyData.setData(Float.valueOf(0));
                                break;
                            case COLTYPE_PSM:
                                lazyData.setData(Integer.valueOf(0));
                                break;
                        }
                    }else{
                        DQuantProteinSet quantProteinSet = quantProteinSetByQchIds.get(m_quantChannels[nbQc].getId()) ;
                        if (quantProteinSet == null) {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                            }
                        } else {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(quantProteinSet.getSelectionLevel());
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(quantProteinSet.getAbundance().isNaN() ? Float.valueOf(0) : quantProteinSet.getAbundance());
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(quantProteinSet.getRawAbundance().isNaN() ? Float.valueOf(0) : quantProteinSet.getRawAbundance());
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(quantProteinSet.getPeptideMatchesCount());
                                    break;
                            }
                        }
                    }
                }
                return lazyData;
            }
        }
        //return null; // should never happen
    }

    public DMasterQuantProteinSet getMasterQuantProteinSet(int row) {

        // Retrieve QuantProtein Set
        DMasterQuantProteinSet quantProteinSet = m_proteinSets.get(row);
        return quantProteinSet;
    }
    
    public DProteinSet getProteinSet(int row) {

        // Retrieve Protein Set
        DMasterQuantProteinSet quantProteinSet = m_proteinSets.get(row);
        return quantProteinSet.getProteinSet();
    }
     
    
    public void setData(Long taskId, DQuantitationChannel[] quantChannels, List<DMasterQuantProteinSet> proteinSets ) {
        this.m_quantChannels = quantChannels ;
        this.m_quantChannelNumber = quantChannels.length;
        this.m_proteinSets = proteinSets;
        fireTableStructureChanged();
        
        m_taskId = taskId;

        fireTableDataChanged();

        
        
    }

    
    public void dataUpdated() {
    
        // no need to do an updateMinMax : scores are known at once
        
        fireTableDataChanged();

    }

    public DMasterQuantProteinSet getQuantProteinSet(int i) {

        return m_proteinSets.get(i);
    }
    
    
    public int findRow(long proteinSetId) {

        int nb = m_proteinSets.size();
        for (int i=0;i<nb;i++) {
            if (proteinSetId == m_proteinSets.get(i).getProteinSetId()) {
                return i;
            }
        }
        return -1;
        
    }
    
    
    public void sortAccordingToModel(ArrayList<Long> proteinSetIds, CompoundTableModel compoundTableModel) {
        
        if (m_proteinSets == null) {
            // data not loaded 
            return;
        }
        
        HashSet<Long> proteinSetIdMap = new HashSet<>(proteinSetIds.size());
        proteinSetIdMap.addAll(proteinSetIds);
        
        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView=0;iView<nb;iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Protein Set
            DMasterQuantProteinSet ps = getQuantProteinSet(iModel);
            if (  proteinSetIdMap.contains(ps.getProteinSetId())  ) {
                proteinSetIds.set(iCur++,ps.getProteinSetId());
            }
        }

    }




    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        filtersMap.put(COLTYPE_PROTEIN_SET_NAME, new StringFilter(getColumnNameForFilter(COLTYPE_PROTEIN_SET_NAME), null));
        filtersMap.put(COLTYPE_NB_PEPTIDE, new IntegerFilter(getColumnNameForFilter(COLTYPE_NB_PEPTIDE), null));
        filtersMap.put(COLTYPE_NB_QUANT_PEPTIDE, new IntegerFilter(getColumnNameForFilter(COLTYPE_NB_QUANT_PEPTIDE), null));

    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }
    
    public int getByQCCount() {
        return m_columnNamesQC.length;
    }

    public int getQCCount() {
        return m_quantChannels.length;
    }
    
    public int getColumStart(int index) {
        return m_columnNames.length+index*m_columnNamesQC.length;
    }
    public int getColumStop(int index) {
        return m_columnNames.length+(1+index)*m_columnNamesQC.length-1;
    }
    
    public String getQCName(int i) {

        StringBuilder sb = new StringBuilder();

        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(i);
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(m_quantChannels[i].getResultFileName());
       /* sb.append("<br/>");
        sb.append(m_quantChannels[i].getRawFileName());*/
        sb.append("</html>");

        return sb.toString();
    }
    
    public String getByQCMColumnName(int index) {
        return m_columnNamesQC[index];
    }
    
    public int getQCNumber(int col) {
        return (col-m_columnNames.length) / m_columnNamesQC.length;
    }
    
    public int getTypeNumber(int col) {
        return (col-m_columnNames.length) % m_columnNamesQC.length;
    }
    
    
    public Long getResultSummaryId() {
        if ((m_proteinSets == null) || (m_proteinSets.size() == 0)) {
            return null;
        }
        
        return m_proteinSets.get(0).getQuantResultSummaryId();
    }
    
    public void setOverviewType(int overviewType) {
        m_overviewType = overviewType;
        fireTableDataChanged();
    }
    
    public int getOverviewType() {
        return m_overviewType;
    }
    
    /**
     * by default the abundance and selectionLevel are hidden
     * return the list of columns ids of these columns
     * @return 
     */
    public List<Integer> getDefaultColumnsToHide() {
        List<Integer> listIds = new ArrayList();
        if (m_quantChannels != null) {
            for (int i=m_quantChannels.length-1; i>=0; i--) {
                listIds.add(m_columnNames.length+COLTYPE_RAW_ABUNDANCE+(i*m_columnNamesQC.length));
                listIds.add(m_columnNames.length+COLTYPE_SELECTION_LEVEL+(i*m_columnNamesQC.length));
            }
        }
        return listIds; 
    }

    @Override
    public HashSet exportSelection(int[] rows) {

        int nbRows = rows.length;
        HashSet selectedObjects = new HashSet();
        for (int i = 0; i < nbRows; i++) {

            int row = rows[i];

            // Retrieve Protein Set
            DMasterQuantProteinSet proteinSet = m_proteinSets.get(row);

            selectedObjects.add(proteinSet.getProteinSet().getId());
        }
        return selectedObjects;
    }

    @Override
    public String getDataColumnIdentifier(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNamesForFilter[col];
        } else {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);

            StringBuilder sb = new StringBuilder();
            sb.append(m_columnNamesQC[id]);
            sb.append(' ');
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            return sb.toString();
        }
    }

    @Override
    public Class getDataColumnClass(int col) {

        switch (col) {
            case COLTYPE_PROTEIN_SET_ID: {
                return Long.class;
            }
            case COLTYPE_PROTEIN_SET_NAME: {
                return String.class;
            }
            case COLTYPE_OVERVIEW: {
                return CompareValueRenderer.CompareValue.class; 
            }
            case COLTYPE_NB_PEPTIDE: {
                return Integer.class; 
            }
            case COLTYPE_NB_QUANT_PEPTIDE: {
                return Integer.class; 
            }
            default: {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                switch (id) {
                    case COLTYPE_SELECTION_LEVEL:
                        return Integer.class;
                    case COLTYPE_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_RAW_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_PSM:
                        return Integer.class;
                        
                }

            }
        }
        return null; // should never happen

    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_PROTEIN_SET_NAME };
        return keys;
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
    public int getInfoColumn() {
        return COLTYPE_PROTEIN_SET_NAME;
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
    public String getExportRowCell(int row, int col) {

        // Retrieve Protein Set
        DMasterQuantProteinSet proteinSet = m_proteinSets.get(row);
        //long rsmId = proteinSet.getResultSummaryId();

        switch (col) {
            case COLTYPE_PROTEIN_SET_ID: {
                return ""+proteinSet.getId();
            }
            case COLTYPE_PROTEIN_SET_NAME: {
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = null;
                if (proteinSet.getProteinSet()!= null) {
                    proteinMatch =proteinSet.getProteinSet().getTypicalProteinMatch();
                    if (proteinMatch == null) {
                        return "";
                    }else {
                        return proteinMatch.getAccession();
                    }
                }else{
                    return "";
                }
            }
                
            case COLTYPE_OVERVIEW:
                return "" ;
                
            case COLTYPE_NB_PEPTIDE: {
               if (proteinSet.getProteinSet()!= null) {
                    return ""+proteinSet.getNbPeptides();
                }else{
                    return "";
                }
            }   
            case COLTYPE_NB_QUANT_PEPTIDE: {
               if (proteinSet.getProteinSet()!= null) {
                    return ""+proteinSet.getNbQuantifiedPeptides();
                }else{
                    return "";
                }
            }       
                
            default: {
                // Quant Channel columns 
                if (proteinSet.getProteinSet()== null) {
                    return "";
                }else{
                    // retrieve quantProteinSet for the quantChannelId
                    Map<Long, DQuantProteinSet> quantProteinSetByQchIds = proteinSet.getQuantProteinSetByQchIds() ;
                    if (quantProteinSetByQchIds == null) {
                        return "";
                    }else{
                        int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length ;
                        int id = col - m_columnNames.length -  (nbQc *m_columnNamesQC.length );
                        DQuantProteinSet quantProteinSet = quantProteinSetByQchIds.get(m_quantChannels[nbQc].getId()) ;
                        if (quantProteinSet == null) {
                            return "";
                        } else {
                            switch (id ) {
                                case COLTYPE_SELECTION_LEVEL : return (quantProteinSet.getSelectionLevel() == null?"":Integer.toString(quantProteinSet.getSelectionLevel()));
                                case COLTYPE_ABUNDANCE : return ((quantProteinSet.getAbundance() == null || quantProteinSet.getAbundance().isNaN())? "":Float.toString(quantProteinSet.getAbundance()));
                                case COLTYPE_RAW_ABUNDANCE : return ((quantProteinSet.getRawAbundance() == null || quantProteinSet.getRawAbundance().isNaN())? "":Float.toString(quantProteinSet.getRawAbundance()));
                                case COLTYPE_PSM : return (quantProteinSet.getPeptideMatchesCount()== null?"":Integer.toString(quantProteinSet.getPeptideMatchesCount()));
                            }
                        }
                    }
                }
            }
        }
        return ""; // should never happen
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
    
}