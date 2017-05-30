/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import javax.swing.SwingUtilities;

/**
 *
 * @author AK249877
 */
public class RawConverter implements Runnable, WorkerInterface {

    private final File m_file;
    private boolean m_run = false;
    private int m_state = WorkerInterface.ACTIVE_STATE;
    private final StringBuilder m_logs;
    private ConversionListener m_conversionListener;
    private final ConversionSettings m_settings;

    public RawConverter(File file, ConversionSettings settings) {
        m_file = file;
        m_settings = settings;
        m_logs = new StringBuilder();
    }

    @Override
    public void run() {

        m_run = true;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        
                        File f = new File(m_settings.getOutputPath() + File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(".raw")) + ".mzdb");

                        if (success) {

                            if (m_state == WorkerInterface.ACTIVE_STATE) {
                                m_state = WorkerInterface.FINISHED_STATE;
                                if (m_conversionListener != null) {

                                    if (f.exists()) {
                                        m_conversionListener.ConversionPerformed(f, m_settings, true);
                                    }
                                }

                                if (m_settings.getDeleteRaw()) {
                                    try {
                                        Files.delete(m_file.toPath());
                                    } catch (NoSuchFileException ex) {
                                        ;
                                    } catch (DirectoryNotEmptyException ex) {
                                        ;
                                    } catch (IOException ex) {
                                        ;
                                    }
                                }

                            }
                        } else {
                            terminate();
                            m_conversionListener.ConversionPerformed(f, m_settings, false);
                        }

                    }
                });

            }
        };

        RawConversionTask task = new RawConversionTask(callback, m_file, m_logs, m_settings);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        m_logs.append("Converting for file: " + this.getFile().getAbsolutePath() + " has come to its end.\n\n");
        m_run = false;
    }

    public void addConversionListener(ConversionListener listener) {
        m_conversionListener = listener;
    }

    @Override
    public void terminate() {
        m_state = WorkerInterface.KILLED_STATE;
        m_run = false;
    }

    @Override
    public boolean isAlive() {
        return m_run;
    }

    public File getFile() {
        return m_file;
    }

    @Override
    public int getState() {
        return m_state;
    }

    @Override
    public int getWorkerType() {
        return WorkerInterface.CONVERTER_TYPE;
    }

    @Override
    public StringBuilder getLogs() {
        return m_logs;
    }
}
