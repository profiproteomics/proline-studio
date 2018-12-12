/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSiteAA;
import fr.proline.studio.rsmexplorer.gui.ptm.PtmSitePeptide;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PeptideView extends ViewPtmAbstract {
    
    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private int _length;
    private int _beginIndex; //begin index at the protein level location 
    private ArrayList<PtmSiteAA> _ptmSiteAAList;
    private PtmSitePeptide _peptide;
    private float _score;
    private boolean _isSelected;
    private int _fontWidth = 14;
    
    public PeptideView(PtmSitePeptide pep) {
        this.x0 = 0;
        this.y0 = 0;
        this._peptide = pep;
        this._length = pep.getSequence().length();
        
        _ptmSiteAAList = pep.getPtmSiteAAList();
        _beginIndex = pep.getBeginInProtein();
        _isSelected = false;
    }
    
    @Override
    public void paint(Graphics2D g, int locationAjusted, int fontWidth) {
        _fontWidth = fontWidth;
        //public abstract void drawRoundRect(int x, int y, int width, int height,int arcWidth, int arcHeight);
        this.x0 = this.m_x + fontWidth + (this._beginIndex - locationAjusted) * fontWidth;
        this.y0 = this.m_y;
        int width = this._length * fontWidth;
        int height = ViewSetting.HEIGHT_AA;
        g.setColor(ViewSetting.PEPTIDE_COLOR);
        g.fillRoundRect(x0, y0, width, height, fontWidth, ViewSetting.HEIGHT_AA);
        if (_isSelected == true) {
            g.setColor(ViewSetting.SELECTED_PEPTIDE_COLOR);
            g.setStroke(ViewSetting.STROKE_PEP);
            g.drawRoundRect(x0, y0, width, height, fontWidth, ViewSetting.HEIGHT_AA);
        }
        for (PtmSiteAA modifyA : _ptmSiteAAList) {
            paintPtm(g, modifyA, y0, fontWidth);
        }
        
    }
    
    @Override
    public void setBeginPoint(int x, int y) {
        m_x = x;
        m_y = y;
    }

    /**
     * paint the PTM on a Amino Acide
     *
     * @param g
     * @param modifyA
     * @param y01
     */
    private void paintPtm(Graphics2D g, PtmSiteAA modifyA, int y01, int fontWidth) {
        Color colorOld = g.getColor();
        g.setColor(modifyA.getColorWithProbability());
        int location = modifyA.getModifyLocPep();
        int x1 = (location - 1) * fontWidth;
        if (location == 0) {//N-termmini at 0
            g.fillRoundRect(this.x0 + x1, y01, fontWidth, ViewSetting.HEIGHT_AA, fontWidth / 2, ViewSetting.HEIGHT_AA);
        } else if (location == 1) {
            g.fillRoundRect(this.x0 + x1, y01, fontWidth, ViewSetting.HEIGHT_AA, fontWidth / 2, ViewSetting.HEIGHT_AA);
            g.fillRect(this.x0 + x1 + fontWidth / 2, y01, fontWidth - fontWidth / 2, ViewSetting.HEIGHT_AA);
        } else if (location == this._length) {
            g.fillRect(this.x0 + x1, y01, fontWidth - fontWidth / 2, ViewSetting.HEIGHT_AA);
            g.fillRoundRect(this.x0 + x1, y01, fontWidth, ViewSetting.HEIGHT_AA, fontWidth / 2, ViewSetting.HEIGHT_AA);
            
        } else {
            g.fillRect(this.x0 + x1, y01, fontWidth, ViewSetting.HEIGHT_AA);//draw one Letter wide
        }
        g.setColor(colorOld);
    }
    
    private String getToolTipText(double x, double y) {
        
        PtmSiteAA pa = getSelect(x, y);
        if (pa == null) {
            return "peptide info: ??";
        } else {
            return "" + pa.getPtmSite() + "  PTM site probability :" + pa.getProbability() * 100 + "%";
        }
    }

    //@todo
    private PtmSiteAA getSelect(double x, double y) {
        return null;
    }
    
    public PtmSitePeptide getSelectedPeptide(int compareX) {
        int xRangA = this.x0;
        int xRangZ = this.x0 + this._length * _fontWidth;
        //logger.debug(" element:"+this._peptide.toString()+"("+xRangA+","+xRangZ+")");
        if (compareX > xRangA && compareX < xRangZ) {
            return this._peptide;
        } else {
            return null;
        }
        
    }
    
    void setSelected(boolean b) {
        this._isSelected = b;
    }
}
