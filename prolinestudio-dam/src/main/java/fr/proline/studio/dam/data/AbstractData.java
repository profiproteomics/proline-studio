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

import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.util.List;

/**
 * Must be extended by classes used as User Data in Result Explorer Nodes
 *
 * @author JM235353
 */
public abstract class AbstractData {

    public enum DataTypes {
        MAIN,
        PROJECT_IDENTIFICATION,
        PROJECT_QUANTITATION,
        DATA_SET,
        ALL_IMPORTED
    }
    protected String m_name;
    protected DataTypes m_dataType;
    // by default, any data can have children when corresponding node is expanded
    protected boolean m_hasChildren = true;

    /**
     * Returns if the corresponding Node will have Children when it will be
     * expanded
     *
     * @return
     */
    public boolean hasChildren() {
        return m_hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        m_hasChildren = hasChildren;
    }
    
    /**
     * This method is called from a thread which is not the AWT and can wait for
     * the answer. So we use a Semaphore to synchronize and wait the result from
     * the AccessDatabaseThread
     *
     * @param list
     */
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, boolean identificationDataset) {
        load(callback, list, null, identificationDataset);
    }
    public abstract void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority, boolean identificationDataset);

    /**
     * Subclasses must overriden this method to give the name of the data
     *
     * @return
     */
    public abstract String getName();


    public DataTypes getDataType() {
        return m_dataType;
    }
}
