/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.utils;

import fr.proline.studio.Property;
import fr.proline.studio.PropertySupport;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Extract Properties form a Map of maps, lists and objects
 *
 * @author JM235353
 */
public class SerializedPropertiesUtil {

    private static final StringBuilder m_sb = new StringBuilder();
    
    public static void getProperties(HashMap<String, String> propertiesList, String name, Map<String, Object> serializedPropertiesMap) {
        
        getPropertiesImpl(propertiesList, name, serializedPropertiesMap);
        m_sb.setLength(0);
    }

    /**
     * If the First Map contains only maps, we remove the first map and use the
     * key as each sub map as a Property Group
     *
     * @param propertiesList
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

    private static void createPropertyGroup(HashMap<String, String> propertiesList, String name, Map map) {

        String nameParam = (name.compareTo("validation_properties") == 0) ? "validation_properties" : null; //JPM.WART : sometimes there is one more hierarchical level, and so validation_properties must be always displayed
        addProperties(propertiesList, nameParam, map);

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
            Map<Object, Integer> propIndex = new HashMap<>();
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

}
