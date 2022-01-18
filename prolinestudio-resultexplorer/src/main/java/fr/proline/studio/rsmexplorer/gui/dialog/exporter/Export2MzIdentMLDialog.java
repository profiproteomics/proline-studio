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
package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.DefaultStorableDialog;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.util.HashMap;
import java.util.prefs.Preferences;


/**
 *
 * @author VD225637
 */
public class Export2MzIdentMLDialog extends DefaultStorableDialog {

    protected final static String MZIDENT_SETTINGS_KEY = "Export2MzIdentML";

    private static final int STEP_PANEL_EXPORT_PARAM_DEF = 0;
    private static final int STEP_PANEL_FILE_CHOOSER = 1;
    private int m_step = STEP_PANEL_EXPORT_PARAM_DEF;

    private DefaultDialog.ProgressTask m_task = null; //VDS ? 
    private Export2MzIdentMLParamPanel m_paramPanel;
    private Export2MzIdentMLFilePanel m_filePanel;

    public Export2MzIdentMLDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Export to MzIdentML format");
        setResizable(true);

//        setDocumentationSuffix(m_contact_FN_key); //VDS TODO
        setButtonName(BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));
        setDocumentationSuffix("id.338fx5o");
        m_paramPanel = new Export2MzIdentMLParamPanel(this);
        this.setHelpHeader(IconManager.getIcon(IconManager.IconType.INFORMATION),"MzIdentML parameters", "Spectrum Matches should have been generated before exporting to MzidentML format.<br> It is also recommended to jave run 'Retrieve proteins sequences'.");
        setInternalComponent(m_paramPanel);
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }

    /***  DefaultStorableDialog Abstract methods ***/

    @Override
    protected String getSettingsKey() {
        return MZIDENT_SETTINGS_KEY;
    }

    @Override
    protected void saveParameters(Preferences filePreferences)  {
        m_paramPanel.saveParameters(filePreferences);
    }

    @Override
    protected boolean checkParameters() {
        return true;
    }

    @Override
    protected void resetParameters()  {
        m_paramPanel.resetParameters();
    }

    @Override
    protected void loadParameters(Preferences preferences) throws Exception {
        m_paramPanel.loadParameters(preferences);
    }

    @Override
    protected boolean okCalled() {
        if (m_step == STEP_PANEL_EXPORT_PARAM_DEF) {
            if (!m_paramPanel.checkParameters()) {
                return false;
            }

            m_paramPanel.saveParameters(null);

            // change to ok button before call to last panel
            setButtonName(BUTTON_OK, "OK");
            setButtonIcon(BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

            setButtonVisible(BUTTON_LOAD, false);
            setButtonVisible(BUTTON_SAVE, false);

            m_filePanel = new Export2MzIdentMLFilePanel(this);
            this.setHelpHeader(IconManager.getIcon(IconManager.IconType.INFORMATION), "MzIdentML output file", "Spectrum Matches should have been generated before exporting to MzidentML format.<br> It is also recommended to jave run 'Retrieve proteins sequences'.");
            replaceInternalComponent(m_filePanel);
            
            revalidate();
            repaint();
            m_step = STEP_PANEL_FILE_CHOOSER;

            return false;
        } else {
            if (!m_filePanel.checkParameters()) {
                return false;
            }
            startTask(m_task);
            return false;
        }
    }

    @Override
    protected boolean loadCalled() {
        if (m_step == STEP_PANEL_EXPORT_PARAM_DEF) {
           super.loadCalled();
        }
        return false;
    }

    @Override
    protected boolean saveCalled() {

        if (m_step == STEP_PANEL_EXPORT_PARAM_DEF) {

            // check parameters 
            if (!m_paramPanel.checkParameters()) {
                return false;
            }

            return super.saveCalled();
        }
        return false;
    }

    public HashMap<String, Object> getExportParams() {
        if (m_paramPanel != null) {
            return m_paramPanel.getExportParams();
        } else {
            return null;
        }
    }

    public String getFileName() {
        if (m_filePanel != null) {
            return m_filePanel.getFileName();
        } else {
            return null;
        }
    }
}
