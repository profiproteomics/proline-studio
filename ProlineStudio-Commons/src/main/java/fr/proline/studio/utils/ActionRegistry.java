package fr.proline.studio.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;

/**
 *
 * @author CB205360
 */
public class ActionRegistry {

    private static ActionRegistry m_instance = new ActionRegistry();

    public static ActionRegistry getInstance() {
        return m_instance;
    }
    
    private Map<Class, List<Action>> actionRegistry = new HashMap<>();
    
    public void registerAction(Class nodeClass, Action action) {
        if (!actionRegistry.containsKey(nodeClass)) 
            actionRegistry.put(nodeClass, new ArrayList<>());
        actionRegistry.get(nodeClass).add(action);
    }
    
    public boolean unregisterAction(Class nodeClass, Action action) {
        if (!actionRegistry.containsKey(nodeClass)) 
            return false;
        return actionRegistry.get(nodeClass).remove(action);
    }
    
    public List<Action> getActions(Class key) {
        return actionRegistry.get(key);
    }
    
}
