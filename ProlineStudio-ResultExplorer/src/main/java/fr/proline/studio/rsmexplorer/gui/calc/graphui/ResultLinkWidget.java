package fr.proline.studio.rsmexplorer.gui.calc.graphui;

import java.awt.Color;
import java.awt.Graphics;

public class ResultLinkWidget {

    private final DataWidget m_dataRepresentation1;
    private final DataWidget m_dataRepresentation2;

    private final int HEIGHT_START_SEGMENT = 10;
    private final int HEIGHT_RESULT_SEGMENT = 40;

    private int m_centerX;
    private int m_centerY;

    public ResultLinkWidget(DataWidget dataRepresentation1, DataWidget dataRepresentation2) {
        m_dataRepresentation1 = dataRepresentation1;
        m_dataRepresentation2 = dataRepresentation2;
    }

    public int getCenterX() {
        return m_centerX;
    }

    public int getCenterY() {
        return m_centerY;
    }

    public void draw(Graphics g) {
        g.setColor(Color.black);

        int x1 = m_dataRepresentation1.getX() + m_dataRepresentation1.getWidth() / 2;
        int x2 = m_dataRepresentation2.getX() + m_dataRepresentation2.getWidth() / 2;
        int y1 = m_dataRepresentation1.getY() + m_dataRepresentation1.getHeight();
        int y2 = m_dataRepresentation2.getY() + m_dataRepresentation2.getHeight();

        int yHorizontalLine = Math.max(y1, y2) + HEIGHT_START_SEGMENT;

        m_centerX = (x1 + x2) / 2;

        m_centerY = yHorizontalLine + HEIGHT_RESULT_SEGMENT / 2;

        g.drawLine(x1, y1, x1, yHorizontalLine);
        g.drawLine(x2, y2, x2, yHorizontalLine);
        g.drawLine(x1, yHorizontalLine, x2, yHorizontalLine);
        g.drawLine(m_centerX, yHorizontalLine, m_centerX, yHorizontalLine + HEIGHT_RESULT_SEGMENT);
    }
}
