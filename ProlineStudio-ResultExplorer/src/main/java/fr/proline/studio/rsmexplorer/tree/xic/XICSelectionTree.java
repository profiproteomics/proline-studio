package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
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
public class XICSelectionTree extends AbstractTree implements TreeWillExpandListener {

    public XICSelectionTree(AbstractNode top, boolean loadAllAtOnce) {

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        XICTransferHandler handler = new XICTransferHandler(true, this);
        setTransferHandler(handler);

        setDragEnabled(true);

        initTree(top);

        if (loadAllAtOnce) {
            loadAllAtOnce(top, false);
        } else {
            startLoading(top, false);
        }
        

    }

    @Override
    protected final void initTree(AbstractNode top) {
        super.initTree(top);

        addTreeWillExpandListener(this);
    }

    @Override
    public void rename(AbstractNode rsmNode, String newName) {
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
        AbstractNode nodeExpanded = (AbstractNode) path.getLastPathComponent();

        // check if the node contains a GlassHourNode (ie : children are not loaded)
        if (nodeExpanded.getChildCount() > 0) {
            AbstractNode childNode = (AbstractNode) nodeExpanded.getChildAt(0);
            if (childNode.getType() == AbstractNode.NodeTypes.HOUR_GLASS) {
                startLoading(nodeExpanded, false);
            }
        }

    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }
}
