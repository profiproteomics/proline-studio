package fr.proline.studio.rserver.actions;


import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

/**
 * Base Class for actions
 * @author JM235353
 */
public abstract class AbstractRAction extends AbstractAction {

    public AbstractRAction(String name) {
        super(name);
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        RTree tree = RTree.getTree();
        
        TreePath treePath = tree.getSelectionPath();
        
        tree.getPathBounds(treePath);
        
        Rectangle r = tree.getPathBounds(treePath);
        Point p = tree.getLocationOnScreen();

        actionPerformed(tree.getSelectedNodes(), p.x+r.x+r.width/2, p.y+r.y+r.height/2);
    }
    
    public void actionPerformed(RNode[] selectedNodes, int x, int y) { 
    }
    
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public abstract void updateEnabled(RNode[] selectedNodes);

    
}
