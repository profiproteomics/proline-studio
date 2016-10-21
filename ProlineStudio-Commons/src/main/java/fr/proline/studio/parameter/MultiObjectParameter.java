package fr.proline.studio.parameter;

import fr.proline.studio.gui.JCheckBoxList;
import fr.proline.studio.gui.JCheckBoxListPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private final E[] m_objects;
    private Object[] m_associatedObjects = null;
    private boolean[] m_defaultSelection = null;
    private AbstractParameterToString<E> m_paramToString = null;

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;
    
    private int m_nbCompulsorySelection = 1;
    private boolean m_allowSelectAll = true;
    
    
    
    public MultiObjectParameter(String key, String name, E[] objects, boolean[] selection, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, JCheckBoxList.class);
        m_objects = objects;
        m_defaultSelection = selection;
        m_paramToString = paramToString;
        
        m_labelVisibility = LabelVisibility.AS_BORDER_TITLE;
    }
    
    public MultiObjectParameter(String key, String name, JCheckBoxList checkBoxList, E[] objects, Object[] associatedObjects, boolean[] selection, AbstractParameterToString<E> paramToString, boolean allowSelectAll) {
        super(key, name, Integer.class, JCheckBoxList.class);
        m_objects = objects;
        m_defaultSelection = selection;
        m_paramToString = paramToString;
        m_allowSelectAll = allowSelectAll;
        if (checkBoxList == null) {
            m_parameterComponent = (JCheckBoxListPanel) getComponent(null);
        } else {
            m_parameterComponent = new JCheckBoxListPanel(checkBoxList, allowSelectAll);
        }

        m_associatedObjects = associatedObjects;

        m_labelVisibility = LabelVisibility.AS_BORDER_TITLE;

    }
    
    public void setCompulsory(int nbCompulsorySelection) {
        m_compulsory = (nbCompulsorySelection>0);
        m_nbCompulsorySelection = nbCompulsorySelection;
    }
    
    @Override
    public void setCompulsory(boolean v) {
        m_compulsory = v;
        m_nbCompulsorySelection = 0;
    }
    
    public void setSelection(int i, boolean v) {
        if (m_parameterComponent == null) {
            return;
        }
        JCheckBoxList checkBoxList = ((JCheckBoxListPanel) m_parameterComponent).getCheckBoxList();
        checkBoxList.selectItem(i, v);
        
    }

    
    @Override
    public boolean componentNeedsScrollPane() {
        return true;
    }
    
    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JCheckBoxList.class)) {

                return m_parameterComponent;
            }
        }

        
        
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            ArrayList<E> list = new ArrayList<>();
            ArrayList<Boolean> visibilityList = new ArrayList<>();
            for (int i=0;i<m_objects.length;i++) {
                E obj = m_objects[i];
                list.add(obj);
                if (m_defaultSelection != null) {
                    visibilityList.add( m_defaultSelection[i] ? Boolean.TRUE : Boolean.FALSE);
                } else {
                    visibilityList.add(Boolean.FALSE);
                }
            }
            
            JCheckBoxList checkboxList = new JCheckBoxList(list, visibilityList);

            m_parameterComponent = new JCheckBoxListPanel(checkboxList, m_allowSelectAll);
            return m_parameterComponent;
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
            
            List selectedList = ((JCheckBoxListPanel) m_parameterComponent).getCheckBoxList().getSelectedItems();
            if ((m_compulsory) && (selectedList.isEmpty())) {
                return new ParameterError("No selection done", m_parameterComponent);
            }
            int nb = selectedList.size();
            if ((m_compulsory) && (nb<m_nbCompulsorySelection)) {
                return new ParameterError("You must select at least "+m_nbCompulsorySelection+ "values", m_parameterComponent);
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
    

    public Object getAssociatedValues(boolean selected) {
        
        if (m_associatedObjects == null) {
            return getObjectValue();
        }
        
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            
            JCheckBoxList checkBoxList = ((JCheckBoxListPanel) m_parameterComponent).getCheckBoxList();
            int[] indices = selected ? checkBoxList.getSelectedIndices() : checkBoxList.getNonSelectedIndices();
                 
            ArrayList associatedSelectedList = new ArrayList(indices.length);
            for (int i : indices) {
                associatedSelectedList.add(m_associatedObjects[i]);
            }

            return associatedSelectedList;
        }
        return null; // should not happen
    }
    
    
    
    public void addLinkedParameters(final AbstractLinkedParameters linkedParameters) {

        // create parameterComponent if needed
        getComponent(null);

        if (m_linkedParametersList == null) {
            m_linkedParametersList = new ArrayList<>(1);
        }
        m_linkedParametersList.add(linkedParameters);

        if (m_parameterComponent instanceof JCheckBoxListPanel) {
            
            JCheckBoxList checkBoxList = ((JCheckBoxListPanel) m_parameterComponent).getCheckBoxList();
            
            checkBoxList.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JCheckBoxList.CheckListItem item = (JCheckBoxList.CheckListItem) e.getSource();
                    linkedParameters.valueChanged(item.toString(), item.isSelected());
                }

            });
            initDefault();
        }
    }
    
}
