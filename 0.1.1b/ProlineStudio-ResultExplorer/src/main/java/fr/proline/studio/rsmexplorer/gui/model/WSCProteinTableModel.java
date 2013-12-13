package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dpm.task.ComputeSCTask;
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
    private static final String[] m_columnNames = {"Protein", "Basic SC", "Specific SC", "Weighted SC"};

    private ComputeSCTask.WSCResultData wscResult = null;
    private List<String> protMatchNames = new ArrayList();
    
    public String getProteinMatchAccession(int row) {
        return protMatchNames.get(row);
    }

    @Override
    public int getColumnCount() {
        if(wscResult ==null)
            return 1;
        return 1 + (wscResult.getComputedSCDatasets().size()*3);        
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return m_columnNames[col];
            default:    {
              int currentRSMNbr = (col%3==0) ? col/3 : (col/3)+1;
              int colSuffixIndex;
              int modulo = col%3;
              switch (modulo){
                case 0:
                    colSuffixIndex=3;
                    break;
                default:
                    colSuffixIndex=modulo;
                    break;                
                }            
              StringBuilder sb = new StringBuilder();
              sb.append(wscResult.getComputedSCDatasets().get(currentRSMNbr-1).getName());
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
                return Float.class;
        }        
    }

    @Override
    public int getRowCount() {      
        return protMatchNames.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Match
        String proteinMatchName = getProteinMatchAccession(row);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatchName;
                
            default: {
              int currentRSMNbr = (col%3==0) ? col/3 : (col/3)+1;
              Long rsmID =wscResult.getComputedSCDatasets().get(currentRSMNbr-1).getResultSummaryId();
              Map<String, ComputeSCTask.SpectralCountsStruct> rsmResult =  wscResult.getRsmSCResult(rsmID);
              ComputeSCTask.SpectralCountsStruct searchedProtSC = rsmResult.get(proteinMatchName);
              int modulo = col%3;
              switch (modulo){
                case 0:
                    return searchedProtSC.getWsc();
                case 1:
                    return searchedProtSC.getBsc();
                case 2 :
                    return searchedProtSC.getSsc();
                }            
            }  
        }
        return null; // should never happen
    }
    

    public void setData(ComputeSCTask.WSCResultData wscResult) {
        this.wscResult = wscResult;
        
        Set<String> names = new HashSet();
        List<Dataset> dss = this.wscResult.getComputedSCDatasets();
        for(Dataset ds :dss) {
           names.addAll(this.wscResult.getRsmSCResult(ds.getResultSummaryId()).keySet());
        }
        protMatchNames = new ArrayList(names);    
        fireTableStructureChanged();
    }
    
    public int findRowToSelect(String searchedText) {
        int rowToBeSelected = 0;
        if ((searchedText != null) && (searchedText.length() > 0)) {

            String searchedTextUpper = searchedText.toUpperCase();

            for (int row = 0; row < getRowCount(); row++) {
                String proteinName = ((String) getValueAt(row, COLTYPE_PROTEIN_NAME)).toUpperCase();
                if (proteinName.indexOf(searchedTextUpper) != -1) {
                    rowToBeSelected = row;
                }
            }
        }

        return rowToBeSelected;
    }
    
}
