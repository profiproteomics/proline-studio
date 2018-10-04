package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * marker to display an interval on the graph (2 verticals lines and a colored rectangle)
 * @author MB243701
 */
public class IntervalMarker extends AbstractMarker{
    private double m_startValue;
    private double m_endValue;
    private LineMarker m_lineMarkerStart;
    private LineMarker m_lineMarkerEnd;
    private Color m_colorArea;
    private Color m_lineColor;
    
    public IntervalMarker(BasePlotPanel plotPanel, Color colorArea, Color lineColor, double startValue, double endValue) {
        super(plotPanel);
        this.m_colorArea = colorArea;
        this.m_lineColor = lineColor;
        this.m_startValue = startValue;
        this.m_endValue = endValue;
        m_lineMarkerStart = new LineMarker(plotPanel, startValue, lineColor);
        m_lineMarkerEnd = new LineMarker(plotPanel, endValue,lineColor);
    }
    
    @Override
    public void paint(Graphics2D g) {
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis(); 
        int sx = xAxis.valueToPixel(m_startValue);
        int ex = xAxis.valueToPixel(m_endValue);
        int y0 =  yAxis.getY();
        int y1 =  yAxis.getHeight();
        
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,  0.1f));
        g.setColor(m_colorArea);
        g.fillRect(sx, y0, ex-sx,y1);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,  1f));
        m_lineMarkerStart.paint(g);
        m_lineMarkerEnd.paint(g);
    }

    @Override
    public AbstractMarker clone(BasePlotPanel plotPanel) throws CloneNotSupportedException {
        IntervalMarker clone =  (IntervalMarker)super.clone(plotPanel); 
        clone.m_lineMarkerEnd = (LineMarker)m_lineMarkerEnd.clone(plotPanel);
        clone.m_lineMarkerStart = (LineMarker)m_lineMarkerStart.clone(plotPanel);
        return clone;
    }
    
    
}
