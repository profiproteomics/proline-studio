package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Biologic Group node used in XIC Design Tree
 * @author JM235353
 */
public class XICBiologicalGroupNode extends AbstractNode {

    public XICBiologicalGroupNode(AbstractData data) {
        super(AbstractNode.NodeTypes.BIOLOGICAL_GROUP, data);
    }
    
    @Override
    public ImageIcon getIcon(boolean expanded) {
        return getIcon(IconManager.IconType.BIOLOGICAL_GROUP);
    }

    @Override
    public Sheet createSheet() {
        return null;
    }

    @Override
    public AbstractNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }
    
    @Override
    public boolean canBeDeleted() {
        return true;
    }
    
}
