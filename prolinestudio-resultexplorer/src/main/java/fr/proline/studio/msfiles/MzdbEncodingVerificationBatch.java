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
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author AK249877
 */
public class MzdbEncodingVerificationBatch implements Runnable {

    private final ArrayList<File> m_files;
    private ArrayList<MsListenerParameter> m_list;
    private MsListener m_listener;

    public MzdbEncodingVerificationBatch(ArrayList<File> files) {
        m_files = files;
        m_list = new ArrayList<MsListenerParameter>();
    }

    public void addMsListener(MsListener listener) {
        m_listener = listener;
    }

    @Override
    public void run() {
        for (int i = 0; i < m_files.size(); i++) {
            verifyEncoding(m_files.get(i));
        }
    }

    private void verifyEncoding(File f) {
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (finished) {
                    if (m_listener != null) {
                        
                        m_list.add(new MsListenerParameter(f, success));
                        
                        if (m_list.size() == m_files.size()) {
                            m_listener.verificationPerformed(m_list);
                        }
                        
                    }
                }
            }

        };

        MzdbEncodingVerificationTask encodingVerificationTask = new MzdbEncodingVerificationTask(callback, f);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(encodingVerificationTask);

    }

}
