/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 26 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.PTMMark;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;

import java.awt.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PTMMarkView extends ViewPtmAbstract {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");

    /**
     * One letter
     */
    private String m_symbol;
    private Color m_color;
    /**
     * location in the protein, to paint above the type figure
     */
    private int m_locationProtein;
    //int _ajustNTermAt1;//useful for N-termini at protein 1;
    /**
     * location in the protein, to paint above the type figure
     */
    private int m_displayedLocationProtein;
    
    private boolean m_isNCtermPTM;

    public PTMMarkView(PTMMark mark) {
        this.m_color = ViewSetting.getColor(mark);
        this.m_symbol = Character.toString(mark.getPtmSymbol());
        this.m_locationProtein = mark.getProteinLocation();
        m_displayedLocationProtein = mark.getProteinLocationToDisplay();
        m_isNCtermPTM = mark.isPTMNorCterm();
    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }

    public int getLocationProtein() {
        return m_locationProtein;
    }
    
    public int getDisplayedLocationProtein() {
        return m_displayedLocationProtein;
    }

    /**
     * location in a protein where this amino acid of protein begin to show
     *
     * @param g
     * @param viewContext
     */
    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {

        int aaWidth = ViewSetting.WIDTH_AA;

        FontMetrics fm = g.getFontMetrics(ViewSetting.FONT_PTM);
        Color oldColor = g.getColor();

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //this.x0 = (int)(this.m_x + aaWidth+ (this._locationProtein - viewContext.getAjustedLocation()-1 + this._ajustNTermAt1) * aaWidth);
        this.x0 = this.m_x + (this.m_locationProtein - viewContext.getAjustedStartLocation()) * ViewSetting.WIDTH_AA;
        this.y0 = this.m_y + ViewSetting.HEIGHT_AA; //reserve location line

        g.setColor(m_color);
        //draw the box
        g.setStroke(ViewSetting.STROKE);
        g.drawLine(x0, y0, (int) (x0 + aaWidth), y0);
        g.drawLine((int) (x0 + aaWidth), y0, (int) (x0 + aaWidth), y0 + ViewSetting.HEIGHT_AA);
        g.drawLine((int) (x0 + aaWidth), y0 + ViewSetting.HEIGHT_AA, x0, y0 + ViewSetting.HEIGHT_AA);
        g.drawLine(x0, y0 + ViewSetting.HEIGHT_AA, x0, y0);

        int yBottom = (int) (y0 + ViewSetting.HEIGHT_AA * 1.5);
        int xCenter = (int) (x0 + aaWidth / 2);
        g.drawLine(xCenter, y0 + ViewSetting.HEIGHT_AA, xCenter, yBottom);
        g.drawLine(x0, yBottom, x0 + ViewSetting.HEIGHT_AA, yBottom);

        //draw PTM Type in the box
        g.setFont(ViewSetting.FONT_PTM);
        int stringWidth = fm.stringWidth(m_symbol);
        // assume that letters are squared: do not use ascent or height but reuse font width to position ptm letter in the middle of the box
        g.drawString(m_symbol, xCenter - stringWidth / 2, y0 + ViewSetting.HEIGHT_AA - (ViewSetting.HEIGHT_AA - stringWidth) / 2); //x, y are base line begin x, y

        //draw the location number upper
        //g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        if(!m_isNCtermPTM ||(m_isNCtermPTM && viewContext.isNCtermIndexShown()) ){
            g.setColor(Color.BLACK);
            g.setFont(ViewSetting.FONT_NUMBER);
            fm = g.getFontMetrics(ViewSetting.FONT_NUMBER);
            int descent = fm.getDescent();

            stringWidth = fm.stringWidth(String.valueOf(m_locationProtein));
            if (stringWidth > (aaWidth - ViewSetting.BORDER_GAP + 3)) {
                Font smallerFont = ViewSetting.FONT_NUMBER_DIAGONAL;
                g.setFont(smallerFont);
                fm = g.getFontMetrics(smallerFont);
                stringWidth = fm.stringWidth(String.valueOf(m_locationProtein));
            }
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawString(String.valueOf(m_displayedLocationProtein), xCenter - stringWidth / 2, y0 - descent / 2);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        g.setColor(oldColor);
    }

    @Override
    public String toString() {
        return "ViewPtmMark{" + "m_type=" + m_symbol + ", _locationProtein=" + m_locationProtein + ", m_displayedLocationProtein=" +m_displayedLocationProtein+ '}';
    }
}
