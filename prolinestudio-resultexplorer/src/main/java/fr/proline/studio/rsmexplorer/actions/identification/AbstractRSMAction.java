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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

/**
 * Base Class for actions on a Tree
 *
 * @author JM235353
 */
public abstract class AbstractRSMAction extends AbstractAction {

    private AbstractTree m_tree;

    public AbstractRSMAction(String name, AbstractTree tree) {
        super(name);
        m_tree = tree;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        TreePath treePath = getTree().getSelectionPath();

        Rectangle r = getTree().getPathBounds(treePath);
        Point p = getTree().getLocationOnScreen();

        int x = (r == null ? (p.x / 2) : (p.x + r.x + r.width / 2));
        int y = (r == null ? (p.y / 2) : (p.y + r.y + r.height / 2));
        actionPerformed(getTree().getSelectedNodes(), x, y);

    }


    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

    }

    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }

    public abstract void updateEnabled(AbstractNode[] selectedNodes);


    public AbstractTree getTree() {
        return m_tree;
    }
}
