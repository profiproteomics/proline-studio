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

package fr.proline.studio.rsmexplorer;

import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.dock.TopPanelListener;
import fr.proline.studio.dock.container.DockComponent;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class DataBoxViewerTopPanel extends AbstractTopPanel {

    private WindowBox m_windowBox;

    public DataBoxViewerTopPanel(WindowBox windowBox) {

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
    public Action[] getActions(DockComponent component) {
        Action[] actions = super.getActions(component);

        Action renameAction = new AbstractAction("Rename...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                OptionDialog dialog = new OptionDialog(WindowManager.getDefault().getMainWindow(), "Rename", null, "New Name", OptionDialog.OptionDialogType.TEXTFIELD);
                dialog.setText(getName());
                dialog.setVisible(true);
                String newName = null;
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    newName = dialog.getText();
                }

                if ((newName != null) && (newName.length() > 0)) {
                    setName(newName);
                    component.setTitle(newName);
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

    public boolean warnBeforeClosing(){
        return !m_windowBox.getEntryBox().isClosable();
    }

    public String getWarnClosingMessage(){
        return m_windowBox.getEntryBox().getClosingWarningMessage();
    }

    @Override
    public void componentClosed() {

        m_windowBox.windowClosed();
    }

    @Override
    public Image getIcon() {
        return m_windowBox.getIcon();
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


    public ParameterList getInParameters(){
        return m_windowBox.getEntryBox().getInParameters();
    }

    public ParameterList getOutParameters(){
        return m_windowBox.getEntryBox().getOutParameters();
    }

    public Object getData(Class parameterType){
        return getData(parameterType, ParameterSubtypeEnum.SINGLE_DATA);
    }
    public Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype){
        return m_windowBox.getEntryBox().getData(parameterType, parameterSubtype);
    }

    public long getProjectId(){
        return m_windowBox.getEntryBox().getProjectId();
    }

    public void loadedDataModified(Long rsetId, Long rsmId, Class c, ArrayList modificationsList, byte reason) {
        //Test if information is pertinent for thos view
        if(m_windowBox.getEntryBox().isDataOfInterest(rsetId,rsmId, c )) {

            if(reason == DataBoxViewerManager.REASON_MODIF.REASON_CHANGE_TITLE.getReasonValue()){
                //VDS: modificationsList shound have 1 entry = new name.
                String title = (String) modificationsList.get(0);
                this.setName(title);
                fireTopPanelPropertyChange(TopPanelListener.TITLE_PROPERTY);
            } else {
                DataBoxViewerManager.REASON_MODIF reasonModif = DataBoxViewerManager.REASON_MODIF.getReasonModifFor(reason);
                if (reasonModif != null && reasonModif.shouldBeSaved()) {
                    //Set title as modified
                    String title = getName();
                    if (!title.endsWith(DataBoxViewerManager.MODIFIED_TITLE_SUFFIX))
                        title = title + " " + DataBoxViewerManager.MODIFIED_TITLE_SUFFIX;
                    this.setName(title);
                    fireTopPanelPropertyChange(TopPanelListener.TITLE_PROPERTY);
                } else { //Warning should add hasSaved property ?
                    //remove title modified
                    String title = getName();
                    if (title.endsWith(DataBoxViewerManager.MODIFIED_TITLE_SUFFIX))
                        title = title.substring(0, title.lastIndexOf(DataBoxViewerManager.MODIFIED_TITLE_SUFFIX) - 1);
                    this.setName(title);
                    fireTopPanelPropertyChange(TopPanelListener.TITLE_PROPERTY);
                }
                m_windowBox.getEntryBox().loadedDataModified(rsetId, rsmId, c, modificationsList, reason);
            }
        }
        repaint();
    }



    @Override
    public String getTopPanelIdentifierKey() {
        return null;
    }

    @Override
    public String getTitle() {
        return getName();
    }
}
