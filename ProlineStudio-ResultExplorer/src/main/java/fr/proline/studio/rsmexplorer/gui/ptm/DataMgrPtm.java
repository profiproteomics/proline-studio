/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.studio.dam.tasks.data.PTMSite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class DataMgrPtm {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
    /**
     * All data for this databox
     */
    private PTMSite _currentPtmSite;
    private ArrayList<Row> _ptmSitePeptideRowSet;

    private ArrayList<PtmSitePeptide> _PtmSitePeptideList;
    private ArrayList<PtmSiteAA> _PtmSiteAA2Mark;
    private String _proteinSequence;
    private int _beginBestFit;

    public DataMgrPtm() {
        _ptmSitePeptideRowSet = new ArrayList<>();
        _beginBestFit = Integer.MAX_VALUE;
    }

    private void fireDataChanged() {
        //this.m_control.fire();

    }

    public String getProteinSequence() {
        return _proteinSequence;
    }

    public ArrayList<PtmSitePeptide> getPtmSitePeptideList() {
        return _PtmSitePeptideList;
    }

    public int getRowCount() {
        return _PtmSitePeptideList.size();
    }

    /**
     * create a list of mark to paint, one (PtmType, location) appear only 1
     * time
     *
     * @return
     */
    private ArrayList<PtmSiteAA> createPtmSitePP2Mark() {
        ArrayList<PtmSiteAA> result = new ArrayList();
        HashMap<Integer, PtmSiteAA> locationPtmMap = new HashMap();
        Integer loc;
        for (PtmSitePeptide mpep : _PtmSitePeptideList) {
            for (PtmSiteAA mAA : mpep.getPtmSiteAAList()) {
                loc = (Integer) mAA.getModifyLocProtein();
                if (locationPtmMap.get(loc) == null) { // first time, only one Time for a location PtmSiteAA
                    locationPtmMap.put(loc, mAA);
                }
            }
        }
        Collection set = locationPtmMap.values();
        for (Object o : set) {
            result.add((PtmSiteAA) o);
        }
        //logger.debug("all ptm Site AA " + result.toString());
        return result;
    }

    public ArrayList<PtmSiteAA> getAllPtmSite2Mark() {
        return this._PtmSiteAA2Mark;
    }

    /**
     *
     * @param selectedPTMSite in first DataBox will be show in this Box
     * @param parentPepInstance peptideMatch
     */
    public void setData(PTMSite selectedPTMSite) {

        if (_currentPtmSite != null && _currentPtmSite.equals(selectedPTMSite)) {
            return;
        }
        //public void setData(PTMSite selectedPTMSite, DPeptideInstance parentPepInstance) {
        _beginBestFit = Integer.MAX_VALUE;
        _currentPtmSite = selectedPTMSite;

        _ptmSitePeptideRowSet = new ArrayList<>();
        if (_currentPtmSite == null) {
            //logger.debug("---->" + this.getClass().toString() + " set data is null");
            fireDataChanged();
            return;
        }
        //@todo verify only the bestPeptideMatch
        List<DPeptideInstance> dpInstanceList = _currentPtmSite.getParentPeptideInstances();
        //List<DPeptideInstance> dpLeafInstanceList = _currentPtmSite.getLeafPeptideInstances(peptideId);
        for (DPeptideInstance parentPeptideInstance : dpInstanceList) {
            long peptideId = parentPeptideInstance.getPeptideId();
            //_currentPtmSite.get
            DPeptideMatch bestPM = _currentPtmSite.getBestPeptideMatchForPeptide(peptideId);
            //here we copy the same as setData in PeptidesOfPTMSiteTableModel
            _ptmSitePeptideRowSet.add(new Row(parentPeptideInstance, bestPM));
        }

        _PtmSitePeptideList = createPtmSitePeptideList();
        _PtmSiteAA2Mark = createPtmSitePP2Mark();
        DProteinMatch pm = _currentPtmSite.getProteinMatch();
        DBioSequence bs = pm.getDBioSequence();
        if (bs != null) {
            _proteinSequence = bs.getSequence();
        } else {
            _proteinSequence = createSequence();
        }
    }

    /**
     * be used by test demo
     */
    public void setData(ArrayList<PtmSitePeptide> list) {
        _PtmSitePeptideList = list;
        for (PtmSitePeptide pp : list) {
            if (this._beginBestFit > pp.getBeginInProtein()) {
                this._beginBestFit = pp.getBeginInProtein();
            }
        }
        _PtmSiteAA2Mark = createPtmSitePP2Mark();
        _proteinSequence = createSequence();
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
            for (int i = 1; i < this._beginBestFit - 1; i++) {//lcoation begin from 1
                sb.append("-");
            }
        }

        for (PtmSitePeptide pp : _PtmSitePeptideList) {
            //logger.debug("In  |"+sb.toString()+"("+sb.length());
            String content = pp.getSequence();
            int cLength = content.length();
            int pIndex = pp.getBeginInProtein();
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

//    protected DPeptideInstance getSelectedPeptideInstance(int row) {
//        if (row < 0 || (row >= getRowCount())) {
//            return null;
//        }
//        return (this._ptmSitePeptideRowSet.get(row)).peptideInstance;
//    }

    /**
     * we copy the same method from PeptidesOfPTMSiteTableModel.java
     */
    static class Row {

        DPeptideInstance peptideInstance;
        DPeptideMatch peptideMatch;

        public Row(DPeptideInstance peptideInstance, DPeptideMatch peptideMatch) {
            this.peptideInstance = peptideInstance;
            this.peptideMatch = peptideMatch;
        }

    }

    public Object getValueAt(int rowIndex) {

        return _ptmSitePeptideRowSet.get(rowIndex);
    }

    /**
     * Attention, the location begin with 1, we count location from 1 to N, so
     * the 0 is N-termini, the length+1 is C-Termini
     */
    private ArrayList<PtmSitePeptide> createPtmSitePeptideList() {
        ArrayList<PtmSitePeptide> ptmPepList = new ArrayList();
        long pepId, pepMatchId;
        Character aa;
        String sequence;
        String ptmReadString;
        boolean isNTermAt1;
        int aaLocationInPep, aaLocationInProtein, peptideBeginLocationInProtein;
        float ptmProbability;
        PtmSitePeptide pPeptide;

        ArrayList<PtmSiteAA> ptmFromStringList;

        for (int rowIndex = 0; rowIndex < _ptmSitePeptideRowSet.size(); rowIndex++) {

            ptmFromStringList = new ArrayList<PtmSiteAA>();
            isNTermAt1 = false;

            DPeptideMatch pepMatch = _ptmSitePeptideRowSet.get(rowIndex).peptideMatch;
            pepId = _ptmSitePeptideRowSet.get(rowIndex).peptideInstance.getId();

            pepMatchId = pepMatch.getId();

            aa = _currentPtmSite.getPtmSpecificity().getRresidueAASpecificity();
            sequence = pepMatch.getPeptide().getSequence();

            //**********  here is all ptm in string like Acetyl (Protein N-term); Phospho (S7); Phospho (T9); Phospho (S10); Phospho (S14); Phospho (Y30)
            DPtmSiteProperties properties = pepMatch.getPtmSiteProperties();

            PeptideReadablePtmString prPtmString = pepMatch.getPeptide().getTransientData().getPeptideReadablePtmString();

            ptmReadString = "";
            if (prPtmString != null) {
                ptmReadString = prPtmString.getReadablePtmString();//get All ptmSite

            }
            aaLocationInProtein = _currentPtmSite.seqPosition;
            aaLocationInPep = _currentPtmSite.getPtmPositionOnPeptide(pepMatch.getPeptide().getId());
            peptideBeginLocationInProtein = aaLocationInProtein - aaLocationInPep;

            String locationSpecitifcity = _currentPtmSite.getPtmSpecificity().getLocationSpecificity();
            if (locationSpecitifcity.contains("N-term")) {

                if (aaLocationInProtein == 1) {
                    isNTermAt1 = true;
                }

            } else if (locationSpecitifcity.contains("C-term")) {
                boolean isCterm = true;
            }

            String[] ptmSet = ptmReadString.split(";");
            for (String ptm : ptmSet) {
                PtmSiteAA pa =new PtmSiteAA(ptm.trim(), peptideBeginLocationInProtein, isNTermAt1);
                ptmFromStringList.add(pa);
                logger.debug(""+this.getClass().toString()+" 1-PtmSiteAA:" + pa.toString());
            }

            //a subset of ptmsite can retrived probability
            if (properties != null) {
                 float  proba =  properties.getMascotDeltaScore();
                 logger.debug(""+this.getClass().toString()+" 2-PtmSiteAA: (" +aa+aaLocationInProtein+")"+ proba );
                Map<String, Float> ptmProbabilitySet = properties.getMascotProbabilityBySite();
                for (PtmSiteAA psa : ptmFromStringList) {
                    Float prob = ptmProbabilitySet.get(psa.getPtmSite());
                    if (prob != null) {
                        psa.setProbability(prob);
                        logger.debug(""+this.getClass().toString()+" 3-PtmSiteAA:" + psa.toString()+prob);
                    }
                }
            }

            
            
            
            pPeptide = new PtmSitePeptide(pepId, pepMatchId, sequence, ptmFromStringList, peptideBeginLocationInProtein);
            if (isNTermAt1 && peptideBeginLocationInProtein == 1) {
                this._beginBestFit = 0;//modify adjustLocation is N-Termini is at 1
            }
            if (this._beginBestFit > pPeptide.getBeginInProtein()) {//@todo isNTermAt1 done
                this._beginBestFit = pPeptide.getBeginInProtein();
            }
            //logger.debug("---calcul begin point:" + this._beginBestFit);
            //logger useful to collect test data
            //logger.debug("PtmSitePeptide Data: ((long)" + pepId + ", (long)" + pepMatchId + ", (Character)\'" + aa + "\', \"" + sequence + "\", \"" + ptm + "\", "
            //        + locationInPep + ", " + seqPositionInProtein + ", " + ptmProbability + "f)");
            ptmPepList.add(pPeptide);
        }
        return ptmPepList;
    }

}
