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

import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import fr.proline.studio.WindowManager;

/**
 *
 * @author VD225637
 */
public class GetSystemInfoButtonAction extends JButton implements ActionListener {

    public GetSystemInfoButtonAction() {

        setIcon(IconManager.getIcon(IconManager.IconType.INFORMATION));
        setToolTipText("Get System information");

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SystemInfoDialog dialog = SystemInfoDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.updateInfo();
        dialog.setVisible(true);
    }
}
