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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import java.awt.Dialog;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 * Dialog to compute the quantitation profile
 *
 * @author MB243701
 */
public class QuantPostProcessingDialog extends DefaultDialog {

    private QuantPostProcessingPanel m_quantPostProcessingPanel;

    public final static String SETTINGS_KEY = "QuantPostProcessing";

    //public QuantPostProcessingMultipleDialog(Window parent, ArrayList<DataSetNode> nodeList) {
    public QuantPostProcessingDialog(Window parent, ArrayList<PtmSpecificity> ptms, boolean isAggregation) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Compute PostProcessing on Proteins Sets Abundances");

        setDocumentationSuffix("id.2dlolyb");

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        setResizable(true);

        init(ptms, isAggregation);

    }

    @Override
    protected boolean saveCalled() {
        // check parameters
        if (!checkParameters()) {
            return false;
        }

        JFileChooser fileChooser = SettingsUtils.getFileChooser(SETTINGS_KEY);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            FilePreferences filePreferences = new FilePreferences(f, null, "");

            // Save Parameters  
            ParameterList parameterList = m_quantPostProcessingPanel.getParameterList();
            parameterList.saveParameters(filePreferences);

            SettingsUtils.addSettingsPath(SETTINGS_KEY, f.getAbsolutePath());
            SettingsUtils.writeDefaultDirectory(SETTINGS_KEY, f.getParent());
        }

        return false;
    }

    @Override
    protected boolean loadCalled() {

        SettingsDialog settingsDialog = new SettingsDialog(this, SETTINGS_KEY);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);

        if (settingsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            if (settingsDialog.isDefaultSettingsSelected()) {
                ParameterList parameterList = m_quantPostProcessingPanel.getParameterList();
                parameterList.initDefaults();
            } else {
                try {
                    File settingsFile = settingsDialog.getSelectedFile();
                    FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");

                    Preferences preferences = NbPreferences.root();
                    String[] keys = filePreferences.keys();
                    for (String key : keys) {
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }

                    m_quantPostProcessingPanel.loadParameters(filePreferences);
                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    setStatus(true, "Parsing of your Settings File failed");
                }
            }
        }

        return false;
    }

    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParameters()) {
            return false;
        }

        // save parameters
        Preferences preferences = NbPreferences.root();
        m_quantPostProcessingPanel.getParameterList().saveParameters(preferences);

        return true;

    }

    private boolean checkParameters() {
        // check parameters
        ParameterError error = m_quantPostProcessingPanel.getParameterList().checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }

        return true;
    }

    public Map<String, Object> getQuantParams() {
        return m_quantPostProcessingPanel.getQuantParams();
    }

    private void init(ArrayList<PtmSpecificity> ptms, boolean isAggregation) {
        Map<Long, String> ptmSpecificityNameById = ptms.stream().collect(Collectors.toMap(ptmS -> ptmS.getId(), ptmS -> ptmS.toString()));
        m_quantPostProcessingPanel = new QuantPostProcessingPanel(false, ptmSpecificityNameById);
        Preferences preferences = NbPreferences.root();
        m_quantPostProcessingPanel.getParameterList().loadParameters(preferences);
        m_quantPostProcessingPanel.setDiscardPeptidesSharingPeakelsChB(isAggregation);
        setInternalComponent(m_quantPostProcessingPanel);
    }

}
