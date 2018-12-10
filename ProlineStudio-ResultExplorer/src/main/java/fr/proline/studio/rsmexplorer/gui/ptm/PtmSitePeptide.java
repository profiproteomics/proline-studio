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
