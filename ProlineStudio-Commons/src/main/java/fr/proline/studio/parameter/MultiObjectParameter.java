package fr.proline.studio.parameter;

import fr.proline.studio.gui.JCheckBoxList;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;


/**
 * Parameter to select an object among a list of objects.
 * For the moment, it can be displayed only by a JCheckBoxList.
 * If a null object is in the list of objects, it is displayed as "< Select >" string
 * and to select another object becomes compulsory.
 * @author JM235353
 */
public class MultiObjectParameter<E> extends AbstractParameter {

    private E[] m_objects;
    private Object[] m_associatedObjects = null;
    private int[] m_defaultIndex = null;
    private AbstractParameterToString<E> m_paramToString = null;


    public MultiObjectParameter(String key, String name, E[] objects, int[] defaultIndex, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, JCheckBoxList.class);
        m_objects = objects;
        m_defaultIndex = defaultIndex;
        m_paramToString = paramToString;
        
    }
    
    public MultiObjectParameter(String key, String name, JCheckBoxList checkBoxList, E[] objects, Object[] associatedObjects, int[] defaultIndex, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, JCheckBoxList.class);
        m_objects = objects;
        m_defaultIndex = defaultIndex;
        m_paramToString = paramToString;
        if (checkBoxList == null) {
            checkBoxList = (JCheckBoxList)getComponent(null);
        }
        m_parameterComponent = checkBoxList;
        m_associatedObjects = associatedObjects;
        //checkBoxList.setCellRenderer(new ParameterComboboxRenderer(paramToString));

    }
    
    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JCheckBoxList.class)) {
                
                JCheckBoxList checkBoxList = ((JCheckBoxList) m_parameterComponent);

                // not used for this component for the moment
                /*if ((value == null) || (! selectItem(combobox, value))) {
                    combobox.setSelectedIndex(m_defaultIndex);
                }*/

                return m_parameterComponent;
            }
        }

        
        
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            ArrayList<E> list = new ArrayList<>();
            ArrayList<Boolean> visibilityList = new ArrayList<>();
            for (E obj : m_objects) {
                list.add(obj);
                visibilityList.add(Boolean.FALSE);
            }
            JCheckBoxList checkboxList = new JCheckBoxList(list, visibilityList);
            
            //checkboxList.setCellRenderer(new ParameterComboboxRenderer(m_paramToString));
            
            // not used for this component for the moment
            /*if ((value == null) || (! selectItem(combobox, value))) {
                    combobox.setSelectedIndex(m_defaultIndex);
            }*/
            m_parameterComponent = checkboxList;
            return checkboxList;
        }

        return null; // should not happen
    }

    /*private boolean selectItem(JCheckBoxList comboBox, Object value) {

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
    }*/
    
    
    @Override
    public void initDefault() {
        // not used for this component for the moment
        //((JComboBox) m_parameterComponent).setSelectedIndex(m_defaultIndex);
    }

    @Override
    public ParameterError checkParameter() {
        
        if (!m_used && !m_compulsory) {
            return null;
        }
        
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            
            List selectedList = ((JCheckBoxList) m_parameterComponent).getSelectedValuesList();
            if (selectedList.isEmpty()) {
                return new ParameterError("No selection done", m_parameterComponent);
            }

        }
        
        return null;
    }

    @Override
    public void setValue(String v) {
        // should not be called
    }
    
    @Override
    public String getStringValue() {
        return null; // should not be called
    }




    @Override
    public List<E> getObjectValue() {
        if (m_graphicalType.equals(JCheckBoxList.class)) {

            JCheckBoxList checkBoxList = ((JCheckBoxList) m_parameterComponent);
            List<E> list = checkBoxList.getSelectedItems();

            return list;
        }
        return null; // should not happen
    }
    

    public Object getAssociatedSelectedObjectValue() {
        
        if (m_associatedObjects == null) {
            return getObjectValue();
        }
        
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            
            JCheckBoxList checkBoxList = ((JCheckBoxList) m_parameterComponent);
            int[] indices = checkBoxList.getSelectedIndices();
                 
            ArrayList associatedSelectedList = new ArrayList(indices.length);
            for (int i : indices) {
                associatedSelectedList.add(m_associatedObjects[i]);
            }

            return associatedSelectedList;
        }
        return null; // should not happen
    }
    
    @Override
    public LabelVisibility showLabel() {
        return LabelVisibility.AS_BORDER_TITLE;
    }
    
    
}
