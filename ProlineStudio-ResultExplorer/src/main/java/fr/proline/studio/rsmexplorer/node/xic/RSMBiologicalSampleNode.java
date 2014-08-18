package fr.proline.studio.rsmexplorer.node.xic;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 *
 * @author JM235353
 */
public class RSMBiologicalSampleNode extends RSMNode {

    public RSMBiologicalSampleNode(AbstractData data) {
        super(NodeTypes.BIOLOGICAL_SAMPLE, data);
    }
    
    @Override
    public ImageIcon getIcon() {
        return getIcon(IconManager.IconType.BIOLOGICAL_SAMPLE);
    }

    @Override
    public Sheet createSheet() {
        return null;
    }

    @Override
    public RSMNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }
    
}
