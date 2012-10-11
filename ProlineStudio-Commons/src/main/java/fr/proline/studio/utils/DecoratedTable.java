package fr.proline.studio.utils;

import java.awt.Color;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * Table which has a bi-color striping, an hability to select columns viewed and
 * the possibility to display an "histogram" on a column
 *
 * @author JM235353
 */
public class DecoratedTable extends JXTable {

    public DecoratedTable() {

        // allow user to hide/show columns
        setColumnControlVisible(true);

        // highlight one line of two
        addHighlighter(HighlighterFactory.createSimpleStriping());

    }

    public void displayColumnAsPercentage(int column) {
        // Display of the Score Column as a percentage
        Color base = PaintUtils.setSaturation(Color.GREEN, .7f);
        MattePainter matte = new MattePainter(PaintUtils.setAlpha(base, 125));
        RelativePainterHighlighter highlighter = new RelativePainterHighlighter(matte);
        highlighter.setRelativizer(new RelativePainterHighlighter.NumberRelativizer(column, 0, 100));
        highlighter.setHighlightPredicate(new HighlightPredicate.ColumnHighlightPredicate(column));
        addHighlighter(highlighter);

    }
}
