package fr.proline.studio.markerbar;

public abstract class AbstractMarker {

    private boolean visibleInMarkerBar;
    private boolean visibleInOverviewBar;
    private int row = -1;
    private int type = -1;

    public AbstractMarker(int row, boolean visibleInMarkerBar, boolean visibleInOverviewBar, int type) {
        this.row = row;
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

    public int getRow() {
        return row;
    }

    
    public int getType() {
        return type;
    }
}
