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
package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.util.HashMap;

/**
 * Wizard used to set Pride properties using MS and PRIDE ontologies
 *
 * @author VD225637
 */
public class ExportPrideDialog extends DefaultDialog {

    private static final int STEP_PANEL_EXPERIMENT_DEF = 0;
    private static final int STEP_PANEL_PROTOCOL_DEF = 1;
    private static final int STEP_PANEL_SAMPLE_DEF = 2;
    private static final int STEP_PANEL_FILE_CHOOSER = 3;

    private int m_step = STEP_PANEL_EXPERIMENT_DEF;

    private DefaultDialog.ProgressTask m_task = null;

    public ExportPrideDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Export to PRIDE format Wizard");
        setResizable(true);

        setDocumentationSuffix("id.37m2jsg");

        setButtonName(DefaultDialog.BUTTON_OK, "Next");
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));

        //CREATE FIRST PANEL    
        //Reinitialize panels if previoiusly sets
        PrideExpDescPanel.getPrideExpDescPanel().resetPanel();
        PrideProtocolDescPanel.getPrideProtocolDescPanel().resetPanel();
        PrideSampleDescPanel.getPrideSampleDescPanel().resetPanel();
        
        setInternalComponent(PrideExpDescPanel.getPrideExpDescPanel());
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }

    /*
     *  Accepted parameters are :
     *  - exp_title : Title of the experiment described in the pride XML file
     *  - exp_short_label : Short label of the experiment described in the pride XML file
     *  - protocol_description : String representation of the full Protocol PRIDE Section (! mandatory in final doc)
     *  - contact_name : Name of the contact for the data exported to the Pride XML file   (! mandatory in final doc)
     *  - contact_institution : Institution to which belongs the Contact (! mandatory in final doc)
     *  - sample_name : Name of the sample analysed (! mandatory in final doc)
     *  - sample_desc : Description of the sample represented as a map with a comment, tissue ... to be completed 
     */
    @Override
    protected boolean okCalled() {

        if (m_step == STEP_PANEL_EXPERIMENT_DEF) {

            // check values of first panel !
            if (!checkWizardParams(PrideExpDescPanel.getPrideExpDescPanel())) {
                return false;
            }

            replaceInternalComponent(PrideProtocolDescPanel.getPrideProtocolDescPanel());
            setButtonVisible(BUTTON_BACK, true);
            revalidate();
            repaint();

            m_step = STEP_PANEL_PROTOCOL_DEF;

            return false;

        } else if (m_step == STEP_PANEL_PROTOCOL_DEF) { // m_step == STEP_PANEL_PROTOCOL_DEF)

            if (!checkWizardParams(PrideProtocolDescPanel.getPrideProtocolDescPanel())) {
                return false;
            }

            replaceInternalComponent(PrideSampleDescPanel.getPrideSampleDescPanel());
            revalidate();
            repaint();

            m_step = STEP_PANEL_SAMPLE_DEF;
            return false;

        } else if (m_step == STEP_PANEL_SAMPLE_DEF) {

            // check values TODO
            if (!checkWizardParams(PrideSampleDescPanel.getPrideSampleDescPanel())) {
                return false;
            }

            // change to ok button before call to last panel
            setButtonName(DefaultDialog.BUTTON_OK, "OK");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));

            replaceInternalComponent(PrideFileSelectionPanel.getPrideFileSelectionPanel());
            revalidate();
            repaint();
            m_step = STEP_PANEL_FILE_CHOOSER;
            return false;

        } else { //STEP_PANEL_FILE_CHOOSER
            // check values TODO
            if (!checkWizardParams(PrideFileSelectionPanel.getPrideFileSelectionPanel())) {
                return false;
            }

            startTask(m_task);
            return false;
        }

    }

    @Override
    protected boolean backCalled() {

        if (m_step == STEP_PANEL_PROTOCOL_DEF) {

            replaceInternalComponent(PrideExpDescPanel.getPrideExpDescPanel());
            setButtonVisible(BUTTON_BACK, false);

            revalidate();
            repaint();

            m_step = STEP_PANEL_EXPERIMENT_DEF;
            return true;

        } else if (m_step == STEP_PANEL_SAMPLE_DEF) {

            replaceInternalComponent(PrideProtocolDescPanel.getPrideProtocolDescPanel());
            revalidate();
            repaint();
            m_step = STEP_PANEL_PROTOCOL_DEF;
            return true;

        } else { //STEP_PANEL_FILE_CHOOSER
            setButtonName(DefaultDialog.BUTTON_OK, "Next");
            setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.ARROW));

            replaceInternalComponent(PrideSampleDescPanel.getPrideSampleDescPanel());
            revalidate();
            repaint();
            m_step = STEP_PANEL_SAMPLE_DEF;
            return true;
        }

    }

    //TODO DEFINE ParameterList for PrideExpDescPanel ExperimentParams       
    private boolean checkWizardParams(PrideWizardPanel panel) {
        Component c = panel.checkExportPrideParams();

        if (c != null) {
            setStatus(true, panel.getErrorMessage());
            highlight(c);
            return false;
        }
        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    public String getFileName() {
        return PrideFileSelectionPanel.getPrideFileSelectionPanel().getFileName();
    }

    public HashMap<String, Object> getExportParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.putAll(PrideProtocolDescPanel.getPrideProtocolDescPanel().getExportPrideParams());
        params.putAll(PrideExpDescPanel.getPrideExpDescPanel().getExportPrideParams());
        params.putAll(PrideSampleDescPanel.getPrideSampleDescPanel().getExportPrideParams());

        return params;
    }

}
