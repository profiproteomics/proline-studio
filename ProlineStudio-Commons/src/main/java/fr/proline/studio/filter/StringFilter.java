package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on String values with wildcards : * and ?
 *
 * @author JM235353
 */
public class StringFilter extends Filter {

    private static final Integer SEARCH_TEXT = 0;
    private String m_filterText = null;
    private Pattern m_searchPattern;

    public StringFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(FilterType.FILTER_STRING, variableName, convertValueInterface, modelColumn);
    }

    @Override
    public Filter cloneFilter() {
        StringFilter clone = new StringFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_filterText = m_filterText;
        clone.m_searchPattern = m_searchPattern;
        setValuesForClone(clone);
        return clone;
    }
    
    public boolean filter(String value) {
        if (m_filterText == null) {
            return true;
        }

        Matcher matcher = m_searchPattern.matcher(value);

        return matcher.matches();
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
    public boolean registerValues() {

        boolean hasChanged = false;
        
        if (isDefined()) {
            
            String lastValue = m_filterText;
            
            m_filterText = ((JTextField) getComponent(SEARCH_TEXT)).getText().trim();
            if (m_filterText.isEmpty()) {
                m_filterText = null;
            }

            m_searchPattern = compileRegex(m_filterText);
            
            hasChanged = (lastValue == null) || (m_filterText==null) || (lastValue.compareTo(m_filterText)!=0);
        }

        registerDefinedAsUsed();
        
        return hasChanged;
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
        p.add(new JLabel("="), c);
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        JTextField vTextField = ((JTextField) getComponent(SEARCH_TEXT));
        if (vTextField == null) {
            vTextField = new JTextField(8);
            vTextField.setToolTipText("<html>Search is based on wildcards:<br>  '*' : can replace all characters<br>  '?' : can replace one character<br><br>Use 'FOO*' to search a string starting with FOO. </html>");
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
    }
}
