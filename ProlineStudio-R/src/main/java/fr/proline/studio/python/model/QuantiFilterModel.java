package fr.proline.studio.python.model;

import fr.proline.studio.filter.FilterTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;

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
    
    public QuantiFilterModel(GlobalTableModelInterface tableModelSource, int[] colsIndex, int[] groupIndex, int option, int threshold) {
        super(tableModelSource);
        
        m_colsIndex = colsIndex;
        m_groupIndex = groupIndex;
        m_option = option;
        m_threshold = threshold;
    }

    @Override
    public boolean filter(int row) {
        
        if (m_option == WHOLE_GROUPS) {
            int count = 0;
            for (int i=0;i<m_colsIndex.length;i++) {
                count += ((Number) getDataValueAt(row, m_colsIndex[i])).intValue();
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
                count += ((Number) getDataValueAt(row, m_colsIndex[i])).intValue();
            }
            return true;
        }
        
        if (m_option == AT_LEAST_ONE_GROUP) {
            int prevGroup = -1;
            int count = 0;
            boolean trueForOneGroup = false;
            for (int i=0;i<m_groupIndex.length;i++) {
                int groupCur = m_groupIndex[i];
                if ((prevGroup!=-1) && (groupCur!=prevGroup)) {
                    if (count>=m_threshold) {
                        trueForOneGroup = true;
                        break;
                    }
                }
                prevGroup = groupCur;
                count += ((Number) getDataValueAt(row, m_colsIndex[i])).intValue();
            }
            return trueForOneGroup;
        }
        
        return true; // should never happen

    }


}
