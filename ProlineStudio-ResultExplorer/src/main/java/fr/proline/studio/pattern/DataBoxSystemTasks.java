/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.studio.rsmexplorer.gui.SystemTasksPanel;

/**
 *
 * @author VD225637
 */
public class DataBoxSystemTasks extends AbstractDataBox  {
    
      
    public DataBoxSystemTasks() {
        super(DataboxType.DataBoxSystemTasks, DataboxStyle.STYLE_UNKNOWN);
        // Name of this databox
        m_typeName = "System Tasks List";
    }
    
    @Override
    public void createPanel() {
        SystemTasksPanel p = new SystemTasksPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;               
    }

    @Override
    public void dataChanged() {
        //
    }
    
}
