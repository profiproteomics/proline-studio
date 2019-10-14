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
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author AK249877
 */
public class DatUploadBatch implements Runnable {

    private final ThreadPoolExecutor m_executor;
    private final ArrayList<File> m_files;
    

    public DatUploadBatch(ArrayList<File> files) {
        m_files = files;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    }

    public void addFile(File f) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            //DatUploader uploader = new DatUploader(f);
            //m_executor.execute(uploader);
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < m_files.size(); i++) {
            addFile(m_files.get(i));
        }
        m_executor.shutdown();
        try {
            m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }
    }

}
