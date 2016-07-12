package fr.proline.studio.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * JList for which each item is a checkBox.
 * Associated ListModel can't be modified, element of the List should be encapsulated. 
 * Use this class addItem or getElementAt... 
 * 
 *
 * @author JM235353
 */
public class JCheckBoxList<E> extends JList {

    DefaultListModel<CheckListItem<E>> model;
        
    public JCheckBoxList(List<? extends E> list, List<Boolean> visibilityList) {
        
        int size = list.size();
        CheckListItem<E>[] items = new CheckListItem[size];
        
        model = new DefaultListModel<>();        
        for (int i=0;i<list.size();i++) {
            items[i] = new CheckListItem<>(list.get(i), visibilityList.get(i));
            model.addElement(items[i]);
        }
        super.setModel(model);
        
        setCellRenderer(new CheckBoxListRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //setVisibleRowCount(visibleRows);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent event) {
                selectItem(event.getPoint());
            }
        });
    }

    @Override
    public void setModel(ListModel model) {
        throw new UnsupportedOperationException("Can't change model");
    }    

    public int getListSize() {
        return getModel().getSize();
    }
    
    public boolean isVisible(int i) {
        return ((CheckListItem) getModel().getElementAt(i)).isSelected();
    }
    
    public void addItem(int index, E item, Boolean isVisible){
        CheckListItem<E> cItem = new CheckListItem<>(item, isVisible);
        model.add(index, cItem);
    }
    
    public E getElementAt(int index){
        CheckListItem<E> item = (CheckListItem<E>) getModel().getElementAt(index);
        return item.getItem();
    }
    
    private void selectItem(Point point) {
        int index = locationToIndex(point);

        if (index >= 0) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(index);
            item.setSelected(!item.isSelected());
            repaint(getCellBounds(index, index));
        }
    }

    public void reinitSelection() {
        int size = getListSize();
        for (int i=0;i<size;i++) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(i);
            item.setSelected(false);
        }
    }
    
    public void unselectAll() {
        selectAll(false);
    }
    public void selectAll() {
        selectAll(true);
    }
    
    private void selectAll(boolean selection) {
        ListModel m = getModel();
        int nb = m.getSize();
        for (int i=0;i<nb;i++) {
            CheckListItem item = (CheckListItem) m.getElementAt(i);
            item.setSelected(selection);
        }
    }
    
    public void selectItem(int index) {
        CheckListItem item = (CheckListItem) getModel().getElementAt(index);
        item.setSelected(true);
    }
    
    public List<E> getSelectedItems() {
        
        ArrayList list = new ArrayList();
        
        int size = getListSize();
        for (int i=0;i<size;i++) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(i);
            if (item.isSelected()) {
                list.add(item.getItem());
            }
        }
        
        return list;
    }

    @Override
    public int[] getSelectedIndices() {
        
        int nbSelected = 0;
        int size = getListSize();
        for (int i = 0; i < size; i++) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(i);
            if (item.isSelected()) {
                nbSelected++;
            }
        }
        

        int[] rv = new int[nbSelected];
        int j = 0;
        for (int i = 0; i < size; i++) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(i);
            if (item.isSelected()) {
                rv[j] = i;
                j++;
            }
        }
        
        return rv;
    }

    public int[] getNonSelectedIndices() {
        
        int nbNonSelected = 0;
        int size = getListSize();
        for (int i = 0; i < size; i++) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(i);
            if (!item.isSelected()) {
                nbNonSelected++;
            }
        }
        

        int[] rv = new int[nbNonSelected];
        int j = 0;
        for (int i = 0; i < size; i++) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(i);
            if (!item.isSelected()) {
                rv[j] = i;
                j++;
            }
        }
        
        return rv;
    }
    
    private static class CheckListItem<E> {

        private E m_item;
        private boolean m_selected;

        public CheckListItem(E item, boolean selected) {
            m_item = item;
            m_selected = selected;
        }

        public E getItem() {
            return m_item;
        }

        public boolean isSelected() {
            return m_selected;
        }

        public void setSelected(boolean isSelected) {
            m_selected = isSelected;
        }

        @Override
        public String toString() {
            return m_item.toString();
        }
    }

    private class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList comp, Object value, int index, boolean isSelected, boolean hasFocus) {
            setEnabled(comp.isEnabled());
            setSelected(((CheckListItem) value).isSelected());
            setFont(comp.getFont());
            setText(value.toString());

            
            setBackground(comp.getBackground());
            setForeground(comp.getForeground());


            return this;
        }
    }
}