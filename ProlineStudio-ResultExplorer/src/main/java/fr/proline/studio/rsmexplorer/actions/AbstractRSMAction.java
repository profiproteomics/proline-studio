package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import fr.proline.studio.rsmexplorer.node.QuantitationTree;
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
public abstract class AbstractRSMAction extends AbstractAction {

    private boolean m_onIdentificationTree;
    
    public AbstractRSMAction(String name) {
        this(name, true);
    }
    public AbstractRSMAction(String name, boolean onIdentificationTree) {
        super(name);
        
        m_onIdentificationTree = onIdentificationTree;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (m_onIdentificationTree) {
            IdentificationTree tree = IdentificationTree.getCurrentTree();
            TreePath treePath = tree.getSelectionPath();
        
            tree.getPathBounds(treePath);
        
            Rectangle r = tree.getPathBounds(treePath);
            Point p = tree.getLocationOnScreen();

            actionPerformed(tree.getSelectedNodes(), p.x+r.x+r.width/2, p.y+r.y+r.height/2);
        } else {
            QuantitationTree tree = QuantitationTree.getCurrentTree();
            TreePath treePath = tree.getSelectionPath();
        
            tree.getPathBounds(treePath);
        
            Rectangle r = tree.getPathBounds(treePath);
            Point p = tree.getLocationOnScreen();

            actionPerformed(tree.getSelectedNodes(), p.x+r.x+r.width/2, p.y+r.y+r.height/2);
        }
    }
    
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
    }
    
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public abstract void updateEnabled(RSMNode[] selectedNodes);

    
}
