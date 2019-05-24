/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.tasks.DatabasePTMSitesTask;
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
    private DDataset m_dataset;

    public final static String SETTINGS_KEY = "QuantPostProcessing";

    public QuantPostProcessingDialog(Window parent, DDataset dataset) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        m_dataset = dataset;
        setTitle("Compute PostProcessing on Proteins Sets Abundances");

        setDocumentationSuffix("id.2dlolyb");

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        setResizable(true);

        init();

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

    private void init() {
        //Get potential PTMs from dataset
        final ArrayList<PtmSpecificity> ptms = new ArrayList<>();
        DatabasePTMSitesTask task = new DatabasePTMSitesTask(null);
        task.initLoadUsedPTMs(m_dataset.getProject().getId(), m_dataset.getResultSummaryId(), ptms);
        task.fetchData();
        Map<Long, String> ptmSpecificityNameById = ptms.stream().collect(Collectors.toMap(ptmS -> ptmS.getId(), ptmS -> ptmS.toString()));
        m_quantPostProcessingPanel = new QuantPostProcessingPanel(false, ptmSpecificityNameById);
        Preferences preferences = NbPreferences.root();
        m_quantPostProcessingPanel.getParameterList().loadParameters(preferences);
        boolean isAggregation = (m_dataset.isQuantitation() && m_dataset.isAggregation()) ? true : false;
        m_quantPostProcessingPanel.setDiscardPeptidesSharingPeakelsChB(isAggregation);
        setInternalComponent(m_quantPostProcessingPanel);
    }

    public Map<String, Object> getQuantParams() {
        return m_quantPostProcessingPanel.getQuantParams();
    }
}
