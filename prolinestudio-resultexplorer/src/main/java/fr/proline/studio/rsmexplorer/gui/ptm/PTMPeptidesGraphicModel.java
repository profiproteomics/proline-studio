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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private int m_beginBestFit;
    
    public PTMPeptidesGraphicModel() {
        m_ptmPeptidesInstances = new ArrayList<>();
        m_beginBestFit = Integer.MAX_VALUE;
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
        
        m_ptmPeptidesInstances = ptmInstances;
        m_allPtmMarks = new HashMap<>();
        m_beginBestFit = Integer.MAX_VALUE;
        
        if (ptmInstances == null || ptmInstances.isEmpty()) {
            LOG.debug(this.getClass().getName() + "setData" + " data is null");
            this.m_proteinSequence = "";
            this.m_beginBestFit = 0;
            return;
        }

        Set<PTMSite> ptmSites = new HashSet<>();
        DProteinMatch pm = null;
        
        //retrive each peptide
        for (PTMPeptideInstance ptmPeptideInstance : ptmInstances){
            
            Iterator<PTMSite> PTMSitesIt = ptmPeptideInstance.getSites().iterator();
            while(PTMSitesIt.hasNext()){                
                PTMSite nextPTMSite = PTMSitesIt.next();
                if(pm == null)
                    pm = nextPTMSite.getProteinMatch();
                if(!ptmSites.contains(nextPTMSite)) {               
                    if (nextPTMSite.isProteinNTerm()) {
                        m_beginBestFit = 0;
                    } else if (m_beginBestFit > ptmPeptideInstance.getStartPosition()) {
                        m_beginBestFit = ptmPeptideInstance.getStartPosition();
                    } 
                }

                //create PTMMark, take all of the site(position, type of modification) from this peptide, in order to create a PTMMark list
                int protLocation;
                for (DPeptidePTM ptm : ptmPeptideInstance.getPeptideInstance().getPeptide().getTransientData().getDPeptidePtmMap().values()) {                
                    if (nextPTMSite.isProteinNTerm()) {
                        protLocation = (int) ptm.getSeqPosition();

                        if (protLocation == 0) {
                            protLocation = 1;
                        }
                    } else {
                        protLocation = ptmPeptideInstance.getStartPosition() + (int) ptm.getSeqPosition();
                    }
                    PTMMark mark = new PTMMark(ptm, protLocation);
                    m_allPtmMarks.put(protLocation, mark);
                }
            }
               
        }
        
        //create sequence  
        if(pm != null){
            DBioSequence bs = null;
            if ( !pm.isDBiosequenceSet() && prjId >0 ) {
                LOG.info("BioSequence is absent from the protein match, trying to load it ...");
                DatabaseBioSequenceTask.fetchData( Collections.singletonList(pm), prjId);
                bs = pm.getDBioSequence();
            }
            m_proteinSequence =  (bs == null) ? createSequence() : bs.getSequence();
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
        if (this.m_beginBestFit > 1) {
            for (int i = 1; i < this.m_beginBestFit - 1; i++) { // location begin from 1
                sb.append("-");
            }
        }

        for (PTMPeptideInstance pepInst : m_ptmPeptidesInstances) {
            boolean isProteinNTerm = false;
            if(pepInst.getSites().size()>0) 
                isProteinNTerm = pepInst.getSites().iterator().next().isProteinNTerm();

             //logger.debug("In  |"+sb.toString()+"("+sb.length());
            String content = pepInst.getSequence();
            int cLength = content.length();
            int pIndex = pepInst.getStartPosition();
            if (pIndex == 1 && isProteinNTerm) {//@todo verify
                pIndex = 0;
            }
            //logger.debug("Sequence (" + pIndex + "," + content + ")");
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
            //logger.debug(content+"("+pIndex+"-"+cLength);
            //logger.debug("Out |"+sb.toString()+"("+sb.length());
        }
        //logger.debug("finl Sequence :" + sb.toString());
        return sb.toString();
    }
    
    public int getBeginBestFit() {
        return this.m_beginBestFit;
    }
       
    public int getRowCount(){
        if (m_ptmPeptidesInstances == null) {
            return -1;
        }
        return m_ptmPeptidesInstances.size(); 
    }
    
    
    public Collection<PTMMark> getAllPtmMarks() {
        return this.m_allPtmMarks.values();
    }
}
