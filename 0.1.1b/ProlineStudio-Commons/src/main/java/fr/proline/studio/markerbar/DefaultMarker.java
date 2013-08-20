package fr.proline.studio.markerbar;


/**
 * Default Marker Class for bookmark or annotation marker
 * @author JM235353
 */
public class DefaultMarker extends AbstractMarker {

    public static int BOOKMARK_MARKER = 0;
    public static int ANNOTATION_MARKER = 1;
    
    public DefaultMarker(int row, int type) {
        super(row, true, true, type);
    }
}
