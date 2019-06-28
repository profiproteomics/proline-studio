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
    * Returns the File object correspondng to this raw file on disk.
    * 
    * @return the File object correspondng to this raw file 
    */
   public File getFile();

   /**
    * 
    * @param params
    * @return 
    */
   public IChromatogram getXIC(MsnExtractionRequest params);
   
   /**
    * Returns the Total Ion IChromatogram of this rax file.
    * @return 
    */
   public IChromatogram getTIC();

   /**
    * Returns the Base Peak IChromatogram.
    * 
    * @return 
    */
   public IChromatogram getBPI();

   /**
    * Extract the features list from this raw file.
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

   public double[] getElutionTimes(int msLevel);

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
   
}
