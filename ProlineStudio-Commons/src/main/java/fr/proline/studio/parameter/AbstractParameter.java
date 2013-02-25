package fr.proline.studio.parameter;

import javax.swing.*;

/**
 *
 * @author jm235353
 */
public abstract class AbstractParameter {

    protected String key;
    protected String name;
    protected Class type;
    protected Class graphicalType;
    protected JComponent parameterComponent = null;
    protected boolean used = true;
    protected Object associatedData = null;

    protected AbstractParameter(String key, String name, Class type, Class graphicalType) {
        this.key = key;
        this.name = name;
        this.type = type;
        this.graphicalType = graphicalType;
    }

    public String getName() {
        return name;
    }
    
    public String getKey() {
        return key;
    }

    public JComponent getComponent() {
        return parameterComponent;
    }
    public abstract JComponent getComponent(Object value);
    public abstract void initDefault();
    
    public abstract ParameterError checkParameter();
    
    
    public abstract String getStringValue();
    public abstract Object getObjectValue();
    
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
        return used;
    }
    
    public void setUsed(boolean used) {
        this.used = used;
    }

    
    @Override
    public String toString() {
        return getName();
    }

    public void setAssociatedData(Object associatedData) {
        this.associatedData = associatedData;
    }
    
    public Object getAssociatedData() {
        return associatedData;
    }
    
}
