package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.rsmexplorer.gui.ptm.PTMMark;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    List<PtmMarkView> _PTMMarkList;

    public PtmMarkModel() {
        _PTMMarkList = new ArrayList<>();
    }

    public List<PtmMarkView> getPTMMarkList() {
        return _PTMMarkList;
    }

    /**
     * treat data
     *
     * @param ptmSiteAA2Mark
     */
    public void setPTM(Collection<PTMMark> ptmMarks) {
        _PTMMarkList = new ArrayList<>();
        for (PTMMark pa : ptmMarks) {
            PtmMarkView p = new PtmMarkView(pa);
            _PTMMarkList.add(p);
        }

    }

    public void addPTM(PTMMark ptm) {
        PtmMarkView p = new PtmMarkView(ptm);
        _PTMMarkList.add(p);
    }

    public boolean removePtm(PTMMark ptm) {
        int index = _PTMMarkList.indexOf(ptm);
        _PTMMarkList.remove(index);
        return _PTMMarkList.remove(ptm);
    }

}
