package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.util.ImageUtilities;

/**
 *
 * @author JM235353
 */
public abstract class RSMNode extends DefaultMutableTreeNode implements Cloneable {

   
    
    public enum NodeTypes {

        TREE_PARENT,
        PROJECT,
        DATA_SET,
        HOUR_GLASS
    }
    private static Action[] actionInstance = null;
    protected NodeTypes type;


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

    public ImageIcon getIcon() {
        return null;
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
    

    
}
