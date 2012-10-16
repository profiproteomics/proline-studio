package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * Node for the opened Project
 * @author JM235353
 */
public class RSMProjectNode extends RSMNode {

    private static ImageIcon icon = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/project.png", false);

    public RSMProjectNode(AbstractData data) {
        super(RSMNode.NodeTypes.TREE_PARENT, data);
    }

    /*@Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canRename() {
        return false;
    }*/
}
