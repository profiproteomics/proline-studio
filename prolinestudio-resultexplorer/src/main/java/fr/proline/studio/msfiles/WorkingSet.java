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
package fr.proline.studio.msfiles;

import fr.proline.studio.msfiles.WorkingSetEntry.Location;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AK249877
 */
public class WorkingSet implements Serializable {

    private String m_name;
    private String m_description;
    private JSONArray m_entries;
    private HashSet<String> m_index;

    public WorkingSet(String name, String description, JSONArray entries) {
        m_name = name;
        m_description = description;
        m_entries = entries;
        m_index = new HashSet<String>();
        updateIndex();
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public String getDescription() {
        return m_description;
    }

    public JSONArray getEntries() {
        return m_entries;
    }

    @Override
    public String toString() {
        return m_name;
    }
    
    public boolean removeEntry(String path) {
        for (int i = 0; i < m_entries.size(); i++) {
            JSONObject innerObj = (JSONObject) m_entries.get(i);
            String currentPath = (String) innerObj.get("path");
            if (path.equalsIgnoreCase(currentPath)) {
                m_entries.remove(i);
                m_index.remove(path);
                return true;
            }
        }
        return false;
    }

    public boolean addEntry(String path, Location location) {
        if (!m_index.contains(path)) {
            JSONObject newJSONObject = new JSONObject();

            //extract filename!
            String delimiter = (path.contains(File.separator) ? File.separator : "/");
            String filename = path.substring(path.lastIndexOf(delimiter) + 1);

            newJSONObject.put("filename", filename);
            newJSONObject.put("location", location.toString().toLowerCase());
            newJSONObject.put("path", path);
            m_index.add(path);

            m_entries.add(newJSONObject);

            return true;
        } else {
            return false;
        }
    }

    public boolean addEntry(JSONObject object) {
        String path = (String) object.get("path");
        if (!m_index.contains(path)) {
            m_index.add(path);
            m_entries.add(object);
            return true;
        } else {
            return false;
        }
    }

    private void updateIndex() {
        for (int i = 0; i < m_entries.size(); i++) {
            JSONObject workingSetEntry = (JSONObject) m_entries.get(i);
            String path = (String) workingSetEntry.get("path");
            m_index.add(path);
        }
    }
}
