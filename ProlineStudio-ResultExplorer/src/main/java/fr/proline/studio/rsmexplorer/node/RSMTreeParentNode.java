package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;

/**
 * Node used a the Tree Parent
 * @author JM235353
 */
public class RSMTreeParentNode extends RSMNode {

    public RSMTreeParentNode(AbstractData data) {
        super(RSMNode.NodeTypes.TREE_PARENT, data);
    }

    /*@Override
    public RSMNode cloneThis() {
        RSMTreeParentNode clonedNode = new RSMTreeParentNode((AbstractData) getUserObject());
        
        addClonedChildren(clonedNode);
        
        return clonedNode;
    }*/
    
}
