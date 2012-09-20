package fr.proline.studio.dam;

import java.util.HashMap;

/**
 * Class Used to save relation betweens objects of the ORM JPM.TODO : object
 * release management
 *
 * @author JM235353
 */
public class ORMDataManager {

    // This Map corresponds to a Map with 3 keys 
    // (ORM Object Class, ORM Object Id, String which correspond of the type of the value saved)
    // For example : ResultSet.class , ResultSet.id=1, "ProteinMatch[].sameset" // all proteins in the same set
    // Another example : ResultSet.class, ResultSet.id, "ProteinMatch" // Typical protein of the ResultSet
    HashMap<Class, HashMap<String, HashMap<Integer, Object>>> generalMap = new HashMap<Class, HashMap<String, HashMap<Integer, Object>>>();

    /**
     * Access to ORMDataManager singleton
     *
     * @return ORMDataManager singleton
     */
    public static ORMDataManager instance() {
        if (instance == null) {
            instance = new ORMDataManager();
        }
        return instance;
    }
    private static ORMDataManager instance = null;

    private ORMDataManager() {
    }

    ;
            
    /**
     * 
     * @param keyClass          for instance ResultSet.class
     * @param keyId             for inscance an id of a ResultSet
     * @param valueClassString  key for the type of value which is saved like "ProteinMatch"
     * @param value             value saved
     */
    public synchronized void put(Class keyClass, Integer keyId, String valueClassString, Object value) {
        HashMap<String, HashMap<Integer, Object>> keyClassmap = generalMap.get(keyClass);
        if (keyClassmap == null) {
            keyClassmap = new HashMap<String, HashMap<Integer, Object>>();
            generalMap.put(keyClass, keyClassmap);
        }

        HashMap<Integer, Object> idMap = keyClassmap.get(valueClassString);
        if (idMap == null) {
            idMap = new HashMap<Integer, Object>();
            keyClassmap.put(valueClassString, idMap);
        }

        idMap.put(keyId, value);

    }

    /**
     *
     * @param keyClass for instance ResultSet.class
     * @param keyId for inscance an id of a ResultSet
     * @param valueClassString key for the type of value which is saved like
     * "ProteinMatch"
     * @return value previously saved
     */
    public synchronized Object get(Class keyClass, Integer keyId, String valueClassString) {
        HashMap<String, HashMap<Integer, Object>> keyClassmap = generalMap.get(keyClass);
        if (keyClassmap == null) {
            return null;
        }

        HashMap<Integer, Object> idMap = keyClassmap.get(valueClassString);
        if (idMap == null) {
            return null;
        }

        return idMap.get(keyId);

    }
}
