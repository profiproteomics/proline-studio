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
package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;


/**
 * Parent for all objects in a graph (nodes, connectors, links)
 * @author JM235353
 */
public abstract class AbstractConnectedGraphObject extends AbstractGraphObject {

    protected GraphGroup m_group = null;
    
    public AbstractConnectedGraphObject(TypeGraphObject type) {
        super(type);
    }
    
    public void setGroup(GraphGroup group) {
        m_group = group;
    }
    
    public GraphGroup getGroup() {
        return m_group;
    }
    
    public abstract boolean isConnected(boolean recursive);
    public abstract boolean canSetSettings();
    public abstract boolean settingsDone();
    public abstract boolean calculationDone();

    public abstract String getPreviousDataName();
    public abstract String getDataName();
    public abstract String getTypeName();
    public abstract String getFullName();
    public String getFullName(int index) {
        return getFullName();
    }
  
    public abstract GlobalTableModelInterface getGlobalTableModelInterface(int index);
    
    
    public abstract void deleteAction();
}
