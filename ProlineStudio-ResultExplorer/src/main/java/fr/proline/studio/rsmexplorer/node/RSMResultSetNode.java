package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import java.awt.Image;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Node for a ResultSet
 * @author JM235353
 */
public class RSMResultSetNode extends RSMNode {

    private static Image icon = ImageUtilities.loadImage("fr/proline/studio/rsmexplorer/images/resultSet.png");

    public RSMResultSetNode(Children children, Lookup lookup, AbstractData data) {
        super(children, lookup, NodeTypes.RESULT_SET, data);
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canRename() {
        return true;
    }
}
