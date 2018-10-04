package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Tree Node representing a Dataset and RSM used as reference for XIC :
 * Creating XIC from identification Tree or cloning an existing XIC which have DS & RSM references defines
 * 
 * @author JM235353
 */
public class XICReferenceRSMNode extends AbstractNode {

    private boolean m_refDSIncorrect = false; // define if the reference DS has been modified since XIC was run
    
    public XICReferenceRSMNode(AbstractData data) {
        super(NodeTypes.REFERENCE_RSM, data);
    }

    public void setRefDatasetIncorrect(boolean isIncorrect){
        m_refDSIncorrect = isIncorrect;
    }
    
    public boolean isRefDatasetIncorrect(){
        return m_refDSIncorrect;
    }
    
    @Override
    public String toString() {
        return "Reference : "+ super.toString();
    }
    
    
    @Override
    public ImageIcon getIcon(boolean expanded) {
        if(m_refDSIncorrect)
            return getIcon(IconManager.IconType.REFERENCE_RSM_ERR);
        else
            return getIcon(IconManager.IconType.REFERENCE_RSM);
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
