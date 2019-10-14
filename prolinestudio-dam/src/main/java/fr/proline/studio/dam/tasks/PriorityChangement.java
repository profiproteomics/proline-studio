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
package fr.proline.studio.dam.tasks;

/**
 * 
 * Manage priority changement, by collecting an index range which
 * needs to be done in priority, and/or a SubTask type which needs 
 * to be done in priority.
 * A SubTask priority is treated before an index range priority.
 * 
 * @author JM235353
 */

public class PriorityChangement {

    private AbstractDatabaseTask m_task;
    private int m_startIndex = -1;
    private int m_stopIndex = -1;
    private int m_subTaskId = -1;

    public PriorityChangement(AbstractDatabaseTask task) {
        m_task = task;
    }

    public void setIndex(int index) {
        m_startIndex = index;
        m_stopIndex = index;
    }

    public void setSubTask(int subTaskId) {
        m_subTaskId = subTaskId;
    }

    public AbstractDatabaseTask getTask() {
        return m_task;
    }

    public void addIndex(int index) {
        if (m_startIndex > index) {
            m_startIndex = index;
        } else if (m_stopIndex < index) {
            m_stopIndex = index;
        }

    }

    public void clearIndex() {
        m_startIndex = -1;
        m_stopIndex  = -1;
    }
    public int getStartIndex() {
        return m_startIndex;
    }

    public int getStopIndex() {
        return m_stopIndex;
    }
    
    public int getSubTaskId() {
        return m_subTaskId;
    }
}