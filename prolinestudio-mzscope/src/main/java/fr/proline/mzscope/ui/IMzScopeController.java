/* 
 * Copyright (C) 2019
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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CB205360
 */
public interface IMzScopeController {

    public static String CURRENT_RAWFILE_VIEWER = "currentRawFileViewer";
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    IRawFileViewer getCurrentRawFileViewer();

    IRawFileViewer getRawFileViewer(IRawFile rawFile, boolean setVisible);

    IRawFileViewer getTabbedMultiRawFileViewer();
    
    void displayFeatures(Map<String, List<IFeature>> features);
}
