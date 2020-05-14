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
package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseRsetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.actions.identification.ImportSearchResultAsRsetJMSAction;
import fr.proline.studio.rsmexplorer.gui.RsetAllPanel;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;

/**
 * Databox for all Rset of a project
 * @author JM235353
 */
public class DataBoxRsetAll extends AbstractDataBox {
    
    private Project m_project = null;
    
    public DataBoxRsetAll() {
        super(DataboxType.DataBoxRsetAll, DataboxStyle.STYLE_RSET);
        
        
        // Name of this databox
        m_typeName = "Search Results";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(Project.class);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ResultSet.class);
        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        RsetAllPanel p = new RsetAllPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
        
        
    }

    @Override
    public void dataChanged() {

            Project p = (m_project != null) ? m_project : (Project) m_previousDataBox.getData(Project.class);

            final ArrayList<ResultSet> resultSetArrayList = new ArrayList<>();
            
            final int loadingId = setLoading();
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    ((RsetAllPanel) getDataBoxPanelInterface()).setData(taskId, resultSetArrayList);

                    setLoaded(loadingId);
                    
                    if (finished) {
                        unregisterTask(taskId);
                    }
                }
            };


            // ask asynchronous loading of data
            
            DatabaseRsetTask task = new DatabaseRsetTask(callback, p.getId(), resultSetArrayList);
            registerTask(task);

    }
    
    @Override
    public Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType!= null ) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(ResultSet.class)) {
                    return ((RsetAllPanel) getDataBoxPanelInterface()).getSelectedResultSet();
                }
            }

        }
        return super.getData(parameterType, parameterSubtype);
    }
    
    @Override
    public void setEntryData(Object data) {
        
        getDataBoxPanelInterface().addSingleValue(data);
        
        m_project = (Project) data;

        dataChanged();
        
        ImportSearchResultAsRsetJMSAction.addEventListener(m_project.getId(), this);
    }

    @Override
    public void windowClosed() {
        ImportSearchResultAsRsetJMSAction.removeEventListener(m_project.getId(), this);
        super.windowClosed();
    }

    
    @Override
    public void stateChanged(ChangeEvent e) {
        dataChanged();
    }
    
}
