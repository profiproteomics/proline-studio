/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.proline.mzscope.mzdb.MzdbRawFile;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author AK249877
 */
public class MgfExportTask extends AbstractDatabaseTask {

    private final File m_file;
    private final MgfExportSettings m_exportSettings;

    private final StringBuilder m_logs;

    public MgfExportTask(AbstractDatabaseCallback callback, File file, StringBuilder logs, MgfExportSettings exportSettings) {
        super(callback, new TaskInfo("Export .mgf file for " + file.getAbsolutePath(), false, "Generic Task", TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_file = file;
        m_logs = logs;
        m_exportSettings = exportSettings;
    }

    @Override
    public boolean fetchData() {
        
        if(!canExportMgf()){
            return false;
        }
        
        try {
            MzdbRawFile mzdbFile = new MzdbRawFile(m_file);
            String outputFileName = m_exportSettings.getDestinationDirectory() + File.separator + m_file.getName().substring(0, m_file.getName().lastIndexOf(".mzdb")) + ".mgf";
            mzdbFile.exportRawFile(outputFileName, m_exportSettings.getMgfExportParameters());
            return true;
        } catch (Exception ex) {
            m_logs.append("Exporting .mgf for " + m_file.getAbsolutePath() + " failed!\n");
            return false;
        }
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    public boolean canExportMgf() {

        MzDbReader reader = null;
        try {
            reader = new MzDbReader(m_file, true);
            SpectrumHeader[] headers = reader.getMs2SpectrumHeaders();
            reader.close();
            if (headers == null) {
                return false;
            } else {
                return true;
            }
        } catch (ClassNotFoundException | FileNotFoundException | SQLiteException e) {
            return false;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

    }

}
