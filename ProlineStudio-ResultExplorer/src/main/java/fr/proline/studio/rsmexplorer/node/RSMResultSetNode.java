package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Node for a ResultSet
 * @author JM235353
 */
public class RSMResultSetNode extends RSMNode {

    private static ImageIcon icon = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/resultSet.png", false);

    public RSMResultSetNode(AbstractData data) {
        super(NodeTypes.RESULT_SET, data);
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }

    /*@Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canRename() {
        return true;
    }*/
}
