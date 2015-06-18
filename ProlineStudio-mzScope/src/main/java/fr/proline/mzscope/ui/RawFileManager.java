package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.mzdb.ThreadedMzdbRawFile;
import fr.proline.mzscope.mzml.MzMLRawFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class RawFileManager {

    private static Logger logger = LoggerFactory.getLogger(RawFileManager.class);
    
    private Map<String, IRawFile> files;

    private IRawFile currentFile;

    private static RawFileManager instance;

    public static RawFileManager getInstance() {
        if (instance == null) {
            instance = new RawFileManager();
        }
        return instance;
    }

    private RawFileManager() {
        files = new HashMap<String,IRawFile>();
    }

    public IRawFile addRawFile(File file) { 
       if (file.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            currentFile = new ThreadedMzdbRawFile(file);
            files.put(file.getName(), currentFile);
            logger.info("Rawfile {} added to RawFileManager",file.getAbsolutePath());
        } else if (file.getAbsolutePath().endsWith(".mzML")) {
            currentFile = new MzMLRawFile(file);
            files.put(file.getName(), currentFile);
            logger.info("Rawfile {} added to RawFileManager",file.getAbsolutePath());
        }
       return currentFile;
    }

    public IRawFile getLastFile() {
        return currentFile;
    }

    public IRawFile getFile(String filename) {
        if (files.containsKey(filename)) {
            logger.info("RawFileManager will give access to {}",filename);
            return files.get(filename);
        } else {
            logger.warn("RawFile {} not found", filename);
        }
        return null;
    }

    public List<IRawFile> getAllFiles(){
        return new ArrayList<IRawFile>(files.values());
    }
}
