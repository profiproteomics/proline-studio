/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.openide.util.Exceptions;

/**
 *
 * @author AK249877
 */
public class FileDeletionTask extends AbstractDatabaseTask {

    private final File m_file;

    public FileDeletionTask(AbstractDatabaseCallback callback, File file) {
        super(callback, new TaskInfo("Delete file " + file.getAbsolutePath(), false, "Generic Task", TaskInfo.INFO_IMPORTANCE_LOW));
        m_file = file;
    }

    @Override
    public boolean fetchData() {
        boolean result = false;
        
        try {
            result = Files.deleteIfExists(m_file.toPath());
            if (!result) {
                m_taskError = new TaskError("File Deletion Error", "File " + m_file.getAbsolutePath() + " could not be deleted.\n");
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return result;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

}
