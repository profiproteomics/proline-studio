package fr.proline.mzscope.mzml;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.model.IExportParameters;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.model.QCMetrics;
import fr.proline.mzscope.model.Spectrum;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MzMLRawFile implements IRawFile {

   private static Logger logger = LoggerFactory.getLogger(MzMLRawFile.class);
   private File mzMLFile;
   private List<Scan> scans;

   public MzMLRawFile(File file) {
      mzMLFile = file;
      init();
   }

   private void init() {
      try {
         long start = System.currentTimeMillis();
         logger.info("Start reading mzMLRawFile "+mzMLFile.getAbsolutePath());
         scans = mzMLReader.read(mzMLFile.getAbsolutePath());
         logger.info("File mzML read in :: " + (System.currentTimeMillis() - start) + " ms");
      } catch (Exception e) {
         logger.error("cannot read file " + mzMLFile.getAbsolutePath(), e);
      }
   }

    @Override
    public String getName() {
        return mzMLFile.getName();
    }
    
    @Override
    public File getFile(){
       return mzMLFile;
    }

   
    public Chromatogram getTIC() {
      throw new UnsupportedOperationException("Not supported yet.");      
   }
    
   @Override
   public Chromatogram getBPI() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
    
   @Override
   public Chromatogram getXIC(MsnExtractionRequest params) {
      Chromatogram chromatogram  = XICExtractor.extract(scans, (float)params.getMinMz(), (float)params.getMaxMz());      
      return chromatogram;      
   }
  
   @Override
   public Spectrum getSpectrum(int spectrumIndex) {
      return toModelSpectrum(scans.get(spectrumIndex));
   }

   @Override
   public int getSpectrumId(double retentionTime) {
      for (Scan s : scans) {
         if (Math.abs(s.getRetentionTime() - retentionTime) < 0.001) 
            return s.getIndex();
      }
      return 0;
   }

   @Override
   public int getNextSpectrumId(int spectrumIndex, int msLevel) {
      return (int)Math.min(spectrumIndex+1, scans.size());
   }

   @Override
   public int getPreviousSpectrumId(int spectrumIndex, int msLevel) {
      return (int)Math.max(spectrumIndex-1, 0);
   }

   private fr.proline.mzscope.model.Spectrum toModelSpectrum(Scan mzmlScan) {
      
      double[] masses = new double[mzmlScan.getMasses().length];
      for (int k = 0; k < masses.length; k++) {
         masses[k] = (double)mzmlScan.getMasses()[k];
      }
      return new fr.proline.mzscope.model.Spectrum(mzmlScan.getIndex(), mzmlScan.getRetentionTime(), masses, mzmlScan.getIntensities(), 1);
   }

   @Override
   public List<IFeature> extractFeatures(FeaturesExtractionRequest params) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

    @Override
    public List<Float> getMsMsEvent(double minMz, double maxMz) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   @Override
   public int getSpectrumCount() {
      return scans.size();
   }
   
   @Override
    public boolean exportRawFile(String mgfFileName, IExportParameters exportParams ){
       return true;
    }
    
    @Override
    public boolean isDIAFile(){
        return false;
    }

    @Override
    public Map<String, Object> getFileProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QCMetrics getFileMetrics() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    
}
