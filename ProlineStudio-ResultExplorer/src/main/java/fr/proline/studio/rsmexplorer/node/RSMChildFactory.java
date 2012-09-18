/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.Data;
import fr.proline.studio.dam.data.IdentificationData;
import fr.proline.studio.dam.data.ResultSetData;
import fr.proline.studio.dam.data.ResultSummaryData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author JM235353
 */
public class RSMChildFactory extends ChildFactory<Data> {

    private Data m_data;
    
    /*private static HashMap<ContainerData, RSMChildFactory> childFactoriesMap = new HashMap<ContainerData, RSMChildFactory>();

    public static RSMChildFactory getChildFactory(ContainerData key) {
        RSMChildFactory childFactory = childFactoriesMap.get(key);
        if (childFactory == null) {
            childFactory = new RSMChildFactory();
            childFactoriesMap.put(key, childFactory);
        }
        return childFactory;
    }*/

    public RSMChildFactory(Data data) {
        m_data = data;
    }
    

    @Override
    protected boolean createKeys(List<Data> list) {
        
        m_data.load(list);
        
        
        return true;
    }

    @Override
    protected Node createNodeForKey(Data key) {

        Node result = null;

        Data.DataTypes type = key.getDataType();
        switch (type) {
            case PROJECT:
                result = new RSMProjectNode(Children.create(new RSMChildFactory(key), true), Lookups.singleton(key), key);
                break;
            case RESULT_SET:
                result = new RSMResultSetNode(Children.LEAF, Lookups.singleton(key), key);

                break;
            case RESULT_SUMMARY:
                result = new RSMResultSummaryNode(Children.create(new RSMChildFactory(key), true), Lookups.singleton(key), key);
                break;
            case CONTEXT:
                result = new RSMContextNode(Children.create(new RSMChildFactory(key), true), Lookups.singleton(key), key);
                break;
        }

        return result;
    }
}
