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
