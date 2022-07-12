/* 
 * Copyright (C) 2019
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
import fr.proline.studio.parameter.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.prefs.Preferences;


/**
 *
 * @author VD225637
 */
public class FilterProtSetDialog extends DefaultStorableDialog implements ComponentListener {

    private static FilterProtSetDialog m_singletonDialog = null;

    private ParameterList m_parameterList;
    private AbstractParameter[] m_proteinFilterParameters;
    private ParametersComboPanel m_proteinPrefiltersPanel;

    private final static String SETTINGS_KEY = "ProtSetFiltering";

    public static FilterProtSetDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new FilterProtSetDialog(parent);
        }
        return m_singletonDialog;
    }

    public FilterProtSetDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("ProteinSet Filtering");

        setDocumentationSuffix("id.2lwamvv");

        m_parameterList = new ParameterList("ProtSet Filtering");
        m_proteinFilterParameters = FilterProteinSetPanel.createProteinSetFilterParameters("", m_parameterList);
        m_parameterList.updateValues(NbPreferences.root());

        setInternalComponent(createInternalPanel());

//        m_proteinPrefiltersPanel.initProteinFilterPanel();

    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        this.setHelpHeaderText("Warning: Filtering will be done on current dataset, keeping previous validation and filters.");

        m_proteinPrefiltersPanel = new ParametersComboPanel(" Filter(s) ", m_proteinFilterParameters);
        m_proteinPrefiltersPanel.addComponentListener( this);
        internalPanel.add(m_proteinPrefiltersPanel, c);

        return internalPanel;
    }

    public HashMap<String, String> getArguments() {
        return m_parameterList.getValues();
    }

    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParameters()) {
            return false;
        }

        // save parameters
        Preferences preferences = NbPreferences.root();
        saveParameters(preferences);

        return true;
    }

    /***  DefaultStorableDialog Abstract methods ***/

    @Override
    protected String getSettingsKey() {
        return SETTINGS_KEY;
    }

    @Override
    protected void saveParameters(Preferences preferences) {
        // Save Parameters        
        m_parameterList.saveParameters(preferences);

    }

    @Override
    protected boolean checkParameters() {
        // check parameters
        ParameterError error = m_parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        return true;
    }

    @Override
    protected void resetParameters() {
        m_parameterList.initDefaults();
        m_proteinPrefiltersPanel.clearPanel();
    }

    @Override
    protected void loadParameters(Preferences filePreferences) throws Exception {
        Preferences preferences = NbPreferences.root();
        String[] keys = filePreferences.keys();
        for (String key : keys) {
            String value = filePreferences.get(key, null);
            preferences.put(key, value);
        }

        m_parameterList.loadParameters(filePreferences);
        m_proteinPrefiltersPanel.updatePanel();
    }


    @Override
    public void componentResized(ComponentEvent e) {
        repack();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

}
