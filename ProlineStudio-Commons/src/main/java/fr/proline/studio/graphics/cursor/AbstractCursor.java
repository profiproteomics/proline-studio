package fr.proline.studio.graphics.cursor;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.MoveableInterface;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author JM235353
 */
public abstract class AbstractCursor implements MoveableInterface {

    public final static Color CURSOR_COLOR = new Color(160,82,45);
    public final static BasicStroke LINE2_STROKE = new BasicStroke(2.0f);
    
    protected BasePlotPanel m_plotPanel;

    public AbstractCursor(BasePlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }

    public abstract void paint(Graphics2D g);
}
