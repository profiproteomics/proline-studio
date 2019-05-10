/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;

import java.awt.Graphics2D;

/**
 *
 * @author Karine XUE
 */
public class ProteinSequenceCtrl {
    
    
    ProteinSequenceView m_view;

    public ProteinSequenceCtrl() {
        m_view = new ProteinSequenceView();
    }
    
    public void setData(String s){
        m_view.setSequence(s);
    }
    
    /**
     * set graphic begin location
     * @param x
     * @param y 
     */
    public void setBeginPoint(int x, int y) {
       this.m_view.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, ViewContext viewContext) {
        this.m_view.paint(g2, viewContext);
    }

    public void setPTMSequencePosition(int i) {
        this.m_view.setPTMSequencePosition(i);
    }
    
    @Override
    public String toString(){
        return m_view.toString();
    }
}
