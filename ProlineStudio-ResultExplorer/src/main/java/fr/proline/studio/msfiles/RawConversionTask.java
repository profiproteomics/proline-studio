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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author AK249877
 */
public class RawConversionTask extends AbstractDatabaseTask {

    private final File m_file;
    private Process m_process = null;
    private final StringBuilder m_logs;
    private ConversionSettings m_settings;

    public RawConversionTask(AbstractDatabaseCallback callback, File file, StringBuilder logs, ConversionSettings settings) {
        super(callback, new TaskInfo("Convert .raw file " + file.getAbsolutePath(), false, "Generic Task", TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_file = file;
        m_logs = logs;
        m_settings = settings;
    }

    @Override
    public boolean fetchData() {
        try {
  
            String suffix = null;
            
            if(m_file.getAbsolutePath().endsWith(".RAW")){
                suffix = ".RAW";
            }else if(m_file.getAbsolutePath().endsWith(".raw")){
                suffix = ".raw";
            }else if(m_file.getAbsolutePath().endsWith(".WIFF")){
                suffix = ".WIFF";
            }else if(m_file.getAbsolutePath().endsWith(".wiff")){
                suffix = ".wiff";
            }
            
            m_process = new ProcessBuilder(m_settings.getConverterPath(), "-i", m_file.getAbsolutePath(), "-o", m_settings.getOutputPath()+File.separator+m_file.getName().substring(0, m_file.getName().lastIndexOf(suffix)) + ".mzdb").start();

            InputStream errorStream = m_process.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = reader.readLine()) != null) {
                m_logs.append(line + "\n");
            }

        } catch (IOException ex) {
            Logger.getLogger(RawConversionTask.class.getName()).log(Level.SEVERE, null, ex);
            if (m_process != null && m_process.isAlive()) {
                m_process.destroy();
            }
            m_taskError = new TaskError("Raw Conversion Error", "An IOException was encountered.");
            return false;
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
