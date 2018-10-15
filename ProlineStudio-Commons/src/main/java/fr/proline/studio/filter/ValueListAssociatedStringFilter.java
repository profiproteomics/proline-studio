package fr.proline.studio.filter;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JM235353
 */
public class ValueListAssociatedStringFilter extends Filter {

    private static final String TEXTFIELD_EXAMPLE = "<Specify Residues list like ST>";
    
    private Object[] m_values = null;
    private boolean[] m_selected = null;
    private String[] m_onTextValues = null;
    
    private JCheckBox[] m_checkBoxArray = null;
    private JTextField[] m_textFieldArray = null;
    
    public ValueListAssociatedStringFilter(String variableName, Object[] values, ConvertValueInterface convertValueInterface, int modelColumn1, int modelColumn2) {
        super(variableName, convertValueInterface, modelColumn1, modelColumn2);

        
        m_values = values;
        m_selected = new boolean[m_values.length];
        for (int i = 0; i < m_selected.length; i++) {
            m_selected[i] = false;
        }
        
        m_onTextValues = new String[m_values.length];
        for (int i = 0; i < m_onTextValues.length; i++) {
            m_onTextValues[i] = "";
        }
    }
    
    @Override
    public Filter cloneFilter4Search() {
       ValueListAssociatedStringFilter clone = new ValueListAssociatedStringFilter(m_variableName, m_values, m_convertValueInterface, m_modelColumn, m_extraModelColumn);
       if (m_selected != null) {
           int nb = m_selected.length;
           clone.m_selected = new boolean[nb];
           clone.m_onTextValues = new String[nb];
           for (int i=0;i<nb;i++) {
               clone.m_selected[i] = m_selected[i];
               clone.m_onTextValues[i] = m_onTextValues[i];
           }
            
       }

        setValuesForClone(clone);
        return clone;
    }
    
    @Override
    public boolean filter(Object v1, Object v2) {

        int index = (Integer) v1;
        if (!m_selected[index]) {
            return false;
        }
        
        String onText = m_onTextValues[index];
        if ((onText == null) || (onText.isEmpty())) {
            return true;
        }

        String value = (v2 == null) ? null : v2.toString();
        return((value == null) || (value.isEmpty())) ||  onText.contains(value);


    }
    
    
    @Override
    public FilterStatus checkValues() {
        return null;
    }
    
    @Override
    public boolean registerValues() {

        boolean hasChanged = false;

        if (isDefined()) {
            
            int nb = m_checkBoxArray.length;
            for (int i=0;i<nb;i++) {
                boolean s = m_checkBoxArray[i].isSelected();
                hasChanged |= (s != m_selected[i]);
                m_selected[i] = s;
            }
            
            for (int i=0;i<nb;i++) {
                String t = m_textFieldArray[i].getText().trim();
                if (t.compareTo(TEXTFIELD_EXAMPLE) == 0) {
                    t = "";
                }
                hasChanged |= (t.compareTo(m_onTextValues[i]) == 0);
                m_onTextValues[i] = t;
            }

        }

        registerDefinedAsUsed();

        return hasChanged;
    }
    
    @Override
    public void createComponents(JPanel p, GridBagConstraints c) {
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        JLabel nameLabel = new JLabel(getName());

        p.add(nameLabel, c);

        
        c.gridx += 3;
        c.gridwidth = 4;
        c.weightx = 1;
        p.add(createInternalPanel(), c);

        c.gridx += 4;


    }
    
    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel(new GridBagLayout());
        internalPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        int nb = m_values.length;
        if (m_checkBoxArray == null) {
            m_checkBoxArray = new JCheckBox[nb];
        }
        if (m_textFieldArray == null) {
            m_textFieldArray = new JTextField[nb];
        }
        
        FocusListener focusListener = null;
        
        for (int i = 0; i < m_values.length; i++) {
            if (m_checkBoxArray[i] == null) {
                m_checkBoxArray[i] = new JCheckBox(m_values[i].toString());
            }
            c.gridx = 0;
            c.gridy = i;
            internalPanel.add(m_checkBoxArray[i], c);

            if (m_textFieldArray[i] == null) {
                
                if (focusListener == null) {
                    focusListener = new FocusListener() {
                        @Override
                        public void focusGained(FocusEvent e) {
                            JTextField textField = (JTextField) e.getComponent();
                            if (textField.getText().compareTo(TEXTFIELD_EXAMPLE) == 0) {
                                textField.setText("");
                                textField.setForeground(Color.black);
                            }
                        }

                        @Override
                        public void focusLost(FocusEvent e) {
                        }
                    };
                }
                
                m_textFieldArray[i] = new JTextField(20);
                m_textFieldArray[i].setText(TEXTFIELD_EXAMPLE);
                m_textFieldArray[i].setForeground(Color.gray);
                m_textFieldArray[i].addFocusListener(focusListener);
                m_textFieldArray[i].setToolTipText("<html>Specify accepted residues for "+m_values[i].toString()+"<br>For instance : ST for a Phosphorylation</html>");
            }
            
            c.gridx++;
            internalPanel.add(new JLabel("on"), c);
            
            c.gridx++;
            internalPanel.add(m_textFieldArray[i], c);
        }

        return internalPanel;
    }
    
    @Override
    public void reset() {
        for (int i=0;i<m_selected.length;i++) {
            m_selected[i] = false;
            m_onTextValues[i] = null;
        }

    }
    
}
