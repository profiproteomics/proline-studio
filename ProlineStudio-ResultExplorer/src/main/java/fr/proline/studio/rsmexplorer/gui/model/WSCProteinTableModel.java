package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dpm.task.SpectralCountTask;
import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * Table Model for Proteins
 * @author JM235353
 */
public class WSCProteinTableModel extends AbstractTableModel {

    public static final int COLTYPE_PROTEIN_NAME    = 0;
    public static final int COLTYPE_BSC             = 1;
    public static final int COLTYPE_SSC             = 2;
    public static final int COLTYPE_WSC             = 3;
    public static final int COLTYPE_STATUS          = 4;
//    private static final String[] m_columnNames = {"Protein", "Basic SC", "Specific SC", "Weighted SC"};
    private static final String[] m_columnNames = {"Protein", "Basic SC", "Specific SC", "Weighted SC","Status"};
    private static final Class[] m_rsmColumnType = {Float.class, Float.class, Float.class,String.class};
    private static final int COLNBR_PER_RSM = m_columnNames.length-1;
    
    private SpectralCountTask.WSCResultData m_wscResult = null;
    private List<String> m_protMatchNames = new ArrayList();
    
    public String getProteinMatchAccession(int row) {
        return m_protMatchNames.get(row);
    }

    @Override
    public int getColumnCount() {
        if (m_wscResult ==null)
            return 1;
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
              sb.append(m_wscResult.getComputedSCDatasets().get(currentRSMNbr-1).getName());
              sb.append("_").append(m_columnNames[colSuffixIndex]);
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
                return m_rsmColumnType[colSuffixIndex-1];
        }        
    }

    @Override
    public int getRowCount() {      
        return m_protMatchNames.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Match
        String proteinMatchName = getProteinMatchAccession(row);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatchName;
                
            default: {
              int currentRSMNbr = (col%COLNBR_PER_RSM==0) ? col/COLNBR_PER_RSM : (col/COLNBR_PER_RSM)+1;
              Long rsmID =m_wscResult.getComputedSCDatasets().get(currentRSMNbr-1).getResultSummaryId();
              Map<String, SpectralCountTask.SpectralCountsStruct> rsmResult =  m_wscResult.getRsmSCResult(rsmID);
              SpectralCountTask.SpectralCountsStruct searchedProtSC = rsmResult.get(proteinMatchName);
              int modulo = col%COLNBR_PER_RSM;
              switch (modulo){
                case 0:
                    if(searchedProtSC != null)
                        return  searchedProtSC.getProtMatchStatus();
                    else
                        return "";
                case 1:
                    if(searchedProtSC != null)
                        return searchedProtSC.getBsc();
                    else
                        return Float.NaN;
                case 2 :
                    if(searchedProtSC != null)
                        return searchedProtSC.getSsc();
                    else
                        return Float.NaN;
                case 3 :
                    if(searchedProtSC != null)
                        return searchedProtSC.getWsc();
                    else
                        return Float.NaN;                }            
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
        fireTableStructureChanged();
    }
    

    
}
