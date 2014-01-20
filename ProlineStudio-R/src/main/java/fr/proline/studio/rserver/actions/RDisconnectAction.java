/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rserver.actions;

import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author JM235353
 */
public class RDisconnectAction extends AbstractRAction {

    public RDisconnectAction() {
        super("Disconnect...");
    }
    
    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        
        RServerManager.getRServerManager().close();
        
        RTree tree = RTree.getTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(selectedNodes[0]);
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        setEnabled(RServerManager.getRServerManager().isConnected());
    }
    
}
