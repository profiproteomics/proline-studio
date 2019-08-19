package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.dam.tasks.data.ptm.PTMSitePeptideInstance;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
/**
 *
 * @author Karine XUE
 */
public class PeptideAreaModel {

    ArrayList<PeptideView> m_viewPeptideList;
    int m_selectedIndex;

    public PeptideAreaModel() {
        m_viewPeptideList = new ArrayList<PeptideView>();
        m_selectedIndex = 0;

    }

    public ArrayList<PeptideView> getViewPeptideList() {
        return m_viewPeptideList;
    }

    /**
     * set all peptide of a given protein as PTMSitePeptideInstance (références: peptide instance + PTMSite + leafs peptide instances...)
     * Create assocated PeptideView object from PTMSitePeptideInstance
     *
     * @param pPeptide
     */
    public void setPTM(List<PTMSitePeptideInstance> pPeptide) {
        m_viewPeptideList = new ArrayList<>();
        for (PTMSitePeptideInstance pep : pPeptide) {
            PeptideView p = new PeptideView(pep);
            m_viewPeptideList.add(p);
        }
    }

     /**
     * set all peptide of a given protein as PTMPeptideInstance (références: peptide instance + all PTMSite...)
     * Create assocated PeptideView object from PTMSitePeptideInstance
     *
     * @param pPeptide
     */
    public void setPTMPeptides(List<PTMPeptideInstance> pPeptide) {
        m_viewPeptideList = new ArrayList<>();
        boolean onlyProtNtermWO_M = true;
        //Identify if PTMPeptideInstance are all Protein NTerm peptide with missing 'M'
        List<PTMSite> allSites = pPeptide.stream().flatMap(pi -> pi.getSites().stream()).collect(Collectors.toList());
        for(PTMSite site : allSites) {           
            onlyProtNtermWO_M = onlyProtNtermWO_M && site.isProteinNTermWithOutM();
        }
        
        for (PTMPeptideInstance pep : pPeptide) {
            PeptideView p = new PeptideView(pep, onlyProtNtermWO_M);
            m_viewPeptideList.add(p);
        }
    }
    
    /**
     * prepare repaint
     *
     * @param index
     */
    protected void setSelectedIndex(int index) {
        if (index == -1) {
            return;
        }
        this.m_selectedIndex = index;
        PeptideView pv;
        for (int i = 0; i < m_viewPeptideList.size(); i++) {
            pv = m_viewPeptideList.get(i);
            if (i != index) {
                pv.setSelected(false);
            } else {
                pv.setSelected(true);
            }
        }
    }

    protected void setRelativeSelected(int relative) {
        int index = this.m_selectedIndex + relative;
        if ((index == -1) || (index == this.getViewPeptideList().size())) {
            return;
        } else {
            this.m_selectedIndex = index;
            this.setSelectedIndex(index);
        }
    }

    protected int getSelectedIndex() {
        return this.m_selectedIndex;
    }

}
