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
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.gui.dialog.ManageSaveWindowsDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to manage the list of Saved User Windows
 * @author JM235353
 */
public class ManageUserWindowsAction extends AbstractRSMAction {

    private char m_windowType;
    
    public ManageUserWindowsAction(char saveWindowType, AbstractTree tree) {
        super(NbBundle.getMessage(ManageUserWindowsAction.class, "CTL_ManagerUserWindowsAction"), tree);
        m_windowType = saveWindowType;
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        ManageSaveWindowsDialog dialog = ManageSaveWindowsDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }
    
    private boolean isForRSM(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSM;
    }
    
    private boolean isForRset(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_RSET;
    }
    
    private boolean isForQuanti(){
        return m_windowType == WindowSavedManager.SAVE_WINDOW_FOR_QUANTI;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        boolean enable = false;
        
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (isForRSM() && WindowSavedManager.hasResultSummaryParameter(wndSaved)) {
                enable = true;
                break;
            } else if (isForRset() && WindowSavedManager.hasResultSetParameter(wndSaved)) {
                enable = true;
                break;
            }else if (isForQuanti() && WindowSavedManager.hasQuantiParameter(wndSaved)) {
                enable = true;
                break;
            }
        }
        
        setEnabled(enable);
    }
    
}
