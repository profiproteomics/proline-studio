/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PtmSitePeptide {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private long _pepId;
    private long _pepMatchId;
    private String _sequence;

    private int _locationInPep;
    private int _locationProtein;
    private float _ptmProbability;
    private int _beginInProtein;

    private ArrayList<PtmSiteAA> _ptmList;

    /**
     * used by test demo
     *
     * @param pepId
     * @param pepMatchId
     * @param aa, specify amino aide
     * @param sequence
     * @param ptm
     * @param locationInPep, the specify aa location in pep
     * @param locationProtein, the specify aa location in protein
     * @param ptmProbability, the specify aa ptm probability
     */
//    public PtmSitePeptide(long pepId, long pepMatchId, Character aa, String sequence, String ptm, int locationInPep, int locationProtein, float ptmProbability) {
//
//        _ptmList = new ArrayList<PtmSiteAA>();
//        _beginInProtein = locationProtein - locationInPep; //@todo bug, when N-term and PTM at 1
//        this._pepId = pepId;
//        this._pepMatchId = pepMatchId;
//        this._sequence = sequence;
//
//        this._locationInPep = locationInPep;
//        this._locationProtein = locationProtein;
//        this._ptmProbability = ptmProbability;
//        String[] ptmSet = ptm.split(";");
//
//        for (String element : ptmSet) {
//            PtmSiteAA pSite = createPtmSiteAA(element, _beginInProtein);
//            if (Objects.equals(pSite.getAminoAcid(), aa)) {//juste a check
//                if (locationInPep != pSite.getModifyLocPep() || locationProtein != pSite.getModifyLocProtein()) {
//                    logger.error("Amino acide: " + aa + "(" + locationInPep + "," + locationProtein + ") <>" + pSite.toString());
//                }
//            }
//            _ptmList.add(pSite);
//        }
//
//    }

    /**
     *
     * @param pepId
     * @param pepMatchId
     * @param sequence
     * @param ptmSet
     * @param distance
     */
    public PtmSitePeptide(long pepId, long pepMatchId, String sequence, ArrayList<PtmSiteAA> ptmSet, int distance) {

        _ptmList = ptmSet;
        _beginInProtein = distance; 
        this._pepId = pepId;
        this._pepMatchId = pepMatchId;
        this._sequence = sequence;
    }
    /**
     * test use
     * @return 
     */
    public int getBeginInProtein() {
        return _beginInProtein;
    }

    public ArrayList<PtmSiteAA> getPtmSiteAAList() {
        return _ptmList;
    }

    public String getSequence() {
        return _sequence;
    }



    @Override
    public String toString() {
        return "PtmSitePeptide{" + "sequence=" + _sequence + ", Modify location( in Peptide: " + _locationInPep + " , in Protein :" + _locationProtein
                + "), ptmProbability=" + _ptmProbability + ", m_ptmList=" + _ptmList + '}';
    }

}
