package fr.proline.studio.rsmexplorer.gui.model;



import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.dam.tasks.DatabaseProteinMatchesTask;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.utils.RelativePainterHighlighter;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Table Model for Peptide Matches
 * @author JM235353
 */
public class ProteinsOfPeptideMatchTableModel extends LazyTableModel implements CompareDataInterface {

    public static final int COLTYPE_PROTEIN_ID             = 0;
    public static final int COLTYPE_PROTEIN_NAME           = 1;
    public static final int COLTYPE_PROTEIN_SCORE          = 2;
    public static final int COLTYPE_PROTEIN_PEPTIDES_COUNT = 3;
    public static final int COLTYPE_PROTEIN_MASS           = 4;
    private static final String[] m_columnNames = {"Id","Protein", "Score", "Peptides", "Mass"};
    private DProteinMatch[] m_proteinMatchArray = null;

    
    
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    private String m_modelName;
    
    public ProteinsOfPeptideMatchTableModel(LazyTable table) {
        super(table);
    }
    
    public DProteinMatch getProteinMatch(int row) {

        if (m_filteredIds != null) {
            row = m_filteredIds.get(row).intValue();
        }
        
        return m_proteinMatchArray[row];
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
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
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_ID:
                return Long.class;
            case COLTYPE_PROTEIN_NAME:
                return String.class;
            case COLTYPE_PROTEIN_SCORE:
                return Float.class;
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return Integer.class;
            case COLTYPE_PROTEIN_MASS:
                return LazyData.class;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        if (m_proteinMatchArray == null) {
            return 0;
        }
                        
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }

        return m_proteinMatchArray.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

               
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        
        DProteinMatch proteinMatch = m_proteinMatchArray[rowFiltered];

        switch (col) {
            case COLTYPE_PROTEIN_ID:
                return proteinMatch.getId();
            case COLTYPE_PROTEIN_NAME:
                return proteinMatch.getAccession();
            case COLTYPE_PROTEIN_SCORE:
                return proteinMatch.getScore();
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return proteinMatch.getPeptideCount();
            case COLTYPE_PROTEIN_MASS:
                LazyData lazyData = getLazyData(row,col);
                
                DBioSequence bioSequence = proteinMatch.getDBioSequence();
                if (bioSequence != null) {
                    lazyData.setData(new Float(bioSequence.getMass()));
                    return lazyData;
                } else if (proteinMatch.isDBiosequenceSet()) {
                    lazyData.setData("");
                    return lazyData;
                }
                
                lazyData.setData(null);
                    
                givePriorityTo(m_taskId, row, col);
                
                return lazyData;
                
        }
        return null; // should never happen
    }

    public void setData(DProteinMatch[] proteinMatchArray) {
        m_proteinMatchArray = proteinMatchArray;

        updateMinMax();
        
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }
        

    }
    
    private void updateMinMax() {
        
        if (m_proteinMatchArray == null) {
            return;
        }

        RelativePainterHighlighter.NumberRelativizer relativizer = m_table.getRelativizer();
        if (relativizer == null) {
            return;
        }

        double maxScore = 0;
        int size = getRowCount();
        for (int i = 0; i < size; i++) {
            DProteinMatch proteinMatch = m_proteinMatchArray[i];
            double score = proteinMatch.getScore();
            if (score > maxScore) {
                maxScore = score;
            }
        }
        relativizer.setMax(maxScore);

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

    public int findRow(long proteinMatchId) {
        
        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (proteinMatchId == m_proteinMatchArray[m_filteredIds.get(i)].getId()) {
                    return i;
                }
            }
            return -1;
        }

        
        int nb = m_proteinMatchArray.length;
        for (int i = 0; i < nb; i++) {
            if (proteinMatchId == m_proteinMatchArray[i].getId()) {
                return i;
            }
        }
        return -1;

    }
    
    @Override
    public int getSubTaskId(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_MASS:
                return DatabaseProteinMatchesTask.SUB_TASK_BIOSEQUENCE;
        }
        return -1;
    }
    
    public void dataUpdated() {
        // no need to do an updateMinMax : scores are known at once

        if (m_filteredIds != null) {
            filter();
        } else {
            fireTableDataChanged();
        }
        
    }

    
    public void sortAccordingToModel(ArrayList<Long> proteinMatchIds) {
        
        if (m_proteinMatchArray == null) {
            // data not loaded 
            return;
        }
        
        HashSet<Long> proteinMatchIdMap = new HashSet<>(proteinMatchIds.size());
        proteinMatchIdMap.addAll(proteinMatchIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            DProteinMatch pm = m_proteinMatchArray[iModel];
            if (proteinMatchIdMap.contains(pm.getId())) {
                proteinMatchIds.set(iCur++, pm.getId());
            }
        }

        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }
    
        @Override
    public void filter() {

        if (m_proteinMatchArray == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_proteinMatchArray.length;
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                try {
                if (!filter(i)) {
                    continue;
                }
                } catch (Exception e) {
                    //e.printStackTrace();
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
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            if (data == null) {
                return true; // should not happen
            }
        }
        
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return ((StringFilter) filter).filter((String)data);
            case COLTYPE_PROTEIN_SCORE: {
                return ((DoubleFilter) filter).filter((Float)data);
            }
            case COLTYPE_PROTEIN_MASS: {
              if (data instanceof String) {
                  return true;
              } else {
                  return ((DoubleFilter) filter).filter((Float)data);
              }
            } case COLTYPE_PROTEIN_PEPTIDES_COUNT: {
                return ((IntegerFilter) filter).filter((Integer)data);
            }

    
        }
        
        return true; // should never happen
    }


    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PROTEIN_ID] = null;
            m_filters[COLTYPE_PROTEIN_NAME] = new StringFilter(getColumnName(COLTYPE_PROTEIN_NAME));
            m_filters[COLTYPE_PROTEIN_SCORE] = new DoubleFilter(getColumnName(COLTYPE_PROTEIN_SCORE));
            m_filters[COLTYPE_PROTEIN_PEPTIDES_COUNT] = new IntegerFilter(getColumnName(COLTYPE_PROTEIN_PEPTIDES_COUNT));
            m_filters[COLTYPE_PROTEIN_MASS] = new DoubleFilter(getColumnName(COLTYPE_PROTEIN_MASS));

        }
    }

    @Override
    public boolean isLoaded() {
        return m_table.isSortable();
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex == COLTYPE_PROTEIN_MASS) {
                return Float.class;
        }
        return getColumnClass(columnIndex);
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
        int[] keys = { COLTYPE_PROTEIN_NAME, COLTYPE_PROTEIN_ID };
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
        return COLTYPE_PROTEIN_NAME;
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }

    
    @Override
    public Color getPlotColor() {
        return null;
    }

    @Override
    public String getPlotTitle() {
        return null;
    }
 
}
