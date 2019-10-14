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

import fr.proline.mzscope.mzdb.MzdbRawFile;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;

/**
 *
 * @author AK249877
 */
public class MgfExportTask extends AbstractDatabaseTask {

    private final File m_file;
    private final MgfExportSettings m_exportSettings;

    public MgfExportTask(AbstractDatabaseCallback callback, File file, MgfExportSettings exportSettings) {
        super(callback, new TaskInfo("Export .mgf file for " + file.getAbsolutePath(), false, "Generic Task", TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_file = file;
        m_exportSettings = exportSettings;
    }

    @Override
    public boolean fetchData() {
        

        try {
            MzdbRawFile mzdbFile = new MzdbRawFile(m_file);
            String outputFileName = m_exportSettings.getDestinationDirectory() + File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(".mzdb")) + ".mgf";
            mzdbFile.exportRawFile(outputFileName, m_exportSettings.getMgfExportParameters());
            return true;
        } catch (Exception ex) {
            m_taskError = new TaskError("Mgf Exportation Error", "Exporting .mgf for " + m_file.getAbsolutePath() + " failed.\n");
            return false;
        }
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

}
