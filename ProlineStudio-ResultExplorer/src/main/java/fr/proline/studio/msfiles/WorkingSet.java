/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.msfiles.WorkingSetEntry.Location;
import java.util.HashSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AK249877
 */
public class WorkingSet {

    private final String m_name;
    private final String m_description;
    private final JSONArray m_entries;
    private HashSet<String> m_index;

    public WorkingSet(String name, String description, JSONArray entries) {
        m_name = name;
        m_description = description;
        m_entries = entries;
        m_index = new HashSet<String>();
        updateIndex();
    }

    public String getName() {
        return m_name;
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
                return true;
            }
        }
        return false;
    }

    public boolean addEntry(String path, Location location) {
        if(!m_index.contains(path)){ 
            JSONObject newEntry = new JSONObject();
            newEntry.put("filename", path);
            newEntry.put("location", location.toString().toLowerCase());
            newEntry.put("path", path);
            m_index.add(path);
            
            m_entries.add(newEntry);
            
            return true;
        }else{
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
