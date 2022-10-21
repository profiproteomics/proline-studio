/* 
 * Copyright (C) 2019
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

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.rsmexplorer.gui.tasklog.TasksPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * Management of Task Logs
 * @author JM235353
 */
public class DataBoxTaskList extends AbstractDataBox {

    private static final int UPDATE_DELAY = 1000;
    
    private Timer m_updateTimer = null;
    
    public DataBoxTaskList() {
        super(DataboxType.DataBoxTaskList, DataboxStyle.STYLE_UNKNOWN);
        
        // Name of this databox
        m_typeName = "User Tasks Logs";

        // Register possible out parameters
        // One or Multiple PeptideMatch
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(TaskInfo.class);
        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        TasksPanel p = new TasksPanel();
        p.initListener();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    
    }

    @Override
    public void dataChanged() {
        // never called
    }
    
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        if (parameterType!= null ) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(TaskInfo.class)) {
                    return ((TasksPanel) getDataBoxPanelInterface()).getSelectedTaskInfo();
                }
            }

        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }
    
    @Override
    public void setEntryData(Object data) {
        // never called
    }

    @Override
    public void windowClosed() {
         m_updateTimer.stop();
         super.windowClosed();
    }
    
    @Override
    public void windowOpened() {    
        if (m_updateTimer == null) {
            ActionListener taskPerformer = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    ((TasksPanel)getDataBoxPanelInterface()).updateData();
                }
            };
            m_updateTimer = new Timer(UPDATE_DELAY, taskPerformer);

        }
        
        m_updateTimer.start();

    }


    
}
