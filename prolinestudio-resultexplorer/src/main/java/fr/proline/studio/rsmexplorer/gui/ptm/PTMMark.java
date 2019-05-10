/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptidePTM;

/**
 *
 * @author CB205360
 */
public class PTMMark {
    
    private DPeptidePTM m_peptidePtm;
    private int m_proteinLocation;
    private int m_proteinDisplayLocation;
    private boolean m_ptmIsNorCTerm;

    public PTMMark(DPeptidePTM peptidePtm, int proteinLocation) {
        this(peptidePtm, proteinLocation, proteinLocation, false);
    }
    
    public PTMMark(DPeptidePTM peptidePtm, int proteinLocation, int proteinDisplayLocation, boolean isNCTermPTM) {
        this.m_peptidePtm = peptidePtm;
        this.m_proteinLocation = proteinLocation;
        m_proteinDisplayLocation = proteinDisplayLocation;
        m_ptmIsNorCTerm = isNCTermPTM;
    }

    public DPeptidePTM getPeptidePtm() {
        return m_peptidePtm;
    }
    
    public int getProteinLocation() {
        return m_proteinLocation;
    }
        
    public int getProteinLocationToDisplay() {
        return m_proteinDisplayLocation;
    }
    
    public boolean isPTMNorCterm(){
        return m_ptmIsNorCTerm;
    }
    /**
     * 
     * @return the first letter of the modification name
     */
    public char getPtmSymbol() {
        DInfoPTM ptm = DInfoPTM.getInfoPTMMap().get(m_peptidePtm.getIdPtmSpecificity());
        return ptm.getPtmShortName().toUpperCase().charAt(0);
    }
    /**
     * The PtmSpecificity contains the informations of (location, amino acide, ptm_id and its name)
     * @return le identifient de PtmSpecificity
     */
    public Long getPtmSpecificityId() {
        return m_peptidePtm.getIdPtmSpecificity();
    }
          
}
