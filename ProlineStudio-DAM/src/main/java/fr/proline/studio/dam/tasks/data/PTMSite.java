/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.PtmSpecificity.PtmLocation;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author CB205360
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PTMSite {

    //
    // Variables defined in Proline OM that will be read out from the database
    //
    public Long proteinMatchId;
    public Long ptmDefinitionId;
    public Integer seqPosition;
    public Long bestPeptideMatchId;
    public Float localizationConfidence;
    // List of peptides ID matching this PTMSite by the position of this PTM in peptide Seq
    public Map<Integer, List<Long>> peptideIdsByPtmPosition;
    //List of all PeptidesInstances Ids (in leaf RSM)
    public Long[] peptideInstanceIds;
    //List of PepidesInstances which don't match this PTM but with same mass
    public Long[] isomericPeptideInstanceIds;

    //
    // Additional information build by DAM task
    //
    @JsonIgnore private DProteinMatch m_proteinMatch;
    @JsonIgnore private DPeptideMatch m_bestPeptideMatch;
    @JsonIgnore private DInfoPTM m_ptmSpecificity;
    @JsonIgnore private Map<Long, Integer> m_ptmPositionByPeptideId;
    @JsonIgnore private Map<Long, List<DPeptideInstance>> m_leafPeptideInstancesByPeptideId;
    @JsonIgnore private List<DPeptideInstance> m_parentPeptideInstances;
    @JsonIgnore private DMasterQuantProteinSet m_masterQuantProteinSet;
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (m_proteinMatch != null) {
            builder.append(m_proteinMatch.getAccession()).append("-");
        } else {
            builder.append("Protein Match ID").append(proteinMatchId).append("-");
        }
        if (m_ptmSpecificity != null) {
            builder.append(m_ptmSpecificity.getPtmShortName()).append("(");
            if (m_ptmSpecificity.getLocationSpecificity().equals("Anywhere")) {
                builder.append(m_ptmSpecificity.getRresidueAASpecificity());
            } else {
                builder.append(m_ptmSpecificity.getLocationSpecificity());
            }
            builder.append(")-");
        } else {
            builder.append("PTM ID").append(ptmDefinitionId).append("-");
        }
        builder.append(seqPosition);
        return builder.toString();
    }

    public void setProteinMatch(DProteinMatch proteinMatch) {
        this.m_proteinMatch = proteinMatch;
    }

    public void setBestPeptideMatch(DPeptideMatch peptideMatch) {
        m_bestPeptideMatch = peptideMatch;
    }

    public void setPTMSpcificity(DInfoPTM specificity) {
        m_ptmSpecificity = specificity;
    }

    @JsonIgnore
    public void seLeafPeptideInstances(List<DPeptideInstance> peptideInstances) {
         m_leafPeptideInstancesByPeptideId = peptideInstances.stream().collect(Collectors.groupingBy(pi -> pi.getPeptideId()));
    }

    public List<DPeptideInstance> getLeafPeptideInstances(Long peptideId) {
        return m_leafPeptideInstancesByPeptideId.get(peptideId);
    }
        
    public DProteinMatch getProteinMatch() {
        return m_proteinMatch;
    }

    public DPeptideMatch getBestPeptideMatch() {
        return m_bestPeptideMatch;
    }
    
    @JsonIgnore
    public void setParentPeptideInstances(List<DPeptideInstance> parentPeptideInstances) {
        m_parentPeptideInstances = parentPeptideInstances;
    }

    public List<DPeptideInstance> getParentPeptideInstances() {
        return m_parentPeptideInstances;
    }
    
    public DInfoPTM getPtmSpecificity() {
        return m_ptmSpecificity;
    }

    public Integer getPeptideCount() {
        return getPtmPositionByPeptideId().keySet().size();
    }

    public boolean isLoaded() {
        return (m_leafPeptideInstancesByPeptideId != null);
    }

    public DPeptideMatch getBestPeptideMatchForPeptide(Long peptideId) {
        
        List<DPeptideMatch> pepMatches = m_leafPeptideInstancesByPeptideId.get(peptideId).stream().flatMap( pi -> pi.getPeptideMatches().stream()).collect(Collectors.toList());
        
        final Float bestProba[] = new Float[1];
        bestProba[0] = 0.00f;
        final DPeptideMatch[] bestPM = new DPeptideMatch[1];
        pepMatches.sort(Comparator.comparing(DPeptideMatch::getScore).reversed());

        pepMatches.forEach((DPeptideMatch pepMatch) -> {
            Float proba = pepMatch.getPtmSiteProperties().getMascotProbabilityBySite().get(toReadablePtmString(pepMatch.getPeptide().getId()));
            // VDS Workaround test for issue #16643                      
            if (proba == null) {
                proba = pepMatch.getPtmSiteProperties().getMascotProbabilityBySite().get(toOtherReadablePtmString(pepMatch.getPeptide().getId()));
            }
            //END VDS Workaround

            if (proba > bestProba[0]) {
                bestPM[0] = pepMatch;
                bestProba[0] = proba;
            }
        });
        
        return bestPM[0];
    }

    private Map<Long, Integer> getPtmPositionByPeptideId() {
        if (m_ptmPositionByPeptideId == null) {
            m_ptmPositionByPeptideId = new HashMap<>();
            for (Map.Entry<Integer, List<Long>> entry : peptideIdsByPtmPosition.entrySet()) {
                for (Long pepId : entry.getValue()) {
                    m_ptmPositionByPeptideId.put(pepId, entry.getKey());
                }
            }
        }
        return m_ptmPositionByPeptideId;
    }

    public String toReadablePtmString(Long peptideId) {
        StringBuilder builder = new StringBuilder(m_ptmSpecificity.getPtmShortName());
        builder.append(" (");
        if (m_ptmSpecificity.getLocationSpecificity().equals("Anywhere")) {
            builder.append(m_ptmSpecificity.getRresidueAASpecificity()).append(getPtmPositionOnPeptide(peptideId)).append(')');
        } else {
            builder.append(m_ptmSpecificity.getLocationSpecificity()).append(')');
        }
        return builder.toString();
    }

    // VDS Workaround test for issue #16643   
    public String toOtherReadablePtmString(Long peptideId) {
        StringBuilder builder = new StringBuilder(m_ptmSpecificity.getPtmShortName());
        builder.append(" (");
        PtmLocation ptmLoc = PtmLocation.withName(m_ptmSpecificity.getLocationSpecificity());
        if (ptmLoc.equals(PtmLocation.ANYWHERE)) {
            builder.append(m_ptmSpecificity.getRresidueAASpecificity()).append(getPtmPositionOnPeptide(peptideId)).append(')');
        } else {
            switch (ptmLoc) {
                case ANY_C_TERM:
                    builder.append(PtmLocation.PROT_C_TERM.toString()).append(')');
                    break;
                case PROT_C_TERM:
                    builder.append(PtmLocation.ANY_C_TERM.toString()).append(')');
                    break;
                case ANY_N_TERM:
                    builder.append(PtmLocation.PROT_N_TERM.toString()).append(')');
                    break;
                case PROT_N_TERM:
                    builder.append(PtmLocation.ANY_N_TERM.toString()).append(')');
                    break;
            }
        }
        return builder.toString();
    }

    public Integer getPtmPositionOnPeptide(Long peptideId) {
        return getPtmPositionByPeptideId().get(peptideId);
    }

    void setQuantProteinSet(DMasterQuantProteinSet mqps) {
        m_masterQuantProteinSet = mqps;
    }

    public DMasterQuantProteinSet getMasterQuantProteinSet() {
        return m_masterQuantProteinSet;
    }
    
}
