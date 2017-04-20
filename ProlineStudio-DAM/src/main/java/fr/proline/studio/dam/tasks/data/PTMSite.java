/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data;

import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CB205360
 */
public class PTMSite {
    
    public Long proteinMatchId;
    public Long definitionId;
    public Integer seqPosition;
    public Long bestPeptideMatchId;
    public Map<Integer, List<Long>> peptideIdsByPtmPosition;
    public Long[] peptideInstanceIds;
    public Long[] isomericPeptideInstanceIds;

    private DProteinMatch proteinMatch;
    private DPeptideMatch bestPeptideMatch;
    private DInfoPTM ptmSpecificity;
    private Map<Long, Integer> reversePtmPositionByPeptideId;
    
    public void setProteinMatch(DProteinMatch proteinMatch) {
        this.proteinMatch = proteinMatch;
    }

    public void setBestPeptideMatch(DPeptideMatch peptideMatch) {
        this.bestPeptideMatch = peptideMatch;
    }

    public void setPTMSpcificity(DInfoPTM specificity) {
        this.ptmSpecificity = specificity;
    }

    public DProteinMatch getProteinMatch() {
        return proteinMatch;
    }

    public DPeptideMatch getBestPeptideMatch() {
        return bestPeptideMatch;
    }

    public DInfoPTM getPtmSpecificity() {
        return ptmSpecificity;
    }

    public Integer getPeptideCount() {
        return getReversePtmPositionByPeptideId().keySet().size();
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
    
    public Integer getPtmPeptidePosition(Long peptideId) {
        return getReversePtmPositionByPeptideId().get(peptideId);
    }
    
}
