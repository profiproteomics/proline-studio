package fr.proline.studio.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 *
 * @author JM235353
 */
public class JCheckBoxList<E> extends JList {

    public JCheckBoxList(List<? extends E> list, List<Boolean> visibilityList) {
        
        int size = list.size();
        CheckListItem<E>[] items = new CheckListItem[size];
        
        for (int i=0;i<list.size();i++) {
            items[i] = new CheckListItem<>(list.get(i), visibilityList.get(i));
        }
        setListData(items);


        setCellRenderer(new CheckBoxListRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //setVisibleRowCount(visibleRows);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                selectItem(event.getPoint());
            }
        });
    }

    public int getListSize() {
        return getModel().getSize();
    }
    
    public boolean isVisible(int i) {
        return ((CheckListItem) getModel().getElementAt(i)).isSelected();
    }
    
    
    private void selectItem(Point point) {
        int index = locationToIndex(point);

        if (index >= 0) {
            CheckListItem item = (CheckListItem) getModel().getElementAt(index);
            item.setSelected(!item.isSelected());
            repaint(getCellBounds(index, index));
        }
    }

    private static class CheckListItem<E> {

        private E m_item;
        private boolean m_selected;

        public CheckListItem(E item, boolean selected) {
            m_item = item;
            m_selected = selected;
        }

        @SuppressWarnings("unused")
        public Object getItem() {
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