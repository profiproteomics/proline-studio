/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.utils;

import fr.proline.studio.graphics.marker.AbstractMarker;
import java.sql.Timestamp;
import java.util.List;

/**
 *
 * @author CB205360
 */
public class Display {
    
    public enum Mode { REPLACE, OVERLAY, SERIES };

    private Mode mode;
    private List<AbstractMarker> markers;
    private String identifier;
            
    public Display(Mode mode) {
        this(mode, new Timestamp(System.currentTimeMillis()).toString());
    }

    public Display(Mode mode, List<AbstractMarker> markers) {
        this(mode, markers, new Timestamp(System.currentTimeMillis()).toString());
    }

    public Display(Mode mode, List<AbstractMarker> markers, String identifier) {
        this.mode = mode;
        this.markers = markers;
        this.identifier = identifier;
    }

    public Display(Mode mode, String identifier) {
        this.mode = mode;
        this.identifier = identifier;
    }

    
    public Display(List<AbstractMarker> markers) {
        this(Mode.REPLACE, markers);
    }
    
    public Mode getMode() {
        return mode;
    }

    public List<AbstractMarker> getMarkers() {
        return markers;
    }

    public String getIdentifier() {
        return identifier;
    }
    
}
