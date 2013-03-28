package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.PropertiesProviderInterface;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.nodes.Sheet;

/**
 * Super class for all nodes
 * @author JM235353
 */
public abstract class RSMNode extends DefaultMutableTreeNode implements Cloneable, PropertiesProviderInterface {

   
    
    public enum NodeTypes {

        TREE_PARENT,
        PROJECT,
        DATA_SET,
        HOUR_GLASS
    }
    private static Action[] actionInstance = null;
    protected NodeTypes type;

    protected boolean isChanging = false;

    public RSMNode(/*Children children,*/ NodeTypes type, AbstractData data) {
        super(data);
        this.type = type;
    }

    public NodeTypes getType() {
        return type;
    }
    
    public AbstractData getData() {
        return (AbstractData) getUserObject();
    }

    public abstract ImageIcon getIcon();
    
    public ImageIcon getIcon(IconManager.IconType iconType) {
        if (isChanging) {
            return IconManager.getIconWithHourGlass(iconType);
        }
        return IconManager.getIcon(iconType);
    }
    
    public boolean searchChildNodeOfAType(NodeTypes type) {
        if (this.type == type) {
            return true;
        }

        /*
         * Node[] nodes = this.getChildren().getNodes();
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


    public boolean canBeDeleted() {
        return false;
    }
    
    public void setIsChanging(boolean isChanging) {
        this.isChanging = isChanging;
    }
    
    public boolean isChanging() {
        return isChanging;
    }
    
    @Override
    public abstract Sheet createSheet();
    
}
