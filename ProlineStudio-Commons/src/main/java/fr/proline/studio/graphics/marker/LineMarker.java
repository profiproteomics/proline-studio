package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.*;

/**
 *
 * @author JM235353
 */
public class LineMarker extends AbstractMarker {

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;
    
    private double m_value;
    private int m_orientation;
    
    public LineMarker(PlotPanel plotPanel, double value, int orientation) {
        super(plotPanel);
        
        m_value = value;
        m_orientation = orientation;
    }
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2 = (Graphics2D) g;
        Stroke prevStroke = g2.getStroke();
        g2.setStroke(dashed);
        

        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        
        if (m_orientation == ORIENTATION_HORIZONTAL) {
            int y = yAxis.valueToPixel(m_value);
            int x1 = xAxis.valueToPixel(xAxis.getMinTick());
            int x2 = xAxis.valueToPixel(xAxis.getMaxTick());

            g.setColor(Color.gray);
            g.drawLine(x1, y, x2, y);


        } else if (m_orientation == ORIENTATION_VERTICAL) {
            int x = xAxis.valueToPixel(m_value);
            int y1 = yAxis.valueToPixel(yAxis.getMinTick());
            int y2 = yAxis.valueToPixel(yAxis.getMaxTick());
            
            g.setColor(Color.gray);
            g.drawLine(x, y1, x, y2);
        }
        
        // restore stroke
        g2.setStroke(prevStroke);
        
    }
    private final static float dash1[] = {10.0f};
    private final static BasicStroke dashed = new BasicStroke(1.0f,  BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
}
