package fr.proline.studio.parameter;

import javax.swing.JComponent;

/**
 * Used for selection with no specific parameter.
 * 
 * @author vd225637
 */
public class NoneParameter extends BooleanParameter {
    
    public NoneParameter(String key, String name ) {
        super(key, name, (Class) null, true);        
    }

    @Override
    public JComponent getComponent(Object value) {
        return null;
    }

    @Override
    public void initDefault() {
        //No param
    }

 
    @Override
    public String getStringValue() {
        return "";
    }

    @Override
    public Object getObjectValue() {
         return "";
    }
    
    @Override
    public Boolean hasComponent(){
        return false;
    }

    
}
