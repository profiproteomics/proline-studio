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
package fr.proline.studio.parameter;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import org.openide.windows.WindowManager;

/**
 * Generic Settings Button
 * @author JM235353
 */
public class SettingsButton extends JButton implements ActionListener {

    private ProgressInterface m_progressInterface = null;
    private SettingsInterface m_settingsInterface = null;
            
    public SettingsButton(ProgressInterface progressInterface, SettingsInterface settingsInterface) {

        m_settingsInterface = settingsInterface;
        m_progressInterface = progressInterface;

        setIcon(IconManager.getIcon(IconManager.IconType.SETTINGS));
        setToolTipText("Settings...");
        setFocusPainted(false);

        addActionListener(this);
    }

        
    public final void setProgressInterface(ProgressInterface progressInterface) {
        m_progressInterface = progressInterface;
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {

        
        if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Settings dialog is not available while data is loading. Please Wait.");
            dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return;
            }
        }
        
        ArrayList<ParameterList> parameterListArray = m_settingsInterface.getParameters();
        if (parameterListArray == null) {
            return;
        }
        DefaultParameterDialog parameterDialog = new DefaultParameterDialog(WindowManager.getDefault().getMainWindow(), "Settings", parameterListArray);
        parameterDialog.setLocationRelativeTo(this);
        parameterDialog.setVisible(true);
        

        if (parameterDialog.getButtonClicked() == DefaultParameterDialog.BUTTON_OK) {
            m_settingsInterface.parametersChanged();
        } else {
            m_settingsInterface.parametersCanceled();
        }

    }
}