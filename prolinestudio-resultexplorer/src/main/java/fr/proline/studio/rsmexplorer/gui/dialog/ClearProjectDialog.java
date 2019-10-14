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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.ClearProjectData;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * dialog to clear rs and rsm of in a given project
 * @author MB243701
 */
public class ClearProjectDialog extends DefaultDialog {
    
    private boolean m_canModifyValues;
    
    private  Project m_project;
    private ClearProjectPanel m_clearProjectPanel = null;
    
    private  List<ClearProjectData> m_listDataToClear = null;
    
    private DefaultDialog.ProgressTask m_task = null;
    
   
    
    public ClearProjectDialog(Window parent, Project p, List<ClearProjectData> listDataToClear) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        m_project = p;
        m_listDataToClear = listDataToClear;

        setResizable(true);

        setTitle("Clean up Project "+m_project.getName());

        //setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:clearproject");

        setButtonVisible(BUTTON_DEFAULT, false);
        
        m_clearProjectPanel = new ClearProjectPanel();
        this.setInternalComponent(m_clearProjectPanel);
        initialize();
    }
    
    
    private void initialize() {
        m_canModifyValues = true;
        if (m_project == null){
            
        }else{
            m_canModifyValues = DatabaseDataManager.getDatabaseDataManager().ownProject(m_project);
            m_clearProjectPanel.setData(m_listDataToClear);
        }
        updateEnabled(m_canModifyValues);
    }
    
    public boolean canModifyValues(){
        return m_canModifyValues;
    }
    
    private void updateEnabled(boolean canModifyValues) {
        
    }
    
    public List<ClearProjectData> getSelectedData(){
        return m_clearProjectPanel.getSelectedData();
    }
    
    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }
    
    @Override
    protected boolean okCalled() {
        List<ClearProjectData> list = getSelectedData();
        
        if (!list.isEmpty()) {
            Object[] options = {"Yes, delete", "No, cancel"};
            int n = JOptionPane.showOptionDialog(this,
                    "Are you sure you want to delete the selected data? (can not be undone)",
                    "Delete Data",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (n == JOptionPane.YES_OPTION) {
                startTask(m_task);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }
    
    @Override
    protected boolean cancelCalled() {
        return true;
    }
}
