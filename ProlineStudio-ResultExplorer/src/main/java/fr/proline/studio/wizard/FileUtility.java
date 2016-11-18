/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

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
