/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.PropertiesWrapperNode;
import java.awt.BorderLayout;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;

/**
 *
 * @author JM235353
 */
public class PropertiesTopComponent extends TopComponent {
 
    private PropertySheet m_propertySheet = null;
    
    public PropertiesTopComponent(String name) {
        
        setName(name);
        
        m_propertySheet = new PropertySheet();
        
        setLayout (new BorderLayout ());
        add(m_propertySheet, BorderLayout.CENTER);
    }
    
    public void setNodes(RSMNode[] nodes) {
        
        int nbNodes = nodes.length;
        
        PropertiesWrapperNode[] wrappedNodes = new PropertiesWrapperNode[nbNodes];
        for (int i=0;i<nbNodes;i++) {
            wrappedNodes[i] = new PropertiesWrapperNode(nodes[i]);
        }
        
        m_propertySheet.setNodes(wrappedNodes);
    }
    
}
