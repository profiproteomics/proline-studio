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
package fr.proline.studio.msfiles;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.proline.studio.Exceptions;

/**
 *
 * @author AK249877
 */
public class RawConversionTask extends AbstractDatabaseTask {

    private final File m_file;
    private Process m_process = null;
    private ConversionSettings m_settings;

    public RawConversionTask(AbstractDatabaseCallback callback, File file, ConversionSettings settings) {
        super(callback, new TaskInfo("Convert .raw file " + file.getAbsolutePath(), false, "Generic Task", TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_file = file;
        m_settings = settings;
    }

    @Override
    public boolean fetchData() {
        try {

            String suffix = null;

            if (m_file.getAbsolutePath().endsWith(".RAW")) {
                suffix = ".RAW";
            } else if (m_file.getAbsolutePath().endsWith(".raw")) {
                suffix = ".raw";
            } else if (m_file.getAbsolutePath().endsWith(".WIFF")) {
                suffix = ".WIFF";
            } else if (m_file.getAbsolutePath().endsWith(".wiff")) {
                suffix = ".wiff";
            }

            List<String> commandParams = new ArrayList<>();
            commandParams.add(m_settings.getConverterPath());
            commandParams.add("-i");
            commandParams.add( m_file.getAbsolutePath());
            commandParams.add("-o");
            commandParams.add(m_settings.getOutputPath()+ File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(suffix))+ ".mzdb");
            if(m_settings.isVersion1_1())
                commandParams.add("-n");
            m_process = new ProcessBuilder(commandParams).start();

        } catch (IOException ex) {
            if (m_process != null && m_process.isAlive()) {
                m_process.destroy();
            }
            m_taskError = new TaskError("Raw Conversion Error", "An IOException was encountered.");
            return false;
        }

        while (m_process != null && m_process.isAlive()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (m_process.exitValue() == 0) {

            if (m_settings.getDeleteRaw()) {
                FileUtility.deleteFile(m_file);
            }

        } else {
            if (m_process != null && m_process.isAlive()) {
                m_process.destroy();
            }
            m_taskError = new TaskError("Raw Conversion Error", "Process abnormal exit.");
            return false;
        }
        return true;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

}
