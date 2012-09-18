/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.Data;
import java.awt.Image;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author JM235353
 */
public class RSMProjectNode extends RSMNode {

    private static Image icon = ImageUtilities.loadImage("fr/proline/studio/rsmexplorer/images/userContext.png");

    public RSMProjectNode(Children children, Lookup lookup, Data data) {
        super(children, lookup, RSMNode.NodeTypes.TREE_PARENT, data);
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
        return false;
    }
}
