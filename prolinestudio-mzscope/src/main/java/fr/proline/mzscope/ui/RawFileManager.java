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

import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.mzdb.ThreadedMzdbRawFile;
import fr.proline.mzscope.mzml.MzMLRawFile;
import fr.proline.mzscope.timstof.TimstofRawFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class RawFileManager {

    private static final Logger logger = LoggerFactory.getLogger(RawFileManager.class);
    
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
        files = new HashMap<>();
    }

    public IRawFile addRawFile(IRawFile rawFile) {
        currentFile = rawFile;
        files.put(rawFile.getFile().getAbsolutePath(), currentFile);
        logger.info("Rawfile {} added to RawFileManager",rawFile.getFile().getAbsolutePath());
        return rawFile;
    }
    
    public IRawFile addRawFile(File file) { 
       String absolutePath =file.getAbsolutePath();
       if (absolutePath.toLowerCase().endsWith(".mzdb")) {
            currentFile = new ThreadedMzdbRawFile(file);
            files.put(absolutePath, currentFile);
            logger.info("mzDB Rawfile {} added to RawFileManager",absolutePath);
        } else if (absolutePath.toLowerCase().endsWith(".mzml")) {
            currentFile = new MzMLRawFile(file);
            files.put(absolutePath, currentFile);
            logger.info("mzML Rawfile {} added to RawFileManager",absolutePath);
        } else if(absolutePath.toLowerCase().endsWith(".d")){
            String[] options = {TimstofRawFile.MS1_SINGLE_SPECTRA, TimstofRawFile.MS1_SPECTRA_PER_SCAN};
            int reply = JOptionPane.showOptionDialog(null, "MS1 Spectra display", "MS1 Format",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, null, options, TimstofRawFile.MS1_SINGLE_SPECTRA);
            if (reply == JOptionPane.YES_OPTION) 
                currentFile = new TimstofRawFile(file);
            else
                currentFile = new TimstofRawFile(file, TimstofRawFile.MS1_SPECTRA_PER_SCAN);
            files.put(absolutePath, currentFile);
            logger.info("TimsTof Rawfile {} added to RawFileManager",absolutePath);
        }
       return currentFile;
    }

    public IRawFile getLastFile() {
        return currentFile;
    }

    public IRawFile getFile(String absoluteFilePath) {
        if (files.containsKey(absoluteFilePath)) {
            logger.info("RawFileManager will give access to {}",absoluteFilePath);
            return files.get(absoluteFilePath);
        } else {
            logger.warn("RawFile {} not found", absoluteFilePath);
        }
        return null;
    }

    public List<IRawFile> getAllFiles(){
        return new ArrayList<>(files.values());
    }
    
    public void removeFile(IRawFile rawFile){
        if(files.containsValue(rawFile)){
            for(Map.Entry<String, IRawFile> e : files.entrySet()){
                if(e.getValue().equals(rawFile) ){
                    rawFile.closeIRawFile();
                    files.remove(e.getKey());
                    break;
                }
            }
        }
    }
    
    public void removeAllFiles(){
        for(IRawFile rFile :  files.values()){
            rFile.closeIRawFile();
        }
        files = new HashMap<>();
    }

    public boolean removeRawFile(IRawFile rawFile) {
        IRawFile removedFile = files.remove(rawFile.getFile().getAbsolutePath());
        return (removedFile != null);
    }
}
