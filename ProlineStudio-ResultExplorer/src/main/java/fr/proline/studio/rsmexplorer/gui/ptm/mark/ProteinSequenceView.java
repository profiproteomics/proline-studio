/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;

import java.awt.*;

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
    public void paint(Graphics2D g, ViewContext viewContext) {

        int aaWidth = ViewSetting.WIDTH_AA;

        if (viewContext.getAjustedLocation() > _sequence.length()) {
            this._sequenceView = _sequence;
        } else {
            this._sequenceView = _sequence.substring(viewContext.getAjustedLocation());
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
        g.drawString(_sequenceView, (x0 + aaWidth), y0 + ViewSetting.HEIGHT_AA); //x, y are base line begin x, y
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

}
