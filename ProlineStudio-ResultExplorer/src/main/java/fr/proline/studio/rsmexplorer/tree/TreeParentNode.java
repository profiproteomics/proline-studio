package fr.proline.studio.rsmexplorer.tree;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Node used a the Tree Parent
 * @author JM235353
 */
public class TreeParentNode extends AbstractNode {
 
    public TreeParentNode(AbstractData data) {
        super(AbstractNode.NodeTypes.TREE_PARENT, data);
    }

    @Override
    public String toString() {
        return "My Projects";
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.USER);
    }
    
    @Override
    public void loadDataForProperties(Runnable callback) {
        // nothing to do
        callback.run();
    }
    
    @Override
    public Sheet createSheet() {
        return null; // should never be called
    }
    
        @Override
    public AbstractNode copyNode() {
        return null;
    }
    
}
