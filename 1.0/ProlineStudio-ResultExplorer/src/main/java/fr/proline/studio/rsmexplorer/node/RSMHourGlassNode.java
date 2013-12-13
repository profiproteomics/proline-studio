package fr.proline.studio.rsmexplorer.node;


import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;

/**
 * Node used for a part of the tree which is not already loaded
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
        return getIcon(IconManager.IconType.HOUR_GLASS);
    }


    
}
