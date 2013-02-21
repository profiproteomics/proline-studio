package fr.proline.studio.markerbar;

public abstract class AbstractMarker {

    private boolean visibleInMarkerBar;
    private boolean visibleInOverviewBar;
    private int rowStart = -1;
    private int rowEnd = -1;
    private int type = -1;

    public AbstractMarker(int rowStart, int rowEnd, boolean visibleInMarkerBar, boolean visibleInOverviewBar, int type) {
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.visibleInMarkerBar = visibleInMarkerBar;
        this.visibleInOverviewBar = visibleInOverviewBar;
        this.type = type;
    }

    public boolean isVisibleInMarkerBar() {
        return visibleInMarkerBar;
    }

    public boolean isVisibleInOverviewBar() {
        return visibleInOverviewBar;
    }

    public int getRowStart() {
        return rowStart;
    }

    public int getRowend() {
        return rowEnd;
    }
    
    public int getType() {
        return type;
    }
}
