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
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * Action to display the experimental design: menu with exp. design parameters
 * and map alignment
 *
 * @author MB243701
 */
public class DisplayExperimentalDesignAction extends AbstractRSMAction {

    private AbstractRSMAction m_xicParamAction;
    private AbstractRSMAction m_mapAlignmentAction;
    private AbstractRSMAction m_mapMozAlignmentAction;

    private JMenu m_menu;

    public DisplayExperimentalDesignAction(AbstractTree tree) {
        super("Display Exp. Design", tree);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));              
    
        m_xicParamAction = new DisplayXicParamAction(getTree());
        m_mapAlignmentAction = new DisplayMapAlignmentAction(getTree());
        m_mapMozAlignmentAction = new DisplayMapMoZAlignmentAction(getTree());

        JMenuItem xicParamItem = new JMenuItem(m_xicParamAction);
        JMenuItem mapAlignItem = new JMenuItem(m_mapAlignmentAction);
        JMenuItem mapMoZAlignItem = new JMenuItem(m_mapMozAlignmentAction);

        m_menu.add(xicParamItem);
        m_menu.add(mapAlignItem);
        m_menu.add(mapMoZAlignItem);

        return m_menu;
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_xicParamAction.updateEnabled(selectedNodes);
        m_mapAlignmentAction.updateEnabled(selectedNodes);
        m_mapMozAlignmentAction.updateEnabled(selectedNodes);
        
        boolean isEnabled = m_xicParamAction.isEnabled() ||  m_mapAlignmentAction.isEnabled() ||m_mapMozAlignmentAction.isEnabled();
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }
}
