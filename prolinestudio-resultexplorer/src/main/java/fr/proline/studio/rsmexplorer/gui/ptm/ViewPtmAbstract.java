/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;

/**
 *
 * @author Karine XUE
 */
public abstract class ViewPtmAbstract extends MouseAdapter {

    protected int x0;
    protected int y0;
    /**
     * Begin point x,y of ViewPtmMark Zone
     */
    protected int m_x;
    protected int m_y;

    public abstract void paint(Graphics2D g, ViewContext viewContext);

    public abstract void setBeginPoint(int x, int y);

}
