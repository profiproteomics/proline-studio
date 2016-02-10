package fr.proline.mzscope.model;

import fr.profi.mzdb.model.Feature;
import java.io.File;
import java.util.List;

/**
 *
 * @author CB205360
 */
public interface IRawFile {
   
   public String getName();
   
   public File getFile();

   public Chromatogram getXIC(MsnExtractionRequest params);
   
   public Chromatogram getTIC();

   public Chromatogram getBPI();

   public List<IFeature> extractFeatures(FeaturesExtractionRequest params);
   
   public Spectrum getSpectrum(int spectrumIndex);

   public int getSpectrumCount();
   
   public int getSpectrumId(double retentionTime);
   
   public int getNextSpectrumId(int spectrumIndex, int msLevel);
   
   public int getPreviousSpectrumId(int spectrumIndex, int msLevel);
   
   /**
    * return the list of MS/MS events times (sec) for the specified mass range
    * @param minMz
    * @param maxMz
    * @return 
    */
   public List<Float> getMsMsEvent(double minMz, double maxMz);
   
   public boolean exportRawFile(String outFileName, IExportParameters exportParams );
   
   public boolean isDIAFile();
}
