/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.timstof;

import fr.proline.brucker.timstof.TimstofReader;
import fr.proline.brucker.timstof.model.TimsFrame;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.model.IChromatogram;
import fr.proline.mzscope.model.IExportParameters;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.model.QCMetrics;
import fr.proline.mzscope.model.Spectrum;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class TimstofRawFile implements IRawFile{

    private  File m_ttDirFile;
    private TimstofReader m_reader;
    private Long m_fileHandler;
    private List<TimsFrame> m_ttFrames;
    private Map<Integer, Integer> m_spectra2FrameIndex;
    private Map<Integer, Integer> m_frame2FirstSpectraIndex;
    private IChromatogram m_ticChromato;
    private IChromatogram m_bpcChromato;
//    private  File m_ttBinFile;
//    private  File m_ttSqliteFile;
    
    private static final Logger LOG = LoggerFactory.getLogger(TimstofRawFile.class);
    
    public TimstofRawFile(File ttFile){
        long start = System.currentTimeMillis();
        if(!ttFile.isDirectory())
           throw new IllegalArgumentException("You should specify a brucker .d directory");
        
        m_ttDirFile = ttFile;
//        // Get TT bin files
//        File[] binFiles = ttFile.listFiles((File dir, String name) -> "analysis.tdf_bin".equalsIgnoreCase(name));        
//        if(binFiles != null && binFiles.length >0 ){
//            m_ttBinFile = binFiles[0];
//        }else{
//            throw new IllegalArgumentException("You should select a valid brucker .d directory (containing analysis.tdf_bin)");
//        }
//        
//        // Get TT Sqlite files
//        File[] sqliteFiles = ttFile.listFiles((File dir, String name) -> "analysis.tdf".equalsIgnoreCase(name));        
//        if(sqliteFiles != null && binFiles.length >0 ){
//            m_ttSqliteFile = sqliteFiles[0];
//        }
//        long end = System.currentTimeMillis();
//        LOG.info(" Read dir "+ttFile.getAbsolutePath()+". Duration "+ ((end-start)/1000));
        
        init();        
    }
    
    private void init() {
        
        m_reader = TimstofReader.getTimstofReader();
        m_fileHandler = m_reader.openTimstofFile(m_ttDirFile);
        LOG.info(" Open file handle "+m_fileHandler);
        m_ttFrames = m_reader.getFullTimsFrames(m_fileHandler);
        Collections.sort(m_ttFrames);
                
        //Init indexex map
        Integer spectrumIndex = 0;
        m_spectra2FrameIndex = new HashMap<>();
        m_frame2FirstSpectraIndex = new HashMap<>();
        for(TimsFrame tf : m_ttFrames){
            Integer nbrSpectrum = (!tf.isPasef()) ? 1 : tf.getPasefMsMSData().size();
            
            
            m_frame2FirstSpectraIndex.put(tf.getId(),spectrumIndex);
            for(int i=0;i<nbrSpectrum; i++){
                m_spectra2FrameIndex.put(spectrumIndex, tf.getId());
                spectrumIndex++;
            }
        }        
    }
    
    @Override
    public String getName() {
       return m_ttDirFile.getName()+"TEST ";
    }

    @Override
    public File getFile() {
        return m_ttDirFile;
    }

    @Override
    public IChromatogram getXIC(MsnExtractionRequest params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IChromatogram getTIC() {
        if(m_ticChromato == null){
            double[] intensities = new double[m_ttFrames.size()];
            double[] times = new double[m_ttFrames.size()];
            int index =0;
            for(TimsFrame frame : m_ttFrames ){
                times[index] = frame.getTime()/60; //Set time in minutes
                intensities[index] = frame.getSummedIntensity().doubleValue();
                index++;
            }
            m_ticChromato = new Chromatogram(getName(), getName(),times,intensities);            
        }
        return m_ticChromato;
    }

    @Override
    public IChromatogram getBPI() {
        if(m_bpcChromato == null){
            double[] intensities = new double[m_ttFrames.size()];
            double[] times = new double[m_ttFrames.size()];
            int index =0;
            for(TimsFrame frame : m_ttFrames ){
                times[index] = frame.getTime()/60; //Set time in minutes
                intensities[index] = frame.getMaxIntensity().doubleValue();
                index++;
            }
            m_bpcChromato = new Chromatogram(getName(), getName(),times,intensities);            
        }
        return m_bpcChromato;       
    }

    @Override
    public List<IFeature> extractFeatures(FeaturesExtractionRequest params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Spectrum getSpectrum(int spectrumIndex) {
        Integer frameId = m_spectra2FrameIndex.get(spectrumIndex);
        LOG.info("GET Spectrum  "+spectrumIndex+" in Frame : "+frameId);
        
        Optional<TimsFrame> opFrame = m_ttFrames.stream().filter(frame -> frame.getId().equals(frameId)).findFirst();
        if(!opFrame.isPresent())
            return null;
        
        TimsFrame tf = opFrame.get();
        if(!tf.spectrumRead()){
            List<TimsFrame> tfs = Collections.singletonList(tf);
            m_reader.fillFramesWithSpectrumData(m_fileHandler, tfs);            
        }
        
        Spectrum spectrum = null;
        if(tf.isPasef() && tf.getPrecursorId() != null){
            
           //Read spcetrum corresponding to index...             
           Integer indexInFrameSpectra = spectrumIndex - m_frame2FirstSpectraIndex.get(tf.getId()); //Index relative to frame 
           List<Integer> precursorIds = tf.getPrecursorId();
           if(indexInFrameSpectra >= precursorIds.size())
               return null;
           Collections.sort(precursorIds);           
           fr.proline.brucker.timstof.model.Spectrum tfSp = tf.getPrecursorSpectrum(precursorIds.get(indexInFrameSpectra));
           spectrum = new Spectrum(spectrumIndex,  tf.getTime().floatValue(), tfSp.getMasses(), tfSp.getIntensities(), 2);
           spectrum.setTitle(tfSp.getTitle());
        } else if (!tf.isPasef()){
            fr.proline.brucker.timstof.model.Spectrum tfSp = tf.getSingleSpectrum();
            //Suppose MS1
            HashMap<Float, Double> massPerInt = new HashMap<>();
            double[] allMasses = tfSp.getMasses();
            float[] allInt = tfSp.getIntensities();
            List<Float> maxInt = new ArrayList<>();
            for(int i = 0; i<allMasses.length; i++){    
                massPerInt.put(allInt[i], allMasses[i]);
                maxInt.add(allInt[i]);
            }
            Collections.sort(maxInt);
            Collections.reverse(maxInt);
            double[] someMasses = new double[50000];
            float[] someInt =new  float[50000];
            for(int i =0 ; i<50000; i++){
             someInt[i]=maxInt.get(i);
             someMasses[i]=massPerInt.get(someInt[i]);
            }
            //spectrum  = new Spectrum(spectrumIndex,  tf.getTime().floatValue(), tfSp.getMasses(), tfSp.getIntensities(), 1);
            spectrum  = new Spectrum(spectrumIndex,  tf.getTime().floatValue(), someMasses, someInt, 1);
            spectrum.setTitle(tfSp.getTitle());
        }
            
        return spectrum;
    }

    @Override
    public int getSpectrumCount() {
        return m_spectra2FrameIndex.size();
    }

    @Override
    public int getSpectrumId(double retentionTime) {
        LOG.info("Search Spectrum for RT "+retentionTime);
        for (TimsFrame fr : m_ttFrames) {
            if (Math.abs(fr.getTime() - retentionTime) < 0.05) {
                LOG.info(" FOUND "+fr.getId());
                return m_frame2FirstSpectraIndex.get(fr.getId());
            }
        }
        return 0; //First spectrum if not found ? or -1 ?
    }

    @Override
    public double[] getElutionTimes(int msLevel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getSpectrumElutionTime(int spectrumIndex) {
        Integer frameId = m_spectra2FrameIndex.get(spectrumIndex);
        Optional<TimsFrame> opFr =  m_ttFrames.stream().filter(fr -> fr.getId().equals(frameId)).findFirst();
        if(opFr.isPresent())
            return opFr.get().getTime();
        return 0; //First spectrum if not found ? or -1 ?
    }

    @Override
    public int getNextSpectrumId(int spectrumIndex, int msLevel) {
        //Pour le moment suppose que PASEF !
        final TimsFrame.MsMsType msmsType = (msLevel == 2) ? TimsFrame.MsMsType.PASEF : TimsFrame.MsMsType.MS;
            
        //Suppose index and times are ordered the same way ! ... To verify ?               
        Integer nextSpectrumId= spectrumIndex+1;
        Integer frameId = m_spectra2FrameIndex.get(spectrumIndex);
        Integer nextFrameId = m_spectra2FrameIndex.get(nextSpectrumId);
        if(frameId.equals(nextFrameId))//same frame so same MsLevel
            return nextSpectrumId;
        
        boolean foundSpectrumFrame = false;
        while(!foundSpectrumFrame){ 
            final Integer finalFrId = nextFrameId;
            Optional<TimsFrame> opFr = m_ttFrames.stream().filter(fr -> fr.getId().equals(finalFrId)).findFirst();
            if(opFr.isPresent()){
                if(opFr.get().getMsmsType().equals(msmsType)){
                    foundSpectrumFrame = true;
                    break;                            
                }else{
                    nextFrameId++; //search in next frame
                    nextSpectrumId = m_frame2FirstSpectraIndex.get(nextFrameId);
                }
            } else 
                break;
        }
        if(foundSpectrumFrame)
            return nextSpectrumId;
        return 0; //First spectrum if not found ? or -1 ?
    }

    @Override
    public int getPreviousSpectrumId(int spectrumIndex, int msLevel) {
        //Pour le moment suppose que PASEF !
        final TimsFrame.MsMsType msmsType = (msLevel == 2) ? TimsFrame.MsMsType.PASEF : TimsFrame.MsMsType.MS;
            
        //Suppose index and times are ordered the same way ! ... To verify ?                
        Integer prevSpectrumId= spectrumIndex-1;
        Integer frameId = m_spectra2FrameIndex.get(spectrumIndex);
        Integer prevFrameId = m_spectra2FrameIndex.get(prevSpectrumId);
        if(frameId.equals(prevFrameId))//same frame so same MsLevel
            return prevSpectrumId;
        
        boolean foundSpectrumFrame = false;
        while(!foundSpectrumFrame){
             final Integer finalFrId = prevFrameId;
            Optional<TimsFrame> opFr =  m_ttFrames.stream().filter(fr -> fr.getId().equals(finalFrId)).findFirst();
            if(opFr.isPresent()){
                if(opFr.get().getMsmsType().equals(msmsType)){
                    foundSpectrumFrame = true;
                    break;                            
                }else{
                    prevFrameId--; //search in next frame
                    prevSpectrumId = m_frame2FirstSpectraIndex.get(prevFrameId);
                }
            } else 
                break;
        }
        if(foundSpectrumFrame)
            return prevSpectrumId;
                
        return 0;     //First spectrum if not found ? or -1 ?    
    }

    @Override
    public List<Float> getMsMsEvent(double minMz, double maxMz) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean exportRawFile(String outFileName, IExportParameters exportParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDIAFile() {
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

    @Override
    public void closeIRawFile() {
        m_reader.closeTimstofFile(m_fileHandler);
    }

    @Override
    public String toString(){
        return getName();
    }
}