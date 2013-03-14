package fr.proline.studio.markerbar;



public class DefaultMarker extends AbstractMarker {

    public static int BOOKMARK_MARKER = 0;
    public static int ANNOTATION_MARKER = 1;
    
    public DefaultMarker(int row, int type) {
        super(row, true, true, type);
    }
}
