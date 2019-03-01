package fr.proline.studio.rsmexplorer.tree;


import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Node used for a part of the tree which is not already loaded
 * @author JM235353
 */
public class HourGlassNode extends AbstractNode {
    
 
    
    public HourGlassNode(AbstractData data) {
        super(AbstractNode.NodeTypes.HOUR_GLASS, data);
    }
 

 
    
    @Override
    public String toString() {
        return "Loading...";
    }
    
    @Override
    public ImageIcon getIcon(boolean expanded) {
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
    public AbstractNode copyNode() {
        AbstractNode copy = new HourGlassNode(getData());
        return copy;
    }
    
    public boolean canBeDeleted() {
        return true;
    }
    
}
