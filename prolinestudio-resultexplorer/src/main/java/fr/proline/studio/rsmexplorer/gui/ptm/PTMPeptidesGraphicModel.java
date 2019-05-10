/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.tasks.DatabaseBioSequenceTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMPeptidesGraphicModel {

    private static final Logger LOG = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    
    private PTMSite m_mainPTMSite; //dupliquer PanelGraphic
    private List<PTMPeptideInstance> m_ptmPeptidesInstances;
    private Map<Integer, PTMMark> m_allPtmMarks;
     
    private String m_proteinSequence;
    private int m_lowerStartLocation;
    private int m_higherEndLocation;
    
    public PTMPeptidesGraphicModel() {
        m_ptmPeptidesInstances = new ArrayList<>();
        m_lowerStartLocation = Integer.MAX_VALUE;
        m_higherEndLocation = Integer.MIN_VALUE;
    }
    
    public List<PTMPeptideInstance> getPTMPeptideInstance() {
        return m_ptmPeptidesInstances;
    }
    
    public PTMPeptideInstance getPeptideAt(int index){
        return m_ptmPeptidesInstances.get(index);
    }
    
    public int getPeptideIndex(PTMPeptideInstance pepInstance){
        for (int i = 0; i < this.m_ptmPeptidesInstances.size(); i ++){           
            if (m_ptmPeptidesInstances.get(i).equals(pepInstance))
                return i;
        }
        return -1;
    }
    
    public String getProteinSequence() {
        return m_proteinSequence;
    }
    
    public PTMSite getMainPTMSite(){
        return m_mainPTMSite;
    }
     
    public void setData(PTMSite mainPTMSite) {
        if (m_mainPTMSite != null && m_mainPTMSite.equals(mainPTMSite)) {
            return;
        }
        
        m_mainPTMSite = mainPTMSite;
    }
        
    
    public void setData(List<PTMPeptideInstance> ptmInstances, long prjId){
        
        if(Objects.equals(ptmInstances,m_ptmPeptidesInstances)){
            return;
        }
        
        //--- Reinitialize variables
        m_ptmPeptidesInstances = ptmInstances;
        if(ptmInstances == null)
            m_ptmPeptidesInstances =new ArrayList<>();
        m_allPtmMarks = new HashMap<>();
        m_lowerStartLocation = Integer.MAX_VALUE;
        m_higherEndLocation = Integer.MIN_VALUE;
        
        if ( m_ptmPeptidesInstances.isEmpty()) {            
            this.m_proteinSequence = "";
            this.m_lowerStartLocation = 0;
            m_higherEndLocation = 0;
            return;
        }

        DProteinMatch pm = null;
        boolean protNTermPTMWithOutMExist = false;
        final Map<Integer, List<PTMSite>> ptmSiteByProteinPos = new HashMap<>();
        Iterator<PTMSite> allSites = m_ptmPeptidesInstances.stream().flatMap(pi -> pi.getSites().stream()).collect(Collectors.toSet()).iterator();
        while (allSites.hasNext()) {
            PTMSite nextPTMSite= allSites.next();            
            if(pm == null)
                pm = nextPTMSite.getProteinMatch();
            int loc = nextPTMSite.getPositionOnProtein();
            if (nextPTMSite.isProteinNTermWithOutM()) {
                protNTermPTMWithOutMExist = true;
            }
            if(!ptmSiteByProteinPos.containsKey(loc))
                ptmSiteByProteinPos.put(loc,new ArrayList<>()); 

            ptmSiteByProteinPos.get(loc).add(nextPTMSite); 
        }
        
        //retrive each peptide
        for (PTMPeptideInstance ptmPeptideInstance : ptmInstances){
            //for Nterm could be 0 or 1 if first AA of prot not present (withouM)
            int ptmPepStartLocOnProt = ptmPeptideInstance.getStartPosition();
            int ptmPepSeqLenght =  ptmPeptideInstance.getSequence().length();            
                    
            if (m_lowerStartLocation > ptmPepStartLocOnProt) 
                m_lowerStartLocation = ptmPepStartLocOnProt;
            if(m_higherEndLocation < (ptmPepStartLocOnProt + ptmPepSeqLenght))
                m_higherEndLocation = ptmPepStartLocOnProt + ptmPepSeqLenght;
            
            //create PTMMark, take all of the site(position, type of modification) from this peptide, in order to create a PTMMark list
            int protLocation;                
            for (DPeptidePTM ptm : ptmPeptideInstance.getPeptideInstance().getPeptide().getTransientData().getDPeptidePtmMap().values()) {
                boolean currentPTMisNCterm = false;
                int locOnPeptide = (int) ptm.getSeqPosition();
                protLocation = ptmPepStartLocOnProt + locOnPeptide;
                int protLocToDisplay = protLocation;
                if(ptmSiteByProteinPos.containsKey(protLocation) ){
                    List<PTMSite> ptmSitesPositionned = ptmSiteByProteinPos.get(protLocation);
                    Optional<PTMSite> correspondingPTMSite = ptmSitesPositionned.stream().filter(site -> site.getPTMSpecificity().getIdPtmSpecificity() == ptm.getIdPtmSpecificity()).findFirst();
                    if(correspondingPTMSite.isPresent()) {
                        if (correspondingPTMSite.get().isProteinNTermWithOutM()) {
                            m_lowerStartLocation = 0;                        
                            protLocToDisplay = 1;
                        }
                        if(correspondingPTMSite.get().isProteinCTerm() || correspondingPTMSite.get().isProteinNTerm())
                            currentPTMisNCterm = true;
                    }
                } else {
                   LOG.warn("Try to display a PTM without associated PTMSites.... "+protLocation+" def id "+ptm.toString());
                }
                if(protNTermPTMWithOutMExist) //Sequence of Prtotein displayed without M : shift location on prot
                    protLocation = protLocation-1;  
                PTMMark mark = new PTMMark(ptm, protLocation, protLocToDisplay,currentPTMisNCterm);
                LOG.trace(" PTMPepGraphicModel setData add PTMMark at " + protLocation +" show " + protLocToDisplay +" symb " + mark.getPtmSymbol());                  
                m_allPtmMarks.put(protLocation, mark);                    
            }
//            }
            LOG.trace(" PTMPepGraphicModel setData m_lowerStartLocation" + m_lowerStartLocation+" nbr m_allPtmMarks "+m_allPtmMarks.size());  
        }
        
        //create sequence  
        if(pm != null){
            DBioSequence bs = pm.getDBioSequence();
            if ( bs == null && prjId >0 ) {
                LOG.trace("BioSequence is absent from the protein match, trying to load it ...");
                DatabaseBioSequenceTask.fetchData( Collections.singletonList(pm), prjId);
                bs = pm.getDBioSequence();
            } 
            m_proteinSequence ="";
            if ( bs != null){
                m_proteinSequence = bs.getSequence();
                if(protNTermPTMWithOutMExist)
                    m_proteinSequence = m_proteinSequence.substring(1);
            } else                      
                m_proteinSequence =  createSequence();
        }

    }

     /**
     * from the created _PtmSitePeptideList, construit the protein sequence
     *
     * @return
     */
    private String createSequence() {
        StringBuilder sb = new StringBuilder();
        //prefix
        if (this.m_lowerStartLocation > 1) {
            for (int i = 1; i < this.m_lowerStartLocation - 1; i++) { // location begin from 1
                sb.append("-");
            }
        }

        for (PTMPeptideInstance pepInst : m_ptmPeptidesInstances) {
            boolean isProteinNTerm = false;
            if(pepInst.getSites().size()>0) 
                isProteinNTerm = pepInst.getSites().iterator().next().isProteinNTermWithOutM();

            String content = pepInst.getSequence();
            int cLength = content.length();
            int pIndex = pepInst.getStartPosition();
            if (pIndex == 1 && isProteinNTerm) {//@todo verify
                pIndex = 0;
            }
            int l = sb.length();
            if (l < pIndex) {
                for (int i = l; i < pIndex; i++) {
                    sb.append("-");
                }
                sb.append(content);
            } else if (sb.length() > cLength + pIndex) {
                sb.replace(pIndex, pIndex + cLength, content);
            } else {
                sb.delete(pIndex, sb.length());
                sb.append(content);
            }
        }
        return sb.toString();
    }
    
    public int getLowerStartInProtSeq() {
        return this.m_lowerStartLocation;
    }
    
    public int getHigherEndInProtSeq() {
        return this.m_higherEndLocation;
    }
       
    public int getRowCount(){
        return m_ptmPeptidesInstances.size(); 
    }
    
    
    public Collection<PTMMark> getAllPtmMarks() {
        return this.m_allPtmMarks.values();
    }
}
