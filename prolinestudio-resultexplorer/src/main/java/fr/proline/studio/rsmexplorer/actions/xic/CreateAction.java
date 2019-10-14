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
import fr.proline.studio.rsmexplorer.tree.xic.QuantExperimentalDesignTree;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author AK249877
 */
public class CreateAction extends AbstractRSMAction {

    private CreateXICBiologicalNodeAction m_createSampleAction;
    private CreateXICBiologicalNodeAction m_createGroupAction;
    private CreateXICBiologicalNodeAction m_createChannelAction;

    private JMenu m_menu;
    private final QuantExperimentalDesignTree m_tree;
    private JMenuItem m_createSampleItem;
    private JMenuItem m_createGroupItem;
    private JMenuItem m_createChannelItem;

    public CreateAction(QuantExperimentalDesignTree tree) {
        super(NbBundle.getMessage(CreateAction.class, "CTL_CreateAction"), tree);
        m_tree = tree;
    }

    @Override
    public JMenuItem getPopupPresenter() {

        if (m_menu == null) {

            m_menu = new JMenu((String) getValue(NAME));
            m_createSampleAction = new CreateXICBiologicalNodeAction(
                    NbBundle.getMessage(CreateAction.class, "CTL_CreateSampleAction"), AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE, m_tree);
            m_createGroupAction = new CreateXICBiologicalNodeAction(
                    NbBundle.getMessage(CreateAction.class, "CTL_CreateGroupAction"), AbstractNode.NodeTypes.BIOLOGICAL_GROUP, m_tree);
            m_createChannelAction = new CreateXICBiologicalNodeAction(
                    NbBundle.getMessage(CreateAction.class, "CTL_CreateChannelAction"), AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS, m_tree);
            m_createSampleItem = new JMenuItem(m_createSampleAction);
            m_createGroupItem = new JMenuItem(m_createGroupAction);
            m_createChannelItem = new JMenuItem(m_createChannelAction);
            //JMenuItem will be added to m_menu in updateEnabled()
        }

        return m_menu;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        if (selectedNodes.length != 1) {
            this.setEnabled(false);
            return;
        }
        AbstractNode.NodeTypes selectedNodeType = selectedNodes[0].getType();
        m_menu.removeAll();
        switch (selectedNodeType) {
            case DATA_SET:
                m_menu.add(m_createGroupItem);
                m_menu.add(m_createSampleItem);
                m_menu.add(m_createChannelItem);
                break;
            case BIOLOGICAL_GROUP:
                m_menu.add(m_createSampleItem);
                break;
            case BIOLOGICAL_SAMPLE:
                m_menu.add(m_createChannelItem);
                break;
            default:
                break;
        }

        m_menu.setEnabled(m_menu.getItemCount() != 0);
        setEnabled(m_menu.isEnabled());
    }

}
