package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ChangePasswordTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.ChangePasswordDialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to connect or disconnect to a UDS database
 *
 * @author jm235353
 */
@ActionID(category = "File", id = "fr.proline.studio.rsmexplorer.actions.ChangePasswordAction")
@ActionRegistration(displayName = "#CTL_ChangePasswordAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 200)
})
@NbBundle.Messages("CTL_ChangePasswordAction=Change password")
public class ChangePasswordAction extends AbstractAction implements ContextAwareAction {

    public ChangePasswordAction() {
        super(NbBundle.getMessage(ChangePasswordAction.class, "CTL_ChangePasswordAction"));
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ChangePasswordAction();
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


                    AbstractServiceCallback callback = new AbstractServiceCallback() {

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


                    ChangePasswordTask task = new ChangePasswordTask(callback, user, oldPwd, pwd);
                    AccessServiceThread.getAccessServiceThread().addTask(task);

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
