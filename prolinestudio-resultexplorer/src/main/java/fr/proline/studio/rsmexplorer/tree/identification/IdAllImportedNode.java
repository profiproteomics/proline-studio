package fr.proline.studio.rsmexplorer.tree.identification;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;



/**
 * Node for All Imported Search Results (rset)
 * @author JM235353
 */
public class IdAllImportedNode extends AbstractNode {
    
    public IdAllImportedNode(AbstractData data) {
        super(NodeTypes.DATA_ALL_IMPORTED, data);
    }
    
    @Override
    public ImageIcon getIcon(boolean expanded) {
        return getIcon(IconManager.IconType.ALL_IMPORTED);
    }

    @Override
    public Sheet createSheet() {
       return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }
    
    @Override
    public AbstractNode copyNode() {
        return null;
    }
}
