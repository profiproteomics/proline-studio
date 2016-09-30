package fr.proline.studio.dam.tasks.data;

public class LightProteinMatch {

    private long m_id;
    private String m_accession;
    private final Float m_score;

    private Long m_proteinSetId = null;
    
    private LightPeptideMatch[] m_peptideMatches;
    private long[] m_peptideMatchesId;
    
    public LightProteinMatch(long id, String accession, Float score, Long proteinSetId) {
        m_id = id;
        m_accession = accession;
        m_score = score;
        m_proteinSetId = proteinSetId;
    }

    public Long getProteinSetId() {
        return m_proteinSetId;
    }

    public long getId() {
        return m_id;
    }

    public void setId(final long pId) {
        m_id = pId;
    }

    public String getAccession() {
        return m_accession;
    }

    public void setAccession(String accession) {
        m_accession = accession;
    }

    public Float getScore() {
        return m_score;
    }

    public LightPeptideMatch[] getPeptideMatches() {
        return m_peptideMatches;
    }

    public void setPeptideMatches(LightPeptideMatch[] peptideMatches) {
        m_peptideMatches = peptideMatches;
    }
    
    public long[] getPeptideMatchesId() {
        return m_peptideMatchesId;
    }

    public void setPeptideMatchesId(long[] peptideMatchesId) {
        m_peptideMatchesId = peptideMatchesId;
    }

}
