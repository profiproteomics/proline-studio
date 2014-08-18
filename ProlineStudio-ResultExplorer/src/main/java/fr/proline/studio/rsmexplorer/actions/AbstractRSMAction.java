package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.gui.dialog.xic.DesignTree;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.IdentificationTree;
import fr.proline.studio.rsmexplorer.node.QuantitationTree;
import fr.proline.studio.rsmexplorer.node.RSMTree;
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

    private RSMTree.TreeType m_treeType;
 
    public AbstractRSMAction(String name, RSMTree.TreeType treeType) {
        super(name);
        
        m_treeType = treeType;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {

        RSMTree tree = null;
        switch (m_treeType) {
            case TREE_IDENTIFICATION:
                tree = IdentificationTree.getCurrentTree(); 
                break;
            case TREE_QUANTITATION:
                tree = QuantitationTree.getCurrentTree(); 
                break;
            case TREE_XIC_DESIGN:
                tree = DesignTree.getDesignTree();
                break;
            default:
                return; // should not happen
        }
    
        TreePath treePath = tree.getSelectionPath();

        tree.getPathBounds(treePath);

        Rectangle r = tree.getPathBounds(treePath);
        Point p = tree.getLocationOnScreen();

        actionPerformed(tree.getSelectedNodes(), p.x + r.x + r.width / 2, p.y + r.y + r.height / 2);

    }
    
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
    }
    
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public abstract void updateEnabled(RSMNode[] selectedNodes);

    
}
