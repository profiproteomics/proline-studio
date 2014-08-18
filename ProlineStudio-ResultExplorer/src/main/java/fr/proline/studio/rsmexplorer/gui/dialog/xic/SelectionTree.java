package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 * Tree to select the identifications to drag and drop to the XIC design tree
 * @author JM235353
 */
public class SelectionTree extends RSMTree implements TreeWillExpandListener {

    public SelectionTree(RSMNode top, boolean loadAllAtOnce) {

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SelectionTransferHandler handler = new SelectionTransferHandler(true);
        setTransferHandler(handler);

        setDragEnabled(true);

        initTree(top);

        if (loadAllAtOnce) {
            loadAllAtOnce(top);
        } else {
            startLoading(top);
        }
        

    }

    @Override
    protected final void initTree(RSMNode top) {
        super.initTree(top);

        addTreeWillExpandListener(this);
    }

    @Override
    public void rename(RSMNode rsmNode, String newName) {
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

        TreePath path = event.getPath();
        RSMNode nodeExpanded = (RSMNode) path.getLastPathComponent();

        // check if the node contains a GlassHourNode (ie : children are not loaded)
        if (nodeExpanded.getChildCount() > 0) {
            RSMNode childNode = (RSMNode) nodeExpanded.getChildAt(0);
            if (childNode.getType() == RSMNode.NodeTypes.HOUR_GLASS) {

                startLoading(nodeExpanded);
            }
        }

    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }
}
