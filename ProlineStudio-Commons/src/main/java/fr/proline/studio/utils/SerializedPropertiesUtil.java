package fr.proline.studio.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * Extract Properties form a Map of maps, lists and objects
 *
 * @author JM235353
 */
public class SerializedPropertiesUtil {

    public static void getProperties(HashMap<String, String> propertiesList, String name, Map<String, Object> serializedPropertiesMap) {

        m_sb = new StringBuilder();
        getPropertiesImpl(propertiesList, name, serializedPropertiesMap);
        m_sb = null;
    }

    /**
     * If the First Map contains only maps, we remove the first map and use the
     * key as each sub map as a Property Group
     *
     * @param sheet
     * @param name
     * @param serializedPropertiesMap
     */
    private static void getPropertiesImpl(HashMap<String, String> propertiesList, String name, Map serializedPropertiesMap) {

        if (serializedPropertiesMap == null) {
            return;
        }

        boolean nonMapObject = false;
        Iterator it = serializedPropertiesMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = serializedPropertiesMap.get(key);
            if (value instanceof Map) {
                continue;
            }
            nonMapObject = true;
        }

        if (nonMapObject) {
            createPropertyGroup(propertiesList, name, serializedPropertiesMap);
        } else {
            it = serializedPropertiesMap.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Map map = (Map) serializedPropertiesMap.get(key);
                createPropertyGroup(propertiesList, key.toString(), map);
            }
        }
    }

    public static void getProperties(Sheet sheet, String name, Map serializedPropertiesMap) {

        m_sb = new StringBuilder();
        getPropertiesImpl(sheet, name, serializedPropertiesMap);
        m_sb = null;
    }
    private static StringBuilder m_sb;

    /**
     * If the First Map contains only maps, we remove the first map and use the
     * key as each sub map as a Property Group
     *
     * @param sheet
     * @param name
     * @param serializedPropertiesMap
     */
    private static void getPropertiesImpl(Sheet sheet, String name, Map serializedPropertiesMap) {

        if (serializedPropertiesMap == null) {
            return;
        }

        boolean nonMapObject = false;
        Iterator it = serializedPropertiesMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = serializedPropertiesMap.get(key);
            if (value instanceof Map) {
                continue;
            }
            nonMapObject = true;
        }

        if (nonMapObject) {
            createPropertyGroup(sheet, name, serializedPropertiesMap);
        } else {
            it = serializedPropertiesMap.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Map map = (Map) serializedPropertiesMap.get(key);
                createPropertyGroup(sheet, key.toString(), map);
            }
        }
    }

    private static void createPropertyGroup(HashMap<String, String> propertiesList, String name, Map map) {

        addProperties(propertiesList, null, map);

    }

    /**
     * Create a Property Group for each map
     *
     * @param sheet
     * @param name
     * @param serializedPropertiesMap
     */
    private static void createPropertyGroup(Sheet sheet, String name, Map map) {

        Sheet.Set propGroup = Sheet.createPropertiesSet();
        propGroup.setName(name);
        propGroup.setDisplayName(name);

        addProperties(propGroup, null, map);

        sheet.put(propGroup);

    }

    private static void addProperties(HashMap<String, String> propertiesList, String name, Map serializedPropertiesMap) {

        ArrayList<Object> listOfKeyMap = null;
        ArrayList<Map> listOfMaps = null;

        Iterator it = serializedPropertiesMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = serializedPropertiesMap.get(key);
            String valueString;
            if (value instanceof Map) {
                if (listOfMaps == null) {
                    listOfMaps = new ArrayList<>();
                    listOfKeyMap = new ArrayList<>();
                }
                listOfMaps.add((Map) value);
                listOfKeyMap.add(key);

                continue;
            } else if (value instanceof List) {

                m_sb.setLength(0);
                List l = (List) value;
                Iterator itList = l.iterator();

                while (itList.hasNext()) {
                    if (m_sb.length() > 0) {
                        m_sb.append(',');
                    }
                    Object valueList = itList.next();

                    if (valueList instanceof Map) {
                        if (listOfMaps == null) {
                            listOfMaps = new ArrayList<>();
                            listOfKeyMap = new ArrayList<>();
                        }
                        listOfMaps.add((Map) valueList);
                        listOfKeyMap.add(key);

                        continue;
                    }

                    if (valueList == null) {
                        m_sb.append("null");
                    } else {
                        m_sb.append(valueList.toString());
                    }

                }

                if (m_sb.length() == 0) {
                    continue;
                }
                valueString = m_sb.toString();
            } else {

                if (value == null) {
                    valueString = "null";
                } else {
                    valueString = value.toString();
                }
            }

            String propName = (name == null) ? key.toString() : name + " / " + key.toString();

            propertiesList.put(propName, valueString);

        }

        if (listOfMaps != null) {
            Map<Object, Integer> propIndex = new HashMap<Object, Integer>();
            int nbMap = listOfMaps.size();
            for (int i = 0; i < nbMap; i++) {
                Object key = listOfKeyMap.get(i);
                StringBuilder keyName = new StringBuilder(key.toString());
                if (propIndex.containsKey(key)) {
                    keyName.append("#").append(propIndex.get(key).toString());
                    propIndex.put(key, propIndex.get(key) + 1);
                } else if (Collections.frequency(listOfKeyMap, key) > 1) {
                    keyName.append("#1");
                    propIndex.put(key, 2);
                }

                Map map = listOfMaps.get(i);
                String propName = (name == null) ? keyName.toString() : name + " / " + keyName.toString();
                addProperties(propertiesList, propName, map);
            }
        }

    }

    /**
     * Add recursively all properties to the Property Group
     *
     * @param propGroup
     * @param name
     * @param serializedPropertiesMap
     */
    private static void addProperties(Sheet.Set propGroup, String name, Map serializedPropertiesMap) {

        ArrayList<Object> listOfKeyMap = null;
        ArrayList<Map> listOfMaps = null;

        Iterator it = serializedPropertiesMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = serializedPropertiesMap.get(key);
            String valueString;
            if (value instanceof Map) {
                if (listOfMaps == null) {
                    listOfMaps = new ArrayList<>();
                    listOfKeyMap = new ArrayList<>();
                }
                listOfMaps.add((Map) value);
                listOfKeyMap.add(key);

                continue;
            } else if (value instanceof List) {

                m_sb.setLength(0);
                List l = (List) value;
                Iterator itList = l.iterator();

                while (itList.hasNext()) {
                    if (m_sb.length() > 0) {
                        m_sb.append(',');
                    }
                    Object valueList = itList.next();

                    if (valueList instanceof Map) {
                        if (listOfMaps == null) {
                            listOfMaps = new ArrayList<>();
                            listOfKeyMap = new ArrayList<>();
                        }
                        listOfMaps.add((Map) valueList);
                        listOfKeyMap.add(key);

                        continue;
                    }

                    if (valueList == null) {
                        m_sb.append("null");
                    } else {
                        m_sb.append(valueList.toString());
                    }

                }

                if (m_sb.length() == 0) {
                    continue;
                }
                valueString = m_sb.toString();
            } else {

                if (value == null) {
                    valueString = "null";
                } else {
                    valueString = value.toString();
                }
            }

            String propName = (name == null) ? key.toString() : name + " / " + key.toString();

            final String _valueString = valueString;
            Property prop = new PropertySupport.ReadOnly(
                    propName,
                    String.class,
                    propName,
                    propName) {

                        @Override
                        public Object getValue() throws InvocationTargetException {
                            return _valueString;
                        }
                    };
            propGroup.put(prop);

        }

        if (listOfMaps != null) {
            Map<Object, Integer> propIndex = new HashMap<Object, Integer>();
            int nbMap = listOfMaps.size();
            for (int i = 0; i < nbMap; i++) {
                Object key = listOfKeyMap.get(i);
                StringBuilder keyName = new StringBuilder(key.toString());
                if (propIndex.containsKey(key)) {
                    keyName.append("#").append(propIndex.get(key).toString());
                    propIndex.put(key, propIndex.get(key) + 1);
                } else if (Collections.frequency(listOfKeyMap, key) > 1) {
                    keyName.append("#1");
                    propIndex.put(key, 2);
                }

                Map map = listOfMaps.get(i);
                String propName = (name == null) ? keyName.toString() : name + " / " + keyName.toString();
                addProperties(propGroup, propName, map);
            }
        }

    }

}
