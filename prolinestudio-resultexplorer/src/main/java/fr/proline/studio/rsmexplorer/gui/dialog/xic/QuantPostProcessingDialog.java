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
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.NbPreferences;
import fr.proline.studio.corewrapper.data.QuantPostProcessingParams;
import fr.proline.studio.gui.DefaultStorableDialog;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Dialog to compute the quantitation profile
 *
 * @author MB243701
 */
public class QuantPostProcessingDialog extends DefaultStorableDialog {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private QuantPostProcessingPanel m_quantPostProcessingPanel;


    //public QuantPostProcessingMultipleDialog(Window parent, ArrayList<DataSetNode> nodeList) {
    public QuantPostProcessingDialog(Window parent, ArrayList<PtmSpecificity> ptms, boolean isAggregation, DDataset paramsFromdataset) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Compute Post-Processing on Proteins Sets Abundances");

        setDocumentationSuffix("id.thw4kt");

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        setResizable(true);

        init(ptms, isAggregation, paramsFromdataset);

    }

    @Override
    protected String getSettingsKey() {
        return QuantPostProcessingParams.SETTINGS_KEY;
    }

    @Override
    protected void saveParameters(Preferences filePreferences)  {
        // Save Parameters
        ParameterList parameterList = m_quantPostProcessingPanel.getParameterList();
        parameterList.saveParameters(filePreferences);
        filePreferences.put(QuantPostProcessingParams.PARAM_VERSION_KEY, QuantPostProcessingParams.CURRENT_VERSION);
    }

    @Override
    protected void resetParameters()  {
        ParameterList parameterList = m_quantPostProcessingPanel.getParameterList();
        parameterList.initDefaults();
    }

    @Override
    protected void loadParameters(Preferences filePreferences) throws Exception {

        String version = filePreferences.get(QuantPostProcessingParams.PARAM_VERSION_KEY, null);
        boolean modifiedPepParamExist = (filePreferences.get(QuantPostProcessingParams.SETTINGS_KEY+"."+QuantPostProcessingParams.getSettingKey(QuantPostProcessingParams.DISCARD_MODIFIED_PEPTIDES), null) != null);
        if(version == null){
            if(modifiedPepParamExist)
                version = "2.0";
            else
                version = "1.0";
        }
        if (!version.equals(QuantPostProcessingParams.CURRENT_VERSION)) {
            String msg = "Try loading Post Processing parameters ("+ QuantPostProcessingParams.CURRENT_VERSION + ") from file with version "+version+". All parameters may not have been taken into account !";
            JOptionPane.showMessageDialog(this, msg, "Load Post Processing parameters error", JOptionPane.ERROR_MESSAGE);
        }

        m_quantPostProcessingPanel.loadParameters(filePreferences, version);
    }

    @Override
    protected boolean checkParameters() {
        // check parameters
        ParameterError error = m_quantPostProcessingPanel.getParameterList().checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }

        return true;
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



    public Map<String, Object> getQuantParams() {
        return m_quantPostProcessingPanel.getQuantParams();
    }

    private void init(ArrayList<PtmSpecificity> ptms, boolean isAggregation, DDataset dataset) {
        Map<Long, String> ptmSpecificityNameById = ptms.stream().collect(Collectors.toMap(ptmS -> ptmS.getId(), ptmS -> ptmS.toString()));
        m_quantPostProcessingPanel = new QuantPostProcessingPanel(false, ptmSpecificityNameById);

        try {
            if ((dataset != null) && (dataset.getQuantProcessingConfigAsMap() != null)) {

                m_quantPostProcessingPanel.setRefinedParams(dataset.getPostQuantProcessingConfigAsMap());

            } else {
                Preferences preferences = NbPreferences.root();
                m_quantPostProcessingPanel.getParameterList().loadParameters(preferences);
            }
        } catch (Exception ex) {
            m_logger.error("error while settings quanti params " + ex);
        }

        m_quantPostProcessingPanel.setDiscardPeptidesSharingPeakelsChB(isAggregation);

        setInternalComponent(m_quantPostProcessingPanel);
    }

}
