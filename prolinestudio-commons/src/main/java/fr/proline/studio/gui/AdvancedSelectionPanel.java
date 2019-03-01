package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Panel used for the selection/deselection of objects. There are two lists, a selected one and a deselected one
 * with arrows to move them. There are also a search textfield and a prefix/suffix combobx for fast selection.
 * @author JM235353
 * @param <E>
 */
public class AdvancedSelectionPanel<E> extends JPanel  {
    
    private JList m_selectedList = null;
    private JList m_unselectedList = null;
    
    private ArrayList<E> m_objects = null;
    
    private JTextField m_selectionTextField = null;
    private JLabel m_selectionLabel = null;
    private JComboBox m_selectionComboBox = null;
    
    private static final String SELECT_FROM_PREFIX_SUFFIX = "<Select from Prefix/Suffix>";
    private static final String SEARCH_FOR_TEXT = "<Search for Text>";
    
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
        add(notSelectedPanel, c);

        c.gridx++;
        add(midActionPanel, c);
        
        c.gridx++;
        add(selectedPanel, c);
        
        setFastSelectionValues(preparePrefixAndSuffix(objects));
    }
    
    private void setFastSelectionValues(String[] columnGroupNamesArray) {

        if (columnGroupNamesArray == null) {
            return;
        }
        
        m_selectionComboBox.setVisible(true);
        m_selectionLabel.setVisible(true);
        
        DefaultComboBoxModel model = (DefaultComboBoxModel) m_selectionComboBox.getModel();
        
        model.addElement(SELECT_FROM_PREFIX_SUFFIX);
        
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
                selectFromText(value, m_selectedList);
                selectFromText(value, m_unselectedList);

                // reselect first one
                m_selectionComboBox.setSelectedIndex(0);
                
            }
        });
        
    }
    
  
    private void selectFromText(String value, JList list) {
        ListSelectionModel sm = list.getSelectionModel();
        sm.clearSelection();
        
        if (value.isEmpty()) {
            return;
        }
        
        value = value.toLowerCase();
        
        DefaultListModel<E> listModel = (DefaultListModel) list.getModel();

        int lastSelectedIndex = -1;
        int nb = listModel.getSize();
        for (int i = 0; i < nb; i++) {
            String valueCur = removeHtmlColor(listModel.getElementAt(i).toString()).toLowerCase();

            if (valueCur.indexOf(value) != -1) {
                sm.addSelectionInterval(i, i);
                lastSelectedIndex = i;
            }

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
        
        m_selectionTextField = new JTextField(SEARCH_FOR_TEXT, 30);
        m_selectionTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (m_selectionTextField.getText().compareTo(SEARCH_FOR_TEXT) == 0) { 
                    m_selectionTextField.setText("");
                }
            }
        });

        m_selectionTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }

            public void textChanged() {
                String value = m_selectionTextField.getText();
                selectFromText(value, m_selectedList);
                selectFromText(value, m_unselectedList);
            }
        });
 
        
        m_selectionComboBox = new JComboBox();
        m_selectionComboBox.setVisible(false);
        
        m_selectionLabel = new JLabel(IconManager.getIcon(IconManager.IconType.SEARCH));
        m_selectionLabel.setVisible(false);
        
        arrowRight.addActionListener(new ActionListener() {
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
        
        arrowLeft.addActionListener(new ActionListener() {
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
        c.gridwidth = 2;
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
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JLabel(IconManager.getIcon(IconManager.IconType.SEARCH)), c);
        
        c.gridx++;
        c.weightx = 1;
        panel.add(m_selectionTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        panel.add(m_selectionLabel, c);
        
        c.gridx++;
        c.weightx = 1;
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
            
            
            // ALL POTENTIAL SUFFIX --------------
            int indexLastSpace = nohtmlValue.lastIndexOf(' ');
            while (indexLastSpace != -1) {
                indexLastSpace = addSuffix(indexLastSpace, nohtmlValue, fullValue, similarColumnsNumberMap, similarColumnsColorsMap );
            }
        }

        // suppression des lignes à un résultat dans similarColumnsNumberMap
        Set<String> suffixSet = similarColumnsNumberMap.keySet();
        String[] suffixArray = suffixSet.toArray(new String[suffixSet.size()]);
        for (int i = 0; i < suffixArray.length; i++) {
            String suffixCur = suffixArray[i];
            if (!similarColumnsNumberMap.containsKey(suffixCur)) {
                // suffix already suppressed
                continue;
            }
            Integer nb = similarColumnsNumberMap.get(suffixCur);
            
            if (nb <= 1) {
                similarColumnsNumberMap.remove(suffixCur);
                continue;
            }
            
            for (int j=i+1;j<suffixArray.length;j++) {
                String suffixCur2 = suffixArray[j];
                if (((suffixCur.length()<suffixCur2.length()) && (suffixCur2.endsWith(suffixCur))) ||
                        ((suffixCur.length()>suffixCur2.length()) && (suffixCur.endsWith(suffixCur2)))) {
                    // suffixCur and suffixCur2 are similar  like "vs 5%" and "5%"
                    Integer nb2 = similarColumnsNumberMap.get(suffixCur2);
                    if (nb == nb2) {
                        // suffixes correspond to the same column, we remove the shortest one
                        if (suffixCur.length()<suffixCur2.length()) {
                            similarColumnsNumberMap.remove(suffixCur);
                        } else {
                            similarColumnsNumberMap.remove(suffixCur2);
                        }
                    }
                    
                }
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
