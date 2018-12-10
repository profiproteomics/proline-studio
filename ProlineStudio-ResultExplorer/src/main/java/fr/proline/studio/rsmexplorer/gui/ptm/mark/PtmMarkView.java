/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 26 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.PtmSiteAA;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PtmMarkView extends ViewPtmAbstract {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private static final BasicStroke STROKE = new BasicStroke(2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

    /**
     * One letter
     */
    String m_type;
    Color m_color;
    /**
     * location in the protein, to paint above the type figure
     */
    int _locationProtein;
    int _ajustNTermAt1;//useful for N-termini at protein 1;

    public PtmMarkView(PtmSiteAA pAA) {
        this.m_color = pAA.getColor();
        this.m_type = Character.toString(pAA.getPtmTypeChar());
        this._locationProtein = pAA.getModifyLocProtein();
        if (pAA.isNTermAt1()) {
            this._ajustNTermAt1 = 1;
        } else {
            this._ajustNTermAt1 = 0;
        }

    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }

    /**
     * location in a protein where this amino acid of protein begin to show
     *
     * @param g
     * @param locationAjusted
     */
    @Override
    public void paint(Graphics2D g, int locationAjusted, int areaWidth) {

        FontMetrics fm = g.getFontMetrics(ViewSetting.FONT_PTM);
        int descent = fm.getDescent();
        int StringHeight = fm.getHeight();

        this.x0 = this.m_x + (this._locationProtein - locationAjusted - 1 + this._ajustNTermAt1) * ViewSetting.WIDTH_AA;
        //this.x0 = this.m_x + (this._locationProtein - locationAjusted-1) * ViewSetting.WIDTH_AA;
        this.y0 = this.m_y + ViewSetting.WIDTH_AA; //reserve location line

        g.setColor(m_color);
        //draw box
        g.setStroke(STROKE);

        g.drawLine(x0, y0, x0 + ViewSetting.WIDTH_AA, y0);
        g.drawLine(x0 + ViewSetting.WIDTH_AA, y0, x0 + ViewSetting.WIDTH_AA, y0 + ViewSetting.WIDTH_AA);
        g.drawLine(x0, y0, x0, y0 + ViewSetting.WIDTH_AA);
        g.drawLine(x0, y0 + ViewSetting.WIDTH_AA, x0 + ViewSetting.WIDTH_AA, y0 + ViewSetting.WIDTH_AA);
        int yBottom = (int) (y0 + ViewSetting.WIDTH_AA * 1.5);
        int xCenter = x0 + ViewSetting.WIDTH_AA / 2;
        g.drawLine(xCenter, y0 + ViewSetting.WIDTH_AA, xCenter, yBottom);
        g.drawLine(x0, yBottom, x0 + ViewSetting.WIDTH_AA, yBottom);

        //draw PTM Type in the box
        g.setFont(ViewSetting.FONT_PTM);

        int stringWidth = fm.stringWidth("" + m_type);
        // g.drawString(m_type, xCenter - stringWidth / 2, y0 + ViewSetting.WIDTH_AA - descent / 3); //x, y are base line begin x, y
        g.drawString(m_type, xCenter - stringWidth / 2, y0 + ViewSetting.WIDTH_AA - 1); //x, y are base line begin x, y
        //draw the location number upper
        g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        g.setFont(ViewSetting.FONT_NUMBER);
        fm = g.getFontMetrics(ViewSetting.FONT_NUMBER);
        stringWidth = fm.stringWidth("" + _locationProtein);
        if (stringWidth > (ViewSetting.WIDTH_AA - ViewSetting.BORDER_GAP + 3)) {
            Font smallerFont = ViewSetting.FONT_NUMBER_DIAGONAL;
            g.setFont(smallerFont);
            fm = g.getFontMetrics(smallerFont);
            stringWidth = fm.stringWidth("" + _locationProtein);
        }

        g.drawString("" + _locationProtein, xCenter - stringWidth / 2, y0 - descent / 2);

    }

    @Override
    public String toString() {
        return "ViewPtmMark{" + "m_type=" + m_type + ", _locationProtein=" + _locationProtein + ", _ajustNTermAt1=" + _ajustNTermAt1 + '}';
    }
}
