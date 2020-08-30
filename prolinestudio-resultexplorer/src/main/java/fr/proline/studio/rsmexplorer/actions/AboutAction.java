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

package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.WindowManager;
import fr.proline.studio.gui.AboutDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.HelpDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AboutAction extends AbstractAction {

    public AboutAction() {
        putValue(Action.NAME, "About");

        setEnabled(true);
    }



    @Override
    public void actionPerformed(ActionEvent e) {

        Frame f = WindowManager.getDefault().getMainWindow();

        AboutDialog aboutDialog = new AboutDialog(f);
        aboutDialog.centerToWindow(f);
        aboutDialog.setVisible(true);

    }
}
