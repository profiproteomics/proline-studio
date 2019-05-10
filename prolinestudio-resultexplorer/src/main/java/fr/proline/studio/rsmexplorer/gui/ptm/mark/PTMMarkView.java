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

    private PTMMark m_mark;
    /**
     * One letter
     */
    private String m_symbol;
    private Color m_color;    
    
    public PTMMarkView(PTMMark mark) {
        m_mark = mark;
        this.m_color = ViewSetting.getColor(mark);
        this.m_symbol = Character.toString(mark.getPtmSymbol());

    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }

    public int getLocationProtein() {
        return m_mark.getProteinLocation();
    }
    
    public int getDisplayedLocationProtein() {
        return m_mark.getProteinLocationToDisplay();
    }

    public String getPTMShortName() {
        return m_mark.getPtmShortName();
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
        this.x0 = this.m_x + (getLocationProtein() - viewContext.getAjustedStartLocation()) * ViewSetting.WIDTH_AA;
        this.y0 = this.m_y + ViewSetting.HEIGHT_AA; //reserve location line
        //TEST CODE
        if(m_mark.isPTMNorCterm()){
            g.setColor(m_color);
            int[] xPtm = {x0, x0+ViewSetting.WIDTH_AA, x0+ViewSetting.WIDTH_AA/2};
            int[] yPtm = {y0 + ViewSetting.HEIGHT_AA, y0 + ViewSetting.HEIGHT_AA, (int)(y0 + (ViewSetting.HEIGHT_AA*1.5))};
            g.fillPolygon(xPtm, yPtm, yPtm.length);
        }
        
        //OK Code
        if(!m_mark.isPTMNorCterm() ){
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
            if(!m_mark.isPTMNorCterm() ||(m_mark.isPTMNorCterm() && viewContext.isNCtermIndexShown()) ){
                g.setColor(Color.BLACK);
                g.setFont(ViewSetting.FONT_NUMBER);
                fm = g.getFontMetrics(ViewSetting.FONT_NUMBER);
                int descent = fm.getDescent();

                stringWidth = fm.stringWidth(String.valueOf(getLocationProtein()));
                if (stringWidth > (aaWidth - ViewSetting.BORDER_GAP + 3)) {
                    Font smallerFont = ViewSetting.FONT_NUMBER_DIAGONAL;
                    g.setFont(smallerFont);
                    fm = g.getFontMetrics(smallerFont);
                    stringWidth = fm.stringWidth(String.valueOf(getLocationProtein()));
                }
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.drawString(String.valueOf(getDisplayedLocationProtein()), xCenter - stringWidth / 2, y0 - descent / 2);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            }
        }
        g.setColor(oldColor);
    }

    @Override
    public String toString() {
        return "ViewPtmMark{" + "m_type=" + m_symbol + ", _locationProtein=" + getLocationProtein() + ", m_displayedLocationProtein=" +getDisplayedLocationProtein()+ '}';
    }
}
