/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.ContainerData;
import fr.proline.studio.dam.ContextData;
import fr.proline.studio.dam.ResultSetData;
import fr.proline.studio.dam.ResultSummaryData;
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
public class RSMChildFactory extends ChildFactory<ContainerData> {

    private static HashMap<ContainerData, RSMChildFactory> childFactoriesMap = new HashMap<ContainerData, RSMChildFactory>();

    public static RSMChildFactory getChildFactory(ContainerData key) {
        RSMChildFactory childFactory = childFactoriesMap.get(key);
        if (childFactory == null) {
            childFactory = new RSMChildFactory();
            childFactoriesMap.put(key, childFactory);
        }
        return childFactory;
    }

    private RSMChildFactory() {
    }

    @Override
    protected boolean createKeys(List<ContainerData> list) {
        ContainerData[] objs = new ContainerData[3];
        objs[0] = new ContextData();
        objs[1] = new ResultSetData();
        objs[2] = new ResultSummaryData();

        list.addAll(Arrays.asList(objs));
        return true;
    }

    @Override
    protected Node createNodeForKey(ContainerData key) {

        Node result = null;

        ContainerData.DataTypes type = key.getDataType();
        switch (type) {
            case RESULT_SET:
                result = new RSMResultSetNode(Children.LEAF, Lookups.singleton(key), key);

                break;
            case RESULT_SUMMARY:
                result = new RSMResultSummaryNode(Children.create(getChildFactory(key), false), Lookups.singleton(key), key);
                break;
            case CONTEXT:
                result = new RSMContextNode(Children.create(getChildFactory(key), false), Lookups.singleton(key), key);
                break;
        }

        return result;
    }
}
