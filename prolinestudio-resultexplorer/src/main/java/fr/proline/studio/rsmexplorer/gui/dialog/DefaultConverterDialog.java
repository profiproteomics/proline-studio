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

import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import fr.proline.studio.parameter.FileParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author AK249877
 */
public class DefaultConverterDialog extends DefaultDialog {

    private static DefaultConverterDialog m_singletonDialog = null;

    private ParameterList m_wizardParameterList;

    private Preferences m_preferences;

    private static final String DIALOG_TITLE = "Choose Converter";

    public static DefaultConverterDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new DefaultConverterDialog(parent);
        }

        return m_singletonDialog;
    }

    public DefaultConverterDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle(DIALOG_TITLE);

        setSize(new Dimension(460, 140));
        setMinimumSize(new Dimension(460, 140));
        setResizable(true);

        setButtonVisible(BUTTON_CANCEL, true);
        setButtonName(BUTTON_OK, "OK");
        setStatusVisible(true);

        m_preferences = NbPreferences.root();

        setInternalComponent(createInternalComponent());

    }

    private JComponent createInternalComponent() {

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        panel.setBorder(BorderFactory.createTitledBorder(""));

        m_wizardParameterList = new ParameterList("Conversion/Upload Settings");

        String[] converterExtentions = {"exe"};
        String[] converterFilterNames = {"raw2mzDB.exe"};
        FileParameter m_converterFilePath = new FileParameter(null, "Converter_(.exe)", "Converter (.exe)", JTextField.class, "", converterFilterNames, converterExtentions);
        m_converterFilePath.setAllFiles(false);
        m_converterFilePath.setSelectionMode(JFileChooser.FILES_ONLY);
        m_converterFilePath.setDefaultDirectory(new File(m_preferences.get("Conversion/Upload_Settings.Converter_(.exe)", System.getProperty("user.home"))));
        m_wizardParameterList.add(m_converterFilePath);

        panel.add(m_wizardParameterList.getPanel());
        return panel;

        //m_wizardParameterList.loadParameters(m_preferences);
        //return m_wizardParameterList.getPanel();
    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }

    @Override
    protected boolean okCalled() {

        ParameterError error = m_wizardParameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            super.getDefaultDialogListener().cancelPerformed(this);
            return false;
        }

        m_wizardParameterList.saveParameters(NbPreferences.root());

        try {
            NbPreferences.root().flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Saving Parameters Failed", e);
        }

        if (super.getDefaultDialogListener() != null) {
            super.getDefaultDialogListener().okPerformed(this);
        }

        return true;

    }

    @Override
    protected boolean cancelCalled() {
        if (super.getDefaultDialogListener() != null) {
            super.getDefaultDialogListener().cancelPerformed(this);
        }
        return true;
    }

}
