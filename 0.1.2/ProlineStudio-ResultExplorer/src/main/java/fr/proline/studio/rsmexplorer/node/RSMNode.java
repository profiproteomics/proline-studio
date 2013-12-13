package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.PropertiesProviderInterface;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.nodes.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Super class for all nodes
 * @author JM235353
 */
public abstract class RSMNode extends DefaultMutableTreeNode implements Cloneable, PropertiesProviderInterface {

   
    
    public enum NodeTypes {

        TREE_PARENT,
        PROJECT,
        DATA_SET,
        DATA_ALL_IMPORTED,
        HOUR_GLASS
    }
    private static Action[] m_actionInstance = null;
    protected NodeTypes m_type;

    protected boolean m_isChanging = false;

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    public RSMNode(/*Children children,*/ NodeTypes type, AbstractData data) {
        super(data);
        m_type = type;
    }

    public NodeTypes getType() {
        return m_type;
    }
    
    public AbstractData getData() {
        return (AbstractData) getUserObject();
    }

    public abstract ImageIcon getIcon();
    
    public ImageIcon getIcon(IconManager.IconType iconType) {
        if (m_isChanging) {
            return IconManager.getIconWithHourGlass(iconType);
        }
        return IconManager.getIcon(iconType);
    }
    
    public boolean searchChildNodeOfAType(NodeTypes type) {
        if (m_type == type) {
            return true;
        }

        /*
         * Node[] nodes = getChildren().getNodes();
         *
         * for (Node nodeCur : nodes) { if (
         * ((RSMNode)nodeCur).searchChildNodeOfAType(type) ) { return true; }
        }
         */ //JPM.TODO : put back when the test code is removed


        return false;
    }

    @Override
    public String toString() {
        AbstractData data = getData();
        if (data == null) {
            return "";
        }

        return data.getName();
    }

    public boolean isInTrash() {
        return false;
    }
    
    public boolean canBeDeleted() {
        return false;
    }
    
    public void setIsChanging(boolean isChanging) {
        m_isChanging = isChanging;
    }
    
    public boolean isChanging() {
        return m_isChanging;
    }
    
    @Override
    public abstract Sheet createSheet();
    
}
