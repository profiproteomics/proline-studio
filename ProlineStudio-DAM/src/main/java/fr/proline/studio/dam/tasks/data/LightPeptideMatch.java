package fr.proline.studio.dam.tasks.data;

//import fr.proline.core.orm.msi.Peptide;
//import fr.proline.core.orm.msi.SequenceMatch;

/**
 *
 * @author JM235353
 */
public class LightPeptideMatch /*implements Comparable<DPeptideMatch>*/ {
    private long m_id;
    //private Integer m_rank;
    //private int m_charge;
    //private Float m_deltaMoz;
    //private double m_experimentalMoz;
    //private int m_missedCleavage;
    private Float m_score;
    //private Peptide m_peptide;
    //private long m_resultSetId;
    
    //private DMsQuery m_msQuery;
    //private boolean m_msQuerySet;
    
    //private SequenceMatch m_sequenceMatch;
    
    private LightProteinMatch[] m_proteinMatchArray = null;
    private String m_proteinSetStringList = null;

    private Integer m_cdPrettyRank;
    //private Integer m_sdPrettyRank;  
    

    
    public LightPeptideMatch(long id, /*Integer rank, int charge, Float deltaMoz, double experimentalMoz, int missedCleavage,*/ Float score, /*long resultSetId,*/ Integer cdPrettyRank /*, Integer sdPrettyRank*/) {
        m_id = id;
        /*m_rank = rank;
        m_charge = charge;
        m_deltaMoz = deltaMoz;
        m_experimentalMoz = experimentalMoz;
        m_missedCleavage = missedCleavage;*/
        m_score = score;
        /*m_resultSetId = resultSetId;
        
        //m_peptide = null;
        //m_msQuery = null;
        m_msQuerySet = false;*/
        //m_sequenceMatch = null;
        m_proteinMatchArray = null;
        
        m_cdPrettyRank = cdPrettyRank;
        //m_sdPrettyRank = sdPrettyRank;
    }
    
    public long getId() {
        return m_id;
    }
    
   /* public void setPeptide(Peptide p) {
    	m_peptide = p;
    }
    
    public Peptide getPeptide() {
        return m_peptide;
    }
    
    public void setSequenceMatch(SequenceMatch sequenceMatch) {
        m_sequenceMatch = sequenceMatch;
    }
    
    public SequenceMatch getSequenceMatch() {
        return m_sequenceMatch;
    }*/
    
    public Float getScore() {
        return m_score;
    }
    
    /*public Integer getRank() {
    	return m_rank;
    }*/

    public Integer getCDPrettyRank() {
    	return m_cdPrettyRank;
    }

    /*public Integer getSDPrettyRank() {
    	return m_sdPrettyRank;
    }
    
    public int getCharge() {
        return m_charge;
    }
    
    public double getExperimentalMoz() {
        return m_experimentalMoz;
    }
    
    public Float getDeltaMoz() {
        return m_deltaMoz;
    }
    
    public int getMissedCleavage() {
        return m_missedCleavage;
    }
    
    public long getResultSetId() {
        return m_resultSetId;
    }*/
    
    /*public void setMsQuery(DMsQuery msQuery) {
        m_msQuery = msQuery;
        m_msQuerySet = true;
    }
    
    public DMsQuery getMsQuery() {
        return m_msQuery;
    } */ 
    
    /*public boolean isMsQuerySet() {
        return m_msQuerySet;
    }*/
    
    public void setProteinSetStringList(String s) {
        m_proteinSetStringList = s;
    }

    public String getProteinSetStringList() {
        return m_proteinSetStringList;
    }

    public LightProteinMatch[] getProteinMatches() {
        return m_proteinMatchArray;
    }

    public void setProteinMatches(LightProteinMatch[] proteinMatchArray) {
        m_proteinMatchArray = proteinMatchArray;
    }

    /*
	@Override
    public int compareTo(LightPeptideMatch peptideMatch) {
		if (m_peptide == null) {
			return 0;
		}
		if (peptideMatch.m_peptide == null) {
			return 0;
		}
		return m_peptide.compareTo(peptideMatch.m_peptide);
    }*/

}
