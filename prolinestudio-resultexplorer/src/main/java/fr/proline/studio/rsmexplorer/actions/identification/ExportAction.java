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
package fr.proline.studio.rsmexplorer.actions.identification;

//import fr.proline.studio.rsmexplorer.actions.ExportPTMDatasetAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;


/**
 * Add Action (menu for sub-actions identification and aggregation)
 *
 * @author JM235353
 */
public class ExportAction extends AbstractRSMAction {

    // Could be ExportXXXAction or ExportXXXJMSAction
    private AbstractRSMAction m_exportDatasetAction;
    private AbstractRSMAction m_exportPTMDatasetAction;
    private AbstractRSMAction m_exportPeakViewSpectraAction;
    private AbstractRSMAction m_exportSpectronautSpectraAction;
    private AbstractRSMAction m_exportMzIdentMLAction;
    private AbstractRSMAction m_exporFastaAction;

    private JMenu m_menu;

    public ExportAction(AbstractTree tree) {
        super("Export", tree);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        m_menu = new JMenu((String) getValue(NAME));

        m_exportDatasetAction = new ExportDatasetJMSAction(getTree());

        if (getTree() == IdentificationTree.getCurrentTree()) {
            m_exportMzIdentMLAction = new ExportMzIdentMLAction(getTree());
        } else {
            m_exportMzIdentMLAction = null;
        }

        JMenuItem exportDatasetItem = new JMenuItem(m_exportDatasetAction);
        m_menu.add(exportDatasetItem);
        if (m_exportMzIdentMLAction != null) {
            JMenuItem exportMzIdentMLItem = new JMenuItem(m_exportMzIdentMLAction);
            m_menu.add(exportMzIdentMLItem);
        }

        JMenu exportMenu = new JMenu("Spectra List");
        m_exportPeakViewSpectraAction = new ExportSpectraListJMSAction(getTree(), ExportSpectraListJMSAction.FormatCompatibility.PeakView);
        m_exportSpectronautSpectraAction = new ExportSpectraListJMSAction(getTree(), ExportSpectraListJMSAction.FormatCompatibility.Spectronaut);

        JMenuItem exportSpectraItem = new JMenuItem(m_exportPeakViewSpectraAction);
        exportMenu.add(exportSpectraItem);
        exportSpectraItem = new JMenuItem(m_exportSpectronautSpectraAction);
        exportMenu.add(exportSpectraItem);
        m_menu.add(exportMenu);
        m_exporFastaAction = new ExportFastaAction(getTree());
        JMenuItem exportFastaItem = new JMenuItem(m_exporFastaAction);
        m_menu.add(exportFastaItem);

        // VDS : Export PTM Dataset in JSON format. For test only
//        m_exportPTMDatasetAction = new ExportPTMDatasetAction(getTree());
//        JMenuItem exportPTMDatasetItem = new JMenuItem(m_exportPTMDatasetAction);
//        m_menu.add(exportPTMDatasetItem);
        return m_menu;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        m_exportDatasetAction.updateEnabled(selectedNodes);
        if (m_exportMzIdentMLAction != null) {
            m_exportMzIdentMLAction.updateEnabled(selectedNodes);
        }

        m_exportSpectronautSpectraAction.updateEnabled(selectedNodes);
        m_exportPeakViewSpectraAction.updateEnabled(selectedNodes);

        boolean isEnabled = m_exportDatasetAction.isEnabled()
               /* || m_exportPTMDatasetAction.isEnabled()*/
                || m_exportSpectronautSpectraAction.isEnabled()
                || m_exportPeakViewSpectraAction.isEnabled()
                || (m_exportMzIdentMLAction != null && m_exportMzIdentMLAction.isEnabled());
        setEnabled(isEnabled);
        m_menu.setEnabled(isEnabled);
    }

}
