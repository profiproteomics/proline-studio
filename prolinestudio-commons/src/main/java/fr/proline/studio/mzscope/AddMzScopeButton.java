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
package fr.proline.studio.mzscope;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.openide.windows.WindowManager;

/**
 * Button to access to mzScope
 * 
 * @author MB243701
 */
public abstract class AddMzScopeButton extends JButton implements ActionListener {

    private MzScopeInterface m_mzScopeInterface = null;
    private ProgressInterface m_progressInterface = null;

    public AddMzScopeButton(ProgressInterface progressInterface, MzScopeInterface mzScopeInterface) {
        m_progressInterface = progressInterface;
        m_mzScopeInterface = mzScopeInterface;
        init();
    }

    private void init() {
        setIcon(IconManager.getIcon(IconManager.IconType.WAVE));
        setToolTipText("Add mzdb file to MzScope...");

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Data are not available, loading is not finished. Please Wait.");
            dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return;
            }
        }

        actionPerformed(m_mzScopeInterface);

    }

    public abstract void actionPerformed(MzScopeInterface mzScopeInterface);

}
