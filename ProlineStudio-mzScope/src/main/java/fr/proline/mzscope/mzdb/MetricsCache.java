/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.mzdb;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.QCMetrics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MetricsCache {

    private static final String CACHE_DIR = "cache_mzscope";
    private static final String CACHEFILE_EXTENSION = ".ksm";
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCache.class);
    
    private static MetricsCache instance = null;
    
    private Kryo kryo;
    
    public static MetricsCache getInstance() {
        if (instance == null) {
            instance = new MetricsCache();
        }
        return instance;
    }
    
    private MetricsCache() {
        this.kryo = new Kryo();
    }
    
    private File getCacheFolder() {
        File dir = new File(System.getProperty("user.dir"));
        File cache = new File(dir, CACHE_DIR);
        if (!cache.exists()) {
            cache.mkdir();
        }
        return cache;
    }
    
    public void writeQC(IRawFile rawFile, QCMetrics metrics) {
        FileOutputStream fos = null;
        try {
            File outputFile = new File(getCacheFolder(), rawFile.getName() + CACHEFILE_EXTENSION);
            fos = new FileOutputStream(outputFile);
            Output out = new Output(fos);
            kryo.writeObject(out, metrics);
            logger.info("QC info wrote into " + outputFile.getAbsolutePath());
            out.close();
            fos.close();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public QCMetrics loadQC(IRawFile rawFile) {
        FileInputStream fis = null;
        try {
            File inputFile = new File(getCacheFolder(), rawFile.getName() + CACHEFILE_EXTENSION);
            if (inputFile.exists()) {
                fis = new FileInputStream(inputFile);
                Input input = new Input(fis);
                QCMetrics metrics = kryo.readObject(input, QCMetrics.class);
                logger.info("QC info loaded from " + inputFile.getAbsolutePath());
                input.close();
                return metrics;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
}
