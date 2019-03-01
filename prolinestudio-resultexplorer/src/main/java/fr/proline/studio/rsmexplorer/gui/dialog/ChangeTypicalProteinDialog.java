package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class ChangeTypicalProteinDialog extends DefaultDialog {

    private static ChangeTypicalProteinDialog m_singletonDialog = null;
    private ChangeTypicalProteinPanel changePanel = null;

    public static ChangeTypicalProteinDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ChangeTypicalProteinDialog(parent);
        }

        return m_singletonDialog;
    }

    public ChangeTypicalProteinDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Change Typical Protein");

        setDocumentationSuffix("id.111kx3o");

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
        JTextArea helpTxt = new JTextArea("Specify rules to satisfy for choosing typical protein, in priority order: \n For each protein set, rule 0 will be tested then rule 1...");
        helpTxt.setRows(2);
        helpTxt.setForeground(Color.gray);
        internalPanel.add(helpTxt, c);

        changePanel = new ChangeTypicalProteinPanel();
        c.gridy++;
        c.gridwidth = 2;
        internalPanel.add(changePanel, c);

        return internalPanel;
    }

    public List<ChangeTypicalRule> getChangeTypicalRules() {
        return changePanel.getChangeTypicalRules();

    }

    @Override
    protected boolean okCalled() {

        changePanel.savePreference(NbPreferences.root());
        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    private void restoreInitialParameters() {
        changePanel.restoreInitialParameters();

    }

}
