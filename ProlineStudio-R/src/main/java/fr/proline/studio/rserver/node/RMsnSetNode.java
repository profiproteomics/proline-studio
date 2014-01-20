package fr.proline.studio.rserver.node;

import fr.proline.studio.rserver.data.AbstractRData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 *
 * @author JM235353
 */
public class RMsnSetNode extends RNode {

    public RMsnSetNode(AbstractRData data) {
        super(RNode.NodeTypes.MSN_SET, data);
    }


    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.MSN_SET);
    }
}
