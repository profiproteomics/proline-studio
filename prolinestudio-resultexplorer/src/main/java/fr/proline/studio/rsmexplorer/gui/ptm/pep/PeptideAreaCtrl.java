/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSitePeptideInstance;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;

import java.awt.Graphics2D;
import java.util.List;

/**
 *
 * @author Karine XUE
 */
public class PeptideAreaCtrl {

    PeptideAreaModel m_mgr;
    PeptideSetView m_pepSetView;

    public PeptideAreaCtrl() {
        m_mgr = new PeptideAreaModel();
        m_pepSetView = new PeptideSetView();
    }

    public void setData(List<PTMSitePeptideInstance> ptmSitePeptide) {
        m_mgr.setPTM(ptmSitePeptide);
        m_pepSetView.setPeptideViewList(m_mgr.getViewPeptideList());
    }
    
    public void setPTMPepInstanceData(List<PTMPeptideInstance> ptmSitePeptide) {
        m_mgr.setPTMPeptides(ptmSitePeptide);
        m_pepSetView.setPeptideViewList(m_mgr.getViewPeptideList());
    }
    
    public void setBeginPoint(int x, int y) {
        this.m_pepSetView.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, ViewContext viewContext) {
        this.m_pepSetView.paint(g2, viewContext);
    }

    /**
     * 
     * @param x
     * @param y
     * @return  -1 when no found
     */
    public int getSelectedIndex(int x, int y) {
        int index = this.m_pepSetView.getSelectedItemIndex(x, y);
        this.m_mgr.setSelectedIndex(index);//change select
        return index;
    }

    public void setSelectedIndex(int i) {
        this.m_mgr.setSelectedIndex(i);       
    }

    public int getSelectedIndex() {
        return this.m_mgr.getSelectedIndex();
    }

    public String getToolTipText(int x, int y) {
        return this.m_pepSetView.getToolTipText(x, y);
    }

    public void setRelativeSelected(int relative) {
        this.m_mgr.setRelativeSelected(relative);
    }

}
