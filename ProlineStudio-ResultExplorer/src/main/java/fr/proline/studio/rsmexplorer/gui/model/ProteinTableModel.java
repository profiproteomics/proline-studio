package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.util.ArrayList;
import java.util.Map;

/**
 * Table Model for Proteins
 * @author JM235353
 */
public class ProteinTableModel extends FilterTableModel implements CompareDataInterface {




    public enum Column {
        PROTEIN_ID("Id", Long.class, 0),
        PROTEIN_NAME("Protein", String.class, 1),
        PROTEIN_DESCRIPTION("Description", String.class, 2),
        SAMESET_SUBSET("Sameset / Subset", Sameset.class, 3),
        PROTEIN_SCORE("Score", Float.class, 4),
        PROTEIN_PEPTIDES_COUNT("Peptides", Integer.class, 5),
        PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT("Unique Seq.", Integer.class, 6),
        PROTEIN_MASS("Mass", Float.class, 7);
        
        String m_name; 
        Class m_class;
        int m_colId;
        
        Column(String n, Class cl, int colId) {
            m_name = n; 
            m_class = cl;
            m_colId = colId;
        }
    }
     
    private DProteinMatch[] m_sameSetMatches = null;
    private DProteinMatch[] m_subSetMatches = null;
    private Long m_rsmId = null;
    private long m_typicalProteinMatchId;

    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    private ProgressInterface m_progressInterface = null;

    private String m_modelName;
    
    public ProteinTableModel(ProgressInterface progressInterface) {
        m_progressInterface = progressInterface;
    }

    public DProteinMatch getProteinMatch(int row) {

        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }

        int sameSetNb = m_sameSetMatches.length;
        if (rowFiltered < sameSetNb) {
            return m_sameSetMatches[rowFiltered];
        }
        return m_subSetMatches[rowFiltered - sameSetNb];
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public String getColumnName(int col) {
        return Column.values()[col].m_name;
    }

    @Override
    public String getToolTipForHeader(int col) {

        if (col == Column.SAMESET_SUBSET.ordinal()) {
            String urlTypical = IconManager.getURLForIcon(IconManager.IconType.TYPICAL);
            String urlSameset = IconManager.getURLForIcon(IconManager.IconType.SAME_SET);
            String urlSubset = IconManager.getURLForIcon(IconManager.IconType.SUB_SET);
            return "<html><img src=\"" + urlTypical + "\">&nbsp;Typical Protein in Sameset<br><img src=\"" + urlSameset + "\">&nbsp;Sameset<br><img src=\"" + urlSubset + "\">&nbsp;Subset</html>";
        } else {
            return getColumnName(col);
        }

    }


    @Override
    public Class getColumnClass(int col) {
        return Column.values()[col].m_class;
    }

    @Override
    public int getRowCount() {
        if (m_sameSetMatches == null) {
            return 0;
        }

        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }

        return m_sameSetMatches.length + m_subSetMatches.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Protein Match
        DProteinMatch proteinMatch = getProteinMatch(row);

        switch (Column.values()[col]) {
            case PROTEIN_ID:
                return proteinMatch.getId();
            case PROTEIN_NAME:
                return proteinMatch.getAccession();
            case PROTEIN_DESCRIPTION:
                return proteinMatch.getDescription();
            case SAMESET_SUBSET:
                if (proteinMatch.getId() == m_typicalProteinMatchId) {
                    return Sameset.getSameset(Sameset.TYPICAL_SAMESET);
                } else if (row < m_sameSetMatches.length) {
                    return Sameset.getSameset(Sameset.SAMESET);
                } else {
                    return Sameset.getSameset(Sameset.SUBSET);
                }
            case PROTEIN_SCORE:
                Float score = Float.valueOf(proteinMatch.getPeptideSet(m_rsmId).getScore());
                return score;
            case PROTEIN_PEPTIDES_COUNT:
                return proteinMatch.getPeptideSet(m_rsmId).getPeptideCount();
            case PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT: 
                try { 
                    return ((Integer)proteinMatch.getPeptideSet(m_rsmId).getSerializedPropertiesAsMap().get("unique_sequence_count"));
                } catch (Exception e) { return null;}
            case PROTEIN_MASS:
                DBioSequence bioSequence = proteinMatch.getDBioSequence();
                if (bioSequence != null) {
                    return new Float(bioSequence.getMass());
                }
                return null;
        }
        return null; // should never happen
    }

    public void setData(long rsmId, long typicalProteinMatchId, DProteinMatch[] sameSetMatches, DProteinMatch[] subSetMatches) {
        m_rsmId = rsmId;
        m_typicalProteinMatchId = typicalProteinMatchId;
        m_sameSetMatches = sameSetMatches;
        m_subSetMatches = subSetMatches;

        if (m_restrainIds != null) {
            m_restrainIds = null;
            m_filteringAsked = true;
        }
        
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }


    }

    public int findRowToSelect(String searchedText) {

        //JPM.TODO ?

        int rowToBeSelected = 0;
        if ((searchedText != null) && (searchedText.length() > 0)) {

            String searchedTextUpper = searchedText.toUpperCase();

            for (int row = 0; row < getRowCount(); row++) {
                String proteinName = ((String) getValueAt(row, Column.PROTEIN_NAME.ordinal())).toUpperCase();
                if (proteinName.indexOf(searchedTextUpper) != -1) {
                    rowToBeSelected = row;
                }
            }
        }

        return rowToBeSelected;
    }

    @Override
    public void filter() {

        if (m_sameSetMatches == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_sameSetMatches.length + m_subSetMatches.length;
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

        switch (Column.values()[col]) {
            case PROTEIN_NAME: {
                return ((StringFilter) filter).filter((String) data);
            }
            case PROTEIN_DESCRIPTION: {
                return ((StringFilter) filter).filter((String) data);
            }
            case PROTEIN_SCORE:
            case PROTEIN_MASS: {
                return ((DoubleFilter) filter).filter((Float) data);
            }
            case PROTEIN_PEPTIDES_COUNT: {
                return ((IntegerFilter) filter).filter((Integer) data);
            }

        }

        return true; // should never happen
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[Column.PROTEIN_NAME.ordinal()] = null;
            m_filters[Column.PROTEIN_NAME.ordinal()] = new StringFilter(getColumnName(Column.PROTEIN_NAME.ordinal()));
            m_filters[Column.PROTEIN_DESCRIPTION.ordinal()] = new StringFilter(getColumnName(Column.PROTEIN_DESCRIPTION.ordinal()));
            m_filters[Column.SAMESET_SUBSET.ordinal()] = null; //JPM.TODO
            m_filters[Column.PROTEIN_SCORE.ordinal()] = new DoubleFilter(getColumnName(Column.PROTEIN_SCORE.ordinal()));
            m_filters[Column.PROTEIN_PEPTIDES_COUNT.ordinal()] = new IntegerFilter(getColumnName(Column.PROTEIN_PEPTIDES_COUNT.ordinal()));
            m_filters[Column.PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT.ordinal()] = new IntegerFilter(getColumnName(Column.PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT.ordinal()));            
            m_filters[Column.PROTEIN_MASS.ordinal()] = new DoubleFilter(getColumnName(Column.PROTEIN_MASS.ordinal()));
        }
    }

    @Override
    public int getLoadingPercentage() {
        return m_progressInterface.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_progressInterface.isLoaded();
    }
    
        @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        
        if (Column.values()[columnIndex].equals(Column.SAMESET_SUBSET)) {
            return String.class;
        }

        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (Column.values()[columnIndex].equals(Column.SAMESET_SUBSET)) {
           return ((Sameset) getValueAt(rowIndex, columnIndex)).toString();
        }
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { Column.PROTEIN_NAME.m_colId, Column.PROTEIN_ID.m_colId };
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
        return Column.PROTEIN_NAME.m_colId;
    }
    
    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }
    
    @Override
    public PlotInformation getPlotInformation() {
        return null;
    }
    
    
    public static class Sameset {
        
        private static final int TYPICAL_SAMESET  = 0;
        private static final int SAMESET  = 1;
        private static final int SUBSET  = 2;

        private int m_type;
        
        private static Sameset[] samesetArray = { new Sameset(TYPICAL_SAMESET), new Sameset(SAMESET), new Sameset(SUBSET) };
        
        private Sameset(int type) {
            m_type = type;
        }
        
        public static Sameset getSameset(int type) {
            return samesetArray[type];
        }
        
        public boolean isTypical() {
            return (m_type == TYPICAL_SAMESET);
        }
        
        public boolean isSameset() {
            return (m_type == SAMESET);
        }
        
        @Override
        public String toString() {
            if (isTypical()) {
                return "typical";
            } else if (isSameset()) {
                return "sameset";
            } else {
                return "subset";
            }
        }
    }
}
