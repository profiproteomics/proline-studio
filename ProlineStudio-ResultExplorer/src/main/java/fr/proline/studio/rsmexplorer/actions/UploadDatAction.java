package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.gui.dialog.UploadDatDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "File", id = "fr.proline.studio.rsmexplorer.actions.UploadDatAction")
@ActionRegistration(displayName = "#CTL_UploadDatAction")
@ActionReferences({
    //@ActionReference(path = "Menu/File", position = 600)
})
@Messages("CTL_UploadDatAction=Upload .dat File(s)")
public final class UploadDatAction extends AbstractAction implements ActionListener, ContextAwareAction {

    private static UploadDatAction m_action = null;

    public UploadDatAction() {
        putValue(Action.NAME, NbBundle.getMessage(UploadDatAction.class, "CTL_UploadDatAction"));
        m_action = this;
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Frame f = WindowManager.getDefault().getMainWindow();

        UploadDatDialog dialog = UploadDatDialog.getDialog(f);
        if (dialog.isVisible()) {
            return;
        }

        dialog.setLocationRelativeTo(f);
        dialog.setVisible(true);

    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new UploadDatAction();
    }
}
