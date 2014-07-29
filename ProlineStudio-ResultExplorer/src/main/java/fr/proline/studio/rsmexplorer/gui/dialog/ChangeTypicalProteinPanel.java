/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.data.ChangeTypicalRule;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;

/**
 *
 * @author VD225637
 */
public class ChangeTypicalProteinPanel extends javax.swing.JPanel {
    
    public static final String[] REGEX_TARGET_OPTIONS = { "Protein Accession", "Protein Description" };
    private static final Integer NBR_MAX_RULES = 3;
    
    private JTextField[] m_regexTextFields = null;
    private JComboBox[] m_regexTargetComboboxs = null;

    public ChangeTypicalProteinPanel() {         
        m_regexTextFields = new JTextField[NBR_MAX_RULES];
        m_regexTargetComboboxs = new JComboBox[NBR_MAX_RULES];
        
        createPanel();
    }
        
    private void createPanel() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        for(int i=0; i< NBR_MAX_RULES; i++){
            String title = "Rule";
            add(createRulePanel(title, i), c);
            c.gridy++;
        }
        
        JLabel regexHelpLabel = new JLabel("( * = any string ; ? = any character )");
        regexHelpLabel.setForeground(Color.gray);
        add(regexHelpLabel, c);                  
    }
    
    protected void savePreference(){
        Preferences preferences = NbPreferences.root();              
        for(int i=0; i<NBR_MAX_RULES; i++){
            preferences.put("TypicalProteinRegex_"+i, m_regexTextFields[i].getText().trim());        
            preferences.putBoolean("TypicalProteinRegexOnAccession_"+i, (m_regexTargetComboboxs[i].getSelectedIndex() == 0));
        }
    }
    
    protected void restoreInitialParameters(){
        Preferences preferences = NbPreferences.root();        
        for(int i=0; i<NBR_MAX_RULES; i++){
            String regex = preferences.get("TypicalProteinRegex_"+i, "*");   
            m_regexTextFields[i].setText(regex);
            boolean regexOnAccession = preferences.getBoolean("TypicalProteinRegexOnAccession_"+i, Boolean.TRUE);
            if (regexOnAccession) {
                m_regexTargetComboboxs[i].setSelectedIndex(0);
            } else {
                m_regexTargetComboboxs[i].setSelectedIndex(1);
            }
        }
    }
    
    private JPanel createRulePanel(String prefixTitle, Integer ruleIndex){
        
        JPanel rulePanel = new JPanel(new GridBagLayout());
        rulePanel.setBorder(BorderFactory.createTitledBorder(prefixTitle+" "+ruleIndex));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        rulePanel.add(new JLabel("Typical Protein Match :"), c);

        c.gridx++;
        m_regexTextFields[ruleIndex] = new JTextField(16);
        rulePanel.add(m_regexTextFields[ruleIndex], c);

        c.gridx++;
        rulePanel.add(new JLabel("on"), c);
        
        m_regexTargetComboboxs[ruleIndex] = new JComboBox(REGEX_TARGET_OPTIONS);
        c.gridx++;
        rulePanel.add(m_regexTargetComboboxs[ruleIndex], c);
                
        
        return rulePanel;
    }
        
    public void enableRules(boolean enabled){
        for(int i=0; i<NBR_MAX_RULES; i++){
            m_regexTextFields[i].setEnabled(enabled);
            m_regexTargetComboboxs[i].setEnabled(enabled);
        }
    }
    
    public List<ChangeTypicalRule> getChangeTypicalRules() {
        ArrayList<ChangeTypicalRule> m_changeTypicalRules = new ArrayList<>(NBR_MAX_RULES);
        for(int i=0; i<NBR_MAX_RULES; i++){
            if(!m_regexTextFields[i].getText().trim().isEmpty() && !m_regexTextFields[i].getText().trim().equals("*")){
                m_changeTypicalRules.add(i, new ChangeTypicalRule(wildcardToRegex(m_regexTextFields[i].getText().trim()), m_regexTargetComboboxs[i].getSelectedIndex() == 0));
            }
        }
        return m_changeTypicalRules;
    }
    
    private String wildcardToRegex(String text) {
        String escapedText = "^"+escapeRegex(text)+"$";
        
        String wildcardsFilter = escapedText.replaceAll("\\*", ".*").replaceAll("\\?", ".");
        return wildcardsFilter;
    } 
    
    private String escapeRegex(String s) {

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
     

}
