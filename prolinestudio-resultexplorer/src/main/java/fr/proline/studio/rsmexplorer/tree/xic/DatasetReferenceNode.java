package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Tree Node representing a Dataset and RSM used as reference for XIC : Creating
 * XIC from identification Tree or cloning an existing XIC which have DS & RSM
 * references defines
 *
 * @author JM235353
 */
public class DatasetReferenceNode extends AbstractNode {

    private boolean m_invalidReference = false; // define if the reference DS has been modified since XIC was run
    private boolean m_isAggrgation;

    public DatasetReferenceNode(AbstractData data) {
        super(NodeTypes.DATASET_REFERENCE, data);
        this.m_isAggrgation = false;
    }

  public DatasetReferenceNode(AbstractData data, boolean isAggragation) {
        super(NodeTypes.DATASET_REFERENCE, data);
        this.m_isAggrgation = isAggragation;
    }
    public void setInvalidReference(boolean isIncorrect) {
        m_invalidReference = isIncorrect;
    }

    public boolean isInvalidReference() {
        return m_invalidReference;
    }

    @Override
    public String toString() {
        if (m_isAggrgation) {
            return super.toString();
        } else {
            return "Identification reference : " + super.toString();
        }
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {
        if (m_invalidReference) {
            return getIcon(IconManager.IconType.REFERENCE_RSM_ERR);
        } else {
            if (m_isAggrgation)
                return getIcon(IconManager.IconType.REFERENCE_AGRRE);
            else
                return getIcon(IconManager.IconType.REFERENCE_RSM);
        }
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
        return false;
    }
}
