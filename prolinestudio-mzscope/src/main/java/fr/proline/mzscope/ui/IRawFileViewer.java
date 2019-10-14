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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.utils.Display;
import fr.proline.mzscope.utils.MzScopeCallback;
import java.awt.Color;

/**
 *
 * @author CB205360
 */
public interface IRawFileViewer {
   
   /**
    * Extract a IChromatogram according to the specified parameters and display the results in this panel. The specified 
    * callback is called in the AWT event dispatch thread when the display finished.
    * 
    * @param params the XIC extraction parameters 
    * @param display the display mode to use
    * @param callback that must be called back after XIC extraction.
    */
   public void extractAndDisplayChromatogram(MsnExtractionRequest params, Display display, MzScopeCallback callback);
   /**
    * Display the chromatogram and return the plot color
    * 
    * @param chromato the chromatogram to display 
    * @param display parameters used to display the specified chromatogram
    * @return the color used to display the sepecified c
    */ 
   public Color displayChromatogram(IChromatogram chromato, Display display);
   
   /**
    * Display the specified Feature in this component.
    * 
    * @param f : the feature to be displayed
    */
   public void displayFeature(IFeature f);
   
   /**
    * Display the scan specified by the index parameter.
    * 
    * @param index 
    */
   public void displayScan(long index);

   /**
    * Returns the current RawFile ie the the raw file associated with the current IChromatogram  (see below)
    * 
    * @return 
    */
   public IRawFile getCurrentRawfile();

   /**
    * Returns the active IChromatogram ie the chromatogram that will be used to select or iterate over scans.
    * 
    * @return 
    */
   public IChromatogram getCurrentChromatogram();
   
   /**
    * Returns the color associated with the specified rawFilename
    * 
    * @param rawFilename
    * @return 
    */
   public Color getPlotColor(String rawFilename);
   
   /**
    * Returns the mode used by this component to display XIC
    * 
    *  @return the mode used by this component to display XIC.
    */
   public Display.Mode getXicDisplayMode();
   
   /**
    * Returns all Chromatograms displayed by this component
    * 
    * @return an iterator over all Chromatograms displayed by this component
    */
   public Iterable<IChromatogram> getAllChromatograms();
   
}
