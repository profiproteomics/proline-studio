/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.dam.tasks.data.ptm.PTMSitePeptideInstance;
import fr.proline.studio.rsmexplorer.gui.ptm.*;
import fr.proline.studio.utils.CyclicColorPalette;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Iterator;
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
     // VDS FIXME: One or the other may be specified : PTMPeptideInstance or PTMSitePeptideInstance
    private boolean isPtmSiteView;
    private PTMSitePeptideInstance m_ptmSitePeptideInst;
    private PTMPeptideInstance m_ptmPeptideInst; 
    protected boolean m_isSelected;
    protected int m_beginIndex;

    public PeptideView(PTMSitePeptideInstance pep) {
        isPtmSiteView = true;
        this.x0 = 0;
        this.y0 = 0;
        this.m_ptmSitePeptideInst = pep;
        m_ptmPeptideInst = pep.getParentPTMPeptideInstance();
        String sequence = m_ptmPeptideInst.getSequence();
        if (sequence != null) {
            this.m_length = m_ptmPeptideInst.getSequence().length();
        } else {
            this.m_length = 0;
        }
        this.m_beginIndex = m_ptmPeptideInst.getStartPosition();
        if ((m_beginIndex == 1) && (pep.getSite().isProteinNTermWithOutM())) {
            m_beginIndex = 0;
        }
        m_isSelected = false;
    }
    
    public PeptideView(PTMPeptideInstance pep) {
        isPtmSiteView = false;
        x0 = 0;
        y0 = 0;
        m_isSelected = false;
        m_ptmPeptideInst = pep;
        m_ptmSitePeptideInst = null;
        m_length = 0;
        m_beginIndex = 0;
        
        if (pep != null ) {
            if(pep.getSequence()!=null) 
                m_length = pep.getSequence().length();
                    
            m_beginIndex = pep.getStartPosition();
            boolean isProteinNTerm = false;
            Iterator<PTMSite> allSites = pep.getSites().iterator();
            while (allSites.hasNext()) {
                PTMSite site = allSites.next();
                if(site.isProteinNTermWithOutM()) {
                    isProteinNTerm = true;
                    break;
                }
            }
            if ((m_beginIndex == 1) && isProteinNTerm)  
                m_beginIndex = 0;   
        }
        m_isSelected = false;
    }
    
    public PeptideView(PTMPeptideInstance pep, boolean displayProtWithOutNtermM) {
        isPtmSiteView = false;
        x0 = 0;
        y0 = 0;
        m_isSelected = false;
        m_ptmPeptideInst = pep;
        m_ptmSitePeptideInst = null;
        m_length = 0;
        m_beginIndex = 0;
        
        if (pep != null ) {
            if(pep.getSequence()!=null) 
                m_length = pep.getSequence().length();
                    
            m_beginIndex = pep.getStartPosition();

            if ((m_beginIndex == 1) && displayProtWithOutNtermM)
                m_beginIndex = 0;   
        }
        m_isSelected = false;
    }
    
    private Map<Integer, DPeptidePTM> getPosPtmMap() {
        return m_ptmPeptideInst.getPeptideInstance().getPeptide().getTransientData().getDPeptidePtmMap();
    }

    protected float getScore() {
        if(isPtmSiteView)
            return m_ptmSitePeptideInst.getBestPeptideMatch().getScore();
        else
            return m_ptmPeptideInst.getBestPepMatch().getScore();
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
        Color c = getColorWithProbability(ViewSetting.PEPTIDE_COLOR, (float) Math.min((Math.max(getScore(),15f) - 15) / 100.0, 1.0));
        g.setColor(c);
        g.fillRoundRect(x0, y0, width, height, aaWidth, ViewSetting.HEIGHT_AA);
        Map<Integer, DPeptidePTM> map = getPosPtmMap();
        for (Map.Entry<Integer, DPeptidePTM> modifyA : map.entrySet()) {
            paintPtm(g, modifyA.getValue(), modifyA.getKey(), y0);
        }

        if (m_isSelected == true) {
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
        DPeptideMatch pepMatch =  isPtmSiteView ? m_ptmSitePeptideInst.getBestPeptideMatch() : m_ptmPeptideInst.getBestPepMatch();
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
        DPeptideMatch pepMatch =  isPtmSiteView ? m_ptmSitePeptideInst.getBestPeptideMatch() : m_ptmPeptideInst.getBestPepMatch();
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
            Map<Integer, DPeptidePTM> map = getPosPtmMap();
            DPeptidePTM ptm = (DPeptidePTM) map.get(position);
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
