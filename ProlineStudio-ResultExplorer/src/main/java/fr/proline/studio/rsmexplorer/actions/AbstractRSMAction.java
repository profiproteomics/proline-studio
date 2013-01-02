package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
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
        RSMNode n = (RSMNode) treePath.getLastPathComponent();
        
        actionPerformed(n);
    }
    
    public void actionPerformed(RSMNode n) { 
    }
    
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public abstract void updateEnabled(RSMNode[] selectedNodes);

    
}
