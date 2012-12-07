package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author JM235353
 */
public abstract class RSMNode extends DefaultMutableTreeNode implements Cloneable {

    public enum NodeTypes {

        TREE_PARENT,
        RESULT_SET,
        RESULT_SUMMARY,
        IDENTIFICATION,
        IDENTIFICATION_FRACTION,
        HOUR_GLASS
    }
    private static Action[] actionInstance = null;
    protected NodeTypes type;
    //protected AbstractData data;

    public RSMNode(/*Children children,*/ NodeTypes type, AbstractData data) {
        super(data);
        this.type = type;
        //this.data = data;
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

    /*@Override */
    public void setName(String name) {
        //data.setName(name); //JPM.TODO
     //   super.setName(name);
    }

    /*@Override
    public Action[] getActions(boolean arg0) {

        if (actionInstance == null) {
            actionInstance = new Action[]{  DisplayAction.getInstance(),     // Display >
                                            null,                            // --------------
                                            ChildParentAction.getInstance(), // Child/Parent >
                                            RenameAction.getInstance(),      // Rename...
                                            DeleteAction.getInstance()};     // Delete...

        }

        return actionInstance;
    }*/
 
    /*public abstract RSMNode cloneThis();
    
    protected void addClonedChildren(RSMNode clonedNode) {
        Enumeration en = clonedNode.children();

        while (en.hasMoreElements()) {
            RSMNode node = (RSMNode) en.nextElement();
            clonedNode.add(node.cloneThis());
        }
    }*/
    
}
