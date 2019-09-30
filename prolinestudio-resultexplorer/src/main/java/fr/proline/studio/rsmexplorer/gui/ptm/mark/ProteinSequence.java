/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 *
 * @author Karine XUE
 */
public class ProteinSequence extends ViewPtmAbstract {

    String m_sequence;
    String m_sequenceView;
    /**
     * the PTM Site Position on the proteine sequence
     */
    private int m_ptmSeqPos;

    public ProteinSequence() {
        this.x0 = 0;
        this.y0 = 0;
    }

    public void setData(String s) {
        m_sequence = s;
        m_sequenceView = m_sequence;
    }

    /**
     * set graphic begin location
     *
     * @param x
     * @param y
     */
    @Override
    public void setBeginPoint(int x, int y) {
        this.x0 = x;
        this.y0 = y;
    }

    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {
        if (m_sequence == null)
            return;
        int aaWidth = ViewSetting.WIDTH_AA;
        int aaHeight = ViewSetting.HEIGHT_AA;
        int adjusteStartLoc = 0;
        int adjusteEndLoc = m_sequence.length();
        if (adjusteEndLoc >= m_sequence.length() || adjusteEndLoc <= 0) {
            adjusteEndLoc = m_sequence.length();
        }

        if (adjusteStartLoc > m_sequence.length()) {
            this.m_sequenceView = m_sequence.substring(0, adjusteEndLoc);
        } else {
            if (adjusteEndLoc <= adjusteStartLoc) {
                adjusteEndLoc = m_sequence.length();
            }
            this.m_sequenceView = m_sequence.substring(adjusteStartLoc, adjusteEndLoc);
        }

        // For debug only
//        g.setColor(Color.lightGray);
//        for (int i = 0; i < _sequenceView.length(); i++){
//            String letter = Character.toString(_sequenceView.charAt(i));
//            g.drawRect((int)(x0 + aaWidth *(i+1)), y0, (int)(aaWidth), ViewSetting.HEIGHT_AA);
//        }
        g.setFont(ViewSetting.FONT_SEQUENCE);
        g.setColor(ViewSetting.SEQUENCE_COLOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(m_sequenceView, (x0 + aaWidth), y0 + ViewSetting.HEIGHT_AA); //x, y are base line begin x, y
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        //g.drawRect(x0+aaWidth*(this._pTMSeqPos-adjuste), y0, aaWidth, ViewSetting.HEIGHT_AA);
        if (m_ptmSeqPos >= 0) {
            int xPtmA = x0 + aaWidth * (this.m_ptmSeqPos - adjusteStartLoc);
            g.setColor(Color.red);
            int[] xPtm = {xPtmA, xPtmA + aaWidth, xPtmA + aaWidth / 2};
            int yPtmA = y0 + aaHeight + 2;
            int[] yPtm = {yPtmA, yPtmA, yPtmA + aaHeight / 2};
            g.fillPolygon(xPtm, yPtm, yPtm.length);
        }
    }

    public void setPTMSequencePosition(int i) {
        this.m_ptmSeqPos = i;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(m_sequenceView);
        if (m_ptmSeqPos >= 0) {
            sb.append("PTMSite @ " + m_ptmSeqPos);
        }
        return sb.toString();
    }
}
