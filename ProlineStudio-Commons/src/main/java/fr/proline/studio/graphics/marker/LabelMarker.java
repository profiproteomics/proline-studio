package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.MoveableInterface;
import java.awt.*;

/**
 * Marker to display text at a specific position
 *
 * @author JM235353
 */
public class LabelMarker extends AbstractMarker implements MoveableInterface {

    public static final int ORIENTATION_X_LEFT = 0;
    public static final int ORIENTATION_X_RIGHT = 1;
    public static final int ORIENTATION_XY_MIDDLE = 2;
    public static final int ORIENTATION_Y_TOP = 3;
    public static final int ORIENTATION_Y_BOTTOM = 4;
    private static final Font MARKER_FONT = new Font("Arial", Font.PLAIN, 12);
    private String m_valueLabel = null;
    private final AbstractCoordinates m_coordinates;
    private final int m_orientationX;
    private final int m_orientationY;
    private static final int LINE_DELTA = 18;
    private final static int COLOR_WIDTH = 5;
    private final static int DELTA_COLOR = 4;

    private int m_xBox;
    private int m_yBox;
    private int m_heightBox;
    private int m_widthBox;
    
    // show a color point in the marker
    private Color m_labelColor = null;

    public LabelMarker(BasePlotPanel plotPanel, AbstractCoordinates coordinates, String valueLabel, int orientationX, int orientationY) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_coordinates = coordinates;
        m_orientationX = orientationX;
        m_orientationY = orientationY;
    }

     public LabelMarker(BasePlotPanel plotPanel, AbstractCoordinates coordinates, String valueLabel, int orientationX, int orientationY, Color labelColor) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_coordinates = coordinates;
        m_orientationX = orientationX;
        m_orientationY = orientationY;
        this.m_labelColor = labelColor;
    }

    private boolean isPaintColor() {
        return this.m_labelColor != null;
    }

    @Override
    public void paint(Graphics2D g) {

        int pixelX = m_coordinates.getPixelX(m_plotPanel);
        int pixelY = m_coordinates.getPixelY(m_plotPanel);

        int deltaX;
        switch (m_orientationX) {
            case ORIENTATION_X_LEFT:
                deltaX = -LINE_DELTA;
                break;
            case ORIENTATION_X_RIGHT:
                deltaX = LINE_DELTA;
                break;
            case ORIENTATION_XY_MIDDLE:
            default:
                deltaX = 0;
                break;
        }
        
        int deltaY;
        switch (m_orientationY) {
            case ORIENTATION_Y_TOP:
                deltaY = -LINE_DELTA;
                break;
            case ORIENTATION_Y_BOTTOM:
                deltaY = LINE_DELTA;
                break;
            case ORIENTATION_XY_MIDDLE:
            default:
                deltaY = 0;
                break;
        }

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


        int delta = 0;

        if (m_orientationX == ORIENTATION_X_RIGHT) {
            m_xBox = pixelX + deltaX;
            m_yBox = pixelY + deltaY - stringHeight / 2 - DELTA;
            m_heightBox = stringHeight + DELTA * 2;
            m_widthBox = stringWidth ;
            g.setColor(new Color(255, 255, 255, 196));
            g.fillRect(m_xBox, m_yBox, m_widthBox, m_heightBox);
            g.setColor(Color.black);
            g.drawRect(m_xBox, m_yBox, m_widthBox, m_heightBox);
            if (isPaintColor()) {
                g.setColor(m_labelColor);
                g.fillRect(m_xBox + DELTA_COLOR, m_yBox+((m_heightBox-COLOR_WIDTH) /2) , COLOR_WIDTH, COLOR_WIDTH);
                delta = 2 * DELTA_COLOR + COLOR_WIDTH;
                g.setColor(Color.black);
            } else {
                delta = DELTA;
            }
            g.drawString(m_valueLabel, m_xBox + delta, m_yBox + metrics.getAscent());
        } else if ((m_orientationX == ORIENTATION_X_LEFT) || (m_orientationX == ORIENTATION_XY_MIDDLE)) {
            m_xBox = pixelX + deltaX - stringWidth ;
            m_yBox = pixelY + deltaY - stringHeight / 2 - DELTA;
            m_heightBox = stringHeight + DELTA * 2;
            m_widthBox = stringWidth  ;
            g.setColor(new Color(255, 255, 255, 128));
            g.fillRect(m_xBox, m_yBox, m_widthBox, m_heightBox);
            g.setColor(Color.black);
            g.drawRect(m_xBox, m_yBox, m_widthBox, m_heightBox);
            if (isPaintColor()) {
                g.setColor(m_labelColor);
                g.fillRect(m_xBox + DELTA_COLOR,  m_yBox+((m_heightBox-COLOR_WIDTH) /2), COLOR_WIDTH, COLOR_WIDTH);
                delta = 2 * DELTA_COLOR + COLOR_WIDTH;
                g.setColor(Color.black);
            } else {
                delta = DELTA;
            }
            g.drawString(m_valueLabel, m_xBox + delta, m_yBox + metrics.getAscent());
        }

    }

    @Override
    public boolean inside(int x, int y) {
        return (x>=m_xBox) && (x<=m_xBox+m_widthBox) && (y>=m_yBox) && (y<=m_yBox+m_heightBox);
    }

    @Override
    public void move(int deltaX, int deltaY) {
        m_coordinates.setPixelPosition(m_plotPanel, m_coordinates.getPixelX(m_plotPanel)+deltaX, m_coordinates.getPixelY(m_plotPanel)+deltaY);
    }

    @Override
    public boolean isMoveable() {
        return (m_orientationX == ORIENTATION_XY_MIDDLE);
    }

}
