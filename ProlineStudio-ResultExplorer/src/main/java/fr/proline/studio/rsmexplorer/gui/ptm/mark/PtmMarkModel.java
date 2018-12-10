package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSiteAA;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
/**
 *
 * @author Karine XUE
 */
public class PtmMarkModel {
    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");

    ArrayList<PtmMarkView> _PTMMarkList;

    public PtmMarkModel() {
        _PTMMarkList = new ArrayList<>();
    }

    public ArrayList<PtmMarkView> getPTMMarkList() {
        return _PTMMarkList;
    }

    /**
     * treat data
     *
     * @param ptmSiteAA2Mark
     */
    public void setPTM(ArrayList<PtmSiteAA> ptmSiteAA2Mark) {
        boolean  ajustNTerminiAt1 = false;

        _PTMMarkList = new ArrayList<>();
        for (PtmSiteAA pa : ptmSiteAA2Mark) {
            PtmMarkView p = new PtmMarkView(pa);
            _PTMMarkList.add(p);
        }
        
       
    }

    public void addPTM(PtmSiteAA ptm) {
        PtmMarkView p = new PtmMarkView(ptm);
        _PTMMarkList.add(p);
    }

    public boolean removePtm(PtmSiteAA ptm) {
        int index = _PTMMarkList.indexOf(ptm);
        _PTMMarkList.remove(index);
        return _PTMMarkList.remove(ptm);
    }

}
