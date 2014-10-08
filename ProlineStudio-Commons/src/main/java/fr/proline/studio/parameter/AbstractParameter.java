package fr.proline.studio.parameter;

import javax.swing.*;

/**
 * Base class for All Parameters
 * @author jm235353
 */
public abstract class AbstractParameter {

    protected String m_key;
    protected String m_name;
    protected Class m_type;
    protected Class m_graphicalType;
    protected JComponent m_parameterComponent = null;
    protected boolean m_used = true;
    protected Object m_associatedData = null;

    protected AbstractParameter(String key, String name, Class type, Class graphicalType) {
        m_key = key;
        m_name = name;
        m_type = type;
        m_graphicalType = graphicalType;
    }

    public String getName() {
        return m_name;
    }
    
    public String getKey() {
        return m_key;
    }

    public JComponent getComponent() {
        return m_parameterComponent;
    }
    public abstract JComponent getComponent(Object value);
    public abstract void initDefault();
    
    public abstract ParameterError checkParameter();
    
    
    public abstract String getStringValue();
    public abstract Object getObjectValue();
    
    public abstract void setValue(String v);
    /**
     * Specify if a component should be associated to the label for this parameter. 
     * Default is true;
     * @return 
     */
    public Boolean hasComponent(){
        return true;
    }
    
    /**
     * Returns if the parameter wants its name to be displayed in the panel
     * @return 
     */
    public boolean showLabel() {
        return true;
    }
    
    /**
     * Called when a window is reopened to be able to clean
     * some parameters (like FileParameter)
     */
    public void clean() {
        
    }
    
    public boolean isUsed() {
        return m_used;
    }
    
    public void setUsed(boolean used) {
        m_used = used;
    }

    
    @Override
    public String toString() {
        return getName();
    }

    public void setAssociatedData(Object associatedData) {
        m_associatedData = associatedData;
    }
    
    public Object getAssociatedData() {
        return m_associatedData;
    }
    
}
