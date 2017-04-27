/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data;

import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.ps.PtmSpecificity.PtmLocation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CB205360
 */
public class PTMSite {
    
    public Long proteinMatchId;
    public Long definitionId; //PTM Definition ID
    public Integer seqPosition;
    public Long bestPeptideMatchId;
    // List of peptides ID matching this PTMSite by the position of this PTM 
    // in peptide Seq
    public Map<Integer, List<Long>> peptideIdsByPtmPosition;
    
    //List of all PeptidesInstances Ids (in leaf RSM)
    public Long[] peptideInstanceIds;
        
    //List of PepidesInstances which don't match this PTM but with same mass
    public Long[] isomericPeptideInstanceIds;

    private DProteinMatch proteinMatch; //PTMSite's Protein Match
    private DPeptideMatch bestPeptideMatch; 
    private DInfoPTM ptmSpecificity;
    private Map<Long, Integer> reversePtmPositionByPeptideId;
    
    private Map<Long, List<DPeptideMatch>> m_peptideMatchesByPepInstanceID;
    private boolean m_peptideMatchesLoaded = false;
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        if(proteinMatch != null)
            builder.append(proteinMatch.getAccession()).append("-");
        else 
            builder.append("Protein Match ID").append(proteinMatchId).append("-");
        if(ptmSpecificity != null) {
            builder.append(ptmSpecificity.getPtmShortName()).append("(");
            if (ptmSpecificity.getLocationSpecificity().equals("Anywhere")) {
                builder.append(ptmSpecificity.getRresidueAASpecificity());
            } else {
                builder.append(ptmSpecificity.getLocationSpecificity());
            }       
            builder.append(")-");
        } else 
            builder.append("PTM ID").append(definitionId).append("-");
         builder.append(seqPosition);
        return builder.toString();
    }
    
    public void setProteinMatch(DProteinMatch proteinMatch) {
        this.proteinMatch = proteinMatch;
    }

    public void setBestPeptideMatch(DPeptideMatch peptideMatch) {
        this.bestPeptideMatch = peptideMatch;
    }

    public void setPTMSpcificity(DInfoPTM specificity) {
        this.ptmSpecificity = specificity;
    }
    
    public void setPepInstancePepMatchesMap(Map<Long, List<DPeptideMatch>> pepMatchesByPepInstanceID) {
        this.m_peptideMatchesByPepInstanceID = pepMatchesByPepInstanceID;
        m_peptideMatchesLoaded = true;
    }

    public DProteinMatch getProteinMatch() {
        return proteinMatch;
    }

    public DPeptideMatch getBestPeptideMatch() {
        return bestPeptideMatch;
    }

    public Map<Long, List<DPeptideMatch>> getPepInstancePepMatchesMap() {
        return m_peptideMatchesByPepInstanceID;
    }
     
    public DInfoPTM getPtmSpecificity() {
        return ptmSpecificity;
    }

    public Integer getPeptideCount() {
        return getReversePtmPositionByPeptideId().keySet().size();
    }

    public boolean isAllPeptideMatchesLoaded(){
        return m_peptideMatchesLoaded;
    }
    
    public void setAllPeptideMatchesLoaded(boolean arePepMatchesLoaded){
        m_peptideMatchesLoaded = arePepMatchesLoaded;
    }
    
    public Map<Long, Integer> getReversePtmPositionByPeptideId() {
        if (reversePtmPositionByPeptideId == null) {
            reversePtmPositionByPeptideId = new HashMap<>();
            for (Map.Entry<Integer, List<Long>> entry : peptideIdsByPtmPosition.entrySet()) {
                for (Long pepId : entry.getValue()) {
                    reversePtmPositionByPeptideId.put(pepId, entry.getKey());
                }
            }
        }
        return reversePtmPositionByPeptideId;
    }

    
    
    public String toReadablePtmString(Long peptideId) {
        StringBuilder builder = new StringBuilder(ptmSpecificity.getPtmShortName());
        builder.append(" (");
        if (ptmSpecificity.getLocationSpecificity().equals("Anywhere")) {
            builder.append(ptmSpecificity.getRresidueAASpecificity()).append(getPtmPeptidePosition(peptideId)).append(')');
        } else {
            builder.append(ptmSpecificity.getLocationSpecificity()).append(')');
        }
        return builder.toString();
    }
    
    // VDS Workaround test for issue #16643   
    public String toOtherReadablePtmString(Long peptideId) {
        StringBuilder builder = new StringBuilder(ptmSpecificity.getPtmShortName());
        builder.append(" (");
        PtmLocation ptmLoc = PtmLocation.withName(ptmSpecificity.getLocationSpecificity());
        if (ptmLoc.equals(PtmLocation.ANYWHERE)) {
            builder.append(ptmSpecificity.getRresidueAASpecificity()).append(getPtmPeptidePosition(peptideId)).append(')');
        } else {
            switch(ptmLoc) {
                case ANY_C_TERM :
                    builder.append(PtmLocation.PROT_C_TERM.toString()).append(')');
                    break;                    
                case PROT_C_TERM :
                    builder.append(PtmLocation.ANY_C_TERM.toString()).append(')');
                    break;
                case ANY_N_TERM :
                    builder.append(PtmLocation.PROT_N_TERM.toString()).append(')');
                    break;
                case PROT_N_TERM :
                    builder.append(PtmLocation.ANY_N_TERM.toString()).append(')');                    
                    break;                    
            }
        }
        return builder.toString();
    } 
            
    public Integer getPtmPeptidePosition(Long peptideId) {
        return getReversePtmPositionByPeptideId().get(peptideId);
    }
    
}
