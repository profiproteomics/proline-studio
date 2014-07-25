package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author JM235353
 */
public class ChangeTypicalProteinDialog extends DefaultDialog {
    
    private static ChangeTypicalProteinDialog m_singletonDialog = null;
    private ChangeTypicalProteinPanel changePanel = null;
//
//    public static final String[] REGEX_TARGET_OPTIONS = { "Protein Accession", "Protein Description" };
//    private static final Integer NBR_RULES = 3;
//    private JTextField[] m_regexTextFields = null;
//    private JComboBox[] m_regexTargetComboboxs = null;
    
//    private List<ChangeTypicalRule> m_changeTypicalRules = null;
    
    
//    private JTextField m_regexTextField = null;
//    private JComboBox m_regexTargetCombobox = null;
    
    public static ChangeTypicalProteinDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ChangeTypicalProteinDialog(parent);
        }

        return m_singletonDialog;
    }
    
    public ChangeTypicalProteinDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Change Typical Protein");
        
        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:changetypicalprot");
//        m_regexTextFields = new JTextField[NBR_RULES];
//        m_regexTargetComboboxs = new JComboBox[NBR_RULES];
//        m_changeTypicalRules = new ArrayList<>(NBR_RULES);
        setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);
        setStatusVisible(false);

        changePanel = new ChangeTypicalProteinPanel();
        setInternalComponent(changePanel);
//        setInternalComponent(createInternalPanel());

        restoreInitialParameters();
    }
    
    private JPanel createInternalPanel() {
        
        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        JTextArea helpTxt = new JTextArea("Specify rules to satisfy for choosing typical protein, in priority order");
        helpTxt.setRows(2);
        helpTxt.setForeground(Color.gray);
        internalPanel.add(helpTxt, c);
        
        changePanel = new ChangeTypicalProteinPanel();
        c.gridy++;
        c.gridwidth=2;
        internalPanel.add(changePanel, c);
        
        return internalPanel;
    }
    
    
    
    public List<ChangeTypicalRule> getChangeTypicalRules() {
        return changePanel.getChangeTypicalRules();
//        
//        for(int i=0; i<NBR_RULES; i++){
//            if(!m_regexTextFields[i].getText().trim().isEmpty()){
//                m_changeTypicalRules.set(i, new ChangeTypicalRule(wildcardToRegex(m_regexTextFields[i].getText().trim()), m_regexTargetComboboxs[i].getSelectedIndex() == 0));
//            }
//        }
//        return m_changeTypicalRules;
    }
        
     
    
    @Override
    protected boolean okCalled() {
//        Preferences preferences = NbPreferences.root();
//        for(int i=0; i<NBR_RULES; i++){
//            preferences.put("TypicalProteinRegex_"+i, m_regexTextFields[i].getText().trim());        
//            preferences.putBoolean("TypicalProteinRegexOnAccession_"+i, (m_regexTargetComboboxs[i].getSelectedIndex() == 0));
//        }
        changePanel.savePreference();
        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    private void restoreInitialParameters() {
        changePanel.restoreInitialParameters();
//        Preferences preferences = NbPreferences.root();
//        for(int i=0; i<NBR_RULES; i++){
//            String regex = preferences.get("TypicalProteinRegex_"+i, "*");   
//            m_regexTextFields[i].setText(regex);
//            boolean regexOnAccession = preferences.getBoolean("TypicalProteinRegexOnAccession_"+i, Boolean.TRUE);
//            if (regexOnAccession) {
//                m_regexTargetComboboxs[i].setSelectedIndex(0);
//            } else {
//                m_regexTargetComboboxs[i].setSelectedIndex(1);
//            }
//        }
    }
    
}
