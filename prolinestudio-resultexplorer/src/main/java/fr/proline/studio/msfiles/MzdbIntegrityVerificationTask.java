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

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.MzDbReaderHelper;
import fr.profi.mzdb.model.Spectrum;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StreamCorruptedException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author AK249877
 */
public class MzdbIntegrityVerificationTask extends AbstractDatabaseTask {

    private final File m_file;
    private boolean m_valid;

    public MzdbIntegrityVerificationTask(AbstractDatabaseCallback callback, File file) {
        super(callback, new TaskInfo("Verify integrity of " + file.getAbsolutePath(), false, "Mzdb Verification", TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_file = file;
    }

    @Override
    public boolean fetchData() {
        if (!isValid(m_file)) {
            m_taskError = new TaskError("mzDB file verification error", "File is corrupted.");
            return false;
        }
        return true;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    public boolean isValid(File file) {

                try {

                    m_valid = true;

                    MzDbReader reader = null;

                    try {
                        reader = new MzDbReader(file, true);

                        SpectrumHeader[] ms2Headers = reader.getMs2SpectrumHeaders();

                        SpectrumHeader[] ms1Headers = reader.getMs1SpectrumHeaders();

                        if (ms1Headers == null || ms1Headers.length < 1) {
                            m_valid = false;
                        } else {
                            long ms1SpectrumId = ms1Headers[0].getSpectrumId();
                            if (ms1SpectrumId != 0) {
                                Spectrum ms1RawSpectrum = reader.getSpectrum(ms1SpectrumId);
                                if (ms1RawSpectrum != null) {
                                    SpectrumData ms1SpectrumData = ms1RawSpectrum.getData();
                                    if (ms1SpectrumData != null) {
                                        final double[] mzList = ms1SpectrumData.getMzList();
                                        if (mzList == null || mzList.length < 1) {
                                            m_valid = false;
                                        }
                                    } else {
                                        m_valid = false;
                                    }
                                } else {
                                    m_valid = false;
                                }
                            }
                        }

                        if (ms2Headers != null && ms2Headers.length > 0) {

                            long ms2SpectrumId = ms2Headers[0].getSpectrumId();

                            if (ms2SpectrumId != 0) {

                                Spectrum ms2RawSpectrum = reader.getSpectrum(ms2SpectrumId);

                                if (ms2RawSpectrum != null) {

                                    SpectrumData ms2SpectrumData = ms2RawSpectrum.getData();

                                    if (ms2SpectrumData != null) {

                                        final double[] mzList = ms2SpectrumData.getMzList();

                                        if (mzList == null || mzList.length < 1) {
                                            m_valid = false;
                                        }

                                    } else {
                                        m_valid = false;
                                    }

                                } else {
                                    m_valid = false;
                                }

                            }

                        }

                    } catch (ClassNotFoundException | FileNotFoundException | SQLiteException e) {
                        m_valid = false;
                    } catch (StreamCorruptedException ex) {
                        Logger.getLogger(MzDbReaderHelper.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }

                } catch (Throwable t) {
                    LoggerFactory.getLogger("ProlineStudio.DAM").debug("Unexpected exception in mzdb verification task", t);
                    m_valid = false;
                }
            

        
        return m_valid;

    }

}
