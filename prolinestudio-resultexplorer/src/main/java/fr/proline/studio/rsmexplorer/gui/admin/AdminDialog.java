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
package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


/**
 *
 * @author JM235353
 */
public class AdminDialog extends DefaultDialog  {

    private static AdminDialog m_singletonDialog = null;
    private Boolean m_viewAsUser = true;

    public static AdminDialog getDialog(Window parent, boolean asUser) {
        if (m_singletonDialog == null || asUser!=m_singletonDialog.m_viewAsUser) {
            m_singletonDialog = new AdminDialog(parent, asUser);
        } 
        return m_singletonDialog;
    }

    private AdminDialog(Window parent, boolean asUSer) {
        super(parent, Dialog.ModalityType.MODELESS);
        m_viewAsUser = asUSer;
        setTitle("Admin Dialog");

        setSize(new Dimension(640, 480));
        setMinimumSize(new Dimension(400, 360));
        setResizable(true);

        setDocumentationSuffix("h.gktawz9n942e");

        setButtonVisible(BUTTON_CANCEL, false);
        setButtonName(BUTTON_OK, "Close");
        //setStatusVisible(true);

        initInternalPanel();

    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JTabbedPane tabbedPane = new JTabbedPane(); 
        

        JPanel userAccountsPanel = new UserAccountsPanel(this, !m_viewAsUser);
        JPanel peaklistSoftwarePanel = new PeaklistSoftwarePanel(this, !m_viewAsUser);
        ProjectsPanel projectsPanel = new ProjectsPanel(this, !m_viewAsUser);
        JPanel fragmentationRuleSetPanel = new FragmentationRuleSetPanel(this, !m_viewAsUser);

        tabbedPane.add("User Accounts", userAccountsPanel);
        tabbedPane.add("Peaklist Softwares", peaklistSoftwarePanel);
        tabbedPane.add("Projects and Databases", projectsPanel);
        tabbedPane.add("Fragmentation Rule Sets", fragmentationRuleSetPanel);

        // JPM.TODO : not done for the moment tabbedPane.add("Instrument Config", instrumentConfigPanel);
        

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(tabbedPane, c);

        setInternalComponent(internalPanel);

    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }


    @Override
    protected boolean okCalled() {
        return true;
    }



}
