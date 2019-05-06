/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;

/**
 *
 * @author Karine XUE
 */
public class PeptideSetView extends ViewPtmAbstract {

    ArrayList<PeptideView> m_peptideList;

    public PeptideSetView() {
        m_peptideList = new ArrayList<>();
    }

    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {
        x0 = m_x;
        y0 = m_y;

        for (PeptideView vp : m_peptideList) {
            paintGrillX(g, viewContext.getAreaWidth(), x0, y0);
            paintGrillX(g, viewContext.getAreaWidth(), x0, y0 + ViewSetting.HEIGHT_AA);
            vp.setBeginPoint(x0, y0);
            g.setColor(ViewSetting.PEPTIDE_COLOR);
            vp.paint(g, viewContext);

            y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
        }
    }

    private void paintGrillX(Graphics2D g, int areaWidth, int x, int y) {
        Stroke s = g.getStroke();//remain original stroke
        g.setColor(CyclicColorPalette.GRAY_GRID);
        g.setStroke(ViewSetting.DASHED);
        g.drawLine(x, y, areaWidth, y);
        g.setStroke(s);
    }

    @Override
    public void setBeginPoint(int x, int y) {
        m_x = x;
        m_y = y;
    }

    protected void setPeptideViewList(ArrayList<PeptideView> peptideList) {
        if(peptideList == null)
            m_peptideList = new ArrayList<>();
        else 
            m_peptideList = peptideList;
    }

    protected int getSelectedItemIndex(int x, int y) {
        boolean selected = false;
        y0 = m_y;
        PeptideView vp;
             for (int i = 0; i < m_peptideList.size(); i++) {
                vp = m_peptideList.get(i);
                int yRangA = y0;
                int yRangZ = yRangA + ViewSetting.HEIGHT_AA;
                if (y > yRangA && y < yRangZ) {
                    //selected = vp.isSelected(x);
                    selected = true; //when x is not inside of peptide, we select it
                    if (selected) {
                        return i;
                    }
                }
                y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
            }        
        return -1;
    }

    protected String getToolTipText(int x, int y) {
       int index  = getSelectedItemIndex(x,y);
        
       if (index != -1)
           return m_peptideList.get(index).getToolTipText(x);
       else return null;
    }

}
