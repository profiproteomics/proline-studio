package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.task.SpectralCountTask;
import fr.proline.studio.filter.*;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.LazyTable;
import fr.proline.studio.utils.LazyTableModel;
import java.awt.Color;
import java.util.*;


/**
 * Table Model for Proteins
 * @author JM235353
 */
public class WSCProteinTableModel extends LazyTableModel {

    public static final int COLTYPE_PROTEIN_NAME    = 0;
    public static final int COLTYPE_STATUS          = 1;
    public static final int COLTYPE_PEPTIDE_NUMBER  = 2;
    public static final int COLTYPE_BSC             = 3;
    public static final int COLTYPE_SSC             = 4;
    public static final int COLTYPE_WSC             = 5;
    

    private static final String[] m_columnNames = {"Protein", "Status", "Peptide Number", "Basic SC", "Specific SC", "Weighted SC" };
    private static final String[] m_columnTooltips = {"Protein", "Status", "Peptide Number", "Basic Spectral Count", "Specific Spectral Count", "Weighted Spectral Count" };
    private static final Class[] m_columnTypes = {String.class, Integer.class, Float.class, Float.class, Float.class };
    private static final int COLNBR_PER_RSM = m_columnNames.length-1;
    
    private SpectralCountTask.WSCResultData m_wscResult = null;
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

    @Override
    public int getColumnCount() {
        if (m_wscResult == null) {
            return 1;
        }
        return 1 + (m_wscResult.getComputedSCDatasets().size()*COLNBR_PER_RSM);        
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return m_columnNames[col];
            default:    {
              int currentRSMNbr = (col%COLNBR_PER_RSM==0) ? col/COLNBR_PER_RSM : (col/COLNBR_PER_RSM)+1;
              int colSuffixIndex;
              int modulo = col%COLNBR_PER_RSM;
              switch (modulo){
                case 0:
                    colSuffixIndex=COLNBR_PER_RSM;
                    break;
                default:
                    colSuffixIndex=modulo;
                    break;                
                }            
              StringBuilder sb = new StringBuilder();
              
              String rsmHtmlColor = CyclicColorPalette.getHTMLColor(currentRSMNbr-1);
              sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
              if (colSuffixIndex == COLTYPE_STATUS) {
                sb.append(m_wscResult.getComputedSCDatasets().get(currentRSMNbr-1).getName());
                sb.append(" ");
              }
              sb.append(m_columnNames[colSuffixIndex]);
                
              sb.append("</html>");
              return sb.toString();
            }           
        }       
    }
    
    
    @Override
    public String getToolTipForHeader(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return m_columnTooltips[col];
            default: {
                int currentRSMNbr = (col % COLNBR_PER_RSM == 0) ? col / COLNBR_PER_RSM : (col / COLNBR_PER_RSM) + 1;
                int colSuffixIndex;
                int modulo = col % COLNBR_PER_RSM;
                switch (modulo) {
                    case 0:
                        colSuffixIndex = COLNBR_PER_RSM;
                        break;
                    default:
                        colSuffixIndex = modulo;
                        break;
                }
                StringBuilder sb = new StringBuilder();

                String rsmHtmlColor = CyclicColorPalette.getHTMLColor(currentRSMNbr - 1);
                sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                sb.append(m_wscResult.getComputedSCDatasets().get(currentRSMNbr - 1).getName());
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
           default:
                int modulo = col%COLNBR_PER_RSM;
                int colSuffixIndex;
                switch (modulo){
                case 0:
                    colSuffixIndex=COLNBR_PER_RSM;
                    break;
                default:
                    colSuffixIndex=modulo;
                    break;                
                }
                return m_columnTypes[colSuffixIndex-1];
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
    public Object getValueAt(int row, int col) {
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        // Retrieve Protein Match
        String proteinMatchName = m_protMatchNames.get(rowFiltered);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatchName;
                
            default: {
              int currentRSMNbr = (col%COLNBR_PER_RSM==0) ? col/COLNBR_PER_RSM : (col/COLNBR_PER_RSM)+1;
              Long rsmID = m_wscResult.getComputedSCDatasets().get(currentRSMNbr-1).getResultSummaryId();
              Map<String, SpectralCountTask.SpectralCountsStruct> rsmResult =  m_wscResult.getRsmSCResult(rsmID);
              SpectralCountTask.SpectralCountsStruct searchedProtSC = rsmResult.get(proteinMatchName);
              int modulo = col%COLNBR_PER_RSM;
              switch (modulo){
                case 1:
                    if(searchedProtSC != null)
                        return  searchedProtSC.getProtMatchStatus();
                    else
                        return "";
                case 2:
                    if(searchedProtSC != null)
                        return searchedProtSC.getPeptideNumber();
                    else
                        return null;
                case 3:
                    if(searchedProtSC != null)
                        return searchedProtSC.getBsc();
                    else
                        return Float.NaN;
                case 4 :
                    if(searchedProtSC != null)
                        return searchedProtSC.getSsc();
                    else
                        return Float.NaN;
                case 0 :
                    if(searchedProtSC != null)
                        return searchedProtSC.getWsc();
                    else
                        return Float.NaN;                
              }            
            }  
        }
        return null; // should never happen
    }
    

    public void setData(SpectralCountTask.WSCResultData wscResult) {
        m_wscResult = wscResult;
        
        Set<String> names = new HashSet();
        List<DDataset> dss = m_wscResult.getComputedSCDatasets();
        for (DDataset ds :dss) {
           names.addAll(m_wscResult.getRsmSCResult(ds.getResultSummaryId()).keySet());
        }
        m_protMatchNames = new ArrayList(names);  
        
        
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
        for (int iView=0;iView<nb;iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Protein Set
            String proteinName = (String) getValueAt(iModel, COLTYPE_PROTEIN_NAME);
            if (  proteinNamesMap.contains(iModel)  ) {
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
            for (int i=1;i<nbCol;i++) {
                int modulo = i % COLNBR_PER_RSM;
                switch (modulo){
                case 1:
                    m_filters[i] = new StringFilter(getColumnName(i));
                    break;
                case 2:
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
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(Integer.valueOf(i));
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

        int modulo = col % COLNBR_PER_RSM;
        int colSuffixIndex = (modulo == 0) ? COLNBR_PER_RSM : modulo;

        if (colSuffixIndex == 1) { // COLTYPE_STATUS
            return ((StringFilter) filter).filter((String) data);
        }
        if (colSuffixIndex == 2) { // COLTYPE_PEPTIDE_NUMBER
            return ((IntegerFilter) filter).filter((Integer) data);
        }

        // other cases
        return ((DoubleFilter) filter).filter((Float) data);


    }

    @Override
    public boolean isLoaded() {
        return (m_protMatchNames.size() >0 );
    }

    @Override
    public int getLoadingPercentage() {
        if (m_protMatchNames.size() >0 ) {
            return 100;
        }
        return 0;
    }

    @Override
    public int getSubTaskId(int col) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    

    
}
