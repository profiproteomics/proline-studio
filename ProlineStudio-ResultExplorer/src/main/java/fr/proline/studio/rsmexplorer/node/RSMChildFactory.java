package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Class used to create children of a note asynchronously
 * @author JM235353
 */
public class RSMChildFactory extends ChildFactory<AbstractData> {

    private AbstractData m_data;
   

    public RSMChildFactory(AbstractData data) {
        m_data = data;
    }
    

    @Override
    protected boolean createKeys(List<AbstractData> list) {
        
        m_data.load(list);
        
        
        return true;
    }

    @Override
    protected Node createNodeForKey(AbstractData key) {

        // result Node
        Node result = null;

        // Children of the Node
        Children children;
        if (key.hasChildren()) {
            // there are children in the database which will be loaded
            children = Children.create(new RSMChildFactory(key), true);
        } else {
            // no child in the database
            children = Children.LEAF;
        }
        
        // Creation of the correct Node type
        AbstractData.DataTypes type = key.getDataType();
        switch (type) {
            case PROJECT:
                result = new RSMProjectNode(children, Lookups.singleton(key), key);
                break;
            case RESULT_SET:
                result = new RSMResultSetNode(children, Lookups.singleton(key), key);
                break;
            case RESULT_SUMMARY:
                result = new RSMResultSummaryNode(children, Lookups.singleton(key), key);
                break;
            case CONTEXT:
                result = new RSMContextNode(children, Lookups.singleton(key), key);
                break;
        }

        return result;
    }
}
