/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.PTMMark;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draw 1 PTM
 * @author Karine XUE
 */
public class PTMMarkView extends ViewPtmAbstract {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");

    private PTMMark m_mark;
    private final List<PTMMark> m_aaLocMarks;
    private final List<PTMMark> m_cOrNtermMarks;

    private boolean hasMultipleAAPTMSpecif; // Multiple PTM on AA Specificity should be represented
    private boolean hasMultipleCorNPTMSpecif; // Multiple PTM on C/N term Specificity should be represented

    /**
     * One letter
     */
    private final String m_symbol;
    private final Color m_color;
    
    public PTMMarkView(List<PTMMark> marks) {
        if(marks == null || marks.isEmpty())
            throw new RuntimeException("Empty PTMMark list !!");
        m_cOrNtermMarks = new ArrayList<>();
        m_aaLocMarks = new ArrayList<>();
        m_mark = null;

        boolean samePtmSpecifId = true;
        boolean sameCorNTermPtmSpecifId = true;
        Long prevAAPtmSpecifId = null;
        Long prevCorNtermSpecifId = null;
        for(PTMMark pm : marks){
            if(pm.isPTMNorCterm()) {
                m_cOrNtermMarks.add(pm);
                if (prevCorNtermSpecifId == null)
                    prevCorNtermSpecifId = pm.getPtmSpecificityId();
                else sameCorNTermPtmSpecifId = prevCorNtermSpecifId.equals(pm.getPtmSpecificityId());
            } else {
                m_aaLocMarks.add(pm);
                if (prevAAPtmSpecifId == null)
                    prevAAPtmSpecifId = pm.getPtmSpecificityId();
                else samePtmSpecifId = prevAAPtmSpecifId.equals(pm.getPtmSpecificityId());
                if(m_mark == null)
                    m_mark = pm;
            }
        }

        if(m_mark ==null)
            m_mark = marks.get(0); //No none N/C term PTM, get first one as reference
        hasMultipleAAPTMSpecif = !samePtmSpecifId;
        hasMultipleCorNPTMSpecif = ! sameCorNTermPtmSpecifId;

        if(!m_aaLocMarks.isEmpty() && hasMultipleAAPTMSpecif) {
            this.m_color = ViewSetting.getMultiPTMColor();
            this.m_symbol = "*";
        } else if(!m_aaLocMarks.isEmpty() && !hasMultipleAAPTMSpecif) {
            this.m_color = ViewSetting.getColor(m_mark);
            this.m_symbol = Character.toString(m_mark.getPtmSymbol());
        } else if(!m_cOrNtermMarks.isEmpty() && hasMultipleCorNPTMSpecif) {
            this.m_color = ViewSetting.getMultiPTMColor();
            this.m_symbol = "*";
        } else { // if(!m_cOrNtermMarks.isEmpty() && !hasMultipleCorNPTMSpecif) {
            this.m_color = ViewSetting.getColor(m_mark);
            this.m_symbol = Character.toString(m_mark.getPtmSymbol());
        }
    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }

    public boolean isNCTerm(){
        return !m_cOrNtermMarks.isEmpty(); //m_mark.isPTMNorCterm();
    }

    public int getLocationProtein() {
        return m_mark.getProteinLocation();
    }
    
    public int getDisplayedLocationProtein() {
        return m_mark.getProteinLocationToDisplay();
    }

    public String getPTMShortName() {
        if(hasMultipleAAPTMSpecif)
            return "Multiple PTMs";
        else
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
//        int xWidth3On4AA = x0+(ViewSetting.WIDTH_AA*3/4);
//        int yUpPtm = (int)(y0 + (ViewSetting.HEIGHT_AA * 0.5));
//        int yCenterAddPtm = (int)(y0 + (ViewSetting.HEIGHT_AA * 1.25));
        int xCenter = x0 + ViewSetting.WIDTH_AA / 2;
        g.setColor(m_color);        

        if(m_aaLocMarks.isEmpty()){
            int[] xPtm = {x0, xWidthAA, xCenter };
            int[] yPtm = {yHeightAA, yHeightAA, yBottom};
//            int[] xPtm = {xWidth3On4AA, x0, xWidth3On4AA/*, x0+ViewSetting.WIDTH_AA/2*/};
//            int[] yPtm = {yUpPtm, yHeightAA, yBottom};/*yHeightAA, (int)(y0 + (ViewSetting.HEIGHT_AA*1.5))};*/
            g.fillPolygon(xPtm, yPtm, yPtm.length);
        } else {
            //draw the box
            g.setStroke(ViewSetting.STROKE);
            g.drawLine(x0, y0, xWidthAA, y0);
            g.drawLine(xWidthAA, y0, xWidthAA, yHeightAA);
            g.drawLine(xWidthAA, yHeightAA, x0, yHeightAA);
            g.drawLine(x0, yHeightAA, x0, y0);

            if(isNCTerm()){
                Color aaColor = g.getColor();
                Color cnTermColor = ViewSetting.getMultiPTMColor();
                if(!hasMultipleCorNPTMSpecif){
                    cnTermColor=ViewSetting.getColor(m_cOrNtermMarks.get(0));
                }

                g.setColor(cnTermColor);
                int[] xPtm = {x0, xWidthAA, xCenter};
                int[] yPtm = {yHeightAA, yHeightAA, yBottom};
                g.fillPolygon(xPtm, yPtm, yPtm.length);
                g.setColor(aaColor);
            } else {
                g.drawLine(xCenter, yHeightAA, xCenter, yBottom);
                g.drawLine(x0, yBottom, xWidthAA, yBottom);
            }

            //draw PTM Type in the box
            g.setFont(ViewSetting.FONT_PTM);
            int stringWidth = fm.stringWidth(m_symbol);
            // assume that letters are squared: do not use ascent or height but reuse font width to position ptm letter in the middle of the box
            g.drawString(m_symbol, xCenter - stringWidth / 2, yHeightAA - (ViewSetting.HEIGHT_AA - stringWidth) / 2); //x, y are base line begin x, y
        }
        
        //draw the location number upper
        //g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        if( !m_aaLocMarks.isEmpty() || (m_aaLocMarks.isEmpty() && viewContext.isNCtermIndexShown()) ){
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
