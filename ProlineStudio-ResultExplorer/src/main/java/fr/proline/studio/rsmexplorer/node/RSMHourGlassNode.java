/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class RSMHourGlassNode extends RSMNode {
    
    
    private static ImageIcon icon = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/hourGlass.png", false);

    
    public RSMHourGlassNode(/*Children children,*/ AbstractData data) {
        super(RSMNode.NodeTypes.HOUR_GLASS, data);
    }
 

 
    
    public String toString() {
        return "Loading...";
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
        return false;
    }*/
    
}
