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
package fr.proline.studio.dam.data;

import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class RunInfoData extends AbstractData {

    public enum Status {

        MISSING, USER_DEFINED, SYSTEM_PROPOSED, LAST_DEFINED, LINKED_IN_DATABASE, NOT_INITIALIZED
    }

    public Status m_status;

    private File m_rawFileOnDisk = null;
    private RawFile m_linkedRawFile = null;
    private RawFile m_selectedRawFile = null;

    private String m_peakListPath = null;

    private Run m_run = null;

    private String m_message = null;

    /**
     * HashMap<RawFileName of RawFile, RawFile>
     */
    private HashMap<String, RawFile> m_potentialRawFileMap = null;

    public RunInfoData() {
        m_status = Status.NOT_INITIALIZED;
    }

    public void setStatus(Status status) {
        m_status = status;
    }

    public Status getStatus() {
        return m_status;
    }

    public void setPotentialRawFiles(HashMap<String, RawFile> potentialRawFileMap) {
        m_potentialRawFileMap = potentialRawFileMap;
    }

    public void addPotentialRawFiles(RawFile linkedFile) {
        if (m_potentialRawFileMap == null) {
            m_potentialRawFileMap = new HashMap();
        }
        String fileName = linkedFile.getRawFileName();
        if (m_potentialRawFileMap.get(fileName) == null) {
            m_potentialRawFileMap.put(linkedFile.getRawFileName(), linkedFile);
        }

    }

    public boolean hasPotentialRawFiles() {
        return m_potentialRawFileMap != null && !m_potentialRawFileMap.isEmpty();
    }

    public HashMap<String, RawFile> getPotentialRawFiles() {
        return m_potentialRawFileMap;
    }

    public void setPeakListPath(String peakListPath) {
        m_peakListPath = peakListPath;
    }

    public String getPeakListPath() {
        return m_peakListPath;
    }

    public void setMessage(String message) {
        m_message = message;
    }

    public boolean hasRawFile() {
        return ((m_linkedRawFile != null) || (m_selectedRawFile != null) || (m_rawFileOnDisk != null));
    }

    public boolean isRunInfoInDatabase() {

        if (m_linkedRawFile != null) {
            return true;
        }

        if (m_selectedRawFile != null && m_selectedRawFile.getOwnerId() != 0) {
            return true;
        }

        return false;
    }

    @Override
    public String getName() {

        String name = null;

        if (m_linkedRawFile != null) {
            name = m_linkedRawFile.getMzDbFileName();
        } else if (m_selectedRawFile != null) {
            name = m_selectedRawFile.getMzDbFileName();
        } else if (m_rawFileOnDisk != null) {
            name = m_rawFileOnDisk.getName();
        }

        if (name != null) {
            return name;
        }

        if (m_message != null) {
            return m_message;
        }

        return "Search...";
    }

    public void setRawFileOnDisk(File rawFileOnDisk) {
        m_rawFileOnDisk = rawFileOnDisk;
        m_selectedRawFile = null;
        m_linkedRawFile = null;
    }

    public File getRawFileOnDisk() {
        return m_rawFileOnDisk;
    }

    public void setLinkedRawFile(RawFile rawFile) {
        m_linkedRawFile = rawFile;
        m_selectedRawFile = null;
        m_rawFileOnDisk = null;
    }

    public RawFile getLinkedRawFile() {
        return m_linkedRawFile;
    }

    public RawFile getSelectedRawFile() {
        //VDS : TO COMMENT Next session in order to have 2 methods : getSelectedRawFile and createRawFileFromFile...
//        if ((m_selectedRawFile == null) && (m_rawFileOnDisk != null)) {
//            RawFile rawFile = new RawFile();
//            rawFile.setRawFileDirectory(m_rawFileOnDisk.getPath());
//            rawFile.setMzDbFileDirectory(m_rawFileOnDisk.getPath());
//            int id = m_rawFileOnDisk.getName().indexOf(".");
//            if (id == -1) {
//                id = m_rawFileOnDisk.getName().length();
//            }
//            String identifier = m_rawFileOnDisk.getName().substring(0, id);
//            rawFile.setRawFileName(identifier + ".raw");
//            rawFile.setIdentifier(identifier);
//            rawFile.setMzDbFileName(m_rawFileOnDisk.getName());
//            m_selectedRawFile = rawFile;
//        }
        //VDS : END TO COMMENT 
        return m_selectedRawFile;
    }

    public void setSelectedRawFile(RawFile rawFile) {
        m_selectedRawFile = rawFile;
        m_rawFileOnDisk = null;
        m_linkedRawFile = null;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority, boolean identificationDataset) {
        // nothing to do
    }

    /**
     * @return the m_run
     */
    public Run getRun() {
        return m_run;
    }

    /**
     * @param run
     */
    public void setRun(Run run) {
        this.m_run = run;
    }

}
