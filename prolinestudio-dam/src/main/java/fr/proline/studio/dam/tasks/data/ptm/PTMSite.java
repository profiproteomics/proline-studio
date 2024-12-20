/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A PTM Site is for one given protein, the modification on a amino acide, so 4
 * keys for describe a PTMSite: protein, position + amino acid + type of
 * modification. In this PTMSite class, for the given modification in this
 * protein, there are a list of peptide which contain this modification.
 *
 * @author CB205360
 */
public class PTMSite {

    private Long m_id;
    private final AbstractJSONPTMSite m_site;
    private DInfoPTM m_ptmSpecificity;

    private DProteinMatch m_proteinMatch;
    private DMasterQuantProteinSet m_masterQuantProteinSet;

    private DPeptideMatch m_bestPeptideMatch;
    /**
     * map of peptideId, peptideInstance
     */
    private Map<Long, PTMSitePeptideInstance> m_ptmSitePeptideInstanceByPepId;
    /**
     * map of peptideId, ptm position
     */
    private Map<Long, Integer> m_ptmPositionByPeptideId;
    private List<DPeptideInstance> m_parentPeptideInstances;
    
    private PTMDataset m_dataset;

    private Optional<Boolean> isNtermSite = Optional.empty();
    private Optional<Boolean> isCtermSite = Optional.empty();

    public PTMSite(AbstractJSONPTMSite jsonSite, DProteinMatch proteinMatch) {
        m_site = jsonSite;
        this.m_proteinMatch = proteinMatch;
        m_id = -1l;
        if(m_site instanceof JSONPTMSite2) {
            m_id = ((JSONPTMSite2) m_site).id;
            isCtermSite = Optional.ofNullable( ((JSONPTMSite2) m_site).isCTerminal);
            isNtermSite =  Optional.ofNullable( ((JSONPTMSite2) m_site).isNTerminal);
        }
    }

    public Long getId(){
        return m_id;
    }

    public AbstractJSONPTMSite getJSONPtmSite(){
        return m_site;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (m_proteinMatch != null) {
            builder.append(m_proteinMatch.getAccession()).append("-");
        } else {
            builder.append("Protein Match ID").append(m_site.proteinMatchId).append("-");
        }
        builder.append(toProteinReadablePtmString());
        return builder.toString();
    }

    public void setProteinMatch(DProteinMatch proteinMatch) {
        this.m_proteinMatch = proteinMatch;
    }

    public DProteinMatch getProteinMatch() {
        return m_proteinMatch;
    }

    public void setBestProbabilityPepMatch(DPeptideMatch peptideMatch) {
        m_bestPeptideMatch = peptideMatch;
    }

    public DPeptideMatch getMostConfidentPepMatch() {
        return m_bestPeptideMatch;
    }

    public Long getBestProbabilityPepMatchId() {
        if (m_bestPeptideMatch != null)
            return m_bestPeptideMatch.getId();
        return m_site.bestPeptideMatchId;
    }

    public void setPTMSpecificity(DInfoPTM specificity) {
        m_ptmSpecificity = specificity;
    }

    public DInfoPTM getPTMSpecificity() {
        return m_ptmSpecificity;
    }
//
//    /**
//     * With leafInstances, we can create a map of leafPepInstanceByPepId
//     * For each peptide found in parentPeptideInstances, we instantiate a PTMPeptideInstance, set it's start postion.
//     * With the peptideId, we can find all of it's leafPeptide, so that a new PTMSitePeptideInstance is create.
//     * at last, we put this PTMSitePeptideInstance in the m_ptmSitePeptideInstanceByPepId.
//     * @param parentPeptideInstances
//     * @param leafInstances
//     */
//    public void setPeptideInstances(List<DPeptideInstance> parentPeptideInstances, List<DPeptideInstance> leafInstances) {
//        m_parentPeptideInstances = parentPeptideInstances;
//        m_ptmSitePeptideInstanceByPepId = new HashMap<>();
//        linkPeptideInstances(parentPeptideInstances, leafInstances);
//    }
    
    public void addPeptideInstances(List<DPeptideInstance> parentPeptideInstances, List<DPeptideInstance> leafInstances) {
        if(m_parentPeptideInstances ==null)
           m_parentPeptideInstances = new ArrayList<>();
        m_parentPeptideInstances.addAll(parentPeptideInstances);
        
        if(m_ptmSitePeptideInstanceByPepId ==null)
            m_ptmSitePeptideInstanceByPepId = new HashMap<>();
        
        linkPeptideInstances(parentPeptideInstances, leafInstances);
    }
    
    private void linkPeptideInstances(List<DPeptideInstance> parentPeptideInstances, List<DPeptideInstance> leafInstances){
        Map<Long, List<DPeptideInstance>> leafPepInstanceByPepId = leafInstances.stream().collect(Collectors.groupingBy(pi -> pi.getPeptideId()));
        for (DPeptideInstance parentPeptideInstance : parentPeptideInstances) {
            PTMPeptideInstance parentPTMPeptideInst = m_dataset.getPTMPeptideInstance(m_proteinMatch, parentPeptideInstance, m_site.seqPosition);
            Long peptideId = parentPeptideInstance.getPeptideId();
            parentPTMPeptideInst.setStartPosition(getPositionOnProtein() - getPositionOnPeptide(peptideId));
            parentPTMPeptideInst.addPTMSite(this);
            List<DPeptideInstance> leafPeptideInstances = leafPepInstanceByPepId.get(peptideId);
            PTMSitePeptideInstance ptmSitePeptideInstance = new PTMSitePeptideInstance(this, parentPTMPeptideInst, leafPeptideInstances, getMostConfidentPepMatch(leafPeptideInstances));
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
    
    public List<PTMSitePeptideInstance> getPTMSitePeptideInstances() {
        return new ArrayList<>(m_ptmSitePeptideInstanceByPepId.values());
    }

    public Integer getPeptideCount() {
        return getPtmPositionByPeptideId().keySet().size();
    }

    public boolean isLoaded() {
        return (m_ptmSitePeptideInstanceByPepId != null);
    }

    private DPeptideMatch getMostConfidentPepMatch(List<DPeptideInstance> peptideInstances) {

        List<DPeptideMatch> pepMatches = peptideInstances.stream().flatMap(pi -> Optional.ofNullable(pi.getPeptideMatches()).map(Collection::stream).orElseGet(Stream::empty)).collect(Collectors.toList());

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
            if (proba != null && proba > bestProba[0]) {
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

    public List<PTMPeptideInstance> getAssociatedPTMPeptideInstances(){
        List<PTMPeptideInstance> res = m_ptmSitePeptideInstanceByPepId.values().stream().map(ptmSitePepInst -> ptmSitePepInst.getParentPTMPeptideInstance()).collect(Collectors.toList());
        return res;
    }
    
    public String toProteinReadablePtmString() {
        StringBuilder builder = new StringBuilder();
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

    public String peptideSpecificReadablePtmString(Long peptideId) {

        Integer pepPosition = getPositionOnPeptide(peptideId);
        StringBuilder builder = new StringBuilder();
        if (m_ptmSpecificity != null) {
            builder.append(m_ptmSpecificity.getPtmShortName()).append("(");
            if (m_ptmSpecificity.getLocationSpecificity().equals("Anywhere")) {
                builder.append(m_ptmSpecificity.getResidueAASpecificity());
            } else {
                builder.append(m_ptmSpecificity.getLocationSpecificity());
            }
            builder.append(pepPosition).append(")");
        } else {
            builder.append("PTM ID").append(m_site.ptmDefinitionId).append("-").append(pepPosition);
        }
        return builder.toString();
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

    public Float getLocalisationConfidence() {
        return m_site.localizationConfidence;
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
        if(JSONPTMSite.class.isInstance(m_site))
            return ((JSONPTMSite)m_site).peptideInstanceIds;
        else {
            return null;
        }
    }
    
    public ArrayList<Long> getPeptideIds(){
        return new ArrayList<>(getPtmPositionByPeptideId().keySet());
    }

    public Integer getPositionOnProtein() {
        return m_site.seqPosition;
    }

    public boolean isNterm(){
        return this.isNtermSite.orElseGet(() -> m_ptmSpecificity.getLocationSpecificity().toLowerCase().endsWith("n-term"));
    }

    public boolean isCterm(){
        return this.isCtermSite.orElseGet(() -> m_ptmSpecificity.getLocationSpecificity().toLowerCase().endsWith("c-term"));
    }

    public boolean isProteinNTerm() {
        return m_ptmSpecificity.getLocationSpecificity().endsWith("N-term");        
    }
    
    public boolean isProteinCTerm() {
        return m_ptmSpecificity.getLocationSpecificity().endsWith("C-term");        
    }
    
    public boolean isProteinNTermWithOutM() {
        return m_ptmSpecificity.getLocationSpecificity().endsWith("N-term") && getPositionOnProtein() == 1;
    }

}
