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
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.rsmexplorer.gui.ptm.*;
import fr.proline.studio.utils.CyclicColorPalette;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PeptideView extends ViewPtmAbstract {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");

    protected int m_length;

    private PTMPeptideInstance m_ptmPeptideInst; 
    protected boolean m_isSelected;
    protected int m_beginIndex;
    Boolean hasNTermPtm = null;
    Boolean hasCTermPtm = null;





    public PeptideView(PTMPeptideInstance pep, boolean displayProtWithOutNtermM) {
        x0 = 0;
        y0 = 0;
        m_isSelected = false;
        m_ptmPeptideInst = pep;
        m_length = 0;
        m_beginIndex = 0;
        
        if (pep != null ) {
            if(pep.getSequence()!=null) 
                m_length = pep.getSequence().length();
                    
            m_beginIndex = pep.getStartPosition();

            if ((m_beginIndex == 1) && displayProtWithOutNtermM)
                m_beginIndex = 0;
        }
        setHasNorCTermModif();
    }

    private void setHasNorCTermModif(){
        hasNTermPtm = false;
        hasCTermPtm = false;
        if(m_ptmPeptideInst != null) {
            m_ptmPeptideInst.getPTMSites().forEach(site -> {
                if (site.isNterm())
                    this.hasNTermPtm = Boolean.TRUE;
                if (site.isCterm())
                    this.hasCTermPtm = Boolean.TRUE;
            });
        }
    }

    private Map<Integer, DPeptidePTM> getPtmPerPosition() {
        Map<Integer, DPeptidePTM> map = m_ptmPeptideInst.getPeptideInstance().getPeptide().getTransientData().getDPeptidePtmMap();
        Map<Integer, DPeptidePTM> correctedMap = new HashMap<>(map.size());
        for (Map.Entry<Integer, DPeptidePTM> ptmPerPos : map.entrySet()) {
            int loc = ptmPerPos.getKey();
            if(hasNTermPtm && loc ==0)
                loc = 1;
            if(hasCTermPtm && loc == -1)
                loc = m_length;
            correctedMap.put(loc, ptmPerPos.getValue());
        }
        return correctedMap;

    }

    protected float getScore() {
            return m_ptmPeptideInst.getRepresentativePepMatch().getScore();
    }
    
    /**
     * paint a peptide
     * @param g
     * @param viewContext 
     */
    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {
        int aaWidth = ViewSetting.WIDTH_AA;

        //this.x0 = (this.m_x + aaWidth + (this.m_beginIndex - viewContext.getAjustedStartLocation()) * aaWidth);
        this.x0 = (this.m_x + aaWidth + this.m_beginIndex * aaWidth);
        this.y0 = this.m_y+1;
        int width = (this.m_length * aaWidth);
        int height = ViewSetting.HEIGHT_AA-1;       
        Color c = getColorWithProbability(ViewSetting.PEPTIDE_COLOR, (float) Math.min(Math.max(getScore(),15f) / 100.0, 1.0));
        g.setColor(c);
        g.fillRoundRect(x0, y0, width, height, aaWidth, ViewSetting.HEIGHT_AA);

        Map<Integer, DPeptidePTM> map = getPtmPerPosition();
        for (Map.Entry<Integer, DPeptidePTM> modifyA : map.entrySet()) {
            paintPtm(g, modifyA.getValue(), modifyA.getKey(), y0);
        }

        if (m_isSelected) {
            g.setColor(ViewSetting.SELECTED_PEPTIDE_COLOR);
            g.setStroke(ViewSetting.STROKE_PEP);
            g.drawRoundRect(x0, y0, width, height, aaWidth, ViewSetting.HEIGHT_AA);
        }

    }

    @Override
    public void setBeginPoint(int x, int y) {
        m_x = x;
        m_y = y;
    }

    /**
     * paint the PTM on a Amino Acide
     * @param g
     * @param ptm
     * @param location
     * @param y01 vertical begin position
     */
    private void paintPtm(Graphics2D g, DPeptidePTM ptm, int location, int y01) {
        int aaWidth = ViewSetting.WIDTH_AA;
        Color colorOld = g.getColor();
        Color c = getColorWithProbability(ViewSetting.getColor(ptm.getIdPtmSpecificity()), getProbability(ptm));
        g.setColor(c);
        int x1 = (location - 1) * aaWidth;
        if (location == 0) {
            g.fillRoundRect(this.x0 + x1, y01, aaWidth, ViewSetting.HEIGHT_AA, aaWidth / 2, ViewSetting.HEIGHT_AA);
        } else if (location == 1) {
            g.fillRoundRect(this.x0 + x1, y01, aaWidth, ViewSetting.HEIGHT_AA, aaWidth / 2, ViewSetting.HEIGHT_AA);
            g.fillRect(this.x0 + x1 + aaWidth / 2, y01, aaWidth - aaWidth / 2, ViewSetting.HEIGHT_AA);
        } else if (location == this.m_length) {
            g.fillRect(this.x0 + x1, y01, aaWidth - aaWidth / 2, ViewSetting.HEIGHT_AA);
            g.fillRoundRect(this.x0 + x1, y01, aaWidth, ViewSetting.HEIGHT_AA, aaWidth / 2, ViewSetting.HEIGHT_AA);

        } else {
            g.fillRect(this.x0 + x1, y01, aaWidth, ViewSetting.HEIGHT_AA);//draw one Letter wide
        }
        g.setColor(colorOld);
    }

    public static Color getColorWithProbability(Color c, Float probability) {
        if (probability != null) {
            float[] hsbvals = new float[3];//Hue Saturation Brightness
            Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbvals);
            Color colorWithProbability = Color.getHSBColor(hsbvals[0], hsbvals[1] * probability, hsbvals[2]);
            return colorWithProbability;
            //return c;
        }
        return CyclicColorPalette.GRAY_TEXT_LIGHT;
    }

    /**
     * get one ptm Probability
     *
     * @param ptm
     * @return
     */
    public Float getProbability(DPeptidePTM ptm) {
        DPeptideMatch pepMatch =  m_ptmPeptideInst.getRepresentativePepMatch();
        DPtmSiteProperties properties = pepMatch.getPtmSiteProperties();
        if (properties != null) {
            String readablePtm = DInfoPTM.getInfoPTMMap().get(ptm.getIdPtmSpecificity()).toReadablePtmString((int) ptm.getSeqPosition());
            Float probability = properties.getMascotProbabilityBySite().get(readablePtm);
            // VDS Workaround test for issue #16643                      
            if (probability == null) {
                readablePtm = DInfoPTM.getInfoPTMMap().get(ptm.getIdPtmSpecificity()).toOtherReadablePtmString((int) ptm.getSeqPosition());
                probability = properties.getMascotProbabilityBySite().get(readablePtm);
                if (probability == null) {
                    // this is a fix modification, set probability to 100%
                    probability = 1.0f;
                }
            }
            return probability;
        }
        return null;
    }
    /**
     * useful for Tooltips
     * @param ptm
     * @return 
     */
    private String getReadablePtmString(DPeptidePTM ptm) {
        DPeptideMatch pepMatch =  m_ptmPeptideInst.getRepresentativePepMatch();
        DPtmSiteProperties properties = pepMatch.getPtmSiteProperties();
        if (properties != null) {
            String readablePtm = DInfoPTM.getInfoPTMMap().get(ptm.getIdPtmSpecificity()).toReadablePtmString((int) ptm.getSeqPosition());
            return readablePtm;
        }
        return null;
    }

    protected String getToolTipText(int x) {
        String s = null;
        if (isSelected(x)) {
            s = "Score: " + getScore();
            int xRangA = x - this.x0;

            int position = xRangA / ViewSetting.WIDTH_AA + 1;
            Map<Integer, DPeptidePTM> ptmPerPosition = getPtmPerPosition();
            DPeptidePTM ptm = ptmPerPosition.get(position);
            if (ptm != null) {
                float prob = getProbability(ptm)*100;
                String readable = getReadablePtmString(ptm);
                s = readable + ", " +String.format("%1$.2f", prob)+"%";;
            }
        }
        return s;
    }

    public boolean isSelected(int compareX) {
        int xRangA = this.x0;
        int xRangZ = this.x0 + this.m_length * ViewSetting.HEIGHT_AA;
        //logger.debug(" element:"+this._peptide.toString()+"("+xRangA+","+xRangZ+")");
        if (compareX > xRangA && compareX < xRangZ) {
            return true;
        } else {
            return false;
        }

    }

    void setSelected(boolean b) {
        this.m_isSelected = b;
    }

}
