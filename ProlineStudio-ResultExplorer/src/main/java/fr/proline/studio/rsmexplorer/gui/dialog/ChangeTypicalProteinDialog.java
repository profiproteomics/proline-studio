package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class ChangeTypicalProteinDialog extends DefaultDialog {
    
    private static ChangeTypicalProteinDialog m_singletonDialog = null;

    public static final String[] REGEX_TARGET_OPTIONS = { "Protein Accession", "Protein Description" };
    
    private JTextField m_regexTextField = null;
    private JComboBox m_regexTargetCombobox = null;
    
    public static ChangeTypicalProteinDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ChangeTypicalProteinDialog(parent);
        }


        return m_singletonDialog;
    }
    
    public ChangeTypicalProteinDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Change Typical Protein");
        setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);
        setStatusVisible(false);

        setInternalComponent(createInternalPanel());

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
        internalPanel.add(new JLabel("Typical Protein Match :"), c);

        c.gridx++;
        m_regexTextField = new JTextField(16);
        internalPanel.add(m_regexTextField, c);

        c.gridx++;
        internalPanel.add(new JLabel("on"), c);
        
        m_regexTargetCombobox = new JComboBox(REGEX_TARGET_OPTIONS);
        c.gridx++;
        internalPanel.add(m_regexTargetCombobox, c);
        
        c.gridy++;
        c.gridx = 1;
        JLabel regexHelpLabel = new JLabel("( * = any string ; ? = any character )");
        regexHelpLabel.setForeground(Color.gray);
        internalPanel.add(regexHelpLabel, c);
        
        return internalPanel;
    }
    
    public String getRegex() {
        return wildcardToRegex(m_regexTextField.getText().trim());
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
    
    public boolean regexOnAccession() {
        return (m_regexTargetCombobox.getSelectedIndex() == 0);
    }
    
    @Override
    protected boolean okCalled() {
        Preferences preferences = NbPreferences.root();
        preferences.put("TypicalProteinRegex", m_regexTextField.getText().trim());
        
        preferences.putBoolean("TypicalProteinRegexOnAccession", (m_regexTargetCombobox.getSelectedIndex() == 0));

        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    private void restoreInitialParameters() {
        Preferences preferences = NbPreferences.root();
        String regex = preferences.get("TypicalProteinRegex", "*");
        m_regexTextField.setText(regex);
        
        boolean regexOnAccession = preferences.getBoolean("TypicalProteinRegexOnAccession", Boolean.TRUE);
        if (regexOnAccession) {
            m_regexTargetCombobox.setSelectedIndex(0);
        } else {
            m_regexTargetCombobox.setSelectedIndex(1);
        }

    }
    
}
