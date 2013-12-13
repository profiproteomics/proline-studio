package fr.proline.studio.markerbar;

/**
 * Base Class for Markers
 * @author JM235353
 */
public abstract class AbstractMarker {

    private boolean m_visibleInMarkerBar;
    private boolean m_visibleInOverviewBar;
    private int m_row = -1;
    private int m_type = -1;

    public AbstractMarker(int row, boolean visibleInMarkerBar, boolean visibleInOverviewBar, int type) {
        m_row = row;
        m_visibleInMarkerBar = visibleInMarkerBar;
        m_visibleInOverviewBar = visibleInOverviewBar;
        m_type = type;
    }

    public boolean isVisibleInMarkerBar() {
        return m_visibleInMarkerBar;
    }

    public boolean isVisibleInOverviewBar() {
        return m_visibleInOverviewBar;
    }

    public int getRow() {
        return m_row;
    }

    
    public int getType() {
        return m_type;
    }
}
