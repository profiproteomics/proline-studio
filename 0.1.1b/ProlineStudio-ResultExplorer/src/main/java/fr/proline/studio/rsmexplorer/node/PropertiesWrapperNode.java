package fr.proline.studio.rsmexplorer.node;


import fr.proline.studio.utils.PropertiesProviderInterface;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;


/**
 * This node class wraps a PropertiesProviderInterface, so we can display properties
 * with a PropertiesSheet (which accepts only netbean Nodes )
 * @author JM235353
 */
public class PropertiesWrapperNode extends AbstractNode {

    private PropertiesProviderInterface m_propertiesProvider;
    
    public PropertiesWrapperNode(PropertiesProviderInterface propertiesProvider) {
        super(new Children.Array());
        
        m_propertiesProvider = propertiesProvider;

    }
    
    @Override
    protected Sheet createSheet() {
        return m_propertiesProvider.createSheet();
    }
   

    
}
