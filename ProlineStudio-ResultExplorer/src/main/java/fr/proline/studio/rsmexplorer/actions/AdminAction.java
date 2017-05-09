package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.rsmexplorer.gui.admin.AdminDialog;
import fr.proline.studio.dam.DatabaseDataManager;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "File",
        id = "fr.proline.studio.rsmexplorer.actions.AdminAction")
@ActionRegistration(displayName = "#CTL_AdminAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 50)
})
@Messages("CTL_AdminAction=Admin")
public final class AdminAction extends AbstractAction implements ActionListener, ContextAwareAction {

    public AdminAction() {
        putValue(Action.NAME, NbBundle.getMessage(AdminAction.class, "CTL_AdminAction"));
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Frame f = WindowManager.getDefault().getMainWindow();

        AdminDialog dialog = AdminDialog.getDialog(f);
        if (dialog.isVisible()) {
            return;
        }

        dialog.setLocationRelativeTo(f);
        dialog.setVisible(true);

    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new SettingsAction();
    }
    
    @Override
     public boolean isEnabled() {
         
         UserAccount user = DatabaseDataManager.getDatabaseDataManager().getLoggedUser();
        if (user == null) {
            return false;
        }
        return DatabaseDataManager.isAdmin(user);
    }
     

}
