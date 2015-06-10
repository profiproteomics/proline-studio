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

    private E[] m_objects;
    private Object[] m_associatedObjects = null;
    private int m_defaultIndex;
    private AbstractParameterToString<E> m_paramToString = null;


    public ObjectParameter(String key, String name, E[] objects, int defaultIndex, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, JComboBox.class);
        m_objects = objects;
        if ((defaultIndex<0) || (defaultIndex>=objects.length)) {
            defaultIndex = 0;
        }
        m_defaultIndex = defaultIndex;
        m_paramToString = paramToString;
        
    }
    
    public ObjectParameter(String key, String name, JComboBox comboBox, E[] objects, Object[] associatedObjects, int defaultIndex, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, JComboBox.class);
        m_objects = objects;
        if ((defaultIndex < 0) || (defaultIndex >= objects.length)) {
            defaultIndex = 0;
        }
        m_defaultIndex = defaultIndex;
        m_paramToString = paramToString;
        m_parameterComponent = comboBox;
        m_associatedObjects = associatedObjects;
        if (paramToString != null) {
            comboBox.setRenderer(new ParameterComboboxRenderer(paramToString));
        }
    }
    
    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JComboBox.class)) {
                
                JComboBox combobox = ((JComboBox) m_parameterComponent);

                if ((value == null) || (! selectItem(combobox, value))) {
                    combobox.setSelectedIndex(m_defaultIndex);
                }

                return m_parameterComponent;
            }
        }

        
        
        if (m_graphicalType.equals(JComboBox.class)) {
            JComboBox combobox = new JComboBox(m_objects);
            combobox.setRenderer(new ParameterComboboxRenderer(m_paramToString));
            
            if ((value == null) || (! selectItem(combobox, value))) {
                    combobox.setSelectedIndex(m_defaultIndex);
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
        ((JComboBox) m_parameterComponent).setSelectedIndex(m_defaultIndex);
    }

    @Override
    public ParameterError checkParameter() {
        
        if (!m_used && !m_compulsory) {
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
    public void setValue(String v) {
        if (m_parameterComponent == null) {
            return; // should not happen
        }

        if (m_graphicalType.equals(JComboBox.class)) {
            selectItem(((JComboBox) m_parameterComponent), v);
        }
    }
    
    @Override
    public String getStringValue() {
        if (m_associatedObjects == null) {
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

        if (m_paramToString != null) {
            return m_paramToString.toString(item);
        }

        return item.toString();
    }

    private String getStringValue(JComboBox comboBox, int index) {
        if (m_associatedObjects == null) {
            return getStringValue( (E) comboBox.getItemAt(index) );
        } else {
            Object associatedObject = m_associatedObjects[index];
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
        
        if (m_associatedObjects == null) {
            return getObjectValue();
        }
        
        if (m_graphicalType.equals(JComboBox.class)) {
            if (m_parameterComponent == null) {
                // JComboBox not already created
                return null;
            }
            int index = ((JComboBox) m_parameterComponent).getSelectedIndex();
            if (index == -1) {
                return null;
            }
            return m_associatedObjects[index];
        }
        return null; // should not happen
    }
    
    
    
    
}
