package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.*;

/**
 * Marker to display text at a specific position
 *
 * @author JM235353
 */
public class LabelMarker extends AbstractMarker {

    public static final int ORIENTATION_X_LEFT = 0;
    public static final int ORIENTATION_X_RIGHT = 1;
    public static final int ORIENTATION_Y_TOP = 2;
    public static final int ORIENTATION_Y_BOTTOM = 3;
    private static final Font MARKER_FONT = new Font("Arial", Font.PLAIN, 12);
    private String m_valueLabel = null;
    private double m_x;
    private double m_y;
    private int m_orientationX;
    private int m_orientationY;
    private static final int LINE_DELTA = 18;
    private final static int COLOR_WIDTH = 5;
    private final static int DELTA_COLOR = 4;

    // show a color point in the marker
    private Color m_labelColor = null;

    public LabelMarker(PlotPanel plotPanel, double x, double y, String valueLabel, int orientationX, int orientationY) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_x = x;
        m_y = y;
        m_orientationX = orientationX;
        m_orientationY = orientationY;
    }

    public LabelMarker(PlotPanel plotPanel, double x, double y, String valueLabel, int orientationX, int orientationY, Color labelColor) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_x = x;
        m_y = y;
        m_orientationX = orientationX;
        m_orientationY = orientationY;
        this.m_labelColor = labelColor;
    }

    private boolean isPaintColor() {
        return this.m_labelColor != null;
    }

    @Override
    public void paint(Graphics2D g) {

        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();

        int pixelX = xAxis.valueToPixel(m_x);
        int pixelY = yAxis.valueToPixel(m_y);

        int deltaX = (m_orientationX == ORIENTATION_X_LEFT) ? -LINE_DELTA : (m_orientationX == ORIENTATION_X_RIGHT) ? LINE_DELTA : 0;
        int deltaY = (m_orientationY == ORIENTATION_Y_TOP) ? -LINE_DELTA : (m_orientationY == ORIENTATION_Y_BOTTOM) ? LINE_DELTA : 0;

        final int DELTA = 3;
        
        g.setFont(MARKER_FONT);
        FontMetrics metrics = g.getFontMetrics();
        int stringWidth = metrics.stringWidth(m_valueLabel);
        int stringHeight = metrics.getHeight();
        stringWidth += DELTA * 2;
        if (isPaintColor()) {
            stringWidth += 2 * DELTA_COLOR + COLOR_WIDTH;
        }

        g.setColor(Color.black);
        g.drawLine(pixelX, pixelY, pixelX + deltaX, pixelY + deltaY);


        int deltaC = 0;

        if (m_orientationX == ORIENTATION_X_RIGHT) {
            int xBox = pixelX + deltaX;
            int yBox = pixelY + deltaY - stringHeight / 2 - DELTA;
            int heightBox = stringHeight + DELTA * 2;
            int widthBox = stringWidth ;
            g.setColor(new Color(255, 255, 255, 196));
            g.fillRect(xBox, yBox, widthBox, heightBox);
            g.setColor(Color.black);
            g.drawRect(xBox, yBox, widthBox, heightBox);
            if (isPaintColor()) {
                g.setColor(m_labelColor);
                g.fillRect(xBox + DELTA_COLOR, yBox+((heightBox-COLOR_WIDTH) /2) , COLOR_WIDTH, COLOR_WIDTH);
                deltaC = DELTA_COLOR;
                g.setColor(Color.black);
            }
            g.drawString(m_valueLabel, xBox + DELTA + deltaC, yBox + metrics.getAscent());
        } else if (m_orientationX == ORIENTATION_X_LEFT) {
            int xBox = pixelX + deltaX - stringWidth ;
            int yBox = pixelY + deltaY - stringHeight / 2 - DELTA;
            int heightBox = stringHeight + DELTA * 2;
            int widthBox = stringWidth  ;
            g.setColor(new Color(255, 255, 255, 128));
            g.fillRect(xBox, yBox, widthBox, heightBox);
            g.setColor(Color.black);
            g.drawRect(xBox, yBox, widthBox, heightBox);
            if (isPaintColor()) {
                g.setColor(m_labelColor);
                g.fillRect(xBox + DELTA_COLOR,  yBox+((heightBox-COLOR_WIDTH) /2), COLOR_WIDTH, COLOR_WIDTH);
                deltaC = DELTA_COLOR;
                g.setColor(Color.black);
            }
            g.drawString(m_valueLabel, xBox + DELTA + deltaC, yBox + metrics.getAscent());
        } else if (m_orientationY == ORIENTATION_Y_TOP) {
            int xBox = pixelX + deltaX - stringWidth / 2 - DELTA;
            int yBox = pixelY + deltaY - stringHeight - DELTA * 2;
            int heightBox = stringHeight + DELTA * 2;
            int widthBox = stringWidth  ;
            g.setColor(new Color(255, 255, 255, 128));
            g.fillRect(xBox, yBox, widthBox, heightBox);
            g.setColor(Color.black);
            g.drawRect(xBox, yBox, widthBox, heightBox);
            if (isPaintColor()) {
                g.setColor(m_labelColor);
                g.fillRect(xBox + DELTA_COLOR,  yBox+((heightBox-COLOR_WIDTH) /2), COLOR_WIDTH, COLOR_WIDTH);
                deltaC = DELTA_COLOR;
                g.setColor(Color.black);
            }
            g.drawString(m_valueLabel, xBox + DELTA + deltaC, yBox + metrics.getAscent());
        } else if (m_orientationY == ORIENTATION_Y_BOTTOM) {
            int xBox = pixelX + deltaX - stringWidth / 2 - DELTA;
            int yBox = pixelY + deltaY;
            int heightBox = stringHeight + DELTA * 2;
            int widthBox = stringWidth ;
            g.setColor(new Color(255, 255, 255, 128));
            g.fillRect(xBox, yBox, widthBox, heightBox);
            g.setColor(Color.black);
            g.drawRect(xBox, yBox, widthBox, heightBox);
            if (isPaintColor()) {
                g.setColor(m_labelColor);
                g.fillRect(xBox + DELTA_COLOR,  yBox+((heightBox-COLOR_WIDTH) /2), COLOR_WIDTH, COLOR_WIDTH);
                deltaC = DELTA_COLOR;
                g.setColor(Color.black);
            }
            g.drawString(m_valueLabel, xBox + DELTA + deltaC, yBox + metrics.getAscent());
        }

    }

}
