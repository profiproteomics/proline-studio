/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;


import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author JM235353
 */
public class RSMHourGlassNode extends RSMNode {
    
 
    
    public RSMHourGlassNode(AbstractData data) {
        super(RSMNode.NodeTypes.HOUR_GLASS, data);
    }
 

 
    
    public String toString() {
        return "Loading...";
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.HOUR_GLASS);
    }

    /*@Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canRename() {
        return false;
    }*/
    
    /*@Override
    public RSMNode cloneThis() {
        RSMHourGlassNode clonedNode = new RSMHourGlassNode((AbstractData) getUserObject());
        
        addClonedChildren(clonedNode);
        
        return clonedNode;
    }*/
    
}
