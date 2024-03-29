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
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ChangePasswordDialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;


/**
 * Action to connect change password
 *
 * @author jm235353
 */

public class ChangePasswordAction extends AbstractAction {

    public ChangePasswordAction() {
        super("Change Password");
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        ChangePasswordDialog dialog = new ChangePasswordDialog(WindowManager.getDefault().getMainWindow());
        dialog.centerToScreen();
        dialog.setVisible(true);
        final boolean[] resultChgPasswd = new boolean[1];
        resultChgPasswd[0] = false;
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            String user = dialog.getUser();
            String oldPwd = dialog.getOldPassword();
            String pwd = dialog.getPassword();

            final Object mutexPasswdLoaded = new Object();

            try {
                synchronized (mutexPasswdLoaded) {

                    AbstractJMSCallback callback = new AbstractJMSCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return false;
                        }

                        @Override
                        public void run(boolean success) {
                            synchronized (mutexPasswdLoaded) {
                                resultChgPasswd[0] = success;
                                mutexPasswdLoaded.notifyAll();
                            }
                        }
                    };

                    fr.proline.studio.dpm.task.jms.ChangePasswordTask task = new fr.proline.studio.dpm.task.jms.ChangePasswordTask(callback, user, oldPwd, pwd);
                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                    // wait untill the change password is done
                    mutexPasswdLoaded.wait();

                    //Test if success 
                    if (resultChgPasswd[0] == false) {
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), callback.getTaskError().getErrorTitle(), "Error", JOptionPane.ERROR_MESSAGE);
                    }

                }

            } catch (InterruptedException ie) {
                // should not happen
            }

        }
    }
}
