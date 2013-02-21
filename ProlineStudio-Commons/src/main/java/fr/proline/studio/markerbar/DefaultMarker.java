package fr.proline.studio.markerbar;

import java.awt.Color;
import java.awt.Graphics;

public class DefaultMarker extends AbstractMarker {

    public static int BOOKMARK_MARKER = 0;
    public static int ANNOTATION_MARKER = 1;
    
    public DefaultMarker(int rowStart, int rowEnd, int type) {
        super(rowStart, rowEnd, true, true, type);
    }
}
