package fr.proline.studio.parameter;

import fr.proline.studio.gui.AdvancedSelectionPanel;
import fr.proline.studio.gui.JCheckBoxList;
import fr.proline.studio.gui.JCheckBoxListPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    private String m_selectedName;
    private String m_unselectedName;
    
    private String[] m_columnGroupNamesArray = null;
    
    public MultiObjectParameter(String key, String name, String selectedName, String unselectedName, Class graphicalType, E[] objects, Object[] associatedObjects, boolean[] selection, AbstractParameterToString<E> paramToString) {
        super(key, name, Integer.class, graphicalType);
        m_objects = objects;
        m_associatedObjects = associatedObjects;
        m_defaultSelection = selection;
        m_paramToString = paramToString;
        
        m_selectedName = selectedName;
        m_unselectedName = unselectedName;
        
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
    
    /*public void setFastSelectionValues(String[] columnGroupNamesArray) {
        m_columnGroupNamesArray = columnGroupNamesArray;
    }*/

    
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
        
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            JCheckBoxList checkBoxList = ((JCheckBoxListPanel) m_parameterComponent).getCheckBoxList();
            checkBoxList.selectItem(i, v);
        } else if (m_graphicalType.equals(AdvancedSelectionPanel.class)) {
            
        }
        
    }

    
    @Override
    public boolean componentNeedsScrollPane() {
        return true;
    }
    
    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JCheckBoxList.class) || m_graphicalType.equals(AdvancedSelectionPanel.class)) {

                return m_parameterComponent;
            }
        }

        ArrayList<E> list = new ArrayList<>();
        ArrayList<Boolean> visibilityList = new ArrayList<>();
        for (int i = 0; i < m_objects.length; i++) {
            E obj = m_objects[i];
            list.add(obj);
            if (m_defaultSelection != null) {
                visibilityList.add(m_defaultSelection[i] ? Boolean.TRUE : Boolean.FALSE);
            } else {
                visibilityList.add(Boolean.FALSE);
            }
        }
        
        
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            
            
            JCheckBoxList checkboxList = new JCheckBoxList(list, visibilityList);

            m_parameterComponent = new JCheckBoxListPanel(checkboxList, m_allowSelectAll);
            
        } else if (m_graphicalType.equals(AdvancedSelectionPanel.class)) {
            AdvancedSelectionPanel selectionPanel = new AdvancedSelectionPanel(m_selectedName, m_unselectedName, list, visibilityList);
            //selectionPanel.setFastSelectionValues(m_columnGroupNamesArray);
            m_parameterComponent = selectionPanel;
        }

        return m_parameterComponent;
    }

    
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
        
        List selectedList = null;
         if (m_graphicalType.equals(JCheckBoxList.class)) {
            selectedList = ((JCheckBoxListPanel) m_parameterComponent).getCheckBoxList().getSelectedItems();
         } else if (m_graphicalType.equals(AdvancedSelectionPanel.class)) {
             selectedList = ((AdvancedSelectionPanel) m_parameterComponent).getSelectedItems();
         }
        

        if ((m_compulsory) && (selectedList.isEmpty())) {
            return new ParameterError("No selection done", m_parameterComponent);
        }
        int nb = selectedList.size();
        if ((m_compulsory) && (nb < m_nbCompulsorySelection)) {
            return new ParameterError("You must select at least " + m_nbCompulsorySelection + "values", m_parameterComponent);
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
        }  else if (m_graphicalType.equals(AdvancedSelectionPanel.class)) {
             List<E> list = ((AdvancedSelectionPanel) m_parameterComponent).getSelectedItems();
             return list;
         }
        
        
        return null; // should not happen
    }
    

    public Object getAssociatedValues(boolean selected) {
        
        if (m_associatedObjects == null) {
            return getObjectValue();
        }
        
        int[] indices = null;
        if (m_graphicalType.equals(JCheckBoxList.class)) {
            
            JCheckBoxList checkBoxList = ((JCheckBoxListPanel) m_parameterComponent).getCheckBoxList();
            indices = selected ? checkBoxList.getSelectedIndices() : checkBoxList.getNonSelectedIndices();
        } else if (m_graphicalType.equals(AdvancedSelectionPanel.class)) {
            AdvancedSelectionPanel selectionPanel = ((AdvancedSelectionPanel) m_parameterComponent);
            indices = selected ? selectionPanel.getSelectedIndices() : selectionPanel.getNonSelectedIndices();
        }
        
        ArrayList associatedSelectedList = new ArrayList(indices.length);
        for (int i : indices) {
            associatedSelectedList.add(m_associatedObjects[i]);
        }

        return associatedSelectedList;

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
        } else if (m_parameterComponent instanceof AdvancedSelectionPanel) {
            // JPM.TODO : not done for the moment, not used
        }
    }
    
}
