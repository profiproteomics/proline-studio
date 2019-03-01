package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.gui.dialog.ApplicationSettingsDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "File", id = "fr.proline.studio.rsmexplorer.actions.SettingsAction")
@ActionRegistration(displayName = "#CTL_SettingsAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 400)
})
@Messages("CTL_SettingsAction=General Settings")
public final class SettingsAction extends AbstractAction implements ActionListener, ContextAwareAction {

    private static SettingsAction m_action = null;

    public SettingsAction() {
        putValue(Action.NAME, NbBundle.getMessage(SettingsAction.class, "CTL_SettingsAction"));
        m_action = this;
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Frame f = WindowManager.getDefault().getMainWindow();

        ApplicationSettingsDialog dialog = ApplicationSettingsDialog.getDialog(f);
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
}
