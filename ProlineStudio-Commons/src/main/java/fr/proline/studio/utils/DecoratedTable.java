package fr.proline.studio.utils;

import java.awt.Color;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * 
 * @author JM235353
 */
public class DecoratedTable extends JXTable {

    public DecoratedTable() {
        // only one row can be selected
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // allow user to hide/show columns
        setColumnControlVisible(true);

        // allow the user to search the table
        //table.setSearchable(null); //JPM.TODO

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
