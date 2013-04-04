package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public abstract class AbstractRSMAction extends AbstractAction {

    public AbstractRSMAction(String name) {
        super(name);
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        RSMTree tree = RSMTree.getTree();
        
        TreePath treePath = tree.getSelectionPath();
        
        tree.getPathBounds(treePath);
        
        Rectangle r = tree.getPathBounds(treePath);
        Point p = tree.getLocationOnScreen();
        
        
        
        actionPerformed(tree.getSelectedNodes(), p.x+r.x+r.width/2, p.y+r.y+r.height/2);
    }
    
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
    }
    
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public abstract void updateEnabled(RSMNode[] selectedNodes);

    
}
