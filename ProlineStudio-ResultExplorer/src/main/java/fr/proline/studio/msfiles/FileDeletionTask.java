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
        boolean b = FileUtility.deleteFile(m_file);
        if (b) {
            m_taskError = new TaskError("File Deletion Error", "File " + m_file.getAbsolutePath() + " could not be deleted.\n");
        }
        return b;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

}
