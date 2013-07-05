package fr.proline.studio.parameter;

import java.awt.Component;
import javax.swing.*;


/**
 * Parameter to select an object among a list of objects.
 * For the moment, it can be displayed only by a JComboBox.
 * If a null object is in the list of objects, it is displayed as "< Select >" string
 * and to select another object becomes compulsory.
 * @author JM235353
 */
public class ObjectParameter<E> extends AbstractParameter {

    private E[] objects;
    private Object[] associatedObjects = null;
    private int defaultIndex;
    private AbstractParameterToString<E> paramToString = null;


    public ObjectParameter(String key, String name, E[] objects, int defaultIndex, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, JComboBox.class);
        this.objects = objects;
        if ((defaultIndex<0) || (defaultIndex>=objects.length)) {
            defaultIndex = 0;
        }
        this.defaultIndex = defaultIndex;
        this.paramToString = paramToString;
        
    }
    
    public ObjectParameter(String key, String name, JComboBox comboBox, E[] objects, Object[] associatedObjects, int defaultIndex, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, JComboBox.class);
        this.objects = objects;
        if ((defaultIndex < 0) || (defaultIndex >= objects.length)) {
            defaultIndex = 0;
        }
        this.defaultIndex = defaultIndex;
        this.paramToString = paramToString;
        this.m_parameterComponent = comboBox;
        this.associatedObjects = associatedObjects;
        comboBox.setRenderer(new ParameterComboboxRenderer(paramToString));

    }
    
    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JComboBox.class)) {
                
                JComboBox combobox = ((JComboBox) m_parameterComponent);

                if ((value == null) || (! selectItem(combobox, value))) {
                    combobox.setSelectedIndex(defaultIndex);
                }

                return m_parameterComponent;
            }
        }

        
        
        if (m_graphicalType.equals(JComboBox.class)) {
            JComboBox combobox = new JComboBox(objects);
            combobox.setRenderer(new ParameterComboboxRenderer(paramToString));
            
            if ((value == null) || (! selectItem(combobox, value))) {
                    combobox.setSelectedIndex(defaultIndex);
            }
            m_parameterComponent = combobox;
            return combobox;
        }

        return null; // should not happen
    }

    private boolean selectItem(JComboBox comboBox, Object value) {

        String valueString = ( value == null) ? "" : value.toString();
        
        int nb = comboBox.getItemCount();
        for (int i=0;i<nb;i++) {
            String itemString = getStringValue(comboBox, i);
            if (itemString.compareTo(valueString) == 0) {
                comboBox.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    public void initDefault() {
        ((JComboBox) m_parameterComponent).setSelectedIndex(defaultIndex);
    }

    @Override
    public ParameterError checkParameter() {
        
        if (!m_used) {
            return null;
        }
        
        if (m_graphicalType.equals(JComboBox.class)) {
            if (((JComboBox) m_parameterComponent).getSelectedItem() == null) {
                return new ParameterError("Invalid Selection", m_parameterComponent);
            }
        }
        
        return null;
    }

    @Override
    public String getStringValue() {
        if (associatedObjects == null) {
            E item = getObjectValue();
        
            return getStringValue(item);
        } else {
            Object item = getAssociatedObjectValue();
            if (item == null) {
                return "";
            } else {
                return item.toString();
            }
        }
    }
    private String getStringValue(E item) {
        if (item == null) {
            return "";
        }

        if (paramToString != null) {
            return paramToString.toString(item);
        }

        return item.toString();
    }

    private String getStringValue(JComboBox comboBox, int index) {
        if (associatedObjects == null) {
            return getStringValue( (E) comboBox.getItemAt(index) );
        } else {
            Object associatedObject = associatedObjects[index];
            if (associatedObject == null) {
                return "";
            } else {
                return associatedObject.toString();
            }
        }
    }
    
    @Override
    public E getObjectValue() {
        if (m_graphicalType.equals(JComboBox.class)) {
            return (E) ((JComboBox) m_parameterComponent).getSelectedItem();
        }
        return null; // should not happen
    }
    

    public Object getAssociatedObjectValue() {
        
        if (associatedObjects == null) {
            return getObjectValue();
        }
        
        if (m_graphicalType.equals(JComboBox.class)) {
            int index = ((JComboBox) m_parameterComponent).getSelectedIndex();
            if (index == -1) {
                return null;
            }
            return associatedObjects[index];
        }
        return null; // should not happen
    }
    
    
    
    
}
