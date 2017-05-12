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
import fr.proline.core.orm.ps.PtmSpecificity.PtmLocation;
import java.util.ArrayList;
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
    @JsonIgnore private DProteinMatch proteinMatch;
    @JsonIgnore private DPeptideMatch bestPeptideMatch;
    @JsonIgnore private DInfoPTM ptmSpecificity;
    @JsonIgnore private Map<Long, Integer> reversePtmPositionByPeptideId;
    @JsonIgnore private Map<DPeptideInstance, List<DPeptideMatch>> m_peptideMatchesByPepInstance;
    @JsonIgnore private Map<Long, List<Map.Entry<DPeptideInstance, List<DPeptideMatch>>>> m_peptideMatchesByPeptideId;
    @JsonIgnore private List<DPeptideInstance> m_parentPeptideInstances;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (proteinMatch != null) {
            builder.append(proteinMatch.getAccession()).append("-");
        } else {
            builder.append("Protein Match ID").append(proteinMatchId).append("-");
        }
        if (ptmSpecificity != null) {
            builder.append(ptmSpecificity.getPtmShortName()).append("(");
            if (ptmSpecificity.getLocationSpecificity().equals("Anywhere")) {
                builder.append(ptmSpecificity.getRresidueAASpecificity());
            } else {
                builder.append(ptmSpecificity.getLocationSpecificity());
            }
            builder.append(")-");
        } else {
            builder.append("PTM ID").append(ptmDefinitionId).append("-");
        }
        builder.append(seqPosition);
        return builder.toString();
    }

    public void setProteinMatch(DProteinMatch proteinMatch) {
        this.proteinMatch = proteinMatch;
    }

    public void setBestPeptideMatch(DPeptideMatch peptideMatch) {
        bestPeptideMatch = peptideMatch;
    }

    public void setPTMSpcificity(DInfoPTM specificity) {
        ptmSpecificity = specificity;
    }

    @JsonIgnore
    public void setPepInstancePepMatchesMap(Map<DPeptideInstance, List<DPeptideMatch>> pepMatchesByPepInstance) {
        m_peptideMatchesByPepInstance = pepMatchesByPepInstance;
        m_peptideMatchesByPeptideId = new HashMap<>();
        for (Map.Entry<DPeptideInstance, List<DPeptideMatch>> entry : m_peptideMatchesByPepInstance.entrySet()) {
            if (!m_peptideMatchesByPeptideId.containsKey(entry.getKey().getPeptideId())) {
               m_peptideMatchesByPeptideId.put(entry.getKey().getPeptideId(), new ArrayList<>());
            }
            m_peptideMatchesByPeptideId.get(entry.getKey().getPeptideId()).add(entry);
        }
    }

    public List<Map.Entry<DPeptideInstance, List<DPeptideMatch>>> getPeptideMatchesForPeptide(Long peptideId) {
        return m_peptideMatchesByPeptideId.get(peptideId);
    }
    
    public DProteinMatch getProteinMatch() {
        return proteinMatch;
    }

    public DPeptideMatch getBestPeptideMatch() {
        return bestPeptideMatch;
    }
    
    @JsonIgnore
    public void setParentPeptideInstances(List<DPeptideInstance> parentPeptideInstances) {
        m_parentPeptideInstances = parentPeptideInstances;
    }

    public List<DPeptideInstance> getParentPeptideInstances() {
        return m_parentPeptideInstances;
    }
    
    public DInfoPTM getPtmSpecificity() {
        return ptmSpecificity;
    }

    public Integer getPeptideCount() {
        return getReversePtmPositionByPeptideId().keySet().size();
    }

    public boolean isAllPeptideMatchesLoaded() {
        return (m_peptideMatchesByPepInstance != null);
    }

    public DPeptideMatch getBestPeptideMatchForPeptide(Long peptideId) {
        
        List<DPeptideMatch> pepMatches = m_peptideMatchesByPeptideId.get(peptideId).stream().map( entry -> entry.getValue()).flatMap(list -> list.stream()).collect(Collectors.toList());
        
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

    public Integer getPtmPeptidePosition(Long peptideId) {
        return getReversePtmPositionByPeptideId().get(peptideId);
    }

}
