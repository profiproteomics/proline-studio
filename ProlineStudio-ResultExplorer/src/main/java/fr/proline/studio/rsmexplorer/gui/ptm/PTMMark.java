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

    public PTMMark(DPeptidePTM m_peptidePtm, int m_proteinLocation) {
        this.m_peptidePtm = m_peptidePtm;
        this.m_proteinLocation = m_proteinLocation;
    }

    public DPeptidePTM getPeptidePtm() {
        return m_peptidePtm;
    }

    public int getProteinLocation() {
        return m_proteinLocation;
    }

    public char getPtmSymbol() {
        DInfoPTM ptm = DInfoPTM.getInfoPTMMap().get(m_peptidePtm.getIdPtmSpecificity());
        return ptm.getPtmShortName().toUpperCase().charAt(0);
    }

    public Long getPtmSpecificityId() {
        return m_peptidePtm.getIdPtmSpecificity();
    }
    
    
    
}
