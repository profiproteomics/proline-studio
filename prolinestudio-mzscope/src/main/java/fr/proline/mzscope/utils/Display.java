/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
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
