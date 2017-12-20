package fr.proline.studio.python.model;

import fr.proline.studio.filter.FilterTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;

/**
 * Abstract Model to do a join/diff between two tables by joining two columns which correspond to a key.
 * @author JM235353
 */
public class QuantiFilterModel extends FilterTableModel   {

    public static final int WHOLE_GROUPS = 0;
    public static final int EVERY_GROUP = 1;
    public static final int AT_LEAST_ONE_GROUP = 2;
    
    private final int[] m_colsIndex;
    private final int[] m_groupIndex;
    private final int m_option;
    private final int m_threshold;
    private final boolean m_reversed;
    
    public QuantiFilterModel(GlobalTableModelInterface tableModelSource, int[] colsIndex, int[] groupIndex, int option, int threshold, boolean reversed) {
        super(tableModelSource);
        
        m_colsIndex = colsIndex;
        m_groupIndex = groupIndex;
        m_option = option;
        m_threshold = threshold;
        m_reversed = reversed;
    }

    
    @Override
    public boolean filter(int row) {
        if (m_reversed) {
            return !filterImpl(row);
        } else {
            return filterImpl(row);
        }
    }

    private boolean filterImpl(int row) {
        
        if (m_option == WHOLE_GROUPS) {
            int count = 0;
            for (int i=0;i<m_colsIndex.length;i++) {
                double quantitation = ((Number) getDataValueAt(row, m_colsIndex[i])).doubleValue();
                if (quantitation > 10e-10) {
                    // positive value;
                    count++;
                }
            }
            return (count>=m_threshold);
        }
        
        if (m_option == EVERY_GROUP) {
            int prevGroup = -1;
            int count = 0;
            for (int i=0;i<m_groupIndex.length;i++) {
                int groupCur = m_groupIndex[i];
                if ((prevGroup!=-1) && (groupCur!=prevGroup)) {
                    if (count<m_threshold) {
                        return false;
                    }
                }
                prevGroup = groupCur;
                double quantitation = ((Number) getDataValueAt(row, m_colsIndex[i])).doubleValue();
                if (quantitation > 10e-10) {
                    // positive value;
                    count++;
                }
            }
            return true;
        }
        
        if (m_option == AT_LEAST_ONE_GROUP) {
            int prevGroup = -1;
            int count = 0;
            for (int i=0;i<m_groupIndex.length;i++) {
                int groupCur = m_groupIndex[i];
                if ((prevGroup!=-1) && (groupCur!=prevGroup)) {
                    if (count>=m_threshold) {
                        break;
                    } else {
                        count = 0;
                    }
                }
                prevGroup = groupCur;
                double quantitation = ((Number) getDataValueAt(row, m_colsIndex[i])).doubleValue();
                if (quantitation > 10e-10) {
                    // positive value;
                    count++;
                }
            }
            return (count>=m_threshold);
        }
        
        return true; // should never happen

    }


}
