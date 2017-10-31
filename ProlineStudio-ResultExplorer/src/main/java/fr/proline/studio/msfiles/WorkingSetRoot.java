/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.util.HashSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AK249877
 */
public class WorkingSetRoot {

    private final JSONArray m_workingSets;
    private final HashSet<String> m_index;

    public WorkingSetRoot(JSONArray workingSets) {
        m_workingSets = workingSets;
        m_index = new HashSet<String>();
        updateIndex();
    }

    public JSONArray getWorkingSets(){
        return m_workingSets;
    }
    
    @Override
    public String toString(){
        return "Home";
    }
    
    public boolean removeWorkingSet(String name){
        for(int i=0; i<m_workingSets.size(); i++){
            JSONObject innerObj = (JSONObject) m_workingSets.get(i);
            String currentName = (String) innerObj.get("name");
            if(name.equalsIgnoreCase(currentName)){
                m_workingSets.remove(i);
                m_index.remove(currentName);
                return true;
            }
        }
        return false;
    }
    
    public boolean addWorkingset(String name, String description){
        
        if(!m_index.contains(name)){
            
            JSONObject newWorkingSet = new JSONObject();
            newWorkingSet.put("name", name);
            newWorkingSet.put("description", description);
            newWorkingSet.put("entries", new JSONArray());
            m_index.add(name);
            
            m_workingSets.add(newWorkingSet);
            
            return true;
        }else{
            return false;
        }
    
    }
    
    private void updateIndex(){
        for(int i=0; i<m_workingSets.size(); i++){
            JSONObject workingSetEntry = (JSONObject) m_workingSets.get(i);
            String name = (String) workingSetEntry.get("name");
            m_index.add(name);
        }
    }
    
}
