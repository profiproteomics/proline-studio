package fr.proline.studio.filter;

import fr.proline.studio.utils.StringUtils;
import java.awt.GridBagConstraints;
import java.util.regex.Matcher;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on String values with wildcards : * and ?
 * The operator could be = or !=, and allows the user to search on an empty string
 * @author JM235353
 */
public class StringDiffFilter extends StringFilter {


    private JComboBox m_cbOp = null;
    private int m_selIndex = 0;
    
    
    public StringDiffFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
    }

    @Override
    public boolean filter(Object v1, Object v2) {
        if (m_filterText == null) {
            return true;
        }
        
        String value = (String) v1;

        Matcher matcher = m_searchPattern.matcher(value);
        boolean found = matcher.matches();
        return m_cbOp.getSelectedIndex() == 0?found:!found;
    }
    
    @Override
    public boolean registerValues() {

        boolean hasChanged = false;
        
        if (isDefined()) {
            
            String lastValue = m_filterText;
            int lastSelIndex = m_selIndex;
            
            m_filterText = ((JTextField) getComponent(SEARCH_TEXT)).getText().trim();
            m_searchPattern = StringUtils.compileRegex(m_filterText);
            m_selIndex = m_cbOp.getSelectedIndex();
            
            hasChanged = (lastValue == null) || (m_filterText==null) || (lastValue.compareTo(m_filterText)!=0) || (lastSelIndex != m_selIndex);
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
        c.gridwidth = 1;
        c.weightx = 0;
        
        String[] opList = new String[2];
        opList[0] = "=";
        opList[1] = "!=";
        m_cbOp = new JComboBox(opList);
        m_cbOp.setSelectedIndex(m_selIndex);
        p.add(m_cbOp, c);
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        p.add(createTextField(), c);

        c.gridx += 2;

    }

    @Override
    public void reset() {
        m_filterText = null;
        m_selIndex = 0;
        m_cbOp.setSelectedIndex(0);
    }
}
