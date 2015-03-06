package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on String values with wildcards : * and ?
 * The operator could be = or !=, and allows the user to search on an empty string
 * @author JM235353
 */
public class StringDiffFilter extends Filter {

    private static final Integer SEARCH_TEXT = 0;
    private String m_filterText = null;
    private Pattern m_searchPattern;
    private JComboBox m_cbOp = null;
    private int m_selIndex = 0;
    
    
    public StringDiffFilter(String variableName, ConvertValueInterface convertValueInterface) {
        super(FilterType.FILTER_STRING_DIFF, variableName, convertValueInterface);
    }

    public boolean filter(String value) {
        if (m_filterText == null) {
            return true;
        }

        Matcher matcher = m_searchPattern.matcher(value);
        boolean found = matcher.matches();
        return m_cbOp.getSelectedIndex() == 0?found:!found;
    }

    @Override
    public FilterStatus checkValues() {
        if (m_filterText == null) {
            return null;
        }

        try {
            compileRegex(m_filterText);
        } catch (Exception e) {
            return new FilterStatus("Regex Pattern Error", getComponent(SEARCH_TEXT));
        }

        return null;
    }

    @Override
    public void registerValues() {

        if (isDefined()) {
            m_filterText = ((JTextField) getComponent(SEARCH_TEXT)).getText().trim();
            m_searchPattern = compileRegex(m_filterText);
            m_selIndex = m_cbOp.getSelectedIndex();
        }

        registerDefinedAsUsed();
    }

    private static Pattern compileRegex(String text) {
        String escapedText = "^" + escapeRegex(text) + "$";
        String wildcardsFilter = escapedText.replaceAll("\\*", ".*").replaceAll("\\?", ".");
        return Pattern.compile(wildcardsFilter, Pattern.CASE_INSENSITIVE);
    }

    private static String escapeRegex(String s) {
        if (s == null) {
            return "";
        }
        int len = s.length();
        if (len == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ("[](){}.+$^|#\\".indexOf(c) != -1) {
                sb.append("\\");
            }
            sb.append(c);
        }
        return sb.toString();
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
        JTextField vTextField = ((JTextField) getComponent(SEARCH_TEXT));
        if (vTextField == null) {
            vTextField = new JTextField(8);
            if (m_filterText != null) {
                vTextField.setText(m_filterText.toString());
            }
            registerComponent(SEARCH_TEXT, vTextField);
        }
        p.add(vTextField, c);

        c.gridx += 2;

    }

    @Override
    public void reset() {
        m_filterText = null;
        m_selIndex = 0;
        m_cbOp.setSelectedIndex(0);
    }
}
