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
