package fr.proline.studio.rsmexplorer.gui.xic.alignment;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PanAxisGesture;
import fr.proline.studio.graphics.PlotBaseAbstract;
import fr.proline.studio.graphics.SelectionGestureLasso;
import fr.proline.studio.graphics.ZoomGesture;
import fr.proline.studio.rsmexplorer.gui.xic.MapAlignmentPanel;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Karine XUE
 */
public class AlignmentPlotPanel extends BasePlotPanel {

    private static final Logger m_logger = LoggerFactory.getLogger(AlignmentPlotPanel.class);

    private MapAlignmentPanel m_control;
    
    public AlignmentPlotPanel(MapAlignmentPanel parentPanel) {
        super();
        m_control = parentPanel;
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        if (m_drawCursor) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        boolean mustRepaint = false;

        int x = e.getX();
        int y = e.getY();

        if (e.isPopupTrigger() && !m_panAxisGesture.isPanning() && !m_zoomGesture.isZooming()) {
            if (m_xAxis != null && m_yAxis != null && m_xAxis.inside(x, y)) {
                m_xAxis.setSelected(true);
                m_yAxis.setSelected(false);
                JPopupMenu popup = createAxisPopup(m_xAxis, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            } else if (m_xAxis != null && m_yAxis != null && m_yAxis.inside(x, y)) {
                m_xAxis.setSelected(false);
                m_yAxis.setSelected(true);
                JPopupMenu popup = createAxisPopup(m_yAxis, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            }
        } else if (!m_zoomGesture.isZooming() && !m_selectionGesture.isSelecting() && !m_moveGesture.isMoving() && (m_panAxisGesture.getAction() != PanAxisGesture.ACTION_PAN)) {

            if (m_xAxis != null && m_xAxis.inside(x, y)) {
                mustRepaint |= m_xAxis.setSelected(!m_xAxis.isSelected());
                mustRepaint |= m_yAxis.setSelected(false);
            } else if (m_yAxis != null && m_yAxis.inside(x, y)) {
                mustRepaint |= m_xAxis.setSelected(false);
                mustRepaint |= m_yAxis.setSelected(!m_yAxis.isSelected());
            }
            if (mustRepaint) {
                repaint();
            }
        }

        if (m_moveGesture.isMoving()) {

            int modifier = e.getModifiers();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);

            m_moveGesture.stopMoving(x, y, isCtrlOrShiftDown);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            m_updateDoubleBuffer = true;
            mustRepaint = true;
        } else if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.stopSelection(x, y);

            int modifier = e.getModifiers();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);

            int action = m_selectionGesture.getAction();
            if (action == SelectionGestureLasso.ACTION_CLICK) {
                Point p = m_selectionGesture.getClickPoint();
                double valueX = m_xAxis.pixelToValue(p.x);
                double valueY = m_yAxis.pixelToValue(p.y);

                if (!m_plots.isEmpty()) {
                    m_plots.get(0).select(valueX, valueY, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true;
                mustRepaint = true;
            } else if (action == SelectionGestureLasso.ACTION_SURROUND) {

                Path2D.Double path = new Path2D.Double();

                Polygon p = m_selectionGesture.getSelectionPolygon();
                double valueX = m_xAxis.pixelToValue(p.xpoints[0]);
                double valueY = m_yAxis.pixelToValue(p.ypoints[0]);
                path.moveTo(valueX, valueY);
                for (int i = 1; i < p.npoints; i++) {
                    valueX = m_xAxis.pixelToValue(p.xpoints[i]);
                    valueY = m_yAxis.pixelToValue(p.ypoints[i]);
                    path.lineTo(valueX, valueY);
                }
                path.closePath();

                double xMin = m_xAxis.pixelToValue(m_selectionGesture.getMinX());
                double xMax = m_xAxis.pixelToValue(m_selectionGesture.getMaxX());
                double yMin = m_yAxis.pixelToValue(m_selectionGesture.getMaxY()); // inversion of min max is normal between pixel and Y Axis
                double yMax = m_yAxis.pixelToValue(m_selectionGesture.getMinY());

                if (!m_plots.isEmpty()) {
                    m_plots.get(0).select(path, xMin, xMax, yMin, yMax, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true;
                mustRepaint = true;
            }

        } else if (m_zoomGesture.isZooming()) {
            m_zoomGesture.stopZooming(e.getX(), e.getY());

            int action = m_zoomGesture.getAction();
            if (action == ZoomGesture.ACTION_ZOOM) {
                double oldMinX = m_xAxis.getMinValue();
                double oldMaxX = m_xAxis.getMaxValue();
                double oldMinY = m_yAxis.getMinValue();
                double oldMaxY = m_yAxis.getMaxValue();
                m_xAxis.setRange(m_xAxis.pixelToValue(m_zoomGesture.getStartX()), m_xAxis.pixelToValue(m_zoomGesture.getEndX()));
                m_yAxis.setRange(m_yAxis.pixelToValue(m_zoomGesture.getEndY()), m_yAxis.pixelToValue(m_zoomGesture.getStartY()));
                m_updateDoubleBuffer = true;
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
            } else if (action == ZoomGesture.ACTION_UNZOOM) {
                if (!m_plots.isEmpty()) {
                    for (PlotBaseAbstract p: m_plots)
                        p.update();//use the calculated min max, not the initial user defined min max
                    double oldMinX = m_xAxis.getMinValue();
                    double oldMaxX = m_xAxis.getMaxValue();
                    double oldMinY = m_yAxis.getMinValue();
                    double oldMaxY = m_yAxis.getMaxValue();
                    m_control.updateZoomButton();
                    updateAxis(m_plots.get(0));
                    fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
                }
                m_updateDoubleBuffer = true;
            } else if (e.isPopupTrigger()) { // action == ZoomGesture.ACTION_NONE
                // WART, in fact we are not zooming, so if it is the right mouse button,
                // the user wants the popup menu
                if (!m_plots.isEmpty()) {
                    PlotBaseAbstract plot = m_plots.get(0);
                    double xValue = m_xAxis.pixelToValue(e.getX());
                    double yValue = m_yAxis.pixelToValue(e.getY());

                    JPopupMenu popup = plot.getPopupMenu(xValue, yValue);
                    if (popup != null) {
                        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                    }
                }
            }

            mustRepaint = true;

        } else if (m_panAxisGesture.isPanning()) {
            m_panAxisGesture.stopPanning(e.getX(), e.getY());
        }

        if (mustRepaint) {
            repaint();
        }
    }


}
