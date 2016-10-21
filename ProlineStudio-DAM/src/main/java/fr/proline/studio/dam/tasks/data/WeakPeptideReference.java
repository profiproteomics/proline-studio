package fr.proline.studio.dam.tasks.data;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author JM235353
 */
public class WeakPeptideReference {
    
    private int m_peptideIndex;
    private Set<Long> m_proteinSetIds = new HashSet<>();
    
    public WeakPeptideReference(int peptideIndex, long proteinSet1, long proteinSet2) {
        m_peptideIndex = peptideIndex;
        m_proteinSetIds.add(proteinSet1);
        m_proteinSetIds.add(proteinSet2);
    }
    
    public void add(long proteinSet1, long proteinSet2) {
        m_proteinSetIds.add(proteinSet1);
        m_proteinSetIds.add(proteinSet2);
    }
    
    public int getPeptideIndex() {
        return m_peptideIndex;
    }
    
    public boolean correspondToProteinSet(long proteinSetId) {
        return m_proteinSetIds.contains(proteinSetId);
    }
}
