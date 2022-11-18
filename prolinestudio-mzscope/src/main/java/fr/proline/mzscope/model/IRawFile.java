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
package fr.proline.mzscope.model;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This interface must be implemented for each file format to make them compatible with mzScope. 
 * 
 * @author CB205360
 */
public interface IRawFile {
    
    /**
     * Return this raw file name.
     * 
     * @return the raw file name.
     */
   public String getName();
   
   /**
    * Returns the File object corresponding to this raw file on disk.
    * 
    * @return the File object corresponding to this raw file
    */
   public File getFile();

   /**
    * 
    * @param params
    * @return 
    */
   public IChromatogram getXIC(ExtractionRequest params);
   
   /**
    * Returns the Total Ion IChromatogram of this rax file.
    * @return
    * @param msLevel
    */
   public IChromatogram getTIC(int msLevel);

   /**
    * Returns the Base Peak IChromatogram.
    * 
    * @return 
    */
   public IChromatogram getBPI();

   /**
    * Extract the list of peakels from this raw file.
    * 
    * @param params
    * @return 
    */
   public List<IPeakel> extractPeakels(FeaturesExtractionRequest params);

   /**
    * Extract the list of peakels from this raw file.
    *
    * @param params
    * @return
    */
   public List<IFeature> extractFeatures(FeaturesExtractionRequest params);

   /**
    * Returns the spectrum corresponding to the specified index.
    * 
    * @param spectrumIndex
    * @return 
    */
   public Spectrum getSpectrum(int spectrumIndex);

   /**
    * Return the total number of spectra.
    * 
    * @return the total number of spectra.
    */
   public int getSpectrumCount();
   
   /**
    * Returns the spectrum index corresponding to the specified retention time. Return 0 if none of the spectrum 
    * correspond to the specified retention time.
    * 
    * @param retentionTime
    * @return the spectrum Id corresponding to the specified retention time.
    */
   public int getSpectrumId(double retentionTime);

   /**
    * Returns an array of elution times for all spectrum of the specified msLevel
    *
    * @param msLevel
    * @return
    */
   public double[] getElutionTimes(int msLevel);

   /**
    * Returns the elution time of the specified spectrum index.
    *
    * @param spectrumIndex
    * @return
    */
   public double getSpectrumElutionTime(int spectrumIndex);

    /**
     * Returns the index of the spectrum following the spectrumIndex and corresponding to the specified MS level.
     *
     * @param spectrumIndex
     * @param msLevel
     * @return The Index of the following spectrum
     */
   public int getNextSpectrumId(int spectrumIndex, int msLevel);
   
   /**
    * Returns the Index of the spectrum preceding the spectrumIndex and corresponding to the specified MS level.
    * 
    * @param spectrumIndex
    * @param msLevel
    * @return The Index of the preceding spectrum.
    */
   public int getPreviousSpectrumId(int spectrumIndex, int msLevel);
   
   /**
    * Return the list of MS/MS events times (sec) for the specified mass range.
    * 
    * @param minMz
    * @param maxMz
    * @return 
    */
   public List<Float> getMsMsEvent(double minMz, double maxMz);
   
   /**
    * Exports this Raw File. The export content is specified by the exportParams parameter.
    * 
    * @param outFileName
    * @param exportParams
    * @return true if the raw file est cussessfully exported.
    */
   public boolean exportRawFile(String outFileName, IExportParameters exportParams );
   
   /**
    * Return true if this file is a DIA file.
    * 
    * @return true if this file is a DIA file.
    */
   public boolean isDIAFile();

   public boolean hasIonMobilitySeparation();


   /**
    * Returns as a Map a set of proerties associated with this raw file. 
    * 
    * @return 
    */
   public Map<String, Object> getFileProperties();

   /**
    * Returns as QC metric object containing metrics calculated from this raw file. 
    * 
    * @return 
    */
   public QCMetrics getFileMetrics();

   /*
   * Close IRawFile : called when file will be closed / removed from file list.
   * All necessary resouces should be closed here
   */
   public void closeIRawFile();

   IonMobilityIndex getIonMobilityIndex();
}
