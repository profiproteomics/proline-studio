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
    private boolean m_ptmIsNorCTerm; //N-term, C-term

    public PTMMark(DPeptidePTM peptidePtm, int proteinLocation) {
        this(peptidePtm, proteinLocation, proteinLocation, false);
    }

    public PTMMark(DPeptidePTM peptidePtm, int proteinLocation, int proteinDisplayLocation, boolean isNCTermPTM) {
        this.m_peptidePtm = peptidePtm;
        this.m_proteinLocation = proteinLocation;
        m_proteinDisplayLocation = proteinDisplayLocation;
        m_ptmIsNorCTerm = isNCTermPTM;
    }

    public boolean equals(PTMMark compare) {
        return (compare.getProteinLocation() == this.m_proteinLocation && m_peptidePtm.getIdPeptide() == compare.getPeptidePtm().getIdPeptide());
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

    public boolean isPTMNorCterm() {
        return m_ptmIsNorCTerm;
    }

    public String getPtmShortName() {
        DInfoPTM ptm = DInfoPTM.getInfoPTMMap().get(m_peptidePtm.getIdPtmSpecificity());
        return ptm.getPtmShortName();
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
     * The PtmSpecificity contains the informations of (location, amino acide,
     * ptm_id and its name)
     *
     * @return le identifient de PtmSpecificity
     */
    public Long getPtmSpecificityId() {
        return m_peptidePtm.getIdPtmSpecificity();
    }

    @Override
    public String toString() {
        return "PTMMark{" + "m_peptidePtm=" + this.getPtmShortName() + " id=" + m_peptidePtm.getIdPtmSpecificity() + ", m_proteinLocation=" + m_proteinLocation  + ", m_ptmIsNorCTerm=" + m_ptmIsNorCTerm + '}';
    }

}
