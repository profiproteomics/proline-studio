/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import static javax.swing.Action.NAME;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author AK249877
 */
public class CreateAction extends AbstractRSMAction {

    private CreateSampleNodeAction m_createSampleAction;
    private CreateGroupNodeAction m_createGroupAction;

    private JMenu m_menu;
    private final XICDesignTree m_tree;

    public CreateAction(XICDesignTree tree) {
        super(NbBundle.getMessage(CreateAction.class, "CTL_CreateAction"), AbstractTree.TreeType.TREE_XIC_DESIGN, tree);
        m_tree = tree;
    }

    @Override
    public JMenuItem getPopupPresenter() {

        if (m_menu == null) {

            m_menu = new JMenu((String) getValue(NAME));           

            m_createSampleAction = new CreateSampleNodeAction(m_tree);
            m_createGroupAction = new CreateGroupNodeAction(m_tree);

            JMenuItem renameItem = new JMenuItem(m_createSampleAction);
            JMenuItem searchNameItem = new JMenuItem(m_createGroupAction);

            m_menu.add(renameItem);
            m_menu.add(searchNameItem);

        }

        return m_menu;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_createSampleAction.updateEnabled(selectedNodes);
        m_createGroupAction.updateEnabled(selectedNodes);

        m_menu.setEnabled(m_createSampleAction.isEnabled() || m_createGroupAction.isEnabled());

        setEnabled(m_menu.isEnabled());

    }

}
