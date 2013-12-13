package fr.proline.studio.utils;

import com.thierry.filtering.TableSelection;
import java.awt.Color;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.AbstractLayoutPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * Table which has a bi-color striping, an ability to select columns viewed and
 * the possibility to display an "histogram" on a column
 *
 * @author JM235353
 */
public class DecoratedTable extends JXTable {

    RelativePainterHighlighter.NumberRelativizer m_relativizer = null;
    
    public DecoratedTable() {

        // allow user to hide/show columns
        setColumnControlVisible(true);

        // highlight one line of two
        addHighlighter(HighlighterFactory.createSimpleStriping());

        TableSelection.installCopyAction(this);
    }

    public void displayColumnAsPercentage(int column) {
        displayColumnAsPercentage(column, AbstractLayoutPainter.HorizontalAlignment.RIGHT);
    }
    public void displayColumnAsPercentage(int column, AbstractLayoutPainter.HorizontalAlignment alignment) {
        // Display of the Score Column as a percentage
        Color base = PaintUtils.setSaturation(Color.GREEN, .7f);
        MattePainter matte = new MattePainter(PaintUtils.setAlpha(base, 125));
        RelativePainterHighlighter highlighter = new RelativePainterHighlighter(matte);
        highlighter.setHorizontalAlignment(alignment);

        m_relativizer = new RelativePainterHighlighter.NumberRelativizer(column, 0, 100);
        highlighter.setRelativizer(m_relativizer);
        highlighter.setHighlightPredicate(new HighlightPredicate.ColumnHighlightPredicate(column));
        addHighlighter(highlighter);

    }
    
    public RelativePainterHighlighter.NumberRelativizer getRelativizer() {
        return m_relativizer;
    }
}
