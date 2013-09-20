package fr.proline.studio.filter;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * Filter base class
 * @author JM235353
 */
public abstract class Filter {


    
    //protected HashMap<Integer, String> m_values = null;
    protected ArrayList<Integer> m_valueKeys = null;
    protected HashMap<Integer, Component> m_components = null;
    
    protected String m_variableName;
    protected boolean m_used;
    protected boolean m_defined;
    
    public Filter(String variableName) {
        m_variableName = variableName;
    }
    
    public abstract FilterStatus checkValues();
    
    
    public void registerDefinedAsUsed() {
        m_used = m_defined;
    }
    public abstract void registerValues();
    

    public ArrayList<Integer> getValueKeys() {
        return m_valueKeys;
    }

    public String getName() {
        return m_variableName;
    }
    
    public boolean isDefined() {
        return m_defined;
    }
    
    public void setDefined(boolean defined) {
        m_defined = defined;
    }

    
    public boolean isUsed() {
        return m_used;
    }
   
    public void setUsed(boolean used) {
        m_used = used;
    }

    
    @Override
    public String toString() {
        return m_variableName;
    }
    
    
    public void registerComponent(Integer key, Component c) {
        if (m_components == null) {
            m_components = new HashMap<>();
        }
        m_components.put(key, c);
    }
    
    public Component getComponent(Integer key) {
        if (m_components == null) {
            return null;
        }
        return m_components.get(key);
    }
    
    public void clearComponents() {
        if (m_components == null) {
            return;
        }
        m_components.clear();
    }
    
    public abstract void createComponents(JPanel p, GridBagConstraints c);
    
    public abstract void reset();
}
