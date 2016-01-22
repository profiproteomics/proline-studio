package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.rsmexplorer.tree.xic.XICDesignTree;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

/**
 * Base Class for actions on a Tree
 * @author JM235353
 */
public abstract class AbstractRSMAction extends AbstractAction {

    private AbstractTree.TreeType m_treeType;
 
    public AbstractRSMAction(String name, AbstractTree.TreeType treeType) {
        super(name);
        
        m_treeType = treeType;
    }
    
    /**
     * Return the AbstractTree.TreeType from which the action has been called.
     * @return 
     */
    protected AbstractTree.TreeType getSourceTreeType(){
        return m_treeType;
    }
    
    public boolean isIdentificationTree() {
        return m_treeType == AbstractTree.TreeType.TREE_IDENTIFICATION;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {

        AbstractTree tree = null;
        switch (m_treeType) {
            case TREE_IDENTIFICATION:
                tree = IdentificationTree.getCurrentTree(); 
                break;
            case TREE_QUANTITATION:
                tree = QuantitationTree.getCurrentTree(); 
                break;
            case TREE_XIC_DESIGN:
                tree = XICDesignTree.getDesignTree();
                break;
            default:
                return; // should not happen
        }
    
        TreePath treePath = tree.getSelectionPath();

        Rectangle r = tree.getPathBounds(treePath);
        Point p = tree.getLocationOnScreen();

        int x = (r == null ? (p.x / 2) : (p.x + r.x + r.width / 2));
        int y = (r == null ? (p.y / 2) : (p.y + r.y + r.height / 2));
        actionPerformed(tree.getSelectedNodes(), x, y);

    }
    
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) { 
    }
    
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public abstract void updateEnabled(AbstractNode[] selectedNodes);

    
}
