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

import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

import javax.swing.*;


/**
 * Action for the menu to display data for Modifications Data
 * @author JM235353
 */
public class DisplayPTMRsmAction extends AbstractRSMAction {


    private DisplayPTMSitesAction m_displayPtmSiteProtein;
    private DisplayPTMClustersAction m_displayPtmClusterProtein;

    private DisplayPTMSitesAction m_displayAnnotatedPtmSiteProtein;
    private DisplayPTMClustersAction m_displayAnnotatedPtmClusterProtein;



    private JMenu m_menu;

    public DisplayPTMRsmAction(AbstractTree tree) {
        super("Display Modifications ", tree);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));
        m_displayPtmSiteProtein = new DisplayPTMSitesAction(getTree());
        m_displayPtmClusterProtein = new DisplayPTMClustersAction(getTree());
        m_displayAnnotatedPtmSiteProtein = new DisplayPTMSitesAction(true, getTree());
        m_displayAnnotatedPtmClusterProtein = new DisplayPTMClustersAction(true , getTree());

        JMenuItem displayPtmSiteProteinItem = new JMenuItem(m_displayPtmSiteProtein);
        JMenuItem displayPtmClusterProteinItem = new JMenuItem(m_displayPtmClusterProtein);
        JMenuItem displayAnnotatedPtmSiteProteinItem = new JMenuItem(m_displayAnnotatedPtmSiteProtein);
        JMenuItem displayAnnotatedPtmClusterProteinItem = new JMenuItem(m_displayAnnotatedPtmClusterProtein);

        m_menu.add(displayPtmSiteProteinItem);
        m_menu.add(displayPtmClusterProteinItem);
        m_menu.addSeparator();
        m_menu.add(displayAnnotatedPtmSiteProteinItem);
        m_menu.add(displayAnnotatedPtmClusterProteinItem);

        return m_menu;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        m_displayPtmSiteProtein.updateEnabled(selectedNodes);
        m_displayPtmClusterProtein.updateEnabled(selectedNodes);
        m_displayAnnotatedPtmClusterProtein.updateEnabled(selectedNodes);
        m_displayAnnotatedPtmSiteProtein.updateEnabled(selectedNodes);

        boolean isEnabled =  m_displayPtmClusterProtein.isEnabled() ||  m_displayAnnotatedPtmClusterProtein.isEnabled() || m_displayPtmSiteProtein.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }
}