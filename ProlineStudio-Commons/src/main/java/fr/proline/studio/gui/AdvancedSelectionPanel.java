package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 *
 * @author JM235353
 * @param <E>
 */
public class AdvancedSelectionPanel<E> extends JPanel  {
    
    private JList m_selectedList = null;
    private JList m_unselectedList = null;
    
    private ArrayList<E> m_objects = null;
    
    private JComboBox m_selectionComboBox = null;
    
    public AdvancedSelectionPanel(String selectedName, String unselectedName, ArrayList<E> objects, ArrayList<Boolean> selection) {
        
        m_objects = objects;
        
        setLayout(new GridBagLayout());
        setBackground(Color.white);

        
        JPanel selectedPanel = createListPanel(true, selectedName, objects, selection);
        JPanel midActionPanel = createMiddlePanel();
        JPanel notSelectedPanel = createListPanel(false, unselectedName, objects, selection);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        add(selectedPanel, c);
        
        c.gridx++;
        add(midActionPanel, c);
        
        c.gridx++;
        add(notSelectedPanel, c);
        
        setFastSelectionValues(preparePrefixAndSuffix(objects));
    }
    
    private void setFastSelectionValues(String[] columnGroupNamesArray) {

        if (columnGroupNamesArray == null) {
            return;
        }
        
        m_selectionComboBox.setVisible(true);
        
        DefaultComboBoxModel model = (DefaultComboBoxModel) m_selectionComboBox.getModel();
        
        model.addElement("<Select from Prefix/Suffix>");
        
        int nb = columnGroupNamesArray.length;
        for (int i=0;i<nb;i++) {
             model.addElement(columnGroupNamesArray[i]);
        }
       
        m_selectionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectionIndex = m_selectionComboBox.getSelectedIndex();
                if (selectionIndex<=0) {
                    return;
                }
                
                String value = removeHtmlColor(m_selectionComboBox.getSelectedItem().toString());
                selectFromPrefixSuffix(value, m_selectedList);
                selectFromPrefixSuffix(value, m_unselectedList);

                // reselect first one
                m_selectionComboBox.setSelectedIndex(0);
                
            }
        });
        
    }
    
    private void selectFromPrefixSuffix(String value, JList list) {
        ListSelectionModel sm = list.getSelectionModel();
        sm.clearSelection();
        
        DefaultListModel<E> listModel = (DefaultListModel) list.getModel();

        int lastSelectedIndex = -1;
        int nb = listModel.getSize();
        for (int i = 0; i < nb; i++) {
            String valueCur = removeHtmlColor(listModel.getElementAt(i).toString());

            if (valueCur.indexOf(value) != -1) {
                sm.addSelectionInterval(i, i);
                lastSelectedIndex = i;
            }
            
            /*int indexSpace = valueCur.lastIndexOf(' ');
            if (indexSpace != -1) {
                String prefix = valueCur.substring(0, indexSpace);
                String suffix = valueCur.substring(indexSpace, valueCur.length());
                if ((prefix.compareTo(value) == 0) || (suffix.compareTo(value) == 0)) {
                    sm.addSelectionInterval(i, i);
                    lastSelectedIndex = i;
                }
            }*/

        }
        list.ensureIndexIsVisible(lastSelectedIndex);
        list.ensureIndexIsVisible(list.getSelectedIndex());
    }
    private String removeHtmlColor(String value) {
        int colorRemoveStart = value.indexOf("</font>", 0);
        int colorRemoveStop = value.indexOf("</html>", 0);
        if ((colorRemoveStart > -1) && (colorRemoveStop > colorRemoveStart)) {
            value = value.substring(colorRemoveStart + "</font>".length(), colorRemoveStop);
        }

        return value;
    }

    public List<E> getSelectedItems() {

        ListModel<E> model = m_selectedList.getModel();
        int size = model.getSize();
        ArrayList<E> l = new ArrayList<>(size);
        for (int i=0;i<size;i++) {
            l.add(model.getElementAt(i));
        }
        
        return l;
    }

    public List<E> getNonSelectedItems() {

        ListModel<E> model = m_unselectedList.getModel();
        int size = model.getSize();
        ArrayList<E> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(model.getElementAt(i));
        }

        return l;
    }
    
    public int[] getSelectedIndices() {

        ListModel<E> model = m_selectedList.getModel();
        int nbNonSelected = model.getSize();

        int[] rv = new int[nbNonSelected];
        for (int i = 0; i < nbNonSelected; i++) {
            E item = model.getElementAt(i);
            rv[i] = m_objects.indexOf(item);
        }

        return rv;
    }
    
    public int[] getNonSelectedIndices() {

        ListModel<E> model = m_unselectedList.getModel();
        int nbNonSelected = model.getSize();

        int[] rv = new int[nbNonSelected];
        for (int i = 0; i < nbNonSelected; i++) {
            E item = model.getElementAt(i);
            rv[i] = m_objects.indexOf(item);
        }

        return rv;
    }
    
    public final JPanel createListPanel(boolean selectedList, String name, ArrayList<E> objects, ArrayList<Boolean> selection) {
        JPanel listPanel = new JPanel(new GridBagLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(name));
        listPanel.setBackground(Color.white);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        DefaultListModel<E> listModel = new DefaultListModel<>();
        for (int i = 0; i < objects.size(); i++) {
            if (((selectedList) && (selection.get(i))) || ((!selectedList) && (!selection.get(i)))) {
                listModel.addElement(objects.get(i));
            }
        }
        JList<E> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        JScrollPane listScrollPane = new JScrollPane(list) {

            private final Dimension preferredSize = new Dimension(280, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        listPanel.add(listScrollPane, c);

        if (selectedList) {
            m_selectedList = list;
        } else {
            m_unselectedList = list;
        }
        return listPanel;
    }
    
    public final JPanel createMiddlePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JButton arrowRight = new JButton(IconManager.getIcon(IconManager.IconType.ARROW));
        JButton arrowLeft = new JButton(IconManager.getIcon(IconManager.IconType.BACK));
        
        m_selectionComboBox = new JComboBox();
        m_selectionComboBox.setVisible(false);
        
        arrowLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List objectsToMoveList = m_unselectedList.getSelectedValuesList();
                
                // new list of selected columns List
                List<E> selectedColumnsNonSorted = getSelectedItems();
                selectedColumnsNonSorted.addAll(objectsToMoveList);
                
                // clean up models
                DefaultListModel<E> selectedModel = (DefaultListModel<E>) m_selectedList.getModel();
                DefaultListModel<E> unselectedModel = (DefaultListModel<E>) m_unselectedList.getModel();
                selectedModel.removeAllElements();
                unselectedModel.removeAllElements();

                int[] selectedIndices = new int[objectsToMoveList.size()];
                int index = 0;
                int indexCur = 0;
                for (int i=0;i<m_objects.size();i++) {
                    E object = m_objects.get(i);
                    if (selectedColumnsNonSorted.contains(object)) {
                        selectedModel.addElement(object);
                        
                        if (objectsToMoveList.contains(object)) {
                            selectedIndices[index] = indexCur;
                            index++;
                        }
                        indexCur++;
                    } else {
                        unselectedModel.addElement(object);
                    }
                    
                }
                m_selectedList.setSelectedIndices(selectedIndices);
                
                
                
            }
        });
        
        arrowRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List objectsToMoveList = m_selectedList.getSelectedValuesList();
                
                // new list of unselected columns List
                List<E> unselectedColumnsNonSorted = getNonSelectedItems();
                unselectedColumnsNonSorted.addAll(objectsToMoveList);
                
                // clean up models
                DefaultListModel<E> selectedModel = (DefaultListModel<E>) m_selectedList.getModel();
                DefaultListModel<E> unselectedModel = (DefaultListModel<E>) m_unselectedList.getModel();
                selectedModel.removeAllElements();
                unselectedModel.removeAllElements();

                int[] selectedIndices = new int[objectsToMoveList.size()];
                int index = 0;
                int indexCur = 0;
                for (int i=0;i<m_objects.size();i++) {
                    E object = m_objects.get(i);
                    if (unselectedColumnsNonSorted.contains(object)) {
                        unselectedModel.addElement(object);
                        if (objectsToMoveList.contains(object)) {
                            selectedIndices[index] = indexCur;
                            index++;
                        }
                        indexCur++;
                    } else {
                        selectedModel.addElement(object);
                    }
                    
                }
                m_unselectedList.setSelectedIndices(selectedIndices);
                
                
                
            }
        });
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        panel.add(Box.createVerticalBox(), c);
        
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(arrowRight, c);
        
        c.gridy++;
        panel.add(arrowLeft, c);
        
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        panel.add(Box.createVerticalBox(), c);
        
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        panel.add(m_selectionComboBox, c);

        
        return panel;
    }

    private String[] preparePrefixAndSuffix(ArrayList<E> objects) {

        HashMap<String, Integer> similarColumnsNumberMap = new HashMap<>();
        HashMap<String, String> similarColumnsColorsMap = new HashMap<>();

        int size = objects.size();
        for (int i = 0; i < size; i++) {

            String fullValue = objects.get(i).toString(); // potentially with html code
            String nohtmlValue = fullValue;
            
            int colorIndexStart = fullValue.indexOf("<font color='", 0);
            int colorIndexStop = fullValue.indexOf("</font>", 0);
            if ((colorIndexStart!=-1) && (colorIndexStop > colorIndexStart)) {    
                nohtmlValue = nohtmlValue.substring(0,colorIndexStart)+nohtmlValue.substring(colorIndexStop+"</font>".length(),nohtmlValue.length());
            }
                
            int indexStart = nohtmlValue.indexOf('<');
            int indexStop = nohtmlValue.indexOf('>');
            while ((indexStart!=-1) && (indexStop > indexStart)) {
                nohtmlValue = nohtmlValue.substring(0,indexStart)+nohtmlValue.substring(indexStop+1,nohtmlValue.length());
                indexStart = nohtmlValue.indexOf('<');
                indexStop = nohtmlValue.indexOf('>');
            }

            // PREFIX --------------
            String prefix = nohtmlValue;

            int indexFirstSpace = prefix.indexOf(' ');
            if (indexFirstSpace != -1) {
                prefix = prefix.substring(0, indexFirstSpace);
            }
            if (prefix.length() > 0) {
                Integer nb = similarColumnsNumberMap.get(prefix);
                if (nb == null) {
                    similarColumnsNumberMap.put(prefix, 1);
                } else {
                    similarColumnsNumberMap.put(prefix, nb + 1);
                }

                colorIndexStart = fullValue.indexOf("<font color='", 0);
                colorIndexStop = fullValue.indexOf("</font>", 0);
                if ((colorIndexStart > -1) && (colorIndexStop > colorIndexStart)) {
                    String colorName = fullValue.substring(colorIndexStart, colorIndexStop + "</font>".length());
                    String curColorName = similarColumnsColorsMap.get(prefix);
                    if (curColorName != null) {
                        if (curColorName.compareTo(colorName) != 0) {
                            similarColumnsColorsMap.put(prefix, "");
                        }
                    } else {
                        similarColumnsColorsMap.put(prefix, colorName);
                    }
                } else {
                    similarColumnsColorsMap.put(prefix, "");
                }
            }
            
            
            // SUFFIX --------------
            
            String suffix = nohtmlValue;
            int indexLastSpace = suffix.lastIndexOf(' ');
            if (indexLastSpace != -1) {
                suffix = nohtmlValue.substring(indexLastSpace, nohtmlValue.length());
            }

            if (suffix.length() > 0) {
                Integer nb = similarColumnsNumberMap.get(suffix);
                if (nb == null) {
                    similarColumnsNumberMap.put(suffix, 1);
                } else {
                    similarColumnsNumberMap.put(suffix, nb + 1);
                }

                colorIndexStart = fullValue.indexOf("<font color='", 0);
                colorIndexStop = fullValue.indexOf("</font>", 0);
                if ((colorIndexStart > -1) && (colorIndexStop > colorIndexStart)) {
                    String colorName = fullValue.substring(colorIndexStart, colorIndexStop + "</font>".length());
                    String curColorName = similarColumnsColorsMap.get(suffix);
                    if (curColorName != null) {
                        if (curColorName.compareTo(colorName) != 0) {
                            similarColumnsColorsMap.put(suffix, "");
                        }
                    } else {
                        similarColumnsColorsMap.put(suffix, colorName);
                    }
                } else {
                    similarColumnsColorsMap.put(suffix, "");
                }

            }
    
            // EXTENDED SUFFIX --------------
            while (indexLastSpace != -1) {
                indexLastSpace = addSuffix(indexLastSpace, nohtmlValue, fullValue, similarColumnsNumberMap, similarColumnsColorsMap );
            }
        }

        // suppression des lignes à un résultat dans similarColumnsNumberMap
        Set<String> colNamesSet = similarColumnsNumberMap.keySet();
        String[] colNamesArray = colNamesSet.toArray(new String[colNamesSet.size()]);
        for (int i = 0; i < colNamesArray.length; i++) {
            String colName = colNamesArray[i];
            Integer nb = similarColumnsNumberMap.get(colName);
            if ((nb <= 1) || (nb>size/2)) {
                similarColumnsNumberMap.remove(colName);
            }
        }
        String[] groups = null;

        int nbGroups = similarColumnsNumberMap.size();
        if (nbGroups > 0) {
            groups = new String[nbGroups];
            Iterator<String> it = similarColumnsNumberMap.keySet().iterator();
            int i = 0;
            while (it.hasNext()) {
                String name = it.next();
                String colorName = similarColumnsColorsMap.get(name);
                groups[i] = ((colorName!=null) && (colorName.length() > 0)) ? "<html>" + colorName + name + "</html>" : name;
                i++;
            }

            Arrays.sort(groups);

        }
        
        return groups;
    }

    private int addSuffix(int indexLastSpace, String nohtmlValue, String fullValue, HashMap<String, Integer> similarColumnsNumberMap, HashMap<String, String> similarColumnsColorsMap) {
        if (indexLastSpace != -1) {
            String suffix = nohtmlValue;
            int indexPreviousSpace = nohtmlValue.substring(0, indexLastSpace).lastIndexOf(' ');
            if (indexPreviousSpace != -1) {
                suffix = nohtmlValue.substring(indexPreviousSpace, nohtmlValue.length());

                if (suffix.length() > 0) {
                    Integer nb = similarColumnsNumberMap.get(suffix);
                    if (nb == null) {
                        similarColumnsNumberMap.put(suffix, 1);
                    } else {
                        similarColumnsNumberMap.put(suffix, nb + 1);
                    }

                    int colorIndexStart = fullValue.indexOf("<font color='", 0);
                    int colorIndexStop = fullValue.indexOf("</font>", 0);
                    if ((colorIndexStart > -1) && (colorIndexStop > colorIndexStart)) {
                        String colorName = fullValue.substring(colorIndexStart, colorIndexStop + "</font>".length());
                        String curColorName = similarColumnsColorsMap.get(suffix);
                        if (curColorName != null) {
                            if (curColorName.compareTo(colorName) != 0) {
                                similarColumnsColorsMap.put(suffix, "");
                            }
                        } else {
                            similarColumnsColorsMap.put(suffix, colorName);
                        }
                    } else {
                        similarColumnsColorsMap.put(suffix, "");
                    }

                }
            }
            
            return indexPreviousSpace;
        }
        
        return -1;
    }
}
