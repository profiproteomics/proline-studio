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

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.utils.Display;
import fr.proline.mzscope.utils.MzScopeCallback;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CB205360
 */
public interface IRawFileViewer {

   final public static String LAST_EXTRACTION_REQUEST = "lastExtractionRequest";

   /**
    * Returns the current RawFile ie the raw file associated and selected with the current viewer (see below)
    *
    * @return the RawFile associated with this viewer.
    */
   public IRawFile getCurrentRawfile();

   /**
    * Returns all the RawFiles of the current viewer
    *
    * @return the RawFile associated with this viewer.
    */
   public List<IRawFile> getAllRawfiles();

   /**
    * Extract data according to the specified parameters and display the results in this viewer. The specified
    * callback is called in the AWT event dispatch thread when the display finishes.
    * 
    * @param params the extraction parameters
    * @param display the display mode to use
    * @param callback that must be called back after extraction.
    */
   public void extractAndDisplay(ExtractionRequest params, Display display, MzScopeCallback callback);

   /**
    * Display the chromatogram and return the plot color
    * 
    * @param chromatogram the chromatogram to display
    * @param display parameters used to display the specified chromatogram
    * @return the color used to display the sepecified c
    */ 
   public Color displayChromatogram(IChromatogram chromatogram, Display display);

   /**
    * Display the supplied chromatograms
    *
    * @param chromatogramByRawFile
    */
   public void displayChromatograms(Map<IRawFile, IChromatogram> chromatogramByRawFile, Display display);

   /**
    * Display the specified Feature in this component.
    *
    * @param f : the feature to be displayed
    */
   public void displayPeakel(IPeakel f);
   
   /**
    * Display the scan specified by the index parameter.
    * 
    * @param index 
    */
   public void displayScan(long index);

   /**
    *
    *
    * @param spectrum
    * @param scaleFactor
    */
   public void setReferenceSpectrum(Spectrum spectrum, Float scaleFactor);

   /**
    * Returns the active IChromatogram ie the chromatogram that will be used to select or iterate over scans.
    * 
    * @return 
    */
   public IChromatogram getCurrentChromatogram();

   /**
    * Returns all Chromatograms displayed by this component
    *
    * @return an iterator over all Chromatograms displayed by this component
    */
   public Iterable<IChromatogram> getAllChromatograms();

   /**
    * Returns the color associated with the specified rawFilename
    * 
    * @param rawFilename
    * @return 
    */
   public Color getPlotColor(String rawFilename);
   
   /**
    * Returns the mode used by this component to display XIC.
    * 
    *  @return the mode used by this component to display XIC.
    */
   public Display.Mode getChromatogramDisplayMode();

   /**
    * Returns the Spectrum currently represented by this viewer.
    *
    * @return the Spectrum currently represented by this viewer.
    */
   public Spectrum getCurrentSpectrum();

   void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

   void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

   public  void changeForceFittedToCentroid();
}
