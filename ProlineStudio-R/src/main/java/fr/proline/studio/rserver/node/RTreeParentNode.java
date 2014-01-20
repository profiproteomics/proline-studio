package fr.proline.studio.rserver.node;

import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.rserver.data.AbstractRData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 *
 * @author JM235353
 */
public class RTreeParentNode extends RNode {
 
    public RTreeParentNode(AbstractRData data) {
        super(RNode.NodeTypes.TREE_PARENT, data);
    }

    @Override
    public String toString() {
        return "R Server";
    }
    
    @Override
    public ImageIcon getIcon() {
        if (RServerManager.getRServerManager().isConnected()) {
            return getIcon(IconManager.IconType.SERVER_ON);
        } else {
            return getIcon(IconManager.IconType.SERVER_OFF);
        }
    }

}
