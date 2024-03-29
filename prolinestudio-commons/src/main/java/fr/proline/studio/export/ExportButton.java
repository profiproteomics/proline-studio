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
package fr.proline.studio.export;

import fr.proline.studio.WindowManager;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTable;

/**
 * Button to export data of a table or image of a JPanel
 * @author JM235353
 */
public class ExportButton extends JButton implements ActionListener {

    private final String m_exportName;
    private JXTable m_table = null;
    private JPanel m_panel = null;

    private ProgressInterface m_progressInterface = null;
            
    public ExportButton(ProgressInterface progressInterface, String exportName, JXTable table) {

        setProgressInterface(progressInterface);
        
        m_exportName = exportName;
        m_table = table;
        
        setIcon(IconManager.getIcon(IconManager.IconType.EXPORT));
        setToolTipText("Export Data...");
        setFocusPainted(false);

        addActionListener(this);
    }
    
    public final void setProgressInterface(ProgressInterface progressInterface) {
        m_progressInterface = progressInterface;
    }

    
    public ExportButton(String exportName, JPanel panel) {


        m_exportName = exportName;
        m_panel = panel;
        
        setIcon(IconManager.getIcon(IconManager.IconType.EXPORT_IMAGE));
        setToolTipText("Export Image...");

        addActionListener(this);
    }
 
    

    @Override
    public void actionPerformed(ActionEvent e) {

        
        if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Export is not available while data is loading. Please Wait.");
            dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return;
            }
        }

        ExportDialog dialog;
        
        if (m_table != null) {
            dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_table, m_exportName);
        }

        else { // then png / svg output only
            dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_panel, m_exportName);
        }
        
        dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
        dialog.setVisible(true);

    }
}
