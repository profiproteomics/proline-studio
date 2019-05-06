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
public class PTMMarkModel {

    List<PTMMarkView> m_ptmMarkList;

    public PTMMarkModel() {
        m_ptmMarkList = new ArrayList<>();
    }

    public List<PTMMarkView> getPTMMarkList() {
        return m_ptmMarkList;
    }

    /**
     * treat data
     *
     * @param ptmSiteAA2Mark
     */
    public void setPTM(Collection<PTMMark> ptmMarks) {
        m_ptmMarkList = new ArrayList<>();
        for (PTMMark pa : ptmMarks) {
            PTMMarkView p = new PTMMarkView(pa);
            m_ptmMarkList.add(p);
        }

    }

    public void addPTM(PTMMark ptm) {
        PTMMarkView p = new PTMMarkView(ptm);
        m_ptmMarkList.add(p);
    }

    public boolean removePtm(PTMMark ptm) {
        int index = m_ptmMarkList.indexOf(ptm);
        m_ptmMarkList.remove(index);
        return m_ptmMarkList.remove(ptm);
    }

}
