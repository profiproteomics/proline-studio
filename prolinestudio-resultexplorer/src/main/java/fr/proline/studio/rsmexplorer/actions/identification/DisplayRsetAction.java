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
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 * Action with a Menu for the Display of Rset (Search Result)
 * @author JM235353
 */
public class DisplayRsetAction extends AbstractRSMAction {

   private DisplayRsetPeptidesAction m_displayRsetPeptidesAction;
   private DisplayUserWindowAction m_displayUserWindowAction;
   private DisplayRsetProteinMatchesAction m_displayRsetProteinMatchesAction;
   private DisplayMSQueryForRsetAction m_displayMSQueryAction;
   private GenerateMSDiagReportAction m_msDiagReportAction;
   private ManageUserWindowsAction m_manageUserWindowsAction;
   private ArrayList<DisplaySavedWindowAction> m_displaySavedWindowActionList;

   private JMenu m_menu;

   public DisplayRsetAction(AbstractTree tree) {
       super(NbBundle.getMessage(DisplayRsetAction.class, "CTL_DisplayRsetAction"), tree);
   }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        
        m_displayMSQueryAction = new DisplayMSQueryForRsetAction(getTree());
        m_displayRsetPeptidesAction = new DisplayRsetPeptidesAction(getTree());
        m_displayRsetProteinMatchesAction = new DisplayRsetProteinMatchesAction(getTree());
        m_msDiagReportAction = new GenerateMSDiagReportAction(getTree());
        m_manageUserWindowsAction = new ManageUserWindowsAction(WindowSavedManager.SAVE_WINDOW_FOR_RSET, getTree());
        m_displayUserWindowAction = new DisplayUserWindowAction(WindowSavedManager.SAVE_WINDOW_FOR_RSET, getTree());
        
        
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        m_displaySavedWindowActionList = new ArrayList<>();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (!WindowSavedManager.hasResultSetParameter(wndSaved)) {
                continue;
            }
            String name = WindowSavedManager.getWindowName(wndSaved);
            m_displaySavedWindowActionList.add(new DisplaySavedWindowAction(name, i, getTree()));
        }
       
        JMenuItem displayMSQueryItem = new JMenuItem(m_displayMSQueryAction);
        JMenuItem displayRsetPeptidesItem = new JMenuItem(m_displayRsetPeptidesAction);
        JMenuItem displayRsetProteinMatchesItem = new JMenuItem(m_displayRsetProteinMatchesAction);
        JMenuItem displayMsDiagReportItem = new JMenuItem(m_msDiagReportAction);
        JMenuItem displayUserWindowItem = new JMenuItem(m_displayUserWindowAction);
        JMenuItem manageUserWindowsItem = new JMenuItem(m_manageUserWindowsAction);
             
        m_menu.add(displayMSQueryItem);
        m_menu.add(displayRsetPeptidesItem);
        m_menu.add(displayRsetProteinMatchesItem);
        m_menu.addSeparator();
        m_menu.add(displayMsDiagReportItem);
        m_menu.addSeparator();
        m_menu.add(displayUserWindowItem);
        m_menu.add(manageUserWindowsItem);
        int nbUserWindows = m_displaySavedWindowActionList.size();
        if (nbUserWindows>0) {
            m_menu.addSeparator();
        }
         for (int i = 0; i <nbUserWindows ; i++) {
            m_menu.add(new JMenuItem(m_displaySavedWindowActionList.get(i)));
        }

        return m_menu;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        
        m_displayRsetPeptidesAction.updateEnabled(selectedNodes);
        m_displayRsetProteinMatchesAction.updateEnabled(selectedNodes);
        m_displayMSQueryAction.updateEnabled(selectedNodes);
        m_msDiagReportAction.updateEnabled(selectedNodes);
        m_displayUserWindowAction.updateEnabled(selectedNodes);
        m_manageUserWindowsAction.updateEnabled(selectedNodes);
        
        boolean listEnabled = false;
        for (int i=0;i<m_displaySavedWindowActionList.size();i++) {
            m_displaySavedWindowActionList.get(i).updateEnabled(selectedNodes);
            listEnabled |= m_displaySavedWindowActionList.get(i).isEnabled();
        }
        
        boolean isEnabled = m_displayRsetPeptidesAction.isEnabled() || m_displayRsetProteinMatchesAction.isEnabled() || m_displayMSQueryAction.isEnabled() || m_msDiagReportAction.isEnabled() || m_displayUserWindowAction.isEnabled() || m_manageUserWindowsAction.isEnabled() || listEnabled;
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

   
}