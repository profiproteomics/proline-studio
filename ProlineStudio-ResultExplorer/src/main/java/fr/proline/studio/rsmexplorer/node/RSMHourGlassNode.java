package fr.proline.studio.rsmexplorer.node;


import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Node used for a part of the tree which is not already loaded
 * @author JM235353
 */
public class RSMHourGlassNode extends RSMNode {
    
 
    
    public RSMHourGlassNode(AbstractData data) {
        super(RSMNode.NodeTypes.HOUR_GLASS, data);
    }
 

 
    
    @Override
    public String toString() {
        return "Loading...";
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.HOUR_GLASS);
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
        // nothing to do
        callback.run();
    }
    
    @Override
    public Sheet createSheet() {
        return null; // should never be called
    }

    @Override
    public RSMNode copyNode() {
        RSMNode copy = new RSMHourGlassNode(getData());
        return copy;
    }
    
}
