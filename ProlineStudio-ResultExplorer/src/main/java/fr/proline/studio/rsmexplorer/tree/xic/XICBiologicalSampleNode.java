package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Tree Node representing a Biological Sample
 * @author JM235353
 */
public class XICBiologicalSampleNode extends AbstractNode {

    public XICBiologicalSampleNode(AbstractData data) {
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
    public AbstractNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }
    
}
