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
package fr.proline.studio.pattern.xic;


import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.xic.ExperimentalDesignPanel;


/**
 * experimental design databox
 * @author 
 */
public class DataboxExperimentalDesign extends AbstractDataBox {

    private DDataset m_dataset;
    
    
    public DataboxExperimentalDesign() { 
        super(DataboxType.DataboxExperimentalDesign, DataboxStyle.STYLE_XIC);
        
        // Name of this databox
        m_typeName = "Experimental Design";
        m_description = "Experimental Design of the quantitation";

        // Register in parameters
        // One Dataset 
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DDataset.class); 
        registerInParameter(inParameter);


        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(DDataset.class);
        registerOutParameter(outParameter);
    }
    
    @Override
    public void createPanel() {
        ExperimentalDesignPanel p = new ExperimentalDesignPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    ((ExperimentalDesignPanel) getDataBoxPanelInterface()).setData(taskId, m_dataset, finished);
                   
                } else {
                    ((ExperimentalDesignPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId); 
                    addDataChanged(DDataset.class); 
                    propagateDataChanged();
                }
            }
        };

        // ask asynchronous loading of data
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadQuantChannels(getProjectId(), m_dataset);
        registerTask(task);

    }
    
    
    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }
   
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {

                if (parameterType.equals(DDataset.class)) {
                    return m_dataset;
                }
            }
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }
   
    @Override
    public String getFullName() {
        if (m_dataset == null) {
            return super.getFullName();
        }
        return m_dataset.getName()+" "+getTypeName();
    }
}
