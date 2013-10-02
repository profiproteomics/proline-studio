package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on String values with wildcards : * and ?
 * @author JM235353
 */
public class StringFilter extends Filter {
    
    private static final Integer SEARCH_TEXT = 0;
    
    private String m_filterText = null;
    private Pattern m_searchPattern;
    
    public StringFilter(String variableName) {
        super(variableName);
    }
    

    
    public boolean filter(String value) {
        if (m_filterText==null) {
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
    public void registerValues() {
        
        if (isDefined()) {
            m_filterText = ((JTextField) getComponent(SEARCH_TEXT)).getText().trim();
            if (m_filterText.isEmpty()) {
                m_filterText = null;
            }

            m_searchPattern = compileRegex(m_filterText);
        }
        
        registerDefinedAsUsed();
    }
    
    
    private static Pattern compileRegex(String text) {
        String escapedText = "^"+escapeRegex(text)+"$";
        String wildcardsFilter = escapedText.replaceAll("\\*", ".*").replaceAll("\\?", ".");
        return Pattern.compile(wildcardsFilter, Pattern.CASE_INSENSITIVE);
    }
    
    private static String escapeRegex(String s) {

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
        JTextField vTextField = new JTextField(8);
        p.add(vTextField, c);
        if (m_filterText != null) {
            vTextField.setText(m_filterText.toString());
        }
        c.gridx+=2;
        registerComponent(SEARCH_TEXT, vTextField);
    }

    @Override
    public void reset() {
        m_filterText = null;
    }

    
}
