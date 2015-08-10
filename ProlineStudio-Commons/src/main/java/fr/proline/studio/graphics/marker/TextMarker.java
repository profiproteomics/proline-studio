package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.*;

/**
 * Marker to display text at a % position in the Plot Panel
 * @author JM235353
 */
public class TextMarker extends AbstractMarker {
    
    private String m_valueLabel = null;
    private double m_percentageX;
    private double m_percentageY;
    
    private static final Font MARKER_FONT = new Font("Arial", Font.BOLD, 16);
    
    public TextMarker(BasePlotPanel plotPanel, double percentageX, double percentageY, String valueLabel) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_percentageX = percentageX;
        m_percentageY = percentageY;

    }


    @Override
    public void paint(Graphics2D g) {

        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();

        double xMax = xAxis.getMaxValue();
        double xMin = xAxis.getMinValue();
        double yMax = yAxis.getMaxValue();
        double yMin = yAxis.getMinValue();
        
        int pixelX = xAxis.valueToPixel(xMin+(xMax-xMin)*m_percentageX);
        int pixelY = yAxis.valueToPixel(yMin+(yMax-yMin)*m_percentageY);


        g.setFont(MARKER_FONT);

        g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        g.drawString(m_valueLabel, pixelX, pixelY);

        
    }
    
    
}
