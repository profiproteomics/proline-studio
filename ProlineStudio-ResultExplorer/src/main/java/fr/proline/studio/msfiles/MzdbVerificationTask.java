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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author AK249877
 */
public class MzdbVerificationTask extends AbstractDatabaseTask {

    private final File m_file;

    public MzdbVerificationTask(AbstractDatabaseCallback callback, File file) {
        super(callback, new TaskInfo("Verify .mzdb file " + file.getAbsolutePath(), false, "Generic Task", TaskInfo.INFO_IMPORTANCE_MEDIUM));
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

    public static boolean isValid(File file) {

        boolean pass = true;

        MzDbReader reader = null;

        try {
            reader = new MzDbReader(file, true);

            SpectrumHeader[] ms2Headers = reader.getMs2SpectrumHeaders();

            SpectrumHeader[] ms1Headers = reader.getMs1SpectrumHeaders();

            if (ms1Headers == null || ms1Headers.length < 1) {
                return false;
            } else {
                    long ms1SpectrumId = ms1Headers[0].getSpectrumId();
                    if (ms1SpectrumId != 0) {
                        Spectrum ms1RawSpectrum = reader.getSpectrum(ms1SpectrumId);
                        if (ms1RawSpectrum != null) {
                            SpectrumData ms1SpectrumData = ms1RawSpectrum.getData();
                            if (ms1SpectrumData != null) {
                                final double[] mzList = ms1SpectrumData.getMzList();
                                if (mzList == null || mzList.length < 1) {
                                    pass = false;
                                }
                            } else {
                                pass = false;
                            }
                        } else {
                            pass = false;
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
                                pass = false;
                            }

                        } else {
                            pass = false;
                        }

                    } else {
                        pass = false;
                    }

                }

            }

        } catch (ClassNotFoundException | FileNotFoundException | SQLiteException e) {
            return false;
        } catch (StreamCorruptedException ex) {
            Logger.getLogger(MzDbReaderHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return pass;

    }

}
