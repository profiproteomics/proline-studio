package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 * Node used a the Tree Parent
 * @author JM235353
 */
public class RSMTreeParentNode extends RSMNode {
 
    public RSMTreeParentNode(AbstractData data) {
        super(RSMNode.NodeTypes.TREE_PARENT, data);
    }

    @Override
    public String toString() {
        return "My Projects";
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.USER);
    }
    
}
