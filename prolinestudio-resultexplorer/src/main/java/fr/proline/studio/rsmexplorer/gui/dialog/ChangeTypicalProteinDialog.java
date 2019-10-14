/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.data.ChangeTypicalRule;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.*;
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
        this.setHelpHeaderText("Specify rules to satisfy for choosing typical protein, in priority order: <br> "
                + "For each protein set, rule 0 will be tested then rule 1...");

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
