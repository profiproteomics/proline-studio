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

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.SwingUtilities;

/**
 *
 * @author AK249877
 */
public class RawConverter implements Runnable {

    private final File m_file;
    private MsListener m_msListener;
    private final ConversionSettings m_settings;

    public RawConverter(File file, ConversionSettings settings) {
        m_file = file;
        m_settings = settings;
    }

    @Override
    public void run() {

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

                        File f = null; 

                        if (m_file.getName().contains(".raw")) {
                            f = new File(m_settings.getOutputPath() + File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(".raw")) + ".mzdb");
                        }else if (m_file.getName().contains(".RAW")) {
                            f = new File(m_settings.getOutputPath() + File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(".RAW")) + ".mzdb");
                        }else if (m_file.getName().contains(".wiff")) {
                            f = new File(m_settings.getOutputPath() + File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(".wiff")) + ".mzdb");
                        }else if (m_file.getName().contains(".WIFF")) {
                            f = new File(m_settings.getOutputPath() + File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(".WIFF")) + ".mzdb");
                        }
                        if (success) {

                                if (m_msListener != null) {

                                    if (f!=null && f.exists()) {
                                        
                                        ArrayList<MsListenerConverterParameter> list = new ArrayList<MsListenerConverterParameter>();
                                        list.add(new MsListenerConverterParameter(f, true, m_settings));
                                        
                                        m_msListener.conversionPerformed(list);
                                    }
                                }

                                if (m_settings.getDeleteRaw()) {
                                    try {
                                        Files.delete(m_file.toPath());
                                        
                                        HashSet<String> directories = new HashSet<String>();
                                        
                                        File outputDirectory = new File(m_file.getParentFile().getAbsolutePath());

                                        while (outputDirectory.getParentFile() != null) {
                                            directories.add(outputDirectory.getAbsolutePath());
                                            outputDirectory = outputDirectory.getParentFile();
                                        }
                                       
                                        MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().expandMultipleTreePath(directories);
                                        MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();
                                        
                                    } catch (NoSuchFileException ex) {
                                        ;
                                    } catch (DirectoryNotEmptyException ex) {
                                        ;
                                    } catch (IOException ex) {
                                        ;
                                    }
                                }

                            
                        } else {
                            if (m_msListener != null) {
                                
                                ArrayList<MsListenerConverterParameter> list = new ArrayList<MsListenerConverterParameter>();
                                list.add(new MsListenerConverterParameter(f, false, m_settings));
                                
                                m_msListener.conversionPerformed(list);
                            }
                        }

                    }
                });

            }
        };

        RawConversionTask task = new RawConversionTask(callback, m_file, m_settings);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    public void addMsListener(MsListener listener) {
        m_msListener = listener;
    }
    
}
