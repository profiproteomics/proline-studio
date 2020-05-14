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
package fr.proline.studio.rsmexplorer;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.table.TableInfo;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * TopComponent for a windox box with databoxes
 * @author JM235353
 */
public class DataBoxViewerTopComponent extends TopComponent {

    private WindowBox m_windowBox = null;
    
    /**
     * Creates new form DataBoxViewerTopComponent
     */
    public DataBoxViewerTopComponent(WindowBox windowBox) {
        
        m_windowBox = windowBox;
        
        // Add panel
        setLayout(new GridLayout());
        add(windowBox.getPanel());
        
        // Set Name
        setName(windowBox.getName()); 
        
        // Set Tooltip
        setToolTipText(windowBox.getName()); 

    }
    
    public WindowBox getWindowBox(){
        return m_windowBox;
    }

    
    public void retrieveTableModels(ArrayList<TableInfo> list) {
        m_windowBox.retrieveTableModels(list);
    }
    
    @Override
    public Action[] getActions() {
        Action[] actions = super.getActions();

        Action renameAction = new AbstractAction("Rename...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Rename", null, "New Name", OptionDialog.OptionDialogType.TEXTFIELD);
                dialog.setText(getName());
                Object o = e.getSource();
                //dialog.setLocation(x, y);
                dialog.setVisible(true);
                String newName = null;
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    newName = dialog.getText();
                }

                if ((newName != null) && (newName.length() > 0)) {
                    setName(newName);
                    m_windowBox.getEntryBox().setUserName(newName);
                }
            }

        };

        List<Action> actionList;
        if (actions == null) {
            actionList = new ArrayList<>(2);
            actionList.add(renameAction);
        } else {
            actionList = new ArrayList<>(actions.length+2);
            actionList.add(renameAction);
            actionList.add(null);
            for (Action a : actions) {
                actionList.add(a);
            }
        }


        return actionList.toArray(new Action[0]);

    }

    
    @Override
    protected void componentOpened() {
        m_windowBox.windowOpened();
    }
    
    @Override
    protected void componentClosed() {
        m_windowBox.windowClosed();
    }
    
    @Override
    public Image getIcon() {
        return m_windowBox.getIcon();
    }
    
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        // JPM.HACK : force the default size after the first display of the window
        // I have not found another way to do it.
        if (firstPaint) {
            firstPaint = false;
            
            // size correctly the sub panels
            m_windowBox.resetDefaultSize();
        }
    }
    private boolean firstPaint = true;

    
    public HashSet<GroupParameter> getInParameters(){
        return m_windowBox.getEntryBox().getInParameters();
    }
    
    public ArrayList<GroupParameter> getOutParameters(){
        return m_windowBox.getEntryBox().getOutParameters();
    }
    
    public Object getData(Class parameterType){
        return getData(parameterType, null);
    }
    public Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype){
        return m_windowBox.getEntryBox().getData(parameterType, parameterSubtype);
    }
    
    public long getProjectId(){
        return m_windowBox.getEntryBox().getProjectId();
    }
    
    public void loadedDataModified(Long rsetId, Long rsmId, Class c, ArrayList modificationsList, int reason) {
        m_windowBox.getEntryBox().loadedDataModified(rsetId, rsmId, c, modificationsList, reason);
    }

}
