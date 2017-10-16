/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.profi.mzdb.MzDbReaderHelper;
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
