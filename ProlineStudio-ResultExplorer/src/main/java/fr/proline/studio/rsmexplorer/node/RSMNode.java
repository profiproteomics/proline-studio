package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.actions.ChildParentAction;
import fr.proline.studio.rsmexplorer.actions.DeleteAction;
import fr.proline.studio.rsmexplorer.actions.DisplayAction;
import fr.proline.studio.rsmexplorer.actions.RenameAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author JM235353
 */
public class RSMNode extends AbstractNode {

    public enum NodeTypes {

        TREE_PARENT,
        RESULT_SET,
        RESULT_SUMMARY,
        IDENTIFICATION,
        IDENTIFICATION_FRACTION,
        HOUR_GLASS
    }
    private static Action[] actionInstance = null;
    private NodeTypes type;
    protected AbstractData data;

    public RSMNode(Children children, Lookup lookup, NodeTypes type, AbstractData data) {
        super(children, lookup);
        this.type = type;
        this.data = data;
    }

    public NodeTypes getType() {
        return type;
    }
    
    public AbstractData getData() {
        return data;
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
    public String getDisplayName() {
        if (data == null) {
            return "";
        }

        return data.getName();
    }

    @Override
    public void setName(String name) {
        //data.setName(name); //JPM.TODO
        super.setName(name);
    }

    @Override
    public Action[] getActions(boolean arg0) {

        if (actionInstance == null) {
            actionInstance = new Action[]{  DisplayAction.getInstance(),     // Display >
                                            null,                            // --------------
                                            ChildParentAction.getInstance(), // Child/Parent >
                                            RenameAction.getInstance(),      // Rename...
                                            DeleteAction.getInstance()};     // Delete...

        }

        return actionInstance;
    }
}
