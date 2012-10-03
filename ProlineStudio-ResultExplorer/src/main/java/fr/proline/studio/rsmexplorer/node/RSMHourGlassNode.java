/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;


import java.awt.Image;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author JM235353
 */
public class RSMHourGlassNode extends RSMNode {
    
    
    private static Image icon = ImageUtilities.loadImage("fr/proline/studio/rsmexplorer/images/hourGlass.png");

    public RSMHourGlassNode(Children children, Lookup lookup) {
        super(children, lookup, RSMNode.NodeTypes.HOUR_GLASS, null);
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
