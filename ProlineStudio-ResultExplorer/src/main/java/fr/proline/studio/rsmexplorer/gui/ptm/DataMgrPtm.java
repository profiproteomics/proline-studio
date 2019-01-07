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
public class DataMgrPtm {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    /**
     * All data for this databox
     */
    private PTMSite _currentPtmSite;
    /**
     * all peptide who contain this PTM Site
     */
    private List<PTMSitePeptideInstance> _peptidesInstances;
    private Map<Integer, PTMMark> _allPtmMarks;

    private String _proteinSequence;
    private int _beginBestFit;

    public DataMgrPtm() {
        _peptidesInstances = new ArrayList<>();
        _beginBestFit = Integer.MAX_VALUE;
    }

    public String getProteinSequence() {
        return _proteinSequence;
    }

    public int getRowCount() {
        if (_peptidesInstances == null) {
            return -1;
        }
        return _peptidesInstances.size();
    }

    public Collection<PTMMark> getAllPtmMarks() {
        return this._allPtmMarks.values();
    }

    /**
     *
     * @param selectedPTMSite in first DataBox will be show in this Box
     */
    public void setData(PTMSite selectedPTMSite) {
        if (_currentPtmSite != null && _currentPtmSite.equals(selectedPTMSite)) {
            return;
        }
        _beginBestFit = Integer.MAX_VALUE;
        _currentPtmSite = selectedPTMSite;

        _peptidesInstances = new ArrayList<>();
        _allPtmMarks = new HashMap<>();

        if (_currentPtmSite == null) {
            logger.debug(this.getClass().getName() + "setData" + " data is null");
            this._proteinSequence = "";
            this._beginBestFit = 0;
            return;
        }

        Set<Long> peptidesIds = new HashSet<>();
       
//retrive each peptide
        for (DPeptideInstance parentPeptideInstance : _currentPtmSite.getParentPeptideInstances()) {
            long peptideId = parentPeptideInstance.getPeptideId();
            peptidesIds.add(peptideId);
            PTMSitePeptideInstance ptmPepInstance = _currentPtmSite.getPTMSitePeptideInstance(peptideId);
            _peptidesInstances.add(ptmPepInstance);
            //retrive all ptm in string format
            if (_currentPtmSite.isProteinNTerm()) {//@todo verify N-term
                _beginBestFit = 0;
            } else if (_beginBestFit > ptmPepInstance.getPTMPeptideInstance().getStartPosition()) {
                _beginBestFit = ptmPepInstance.getPTMPeptideInstance().getStartPosition();
            }
           logger.debug("begin best fit is="+_beginBestFit);
//create PTMMark, take all of the site(position, type of modification) from this peptide, in order to create a PTMMark list

            for (DPeptidePTM ptm : parentPeptideInstance.getPeptide().getTransientData().getDPeptidePtmMap().values()) {
                int protLocation;
                if (_currentPtmSite.isProteinNTerm()) {
                   protLocation = (int) ptm.getSeqPosition();
                   
                    if (protLocation == 0) {
                        protLocation = 1;
                    }
                } else {
                    protLocation = ptmPepInstance.getPTMPeptideInstance().getStartPosition() + (int) ptm.getSeqPosition();
                }
                PTMMark mark = new PTMMark(ptm, protLocation);
                _allPtmMarks.put(protLocation, mark);
            }
        }
//create sequence
        DProteinMatch pm = _currentPtmSite.getProteinMatch();
        DBioSequence bs = pm.getDBioSequence();
        if (bs != null) {
            _proteinSequence = bs.getSequence();
        } else {
            _proteinSequence = createSequence();
        }
        logger.debug(" row/peptide size=" + _peptidesInstances.size());
    }

    int getBeginBestFit() {
        return this._beginBestFit;
    }

    /**
     * from the created _PtmSitePeptideList, construit the protein sequence
     *
     * @return
     */
    private String createSequence() {
        StringBuilder sb = new StringBuilder();
        //prefix
        if (this._beginBestFit > 1) {
            for (int i = 1; i < this._beginBestFit - 1; i++) { // location begin from 1
                sb.append("-");
            }
        }

        for (PTMSitePeptideInstance item : _peptidesInstances) {
            PTMPeptideInstance pp = item.getPTMPeptideInstance();
            
            PTMSite site = item.getSite();
            //logger.debug("In  |"+sb.toString()+"("+sb.length());
            String content = pp.getSequence();
            int cLength = content.length();
            int pIndex = pp.getStartPosition();
            if (pIndex == 1 && site.isProteinNTerm()) {//@todo verify
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

    protected DPeptideInstance getSelectedPeptideInstance(int row) {
        if (this.getRowCount() == 0) {
            return null;
        }
        if (row < 0 || (row >= getRowCount())) {
            return null;
        }
        if (this._peptidesInstances.get(row) == null) {
            return null;
        }
        return this._peptidesInstances.get(row).getPTMPeptideInstance().getPeptideInstance();
    }

    public Object getValueAt(int rowIndex) {
        return _peptidesInstances.get(rowIndex);
    }

    public List<PTMSitePeptideInstance> getPTMSitePeptideInstances() {
        return _peptidesInstances;
    }

}
