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
package fr.proline.studio.msfiles;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author AK249877
 */
public class FileUtility {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("FileUtil");

    public static boolean deleteFile(File f) {
        try {
            Files.delete(f.toPath());
        } catch (NoSuchFileException x) {
            logger.error("Trying to delete file " + f.getAbsolutePath() + ", which does not exist!", x);
            return false;
        } catch (DirectoryNotEmptyException x) {
            logger.error("Directory " + f.getAbsolutePath() + " is not empty!", x);
            return false;
        } catch (IOException x) {
            logger.error("You do not have the right to delete: " + f.toPath().toString() + "!", x);
            return false;
        }
        return true;
    }

    public static boolean isCompletelyWritten(File file) {
        RandomAccessFile stream = null;
        try {
            stream = new RandomAccessFile(file, "rw");
            return true;
        } catch (Exception e) {
            logger.debug("Skipping file " + file.getName() + " for this iteration due it's not completely written");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.error("Exception during closing file " + file.getName());
                }
            }
        }
        return false;
    }


}
