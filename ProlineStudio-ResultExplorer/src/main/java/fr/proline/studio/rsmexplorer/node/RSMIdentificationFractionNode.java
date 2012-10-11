package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author JM235353
 */
public class RSMIdentificationFractionNode extends RSMNode {

    private static ImageIcon icon = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/identificationFraction.png", false);

    public RSMIdentificationFractionNode(AbstractData data) {
        super(NodeTypes.IDENTIFICATION_FRACTION, data);
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
