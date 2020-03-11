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
package fr.proline.studio.dam.tasks.data;

import java.util.Date;

/**
 * Class Used as template to display all projects and databases size for Admin
 * dialog
 *
 * @author JM235353
 */
public class ProjectInfo {

    private long m_projectId;
    private String m_name;
    private String m_description;
    private String m_user;
    private double m_size; // in MB
    private String m_dbname;
    private String m_properties;
    private Date m_lastDatasetDate;
    private Integer m_rawFilesCount;
    private Status m_status;

    public enum Status {
        ACTIVE, ARCHIVED
    }

    public ProjectInfo(long projectId, String name, String description, String properties, String user) {
        m_projectId = projectId;
        m_name = name;
        m_description = description;
        m_user = user;
        m_properties = properties;
        setStatus(m_properties);
    }

    private void setStatus(String properties) {
        if (properties == null || properties.isEmpty()) {
            m_status = Status.ACTIVE;
        } else if (properties.contains("\"is_active\":false")) {
            m_status = Status.ARCHIVED;
        }

    }

    public Status getStatus() {
        return m_status;
    }

    public void addDb(String dbname, double size) {
        if (m_dbname == null) {
            m_dbname = dbname;
            m_size = size;
        } else {
            m_size += size;
            m_dbname += ", " + dbname;
        }
    }

    public Date getLastDatasetDate() {
        return m_lastDatasetDate;
    }

    public void setLastDatasetDate(Date date) {
        m_lastDatasetDate = date;
    }

    public long getProjectId() {
        return m_projectId;
    }

    public String getName() {
        return m_name;
    }

    public String getDescription() {
        return m_description;
    }

    public String getProperties() {
        return m_properties;
    }

    public String getUser() {
        return m_user;
    }

    public double getSize() {
        return m_size;
    }

    public String getDBName() {
        return m_dbname;
    }

    public Integer getRawFilesCount() {
        return m_rawFilesCount;
    }

    public void setRawFilesCount(int count) {
        m_rawFilesCount = count;
    }
}
