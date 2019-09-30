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
 * Draw 1 PTM
 * @author Karine XUE
 */
public class PTMMarkView extends ViewPtmAbstract {

    private static Logger m_logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");

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


        FontMetrics fm = g.getFontMetrics(ViewSetting.FONT_PTM);
        Color oldColor = g.getColor();
        
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        //this.x0 = this.m_x + (getLocationProtein() - viewContext.getAjustedStartLocation()) * ViewSetting.WIDTH_AA;
        this.x0 = this.m_x + getLocationProtein() * ViewSetting.WIDTH_AA;
        this.y0 = this.m_y + ViewSetting.HEIGHT_AA; //reserve location line        
        int xWidthAA = x0+ViewSetting.WIDTH_AA;
        int yHeightAA = y0 + ViewSetting.HEIGHT_AA;
        int yBottom = (int) (y0 + ViewSetting.HEIGHT_AA * 1.5);
        int xCenter = (int) (x0 + ViewSetting.WIDTH_AA / 2);
        
        g.setColor(m_color);        
        //TEST CODE
        if(m_mark.isPTMNorCterm()){        
            int[] xPtm = {x0, xWidthAA, x0+ViewSetting.WIDTH_AA/2};
            int[] yPtm = {yHeightAA, yHeightAA, (int)(y0 + (ViewSetting.HEIGHT_AA*1.5))};
            g.fillPolygon(xPtm, yPtm, yPtm.length);
        } else {
            //draw the box
            g.setStroke(ViewSetting.STROKE);
            g.drawLine(x0, y0, xWidthAA, y0);
            g.drawLine(xWidthAA, y0, xWidthAA, yHeightAA);
            g.drawLine(xWidthAA, yHeightAA, x0, yHeightAA);
            g.drawLine(x0, yHeightAA, x0, y0);
            
            g.drawLine(xCenter, yHeightAA, xCenter, yBottom);
            g.drawLine(x0, yBottom, xWidthAA, yBottom);

            //draw PTM Type in the box
            g.setFont(ViewSetting.FONT_PTM);
            int stringWidth = fm.stringWidth(m_symbol);
            // assume that letters are squared: do not use ascent or height but reuse font width to position ptm letter in the middle of the box
            g.drawString(m_symbol, xCenter - stringWidth / 2, yHeightAA - (ViewSetting.HEIGHT_AA - stringWidth) / 2); //x, y are base line begin x, y
        }
        
        //draw the location number upper
        //g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        if(!m_mark.isPTMNorCterm() ||(m_mark.isPTMNorCterm() && viewContext.isNCtermIndexShown()) ){
            g.setColor(Color.BLACK);
            g.setFont(ViewSetting.FONT_NUMBER);
            fm = g.getFontMetrics(ViewSetting.FONT_NUMBER);
            int descent = fm.getDescent();

            int stringWidth = fm.stringWidth(String.valueOf(getLocationProtein()));
            if (stringWidth > (ViewSetting.WIDTH_AA - ViewSetting.BORDER_GAP + 3)) {
                Font smallerFont = ViewSetting.FONT_NUMBER_DIAGONAL;
                g.setFont(smallerFont);
                fm = g.getFontMetrics(smallerFont);
                stringWidth = fm.stringWidth(String.valueOf(getLocationProtein()));
            }
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawString(String.valueOf(getDisplayedLocationProtein()), xCenter - stringWidth / 2, y0 - descent / 2);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        
        g.setColor(oldColor);
    }

    @Override
    public String toString() {
        return "ViewPtmMark{" + "m_type=" + m_symbol + ", _locationProtein=" + getLocationProtein() + ", m_displayedLocationProtein=" +getDisplayedLocationProtein()+ '}';
    }
}
