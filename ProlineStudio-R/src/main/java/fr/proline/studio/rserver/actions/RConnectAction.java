package fr.proline.studio.rserver.actions;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rserver.dialog.RServerConnectionDialog;
import fr.proline.studio.rserver.node.RNode;
import fr.proline.studio.rserver.node.RTree;
import javax.swing.tree.DefaultTreeModel;
import org.openide.windows.WindowManager;

/**
 * Action to connect to the Proline Server
 * @author JM235353
 */
public class RConnectAction extends AbstractRAction {

    public RConnectAction() {
        super("Connect...");
    }
    
    @Override
    public void actionPerformed(RNode[] selectedNodes, int x, int y) {
        RServerConnectionDialog dialog = RServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        RTree tree = RTree.getTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.nodeChanged(selectedNodes[0]);
    }
    
    @Override
    public void updateEnabled(RNode[] selectedNodes) {
        setEnabled(!RServerManager.getRServerManager().isConnected());
    }
    
}
