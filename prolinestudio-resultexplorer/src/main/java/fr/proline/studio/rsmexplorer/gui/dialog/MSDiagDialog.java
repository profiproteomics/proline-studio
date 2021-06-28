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

import fr.proline.studio.NbPreferences;
import fr.proline.studio.gui.DefaultStorableDialog;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 *
 * Dialog used to set MSDiag settings
 *
 * @author AW
 */
public class MSDiagDialog extends DefaultStorableDialog {

    private static MSDiagDialog m_singletonDialog = null;
    private final static String SETTINGS_KEY = "MSDiag";

    private JComboBox m_parserComboBox;
    private ParameterList m_sourceParameterList;
    private JPanel m_parserParametersPanel = null;

    public static MSDiagDialog getDialog(Window parent/*, long projectId*/) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new MSDiagDialog(parent);
        }

        m_singletonDialog.reinitialize();

        return m_singletonDialog;
    }

    private MSDiagDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Define settings for Statistical Reports");

        setDocumentationSuffix("id.1qoc8b1");

        setResizable(true);
        setMinimumSize(new Dimension(200, 240));

        initInternalPanel();

    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        // create all other parameters panel
        JPanel allParametersPanel = createAllParametersPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridy++;
        c.weighty = 0;
        internalPanel.add(allParametersPanel, c);

        setInternalComponent(internalPanel);

    }

    private JPanel createAllParametersPanel() {
        JPanel allParametersPanel = new JPanel(new GridBagLayout());
        allParametersPanel.setBorder(BorderFactory.createTitledBorder(" Quality Control Reports Settings "));

        // create parserPanel
        JPanel settingsPanel = createSettingsPanel();

        // create parameter panel
        m_parserParametersPanel = createMSDiagParametersPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        allParametersPanel.add(settingsPanel, c);

        c.gridy++;
        allParametersPanel.add(m_parserParametersPanel, c);

        // init the first parser parameters panel selected
        m_parserComboBox.setSelectedIndex(0);

        return allParametersPanel;
    }

    private JPanel createSettingsPanel() {
        // Creation of Objects for the Parser Panel
        JPanel parserPanel = new JPanel(new GridBagLayout());

        m_parserComboBox = new JComboBox(createParameters());

        // Placement of Objects for Parser Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        parserPanel.add(m_parserComboBox, c);

//      
        m_parserComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_parserComboBox.getSelectedIndex();

                initParameters();

                // resize the dialog
                repack();
            }
        });

        return parserPanel;
    }

    private JPanel createMSDiagParametersPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(" Statistical Settings... "));
        return panel;
    }

    private void initParameters() {

        // remove all parameters
        m_parserParametersPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        m_parserParametersPanel.add(parameterList.getPanel(), c);

    }

    private void reinitialize() {

        // reinit of some parameters
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.clean();

    }

    @Override
    protected String getSettingsKey() {
        return SETTINGS_KEY;
    }

    @Override
    protected boolean checkParameters() {
        //       ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        // check source parameters
        //ParameterError error = m_sourceParameterList.checkParameters();
        // check specific parameters
//        if (error == null) {
//            error = parameterList.checkParameters();
//        }
//
//        // report error
//        if (error != null) {
//            setStatus(true, error.getErrorMessage());
//            highlight(error.getParameterComponent());
//            return false;
//        }
        return true;
    }

    @Override
    protected void resetParameters() throws Exception {
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.initDefaults();

    }

    @Override
    protected void loadParameters(Preferences filePreferences) throws Exception {
        Preferences preferences = NbPreferences.root();
        String[] keys = filePreferences.keys();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = filePreferences.get(key, null);
            preferences.put(key, value);
        }


        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.loadParameters(filePreferences);
    }


    @Override
    protected void saveParameters(Preferences preferences) {

        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();

        // save parser
        String parserSelected = parameterList.toString();
        preferences.put("IdentificationParser", parserSelected);

        // save file path
        // Save Other Parameters
        //m_sourceParameterList.saveParameters(preferences);
        //parameterList.saveParameters(preferences);
    }

    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParameters()) {
            return false;
        }

        saveParameters(NbPreferences.root());

        return true;

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }


    public HashMap<String, String> getMSDiagSettings() {
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        return parameterList.getValues();
    }

    private ParameterList[] createParameters() {
        ParameterList[] plArray = new ParameterList[1];
        plArray[0] = createMSDiagSettings();

        return plArray;
    }

    private ParameterList createMSDiagSettings() {
        ParameterList parameterList = new ParameterList(SETTINGS_KEY);
        parameterList.add(new StringParameter("score.windows", "Score windows (ex: 20-40-60)", JTextField.class, "20-40-60", null, null));
        parameterList.add(new IntegerParameter("max.rank", "Max rank", JTextField.class, new Integer(1), new Integer(0), null));
        //parameterList.add(new IntegerParameter("scan.groups.size", "Scan groups size", JTextField.class, new Integer(1), new Integer(0), new Integer(0)));

        return parameterList;
    }

}
