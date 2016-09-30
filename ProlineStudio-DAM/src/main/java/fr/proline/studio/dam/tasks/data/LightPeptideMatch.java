package fr.proline.studio.dam.tasks.data;

/**
 *
 * @author JM235353
 */
public class LightPeptideMatch {
    private final long m_id;
    private final Float m_score;

    private String m_sequence;
    
    private LightProteinMatch[] m_proteinMatchArray = null;


    private Integer m_cdPrettyRank;

    public LightPeptideMatch(long id, Float score, Integer cdPrettyRank, String sequence) {
        m_id = id;
        m_score = score;
        m_proteinMatchArray = null;
        
        m_cdPrettyRank = cdPrettyRank;
        m_sequence = sequence;
    }
    
    public long getId() {
        return m_id;
    }
    
    public void setSequence(String sequence) {
    	m_sequence = sequence;
    }
    
    public String getSequence() {
        return m_sequence;
    }

    public Float getScore() {
        return m_score;
    }

    public Integer getCDPrettyRank() {
    	return m_cdPrettyRank;
    }

    public LightProteinMatch[] getProteinMatches() {
        return m_proteinMatchArray;
    }

    public void setProteinMatches(LightProteinMatch[] proteinMatchArray) {
        m_proteinMatchArray = proteinMatchArray;
    }

}
