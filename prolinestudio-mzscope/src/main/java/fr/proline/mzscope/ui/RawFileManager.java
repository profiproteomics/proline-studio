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

    public IRawFile addRawFile(IRawFile rawFile) {
        currentFile = rawFile;
        files.put(rawFile.getName(), currentFile);
        logger.info("Rawfile {} added to RawFileManager",rawFile.getFile().getAbsolutePath());
        return rawFile;
    }
    
    public IRawFile addRawFile(File file) { 
       if (file.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            currentFile = new ThreadedMzdbRawFile(file);
            files.put(file.getName(), currentFile);
            logger.info("Rawfile {} added to RawFileManager",file.getAbsolutePath());
        } else if (file.getAbsolutePath().toLowerCase().endsWith(".mzml")) {
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
    
    public void removeAllFiles(){
        files = new HashMap<String,IRawFile>();
    }
}
