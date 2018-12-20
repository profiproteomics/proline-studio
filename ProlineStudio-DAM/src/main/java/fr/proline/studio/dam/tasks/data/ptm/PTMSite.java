/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author CB205360
 */
public class PTMSite {

    private JSONPTMSite m_site;
    private DInfoPTM m_ptmSpecificity;

    private DProteinMatch m_proteinMatch;
    private DMasterQuantProteinSet m_masterQuantProteinSet;

    private DPeptideMatch m_bestPeptideMatch;

    private Map<Long, PTMSitePeptideInstance> m_ptmSitePeptideInstanceByPepId;
    private Map<Long, Integer> m_ptmPositionByPeptideId;
    private List<DPeptideInstance> m_parentPeptideInstances;

    private PTMDataset m_dataset;

    public PTMSite(JSONPTMSite jsonSite) {
        m_site = jsonSite;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (m_proteinMatch != null) {
            builder.append(m_proteinMatch.getAccession()).append("-");
        } else {
            builder.append("Protein Match ID").append(m_site.proteinMatchId).append("-");
        }
        if (m_ptmSpecificity != null) {
            builder.append(m_ptmSpecificity.getPtmShortName()).append("(");
            if (m_ptmSpecificity.getLocationSpecificity().equals("Anywhere")) {
                builder.append(m_ptmSpecificity.getResidueAASpecificity());
            } else {
                builder.append(m_ptmSpecificity.getLocationSpecificity());
            }
            builder.append(")-");
        } else {
            builder.append("PTM ID").append(m_site.ptmDefinitionId).append("-");
        }
        builder.append(m_site.seqPosition);
        return builder.toString();
    }

    public void setProteinMatch(DProteinMatch proteinMatch) {
        this.m_proteinMatch = proteinMatch;
    }

    public DProteinMatch getProteinMatch() {
        return m_proteinMatch;
    }

    public void setBestPeptideMatch(DPeptideMatch peptideMatch) {
        m_bestPeptideMatch = peptideMatch;
    }

    public DPeptideMatch getBestPeptideMatch() {
        return m_bestPeptideMatch;
    }

    public void setPTMSpecificity(DInfoPTM specificity) {
        m_ptmSpecificity = specificity;
    }

    public DInfoPTM getPTMSpecificity() {
        return m_ptmSpecificity;
    }

    public void setPeptideInstances(List<DPeptideInstance> parentPeptideInstances, List<DPeptideInstance> leafInstances) {
        m_parentPeptideInstances = parentPeptideInstances;
        m_ptmSitePeptideInstanceByPepId = new HashMap<>();
        Map<Long, List<DPeptideInstance>> leafPepInstanceByPepId = leafInstances.stream().collect(Collectors.groupingBy(pi -> pi.getPeptideId()));
        for (DPeptideInstance parentPeptideInstance : parentPeptideInstances) {
            PTMPeptideInstance ptmPeptide = m_dataset.getPTMPeptideInstance(m_site.proteinMatchId, parentPeptideInstance);
            Long peptideId = parentPeptideInstance.getPeptideId();
            ptmPeptide.setStartPosition(getPositionOnProtein() - getPositionOnPeptide(peptideId));
            ptmPeptide.addPTMSite(this);
            List<DPeptideInstance> leafPeptideInstances = leafPepInstanceByPepId.get(peptideId);
            PTMSitePeptideInstance ptmSitePeptideInstance = new PTMSitePeptideInstance(this, ptmPeptide, leafPeptideInstances, getBestPeptideMatch(leafPeptideInstances));
            m_ptmSitePeptideInstanceByPepId.put(peptideId, ptmSitePeptideInstance);
        }

    }

    public List<DPeptideInstance> getParentPeptideInstances() {
        return m_parentPeptideInstances;
    }

    public void setQuantProteinSet(DMasterQuantProteinSet mqps) {
        m_masterQuantProteinSet = mqps;
    }

    public PTMSitePeptideInstance getPTMSitePeptideInstance(Long peptideId) {
        return m_ptmSitePeptideInstanceByPepId.get(peptideId);
    }

    public Integer getPeptideCount() {
        return getPtmPositionByPeptideId().keySet().size();
    }

    public boolean isLoaded() {
        return (m_ptmSitePeptideInstanceByPepId != null);
    }

    private DPeptideMatch getBestPeptideMatch(List<DPeptideInstance> peptideInstances) {

        List<DPeptideMatch> pepMatches = peptideInstances.stream().flatMap(pi -> pi.getPeptideMatches().stream()).collect(Collectors.toList());

        final Float bestProba[] = new Float[1];
        bestProba[0] = 0.00f;
        final DPeptideMatch[] bestPM = new DPeptideMatch[1];
        pepMatches.sort(Comparator.comparing(DPeptideMatch::getScore).reversed());

        pepMatches.forEach((DPeptideMatch pepMatch) -> {
            int position = getPositionOnPeptide(pepMatch.getPeptide().getId());
            Float proba = pepMatch.getPtmSiteProperties().getMascotProbabilityBySite().get(m_ptmSpecificity.toReadablePtmString(position));
            // VDS Workaround test for issue #16643
            if (proba == null) {
                proba = pepMatch.getPtmSiteProperties().getMascotProbabilityBySite().get(m_ptmSpecificity.toOtherReadablePtmString(position));
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
            for (Map.Entry<Integer, List<Long>> entry : m_site.peptideIdsByPtmPosition.entrySet()) {
                for (Long pepId : entry.getValue()) {
                    m_ptmPositionByPeptideId.put(pepId, entry.getKey());
                }
            }
        }
        return m_ptmPositionByPeptideId;
    }

    public String toReadablePtmString(Long peptideId) {
        return m_ptmSpecificity.toReadablePtmString(getPositionOnPeptide(peptideId));
    }

    // VDS Workaround test for issue #16643
    public String toOtherReadablePtmString(Long peptideId) {
        return m_ptmSpecificity.toOtherReadablePtmString(getPositionOnPeptide(peptideId));
    }

    public Integer getPositionOnPeptide(Long peptideId) {
        return getPtmPositionByPeptideId().get(peptideId);
    }

    public DMasterQuantProteinSet getMasterQuantProteinSet() {
        return m_masterQuantProteinSet;
    }

    public void setDataset(PTMDataset ptmDataset) {
        m_dataset = ptmDataset;
    }

    public PTMDataset getPTMdataset() {
        return m_dataset;
    }

    public Long[] getPeptideInstanceIds() {
        return m_site.peptideInstanceIds;
    }

    public Integer getPositionOnProtein() {
        return m_site.seqPosition;
    }

    public boolean isProteinNTerm() {
        if (m_ptmSpecificity.getLocationSpecificity().endsWith("N-term") && getPositionOnProtein() == 1) {
            return true;
        } else if (getPositionOnProtein()-getModifLocationInPep()== 1) {
            return true;
        } else {
            return false;
        }
    }

    private int getModifLocationInPep() {
        String locationSpecitifcity = m_ptmSpecificity.getLocationSpecificity();
        if (locationSpecitifcity.contains("N-term")) {
            return 0;
        } else if (locationSpecitifcity.contains("C-term")) {
            return -1;
        }
        return (int) this.getPositionOnPeptide(this.getBestPeptideMatch().getPeptide().getId());
    }

}
