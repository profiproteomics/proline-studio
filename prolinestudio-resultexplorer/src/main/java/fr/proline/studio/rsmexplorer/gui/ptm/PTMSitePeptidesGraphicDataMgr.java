/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;

import java.util.*;

import fr.proline.studio.dam.tasks.data.ptm.PTMSitePeptideInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PTMSitePeptidesGraphicDataMgr {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    /**
     * All data for this databox
     */
    private PTMSite m_currentPtmSite; //dupliquer PanelGraphic ?
    /**
     * all peptide who contain this PTM Site
     */
    private List<PTMSitePeptideInstance> m_peptidesInstances;
    private Map<Integer, PTMMark> m_allPtmMarks;

    private String m_proteinSequence;
    private int m_beginBestFit;

    public PTMSitePeptidesGraphicDataMgr() {
        m_peptidesInstances = new ArrayList<>();
        m_beginBestFit = Integer.MAX_VALUE;
    }

    public String getProteinSequence() {
        return m_proteinSequence;
    }

    public int getRowCount() {
        if (m_peptidesInstances == null) {
            return -1;
        }
        return m_peptidesInstances.size();
    }

    public Collection<PTMMark> getAllPtmMarks() {
        return this.m_allPtmMarks.values();
    }

    /**
     *
     * @param selectedPTMSite in first DataBox will be show in this Box
     */
    public void setData(PTMSite selectedPTMSite) {
        if (m_currentPtmSite != null && m_currentPtmSite.equals(selectedPTMSite)) {
            return;
        }
        m_beginBestFit = Integer.MAX_VALUE;
        m_currentPtmSite = selectedPTMSite;

        m_peptidesInstances = new ArrayList<>();
        m_allPtmMarks = new HashMap<>();

        if (m_currentPtmSite == null) {
            logger.debug(this.getClass().getName() + "setData" + " data is null");
            this.m_proteinSequence = "";
            this.m_beginBestFit = 0;
            return;
        }

        Set<Long> peptidesIds = new HashSet<>();

//retrive each peptide
        for (DPeptideInstance parentPeptideInstance : m_currentPtmSite.getParentPeptideInstances()) {
            long peptideId = parentPeptideInstance.getPeptideId();
            peptidesIds.add(peptideId);
            PTMSitePeptideInstance ptmPepInstance = m_currentPtmSite.getPTMSitePeptideInstance(peptideId);
            m_peptidesInstances.add(ptmPepInstance);
            //retrive all ptm in string format
            if (m_currentPtmSite.isProteinNTermWithOutM()) {//@todo verify N-term
                m_beginBestFit = 0;
            } else if (ptmPepInstance!= null && m_beginBestFit > ptmPepInstance.getParentPTMPeptideInstance().getStartPosition()) {
                m_beginBestFit = ptmPepInstance.getParentPTMPeptideInstance().getStartPosition();
            }
//create PTMMark, take all of the site(position, type of modification) from this peptide, in order to create a PTMMark list

            for (DPeptidePTM ptm : parentPeptideInstance.getPeptide().getTransientData().getDPeptidePtmMap().values()) {
                int protLocation =0;
                if (m_currentPtmSite.isProteinNTermWithOutM()) {
                    protLocation = (int) ptm.getSeqPosition();

                    if (protLocation == 0) {
                        protLocation = 1;
                    }
                } else if (ptmPepInstance!= null){
                    protLocation = ptmPepInstance.getParentPTMPeptideInstance().getStartPosition() + (int) ptm.getSeqPosition();
                }
                PTMMark mark = new PTMMark(ptm, protLocation);
                m_allPtmMarks.put(protLocation, mark);
            }
        }
//create sequence
        DProteinMatch pm = m_currentPtmSite.getProteinMatch();
        DBioSequence bs = pm.getDBioSequence();
        if (bs != null) {
            m_proteinSequence = bs.getSequence();
        } else {
//            if (ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId() >0 ) {                
//                DatabaseBioSequenceTask.fetchData( Collections.singletonList(pm), ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject().getId());
//                bs = pm.getDBioSequence();
//            }   
//            if (bs != null) {
//                m_proteinSequence = bs.getSequence();
//            } else {
                m_proteinSequence = createSequence();
//            }
        }
    }

    int getBeginBestFit() {
        return this.m_beginBestFit;
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

        for (PTMSitePeptideInstance item : m_peptidesInstances) {
            PTMPeptideInstance pp = item.getParentPTMPeptideInstance();

            PTMSite site = item.getSite();
            //logger.debug("In  |"+sb.toString()+"("+sb.length());
            String content = pp.getSequence();
            int cLength = content.length();
            int pIndex = pp.getStartPosition();
            if (pIndex == 1 && site.isProteinNTermWithOutM()) {//@todo verify
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

    protected PTMSitePeptideInstance getSelectedPTMSitePeptideInstance(int row) {
        if (this.getRowCount() == 0) {
            return null;
        }
        if (row < 0 || (row >= getRowCount())) {
            return null;
        }
        if (this.m_peptidesInstances.get(row) == null) {
            return null;
        }
        return this.m_peptidesInstances.get(row);
    }

    protected DPeptideInstance getSelectedDPeptideInstance(int row) {
        PTMSitePeptideInstance selected = this.getSelectedPTMSitePeptideInstance(row);
        if (selected != null) {
            return selected.getParentPTMPeptideInstance().getPeptideInstance();
        } else {
            return null;
        }
    }

    public int getPeptideIndex(DPeptideInstance pep) {
        DPeptideInstance comparePep;
        for (int row = 0; row < this.m_peptidesInstances.size(); row ++){
            comparePep = m_peptidesInstances.get(row).getParentPTMPeptideInstance().getPeptideInstance();
            if (comparePep.equals(pep))
                return row;
        }
        return -1;
    }

    public List<PTMSitePeptideInstance> getPTMSitePeptideInstances() {
        return m_peptidesInstances;
    }

    protected int getPTMSiteSeqPos() {
        return this.m_currentPtmSite.getPositionOnProtein();
    }

}
