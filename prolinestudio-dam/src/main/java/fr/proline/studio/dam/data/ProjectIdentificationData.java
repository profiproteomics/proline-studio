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

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import java.util.List;

/**
 * Correspond to the opened Project from UDS DB
 *
 * @author JM235353
 */
public class ProjectIdentificationData extends AbstractData {

    private Project m_project = null;
    private String m_temporaryName = null;

    public ProjectIdentificationData(Project project) {
        m_dataType = AbstractData.DataTypes.PROJECT_IDENTIFICATION;
        m_project = project;
    }
    
    public ProjectIdentificationData(String temporaryName) {
        m_dataType = AbstractData.DataTypes.PROJECT_IDENTIFICATION;
        m_temporaryName = temporaryName;
    }
    
    

    @Override
    public String getName() {
        if (m_project != null) {
            return m_project.getName();
        }
        if (m_temporaryName != null) {
            return m_temporaryName;
        }
        
        return "";
    }
    
    public void setProject(Project project) {
        m_project = project;
        m_temporaryName = null;
    }
    
    public Project getProject() {
        return m_project;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority, boolean identificationDataset) {

        list.add(new AllImportedData());
        
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadParentDataset(m_project, list, true);
        if (priority != null) {
            task.setPriority(priority);
        }
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
}
