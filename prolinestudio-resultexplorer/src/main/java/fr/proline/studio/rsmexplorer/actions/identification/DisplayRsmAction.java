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
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * Action for the menu to display data for Identification Summary
 * @author JM235353
 */
public class DisplayRsmAction extends AbstractRSMAction {

    private DisplayRsmPSMAction m_displayRsmPSMAction;
    private DisplayRsmPeptidesAction m_displayRsmPeptidesAction;
    private DisplayRsmProteinSetsAction m_displayRsmProteinSetsAction;
//    private DisplayPTMSitesAction m_displayPtmSiteProtein;
//    private DisplayPTMClustersAction m_displayPtmClusterProtein;
    

    private DisplayAdjacencyMatrixAction m_displayAdjacencyMatrixAction;
    private DisplayMSQueryAction m_displayMSQueryAction;
    
    private DisplayUserWindowAction m_displayUserWindowAction;
    private ManageUserWindowsAction m_manageUserWindowsAction;
    private ArrayList<DisplaySavedWindowAction> m_displaySavedWindowActionList;
    
    private JMenu m_menu;

    public DisplayRsmAction(AbstractTree tree) {
        super("Display Identification Summary", tree);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));

        m_displayMSQueryAction = new DisplayMSQueryAction(getTree());
        m_displayRsmPSMAction = new DisplayRsmPSMAction(getTree());
        m_displayRsmPeptidesAction = new DisplayRsmPeptidesAction(getTree());
        m_displayRsmProteinSetsAction = new DisplayRsmProteinSetsAction(getTree());
//        m_displayPtmSiteProtein = new DisplayPTMSitesAction(getTree());
//        m_displayPtmClusterProtein = new DisplayPTMClustersAction(getTree());
        m_displayAdjacencyMatrixAction = new DisplayAdjacencyMatrixAction(getTree());
        
        
        m_manageUserWindowsAction = new ManageUserWindowsAction(WindowSavedManager.SAVE_WINDOW_FOR_RSM, getTree());
        m_displayUserWindowAction = new DisplayUserWindowAction(WindowSavedManager.SAVE_WINDOW_FOR_RSM, getTree());
        
        ArrayList<String> savedWindowsList = WindowSavedManager.readSavedWindows();
        int nb = savedWindowsList.size();
        m_displaySavedWindowActionList = new ArrayList<>();
        for (int i = 0; i < nb; i++) {
            String wndSaved = savedWindowsList.get(i);
            if (!WindowSavedManager.hasResultSummaryParameter(wndSaved)) {
                continue;
            }
            String name = WindowSavedManager.getWindowName(wndSaved);
            m_displaySavedWindowActionList.add(new DisplaySavedWindowAction(name, i, getTree()));
        }


        JMenuItem displayMSQueryItem = new JMenuItem(m_displayMSQueryAction);
        JMenuItem displayRsmPSMItem = new JMenuItem(m_displayRsmPSMAction);
        JMenuItem displayRsmPeptidesItem = new JMenuItem(m_displayRsmPeptidesAction);
        JMenuItem displayRsmProteinSetsItem = new JMenuItem(m_displayRsmProteinSetsAction);
//        JMenuItem displayPtmSiteProteinItem = new JMenuItem(m_displayPtmSiteProtein);
//        JMenuItem displayPtmClusterProteinItem = new JMenuItem(m_displayPtmClusterProtein);
        JMenuItem displayAdjacencyMatrixItem = new JMenuItem(m_displayAdjacencyMatrixAction);
        JMenuItem displayUserWindowItem = new JMenuItem(m_displayUserWindowAction);
        JMenuItem manageUserWindowsItem = new JMenuItem(m_manageUserWindowsAction);

        m_menu.add(displayMSQueryItem);
        m_menu.add(displayRsmPSMItem);
        m_menu.add(displayRsmPeptidesItem);
        m_menu.add(displayRsmProteinSetsItem);        
        m_menu.add(displayAdjacencyMatrixItem);
        m_menu.addSeparator();
//        m_menu.add(displayPtmSiteProteinItem);
//        m_menu.add(displayPtmClusterProteinItem);
//        m_menu.addSeparator();
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

        m_displayRsmPSMAction.updateEnabled(selectedNodes);
        m_displayRsmPeptidesAction.updateEnabled(selectedNodes);
        m_displayRsmProteinSetsAction.updateEnabled(selectedNodes);
//        m_displayPtmSiteProtein.updateEnabled(selectedNodes);
//        m_displayPtmClusterProtein.updateEnabled(selectedNodes);
        m_displayAdjacencyMatrixAction.updateEnabled(selectedNodes);
        m_displayUserWindowAction.updateEnabled(selectedNodes);
        m_manageUserWindowsAction.updateEnabled(selectedNodes);
        m_displayMSQueryAction.updateEnabled(selectedNodes);
        
        boolean listEnabled = false;
        for (int i=0;i<m_displaySavedWindowActionList.size();i++) {
            m_displaySavedWindowActionList.get(i).updateEnabled(selectedNodes);
            listEnabled |= m_displaySavedWindowActionList.get(i).isEnabled();
        }

        boolean isEnabled = m_displayRsmPSMAction.isEnabled() || m_displayRsmPeptidesAction.isEnabled() ||/* m_displayPtmClusterProtein.isEnabled() || */m_displayRsmProteinSetsAction.isEnabled()|| /*m_displayPtmSiteProtein.isEnabled() || */m_displayMSQueryAction.isEnabled() || m_displayUserWindowAction.isEnabled()|| m_manageUserWindowsAction.isEnabled() || listEnabled;
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);

    }
}