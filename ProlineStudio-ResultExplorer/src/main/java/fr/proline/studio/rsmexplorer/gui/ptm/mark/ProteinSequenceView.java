/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * View of the sequence of a proteine
 *
 * @author Karine XUE
 */
public class ProteinSequenceView extends ViewPtmAbstract {

    private String _sequence;
    private String _sequenceView;

    public ProteinSequenceView() {
        this.x0 = 0;
        this.y0 = 0;
        this._sequence = "";
        this._sequenceView = _sequence;
    }

    public void setSequence(String sequenceProtein) {
        this._sequence = sequenceProtein;
        this._sequenceView = _sequence;
    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.x0 = x;
        this.y0 = y;
    }

    @Override
    public void paint(Graphics2D g, int locationAjusted, int fontWidth) {
        fontWidth = 14;
        if (locationAjusted > _sequence.length()) {
            this._sequenceView = _sequence; 
        } else {
            this._sequenceView = _sequence.substring(locationAjusted);
        }
        g.setColor(ViewSetting.SEQUENCE_COLOR);
        g.setFont(ViewSetting.FONT_SEQUENCE);
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int StringHeight = fm.getHeight();

        for (int i = 0; i < _sequenceView.length(); i++){
            String letter = Character.toString(_sequenceView.charAt(i));
            g.drawString(letter, x0+fontWidth*(i+1), y0 + ascent - descent / 2); //x, y are base line begin x, y
        }
        g.drawString(_sequenceView, x0+fontWidth, y0 + ascent - descent / 2); //x, y are base line begin x, y

    }

}
